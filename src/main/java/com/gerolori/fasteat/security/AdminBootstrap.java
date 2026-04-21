package com.gerolori.fasteat.security;

import com.gerolori.fasteat.domain.entity.Role;
import com.gerolori.fasteat.domain.entity.RoleName;
import com.gerolori.fasteat.domain.entity.User;
import com.gerolori.fasteat.domain.repository.RoleRepository;
import com.gerolori.fasteat.domain.repository.UserRepository;
import java.util.Set;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile({"local", "test"})
@ConditionalOnProperty(name = "fasteat.security.admin-bootstrap.enabled", havingValue = "true", matchIfMissing = true)
public class AdminBootstrap implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminBootstrapProperties properties;

    public AdminBootstrap(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AdminBootstrapProperties properties
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Set<String> bootstrapAdmins = properties.normalizedAdminEmails();
        if (bootstrapAdmins.isEmpty()) {
            return;
        }

        Role adminRole = ensureRole(RoleName.ADMIN);
        Role customerRole = ensureRole(RoleName.CUSTOMER);
        for (String adminEmail : bootstrapAdmins) {
            bootstrapAdminUser(adminEmail, adminRole, customerRole);
        }
    }

    private Role ensureRole(RoleName roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(roleName)));
    }

    private void bootstrapAdminUser(String email, Role adminRole, Role customerRole) {
        userRepository.findByEmailIgnoreCase(email)
                .ifPresentOrElse(
                        user -> ensureBootstrapRoles(user, adminRole, customerRole),
                        () -> createBootstrapAdminIfConfigured(email, adminRole, customerRole)
                );
    }

    private void ensureBootstrapRoles(User user, Role adminRole, Role customerRole) {
        boolean changed = false;
        if (!hasRole(user, customerRole.getName())) {
            user.getRoles().add(customerRole);
            changed = true;
        }
        if (!hasRole(user, adminRole.getName())) {
            user.getRoles().add(adminRole);
            changed = true;
        }

        if (changed) {
            userRepository.save(user);
        }
    }

    private void createBootstrapAdminIfConfigured(String email, Role adminRole, Role customerRole) {
        if (!properties.isCreateMissingUsers()) {
            return;
        }

        String rawPassword = properties.getDefaultPassword();
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalStateException(
                    "fasteat.security.admin-bootstrap.default-password must be configured when create-missing-users=true"
            );
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.getRoles().add(adminRole);
        user.getRoles().add(customerRole);
        userRepository.save(user);
    }

    private boolean hasRole(User user, RoleName roleName) {
        return user.getRoles().stream()
                .map(Role::getName)
                .anyMatch(roleName::equals);
    }
}
