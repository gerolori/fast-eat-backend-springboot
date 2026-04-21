package com.gerolori.fasteat.web.users;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gerolori.fasteat.domain.entity.Role;
import com.gerolori.fasteat.domain.entity.RoleName;
import com.gerolori.fasteat.domain.entity.User;
import com.gerolori.fasteat.domain.repository.UserRepository;
import com.gerolori.fasteat.web.error.ProfileValidationException;
import com.gerolori.fasteat.web.error.ResourceNotFoundException;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class UserProfileServiceTest {

    private UserRepository userRepository;
    private UserProfileService userProfileService;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        userProfileService = new UserProfileService(userRepository, new ObjectMapper());
    }

    @Test
    void resolvesCurrentUserProfileFromPrincipalUserId() {
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");

        User user = new User();
        user.setId(userId);
        user.setEmail("customer@fasteat.test");
        user.setDisplayName("Giulia Rossi");
        user.setPhoneNumber("+39-333-1234567");
        user.setCreatedAt(Instant.parse("2026-04-01T09:00:00Z"));
        user.setUpdatedAt(Instant.parse("2026-04-20T10:15:00Z"));
        user.setRoles(Set.of(new Role(RoleName.CUSTOMER)));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        var response = userProfileService.getCurrentUserProfile(userId);

        assertThat(response.id()).isEqualTo(userId);
        assertThat(response.email()).isEqualTo("customer@fasteat.test");
        assertThat(response.displayName()).isEqualTo("Giulia Rossi");
        assertThat(response.phoneNumber()).isEqualTo("+39-333-1234567");
        assertThat(response.paymentProfile()).isNull();
        assertThat(response.subscription().enabled()).isFalse();
        assertThat(response.roles()).containsExactly(RoleName.CUSTOMER);
    }

    @Test
    void throwsNotFoundWhenPrincipalUserDoesNotExist() {
        UUID missingUserId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        when(userRepository.findById(missingUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userProfileService.getCurrentUserProfile(missingUserId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void rejectsRestrictedFieldsInPatchPayload() {
        UUID userId = UUID.fromString("33333333-3333-3333-3333-333333333333");

        assertThatThrownBy(() -> userProfileService.patchCurrentUserProfile(userId, new ObjectMapper().createObjectNode().put("roles", "ADMIN")))
                .isInstanceOf(ProfileValidationException.class)
                .hasMessage("Field 'roles' is not writable in profile updates");
    }

    @Test
    void validatesPaymentProfileFieldsAsTokenizedMetadataOnly() {
        UUID userId = UUID.fromString("55555555-5555-5555-5555-555555555555");

        User user = new User();
        user.setId(userId);
        user.setEmail("customer@fasteat.test");
        user.setRoles(Set.of(new Role(RoleName.CUSTOMER)));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        var payload = new ObjectMapper().createObjectNode()
                .put("paymentProvider", "stripe")
                .put("paymentMethodReference", "pm_tok_123")
                .put("paymentLast4", "4242");

        var response = userProfileService.patchCurrentUserProfile(userId, payload);

        assertThat(response.paymentProfile()).isNotNull();
        assertThat(response.paymentProfile().provider()).isEqualTo("stripe");
        assertThat(response.paymentProfile().methodReference()).isEqualTo("pm_tok_123");
        assertThat(response.paymentProfile().last4()).isEqualTo("4242");
    }

    @Test
    void updatesSubscriptionStateViaDedicatedToggleEndpointFlow() {
        UUID userId = UUID.fromString("77777777-7777-7777-7777-777777777777");

        User user = new User();
        user.setId(userId);
        user.setEmail("customer@fasteat.test");
        user.setRoles(Set.of(new Role(RoleName.CUSTOMER)));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        var response = userProfileService.updateSubscriptionState(userId, true);

        assertThat(response.subscription().enabled()).isTrue();
    }
}
