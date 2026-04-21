package com.gerolori.fasteat.web.admin.order;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gerolori.fasteat.config.TraceIdResolver;
import com.gerolori.fasteat.domain.entity.OrderStatus;
import com.gerolori.fasteat.web.error.GlobalApiExceptionHandler;
import com.gerolori.fasteat.web.order.OrderService;
import com.gerolori.fasteat.web.order.dto.OrderMoneyResponse;
import com.gerolori.fasteat.web.order.dto.OrderResponse;
import com.gerolori.fasteat.web.shared.PagedResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class AdminOrderControllerTest {

    @Mock
    private AdminOrderAccessGuard accessGuard;

    @Mock
    private OrderService orderService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new AdminOrderController(accessGuard, orderService))
                .setControllerAdvice(new GlobalApiExceptionHandler(new TraceIdResolver()))
                .setValidator(validator)
                .build();
    }

    @Test
    void listsOrdersForAdmin() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        doNothing().when(accessGuard).requireAdmin(any());
        when(orderService.getAdminOrders(OrderStatus.PENDING, 0, 1))
                .thenReturn(new PagedResponse<>(List.of(order(orderId, ownerId, OrderStatus.PENDING)), 0, 1, 1, 1, false, false));

        mockMvc.perform(get("/admin/orders")
                        .param("status", "PENDING")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.items[0].status").value("PENDING"));
    }

    @Test
    void getsSingleOrderForAdmin() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        doNothing().when(accessGuard).requireAdmin(any());
        when(orderService.getAdminOrder(orderId)).thenReturn(order(orderId, ownerId, OrderStatus.CONFIRMED));

        mockMvc.perform(get("/admin/orders/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void updatesOrderStatusForAdmin() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        doNothing().when(accessGuard).requireAdmin(any());
        when(orderService.transitionOrderAsAdmin(eq(orderId), eq(OrderStatus.PREPARING)))
                .thenReturn(order(orderId, ownerId, OrderStatus.PREPARING));

        mockMvc.perform(patch("/admin/orders/{orderId}/status", orderId)
                        .contentType("application/json")
                        .content("{\"status\":\"PREPARING\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PREPARING"));
    }

    @Test
    void mapsForbiddenEnvelopeWhenNotAdmin() throws Exception {
        UUID orderId = UUID.randomUUID();
        doThrow(new AccessDeniedException("Admin role required")).when(accessGuard).requireAdmin(any());

        mockMvc.perform(get("/admin/orders/{orderId}", orderId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("AUTHZ_INSUFFICIENT_ROLE"));
    }

    private OrderResponse order(UUID orderId, UUID ownerId, OrderStatus status) {
        return new OrderResponse(
                orderId,
                status,
                Instant.parse("2026-04-20T12:50:00Z"),
                Instant.parse("2026-04-20T13:15:00Z"),
                null,
                ownerId,
                List.of(),
                new OrderMoneyResponse("12.50", "USD"),
                "Via Torino 21, Milano",
                null,
                Instant.parse("2026-04-20T12:30:00Z")
        );
    }
}
