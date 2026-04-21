package com.gerolori.fasteat.web.admin.ingredient;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record AdminMenuIngredientReplaceRequest(@NotNull List<UUID> ingredientIds) {
}
