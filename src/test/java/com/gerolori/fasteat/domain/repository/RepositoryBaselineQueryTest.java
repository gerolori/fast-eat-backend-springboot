package com.gerolori.fasteat.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.gerolori.fasteat.domain.config.JpaAuditingConfig;
import com.gerolori.fasteat.domain.entity.Menu;
import com.gerolori.fasteat.domain.entity.Order;
import com.gerolori.fasteat.domain.entity.OrderItem;
import com.gerolori.fasteat.domain.entity.OrderStatus;
import com.gerolori.fasteat.domain.entity.Restaurant;
import com.gerolori.fasteat.domain.entity.Role;
import com.gerolori.fasteat.domain.entity.RoleName;
import com.gerolori.fasteat.domain.entity.User;
import java.math.BigDecimal;
import java.util.UUID;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfig.class)
@ActiveProfiles("test")
class RepositoryBaselineQueryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void supportsOwnershipScopedOrderQueriesWithItems() {
        User owner = saveUser("owner-order-query@fasteat.test");
        Restaurant restaurant = saveRestaurant(owner.getId(), "Order Query Kitchen");
        Menu menu = saveMenu(restaurant, "Query Bowl", true);

        Order order = new Order();
        order.setOwnerUser(owner);
        order.setStatus(OrderStatus.PENDING);
        order.setDeliveryAddress("Makati City");
        order.setTotalAmount(new BigDecimal("9.99"));

        OrderItem item = new OrderItem();
        item.setMenu(menu);
        item.setMenuName(menu.getName());
        item.setUnitPrice(new BigDecimal("9.99"));
        item.setQuantity(1);
        item.setLineTotal(new BigDecimal("9.99"));
        order.addItem(item);

        Order persisted = orderRepository.saveAndFlush(order);

        Order ownerScoped = orderRepository.findByIdAndOwnerUserId(persisted.getId(), owner.getId()).orElseThrow();
        assertThat(ownerScoped.getItems()).hasSize(1);

