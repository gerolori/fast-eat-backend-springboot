package com.gerolori.fasteat.web.admin.restaurant;

import com.gerolori.fasteat.domain.entity.Restaurant;
import com.gerolori.fasteat.domain.repository.RestaurantRepository;
import com.gerolori.fasteat.web.error.BusinessRuleViolationException;
import com.gerolori.fasteat.web.error.ResourceNotFoundException;
import com.gerolori.fasteat.web.shared.ImageUrlStrategy;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AdminRestaurantService {

    private final RestaurantRepository restaurantRepository;

    public AdminRestaurantService(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    public AdminRestaurantResponse createRestaurant(AdminRestaurantRequest request) {
        ensureNameNotTaken(request.ownerUserId(), request.name(), null);

        Restaurant restaurant = new Restaurant();
        applyEditableFields(restaurant, request);
        restaurant.setAvailable(true);
        restaurant.setVisible(true);

        return toResponse(restaurantRepository.save(restaurant));
    }

    public AdminRestaurantResponse updateRestaurant(UUID restaurantId, AdminRestaurantRequest request) {
        Restaurant restaurant = getRestaurantOrThrow(restaurantId);
        ensureNameNotTaken(request.ownerUserId(), request.name(), restaurantId);
        applyEditableFields(restaurant, request);
        return toResponse(restaurantRepository.save(restaurant));
    }

    public void deleteRestaurant(UUID restaurantId) {
        Restaurant restaurant = getRestaurantOrThrow(restaurantId);
        restaurantRepository.delete(restaurant);
    }

    public AdminRestaurantResponse updateAvailability(UUID restaurantId, boolean available) {
        Restaurant restaurant = getRestaurantOrThrow(restaurantId);
        restaurant.setAvailable(available);
        return toResponse(restaurantRepository.save(restaurant));
    }

    public AdminRestaurantResponse updateVisibility(UUID restaurantId, boolean visible) {
        Restaurant restaurant = getRestaurantOrThrow(restaurantId);
        restaurant.setVisible(visible);
        return toResponse(restaurantRepository.save(restaurant));
    }

    private Restaurant getRestaurantOrThrow(UUID restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + restaurantId));
    }

    private void applyEditableFields(Restaurant restaurant, AdminRestaurantRequest request) {
        restaurant.setOwnerUserId(request.ownerUserId());
        restaurant.setName(request.name());
        restaurant.setSummary(request.summary());
        restaurant.setDescription(request.description());
        restaurant.setCategory(request.category());
        restaurant.setImageUrl(ImageUrlStrategy.normalize(request.imageUrl()));
        restaurant.setCity(request.city());
        restaurant.setState(request.state());
        restaurant.setCountry(request.country());
        restaurant.setLatitude(request.latitude());
        restaurant.setLongitude(request.longitude());
    }

    private void ensureNameNotTaken(UUID ownerUserId, String name, UUID currentRestaurantId) {
        boolean exists = currentRestaurantId == null
                ? restaurantRepository.existsByOwnerUserIdAndNameIgnoreCase(ownerUserId, name)
                : restaurantRepository.existsByOwnerUserIdAndNameIgnoreCaseAndIdNot(ownerUserId, name, currentRestaurantId);

        if (exists) {
            throw new BusinessRuleViolationException(
                    "RESTAURANT_NAME_CONFLICT",
                    "Restaurant name already exists for this owner"
            );
        }
    }

    private AdminRestaurantResponse toResponse(Restaurant restaurant) {
        return new AdminRestaurantResponse(
                restaurant.getId(),
                restaurant.getOwnerUserId(),
                restaurant.getName(),
                restaurant.getSummary(),
                restaurant.getDescription(),
                restaurant.getCategory(),
                restaurant.getImageUrl(),
                restaurant.isAvailable(),
                restaurant.isVisible(),
                restaurant.getCity(),
                restaurant.getState(),
                restaurant.getCountry(),
                restaurant.getLatitude(),
                restaurant.getLongitude()
        );
    }
}
