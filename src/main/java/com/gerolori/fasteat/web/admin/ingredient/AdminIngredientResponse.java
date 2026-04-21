package com.gerolori.fasteat.web.admin.ingredient;

import java.time.Instant;
import java.util.UUID;

public record AdminIngredientResponse(
        UUID ingredientId,
        String name,
        String summary,
        String description,
        String category,
        boolean isAvailable,
        String imageUrl,
        Instant updatedAt
) {
}
