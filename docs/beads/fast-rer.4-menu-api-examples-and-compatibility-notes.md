# fast-rer.4 — Menu API Examples and Compatibility Notes (A2.4)

This bead provides example contracts and compatibility notes for A2 clients using `/menus` endpoints.

References:

- `docs/beads/fast-rer.1-menu-listing-api-surface.md`
- `docs/beads/fast-rer.2-menu-detail-and-ingredient-api-contracts.md`
- `docs/beads/fast-rer.3-menu-availability-and-status-semantics.md`

## Canonical endpoint examples

### 1) List menus

`GET /menus?q=burger&available=true&page=0&size=2`

```json
{
  "items": [
    {
      "menuId": "8f8f2d1f-63f6-41be-a698-bdcf95a2e547",
      "name": "Chicken Burger Combo",
      "summary": "Burger + fries + drink",
      "price": { "amount": "12.50", "currency": "USD" },
      "isAvailable": true
    }
  ],
  "page": 0,
  "size": 2,
  "totalItems": 1,
  "totalPages": 1,
  "hasNext": false,
  "hasPrevious": false
}
```

### 2) Read menu detail

`GET /menus/8f8f2d1f-63f6-41be-a698-bdcf95a2e547`

```json
{
  "menuId": "8f8f2d1f-63f6-41be-a698-bdcf95a2e547",
  "name": "Chicken Burger Combo",
  "summary": "Burger + fries + drink",
  "description": "Crispy chicken burger with seasoned fries",
  "price": { "amount": "12.50", "currency": "USD" },
  "isAvailable": true,
  "ingredients": [
    { "ingredientId": "i-1", "name": "Chicken Fillet", "quantity": "1", "unit": "piece", "isOptional": false }
  ]
}
```

## Error examples

Menu not found (`404`):

```json
{
  "error": "MENU_NOT_FOUND",
  "message": "Menu not found",
  "status": 404,
  "path": "/menus/unknown-id",
  "timestamp": "2026-04-20T12:00:00Z",
  "traceId": "1a5af1db-34b0-4328-adff-259a5ef1da5e"
}
```

## Compatibility notes

- A2 contract is pluralized: `/menus` and `/menus/{menuId}` are canonical.
- `menuId` remains stable; do not alias to `id` or `itemId` in client models.
- Shared fields across list/detail must keep names and monetary shape identical (`summary`, `price.amount`, `price.currency`).
- If future protection is added, auth/authz errors must follow S1.4 categories and envelope.

## Non-goals

- No backward-compatibility promise for legacy `/menu` routes.
- No implementation details about repositories/controllers.
