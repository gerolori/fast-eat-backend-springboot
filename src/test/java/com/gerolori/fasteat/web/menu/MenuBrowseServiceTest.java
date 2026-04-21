package com.gerolori.fasteat.web.menu;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.gerolori.fasteat.domain.entity.Ingredient;
import com.gerolori.fasteat.domain.entity.Menu;
import com.gerolori.fasteat.domain.entity.Restaurant;
import com.gerolori.fasteat.domain.repository.MenuRepository;
import com.gerolori.fasteat.web.error.ResourceNotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MenuBrowseServiceTest {

    @Mock
    private MenuRepository menuRepository;

    private MenuBrowseService service;

    @BeforeEach
    void setUp() {
        service = new MenuBrowseService(menuRepository);
    }

    @Test
    void listMenusAppliesFiltersAndPagination() {
        Menu nearbyAvailable = buildMenu("Chicken Bowl", "Balanced meal", "MAIN", new BigDecimal("8.99"), true, 14.6000, 120.9850);
        Menu farAvailable = buildMenu("Seafood Pasta", "Creamy pasta", "MAIN", new BigDecimal("12.50"), true, 15.0000, 121.5000);
        Menu unavailable = buildMenu("Veggie Wrap", "Plant based", "SNACK", new BigDecimal("6.00"), false, 14.5900, 120.9800);

        when(menuRepository.findAll()).thenReturn(List.of(nearbyAvailable, farAvailable, unavailable));

        MenuBrowseQuery query = new MenuBrowseQuery(
                "chicken",
                List.of("MAIN"),
                true,
                new BigDecimal("8.00"),
                new BigDecimal("10.00"),
                14.5995,
                120.9842,
                5.0,
                "distance",
                "asc",
                0,
                1
        );

        var response = service.listMenus(query);

        assertThat(response.totalItems()).isEqualTo(1);
        assertThat(response.totalPages()).isEqualTo(1);
        assertThat(response.hasNext()).isFalse();
        assertThat(response.hasPrevious()).isFalse();
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).name()).isEqualTo("Chicken Bowl");
        assertThat(response.items().get(0).distanceKm()).isNotNull();
    }

    @Test
    void getMenuReturnsDetailProjectionWithIngredients() {
        UUID menuId = UUID.randomUUID();
        Menu menu = buildMenu("Chicken Bowl", "Balanced meal", "MAIN", new BigDecimal("8.99"), true, 14.6000, 120.9850);
        menu.setId(menuId);
        menu.setDescription("Detailed description");
        menu.setUpdatedAt(Instant.parse("2026-04-20T08:30:00Z"));

        Ingredient ingredient = new Ingredient();
        ingredient.setId(UUID.randomUUID());
        ingredient.setName("Chicken");
        menu.getIngredients().add(ingredient);

        when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));

        var response = service.getMenu(menuId);

        assertThat(response.menuId()).isEqualTo(menuId);
        assertThat(response.description()).isEqualTo("Detailed description");
        assertThat(response.price().amount()).isEqualTo("8.99");
        assertThat(response.ingredients()).hasSize(1);
        assertThat(response.ingredients().get(0).ingredientId()).isEqualTo(ingredient.getId());
    }

    @Test
    void getMenuThrowsNotFoundWhenMissing() {
        UUID menuId = UUID.randomUUID();
        when(menuRepository.findById(menuId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getMenu(menuId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Menu not found");
    }

    @Test
    void listMenusUsesRelevanceOrderingWhenQueryProvided() {
        Menu bothMatches = buildMenu("Chicken Chicken", "Popular chicken combo", "MAIN", new BigDecimal("10.00"), true, 14.6000, 120.9850);
        Menu nameOnly = buildMenu("Chicken Bowl", "Balanced meal", "MAIN", new BigDecimal("8.99"), true, 14.6000, 120.9850);
        Menu summaryOnly = buildMenu("Protein Bowl", "Savory chicken option", "MAIN", new BigDecimal("9.99"), true, 14.6000, 120.9850);

        when(menuRepository.findAll()).thenReturn(List.of(summaryOnly, nameOnly, bothMatches));

        MenuBrowseQuery query = new MenuBrowseQuery(
                "chicken",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                0,
                20
        );

        var response = service.listMenus(query);

        assertThat(response.items()).extracting(item -> item.name())
                .containsExactly("Chicken Chicken", "Chicken Bowl", "Protein Bowl");
    }

    private Menu buildMenu(
            String name,
            String summary,
            String category,
            BigDecimal price,
            boolean available,
            double latitude,
            double longitude
    ) {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(UUID.randomUUID());
        restaurant.setName("Fast Eat Downtown");
        restaurant.setOwnerUserId(UUID.randomUUID());
        restaurant.setLatitude(latitude);
        restaurant.setLongitude(longitude);

        Menu menu = new Menu();
        menu.setId(UUID.randomUUID());
        menu.setRestaurant(restaurant);
        menu.setName(name);
        menu.setSummary(summary);
        menu.setCategory(category);
        menu.setPrice(price);
        menu.setAvailable(available);
        menu.setRating(new BigDecimal("4.50"));
        menu.setRatingCount(20L);
        menu.setImageUrl("https://cdn.example.com/menu.png");
        return menu;
    }
}
