package com.gerolori.fasteat.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gerolori.fasteat.domain.repository.MenuRepository;
import com.gerolori.fasteat.domain.repository.RestaurantRepository;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class CatalogPersistenceMappingTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Test
    void persistsRestaurantMenuAndIngredientRelationships() {
        Ingredient ingredient = new Ingredient();
        ingredient.setName("Chicken");
        ingredient.setCategory("PROTEIN");
        ingredient.setSummary("Lean chicken breast");
        ingredient.setAvailable(true);
        ingredient = entityManager.persistFlushFind(ingredient);

        Restaurant restaurant = buildRestaurant("Makati");
        Menu menu = buildMenu();
        menu.getIngredients().add(ingredient);
        restaurant.addMenu(menu);

        Restaurant savedRestaurant = restaurantRepository.saveAndFlush(restaurant);
        entityManager.clear();

        Menu savedMenu = menuRepository.findById(savedRestaurant.getMenus().get(0).getId()).orElseThrow();

        assertThat(savedMenu.getRestaurant().getId()).isEqualTo(savedRestaurant.getId());
        assertThat(savedMenu.getIngredients())
                .extracting(Ingredient::getId)
                .containsExactly(ingredient.getId());
    }

    @Test
    void rejectsMenuWithoutRestaurant() {
        Menu menu = buildMenu();

        assertThatThrownBy(() -> menuRepository.saveAndFlush(menu))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findsVisibleRestaurantsByCityIgnoringCase() {
        Restaurant visibleInCity = buildRestaurant("Quezon City");
        Restaurant hiddenInCity = buildRestaurant("Quezon City");
        hiddenInCity.setVisible(false);
        Restaurant visibleElsewhere = buildRestaurant("Makati");

        restaurantRepository.save(visibleInCity);
        restaurantRepository.save(hiddenInCity);
        restaurantRepository.saveAndFlush(visibleElsewhere);

        var page = restaurantRepository.findByCityIgnoreCaseAndVisibleTrue("quezon city", PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getName()).isEqualTo(visibleInCity.getName());
    }

    private Restaurant buildRestaurant(String city) {
        Restaurant restaurant = new Restaurant();
        restaurant.setOwnerUserId(UUID.randomUUID());
        restaurant.setName("Fast Eat Downtown");
        restaurant.setSummary("Neighborhood kitchen");
        restaurant.setDescription("Daily fresh menu");
        restaurant.setCategory("CASUAL_DINING");
        restaurant.setImageUrl("https://cdn.example.com/restaurants/downtown.jpg");
        restaurant.setCity(city);
        restaurant.setCountry("Philippines");
        restaurant.setState("NCR");
        restaurant.setLatitude(14.5995);
        restaurant.setLongitude(120.9842);
        restaurant.setRating(new BigDecimal("4.50"));
        restaurant.setRatingCount(120L);
        restaurant.setAvailable(true);
        restaurant.setVisible(true);
        return restaurant;
    }

    private Menu buildMenu() {
        Menu menu = new Menu();
        menu.setName("Grilled Chicken Bowl");
        menu.setSummary("Balanced meal bowl");
        menu.setDescription("Grilled chicken with rice and vegetables");
        menu.setCategory("MAIN_COURSE");
        menu.setImageUrl("https://cdn.example.com/menus/chicken-bowl.jpg");
        menu.setPrice(new BigDecimal("8.99"));
        menu.setAvailable(true);
        menu.setRating(new BigDecimal("4.70"));
        menu.setRatingCount(52L);
        return menu;
    }
}
