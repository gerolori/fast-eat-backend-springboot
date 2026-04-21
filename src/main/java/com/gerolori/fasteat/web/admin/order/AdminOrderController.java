package com.gerolori.fasteat.web.admin.order;

import com.gerolori.fasteat.domain.entity.OrderStatus;
import com.gerolori.fasteat.web.order.OrderService;
import com.gerolori.fasteat.web.order.dto.OrderResponse;
import com.gerolori.fasteat.web.order.dto.UpdateOrderStatusRequest;
import com.gerolori.fasteat.web.shared.PagedResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final AdminOrderAccessGuard accessGuard;
    private final OrderService orderService;

    public AdminOrderController(AdminOrderAccessGuard accessGuard, OrderService orderService) {
        this.accessGuard = accessGuard;
        this.orderService = orderService;
    }

    @GetMapping
    public PagedResponse<OrderResponse> getOrders(
            Authentication authentication,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size
    ) {
        accessGuard.requireAdmin(authentication);
        return orderService.getAdminOrders(status, page, size);
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOrder(Authentication authentication, @PathVariable UUID orderId) {
        accessGuard.requireAdmin(authentication);
        return orderService.getAdminOrder(orderId);
    }

    @PatchMapping("/{orderId}/status")
    public OrderResponse updateOrderStatus(
            Authentication authentication,
            @PathVariable UUID orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        accessGuard.requireAdmin(authentication);
        return orderService.transitionOrderAsAdmin(orderId, request.status());
    }
}
