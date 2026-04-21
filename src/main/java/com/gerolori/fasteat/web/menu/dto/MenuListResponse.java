package com.gerolori.fasteat.web.menu.dto;

import java.util.List;

public record MenuListResponse(
        List<MenuListItemResponse> items,
        int page,
        int size,
        long totalItems,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
}
