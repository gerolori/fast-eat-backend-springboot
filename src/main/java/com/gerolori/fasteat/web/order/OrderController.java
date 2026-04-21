package com.gerolori.fasteat.web.order;

import com.gerolori.fasteat.security.AuthPrincipal;
import com.gerolori.fasteat.security.JwtAuthenticationException;
import com.gerolori.fasteat.domain.entity.OrderStatus;
import com.gerolori.fasteat.web.order.dto.CreateOrderRequest;
import com.gerolori.fasteat.web.order.dto.OrderResponse;
import com.gerolori.fasteat.web.order.dto.UpdateOrderStatusRequest;
import com.gerolori.fasteat.web.shared.PagedResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            Authentication authentication,
            @Valid @RequestBody CreateOrderRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        AuthPrincipal principal = extractPrincipal(authentication);
        OrderService.CreatedOrderResult result = orderService.createOrder(principal.userId(), request, idempotencyKey);
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(result.response());
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOrder(Authentication authentication, @PathVariable UUID orderId) {
        AuthPrincipal principal = extractPrincipal(authentication);
        return orderService.getOrder(principal.userId(), principal.roles(), orderId);
    }

    @GetMapping("/me")
    public PagedResponse<OrderResponse> getMyOrders(
            Authentication authentication,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size
    ) {
        AuthPrincipal principal = extractPrincipal(authentication);
        return orderService.getMyOrders(principal.userId(), status, page, size);
    }

    @GetMapping("/me/completed")
    public PagedResponse<OrderResponse> getMyCompletedOrders(
            Authentication authentication,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size
    ) {
        AuthPrincipal principal = extractPrincipal(authentication);
        return orderService.getMyCompletedOrders(principal.userId(), page, size);
    }

    @PatchMapping("/{orderId}")
    public OrderResponse transitionOrder(
            Authentication authentication,
            @PathVariable UUID orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        AuthPrincipal principal = extractPrincipal(authentication);
        return orderService.transitionOrder(principal.userId(), principal.roles(), orderId, request.status());
    }

    private AuthPrincipal extractPrincipal(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof AuthPrincipal principal) {
            return principal;
        }

        throw new JwtAuthenticationException("AUTH_INVALID_TOKEN", "Authentication principal is invalid");
    }
}
