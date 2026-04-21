# fast-1j3.6 — Order Workflow Contract Examples (O1.6)

This bead provides end-to-end examples tying O1 contracts together for client integrators.

## Workflow A: create -> progress -> complete

1. `POST /orders` -> `201` with `status=PENDING`
2. `GET /orders/{orderId}` -> `status=CONFIRMED`
3. `GET /orders/{orderId}` -> `status=PREPARING`
4. `GET /orders/{orderId}` -> `status=READY_FOR_PICKUP`
5. `GET /orders/{orderId}` -> `status=COMPLETED`

Status values remain exactly those in `fast-2p4.6`.

## Workflow B: create -> cancel by owner

1. `POST /orders` -> `status=PENDING`
2. Owner cancellation request (implementation path deferred) while cancellable state
3. `GET /orders/{orderId}` -> `status=CANCELLED`

After cancellation, further progression attempts must fail with `ORDER_INVALID_TRANSITION`.

## Workflow C: ownership denial

1. Caller A creates order.
2. Caller B requests `GET /orders/{orderId}`.
3. Response -> `403 AUTHZ_OWNERSHIP_DENIED` with S1.4 envelope.

## Example timeline payload shape

```json
{
  "orderId": "77f31f22-4d1f-4f8f-9158-2cce1eb8ca7d",
  "status": "READY_FOR_PICKUP",
  "statusHistory": [
    { "status": "PENDING", "at": "2026-04-20T12:30:00Z" },
    { "status": "CONFIRMED", "at": "2026-04-20T12:33:00Z" },
    { "status": "PREPARING", "at": "2026-04-20T12:40:00Z" },
    { "status": "READY_FOR_PICKUP", "at": "2026-04-20T12:55:00Z" }
  ]
}
```

`statusHistory` is optional contract direction and may be introduced incrementally.

## Client compatibility notes

- Use enum-safe parsing for status values; unknown future status should be treated as non-terminal by default unless documented otherwise.
- Use `traceId` from error responses for support/debug workflows.
- This bead is examples-only and does not assert existing runtime endpoint implementation.
