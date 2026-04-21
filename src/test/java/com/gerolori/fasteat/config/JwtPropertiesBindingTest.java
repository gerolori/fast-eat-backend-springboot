package com.gerolori.fasteat.config;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtPropertiesBindingTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void bindsJwtPropertiesFromConfiguredNamespace() {
        MapConfigurationPropertySource source = new MapConfigurationPropertySource(Map.of(
            "fasteat.security.jwt.secret", "test-secret-value",
            "fasteat.security.jwt.expiration-ms", "60000",
            "fasteat.security.jwt.issuer", "fasteat-test"
        ));

        JwtProperties properties = new Binder(source)
            .bind("fasteat.security.jwt", Bindable.of(JwtProperties.class))
            .orElseThrow(() -> new IllegalStateException("JWT properties were not bound"));

        assertThat(properties.secret()).isEqualTo("test-secret-value");
        assertThat(properties.expirationMs()).isEqualTo(60000L);
        assertThat(properties.issuer()).isEqualTo("fasteat-test");
    }

    @Test
    void validatesJwtPropertiesConstraints() {
        JwtProperties invalid = new JwtProperties("", 0L, "");

        assertThat(validator.validate(invalid)).hasSize(3);
    }
}
