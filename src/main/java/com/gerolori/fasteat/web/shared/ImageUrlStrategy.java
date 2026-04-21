package com.gerolori.fasteat.web.shared;

import com.gerolori.fasteat.domain.entity.Menu;
import com.gerolori.fasteat.domain.entity.Restaurant;

public final class ImageUrlStrategy {

    private ImageUrlStrategy() {
    }

    public static String resolveRestaurantImageUrl(Restaurant restaurant) {
        return normalize(restaurant.getImageUrl());
    }

    public static String resolveMenuImageUrl(Menu menu) {
        String menuImageUrl = normalize(menu.getImageUrl());
        if (menuImageUrl != null) {
            return menuImageUrl;
        }

        if (menu.getRestaurant() == null) {
            return null;
        }

        return normalize(menu.getRestaurant().getImageUrl());
    }

    public static String normalize(String imageUrl) {
        if (imageUrl == null) {
            return null;
        }

        String trimmed = imageUrl.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
