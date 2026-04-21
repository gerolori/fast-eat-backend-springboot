package com.gerolori.fasteat.web.order;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.gerolori.fasteat.config.TraceIdResolver;
import com.gerolori.fasteat.domain.entity.OrderStatus;
import com.gerolori.fasteat.domain.entity.RoleName;
import com.gerolori.fasteat.security.AuthPrincipal;
import com.gerolori.fasteat.web.error.GlobalApiExceptionHandler;
import com.gerolori.fasteat.web.order.dto.OrderMoneyResponse;
import com.gerolori.fasteat.web.order.dto.OrderResponse;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class OrderControllerTest {

    private OrderService orderService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        orderService = Mockito.mock(OrderService.class);
        ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new OrderController(orderService))
                .setValidator(validator)
                .setControllerAdvice(new GlobalApiExceptionHandler(new TraceIdResolver()))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void postOrdersReturnsCreatedForNewOrder() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        when(orderService.createOrder(Mockito.eq(userId), Mockito.any(), Mockito.eq("idem-1")))
                .thenReturn(new OrderService.CreatedOrderResult(order(orderId, userId), true));

        mockMvc.perform(post("/orders")
                        .principal(authentication(userId, Set.of(RoleName.CUSTOMER)))
                        .header("Idempotency-Key", "idem-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "items": [{"menuId": "11111111-1111-1111-1111-111111111111", "quantity": 2}],
                                  "deliveryAddress": "Via Torino 21, Milano",
                                  "note": "No onions"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void postOrdersReturnsOkWhenRequestIsIdempotentReplay() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        when(orderService.createOrder(Mockito.eq(userId), Mockito.any(), Mockito.eq("idem-1")))
                .thenReturn(new OrderService.CreatedOrderResult(order(orderId, userId), false));

        mockMvc.perform(post("/orders")
                        .principal(authentication(userId, Set.of(RoleName.CUSTOMER)))
                        .header("Idempotency-Key", "idem-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "items": [{"menuId": "11111111-1111-1111-1111-111111111111", "quantity": 1}],
                                  "deliveryAddress": "Via Torino 21, Milano"
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void postOrdersReturnsConflictWhenRestaurantIsHidden() throws Exception {
        UUID userId = UUID.randomUUID();
        when(orderService.createOrder(Mockito.eq(userId), Mockito.any(), Mockito.isNull()))
                .thenThrow(new OrderApiException(
                        "ORDER_MENU_UNAVAILABLE",
                        org.springframework.http.HttpStatus.CONFLICT,
                        "One or more menu items are unavailable or belong to hidden restaurants"
                ));

        mockMvc.perform(post("/orders")
                        .principal(authentication(userId, Set.of(RoleName.CUSTOMER)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "items": [{"menuId": "11111111-1111-1111-1111-111111111111", "quantity": 1}],
                                  "deliveryAddress": "Via Torino 21, Milano"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("ORDER_MENU_UNAVAILABLE"));
    }

    @Test
    void getOrderReturnsOwnershipDeniedEnvelope() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        when(orderService.getOrder(userId, Set.of(RoleName.CUSTOMER), orderId))
                .thenThrow(new OrderApiException("AUTHZ_OWNERSHIP_DENIED", org.springframework.http.HttpStatus.FORBIDDEN, "You are not allowed to access this order"));

        mockMvc.perform(get("/orders/{orderId}", orderId)
                        .principal(authentication(userId, Set.of(RoleName.CUSTOMER))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("AUTHZ_OWNERSHIP_DENIED"));
    }

    @Test
    void patchOrderReturnsStableTransitionError() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        when(orderService.transitionOrder(userId, Set.of(RoleName.CUSTOMER), orderId, OrderStatus.CONFIRMED))
                .thenThrow(new OrderApiException("ORDER_INVALID_TRANSITION", org.springframework.http.HttpStatus.CONFLICT, "Customers may only cancel orders"));

        mockMvc.perform(patch("/orders/{orderId}", orderId)
                        .principal(authentication(userId, Set.of(RoleName.CUSTOMER)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "CONFIRMED"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("ORDER_INVALID_TRANSITION"));
    }

    private UsernamePasswordAuthenticationToken authentication(UUID userId, Set<RoleName> roles) {
        AuthPrincipal principal = new AuthPrincipal(userId, roles);
        return new UsernamePasswordAuthenticationToken(principal, null, principal.authorities());
    }

    private OrderResponse order(UUID orderId, UUID userId) {
        return new OrderResponse(
                orderId,
                OrderStatus.PENDING,
                Instant.parse("2026-04-20T12:30:00Z"),
                userId,
                List.of(),
                new OrderMoneyResponse("10.00", "USD"),
                "Via Torino 21, Milano",
                null,
                Instant.parse("2026-04-20T12:30:00Z")
        );
    }
}
