package com.gerolori.fasteat.web.restaurant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.gerolori.fasteat.domain.entity.Restaurant;
import com.gerolori.fasteat.domain.repository.RestaurantRepository;
import com.gerolori.fasteat.web.error.ResourceNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class RestaurantBrowseServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    private RestaurantBrowseService restaurantBrowseService;

    @BeforeEach
    void setUp() {
        restaurantBrowseService = new RestaurantBrowseService(restaurantRepository);
    }

    @Test
    void returnsVisibleRestaurantsUsingSharedPaginationEnvelope() {
        Restaurant first = buildRestaurant("Fast Eat One");
        Restaurant second = buildRestaurant("Fast Eat Two");

        var pageRequest = PageRequest.of(1, 2);
        var page = new PageImpl<>(List.of(first, second), pageRequest, 5);
        when(restaurantRepository.findByVisibleTrue(pageRequest)).thenReturn(page);

        var response = restaurantBrowseService.getRestaurants(1, 2);

        assertThat(response.page()).isEqualTo(1);
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.totalItems()).isEqualTo(5);
        assertThat(response.totalPages()).isEqualTo(3);
        assertThat(response.hasNext()).isTrue();
        assertThat(response.hasPrevious()).isTrue();
        assertThat(response.items()).hasSize(2);
        assertThat(response.items().get(0).restaurantId()).isEqualTo(first.getId());
        assertThat(response.items().get(0).imageUrl()).isEqualTo(first.getImageUrl());
    }

    @Test
    void returnsRestaurantDetailForVisibleRestaurant() {
        UUID restaurantId = UUID.randomUUID();
        Restaurant restaurant = buildRestaurant("Fast Eat Downtown");
        restaurant.setId(restaurantId);
        restaurant.setDescription("Daily fresh menu");
        restaurant.setLatitude(14.5995);
        restaurant.setLongitude(120.9842);

        when(restaurantRepository.findByIdAndVisibleTrue(restaurantId)).thenReturn(Optional.of(restaurant));

        var response = restaurantBrowseService.getRestaurant(restaurantId);

        assertThat(response.restaurantId()).isEqualTo(restaurantId);
        assertThat(response.name()).isEqualTo("Fast Eat Downtown");
        assertThat(response.description()).isEqualTo("Daily fresh menu");
        assertThat(response.imageUrl()).isEqualTo("https://cdn.example.com/restaurants/fast-eat.jpg");
        assertThat(response.latitude()).isEqualTo(14.5995);
        assertThat(response.longitude()).isEqualTo(120.9842);
    }

    @Test
    void throwsNotFoundWhenRestaurantDoesNotExistOrIsHidden() {
        UUID restaurantId = UUID.randomUUID();
        when(restaurantRepository.findByIdAndVisibleTrue(restaurantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> restaurantBrowseService.getRestaurant(restaurantId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Restaurant not found: " + restaurantId);
    }

    @Test
    void restaurantImageProjectionNormalizesWhitespaceAcrossListAndDetail() {
        UUID restaurantId = UUID.randomUUID();
        Restaurant restaurant = buildRestaurant("Fast Eat Downtown");
        restaurant.setId(restaurantId);
        restaurant.setImageUrl("  https://cdn.example.com/restaurants/normalized.jpg  ");

        var pageRequest = PageRequest.of(0, 20);
        var page = new PageImpl<>(List.of(restaurant), pageRequest, 1);
        when(restaurantRepository.findByVisibleTrue(pageRequest)).thenReturn(page);
        when(restaurantRepository.findByIdAndVisibleTrue(restaurantId)).thenReturn(Optional.of(restaurant));

        var listResponse = restaurantBrowseService.getRestaurants(0, 20);
        var detailResponse = restaurantBrowseService.getRestaurant(restaurantId);

        assertThat(listResponse.items().get(0).imageUrl()).isEqualTo("https://cdn.example.com/restaurants/normalized.jpg");
        assertThat(detailResponse.imageUrl()).isEqualTo("https://cdn.example.com/restaurants/normalized.jpg");
    }

    private Restaurant buildRestaurant(String name) {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(UUID.randomUUID());
        restaurant.setOwnerUserId(UUID.randomUUID());
        restaurant.setName(name);
        restaurant.setSummary("Neighborhood kitchen");
        restaurant.setCategory("CASUAL_DINING");
        restaurant.setImageUrl("https://cdn.example.com/restaurants/fast-eat.jpg");
        restaurant.setAvailable(true);
        restaurant.setVisible(true);
        restaurant.setRating(new BigDecimal("4.50"));
        restaurant.setRatingCount(120L);
        restaurant.setCity("Makati");
        restaurant.setState("NCR");
        restaurant.setCountry("Philippines");
        return restaurant;
    }
}
