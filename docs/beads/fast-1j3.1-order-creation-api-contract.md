# fast-1j3.1 — Order Creation API Contract (O1.1)

This bead defines the contract-level API for creating orders.

Aligned references:

- `docs/beads/fast-2lk.2-authorization-model.md`
- `docs/beads/fast-2lk.4-security-error-handling-contract.md`
- `docs/beads/fast-2p4.6-order-state-model-and-transition-invariants.md`
- `docs/beads/fast-rer.2-menu-detail-and-ingredient-api-contracts.md`

## Endpoint

- `POST /orders`
- Auth required (`CUSTOMER`/`ADMIN` authenticated principal)
- Owner identity is derived from principal context, never from request `userId`

## Request contract

```json
{
  "items": [
    { "menuId": "8f8f2d1f-63f6-41be-a698-bdcf95a2e547", "quantity": 2 },
    { "menuId": "2e0d4b81-1cf6-4c2c-81c8-2e1ed4f46921", "quantity": 1 }
  ],
  "note": "No onions",
  "deliveryAddress": "Via Torino 21, Milano"
}
```

Rules:

- `items` must contain at least one line.
- Every line requires `menuId` and positive integer `quantity`.
- Unknown/restricted fields are rejected.
- Menu existence/availability are validated at creation time.

## Success response (`201 Created`)

```json
{
  "orderId": "77f31f22-4d1f-4f8f-9158-2cce1eb8ca7d",
  "status": "PENDING",
  "ownerUserId": "principal-derived",
  "items": [
    {
      "menuId": "8f8f2d1f-63f6-41be-a698-bdcf95a2e547",
      "summary": "Burger + fries + drink",
      "price": { "amount": "12.50", "currency": "USD" },
      "quantity": 2,
      "lineTotal": { "amount": "25.00", "currency": "USD" }
    }
  ],
  "total": { "amount": "35.00", "currency": "USD" },
  "createdAt": "2026-04-20T12:30:00Z"
}
```

## Failure categories

- `401`: auth failures (`AUTH_MISSING_TOKEN`, `AUTH_INVALID_TOKEN`, ...)
- `403`: ownership/role policy denials
- `404`: referenced resource not found for one or more lines (`ORDER_NOT_FOUND`)
- `409` or `422`: availability/business validation failures (`ORDER_MENU_UNAVAILABLE`, `ORDER_INVALID_ITEM_QUANTITY`)

This bead is contract-only and does not imply that `POST /orders` is currently implemented.
