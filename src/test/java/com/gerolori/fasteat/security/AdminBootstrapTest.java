package com.gerolori.fasteat.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.gerolori.fasteat.domain.config.JpaAuditingConfig;
import com.gerolori.fasteat.domain.entity.Role;
import com.gerolori.fasteat.domain.entity.RoleName;
import com.gerolori.fasteat.domain.entity.User;
import com.gerolori.fasteat.domain.repository.UserRepository;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        JpaAuditingConfig.class,
        SecurityBeansConfig.class,
        AdminBootstrap.class
})
@EnableConfigurationProperties(AdminBootstrapProperties.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "fasteat.security.admin-bootstrap.enabled=true",
        "fasteat.security.admin-bootstrap.admin-emails=bootstrap-admin@fasteat.test",
        "fasteat.security.admin-bootstrap.create-missing-users=true",
        "fasteat.security.admin-bootstrap.default-password=bootstrap-pass-123"
})
class AdminBootstrapTest {

    @Autowired
    private AdminBootstrap adminBootstrap;

    @Autowired
    private UserRepository userRepository;

    @Test
    void bootstrapsConfiguredAdminAndKeepsRoleAssignmentScopedToConfiguredEmails() throws Exception {
        User regularUser = new User();
        regularUser.setEmail("regular-user@fasteat.test");
        regularUser.setPasswordHash("hash");
        userRepository.save(regularUser);

        adminBootstrap.run(new DefaultApplicationArguments(new String[0]));

        User bootstrappedAdmin = userRepository.findByEmailIgnoreCase("bootstrap-admin@fasteat.test").orElseThrow();
        assertThat(bootstrappedAdmin.getPasswordHash()).isNotEqualTo("bootstrap-pass-123");
        assertThat(roleNames(bootstrappedAdmin)).containsExactlyInAnyOrder(RoleName.ADMIN, RoleName.CUSTOMER);

        User unchangedRegularUser = userRepository.findByEmailIgnoreCase("regular-user@fasteat.test").orElseThrow();
        assertThat(roleNames(unchangedRegularUser)).doesNotContain(RoleName.ADMIN);
    }

    private Set<RoleName> roleNames(User user) {
        return user.getRoles().stream().map(Role::getName).collect(java.util.stream.Collectors.toSet());
    }
}
