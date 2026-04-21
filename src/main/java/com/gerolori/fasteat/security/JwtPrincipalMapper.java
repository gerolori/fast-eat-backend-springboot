package com.gerolori.fasteat.security;

import com.gerolori.fasteat.domain.entity.RoleName;
import io.jsonwebtoken.Claims;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class JwtPrincipalMapper {

    public AuthPrincipal fromClaims(Claims claims) {
        String subject = claims.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new JwtAuthenticationException("AUTH_INVALID_TOKEN", "Token subject is missing");
        }

        UUID userId;
        try {
            userId = UUID.fromString(subject);
        } catch (IllegalArgumentException exception) {
            throw new JwtAuthenticationException("AUTH_INVALID_TOKEN", "Token subject is invalid", exception);
        }

        Object rolesClaim = claims.get("roles");
        if (!(rolesClaim instanceof Collection<?> collection) || collection.isEmpty()) {
            throw new JwtAuthenticationException("AUTH_INVALID_TOKEN", "Token roles claim is missing");
        }

        Set<RoleName> roles = collection.stream()
                .map(this::toRole)
                .collect(Collectors.toUnmodifiableSet());

        return new AuthPrincipal(userId, roles);
    }

    private RoleName toRole(Object value) {
        if (!(value instanceof String roleName)) {
            throw new JwtAuthenticationException("AUTH_INVALID_TOKEN", "Token roles claim is invalid");
        }

        try {
            return RoleName.valueOf(roleName);
        } catch (IllegalArgumentException exception) {
            throw new JwtAuthenticationException("AUTH_INVALID_TOKEN", "Token role is unknown", exception);
        }
    }
}
