package com.gerolori.fasteat.web.admin.menu;

import com.gerolori.fasteat.web.menu.dto.MoneyResponse;
import java.time.Instant;
import java.util.UUID;

public record AdminMenuResponse(
        UUID menuId,
        UUID restaurantId,
        String name,
        String summary,
        String description,
        String category,
        MoneyResponse price,
        boolean isAvailable,
        boolean isActive,
        String imageUrl,
        Instant updatedAt
) {
}
