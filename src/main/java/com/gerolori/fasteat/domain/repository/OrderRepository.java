package com.gerolori.fasteat.domain.repository;

import com.gerolori.fasteat.domain.entity.Order;
import com.gerolori.fasteat.domain.entity.OrderStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    @EntityGraph(attributePaths = {"items", "items.menu"})
    Optional<Order> findByIdAndOwnerUserId(UUID id, UUID ownerUserId);

    @EntityGraph(attributePaths = {"items", "items.menu"})
    Optional<Order> findByOwnerUserIdAndIdempotencyKey(UUID ownerUserId, String idempotencyKey);

    @EntityGraph(attributePaths = {"items", "items.menu"})
    Optional<Order> findById(UUID id);

    Page<Order> findByOwnerUserIdOrderByCreatedAtDesc(UUID ownerUserId, Pageable pageable);

    Page<Order> findByOwnerUserIdAndStatusOrderByCreatedAtDesc(UUID ownerUserId, OrderStatus status, Pageable pageable);

    Page<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
