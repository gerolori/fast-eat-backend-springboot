package com.gerolori.fasteat.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootTest
@ActiveProfiles("test")
class SecurityPublicRoutePolicyIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void bypassesMalformedAuthorizationForPublicGetAndOptionsRoutes() throws Exception {
        mockMvc.perform(get("/restaurants")
                        .header("Authorization", "Token malformed"))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isNotEqualTo(401));

        mockMvc.perform(options("/orders")
                        .header("Authorization", "Token malformed"))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isNotEqualTo(401));
    }

    @Test
    void keepsMalformedAuthorizationUnauthorizedOnProtectedRoutes() throws Exception {
        mockMvc.perform(get("/orders")
                        .header("Authorization", "Token malformed"))
                .andExpect(status().isUnauthorized());
    }
}
