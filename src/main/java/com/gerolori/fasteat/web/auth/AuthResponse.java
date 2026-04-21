package com.gerolori.fasteat.web.auth;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        PrincipalSummary principal
) {
}
