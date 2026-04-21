package com.gerolori.fasteat.web.menu.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MenuDetailResponse(
        UUID menuId,
        String name,
        String summary,
        String description,
        MoneyResponse price,
        boolean isAvailable,
        MenuAvailabilityStatus status,
        String imageUrl,
        List<MenuIngredientResponse> ingredients,
        Instant updatedAt
) {
}
