package com.gerolori.fasteat.web.admin.restaurant;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class AdminRestaurantAccessGuardTest {

    private final AdminRestaurantAccessGuard guard = new AdminRestaurantAccessGuard();

    @Test
    void allowsAdminAuthentication() {
        var auth = new TestingAuthenticationToken("admin", "n/a", "ROLE_ADMIN");
        auth.setAuthenticated(true);

        assertThatCode(() -> guard.requireAdmin(auth)).doesNotThrowAnyException();
    }

    @Test
    void rejectsMissingAuthentication() {
        assertThatThrownBy(() -> guard.requireAdmin(null))
                .hasMessageContaining("Authentication required");
    }

    @Test
    void rejectsNonAdminAuthentication() {
        var auth = new TestingAuthenticationToken(
                "user",
                "n/a",
                java.util.List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
        auth.setAuthenticated(true);

        assertThatThrownBy(() -> guard.requireAdmin(auth))
                .hasMessageContaining("Admin role required");
    }
}
