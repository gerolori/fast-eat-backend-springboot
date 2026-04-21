package com.gerolori.fasteat.web.order;

import com.gerolori.fasteat.domain.entity.Menu;
import com.gerolori.fasteat.domain.entity.Order;
import com.gerolori.fasteat.domain.entity.OrderItem;
import com.gerolori.fasteat.domain.entity.OrderStatus;
import com.gerolori.fasteat.domain.entity.RoleName;
import com.gerolori.fasteat.domain.entity.User;
import com.gerolori.fasteat.domain.repository.MenuRepository;
import com.gerolori.fasteat.domain.repository.OrderRepository;
import com.gerolori.fasteat.domain.repository.UserRepository;
import com.gerolori.fasteat.web.order.dto.CreateOrderItemRequest;
import com.gerolori.fasteat.web.order.dto.CreateOrderRequest;
import com.gerolori.fasteat.web.order.dto.OrderItemResponse;
import com.gerolori.fasteat.web.order.dto.OrderMoneyResponse;
import com.gerolori.fasteat.web.order.dto.OrderResponse;
import com.gerolori.fasteat.web.order.dto.OrderTrackingPositionResponse;
import com.gerolori.fasteat.web.shared.PagedResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private static final long DEFAULT_EXPECTED_DELIVERY_MINUTES = 45L;

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            OrderStatus.PENDING, Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
            OrderStatus.CONFIRMED, Set.of(OrderStatus.PREPARING, OrderStatus.CANCELLED),
            OrderStatus.PREPARING, Set.of(OrderStatus.READY_FOR_PICKUP, OrderStatus.CANCELLED),
            OrderStatus.READY_FOR_PICKUP, Set.of(OrderStatus.COMPLETED)
    );

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final MenuRepository menuRepository;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository, MenuRepository menuRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.menuRepository = menuRepository;
    }

    @Transactional
    public CreatedOrderResult createOrder(UUID userId, CreateOrderRequest request, String idempotencyKey) {
        User owner = findRequiredUser(userId);
        ensurePaymentProfile(owner);

        String normalizedDeliveryAddress = normalize(request.deliveryAddress());
        if (normalizedDeliveryAddress == null) {
            throw new OrderApiException("ORDER_INVALID_DELIVERY_ADDRESS", HttpStatus.UNPROCESSABLE_ENTITY, "deliveryAddress must not be blank");
        }

        String requestHash = buildRequestHash(request);
        String normalizedKey = normalize(idempotencyKey);
        if (normalizedKey != null) {
            Order existing = orderRepository.findByOwnerUserIdAndIdempotencyKey(userId, normalizedKey).orElse(null);
            if (existing != null) {
                if (!requestHash.equals(existing.getIdempotencyHash())) {
                    throw new OrderApiException("ORDER_DUPLICATE_REQUEST", HttpStatus.CONFLICT, "Idempotency key has already been used for a different request");
                }
                return new CreatedOrderResult(toResponse(existing), false);
            }
        }

        Order saved = persistOrder(owner, request, normalizedDeliveryAddress, normalizedKey, requestHash);
        return new CreatedOrderResult(toResponse(saved), true);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID requesterUserId, Set<RoleName> roles, UUID orderId) {
        boolean isAdmin = roles.contains(RoleName.ADMIN);
        Order order = isAdmin
                ? orderRepository.findById(orderId).orElseThrow(() -> notFound(orderId))
                : orderRepository.findByIdAndOwnerUserId(orderId, requesterUserId).orElse(null);

        if (order == null) {
            boolean exists = orderRepository.existsById(orderId);
            if (exists && !isAdmin) {
                throw new OrderApiException("AUTHZ_OWNERSHIP_DENIED", HttpStatus.FORBIDDEN, "You are not allowed to access this order");
            }
            throw notFound(orderId);
        }

        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public PagedResponse<OrderResponse> getMyOrders(UUID ownerUserId, OrderStatus status, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<Order> orders = status == null
                ? orderRepository.findByOwnerUserIdOrderByCreatedAtDesc(ownerUserId, pageable)
                : orderRepository.findByOwnerUserIdAndStatusOrderByCreatedAtDesc(ownerUserId, status, pageable);

        return PagedResponse.from(orders.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<OrderResponse> getMyCompletedOrders(UUID ownerUserId, int page, int size) {
        return getMyOrders(ownerUserId, OrderStatus.COMPLETED, page, size);
    }

    @Transactional(readOnly = true)
    public PagedResponse<OrderResponse> getAdminOrders(OrderStatus status, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<Order> orders = status == null
                ? orderRepository.findAllByOrderByCreatedAtDesc(pageable)
                : orderRepository.findByStatusOrderByCreatedAtDesc(status, pageable);

        return PagedResponse.from(orders.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public OrderResponse getAdminOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> notFound(orderId));
        return toResponse(order);
    }

    @Transactional
    public OrderResponse transitionOrderAsAdmin(UUID orderId, OrderStatus targetStatus) {
        return transitionOrder(null, Set.of(RoleName.ADMIN), orderId, targetStatus);
    }

    @Transactional
    public OrderResponse transitionOrder(UUID requesterUserId, Set<RoleName> roles, UUID orderId, OrderStatus targetStatus) {
        if (targetStatus == null) {
            throw new OrderApiException("ORDER_INVALID_TRANSITION", HttpStatus.CONFLICT, "Target status is required");
        }

        Order order = orderRepository.findById(orderId).orElseThrow(() -> notFound(orderId));
        boolean isAdmin = roles.contains(RoleName.ADMIN);
        boolean isOwner = order.getOwnerUser().getId().equals(requesterUserId);
        if (!isAdmin && !isOwner) {
            throw new OrderApiException("AUTHZ_OWNERSHIP_DENIED", HttpStatus.FORBIDDEN, "You are not allowed to update this order");
        }

        if (!isAdmin && targetStatus != OrderStatus.CANCELLED) {
            throw new OrderApiException("ORDER_INVALID_TRANSITION", HttpStatus.CONFLICT, "Customers may only cancel orders");
        }

        OrderStatus currentStatus = order.getStatus();
        Set<OrderStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        if (!allowed.contains(targetStatus)) {
            throw new OrderApiException(
                    "ORDER_INVALID_TRANSITION",
                    HttpStatus.CONFLICT,
                    "Cannot transition from " + currentStatus + " to " + targetStatus
            );
        }

        order.setStatus(targetStatus);
        order.setStatusUpdatedAt(Instant.now());
        if (targetStatus == OrderStatus.CANCELLED) {
            order.setCancelledAt(Instant.now());
        }
        if (targetStatus == OrderStatus.COMPLETED) {
            order.setCompletedAt(Instant.now());
        }

        return toResponse(orderRepository.save(order));
    }

    private Order persistOrder(
            User owner,
            CreateOrderRequest request,
            String normalizedDeliveryAddress,
            String normalizedKey,
            String requestHash
    ) {
        Map<UUID, Integer> quantities = mergeQuantities(request.items());
        List<Menu> menus = menuRepository.findAllById(quantities.keySet());
        if (menus.size() != quantities.size()) {
            throw new OrderApiException("ORDER_NOT_FOUND", HttpStatus.NOT_FOUND, "One or more menu items were not found");
        }

        Order order = new Order();
        order.setOwnerUser(owner);
        order.setDeliveryAddress(normalizedDeliveryAddress);
        order.setNote(normalize(request.note()));
        order.setIdempotencyKey(normalizedKey);
        order.setIdempotencyHash(requestHash);
        order.setStatus(OrderStatus.PENDING);
        order.setStatusUpdatedAt(Instant.now());

        BigDecimal total = BigDecimal.ZERO;
        for (Menu menu : menus) {
            validateMenuForOrder(menu);
            int quantity = quantities.get(menu.getId());

            OrderItem item = new OrderItem();
            item.setMenu(menu);
            item.setMenuName(menu.getName());
            item.setUnitPrice(menu.getPrice());
            item.setQuantity(quantity);
            item.setLineTotal(menu.getPrice().multiply(BigDecimal.valueOf(quantity)));
            order.addItem(item);
            total = total.add(item.getLineTotal());
        }

        order.setTotalAmount(total.setScale(2, RoundingMode.HALF_UP));

        try {
            return orderRepository.save(order);
        } catch (DataIntegrityViolationException ex) {
            if (normalizedKey == null) {
                throw ex;
            }
            Order existing = orderRepository.findByOwnerUserIdAndIdempotencyKey(owner.getId(), normalizedKey)
                    .orElseThrow(() -> ex);
            if (!requestHash.equals(existing.getIdempotencyHash())) {
                throw new OrderApiException("ORDER_DUPLICATE_REQUEST", HttpStatus.CONFLICT, "Idempotency key has already been used for a different request");
            }
            return existing;
        }
    }

    private Map<UUID, Integer> mergeQuantities(List<CreateOrderItemRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new OrderApiException("ORDER_INVALID_ITEM_QUANTITY", HttpStatus.UNPROCESSABLE_ENTITY, "Order must contain at least one item");
        }

        Map<UUID, Integer> merged = new LinkedHashMap<>();
        for (CreateOrderItemRequest item : items) {
            if (item.menuId() == null) {
                throw new OrderApiException("ORDER_NOT_FOUND", HttpStatus.NOT_FOUND, "menuId is required");
            }
            if (item.quantity() == null || item.quantity() <= 0) {
                throw new OrderApiException("ORDER_INVALID_ITEM_QUANTITY", HttpStatus.UNPROCESSABLE_ENTITY, "quantity must be greater than zero");
            }
            merged.merge(item.menuId(), item.quantity(), Integer::sum);
        }
        return merged;
    }

    private void validateMenuForOrder(Menu menu) {
        boolean restaurantOrderable = menu.getRestaurant() != null
                && menu.getRestaurant().isAvailable()
                && menu.getRestaurant().isVisible();
        if (!menu.isAvailable() || !restaurantOrderable) {
            throw new OrderApiException(
                    "ORDER_MENU_UNAVAILABLE",
                    HttpStatus.CONFLICT,
                    "One or more menu items are unavailable or belong to hidden restaurants"
            );
        }
    }

    private void ensurePaymentProfile(User owner) {
        if (normalize(owner.getPaymentProvider()) == null || normalize(owner.getPaymentMethodReference()) == null) {
            throw new OrderApiException(
                    "ORDER_PAYMENT_PROFILE_REQUIRED",
                    HttpStatus.CONFLICT,
                    "A payment profile is required before creating an order"
            );
        }
    }

    private User findRequiredUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new OrderApiException("ORDER_NOT_FOUND", HttpStatus.NOT_FOUND, "User not found"));
    }

    private OrderApiException notFound(UUID orderId) {
        return new OrderApiException("ORDER_NOT_FOUND", HttpStatus.NOT_FOUND, "Order not found: " + orderId);
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = new ArrayList<>(order.getItems().size());
        for (OrderItem item : order.getItems()) {
            items.add(new OrderItemResponse(
                    item.getMenu().getId(),
                    item.getMenuName(),
                    money(item.getUnitPrice()),
                    item.getQuantity(),
                    money(item.getLineTotal())
            ));
        }

        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getStatusUpdatedAt(),
                expectedDeliveryTimestamp(order),
                currentPosition(order),
                order.getOwnerUser().getId(),
                items,
                money(order.getTotalAmount()),
                order.getDeliveryAddress(),
                order.getNote(),
                order.getCreatedAt()
        );
    }

    private Instant expectedDeliveryTimestamp(Order order) {
        if (order.getStatus() == OrderStatus.CANCELLED) {
            return null;
        }
        Instant anchor = order.getCreatedAt() != null ? order.getCreatedAt() : order.getStatusUpdatedAt();
        if (anchor == null) {
            return null;
        }
        return anchor.plusSeconds(DEFAULT_EXPECTED_DELIVERY_MINUTES * 60);
    }

    private OrderTrackingPositionResponse currentPosition(Order order) {
        return null;
    }

    private OrderMoneyResponse money(BigDecimal value) {
        BigDecimal normalized = value == null ? BigDecimal.ZERO : value.setScale(2, RoundingMode.HALF_UP);
        return new OrderMoneyResponse(normalized.toPlainString(), "USD");
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String buildRequestHash(CreateOrderRequest request) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String canonical = canonicalPayload(request);
            byte[] hash = digest.digest(canonical.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", ex);
        }
    }

    private String canonicalPayload(CreateOrderRequest request) {
        List<String> lines = request.items().stream()
                .sorted(Comparator.comparing(CreateOrderItemRequest::menuId))
                .map(item -> item.menuId() + ":" + item.quantity())
                .toList();
        return String.join("|", lines)
                + "#"
                + normalize(request.deliveryAddress())
                + "#"
                + normalize(request.note());
    }

    public record CreatedOrderResult(OrderResponse response, boolean created) {
    }
}
