package com.gerolori.fasteat.web.restaurant;

import com.gerolori.fasteat.domain.entity.Restaurant;
import com.gerolori.fasteat.domain.repository.RestaurantRepository;
import com.gerolori.fasteat.web.error.ResourceNotFoundException;
import com.gerolori.fasteat.web.shared.PagedResponse;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class RestaurantBrowseService {

    private final RestaurantRepository restaurantRepository;

    public RestaurantBrowseService(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    public PagedResponse<RestaurantListItemResponse> getRestaurants(int page, int size) {
        var restaurantPage = restaurantRepository.findByVisibleTrue(PageRequest.of(page, size))
                .map(this::toListItemResponse);

        return PagedResponse.from(restaurantPage);
    }

    public RestaurantDetailResponse getRestaurant(UUID restaurantId) {
        Restaurant restaurant = restaurantRepository.findByIdAndVisibleTrue(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + restaurantId));

        return toDetailResponse(restaurant);
    }

    private RestaurantListItemResponse toListItemResponse(Restaurant restaurant) {
        return new RestaurantListItemResponse(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getSummary(),
                restaurant.getCategory(),
                restaurant.getImageUrl(),
                restaurant.isAvailable(),
                restaurant.getRating(),
                restaurant.getRatingCount(),
                restaurant.getCity(),
                restaurant.getState(),
                restaurant.getCountry()
        );
    }

    private RestaurantDetailResponse toDetailResponse(Restaurant restaurant) {
        return new RestaurantDetailResponse(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getSummary(),
                restaurant.getDescription(),
                restaurant.getCategory(),
                restaurant.getImageUrl(),
                restaurant.isAvailable(),
                restaurant.getRating(),
                restaurant.getRatingCount(),
                restaurant.getCity(),
                restaurant.getState(),
                restaurant.getCountry(),
                restaurant.getLatitude(),
                restaurant.getLongitude()
        );
    }
}
