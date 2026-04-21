package com.gerolori.fasteat.web.order.dto;

import com.gerolori.fasteat.domain.entity.OrderStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID orderId,
        OrderStatus status,
        Instant statusUpdatedAt,
        Instant expectedDeliveryTimestamp,
        OrderTrackingPositionResponse currentPosition,
        UUID ownerUserId,
        List<OrderItemResponse> items,
        OrderMoneyResponse total,
        String deliveryAddress,
        String note,
        Instant createdAt
) {
}
