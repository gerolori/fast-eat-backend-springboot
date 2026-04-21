package com.gerolori.fasteat.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gerolori.fasteat.config.JwtProperties;
import com.gerolori.fasteat.config.TraceIdResolver;
import com.gerolori.fasteat.domain.entity.RoleName;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

class BearerTokenAuthenticationFilterTest {

    private final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();

    private final JwtTokenService jwtTokenService = new JwtTokenService(
            new JwtProperties("security-filter-test-secret", 60000L, "fasteat-test"),
            new JwtPrincipalMapper()
    );

    private final BearerTokenAuthenticationFilter filter = new BearerTokenAuthenticationFilter(
            jwtTokenService,
            new ApiAuthenticationEntryPoint(objectMapper, new TraceIdResolver())
    );

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void passesThroughWhenAuthorizationHeaderIsMissing() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/orders");
        request.setServletPath("/orders");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void returnsUnauthorizedWhenAuthorizationHeaderIsMalformed() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/orders");
        request.setServletPath("/orders");
        request.addHeader("Authorization", "Token abc");

        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(401);
        JsonNode payload = objectMapper.readTree(response.getContentAsByteArray());
        assertThat(payload.get("error").asText()).isEqualTo("AUTH_MALFORMED_TOKEN");
    }

    @Test
    void populatesSecurityContextForValidBearerToken() throws ServletException, IOException {
        String token = jwtTokenService.generateAccessToken(new AuthPrincipal(UUID.randomUUID(), Set.of(RoleName.CUSTOMER)));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/orders");
        request.setServletPath("/orders");
        request.addHeader("Authorization", "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isInstanceOf(AuthPrincipal.class);
        assertThat(authentication.getAuthorities()).extracting("authority").contains("ROLE_CUSTOMER");
    }

    @Test
    void ignoresMalformedAuthorizationOnPublicRoutes() throws ServletException, IOException {
        assertPublicRouteBypassesBearerFilter("GET", "/health");
        assertPublicRouteBypassesBearerFilter("GET", "/readiness");
        assertPublicRouteBypassesBearerFilter("GET", "/restaurants");
        assertPublicRouteBypassesBearerFilter("GET", "/restaurants/restaurant-1");
        assertPublicRouteBypassesBearerFilter("GET", "/menus");
        assertPublicRouteBypassesBearerFilter("GET", "/menus/menu-1");
        assertPublicRouteBypassesBearerFilter("OPTIONS", "/orders");
    }

    private void assertPublicRouteBypassesBearerFilter(String method, String path) throws ServletException, IOException {
        SecurityContextHolder.clearContext();

        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setServletPath(path);
        request.addHeader("Authorization", "Token abc");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
