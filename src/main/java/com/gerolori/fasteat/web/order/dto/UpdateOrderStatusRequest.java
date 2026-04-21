package com.gerolori.fasteat.web.order.dto;

import com.gerolori.fasteat.domain.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
        @NotNull OrderStatus status
) {
}
