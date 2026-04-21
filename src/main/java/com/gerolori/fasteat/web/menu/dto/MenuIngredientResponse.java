package com.gerolori.fasteat.web.menu.dto;

import java.util.UUID;

public record MenuIngredientResponse(
        UUID ingredientId,
        String name,
        String summary,
        String imageUrl,
        boolean isAvailable
) {
}
