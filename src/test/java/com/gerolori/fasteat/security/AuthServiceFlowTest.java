package com.gerolori.fasteat.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gerolori.fasteat.config.JwtProperties;
import com.gerolori.fasteat.domain.config.JpaAuditingConfig;
import com.gerolori.fasteat.domain.entity.RoleName;
import com.gerolori.fasteat.domain.entity.User;
import com.gerolori.fasteat.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        JpaAuditingConfig.class,
        SecurityBeansConfig.class,
        AuthService.class,
        JwtTokenService.class,
        JwtPrincipalMapper.class,
        AuthServiceFlowTest.JwtTestConfig.class
})
@ActiveProfiles("test")
class AuthServiceFlowTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void supportsRegisterLoginRefreshRotationAndLogoutRevocation() {
        AuthService.AuthResult registerResult = authService.register("flow-user@fasteat.test", "password123");

        assertThat(registerResult.accessToken()).isNotBlank();
        assertThat(registerResult.refreshToken()).isNotBlank();
        assertThat(registerResult.roles()).contains(RoleName.CUSTOMER);
        assertThat(registerResult.roles()).doesNotContain(RoleName.ADMIN);
        User persisted = userRepository.findByEmailIgnoreCase("flow-user@fasteat.test").orElseThrow();
        assertThat(persisted.getPasswordHash()).isNotEqualTo("password123");

        assertThatThrownBy(() -> authService.login("flow-user@fasteat.test", "wrong-password"))
                .isInstanceOf(JwtAuthenticationException.class)
                .extracting(ex -> ((JwtAuthenticationException) ex).getErrorCode())
                .isEqualTo("AUTH_INVALID_CREDENTIALS");

        AuthService.AuthResult loginResult = authService.login("flow-user@fasteat.test", "password123");
        assertThat(loginResult.refreshToken()).isNotEqualTo(registerResult.refreshToken());

        AuthService.AuthResult rotated = authService.refresh(loginResult.refreshToken());
        assertThat(rotated.refreshToken()).isNotEqualTo(loginResult.refreshToken());

        assertThatThrownBy(() -> authService.refresh(loginResult.refreshToken()))
                .isInstanceOf(JwtAuthenticationException.class)
                .extracting(ex -> ((JwtAuthenticationException) ex).getErrorCode())
                .isEqualTo("AUTH_INVALID_TOKEN");

        authService.logout(rotated.refreshToken());

        assertThatThrownBy(() -> authService.refresh(rotated.refreshToken()))
                .isInstanceOf(JwtAuthenticationException.class)
                .extracting(ex -> ((JwtAuthenticationException) ex).getErrorCode())
                .isEqualTo("AUTH_INVALID_TOKEN");
    }

    @TestConfiguration
    static class JwtTestConfig {

        @Bean
        JwtProperties jwtProperties() {
            return new JwtProperties("auth-flow-test-secret", 60000L, "fasteat-test");
        }
    }
}
