package com.gerolori.fasteat.web.restaurant;

import com.gerolori.fasteat.web.shared.PagedResponse;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/restaurants")
public class RestaurantBrowseController {

    private final RestaurantBrowseService restaurantBrowseService;

    public RestaurantBrowseController(RestaurantBrowseService restaurantBrowseService) {
        this.restaurantBrowseService = restaurantBrowseService;
    }

    @GetMapping
    public PagedResponse<RestaurantListItemResponse> getRestaurants(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size
    ) {
        return restaurantBrowseService.getRestaurants(page, size);
    }

    @GetMapping("/{restaurantId}")
    public RestaurantDetailResponse getRestaurant(@PathVariable UUID restaurantId) {
        return restaurantBrowseService.getRestaurant(restaurantId);
    }
}
