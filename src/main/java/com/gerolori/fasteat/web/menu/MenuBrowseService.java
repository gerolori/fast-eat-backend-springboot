package com.gerolori.fasteat.web.menu;

import com.gerolori.fasteat.domain.entity.Ingredient;
import com.gerolori.fasteat.domain.entity.Menu;
import com.gerolori.fasteat.domain.repository.MenuRepository;
import com.gerolori.fasteat.web.error.ResourceNotFoundException;
import com.gerolori.fasteat.web.menu.dto.MenuDetailResponse;
import com.gerolori.fasteat.web.menu.dto.MenuAvailabilityStatus;
import com.gerolori.fasteat.web.menu.dto.MenuIngredientResponse;
import com.gerolori.fasteat.web.menu.dto.MenuListItemResponse;
import com.gerolori.fasteat.web.menu.dto.MenuListResponse;
import com.gerolori.fasteat.web.menu.dto.MoneyResponse;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MenuBrowseService {

    private static final String DEFAULT_CURRENCY = "USD";
    private static final double EARTH_RADIUS_KM = 6371.0;

    private final MenuRepository menuRepository;

    public MenuBrowseService(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    public MenuListResponse listMenus(MenuBrowseQuery query) {
        List<String> categories = query.categories() == null
                ? List.of()
                : query.categories().stream()
                        .filter(category -> category != null && !category.isBlank())
                        .map(String::trim)
                        .toList();

        boolean hasLocationContext = query.lat() != null && query.lng() != null;

        List<RankedMenu> filtered = menuRepository.findAll().stream()
                .filter(this::isRestaurantVisible)
                .map(menu -> toRankedMenu(menu, query.lat(), query.lng(), hasLocationContext))
                .filter(rankedMenu -> matchesTextQuery(rankedMenu.menu(), query.q()))
                .filter(rankedMenu -> matchesCategories(rankedMenu.menu(), categories))
                .filter(rankedMenu -> matchesAvailability(rankedMenu.menu(), query.available()))
                .filter(rankedMenu -> matchesPriceRange(rankedMenu.menu(), query.minPrice(), query.maxPrice()))
                .filter(rankedMenu -> matchesRadius(rankedMenu.distanceKm(), query.radiusKm(), hasLocationContext))
                .sorted(resolveComparator(query.sortBy(), query.sortDir(), hasLocationContext, query.q()))
                .toList();

        long totalItems = filtered.size();
        int totalPages = query.size() <= 0 ? 0 : (int) Math.ceil(totalItems / (double) query.size());
        int fromIndex = Math.max(0, query.page()) * query.size();
        int toIndex = Math.min(fromIndex + query.size(), filtered.size());

        List<MenuListItemResponse> items = fromIndex >= filtered.size()
                ? List.of()
                : filtered.subList(fromIndex, toIndex).stream()
                        .map(rankedMenu -> toListItem(rankedMenu.menu(), rankedMenu.distanceKm(), hasLocationContext))
                        .toList();

        return new MenuListResponse(
                items,
                query.page(),
                query.size(),
                totalItems,
                totalPages,
                query.page() + 1 < totalPages,
                query.page() > 0
        );
    }

    public MenuDetailResponse getMenu(UUID menuId) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu not found: " + menuId));
        if (!isRestaurantVisible(menu)) {
            throw new ResourceNotFoundException("Menu not found: " + menuId);
        }

        MenuAvailabilityStatus status = availabilityStatus(menu);

        List<MenuIngredientResponse> ingredients = toIngredientResponses(menu);

        return new MenuDetailResponse(
                menu.getId(),
                menu.getName(),
                menu.getSummary(),
                menu.getDescription(),
                toMoney(menu),
                status == MenuAvailabilityStatus.AVAILABLE,
                status,
                menu.getImageUrl(),
                ingredients,
                menu.getUpdatedAt()
        );
    }

    public List<MenuIngredientResponse> getMenuIngredients(UUID menuId) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu not found: " + menuId));
        if (!isRestaurantVisible(menu)) {
            throw new ResourceNotFoundException("Menu not found: " + menuId);
        }

        return toIngredientResponses(menu);
    }

    private RankedMenu toRankedMenu(Menu menu, Double lat, Double lng, boolean hasLocationContext) {
        Double distanceKm = hasLocationContext ? computeDistanceKm(menu, lat, lng) : null;
        return new RankedMenu(menu, distanceKm);
    }

    private boolean matchesTextQuery(Menu menu, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }

        String normalized = query.trim().toLowerCase(Locale.ROOT);
        String name = valueOrEmpty(menu.getName()).toLowerCase(Locale.ROOT);
        String summary = valueOrEmpty(menu.getSummary()).toLowerCase(Locale.ROOT);
        return name.contains(normalized) || summary.contains(normalized);
    }

    private boolean matchesCategories(Menu menu, List<String> categories) {
        if (categories.isEmpty()) {
            return true;
        }

        return categories.stream()
                .anyMatch(category -> category.equalsIgnoreCase(valueOrEmpty(menu.getCategory())));
    }

    private boolean matchesAvailability(Menu menu, Boolean available) {
        if (available == null) {
            return true;
        }

        return (availabilityStatus(menu) == MenuAvailabilityStatus.AVAILABLE) == available;
    }

    private boolean matchesPriceRange(Menu menu, java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice) {
        if (minPrice != null && menu.getPrice().compareTo(minPrice) < 0) {
            return false;
        }

        if (maxPrice != null && menu.getPrice().compareTo(maxPrice) > 0) {
            return false;
        }

        return true;
    }

    private boolean matchesRadius(Double distanceKm, Double radiusKm, boolean hasLocationContext) {
        if (!hasLocationContext || radiusKm == null) {
            return true;
        }

        return distanceKm != null && distanceKm <= radiusKm;
    }

    private Comparator<RankedMenu> resolveComparator(String sortBy, String sortDir, boolean hasLocationContext, String q) {
        String effectiveSortBy = resolveSortBy(sortBy, hasLocationContext, q);
        String normalizedQuery = normalizeQuery(q);

        Comparator<RankedMenu> comparator = switch (effectiveSortBy) {
            case "price" -> Comparator.comparing(rankedMenu -> rankedMenu.menu().getPrice());
            case "rating" -> Comparator.comparing(rankedMenu -> rankedMenu.menu().getRating());
            case "relevance" -> Comparator.<RankedMenu>comparingInt(rankedMenu -> relevanceScore(rankedMenu.menu(), normalizedQuery))
                    .thenComparing(rankedMenu -> valueOrEmpty(rankedMenu.menu().getName()), String.CASE_INSENSITIVE_ORDER);
            case "distance" -> Comparator.comparing(
                    RankedMenu::distanceKm,
                    Comparator.nullsLast(Double::compareTo)
            );
            default -> Comparator.comparing(
                    rankedMenu -> valueOrEmpty(rankedMenu.menu().getName()),
                    String.CASE_INSENSITIVE_ORDER
            );
        };

        if (sortDir != null && sortDir.equalsIgnoreCase("desc")) {
            return comparator.reversed();
        }

        if (sortDir == null || sortDir.isBlank()) {
            if ("rating".equals(effectiveSortBy) || "relevance".equals(effectiveSortBy)) {
                return comparator.reversed();
            }
        }

        return comparator;
    }

    private String resolveSortBy(String sortBy, boolean hasLocationContext, String q) {
        if (sortBy != null && !sortBy.isBlank()) {
            return sortBy.trim().toLowerCase(Locale.ROOT);
        }

        if (q != null && !q.isBlank()) {
            return "relevance";
        }

        if (hasLocationContext) {
            return "distance";
        }

        return "name";
    }

    private int relevanceScore(Menu menu, String normalizedQuery) {
        if (normalizedQuery.isBlank()) {
            return 0;
        }

        int score = 0;
        if (valueOrEmpty(menu.getName()).toLowerCase(Locale.ROOT).contains(normalizedQuery)) {
            score += 2;
        }

        if (valueOrEmpty(menu.getSummary()).toLowerCase(Locale.ROOT).contains(normalizedQuery)) {
            score += 1;
        }

        return score;
    }

    private String normalizeQuery(String q) {
        return q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
    }

    private MenuListItemResponse toListItem(Menu menu, Double distanceKm, boolean hasLocationContext) {
        MenuAvailabilityStatus status = availabilityStatus(menu);
        return new MenuListItemResponse(
                menu.getId(),
                menu.getName(),
                menu.getSummary(),
                menu.getCategory(),
                toMoney(menu),
                menu.getImageUrl(),
                status == MenuAvailabilityStatus.AVAILABLE,
                status,
                menu.getRating(),
                menu.getRatingCount(),
                hasLocationContext ? distanceKm : null
        );
    }

    private List<MenuIngredientResponse> toIngredientResponses(Menu menu) {
        return menu.getIngredients().stream()
                .sorted(Comparator.comparing(Ingredient::getName, String.CASE_INSENSITIVE_ORDER))
                .map(ingredient -> new MenuIngredientResponse(
                        ingredient.getId(),
                        ingredient.getName(),
                        ingredient.getSummary(),
                        ingredient.getImageUrl(),
                        ingredient.isAvailable()
                ))
                .toList();
    }

    private MenuAvailabilityStatus availabilityStatus(Menu menu) {
        if (!menu.isActive() || menu.getRestaurant() == null || !menu.getRestaurant().isAvailable()) {
            return MenuAvailabilityStatus.INACTIVE;
        }

        if (!menu.isAvailable()) {
            return MenuAvailabilityStatus.SOLD_OUT;
        }

        return MenuAvailabilityStatus.AVAILABLE;
    }

    private boolean isRestaurantVisible(Menu menu) {
        return menu.getRestaurant() != null && menu.getRestaurant().isVisible();
    }

    private MoneyResponse toMoney(Menu menu) {
        return new MoneyResponse(menu.getPrice().toPlainString(), DEFAULT_CURRENCY);
    }

    private Double computeDistanceKm(Menu menu, double lat, double lng) {
        if (menu.getRestaurant() == null
                || menu.getRestaurant().getLatitude() == null
                || menu.getRestaurant().getLongitude() == null) {
            return null;
        }

        double lat1 = Math.toRadians(lat);
        double lng1 = Math.toRadians(lng);
        double lat2 = Math.toRadians(menu.getRestaurant().getLatitude());
        double lng2 = Math.toRadians(menu.getRestaurant().getLongitude());

        double deltaLat = lat2 - lat1;
        double deltaLng = lng2 - lng1;

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private record RankedMenu(Menu menu, Double distanceKm) {
    }
}
