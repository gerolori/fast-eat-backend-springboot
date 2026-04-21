package com.gerolori.fasteat.web.admin.menu;

import com.gerolori.fasteat.domain.entity.Menu;
import com.gerolori.fasteat.domain.entity.Restaurant;
import com.gerolori.fasteat.domain.repository.MenuRepository;
import com.gerolori.fasteat.domain.repository.RestaurantRepository;
import com.gerolori.fasteat.web.error.ResourceNotFoundException;
import com.gerolori.fasteat.web.menu.dto.MoneyResponse;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminMenuService {

    private static final String DEFAULT_CURRENCY = "USD";

    private final MenuRepository menuRepository;
    private final RestaurantRepository restaurantRepository;

    public AdminMenuService(MenuRepository menuRepository, RestaurantRepository restaurantRepository) {
        this.menuRepository = menuRepository;
        this.restaurantRepository = restaurantRepository;
    }

    public AdminMenuResponse createMenu(UUID restaurantId, AdminMenuUpsertRequest request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + restaurantId));

        Menu menu = new Menu();
        menu.setRestaurant(restaurant);
        applyUpsert(menu, request);
        return toResponse(menuRepository.save(menu));
    }

    public AdminMenuResponse updateMenu(UUID restaurantId, UUID menuId, AdminMenuUpsertRequest request) {
        Menu menu = menuRepository.findByIdAndRestaurantId(menuId, restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu not found: " + menuId));

        applyUpsert(menu, request);
        return toResponse(menu);
    }

    public void deleteMenu(UUID restaurantId, UUID menuId) {
        Menu menu = menuRepository.findByIdAndRestaurantId(menuId, restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu not found: " + menuId));

        menuRepository.delete(menu);
    }

    public AdminMenuResponse setMenuAvailability(UUID restaurantId, UUID menuId, boolean available) {
        Menu menu = menuRepository.findByIdAndRestaurantId(menuId, restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu not found: " + menuId));
        menu.setAvailable(available);
        return toResponse(menu);
    }

    public AdminMenuResponse setMenuActive(UUID restaurantId, UUID menuId, boolean active) {
        Menu menu = menuRepository.findByIdAndRestaurantId(menuId, restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu not found: " + menuId));
        menu.setActive(active);
        return toResponse(menu);
    }

    private void applyUpsert(Menu menu, AdminMenuUpsertRequest request) {
        menu.setName(request.name().trim());
        menu.setSummary(request.summary());
        menu.setDescription(request.description());
        menu.setCategory(request.category());
        menu.setPrice(request.price());
        menu.setImageUrl(request.imageUrl());
        menu.setAvailable(request.available() == null || request.available());
        if (menu.getId() == null) {
            menu.setActive(true);
        }
    }

    private AdminMenuResponse toResponse(Menu menu) {
        return new AdminMenuResponse(
                menu.getId(),
                menu.getRestaurant().getId(),
                menu.getName(),
                menu.getSummary(),
                menu.getDescription(),
                menu.getCategory(),
                new MoneyResponse(menu.getPrice().toPlainString(), DEFAULT_CURRENCY),
                menu.isAvailable(),
                menu.isActive(),
                menu.getImageUrl(),
                menu.getUpdatedAt()
        );
    }
}
