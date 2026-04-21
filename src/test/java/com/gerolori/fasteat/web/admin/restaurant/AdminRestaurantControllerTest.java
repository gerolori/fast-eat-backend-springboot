package com.gerolori.fasteat.web.admin.restaurant;

import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
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
import com.gerolori.fasteat.web.error.BusinessRuleViolationException;
import com.gerolori.fasteat.web.error.GlobalApiExceptionHandler;
import com.gerolori.fasteat.web.error.ResourceNotFoundException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@ExtendWith(MockitoExtension.class)
class AdminRestaurantControllerTest {

    @Mock
    private AdminRestaurantAccessGuard accessGuard;

    @Mock
    private AdminRestaurantService adminRestaurantService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new AdminRestaurantController(accessGuard, adminRestaurantService))
                .setControllerAdvice(new GlobalApiExceptionHandler(new TraceIdResolver()))
                .setValidator(validator)
                .build();
    }

    @Test
    void createsRestaurantForAdminRequest() throws Exception {
        AdminRestaurantRequest request = requestPayload();
        AdminRestaurantResponse response = responsePayload();

        doNothing().when(accessGuard).requireAdmin(any());
        when(adminRestaurantService.createRestaurant(any(AdminRestaurantRequest.class))).thenReturn(response);

        mockMvc.perform(post("/admin/restaurants")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.restaurantId").value(response.restaurantId().toString()))
                .andExpect(jsonPath("$.visible").value(true));
    }

    @Test
    void updatesRestaurantAndMapsConflictEnvelope() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        AdminRestaurantRequest request = requestPayload();
        doNothing().when(accessGuard).requireAdmin(any());
        when(adminRestaurantService.updateRestaurant(eq(restaurantId), any(AdminRestaurantRequest.class)))
                .thenThrow(new BusinessRuleViolationException("RESTAURANT_NAME_CONFLICT", "Restaurant name already exists for this owner"));

        mockMvc.perform(put("/admin/restaurants/{restaurantId}", restaurantId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("RESTAURANT_NAME_CONFLICT"))
                .andExpect(jsonPath("$.traceId", not(blankOrNullString())));
    }

    @Test
    void deletesRestaurant() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        doNothing().when(accessGuard).requireAdmin(any());

        mockMvc.perform(delete("/admin/restaurants/{restaurantId}", restaurantId))
                .andExpect(status().isNoContent());

        verify(adminRestaurantService).deleteRestaurant(restaurantId);
    }

    @Test
    void updatesStatusAndVisibility() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        AdminRestaurantResponse response = responsePayload();
        doNothing().when(accessGuard).requireAdmin(any());
        when(adminRestaurantService.updateAvailability(restaurantId, false)).thenReturn(response);
        when(adminRestaurantService.updateVisibility(restaurantId, false)).thenReturn(response);

        mockMvc.perform(patch("/admin/restaurants/{restaurantId}/status", restaurantId)
                        .contentType("application/json")
                        .content("{\"available\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.restaurantId").value(response.restaurantId().toString()));

        mockMvc.perform(patch("/admin/restaurants/{restaurantId}/visibility", restaurantId)
                        .contentType("application/json")
                        .content("{\"visible\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.restaurantId").value(response.restaurantId().toString()));
    }

    @Test
    void rejectsMissingAvailabilityFieldInStatusPatch() throws Exception {
        UUID restaurantId = UUID.randomUUID();

        mockMvc.perform(patch("/admin/restaurants/{restaurantId}/status", restaurantId)
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

        verifyNoInteractions(adminRestaurantService);
    }

    @Test
    void rejectsMissingVisibilityFieldInVisibilityPatch() throws Exception {
        UUID restaurantId = UUID.randomUUID();

        mockMvc.perform(patch("/admin/restaurants/{restaurantId}/visibility", restaurantId)
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

        verifyNoInteractions(adminRestaurantService);
    }

    @Test
    void mapsNotFoundAndForbiddenEnvelopes() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        doNothing().when(accessGuard).requireAdmin(any());
        when(adminRestaurantService.updateVisibility(restaurantId, false))
                .thenThrow(new ResourceNotFoundException("Restaurant not found: " + restaurantId));

        mockMvc.perform(patch("/admin/restaurants/{restaurantId}/visibility", restaurantId)
                        .contentType("application/json")
                        .content("{\"visible\":false}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));

        doThrow(new AccessDeniedException("Admin role required")).when(accessGuard).requireAdmin(any());

        mockMvc.perform(delete("/admin/restaurants/{restaurantId}", restaurantId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("AUTHZ_INSUFFICIENT_ROLE"));
    }

    private AdminRestaurantRequest requestPayload() {
        return new AdminRestaurantRequest(
                UUID.randomUUID(),
                "Fast Eat Downtown",
                "Neighborhood kitchen",
                "Daily fresh menu",
                "CASUAL_DINING",
                "https://cdn.example.com/restaurants/downtown.jpg",
                "Makati",
                "NCR",
                "Philippines",
                14.5995,
                120.9842
        );
    }

    private AdminRestaurantResponse responsePayload() {
        return new AdminRestaurantResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Fast Eat Downtown",
                "Neighborhood kitchen",
                "Daily fresh menu",
                "CASUAL_DINING",
                "https://cdn.example.com/restaurants/downtown.jpg",
                true,
                true,
                "Makati",
                "NCR",
                "Philippines",
                14.5995,
                120.9842
        );
    }
}
