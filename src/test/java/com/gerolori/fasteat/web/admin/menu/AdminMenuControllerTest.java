package com.gerolori.fasteat.web.admin.menu;

import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gerolori.fasteat.config.TraceIdResolver;
import com.gerolori.fasteat.web.error.GlobalApiExceptionHandler;
import com.gerolori.fasteat.web.error.ResourceNotFoundException;
import com.gerolori.fasteat.web.menu.dto.MoneyResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class AdminMenuControllerTest {

    private AdminMenuService adminMenuService;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        adminMenuService = Mockito.mock(AdminMenuService.class);
        objectMapper = new ObjectMapper();

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new AdminMenuController(adminMenuService))
                .setControllerAdvice(new GlobalApiExceptionHandler(new TraceIdResolver()))
                .setValidator(validator)
                .build();

        authenticateAs("ROLE_ADMIN");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createMenuReturnsCreated() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        UUID menuId = UUID.randomUUID();
        AdminMenuResponse response = new AdminMenuResponse(
                menuId,
                restaurantId,
                "Chicken Bowl",
                "Balanced meal",
                "Detail",
                "MAIN",
                new MoneyResponse("8.99", "USD"),
                true,
                true,
                "https://cdn.example.com/menu.png",
                Instant.parse("2026-04-21T10:00:00Z")
        );

        when(adminMenuService.createMenu(eq(restaurantId), Mockito.any())).thenReturn(response);

        mockMvc.perform(post("/admin/restaurants/{restaurantId}/menus", restaurantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Chicken Bowl",
                                  "summary": "Balanced meal",
                                  "description": "Detail",
                                  "category": "MAIN",
                                  "price": 8.99,
                                  "available": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.menuId").value(menuId.toString()))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    void deleteMenuReturnsNoContent() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        UUID menuId = UUID.randomUUID();

        mockMvc.perform(delete("/admin/restaurants/{restaurantId}/menus/{menuId}", restaurantId, menuId))
                .andExpect(status().isNoContent());

        verify(adminMenuService).deleteMenu(restaurantId, menuId);
    }

    @Test
    void updateAvailabilityAndStatusAreMapped() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        UUID menuId = UUID.randomUUID();
        AdminMenuResponse soldOut = baseResponse(menuId, restaurantId, false, true);
        AdminMenuResponse inactive = baseResponse(menuId, restaurantId, false, false);

        when(adminMenuService.setMenuAvailability(restaurantId, menuId, false)).thenReturn(soldOut);
        when(adminMenuService.setMenuActive(restaurantId, menuId, false)).thenReturn(inactive);

                mockMvc.perform(patch("/admin/restaurants/{restaurantId}/menus/{menuId}/availability", restaurantId, menuId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AdminMenuAvailabilityRequest(false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAvailable").value(false));

                mockMvc.perform(patch("/admin/restaurants/{restaurantId}/menus/{menuId}/status", restaurantId, menuId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AdminMenuStatusRequest(false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));
    }

    @Test
    void mapsNotFoundToSharedEnvelope() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        UUID menuId = UUID.randomUUID();
        when(adminMenuService.updateMenu(eq(restaurantId), eq(menuId), Mockito.any()))
                .thenThrow(new ResourceNotFoundException("Menu not found"));

        mockMvc.perform(put("/admin/restaurants/{restaurantId}/menus/{menuId}", restaurantId, menuId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Updated",
                                  "price": 9.99
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.traceId", not(blankOrNullString())));
    }

    @Test
    void rejectsNonAdminRole() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        authenticateAs("ROLE_CUSTOMER");

        mockMvc.perform(post("/admin/restaurants/{restaurantId}/menus", restaurantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Chicken Bowl",
                                  "price": 8.99
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("AUTHZ_INSUFFICIENT_ROLE"));

        verifyNoInteractions(adminMenuService);
    }

    private void authenticateAs(String authority) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                UUID.randomUUID(),
                "n/a",
                List.of(new SimpleGrantedAuthority(authority))
        ));
    }

    private AdminMenuResponse baseResponse(UUID menuId, UUID restaurantId, boolean available, boolean active) {
        return new AdminMenuResponse(
                menuId,
                restaurantId,
                "Chicken Bowl",
                "Balanced meal",
                "Detail",
                "MAIN",
                new MoneyResponse("8.99", "USD"),
                available,
                active,
                "https://cdn.example.com/menu.png",
                Instant.parse("2026-04-21T10:00:00Z")
        );
    }
}
