package com.gerolori.fasteat.web.menu.dto;

import java.util.UUID;

public record MenuIngredientResponse(
        UUID ingredientId,
        String name,
        String quantity,
        String unit,
        boolean isOptional
) {
}
