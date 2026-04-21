package com.gerolori.fasteat.web.admin.menu;

import jakarta.validation.constraints.NotNull;

public record AdminMenuAvailabilityRequest(@NotNull Boolean available) {
}
