package com.gerolori.fasteat.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gerolori.fasteat.domain.entity.Role;
import com.gerolori.fasteat.domain.entity.RoleName;
import com.gerolori.fasteat.domain.entity.User;
import com.gerolori.fasteat.domain.repository.RoleRepository;
import com.gerolori.fasteat.domain.repository.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AuthSecurityIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void authEndpointsSupportRefreshRotationAndLogoutRevocation() throws Exception {
        String email = uniqueEmail("flow");
        String password = "password123";

        TokenPair registerTokens = register(email, password);
        TokenPair loginTokens = login(email, password);

        assertThat(loginTokens.refreshToken()).isNotEqualTo(registerTokens.refreshToken());

        TokenPair rotatedTokens = refresh(loginTokens.refreshToken());
        assertThat(rotatedTokens.refreshToken()).isNotEqualTo(loginTokens.refreshToken());

        refreshExpectUnauthorized(loginTokens.refreshToken(), "AUTH_INVALID_TOKEN");

        logout(rotatedTokens.refreshToken());
        refreshExpectUnauthorized(rotatedTokens.refreshToken(), "AUTH_INVALID_TOKEN");
    }

    @Test
    void bearerValidationRejectsMalformedProtectedRequestsAndBypassesPublicRoutes() throws Exception {
        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Token malformed"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("AUTH_MALFORMED_TOKEN"));

        mockMvc.perform(get("/restaurants")
                        .header("Authorization", "Token malformed"))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isNotEqualTo(401));
    }

    @Test
    void validBearerTokenAuthenticatesProtectedCustomerRoute() throws Exception {
        String email = uniqueEmail("customer");
        TokenPair registerTokens = register(email, "password123");

        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + registerTokens.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void adminAndCustomerAuthorizationBehavesUnderSecurityChain() throws Exception {
        String email = uniqueEmail("authz");
        String password = "password123";

        register(email, password);
        TokenPair customerTokens = login(email, password);

        UUID missingRestaurantId = UUID.randomUUID();

        mockMvc.perform(delete("/admin/restaurants/{restaurantId}", missingRestaurantId)
                        .header("Authorization", "Bearer " + customerTokens.accessToken()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("AUTHZ_INSUFFICIENT_ROLE"));

        promoteToAdmin(email);
        TokenPair adminTokens = login(email, password);

        mockMvc.perform(delete("/admin/restaurants/{restaurantId}", missingRestaurantId)
                        .header("Authorization", "Bearer " + adminTokens.accessToken()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));
    }

    private TokenPair register(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isCreated())
                .andReturn();

        return tokenPair(result);
    }

    private TokenPair login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();

        return tokenPair(result);
    }

    private TokenPair refresh(String refreshToken) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(refreshToken)))
                .andExpect(status().isOk())
                .andReturn();

        return tokenPair(result);
    }

    private void refreshExpectUnauthorized(String refreshToken, String errorCode) throws Exception {
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(refreshToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value(errorCode));
    }

    private void logout(String refreshToken) throws Exception {
        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(refreshToken)))
                .andExpect(status().isNoContent());
    }

    private TokenPair tokenPair(MvcResult result) throws Exception {
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return new TokenPair(
                json.path("accessToken").asText(),
                json.path("refreshToken").asText()
        );
    }

    private void promoteToAdmin(String email) {
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.ADMIN)));
        user.getRoles().add(adminRole);
        userRepository.save(user);
    }

    private String uniqueEmail(String prefix) {
        return prefix + "-" + UUID.randomUUID() + "@fasteat.test";
    }

    private record TokenPair(String accessToken, String refreshToken) {
    }
}
