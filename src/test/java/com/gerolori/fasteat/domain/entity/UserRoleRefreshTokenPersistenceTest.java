package com.gerolori.fasteat.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.gerolori.fasteat.domain.config.JpaAuditingConfig;
import com.gerolori.fasteat.domain.repository.RefreshTokenRepository;
import com.gerolori.fasteat.domain.repository.RoleRepository;
import com.gerolori.fasteat.domain.repository.UserRepository;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfig.class)
@ActiveProfiles("test")
class UserRoleRefreshTokenPersistenceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    void persistsUserRoleMappingAndLooksUpByEmailIgnoringCase() {
        Role customerRole = roleRepository.save(new Role(RoleName.CUSTOMER));

        User user = new User();
        user.setEmail("customer@fasteat.test");
        user.setPasswordHash("hashed-password");
        user.getRoles().add(customerRole);
        userRepository.saveAndFlush(user);

        User persisted = userRepository.findByEmailIgnoreCase("CUSTOMER@FASTEAT.TEST").orElseThrow();

        assertThat(persisted.getId()).isNotNull();
        assertThat(persisted.getCreatedAt()).isNotNull();
        assertThat(persisted.getRoles()).extracting(Role::getName).containsExactly(RoleName.CUSTOMER);
    }

    @Test
    void persistsRefreshTokenAndFindsOnlyNonRevokedTokens() {
        Role customerRole = roleRepository.save(new Role(RoleName.CUSTOMER));

        User user = new User();
        user.setEmail("refresh@fasteat.test");
        user.setPasswordHash("hashed-password");
        user.getRoles().add(customerRole);
        User persistedUser = userRepository.saveAndFlush(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(persistedUser);
        refreshToken.setTokenHash("abc123");
        refreshToken.setExpiresAt(Instant.now().plusSeconds(600));
        refreshTokenRepository.saveAndFlush(refreshToken);

        assertThat(refreshTokenRepository.findByTokenHashAndRevokedAtIsNull("abc123")).isPresent();

        refreshToken.setRevokedAt(Instant.now());
        refreshTokenRepository.saveAndFlush(refreshToken);

        assertThat(refreshTokenRepository.findByTokenHashAndRevokedAtIsNull("abc123")).isEmpty();
    }
}