        PageRequest page = PageRequest.of(0, 10);
        assertThat(orderRepository.findByOwnerUserIdOrderByCreatedAtDesc(owner.getId(), page).getTotalElements()).isEqualTo(1);
        assertThat(orderRepository
                        .findByOwnerUserIdAndStatusOrderByCreatedAtDesc(owner.getId(), OrderStatus.PENDING, page)
                        .getTotalElements())
                .isEqualTo(1);
    }

    @Test
    void supportsBaselineUserRestaurantAndMenuPredicates() {
        User owner = saveUser("owner-catalog-query@fasteat.test");
        saveUser("secondary-catalog-query@fasteat.test");

        Restaurant restaurant = saveRestaurant(owner.getId(), "Catalog Query Kitchen");
        Menu availableMenu = saveMenu(restaurant, "Available Menu", true);
        saveMenu(restaurant, "Unavailable Menu", false);

        PageRequest page = PageRequest.of(0, 10);
        assertThat(userRepository.findAllByOrderByCreatedAtDesc(page).getTotalElements()).isGreaterThanOrEqualTo(2);

        assertThat(restaurantRepository.findByOwnerUserId(owner.getId(), page).getTotalElements()).isEqualTo(1);
        assertThat(restaurantRepository.findByIdAndOwnerUserId(restaurant.getId(), owner.getId())).isPresent();

        assertThat(menuRepository.findByRestaurantId(restaurant.getId(), page).getTotalElements()).isEqualTo(2);
        assertThat(menuRepository.findByIdAndAvailableTrue(availableMenu.getId())).isPresent();
        assertThat(menuRepository.existsByIdAndAvailableTrue(availableMenu.getId())).isTrue();
    }

    @Test
    void supportsRuntimeCatalogVisibilityAvailabilityAndUserLookups() {
        User owner = saveUser("owner-runtime-query@fasteat.test");
        saveUser("Case.User@fasteat.test");

        Restaurant visibleRestaurant = saveRestaurant(owner.getId(), "Visible Kitchen");
        visibleRestaurant.setCity("Makati");
        visibleRestaurant.setVisible(true);
        visibleRestaurant.setAvailable(true);
        visibleRestaurant = restaurantRepository.saveAndFlush(visibleRestaurant);

        Restaurant hiddenRestaurant = saveRestaurant(owner.getId(), "Hidden Kitchen");
        hiddenRestaurant.setCity("Makati");
        hiddenRestaurant.setVisible(false);
        hiddenRestaurant = restaurantRepository.saveAndFlush(hiddenRestaurant);

        Restaurant unavailableRestaurant = saveRestaurant(owner.getId(), "Unavailable Kitchen");
        unavailableRestaurant.setCity("Makati");
        unavailableRestaurant.setVisible(true);
        unavailableRestaurant.setAvailable(false);
        unavailableRestaurant = restaurantRepository.saveAndFlush(unavailableRestaurant);

        Menu availableMenu = saveMenu(visibleRestaurant, "Visible Menu", true);
        Menu unavailableMenu = saveMenu(visibleRestaurant, "Hidden Menu", false);

        PageRequest page = PageRequest.of(0, 20);
        assertThat(userRepository.existsByEmailIgnoreCase("case.user@FASTEAT.TEST")).isTrue();
        assertThat(userRepository.findByEmailIgnoreCase("CASE.USER@fasteat.test")).isPresent();

        assertThat(restaurantRepository.findByVisibleTrue(page).map(Restaurant::getId).getContent())
                .contains(visibleRestaurant.getId(), unavailableRestaurant.getId())
                .doesNotContain(hiddenRestaurant.getId());
        assertThat(restaurantRepository.findByCityIgnoreCaseAndVisibleTrue("mAKAtI", page)
                        .map(Restaurant::getId)
                        .getContent())
                .contains(visibleRestaurant.getId(), unavailableRestaurant.getId())
                .doesNotContain(hiddenRestaurant.getId());
        assertThat(restaurantRepository.findByIdAndVisibleTrue(hiddenRestaurant.getId())).isEmpty();
        assertThat(restaurantRepository.findByIdAndAvailableTrue(visibleRestaurant.getId())).isPresent();
        assertThat(restaurantRepository.findByIdAndAvailableTrue(unavailableRestaurant.getId())).isEmpty();

        assertThat(menuRepository.findByRestaurantIdAndAvailableTrue(visibleRestaurant.getId(), page).getTotalElements())
                .isEqualTo(1);
        assertThat(menuRepository.findByIdAndAvailableTrue(availableMenu.getId())).isPresent();
        assertThat(menuRepository.findByIdAndAvailableTrue(unavailableMenu.getId())).isEmpty();
        assertThat(menuRepository.existsByIdAndAvailableTrue(unavailableMenu.getId())).isFalse();
    }

    @Test
    void supportsIdempotencyAndStatusQueriesUsedByOrderFlows() {
        User owner = saveUser("owner-idempotency-query@fasteat.test");
        Restaurant restaurant = saveRestaurant(owner.getId(), "Idempotency Kitchen");
        Menu menu = saveMenu(restaurant, "Idempotent Bowl", true);

        Order pendingOrder = saveOrder(owner, menu, "idem-1", OrderStatus.PENDING);
        saveOrder(owner, menu, "idem-2", OrderStatus.CONFIRMED);

        entityManager.clear();

        Order idempotentLookup = orderRepository
                .findByOwnerUserIdAndIdempotencyKey(owner.getId(), "idem-1")
                .orElseThrow();
        assertThat(Hibernate.isInitialized(idempotentLookup.getItems())).isTrue();
        assertThat(idempotentLookup.getItems()).hasSize(1);
        assertThat(Hibernate.isInitialized(idempotentLookup.getItems().get(0).getMenu())).isTrue();

        Order adminLookup = orderRepository.findById(pendingOrder.getId()).orElseThrow();
        assertThat(Hibernate.isInitialized(adminLookup.getItems())).isTrue();
        assertThat(adminLookup.getItems()).hasSize(1);

        PageRequest page = PageRequest.of(0, 10);
        assertThat(orderRepository.findByStatusOrderByCreatedAtDesc(OrderStatus.PENDING, page).getTotalElements())
                .isEqualTo(1);
    }

    private User saveUser(String email) {
        Role customerRole = roleRepository.findByName(RoleName.CUSTOMER)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.CUSTOMER)));

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("hashed-password");
        user.getRoles().add(customerRole);
        return userRepository.saveAndFlush(user);
    }

    private Restaurant saveRestaurant(UUID ownerUserId, String name) {
        Restaurant restaurant = new Restaurant();
        restaurant.setOwnerUserId(ownerUserId);
        restaurant.setName(name);
        restaurant.setAvailable(true);
        return restaurantRepository.saveAndFlush(restaurant);
    }

    private Menu saveMenu(Restaurant restaurant, String name, boolean available) {
        Menu menu = new Menu();
        menu.setRestaurant(restaurant);
        menu.setName(name);
        menu.setPrice(new BigDecimal("9.99"));
        menu.setAvailable(available);
        return menuRepository.saveAndFlush(menu);
    }

    private Order saveOrder(User owner, Menu menu, String idempotencyKey, OrderStatus status) {
        Order order = new Order();
        order.setOwnerUser(owner);
        order.setStatus(status);
        order.setDeliveryAddress("Makati City");
        order.setIdempotencyKey(idempotencyKey);
        order.setIdempotencyHash("hash-" + idempotencyKey);
        order.setTotalAmount(new BigDecimal("9.99"));

        OrderItem item = new OrderItem();
        item.setMenu(menu);
        item.setMenuName(menu.getName());
        item.setUnitPrice(new BigDecimal("9.99"));
        item.setQuantity(1);
        item.setLineTotal(new BigDecimal("9.99"));
        order.addItem(item);

        return orderRepository.saveAndFlush(order);
    }
}
