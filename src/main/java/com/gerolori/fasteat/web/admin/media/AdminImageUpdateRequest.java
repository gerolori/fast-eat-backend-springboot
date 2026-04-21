package com.gerolori.fasteat.web.admin.media;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminImageUpdateRequest(@NotBlank @Size(max = 512) String imageUrl) {
}
