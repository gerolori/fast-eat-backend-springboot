package com.gerolori.fasteat.web.admin.media;

import java.time.Instant;
import java.util.UUID;

public record AdminImageUpdateResponse(
        UUID resourceId,
        String imageUrl,
        Instant updatedAt
) {
}
