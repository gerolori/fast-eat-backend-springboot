package com.gerolori.fasteat.web.admin.restaurant;

import java.util.UUID;

public record AdminRestaurantResponse(
        UUID restaurantId,
        UUID ownerUserId,
        String name,
        String summary,
        String description,
        String category,
        String imageUrl,
        boolean available,
        boolean visible,
        String city,
        String state,
        String country,
        Double latitude,
        Double longitude
) {
}
