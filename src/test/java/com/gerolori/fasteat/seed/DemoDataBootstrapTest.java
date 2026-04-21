package com.gerolori.fasteat.seed;

import static org.assertj.core.api.Assertions.assertThat;

import com.gerolori.fasteat.domain.entity.OrderStatus;
import com.gerolori.fasteat.domain.entity.RoleName;
import com.gerolori.fasteat.domain.repository.OrderRepository;
import com.gerolori.fasteat.domain.repository.RoleRepository;
import com.gerolori.fasteat.domain.repository.UserRepository;
import java.util.EnumSet;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class DemoDataBootstrapTest {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void bootstrapsDeterministicSeedDataForLocalAndTestProfiles() {
        assertThat(roleRepository.findByName(RoleName.ADMIN)).isPresent();
        assertThat(roleRepository.findByName(RoleName.CUSTOMER)).isPresent();

        assertThat(userRepository.findByEmailIgnoreCase("admin@fasteat.local")).isPresent();
        assertThat(userRepository.findByEmailIgnoreCase("owner@fasteat.local")).isPresent();
        assertThat(userRepository.findByEmailIgnoreCase("customer@fasteat.local")).isPresent();

        var statuses = orderRepository.findAll().stream()
                .map(order -> order.getStatus())
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(OrderStatus.class)));

        assertThat(statuses).containsAll(EnumSet.allOf(OrderStatus.class));
    }
}
