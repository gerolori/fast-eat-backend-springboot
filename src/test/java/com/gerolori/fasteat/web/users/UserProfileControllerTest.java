package com.gerolori.fasteat.web.users;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gerolori.fasteat.config.TraceIdResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.gerolori.fasteat.domain.entity.RoleName;
import com.gerolori.fasteat.security.AuthPrincipal;
import com.gerolori.fasteat.web.error.GlobalApiExceptionHandler;
import com.gerolori.fasteat.web.users.dto.PaymentProfileResponse;
import com.gerolori.fasteat.web.users.dto.SubscriptionStateResponse;
import com.gerolori.fasteat.web.users.dto.UserProfileResponse;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class UserProfileControllerTest {

    private UserProfileService userProfileService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        userProfileService = Mockito.mock(UserProfileService.class);
        UserProfileController controller = new UserProfileController(userProfileService);
        ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalApiExceptionHandler(new TraceIdResolver()))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void getMeReturnsPrincipalScopedProfile() throws Exception {
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        when(userProfileService.getCurrentUserProfile(userId)).thenReturn(new UserProfileResponse(
                userId,
                "customer@fasteat.test",
                "Giulia Rossi",
                "+39-333-1234567",
                null,
                new SubscriptionStateResponse(false),
                Set.of(RoleName.CUSTOMER),
                Instant.parse("2026-04-01T09:00:00Z"),
                Instant.parse("2026-04-20T10:15:00Z")
        ));

        AuthPrincipal principal = new AuthPrincipal(userId, Set.of(RoleName.CUSTOMER));
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.authorities()
        );

        mockMvc.perform(get("/users/me").principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("11111111-1111-1111-1111-111111111111"))
                .andExpect(jsonPath("$.email").value("customer@fasteat.test"))
                .andExpect(jsonPath("$.displayName").value("Giulia Rossi"));
    }

    @Test
    void patchMeDelegatesToServiceUsingPrincipalUserId() throws Exception {
        UUID userId = UUID.fromString("44444444-4444-4444-4444-444444444444");
        when(userProfileService.patchCurrentUserProfile(Mockito.eq(userId), Mockito.any())).thenReturn(new UserProfileResponse(
                userId,
                "customer@fasteat.test",
                "Giulia R.",
                "+39-333-0000000",
                new PaymentProfileResponse("stripe", "pm_tok_123", "4242"),
                new SubscriptionStateResponse(false),
                Set.of(RoleName.CUSTOMER),
                Instant.parse("2026-04-01T09:00:00Z"),
                Instant.parse("2026-04-20T10:15:00Z")
        ));

        AuthPrincipal principal = new AuthPrincipal(userId, Set.of(RoleName.CUSTOMER));
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.authorities()
        );

        mockMvc.perform(patch("/users/me")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                {
                                  "displayName": "Giulia R.",
                                  "paymentProvider": "stripe",
                                  "paymentMethodReference": "pm_tok_123",
                                  "paymentLast4": "4242"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Giulia R."))
                .andExpect(jsonPath("$.paymentProfile.provider").value("stripe"))
                .andExpect(jsonPath("$.paymentProfile.methodReference").value("pm_tok_123"));
    }

    @Test
    void updateSubscriptionTogglesStateForPrincipalScopedUser() throws Exception {
        UUID userId = UUID.fromString("66666666-6666-6666-6666-666666666666");
        when(userProfileService.updateSubscriptionState(userId, true)).thenReturn(new UserProfileResponse(
                userId,
                "customer@fasteat.test",
                "Giulia R.",
                "+39-333-0000000",
                null,
                new SubscriptionStateResponse(true),
                Set.of(RoleName.CUSTOMER),
                Instant.parse("2026-04-01T09:00:00Z"),
                Instant.parse("2026-04-20T10:15:00Z")
        ));

        AuthPrincipal principal = new AuthPrincipal(userId, Set.of(RoleName.CUSTOMER));
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.authorities()
        );

        mockMvc.perform(patch("/users/me/subscription")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "enabled": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subscription.enabled").value(true));
    }
}
