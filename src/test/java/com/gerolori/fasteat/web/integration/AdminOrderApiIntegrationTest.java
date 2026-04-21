package com.gerolori.fasteat.web.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gerolori.fasteat.domain.entity.Role;
import com.gerolori.fasteat.domain.entity.RoleName;
import com.gerolori.fasteat.domain.entity.User;
import com.gerolori.fasteat.domain.repository.RoleRepository;
import com.gerolori.fasteat.domain.repository.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "fasteat.seed.demo.enabled=false",
        "fasteat.security.admin-bootstrap.enabled=false"
})
class AdminOrderApiIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        clearDatabase();
    }

    @Test
    void adminCanManageRestaurantMenuMediaAndOrderLifecycle() throws Exception {
        TestIdentity admin = registerIdentity("admin", "password123");
        String adminToken = promoteToAdminAndLogin(admin.userId(), admin.email(), "password123");

        TestIdentity customer = registerIdentity("customer", "password123");
        setPaymentProfile(customer.userId());

        UUID restaurantId = createRestaurant(adminToken, customer.userId());
        UUID menuId = createMenu(adminToken, restaurantId);

        mockMvc.perform(put("/admin/media/restaurants/{restaurantId}/image", restaurantId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "imageUrl": "https://cdn.example.com/admin-restaurant.png"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resourceId").value(restaurantId.toString()));

        mockMvc.perform(put("/admin/media/menus/{menuId}/image", menuId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "imageUrl": "https://cdn.example.com/admin-menu.png"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resourceId").value(menuId.toString()));

        String idemKey = "idem-" + UUID.randomUUID();
        MvcResult createdOrder = mockMvc.perform(post("/orders")
                        .header("Authorization", "Bearer " + customer.accessToken())
                        .header("Idempotency-Key", idemKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderPayload(menuId, 2, "Via Torino 21, Milano")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();

        UUID orderId = UUID.fromString(readBody(createdOrder).path("orderId").asText());

        MvcResult adminListResult = mockMvc.perform(get("/admin/orders")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("status", "PENDING")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode items = readBody(adminListResult).path("items");
        assertThat(items.isArray()).isTrue();
        assertThat(items).anySatisfy(node -> assertThat(node.path("orderId").asText()).isEqualTo(orderId.toString()));

        mockMvc.perform(get("/admin/orders/{orderId}", orderId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerUserId").value(customer.userId().toString()));

        mockMvc.perform(patch("/admin/orders/{orderId}/status", orderId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "CONFIRMED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void orderCreateReadErrorAndIdempotencyBehaviorsAreStable() throws Exception {
        TestIdentity admin = registerIdentity("admin", "password123");
        String adminToken = promoteToAdminAndLogin(admin.userId(), admin.email(), "password123");

        TestIdentity customer = registerIdentity("customer", "password123");
        setPaymentProfile(customer.userId());

        UUID restaurantId = createRestaurant(adminToken, customer.userId());
        UUID menuId = createMenu(adminToken, restaurantId);

        String idemKey = "idem-" + UUID.randomUUID();
        MvcResult createResult = mockMvc.perform(post("/orders")
                        .header("Authorization", "Bearer " + customer.accessToken())
                        .header("Idempotency-Key", idemKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderPayload(menuId, 1, "Via Torino 21, Milano")))
                .andExpect(status().isCreated())
                .andReturn();

        UUID orderId = UUID.fromString(readBody(createResult).path("orderId").asText());

        mockMvc.perform(post("/orders")
                        .header("Authorization", "Bearer " + customer.accessToken())
                        .header("Idempotency-Key", idemKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderPayload(menuId, 1, "Via Torino 21, Milano")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()));

        mockMvc.perform(post("/orders")
                        .header("Authorization", "Bearer " + customer.accessToken())
                        .header("Idempotency-Key", idemKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderPayload(menuId, 3, "Via Torino 21, Milano")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("ORDER_DUPLICATE_REQUEST"));

        TestIdentity otherCustomer = registerIdentity("other", "password123");

        mockMvc.perform(get("/orders/{orderId}", orderId)
                        .header("Authorization", "Bearer " + otherCustomer.accessToken()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("AUTHZ_OWNERSHIP_DENIED"));

        mockMvc.perform(get("/orders/{orderId}", UUID.randomUUID())
                        .header("Authorization", "Bearer " + customer.accessToken()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("ORDER_NOT_FOUND"));

        mockMvc.perform(patch("/admin/restaurants/{restaurantId}/visibility", restaurantId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "visible": false
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/orders")
                        .header("Authorization", "Bearer " + customer.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderPayload(menuId, 1, "Via Torino 21, Milano")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("ORDER_MENU_UNAVAILABLE"));
    }

    @Test
    void adminEndpointsRequireAdminRole() throws Exception {
        TestIdentity customer = registerIdentity("customer", "password123");

        mockMvc.perform(get("/admin/orders")
                        .header("Authorization", "Bearer " + customer.accessToken()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("AUTHZ_INSUFFICIENT_ROLE"));

        mockMvc.perform(post("/admin/restaurants")
                        .header("Authorization", "Bearer " + customer.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ownerUserId": "%s",
                                  "name": "Role Guard Test"
                                }
                                """.formatted(customer.userId())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("AUTHZ_INSUFFICIENT_ROLE"));

        mockMvc.perform(get("/admin/orders"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", not(blankOrNullString())));
    }

    private TestIdentity registerIdentity(String prefix, String password) throws Exception {
        String email = prefix + "-" + UUID.randomUUID() + "@fasteat.test";
        MvcResult registerResult = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode payload = readBody(registerResult);
        UUID userId = UUID.fromString(payload.path("principal").path("userId").asText());
        String accessToken = payload.path("accessToken").asText();
        return new TestIdentity(userId, email, accessToken);
    }

    private String promoteToAdminAndLogin(UUID userId, String email, String password) throws Exception {
        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.ADMIN)));

        User user = userRepository.findById(userId).orElseThrow();
        user.getRoles().add(adminRole);
        userRepository.save(user);

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();

        return readBody(loginResult).path("accessToken").asText();
    }

    private void setPaymentProfile(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setPaymentProvider("stripe");
        user.setPaymentMethodReference("pm_test_visa");
        user.setPaymentLast4("4242");
        userRepository.save(user);
    }

    private UUID createRestaurant(String adminToken, UUID ownerUserId) throws Exception {
        MvcResult result = mockMvc.perform(post("/admin/restaurants")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ownerUserId": "%s",
                                  "name": "Integration Kitchen",
                                  "summary": "Admin-managed kitchen",
                                  "description": "Kitchen used for admin integration tests",
                                  "category": "CASUAL_DINING",
                                  "city": "Milano",
                                  "state": "MI",
                                  "country": "Italy"
                                }
                                """.formatted(ownerUserId)))
                .andExpect(status().isCreated())
                .andReturn();

        return UUID.fromString(readBody(result).path("restaurantId").asText());
    }

    private UUID createMenu(String adminToken, UUID restaurantId) throws Exception {
        MvcResult result = mockMvc.perform(post("/admin/restaurants/{restaurantId}/menus", restaurantId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Chicken Bowl",
                                  "summary": "Balanced meal",
                                  "description": "Protein and greens",
                                  "category": "MAIN",
                                  "price": 9.99,
                                  "available": true,
                                  "imageUrl": "https://cdn.example.com/chicken-bowl.png"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        return UUID.fromString(readBody(result).path("menuId").asText());
    }

    private String orderPayload(UUID menuId, int quantity, String deliveryAddress) {
        return """
                {
                  "items": [
                    {
                      "menuId": "%s",
                      "quantity": %d
                    }
                  ],
                  "deliveryAddress": "%s",
                  "note": "No onions"
                }
                """.formatted(menuId, quantity, deliveryAddress);
    }

    private JsonNode readBody(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsByteArray());
    }

    private void clearDatabase() {
        jdbcTemplate.execute("DELETE FROM order_items");
        jdbcTemplate.execute("DELETE FROM orders");
        jdbcTemplate.execute("DELETE FROM menu_ingredients");
        jdbcTemplate.execute("DELETE FROM menus");
        jdbcTemplate.execute("DELETE FROM restaurants");
        jdbcTemplate.execute("DELETE FROM refresh_tokens");
        jdbcTemplate.execute("DELETE FROM user_roles");
        jdbcTemplate.execute("DELETE FROM users");
        jdbcTemplate.execute("DELETE FROM roles");
    }

    private record TestIdentity(UUID userId, String email, String accessToken) {
    }
}
