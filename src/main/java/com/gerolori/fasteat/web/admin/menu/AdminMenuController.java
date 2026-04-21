package com.gerolori.fasteat.web.admin.menu;

import com.gerolori.fasteat.web.admin.AdminAuthorization;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/restaurants/{restaurantId}/menus")
public class AdminMenuController {

    private final AdminMenuService adminMenuService;

    public AdminMenuController(AdminMenuService adminMenuService) {
        this.adminMenuService = adminMenuService;
    }

    @PostMapping
    public ResponseEntity<AdminMenuResponse> createMenu(
            @PathVariable UUID restaurantId,
            @Valid @RequestBody AdminMenuUpsertRequest request
    ) {
        AdminAuthorization.requireAdmin();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminMenuService.createMenu(restaurantId, request));
    }

    @PutMapping("/{menuId}")
    public AdminMenuResponse updateMenu(
            @PathVariable UUID restaurantId,
            @PathVariable UUID menuId,
            @Valid @RequestBody AdminMenuUpsertRequest request
    ) {
        AdminAuthorization.requireAdmin();
        return adminMenuService.updateMenu(restaurantId, menuId, request);
    }

    @DeleteMapping("/{menuId}")
    public ResponseEntity<Void> deleteMenu(@PathVariable UUID restaurantId, @PathVariable UUID menuId) {
        AdminAuthorization.requireAdmin();
        adminMenuService.deleteMenu(restaurantId, menuId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{menuId}/availability")
    public AdminMenuResponse setAvailability(
            @PathVariable UUID restaurantId,
            @PathVariable UUID menuId,
            @Valid @RequestBody AdminMenuAvailabilityRequest request
    ) {
        AdminAuthorization.requireAdmin();
        return adminMenuService.setMenuAvailability(restaurantId, menuId, request.available());
    }

    @PatchMapping("/{menuId}/status")
    public AdminMenuResponse setStatus(
            @PathVariable UUID restaurantId,
            @PathVariable UUID menuId,
            @Valid @RequestBody AdminMenuStatusRequest request
    ) {
        AdminAuthorization.requireAdmin();
        return adminMenuService.setMenuActive(restaurantId, menuId, request.active());
    }
}
