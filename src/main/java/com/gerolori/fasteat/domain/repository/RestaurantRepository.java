package com.gerolori.fasteat.domain.repository;

import com.gerolori.fasteat.domain.entity.Restaurant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {

    Page<Restaurant> findByVisibleTrue(Pageable pageable);

    Page<Restaurant> findByCityIgnoreCaseAndVisibleTrue(String city, Pageable pageable);

    Page<Restaurant> findByOwnerUserId(UUID ownerUserId, Pageable pageable);

    Optional<Restaurant> findByIdAndOwnerUserId(UUID id, UUID ownerUserId);

    Optional<Restaurant> findByIdAndAvailableTrue(UUID id);

    Optional<Restaurant> findByIdAndVisibleTrue(UUID id);

    boolean existsByOwnerUserIdAndNameIgnoreCase(UUID ownerUserId, String name);

    boolean existsByOwnerUserIdAndNameIgnoreCaseAndIdNot(UUID ownerUserId, String name, UUID id);
}
