package com.gerolori.fasteat.web.admin.restaurant;

import jakarta.validation.constraints.NotNull;

public record RestaurantVisibilityUpdateRequest(@NotNull Boolean visible) {
}
