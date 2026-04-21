package com.gerolori.fasteat.web.menu.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record MenuListItemResponse(
        UUID menuId,
        String name,
        String summary,
        String category,
        MoneyResponse price,
        String imageUrl,
        boolean isAvailable,
        MenuAvailabilityStatus status,
        BigDecimal rating,
        long ratingCount,
        Double distanceKm
) {
}
