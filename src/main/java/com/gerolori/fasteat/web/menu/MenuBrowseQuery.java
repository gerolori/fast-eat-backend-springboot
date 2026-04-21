package com.gerolori.fasteat.web.menu;

import java.math.BigDecimal;
import java.util.List;

public record MenuBrowseQuery(
        String q,
        List<String> categories,
        Boolean available,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Double lat,
        Double lng,
        Double radiusKm,
        String sortBy,
        String sortDir,
        int page,
        int size
) {
}
