package com.gerolori.fasteat.web.admin.menu;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gerolori.fasteat.domain.entity.Menu;
import com.gerolori.fasteat.domain.entity.Restaurant;
import com.gerolori.fasteat.domain.repository.MenuRepository;
import com.gerolori.fasteat.domain.repository.RestaurantRepository;
import com.gerolori.fasteat.web.error.ResourceNotFoundException;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminMenuServiceTest {

    @Mock
    private MenuRepository menuRepository;
    @Mock
    private RestaurantRepository restaurantRepository;

    private AdminMenuService service;

    @BeforeEach
    void setUp() {
        service = new AdminMenuService(menuRepository, restaurantRepository);
    }

    @Test
    void createMenuPersistsUnderRestaurant() {
        UUID restaurantId = UUID.randomUUID();
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(menuRepository.save(any(Menu.class))).thenAnswer(invocation -> invocation.getArgument(0, Menu.class));

        AdminMenuUpsertRequest request = new AdminMenuUpsertRequest(
                "Chicken Bowl",
                "Balanced meal",
                "Detail",
                "MAIN",
                new BigDecimal("8.99"),
                true,
                " https://cdn.example.com/menu.png "
        );

        service.createMenu(restaurantId, request);

        ArgumentCaptor<Menu> captor = ArgumentCaptor.forClass(Menu.class);
        verify(menuRepository).save(captor.capture());
        assertThat(captor.getValue().getRestaurant().getId()).isEqualTo(restaurantId);
        assertThat(captor.getValue().isActive()).isTrue();
        assertThat(captor.getValue().getImageUrl()).isEqualTo("https://cdn.example.com/menu.png");
    }

    @Test
    void deleteMenuRemovesRestaurantScopedMenu() {
        UUID restaurantId = UUID.randomUUID();
        UUID menuId = UUID.randomUUID();
        Menu menu = new Menu();
        menu.setId(menuId);

        when(menuRepository.findByIdAndRestaurantId(menuId, restaurantId)).thenReturn(Optional.of(menu));

        service.deleteMenu(restaurantId, menuId);

        verify(menuRepository).delete(menu);
    }

    @Test
    void setMenuActiveTogglesStatus() {
        UUID restaurantId = UUID.randomUUID();
        UUID menuId = UUID.randomUUID();
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);
        Menu menu = new Menu();
        menu.setId(menuId);
        menu.setRestaurant(restaurant);
        menu.setName("Chicken Bowl");
        menu.setPrice(new BigDecimal("8.99"));

        when(menuRepository.findByIdAndRestaurantId(menuId, restaurantId)).thenReturn(Optional.of(menu));

        var response = service.setMenuActive(restaurantId, menuId, false);

        assertThat(menu.isActive()).isFalse();
        assertThat(response.isActive()).isFalse();
    }

    @Test
    void updateMenuFailsWhenMissing() {
        UUID restaurantId = UUID.randomUUID();
        UUID menuId = UUID.randomUUID();
        when(menuRepository.findByIdAndRestaurantId(menuId, restaurantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateMenu(
                restaurantId,
                menuId,
                new AdminMenuUpsertRequest("A", null, null, null, new BigDecimal("1.00"), true, null)
        )).isInstanceOf(ResourceNotFoundException.class);
    }
}
