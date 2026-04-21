package com.gerolori.fasteat.web.users.dto;

public record PaymentProfileResponse(
        String provider,
        String methodReference,
        String last4
) {
}
