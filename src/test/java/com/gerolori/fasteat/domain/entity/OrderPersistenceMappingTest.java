package com.gerolori.fasteat.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.gerolori.fasteat.domain.config.JpaAuditingConfig;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfig.class)
@ActiveProfiles("test")
class OrderPersistenceMappingTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void persistsOrderOwnerStatusAndLineItems() {
        Role customerRole = entityManager.persistFlushFind(new Role(RoleName.CUSTOMER));

        User customer = new User();
        customer.setEmail("order-owner@fasteat.test");
        customer.setPasswordHash("hashed-password");
        customer.getRoles().add(customerRole);
        customer = entityManager.persistFlushFind(customer);

        Restaurant restaurant = new Restaurant();
        restaurant.setOwnerUserId(customer.getId());
        restaurant.setName("Fast Eat Hub");
        restaurant.setAvailable(true);
        restaurant = entityManager.persistFlushFind(restaurant);

        Menu menu = new Menu();
        menu.setRestaurant(restaurant);
        menu.setName("Chicken Rice Bowl");
        menu.setPrice(new BigDecimal("7.50"));
        menu.setAvailable(true);
        menu = entityManager.persistFlushFind(menu);

        Order order = new Order();
        order.setOwnerUser(customer);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setStatusUpdatedAt(Instant.parse("2026-04-20T12:45:00Z"));
        order.setDeliveryAddress("Via Torino 21, Milano");
        order.setNote("No onions");
        order.setTotalAmount(new BigDecimal("15.00"));

        OrderItem first = new OrderItem();
        first.setMenu(menu);
        first.setMenuName(menu.getName());
        first.setUnitPrice(new BigDecimal("7.50"));
        first.setQuantity(1);
        first.setLineTotal(new BigDecimal("7.50"));

        OrderItem second = new OrderItem();
        second.setMenu(menu);
        second.setMenuName(menu.getName());
        second.setUnitPrice(new BigDecimal("7.50"));
        second.setQuantity(1);
        second.setLineTotal(new BigDecimal("7.50"));

        order.addItem(first);
        order.addItem(second);

        Order persisted = entityManager.persistFlushFind(order);
        entityManager.clear();

        Order reloaded = entityManager.find(Order.class, persisted.getId());
        assertThat(reloaded.getOwnerUser().getId()).isEqualTo(customer.getId());
        assertThat(reloaded.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(reloaded.getItems()).hasSize(2);
        assertThat(reloaded.getItems()).extracting(OrderItem::getQuantity).containsOnly(1);

        Object rawStatus = entityManager
                .getEntityManager()
                .createNativeQuery("select status from orders where id = :id")
                .setParameter("id", persisted.getId())
                .getSingleResult();
        assertThat(rawStatus).isEqualTo("CONFIRMED");
    }
}
