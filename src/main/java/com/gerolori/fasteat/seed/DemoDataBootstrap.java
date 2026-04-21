package com.gerolori.fasteat.seed;

import com.gerolori.fasteat.domain.entity.Menu;
import com.gerolori.fasteat.domain.entity.Order;
import com.gerolori.fasteat.domain.entity.OrderItem;
import com.gerolori.fasteat.domain.entity.OrderStatus;
import com.gerolori.fasteat.domain.entity.Restaurant;
import com.gerolori.fasteat.domain.entity.Role;
import com.gerolori.fasteat.domain.entity.RoleName;
import com.gerolori.fasteat.domain.entity.User;
import com.gerolori.fasteat.domain.repository.MenuRepository;
import com.gerolori.fasteat.domain.repository.OrderRepository;
import com.gerolori.fasteat.domain.repository.RestaurantRepository;
import com.gerolori.fasteat.domain.repository.RoleRepository;
import com.gerolori.fasteat.domain.repository.UserRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile({"local", "test"})
@ConditionalOnProperty(name = "fasteat.seed.demo.enabled", havingValue = "true")
public class DemoDataBootstrap implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuRepository menuRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoDataBootstrap(
            UserRepository userRepository,
            RoleRepository roleRepository,
            RestaurantRepository restaurantRepository,
            MenuRepository menuRepository,
            OrderRepository orderRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.restaurantRepository = restaurantRepository;
        this.menuRepository = menuRepository;
        this.orderRepository = orderRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Role adminRole = ensureRole(RoleName.ADMIN);
        Role customerRole = ensureRole(RoleName.CUSTOMER);

        User admin = ensureUser("admin@fasteat.local", "admin1234", Set.of(adminRole, customerRole));
        User owner = ensureUser("owner@fasteat.local", "owner1234", Set.of(customerRole));
        User customer = ensureUser("customer@fasteat.local", "customer1234", Set.of(customerRole));

        Restaurant kitchenOne = ensureRestaurant(owner, "Fast Eat Downtown Kitchen", "Makati", "CASUAL_DINING");
        Restaurant kitchenTwo = ensureRestaurant(admin, "Fast Eat Uptown Kitchen", "Taguig", "FUSION");

        Menu burger = ensureMenu(kitchenOne, "Classic Burger Combo", new BigDecimal("12.50"), true);
        Menu riceBowl = ensureMenu(kitchenOne, "Chicken Rice Bowl", new BigDecimal("9.90"), true);
        Menu pasta = ensureMenu(kitchenTwo, "Creamy Mushroom Pasta", new BigDecimal("11.20"), true);

        seedOrdersIfMissing(customer, List.of(burger, riceBowl, pasta));
    }

    private Role ensureRole(RoleName roleName) {
        return roleRepository.findByName(roleName).orElseGet(() -> roleRepository.save(new Role(roleName)));
    }

    private User ensureUser(String email, String rawPassword, Set<Role> roles) {
        return userRepository.findByEmailIgnoreCase(email).orElseGet(() -> {
            User user = new User();
            user.setEmail(email);
            user.setPasswordHash(passwordEncoder.encode(rawPassword));
            user.getRoles().addAll(roles);
            return userRepository.save(user);
        });
    }

    private Restaurant ensureRestaurant(User owner, String name, String city, String category) {
        return restaurantRepository.findByOwnerUserId(owner.getId(), PageRequest.of(0, 20)).stream()
                .filter(restaurant -> restaurant.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> {
                    Restaurant restaurant = new Restaurant();
                    restaurant.setOwnerUserId(owner.getId());
                    restaurant.setName(name);
                    restaurant.setCity(city);
                    restaurant.setCountry("Philippines");
                    restaurant.setState("Metro Manila");
                    restaurant.setCategory(category);
                    restaurant.setSummary("Seeded demo restaurant");
                    restaurant.setDescription("Deterministic seeded restaurant for local and test profile.");
                    restaurant.setAvailable(true);
                    return restaurantRepository.save(restaurant);
                });
    }

    private Menu ensureMenu(Restaurant restaurant, String name, BigDecimal price, boolean available) {
        return menuRepository.findByRestaurantId(restaurant.getId(), PageRequest.of(0, 40)).stream()
                .filter(menu -> menu.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> {
                    Menu menu = new Menu();
                    menu.setRestaurant(restaurant);
                    menu.setName(name);
                    menu.setPrice(price);
                    menu.setAvailable(available);
                    menu.setCategory("MAIN");
                    menu.setSummary("Seeded demo menu");
                    return menuRepository.save(menu);
                });
    }

    private void seedOrdersIfMissing(User customer, List<Menu> menus) {
        boolean alreadySeeded = orderRepository.findByOwnerUserIdOrderByCreatedAtDesc(customer.getId(), PageRequest.of(0, 1))
                .hasContent();
        if (alreadySeeded) {
            return;
        }

        List<Menu> sortedMenus = menus.stream()
                .sorted(Comparator.comparing(Menu::getName))
                .toList();

        createSeedOrder(customer, sortedMenus.get(0), OrderStatus.PENDING, null);
        createSeedOrder(customer, sortedMenus.get(1), OrderStatus.CONFIRMED, null);
        createSeedOrder(customer, sortedMenus.get(2), OrderStatus.PREPARING, null);
        createSeedOrder(customer, sortedMenus.get(0), OrderStatus.READY_FOR_PICKUP, null);
        createSeedOrder(customer, sortedMenus.get(1), OrderStatus.COMPLETED, Instant.now().minusSeconds(1800));
        createSeedOrder(customer, sortedMenus.get(2), OrderStatus.CANCELLED, Instant.now().minusSeconds(1200));
    }

    private void createSeedOrder(User customer, Menu menu, OrderStatus status, Instant terminalTimestamp) {
        Order order = new Order();
        order.setOwnerUser(customer);
        order.setStatus(status);
        order.setStatusUpdatedAt(Instant.now());
        order.setDeliveryAddress("Seed Address - Metro Manila");
        order.setNote("Seeded " + status.name() + " order");

        OrderItem item = new OrderItem();
        item.setMenu(menu);
        item.setMenuName(menu.getName());
        item.setUnitPrice(menu.getPrice());
        item.setQuantity(1);
        item.setLineTotal(menu.getPrice());

        order.addItem(item);
        order.setTotalAmount(item.getLineTotal());

        if (status == OrderStatus.COMPLETED) {
            order.setCompletedAt(terminalTimestamp);
        }
        if (status == OrderStatus.CANCELLED) {
            order.setCancelledAt(terminalTimestamp);
        }

        orderRepository.save(order);
    }
}
