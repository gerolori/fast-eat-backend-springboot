package com.gerolori.fasteat.web.admin.media;

import com.gerolori.fasteat.web.admin.AdminAuthorization;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/media")
public class AdminMediaController {

    private final AdminMediaService adminMediaService;

    public AdminMediaController(AdminMediaService adminMediaService) {
        this.adminMediaService = adminMediaService;
    }

    @PutMapping("/restaurants/{restaurantId}/image")
    public AdminImageUpdateResponse updateRestaurantImage(
            @PathVariable UUID restaurantId,
            @Valid @RequestBody AdminImageUpdateRequest request
    ) {
        AdminAuthorization.requireAdmin();
        return adminMediaService.updateRestaurantImage(restaurantId, request.imageUrl());
    }

    @PutMapping("/menus/{menuId}/image")
    public AdminImageUpdateResponse updateMenuImage(
            @PathVariable UUID menuId,
            @Valid @RequestBody AdminImageUpdateRequest request
    ) {
        AdminAuthorization.requireAdmin();
        return adminMediaService.updateMenuImage(menuId, request.imageUrl());
    }
}
