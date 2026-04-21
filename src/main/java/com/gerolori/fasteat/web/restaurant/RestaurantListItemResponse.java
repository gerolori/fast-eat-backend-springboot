package com.gerolori.fasteat.web.restaurant;

import java.math.BigDecimal;
import java.util.UUID;

public record RestaurantListItemResponse(
        UUID restaurantId,
        String name,
        String summary,
        String category,
        String imageUrl,
        boolean isAvailable,
        BigDecimal rating,
        long ratingCount,
        String city,
        String state,
        String country
) {
}
