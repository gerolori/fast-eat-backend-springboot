package com.gerolori.fasteat.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class SecurityRouteMatcherTest {

    @Test
    void marksExpectedPublicRoutesAsPublic() {
        assertThat(isPublic("GET", "/auth/login")).isTrue();
        assertThat(isPublic("GET", "/v3/api-docs/openapi.json")).isTrue();
        assertThat(isPublic("GET", "/swagger-ui/index.html")).isTrue();
        assertThat(isPublic("GET", "/swagger-ui.html")).isTrue();
        assertThat(isPublic("GET", "/error")).isTrue();
        assertThat(isPublic("GET", "/health")).isTrue();
        assertThat(isPublic("GET", "/readiness")).isTrue();
        assertThat(isPublic("GET", "/restaurants")).isTrue();
        assertThat(isPublic("GET", "/restaurants/abc")).isTrue();
        assertThat(isPublic("GET", "/menus")).isTrue();
        assertThat(isPublic("GET", "/menus/abc")).isTrue();
        assertThat(isPublic("OPTIONS", "/orders")).isTrue();
    }

    @Test
    void keepsNonGetBrowseRoutesNonPublic() {
        assertThat(isPublic("POST", "/restaurants")).isFalse();
        assertThat(isPublic("POST", "/menus")).isFalse();
        assertThat(isPublic("GET", "/orders")).isFalse();
    }

    private boolean isPublic(String method, String path) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setServletPath(path);
        return SecurityRouteMatcher.isPublicRoute(request);
    }
}
