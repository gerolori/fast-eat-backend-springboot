package com.gerolori.fasteat.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "fasteat.security.jwt")
public record JwtProperties(
    @NotBlank String secret,
    @NotNull @Positive Long expirationMs,
    @NotBlank String issuer
) {
}
