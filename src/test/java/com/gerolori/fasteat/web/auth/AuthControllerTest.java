package com.gerolori.fasteat.web.auth;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gerolori.fasteat.config.TraceIdResolver;
import com.gerolori.fasteat.domain.entity.RoleName;
import com.gerolori.fasteat.security.AuthService;
import com.gerolori.fasteat.security.JwtAuthenticationException;
import com.gerolori.fasteat.web.error.GlobalApiExceptionHandler;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AuthControllerTest {

    private AuthService authService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        authService = Mockito.mock(AuthService.class);
        AuthController controller = new AuthController(authService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalApiExceptionHandler(new TraceIdResolver()))
                .build();
    }

    @Test
    void registerAndLoginEndpointsReturnAuthPayload() throws Exception {
        AuthService.AuthResult authResult = new AuthService.AuthResult(
                "access-token",
                "refresh-token",
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                Set.of(RoleName.CUSTOMER)
        );

        when(authService.register(anyString(), anyString())).thenReturn(authResult);
        when(authService.login(anyString(), anyString())).thenReturn(authResult);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "controller@fasteat.test",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "controller@fasteat.test",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.principal.userId").value("11111111-1111-1111-1111-111111111111"));
    }

    @Test
    void refreshAndLogoutEndpointsSupportTokenLifecycleRoutes() throws Exception {
        AuthService.AuthResult refreshResult = new AuthService.AuthResult(
                "new-access-token",
                "new-refresh-token",
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                Set.of(RoleName.CUSTOMER)
        );

        when(authService.refresh("old-refresh-token")).thenReturn(refreshResult);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "old-refresh-token"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"));

        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "new-refresh-token"
                                }
                                """))
                .andExpect(status().isNoContent());
    }

    @Test
    void mapsAuthExceptionsToSharedErrorEnvelope() throws Exception {
        when(authService.login(anyString(), anyString()))
                .thenThrow(new JwtAuthenticationException("AUTH_INVALID_CREDENTIALS", "Invalid email or password"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "controller@fasteat.test",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("AUTH_INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.traceId").isString());
    }
}
