package com.gerolori.fasteat.web.users.dto;

public record UpdateUserProfileRequest(
        String displayName,
        String phoneNumber,
        String paymentProvider,
        String paymentMethodReference,
        String paymentLast4
) {
}
