package com.gerolori.fasteat.security;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityBeansConfigTest {

    @Test
    void exposesPasswordEncoderBeanAndHashesPasswords() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(SecurityBeansConfig.class)) {
            PasswordEncoder passwordEncoder = context.getBean(PasswordEncoder.class);

            assertThat(passwordEncoder).isInstanceOf(BCryptPasswordEncoder.class);

            String encoded = passwordEncoder.encode("fast-eat-password");
            assertThat(encoded).isNotEqualTo("fast-eat-password");
            assertThat(passwordEncoder.matches("fast-eat-password", encoded)).isTrue();
        }
    }
}
