package com.gerolori.fasteat.web.order.dto;

import java.util.UUID;

public record OrderItemResponse(
        UUID menuId,
        String summary,
        OrderMoneyResponse price,
        int quantity,
        OrderMoneyResponse lineTotal
) {
}
