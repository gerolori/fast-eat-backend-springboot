package com.gerolori.fasteat.web.order.dto;

import java.time.Instant;

public record OrderTrackingPositionResponse(
        double latitude,
        double longitude,
        Instant updatedAt
) {
}
