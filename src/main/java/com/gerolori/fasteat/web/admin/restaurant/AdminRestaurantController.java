package com.gerolori.fasteat.web.admin.restaurant;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/restaurants")
public class AdminRestaurantController {

    private final AdminRestaurantAccessGuard accessGuard;
    private final AdminRestaurantService adminRestaurantService;

    public AdminRestaurantController(
            AdminRestaurantAccessGuard accessGuard,
            AdminRestaurantService adminRestaurantService
    ) {
        this.accessGuard = accessGuard;
        this.adminRestaurantService = adminRestaurantService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminRestaurantResponse createRestaurant(
            Authentication authentication,
            @Valid @RequestBody AdminRestaurantRequest request
    ) {
        accessGuard.requireAdmin(authentication);
        return adminRestaurantService.createRestaurant(request);
    }

    @PutMapping("/{restaurantId}")
    public AdminRestaurantResponse updateRestaurant(
            Authentication authentication,
            @PathVariable UUID restaurantId,
            @Valid @RequestBody AdminRestaurantRequest request
    ) {
        accessGuard.requireAdmin(authentication);
        return adminRestaurantService.updateRestaurant(restaurantId, request);
    }

    @DeleteMapping("/{restaurantId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRestaurant(Authentication authentication, @PathVariable UUID restaurantId) {
        accessGuard.requireAdmin(authentication);
        adminRestaurantService.deleteRestaurant(restaurantId);
    }

    @PatchMapping("/{restaurantId}/status")
    public AdminRestaurantResponse updateRestaurantStatus(
            Authentication authentication,
            @PathVariable UUID restaurantId,
            @Valid @RequestBody RestaurantAvailabilityUpdateRequest request
    ) {
        accessGuard.requireAdmin(authentication);
        return adminRestaurantService.updateAvailability(restaurantId, request.available());
    }

    @PatchMapping("/{restaurantId}/visibility")
    public AdminRestaurantResponse updateRestaurantVisibility(
            Authentication authentication,
            @PathVariable UUID restaurantId,
            @Valid @RequestBody RestaurantVisibilityUpdateRequest request
    ) {
        accessGuard.requireAdmin(authentication);
        return adminRestaurantService.updateVisibility(restaurantId, request.visible());
    }
}
