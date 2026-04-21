package com.gerolori.fasteat.web.users.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateSubscriptionStateRequest(@NotNull Boolean enabled) {
}
