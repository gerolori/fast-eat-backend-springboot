package com.gerolori.fasteat.web.users;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gerolori.fasteat.domain.entity.Role;
import com.gerolori.fasteat.domain.entity.RoleName;
import com.gerolori.fasteat.domain.entity.User;
import com.gerolori.fasteat.domain.repository.UserRepository;
import com.gerolori.fasteat.web.error.ProfileValidationException;
import com.gerolori.fasteat.web.error.ResourceNotFoundException;
import com.gerolori.fasteat.web.users.dto.PaymentProfileResponse;
import com.gerolori.fasteat.web.users.dto.SubscriptionStateResponse;
import com.gerolori.fasteat.web.users.dto.UpdateUserProfileRequest;
import com.gerolori.fasteat.web.users.dto.UserProfileResponse;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileService {

    private static final Set<String> ALLOWED_FIELDS = Set.of(
            "displayName",
            "phoneNumber",
            "paymentProvider",
            "paymentMethodReference",
            "paymentLast4"
    );
    private static final Set<String> RESTRICTED_FIELDS = Set.of(
            "id",
            "userId",
            "role",
            "roles",
            "authorities",
            "password",
            "passwordHash",
            "cardNumber",
            "pan",
            "cvv",
            "expiryMonth",
            "expiryYear",
            "status",
            "enabled",
            "locked",
            "subscription",
            "subscriptionEnabled",
            "createdAt",
            "updatedAt"
    );

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public UserProfileService(UserRepository userRepository, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile(UUID userId) {
        return toProfileResponse(findRequiredUser(userId));
    }

    @Transactional
    public UserProfileResponse patchCurrentUserProfile(UUID userId, JsonNode payload) {
        validatePayloadShape(payload);

        Map<String, JsonNode> fields = payloadFields(payload);
        if (fields.isEmpty()) {
            throw new ProfileValidationException(
                    "PROFILE_EMPTY_UPDATE",
                    "At least one mutable profile field is required",
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    List.of(new ProfileErrorDetail("payload", "empty update"))
            );
        }

        for (String fieldName : fields.keySet()) {
            if (RESTRICTED_FIELDS.contains(fieldName)) {
                throw new ProfileValidationException(
                        "PROFILE_RESTRICTED_FIELD",
                        "Field '" + fieldName + "' is not writable in profile updates",
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        List.of(new ProfileErrorDetail(fieldName, "restricted field"))
                );
            }

            if (!ALLOWED_FIELDS.contains(fieldName)) {
                throw new ProfileValidationException(
                        "PROFILE_UNKNOWN_FIELD",
                        "Field '" + fieldName + "' is not supported in profile updates",
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        List.of(new ProfileErrorDetail(fieldName, "unknown field"))
                );
            }
        }

        UpdateUserProfileRequest request = objectMapper.convertValue(payload, UpdateUserProfileRequest.class);

        User user = findRequiredUser(userId);
        if (fields.containsKey("displayName")) {
            user.setDisplayName(validateDisplayName(request.displayName()));
        }
        if (fields.containsKey("phoneNumber")) {
            user.setPhoneNumber(validatePhoneNumber(request.phoneNumber()));
        }

        applyPaymentProfilePatch(user, request, fields);

        return toProfileResponse(userRepository.save(user));
    }

    @Transactional
    public UserProfileResponse updateSubscriptionState(UUID userId, boolean enabled) {
        User user = findRequiredUser(userId);
        user.setSubscriptionEnabled(enabled);
        return toProfileResponse(userRepository.save(user));
    }

    private UserProfileResponse toProfileResponse(User user) {
        Set<RoleName> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());

        PaymentProfileResponse paymentProfile = null;
        if (user.getPaymentProvider() != null && user.getPaymentMethodReference() != null) {
            paymentProfile = new PaymentProfileResponse(
                    user.getPaymentProvider(),
                    user.getPaymentMethodReference(),
                    user.getPaymentLast4()
            );
        }

        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getPhoneNumber(),
                paymentProfile,
                new SubscriptionStateResponse(user.isSubscriptionEnabled()),
                roles,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private User findRequiredUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void validatePayloadShape(JsonNode payload) {
        if (payload == null || !payload.isObject()) {
            throw new ProfileValidationException(
                    "PROFILE_INVALID_VALUE",
                    "Profile update payload must be a JSON object",
                    HttpStatus.BAD_REQUEST,
                    List.of(new ProfileErrorDetail("payload", "must be an object"))
            );
        }
    }

    private Map<String, JsonNode> payloadFields(JsonNode payload) {
        Iterator<Map.Entry<String, JsonNode>> iterator = payload.fields();
        Map<String, JsonNode> result = new java.util.LinkedHashMap<>();
        while (iterator.hasNext()) {
            Map.Entry<String, JsonNode> field = iterator.next();
            result.put(field.getKey(), field.getValue());
        }
        return result;
    }

    private String validateDisplayName(String displayName) {
        String normalized = normalize(displayName);
        if (normalized == null) {
            throw new ProfileValidationException(
                    "PROFILE_INVALID_VALUE",
                    "displayName must not be blank",
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    List.of(new ProfileErrorDetail("displayName", "must not be blank"))
            );
        }
        if (normalized.length() > 120) {
            throw new ProfileValidationException(
                    "PROFILE_INVALID_VALUE",
                    "displayName must be at most 120 characters",
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    List.of(new ProfileErrorDetail("displayName", "length must be <= 120"))
            );
        }
        return normalized;
    }

    private String validatePhoneNumber(String phoneNumber) {
        String normalized = normalize(phoneNumber);
        if (normalized == null) {
            throw new ProfileValidationException(
                    "PROFILE_INVALID_VALUE",
                    "phoneNumber must not be blank",
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    List.of(new ProfileErrorDetail("phoneNumber", "must not be blank"))
            );
        }
        if (!normalized.matches("^[+0-9()\\- ]{6,32}$")) {
            throw new ProfileValidationException(
                    "PROFILE_INVALID_VALUE",
                    "phoneNumber format is invalid",
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    List.of(new ProfileErrorDetail("phoneNumber", "invalid format"))
            );
        }
        return normalized;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private void applyPaymentProfilePatch(User user, UpdateUserProfileRequest request, Map<String, JsonNode> fields) {
        boolean touchesPaymentProfile = fields.containsKey("paymentProvider")
                || fields.containsKey("paymentMethodReference")
                || fields.containsKey("paymentLast4");

        if (!touchesPaymentProfile) {
            return;
        }

        String provider = fields.containsKey("paymentProvider")
                ? validatePaymentProvider(request.paymentProvider())
                : user.getPaymentProvider();
        String methodReference = fields.containsKey("paymentMethodReference")
                ? validatePaymentMethodReference(request.paymentMethodReference())
                : user.getPaymentMethodReference();
        String last4 = fields.containsKey("paymentLast4")
                ? validatePaymentLast4(request.paymentLast4())
                : user.getPaymentLast4();

        if (provider == null || methodReference == null) {
            throw new ProfileValidationException(
                    "PROFILE_INVALID_VALUE",
                    "paymentProvider and paymentMethodReference are both required for payment profile updates",
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    List.of(new ProfileErrorDetail("paymentProfile", "provider/reference pair required"))
            );
        }

        user.setPaymentProvider(provider);
        user.setPaymentMethodReference(methodReference);
        user.setPaymentLast4(last4);
    }

    private String validatePaymentProvider(String provider) {
        String normalized = normalize(provider);
        if (normalized == null) {
            throw new ProfileValidationException(
                    "PROFILE_INVALID_VALUE",
                    "paymentProvider must not be blank",
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    List.of(new ProfileErrorDetail("paymentProvider", "must not be blank"))
            );
        }
        if (normalized.length() > 64) {
            throw new ProfileValidationException(
                    "PROFILE_INVALID_VALUE",
                    "paymentProvider must be at most 64 characters",
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    List.of(new ProfileErrorDetail("paymentProvider", "length must be <= 64"))
            );
        }
        return normalized;
    }

    private String validatePaymentMethodReference(String reference) {
        String normalized = normalize(reference);
        if (normalized == null) {
            throw new ProfileValidationException(
                    "PROFILE_INVALID_VALUE",
                    "paymentMethodReference must not be blank",
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    List.of(new ProfileErrorDetail("paymentMethodReference", "must not be blank"))
            );
        }
        if (normalized.length() > 191) {
            throw new ProfileValidationException(
                    "PROFILE_INVALID_VALUE",
                    "paymentMethodReference must be at most 191 characters",
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    List.of(new ProfileErrorDetail("paymentMethodReference", "length must be <= 191"))
            );
        }
        return normalized;
    }

    private String validatePaymentLast4(String last4) {
        String normalized = normalize(last4);
        if (normalized == null) {
            return null;
        }
        if (!normalized.matches("^[0-9]{4}$")) {
            throw new ProfileValidationException(
                    "PROFILE_INVALID_VALUE",
                    "paymentLast4 must contain exactly 4 digits",
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    List.of(new ProfileErrorDetail("paymentLast4", "must be exactly 4 digits"))
            );
        }
        return normalized;
    }

    public record ProfileErrorDetail(String field, String reason) {
    }
}
