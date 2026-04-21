# fast-31f.3 — React Native Order Lifecycle Contract Mapping (X3.3)

This bead maps the **currently implemented order lifecycle contract** to React Native behavior.

Primary backend sources:

- `src/main/java/com/gerolori/fasteat/web/order/OrderController.java`
- `src/main/java/com/gerolori/fasteat/web/order/OrderService.java`
- `src/main/java/com/gerolori/fasteat/web/order/dto/*.java`

## Implemented endpoints

| Endpoint | Auth | Behavior |
|---|---|---|
| `POST /orders` | Required | Creates new order, or returns existing order for idempotent replay. |
| `GET /orders/{orderId}` | Required | Owner can read own order; admin can read any order. |
| `PATCH /orders/{orderId}` | Required | Transition status with role and transition guards. |

## Create order contract details

Request body:

- `items[]` (required, non-empty)
  - `menuId` (UUID, required)
  - `quantity` (int, >= 1)
- `deliveryAddress` (required, non-blank)
- `note` (optional)

Header:

- `Idempotency-Key` (optional)

Create semantics:

- First successful request with key -> `201 Created`
- Replayed identical request with same key -> `200 OK` + same `OrderResponse`
- Same key with different payload -> `409 ORDER_DUPLICATE_REQUEST`

Precondition for mobile checkout:

- User must have payment profile (`paymentProvider` + `paymentMethodReference`) or backend returns `409 ORDER_PAYMENT_PROFILE_REQUIRED`.

## Status model and transitions implemented

Statuses:

- `PENDING`
- `CONFIRMED`
- `PREPARING`
- `READY_FOR_PICKUP`
- `COMPLETED`
- `CANCELLED`

Allowed transitions:

- `PENDING -> CONFIRMED | CANCELLED`
- `CONFIRMED -> PREPARING | CANCELLED`
- `PREPARING -> READY_FOR_PICKUP | CANCELLED`
- `READY_FOR_PICKUP -> COMPLETED`

Role behavior:

- Admin: may perform any valid transition.
- Non-admin (customer): may only request `CANCELLED` and only when transition is valid.

RN implication: show cancellation action only for customer-owned orders in cancellable states.

## Ownership and errors

- Access/update by non-owner non-admin -> `403 AUTHZ_OWNERSHIP_DENIED`
- Missing order -> `404 ORDER_NOT_FOUND`
- Invalid transition -> `409 ORDER_INVALID_TRANSITION`
- Unavailable/hidden menu during creation -> `409 ORDER_MENU_UNAVAILABLE`
- Validation failures (DTO constraints) -> `400 VALIDATION_ERROR`

All errors follow shared envelope shape (`error`, `message`, `status`, `path`, `timestamp`, `traceId`, optional `details`).
