package com.gerolori.fasteat.web.users;

import com.fasterxml.jackson.databind.JsonNode;
import com.gerolori.fasteat.security.AuthPrincipal;
import com.gerolori.fasteat.security.JwtAuthenticationException;
import com.gerolori.fasteat.web.users.dto.UpdateSubscriptionStateRequest;
import com.gerolori.fasteat.web.users.dto.UserProfileResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/me")
    public UserProfileResponse getMe(Authentication authentication) {
        return userProfileService.getCurrentUserProfile(extractPrincipal(authentication).userId());
    }

    @PatchMapping("/me")
    public UserProfileResponse patchMe(Authentication authentication, @RequestBody JsonNode request) {
        return userProfileService.patchCurrentUserProfile(extractPrincipal(authentication).userId(), request);
    }

    @PatchMapping("/me/subscription")
    public UserProfileResponse updateSubscription(
            Authentication authentication,
            @Valid @RequestBody UpdateSubscriptionStateRequest request
    ) {
        return userProfileService.updateSubscriptionState(extractPrincipal(authentication).userId(), request.enabled());
    }

    private AuthPrincipal extractPrincipal(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof AuthPrincipal principal) {
            return principal;
        }

        throw new JwtAuthenticationException("AUTH_INVALID_TOKEN", "Authentication principal is invalid");
    }
}
