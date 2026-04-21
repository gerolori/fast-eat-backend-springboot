package com.gerolori.fasteat.web.users.dto;

import com.gerolori.fasteat.domain.entity.RoleName;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String email,
        String displayName,
        String phoneNumber,
        PaymentProfileResponse paymentProfile,
        SubscriptionStateResponse subscription,
        Set<RoleName> roles,
        Instant createdAt,
        Instant updatedAt
) {
}
