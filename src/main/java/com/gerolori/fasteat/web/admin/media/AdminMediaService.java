package com.gerolori.fasteat.web.admin.media;

import com.gerolori.fasteat.domain.entity.Menu;
import com.gerolori.fasteat.domain.entity.Restaurant;
import com.gerolori.fasteat.domain.repository.MenuRepository;
import com.gerolori.fasteat.domain.repository.RestaurantRepository;
import com.gerolori.fasteat.web.error.ResourceNotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminMediaService {

    private final RestaurantRepository restaurantRepository;
    private final MenuRepository menuRepository;

    public AdminMediaService(RestaurantRepository restaurantRepository, MenuRepository menuRepository) {
        this.restaurantRepository = restaurantRepository;
        this.menuRepository = menuRepository;
    }

    public AdminImageUpdateResponse updateRestaurantImage(UUID restaurantId, String imageUrl) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + restaurantId));

        restaurant.setImageUrl(imageUrl);
        return new AdminImageUpdateResponse(restaurant.getId(), restaurant.getImageUrl(), restaurant.getUpdatedAt());
    }

    public AdminImageUpdateResponse updateMenuImage(UUID menuId, String imageUrl) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu not found: " + menuId));

        menu.setImageUrl(imageUrl);
        return new AdminImageUpdateResponse(menu.getId(), menu.getImageUrl(), menu.getUpdatedAt());
    }
}
