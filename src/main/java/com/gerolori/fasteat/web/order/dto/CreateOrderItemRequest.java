package com.gerolori.fasteat.web.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateOrderItemRequest(
        @NotNull UUID menuId,
        @NotNull @Min(1) Integer quantity
) {
}
