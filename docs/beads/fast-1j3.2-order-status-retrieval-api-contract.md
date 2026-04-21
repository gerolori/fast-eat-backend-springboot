# fast-1j3.2 — Order Status Retrieval API Contract (O1.2)

This bead defines status retrieval semantics for an individual order.

## Endpoint

- Canonical: `GET /orders/{orderId}`
- Auth required
- Owner-scoped for non-admin callers: `order.ownerUserId == principal.userId`

## Status field contract

`status` must be one of:

- `PENDING`
- `CONFIRMED`
- `PREPARING`
- `READY_FOR_PICKUP`
- `COMPLETED`
- `CANCELLED`

No alternate aliases are allowed in API responses.

## Response example (`200 OK`)

```json
{
  "orderId": "77f31f22-4d1f-4f8f-9158-2cce1eb8ca7d",
  "status": "PREPARING",
  "statusUpdatedAt": "2026-04-20T12:45:00Z",
  "items": [
    {
      "menuId": "8f8f2d1f-63f6-41be-a698-bdcf95a2e547",
      "summary": "Burger + fries + drink",
      "price": { "amount": "12.50", "currency": "USD" },
      "quantity": 2
    }
  ],
  "total": { "amount": "25.00", "currency": "USD" },
  "createdAt": "2026-04-20T12:30:00Z"
}
```

## Error semantics

- `401` for unauthenticated requests (S1.4 categories)
- `403` for authenticated owner/role denial
- `404` for unknown order ID

Example (`403`):

```json
{
  "error": "AUTHZ_OWNERSHIP_DENIED",
  "message": "You are not allowed to access this order",
  "status": 403,
  "path": "/orders/77f31f22-4d1f-4f8f-9158-2cce1eb8ca7d",
  "timestamp": "2026-04-20T12:50:00Z",
  "traceId": "7f7d2790-b050-43d6-a1d2-396ec699798b"
}
```

`traceId` follows the correlation/logging mapping from `fast-3q7.2`.
