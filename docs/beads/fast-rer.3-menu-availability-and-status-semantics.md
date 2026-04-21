# fast-rer.3 — Menu Availability and Status Semantics (A2.3)

This bead defines contract semantics for menu availability across `GET /menus` and `GET /menus/{menuId}`.

It extends:

- `docs/beads/fast-rer.1-menu-listing-api-surface.md`
- `docs/beads/fast-rer.2-menu-detail-and-ingredient-api-contracts.md`

## Canonical availability fields

- `isAvailable` is the canonical boolean in both list and detail payloads.
- `menuId`, `summary`, and `price` object shape stay unchanged:
  - `price.amount` is a string decimal
  - `price.currency` is a currency code string

## Query semantics for `available`

`GET /menus?available=<bool>` behavior:

- `available=true` -> only `isAvailable=true` cards.
- `available=false` -> only `isAvailable=false` cards.
- omitted -> both states are eligible.

Filtering is composed with other filters using AND semantics.

## Detail endpoint semantics for unavailable menus

For `GET /menus/{menuId}`:

- Existing but unavailable menu returns `200` with `isAvailable=false`.
- Not-found menu returns `404` with stable not-found error category.
- Unavailability is not modeled as not-found.

## Order-flow alignment

- `isAvailable=false` means the menu must not be accepted for new order placement in normal flow.
- Existing historical orders that already reference the menu remain readable from order APIs.
- Availability is evaluated at order creation time and early lifecycle checks, consistent with `fast-2p4.6` direction.

## Example payload fragments

Available card:

```json
{
  "menuId": "8f8f2d1f-63f6-41be-a698-bdcf95a2e547",
  "summary": "Burger + fries + drink",
  "price": { "amount": "12.50", "currency": "USD" },
  "isAvailable": true
}
```

Unavailable detail:

```json
{
  "menuId": "8f8f2d1f-63f6-41be-a698-bdcf95a2e547",
  "summary": "Burger + fries + drink",
  "price": { "amount": "12.50", "currency": "USD" },
  "isAvailable": false,
  "ingredients": []
}
```

## Non-goals

- No admin write API for status toggling.
- No scheduling/time-window model for future availability rules.
- No runtime implementation details.
