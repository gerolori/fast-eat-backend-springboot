package com.gerolori.fasteat.web.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gerolori.fasteat.domain.entity.Menu;
import com.gerolori.fasteat.domain.entity.Order;
import com.gerolori.fasteat.domain.entity.OrderStatus;
import com.gerolori.fasteat.domain.entity.Restaurant;
import com.gerolori.fasteat.domain.entity.RoleName;
import com.gerolori.fasteat.domain.entity.User;
import com.gerolori.fasteat.domain.repository.MenuRepository;
import com.gerolori.fasteat.domain.repository.OrderRepository;
import com.gerolori.fasteat.domain.repository.UserRepository;
import com.gerolori.fasteat.web.order.dto.CreateOrderItemRequest;
import com.gerolori.fasteat.web.order.dto.CreateOrderRequest;
import com.gerolori.fasteat.web.order.dto.OrderResponse;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class OrderServiceTest {

    private OrderRepository orderRepository;
    private UserRepository userRepository;
    private MenuRepository menuRepository;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderRepository = Mockito.mock(OrderRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        menuRepository = Mockito.mock(MenuRepository.class);
        orderService = new OrderService(orderRepository, userRepository, menuRepository);
    }

    @Test
    void createOrderCreatesPendingOrder() {
        UUID userId = UUID.randomUUID();
        User owner = user(userId, "stripe", "pm_abc");
        UUID menuId = UUID.randomUUID();
        Menu menu = menu(menuId, true, true, true, new BigDecimal("7.50"));

        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));
        when(menuRepository.findAllById(any())).thenReturn(List.of(menu));
        when(orderRepository.save(any())).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(UUID.fromString("77f31f22-4d1f-4f8f-9158-2cce1eb8ca7d"));
            order.setCreatedAt(Instant.parse("2026-04-20T12:30:00Z"));
            return order;
        });

        CreateOrderRequest request = new CreateOrderRequest(
                List.of(new CreateOrderItemRequest(menuId, 2)),
                "Via Torino 21, Milano",
                "No onions"
        );

        OrderService.CreatedOrderResult result = orderService.createOrder(userId, request, null);

        assertThat(result.created()).isTrue();
        assertThat(result.response().status()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.response().total().amount()).isEqualTo("15.00");
        assertThat(result.response().items()).hasSize(1);
        assertThat(result.response().expectedDeliveryTimestamp()).isEqualTo(Instant.parse("2026-04-20T13:15:00Z"));
        assertThat(result.response().currentPosition()).isNull();
    }

    @Test
    void createOrderRequiresPaymentProfile() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user(userId, null, null)));

        CreateOrderRequest request = new CreateOrderRequest(
                List.of(new CreateOrderItemRequest(UUID.randomUUID(), 1)),
                "Via Torino 21, Milano",
                null
        );

        assertThatThrownBy(() -> orderService.createOrder(userId, request, null))
                .isInstanceOf(OrderApiException.class)
                .hasMessage("A payment profile is required before creating an order");
    }

    @Test
    void createOrderRejectsMenusFromHiddenRestaurants() {
        UUID userId = UUID.randomUUID();
        UUID menuId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user(userId, "stripe", "pm_abc")));
        when(menuRepository.findAllById(any())).thenReturn(List.of(menu(menuId, true, true, false, new BigDecimal("7.50"))));

        CreateOrderRequest request = new CreateOrderRequest(
                List.of(new CreateOrderItemRequest(menuId, 1)),
                "Via Torino 21, Milano",
                null
        );

        assertThatThrownBy(() -> orderService.createOrder(userId, request, null))
                .isInstanceOfSatisfying(OrderApiException.class, ex -> {
                    assertThat(ex.getErrorCode()).isEqualTo("ORDER_MENU_UNAVAILABLE");
                    assertThat(ex.getMessage()).contains("hidden restaurants");
                });
    }

    @Test
    void getOrderRejectsOwnershipViolation() {
        UUID requesterId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findByIdAndOwnerUserId(orderId, requesterId)).thenReturn(Optional.empty());
        when(orderRepository.existsById(orderId)).thenReturn(true);

        assertThatThrownBy(() -> orderService.getOrder(requesterId, Set.of(RoleName.CUSTOMER), orderId))
                .isInstanceOf(OrderApiException.class)
                .hasMessage("You are not allowed to access this order");
    }

    @Test
    void transitionRejectsInvalidStateJump() {
        UUID orderId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        Order order = new Order();
        order.setId(orderId);
        order.setOwnerUser(user(ownerId, "stripe", "pm_abc"));
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.transitionOrder(ownerId, Set.of(RoleName.CUSTOMER), orderId, OrderStatus.CONFIRMED))
                .isInstanceOf(OrderApiException.class)
                .hasMessage("Customers may only cancel orders");
    }

    @Test
    void idempotencyReplaysSamePayloadWithoutCreatingDuplicate() {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID menuId = UUID.randomUUID();

        User owner = user(userId, "stripe", "pm_abc");
        Order existing = new Order();
        existing.setId(orderId);
        existing.setOwnerUser(owner);
        existing.setStatus(OrderStatus.PENDING);
        existing.setStatusUpdatedAt(Instant.parse("2026-04-20T12:30:00Z"));
        existing.setDeliveryAddress("Via Torino 21, Milano");
        existing.setTotalAmount(new BigDecimal("7.50"));
        existing.setCreatedAt(Instant.parse("2026-04-20T12:30:00Z"));
        CreateOrderRequest request = new CreateOrderRequest(
                List.of(new CreateOrderItemRequest(menuId, 1)),
                "Via Torino 21, Milano",
                null
        );
        existing.setIdempotencyHash(hashFor(request));
        existing.addItem(orderItem(existing, menu(menuId, true, true, true, new BigDecimal("7.50")), 1));

        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));
        when(orderRepository.findByOwnerUserIdAndIdempotencyKey(userId, "idem-1")).thenReturn(Optional.of(existing));

        OrderService.CreatedOrderResult result = orderService.createOrder(userId, request, "idem-1");

        assertThat(result.created()).isFalse();
        verify(orderRepository, never()).save(any());
    }

    @Test
    void getMyOrdersReturnsPagedHistory() {
        UUID ownerId = UUID.randomUUID();
        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setOwnerUser(user(ownerId, "stripe", "pm_abc"));
        order.setStatus(OrderStatus.COMPLETED);
        order.setCreatedAt(Instant.parse("2026-04-20T12:30:00Z"));
        order.setStatusUpdatedAt(Instant.parse("2026-04-20T12:50:00Z"));
        order.setTotalAmount(new BigDecimal("7.50"));
        order.setDeliveryAddress("Via Torino 21, Milano");

        when(orderRepository.findByOwnerUserIdAndStatusOrderByCreatedAtDesc(ownerId, OrderStatus.COMPLETED, PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of(order), PageRequest.of(0, 20), 1));

        var page = orderService.getMyOrders(ownerId, OrderStatus.COMPLETED, 0, 20);

        assertThat(page.items()).hasSize(1);
        assertThat(page.items().get(0).status()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    void getMyCompletedOrdersDelegatesToCompletedStatusFilter() {
        UUID ownerId = UUID.randomUUID();
        when(orderRepository.findByOwnerUserIdAndStatusOrderByCreatedAtDesc(ownerId, OrderStatus.COMPLETED, PageRequest.of(1, 5)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(1, 5), 0));

        var page = orderService.getMyCompletedOrders(ownerId, 1, 5);

        assertThat(page.items()).isEmpty();
        assertThat(page.page()).isEqualTo(1);
        assertThat(page.size()).isEqualTo(5);
    }

    @Test
    void getAdminOrdersSupportsStatusFiltering() {
        UUID ownerId = UUID.randomUUID();
        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setOwnerUser(user(ownerId, "stripe", "pm_abc"));
        order.setStatus(OrderStatus.PREPARING);
        order.setStatusUpdatedAt(Instant.parse("2026-04-20T12:50:00Z"));
        order.setCreatedAt(Instant.parse("2026-04-20T12:30:00Z"));
        order.setDeliveryAddress("Via Torino 21, Milano");
        order.setTotalAmount(new BigDecimal("9.99"));

        when(orderRepository.findByStatusOrderByCreatedAtDesc(OrderStatus.PREPARING, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(order), PageRequest.of(0, 10), 1));

        var page = orderService.getAdminOrders(OrderStatus.PREPARING, 0, 10);

        assertThat(page.items()).hasSize(1);
        assertThat(page.items().get(0).status()).isEqualTo(OrderStatus.PREPARING);
    }

    @Test
    void transitionOrderAsAdminProgressesOperationalState() {
        UUID orderId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        Order order = new Order();
        order.setId(orderId);
        order.setOwnerUser(user(ownerId, "stripe", "pm_abc"));
        order.setStatus(OrderStatus.CONFIRMED);
        order.setStatusUpdatedAt(Instant.parse("2026-04-20T12:40:00Z"));
        order.setCreatedAt(Instant.parse("2026-04-20T12:30:00Z"));
        order.setDeliveryAddress("Via Torino 21, Milano");
        order.setTotalAmount(new BigDecimal("10.00"));

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.transitionOrderAsAdmin(orderId, OrderStatus.PREPARING);

        assertThat(response.status()).isEqualTo(OrderStatus.PREPARING);
    }

    private User user(UUID id, String provider, String reference) {
        User user = new User();
        user.setId(id);
        user.setPaymentProvider(provider);
        user.setPaymentMethodReference(reference);
        return user;
    }

    private Menu menu(UUID id, boolean menuAvailable, boolean restaurantAvailable, boolean restaurantVisible, BigDecimal price) {
        Restaurant restaurant = new Restaurant();
        restaurant.setAvailable(restaurantAvailable);
        restaurant.setVisible(restaurantVisible);

        Menu menu = new Menu();
        menu.setId(id);
        menu.setAvailable(menuAvailable);
        menu.setRestaurant(restaurant);
        menu.setPrice(price);
        menu.setName("Burger");
        return menu;
    }

    private com.gerolori.fasteat.domain.entity.OrderItem orderItem(Order order, Menu menu, int quantity) {
        com.gerolori.fasteat.domain.entity.OrderItem item = new com.gerolori.fasteat.domain.entity.OrderItem();
        item.setOrder(order);
        item.setMenu(menu);
        item.setMenuName(menu.getName());
        item.setUnitPrice(menu.getPrice());
        item.setQuantity(quantity);
        item.setLineTotal(menu.getPrice().multiply(BigDecimal.valueOf(quantity)));
        return item;
    }

    private String hashFor(CreateOrderRequest request) {
        try {
            String canonical = request.items().stream()
                    .map(item -> item.menuId() + ":" + item.quantity())
                    .sorted()
                    .reduce((a, b) -> a + "|" + b)
                    .orElse("")
                    + "#" + request.deliveryAddress() + "#" + null;
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
