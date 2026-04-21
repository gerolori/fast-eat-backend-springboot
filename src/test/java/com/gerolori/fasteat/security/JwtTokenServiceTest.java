package com.gerolori.fasteat.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gerolori.fasteat.config.JwtProperties;
import com.gerolori.fasteat.domain.entity.RoleName;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtTokenServiceTest {

    private final JwtTokenService jwtTokenService = new JwtTokenService(
            new JwtProperties("unit-test-secret", 60000L, "fasteat-test"),
            new JwtPrincipalMapper()
    );

    @Test
    void generatesAndParsesJwtWithSubjectAndRolesClaims() {
        AuthPrincipal principal = new AuthPrincipal(UUID.randomUUID(), Set.of(RoleName.ADMIN, RoleName.CUSTOMER));

        String token = jwtTokenService.generateAccessToken(principal);
        AuthPrincipal parsed = jwtTokenService.parseAccessToken(token);

        assertThat(parsed.userId()).isEqualTo(principal.userId());
        assertThat(parsed.roles()).containsExactlyInAnyOrder(RoleName.ADMIN, RoleName.CUSTOMER);
    }

    @Test
    void rejectsTamperedJwtToken() {
        AuthPrincipal principal = new AuthPrincipal(UUID.randomUUID(), Set.of(RoleName.CUSTOMER));
        String token = jwtTokenService.generateAccessToken(principal);

        String tampered = token.substring(0, token.length() - 1) + "x";

        assertThatThrownBy(() -> jwtTokenService.parseAccessToken(tampered))
                .isInstanceOf(JwtAuthenticationException.class)
                .hasMessageContaining("invalid")
                .extracting(ex -> ((JwtAuthenticationException) ex).getErrorCode())
                .isEqualTo("AUTH_INVALID_TOKEN");
    }
}
