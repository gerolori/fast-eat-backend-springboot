package com.gerolori.fasteat.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.util.matcher.RequestMatcher;

public final class SecurityRouteMatcher {

    private static final RequestMatcher PUBLIC_ROUTE_MATCHER = SecurityRouteMatcher::isPublicRoute;

    private SecurityRouteMatcher() {
    }

    public static RequestMatcher publicRouteMatcher() {
        return PUBLIC_ROUTE_MATCHER;
    }

    public static boolean isPublicRoute(HttpServletRequest request) {
        String path = request.getServletPath();
        if (path == null || path.isBlank()) {
            path = request.getRequestURI();
            String contextPath = request.getContextPath();
            if (contextPath != null && !contextPath.isBlank() && path != null && path.startsWith(contextPath)) {
                path = path.substring(contextPath.length());
            }
        }

        if (path == null || path.isBlank()) {
            return false;
        }

        if (path.startsWith("/auth/")
                || path.equals("/auth")
                || path.startsWith("/v3/api-docs/")
                || path.equals("/v3/api-docs")
                || path.startsWith("/swagger-ui/")
                || path.equals("/swagger-ui")
                || path.equals("/swagger-ui.html")
                || path.equals("/error")
                || path.equals("/health")
                || path.equals("/readiness")) {
            return true;
        }

        String method = request.getMethod();
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        if (!"GET".equalsIgnoreCase(method)) {
            return false;
        }

        return path.equals("/restaurants")
                || path.startsWith("/restaurants/")
                || path.equals("/menus")
                || path.startsWith("/menus/");
    }
}
