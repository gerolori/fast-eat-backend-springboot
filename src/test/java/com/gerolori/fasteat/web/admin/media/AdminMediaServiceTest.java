package com.gerolori.fasteat.web.admin.media;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.gerolori.fasteat.domain.entity.Menu;
import com.gerolori.fasteat.domain.entity.Restaurant;
import com.gerolori.fasteat.domain.repository.MenuRepository;
import com.gerolori.fasteat.domain.repository.RestaurantRepository;
import com.gerolori.fasteat.web.error.ResourceNotFoundException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminMediaServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;
    @Mock
    private MenuRepository menuRepository;

    private AdminMediaService service;

    @BeforeEach
    void setUp() {
        service = new AdminMediaService(restaurantRepository, menuRepository);
    }

    @Test
    void updateRestaurantImagePersistsOnEntity() {
        UUID restaurantId = UUID.randomUUID();
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));

        var response = service.updateRestaurantImage(restaurantId, " https://cdn.example.com/rest.jpg ");

        assertThat(restaurant.getImageUrl()).isEqualTo("https://cdn.example.com/rest.jpg");
        assertThat(response.resourceId()).isEqualTo(restaurantId);
        assertThat(response.imageUrl()).isEqualTo("https://cdn.example.com/rest.jpg");
    }

    @Test
    void updateMenuImageFailsWhenMenuMissing() {
        UUID menuId = UUID.randomUUID();
        when(menuRepository.findById(menuId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateMenuImage(menuId, "https://cdn.example.com/menu.jpg"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
