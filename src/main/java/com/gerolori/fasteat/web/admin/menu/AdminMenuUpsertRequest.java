package com.gerolori.fasteat.web.admin.menu;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record AdminMenuUpsertRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 255) String summary,
        @Size(max = 1000) String description,
        @Size(max = 80) String category,
        @NotNull @DecimalMin(value = "0.00") BigDecimal price,
        Boolean available,
        @Size(max = 512) String imageUrl
) {
}
