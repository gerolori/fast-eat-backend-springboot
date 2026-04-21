package com.gerolori.fasteat.web.restaurant;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gerolori.fasteat.config.TraceIdResolver;
import com.gerolori.fasteat.web.error.GlobalApiExceptionHandler;
import com.gerolori.fasteat.web.error.ResourceNotFoundException;
import com.gerolori.fasteat.web.shared.PagedResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class RestaurantBrowseControllerTest {

    @Mock
    private RestaurantBrowseService restaurantBrowseService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new RestaurantBrowseController(restaurantBrowseService))
                .setControllerAdvice(new GlobalApiExceptionHandler(new TraceIdResolver()))
                .build();
    }

    @Test
    void returnsPaginatedRestaurantListing() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        var item = new RestaurantListItemResponse(
                restaurantId,
                "Fast Eat Downtown",
                "Neighborhood kitchen",
                "CASUAL_DINING",
                "https://cdn.example.com/restaurants/downtown.jpg",
                true,
                new BigDecimal("4.50"),
                120L,
                "Makati",
                "NCR",
                "Philippines"
        );
        var response = new PagedResponse<>(List.of(item), 0, 1, 3, 3, true, false);

        when(restaurantBrowseService.getRestaurants(0, 1)).thenReturn(response);

        mockMvc.perform(get("/restaurants").param("page", "0").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].restaurantId").value(restaurantId.toString()))
                .andExpect(jsonPath("$.items[0].imageUrl").value("https://cdn.example.com/restaurants/downtown.jpg"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalItems").value(3))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.hasPrevious").value(false));
    }

    @Test
    void usesDefaultPaginationWhenQueryParametersAreMissing() throws Exception {
        when(restaurantBrowseService.getRestaurants(0, 20)).thenReturn(new PagedResponse<>(List.of(), 0, 20, 0, 0, false, false));

        mockMvc.perform(get("/restaurants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20));

        verify(restaurantBrowseService).getRestaurants(eq(0), eq(20));
    }

    @Test
    void returnsRestaurantDetail() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        var response = new RestaurantDetailResponse(
                restaurantId,
                "Fast Eat Downtown",
                "Neighborhood kitchen",
                "Daily fresh menu",
                "CASUAL_DINING",
                "https://cdn.example.com/restaurants/downtown.jpg",
                true,
                new BigDecimal("4.50"),
                120L,
                "Makati",
                "NCR",
                "Philippines",
                14.5995,
                120.9842
        );
        when(restaurantBrowseService.getRestaurant(restaurantId)).thenReturn(response);

        mockMvc.perform(get("/restaurants/{restaurantId}", restaurantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.restaurantId").value(restaurantId.toString()))
                .andExpect(jsonPath("$.name").value("Fast Eat Downtown"))
                .andExpect(jsonPath("$.imageUrl").value("https://cdn.example.com/restaurants/downtown.jpg"));
    }

    @Test
    void mapsMissingRestaurantToSharedNotFoundEnvelope() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        when(restaurantBrowseService.getRestaurant(restaurantId))
                .thenThrow(new ResourceNotFoundException("Restaurant not found: " + restaurantId));

        mockMvc.perform(get("/restaurants/{restaurantId}", restaurantId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/restaurants/" + restaurantId))
                .andExpect(jsonPath("$.traceId", not(blankOrNullString())));
    }
}
