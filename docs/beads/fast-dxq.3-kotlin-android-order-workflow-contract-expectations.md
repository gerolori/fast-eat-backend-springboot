# fast-dxq.3 — Kotlin Android Order Workflow Contract Expectations (X2.3)

This bead maps Kotlin Android order placement/tracking expectations to the currently implemented backend order workflow.

References:

- `src/main/java/com/gerolori/fasteat/web/order/OrderController.java`
- `src/main/java/com/gerolori/fasteat/web/order/OrderService.java`
- `src/main/java/com/gerolori/fasteat/web/order/dto/*.java`
- `src/main/java/com/gerolori/fasteat/domain/entity/OrderStatus.java`
- `src/main/java/com/gerolori/fasteat/web/error/GlobalApiExceptionHandler.java`
- `docs/beads/fast-1j3.6-order-workflow-contract-examples.md`

## Endpoint surface (auth required)

- `POST /orders`
- `GET /orders/{orderId}`
- `PATCH /orders/{orderId}`

## Create-order expectations

Request:

- `items[]` with `menuId` + `quantity`
- `deliveryAddress`
- optional `note`
- optional header `Idempotency-Key`

Response:

- `201 Created` on first successful request
- `200 OK` on idempotent replay with same payload hash
- `409 ORDER_DUPLICATE_REQUEST` when same key is reused with a different payload

## Status and transition expectations

Current status enum values:

- `PENDING`
- `CONFIRMED`
- `PREPARING`
- `READY_FOR_PICKUP`
- `COMPLETED`
- `CANCELLED`

Transition constraints currently implemented:

- `PENDING -> CONFIRMED | CANCELLED`
- `CONFIRMED -> PREPARING | CANCELLED`
- `PREPARING -> READY_FOR_PICKUP | CANCELLED`
- `READY_FOR_PICKUP -> COMPLETED`

Role/ownership rules:

- Customers may only cancel (`status=CANCELLED`) and only on their own orders.
- Admin may read/transition any order.
- Non-owner customer reads/updates return `403 AUTHZ_OWNERSHIP_DENIED`.

## Order response model Kotlin should bind

- `orderId`
- `status`
- `statusUpdatedAt`
- `ownerUserId`
- `items[]` with `menuId`, `menuName`, `unitPrice`, `quantity`, `lineTotal`
- `total`
- `deliveryAddress`
- `note`
- `createdAt`

Monetary objects are string-based decimal amounts with explicit currency code (`USD`).

## Error expectations for client UX

Common order error codes to handle explicitly:

- `ORDER_PAYMENT_PROFILE_REQUIRED` (`409`)
- `ORDER_MENU_UNAVAILABLE` (`409`)
- `ORDER_INVALID_ITEM_QUANTITY` (`422`)
- `ORDER_INVALID_DELIVERY_ADDRESS` (`422`)
- `ORDER_INVALID_TRANSITION` (`409`)
- `ORDER_NOT_FOUND` (`404`)
- `AUTHZ_OWNERSHIP_DENIED` (`403`)

All follow shared API error envelope and include `traceId` for support correlation.

## Compatibility notes

- Keep client status parsing unknown-safe for future backend enum expansion.
- Preserve `Idempotency-Key` generation per checkout attempt to avoid accidental duplicate charge/order UX.
- Do not assume server returns timeline history yet; current response exposes latest status fields only.
