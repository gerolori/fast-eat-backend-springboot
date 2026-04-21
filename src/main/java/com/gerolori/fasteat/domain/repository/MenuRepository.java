package com.gerolori.fasteat.domain.repository;

import com.gerolori.fasteat.domain.entity.Menu;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<Menu, UUID> {

    Page<Menu> findByRestaurantIdAndAvailableTrue(UUID restaurantId, Pageable pageable);

    Page<Menu> findByRestaurantId(UUID restaurantId, Pageable pageable);

    Optional<Menu> findByIdAndAvailableTrue(UUID id);

    boolean existsByIdAndAvailableTrue(UUID id);

    Optional<Menu> findByIdAndRestaurantId(UUID id, UUID restaurantId);
}
