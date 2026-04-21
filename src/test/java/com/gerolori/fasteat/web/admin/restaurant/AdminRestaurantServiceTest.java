package com.gerolori.fasteat.web.admin.restaurant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gerolori.fasteat.domain.entity.Restaurant;
import com.gerolori.fasteat.domain.repository.RestaurantRepository;
import com.gerolori.fasteat.web.error.BusinessRuleViolationException;
import com.gerolori.fasteat.web.error.ResourceNotFoundException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminRestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    private AdminRestaurantService service;

    @BeforeEach
    void setUp() {
        service = new AdminRestaurantService(restaurantRepository);
    }

    @Test
    void createsRestaurantWithDefaultStatusAndVisibility() {
        AdminRestaurantRequest request = sampleRequest();

        when(restaurantRepository.existsByOwnerUserIdAndNameIgnoreCase(request.ownerUserId(), request.name())).thenReturn(false);
        when(restaurantRepository.save(any(Restaurant.class))).thenAnswer(invocation -> {
            Restaurant saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        AdminRestaurantResponse response = service.createRestaurant(request);

        assertThat(response.restaurantId()).isNotNull();
        assertThat(response.available()).isTrue();
        assertThat(response.visible()).isTrue();
        assertThat(response.name()).isEqualTo(request.name());
        assertThat(response.imageUrl()).isEqualTo("https://cdn.example.com/restaurants/downtown.jpg");
    }

    @Test
    void rejectsCreateWhenOwnerAlreadyHasSameRestaurantName() {
        AdminRestaurantRequest request = sampleRequest();
        when(restaurantRepository.existsByOwnerUserIdAndNameIgnoreCase(request.ownerUserId(), request.name())).thenReturn(true);

        assertThatThrownBy(() -> service.createRestaurant(request))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Restaurant name already exists");
    }

    @Test
    void updatesRestaurantFields() {
        UUID restaurantId = UUID.randomUUID();
        Restaurant restaurant = existingRestaurant(restaurantId);
        AdminRestaurantRequest request = sampleRequest();

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(restaurantRepository.existsByOwnerUserIdAndNameIgnoreCaseAndIdNot(request.ownerUserId(), request.name(), restaurantId))
                .thenReturn(false);
        when(restaurantRepository.save(restaurant)).thenReturn(restaurant);

        AdminRestaurantResponse response = service.updateRestaurant(restaurantId, request);

        assertThat(response.name()).isEqualTo(request.name());
        assertThat(response.ownerUserId()).isEqualTo(request.ownerUserId());
        assertThat(response.city()).isEqualTo(request.city());
    }

    @Test
    void deletesRestaurantWhenPresent() {
        UUID restaurantId = UUID.randomUUID();
        Restaurant restaurant = existingRestaurant(restaurantId);
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));

        service.deleteRestaurant(restaurantId);

        verify(restaurantRepository).delete(restaurant);
    }

    @Test
    void updatesAvailabilityAndVisibility() {
        UUID restaurantId = UUID.randomUUID();
        Restaurant restaurant = existingRestaurant(restaurantId);
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(restaurantRepository.save(restaurant)).thenReturn(restaurant);

        AdminRestaurantResponse availabilityResponse = service.updateAvailability(restaurantId, false);
        AdminRestaurantResponse visibilityResponse = service.updateVisibility(restaurantId, false);

        assertThat(availabilityResponse.available()).isFalse();
        assertThat(visibilityResponse.visible()).isFalse();
    }

    @Test
    void throwsNotFoundForMissingRestaurant() {
        UUID restaurantId = UUID.randomUUID();
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateAvailability(restaurantId, true))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Restaurant not found: " + restaurantId);
    }

    @Test
    void updateRejectsDuplicateNameForAnotherRestaurant() {
        UUID restaurantId = UUID.randomUUID();
        Restaurant restaurant = existingRestaurant(restaurantId);
        AdminRestaurantRequest request = sampleRequest();

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(restaurantRepository.existsByOwnerUserIdAndNameIgnoreCaseAndIdNot(request.ownerUserId(), request.name(), restaurantId))
                .thenReturn(true);

        assertThatThrownBy(() -> service.updateRestaurant(restaurantId, request))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Restaurant name already exists");
    }

    private AdminRestaurantRequest sampleRequest() {
        return new AdminRestaurantRequest(
                UUID.randomUUID(),
                "Fast Eat Downtown",
                "Neighborhood kitchen",
                "Daily fresh menu",
                "CASUAL_DINING",
                " https://cdn.example.com/restaurants/downtown.jpg ",
                "Makati",
                "NCR",
                "Philippines",
                14.5995,
                120.9842
        );
    }

    private Restaurant existingRestaurant(UUID restaurantId) {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);
        restaurant.setOwnerUserId(UUID.randomUUID());
        restaurant.setName("Old Name");
        restaurant.setAvailable(true);
        restaurant.setVisible(true);
        return restaurant;
    }
}
