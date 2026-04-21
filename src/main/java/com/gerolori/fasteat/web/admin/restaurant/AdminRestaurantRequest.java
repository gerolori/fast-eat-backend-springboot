package com.gerolori.fasteat.web.admin.restaurant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record AdminRestaurantRequest(
        @NotNull UUID ownerUserId,
        @NotBlank @Size(max = 120) String name,
        @Size(max = 255) String summary,
        @Size(max = 1000) String description,
        @Size(max = 80) String category,
        @Size(max = 512) String imageUrl,
        @Size(max = 120) String city,
        @Size(max = 120) String state,
        @Size(max = 120) String country,
        Double latitude,
        Double longitude
) {
}
