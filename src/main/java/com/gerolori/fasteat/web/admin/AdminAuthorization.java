package com.gerolori.fasteat.web.admin;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class AdminAuthorization {

    private static final String ADMIN_AUTHORITY = "ROLE_ADMIN";

    private AdminAuthorization() {
    }

    public static void requireAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Admin role required");
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> ADMIN_AUTHORITY.equals(authority.getAuthority()));
        if (!isAdmin) {
            throw new AccessDeniedException("Admin role required");
        }
    }
}
