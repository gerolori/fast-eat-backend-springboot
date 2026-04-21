package com.gerolori.fasteat.web.admin.ingredient;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminIngredientUpsertRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 255) String summary,
        @Size(max = 1000) String description,
        @Size(max = 80) String category,
        Boolean available,
        @Size(max = 512) String imageUrl
) {
}
