package com.gerolori.fasteat.web.admin.menu;

import jakarta.validation.constraints.NotNull;

public record AdminMenuStatusRequest(@NotNull Boolean active) {
}
