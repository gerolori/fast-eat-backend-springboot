# fast-rer.2 — Menu Detail and Ingredient API Contracts (A2.2)

## Scope and intent

- Define the **menu detail read contract** for the A2 API stream.
- Specify nested ingredient representation for client consumption.
- Keep this document at API-contract level only (no persistence, schema, or implementation wiring details).

This contract aligns with:

- menu API sequencing from A2.1 (listing first, detail second)
- domain baseline in `docs/beads/fast-2p4.1-domain-entity-inventory.md` (Menu as catalog root, Ingredient nested under Menu usage)
- mapping boundary rules in `docs/beads/fast-2p4.7-dto-to-entity-mapping-boundaries.md` (DTO contract decoupled from entities)
- security baseline in `docs/beads/fast-2lk.2-authorization-model.md` (menu reads are public in MVP)

## Resource namespace standardization

Use plural resource naming for menu reads:

- listing: `GET /menus`
- detail: `GET /menus/{menuId}`

`/menus/{menuId}` is the canonical menu detail endpoint for A2.

## Endpoint contract

### `GET /menus/{menuId}`

Returns full detail for one menu item, including nested ingredient summaries required by detail screens and order-building UX.

### Path parameters

- `menuId` (required): stable menu identifier as an opaque API ID string.

### Access model

- MVP baseline: publicly readable (no authentication required by default).
- If deployments later protect this route, they should follow S1.4 auth/authz error semantics.

### Success response

- Status: `200 OK`
- Body: menu detail payload

```json
{
  "menuId": "9e4b9f40-6d5a-4be6-bf40-6dfb03c88e20",
  "name": "Chicken Burger Combo",
  "summary": "Crispy chicken burger with fries and drink",
  "description": "Served with seasoned fries and a soft drink. Includes house sauce.",
  "price": {
    "amount": "12.50",
    "currency": "USD"
  },
  "isAvailable": true,
  "imageUrl": "https://cdn.fast-eat.dev/menus/chicken-burger-combo.png",
  "ingredients": [
    {
      "ingredientId": "c7d1dd37-78ce-49c9-b725-767f2468dc9a",
      "name": "Chicken Fillet",
      "quantity": "1",
      "unit": "piece",
      "isOptional": false
    },
    {
      "ingredientId": "a22be57d-4432-4f84-8fc9-f0beac7d9a5d",
      "name": "House Sauce",
      "quantity": "1",
      "unit": "serving",
      "isOptional": true
    }
  ],
  "updatedAt": "2026-04-20T08:30:00Z"
}
```

## Menu detail payload shape

Required top-level fields for detail response:

- `menuId`: stable API identifier
- `name`: display name
- `summary`: short preview text compatible with list surfaces
- `description`: expanded long-form detail text
- `price`: object with:
  - `amount` (string decimal)
  - `currency` (ISO-style currency code)
- `isAvailable`: current availability boolean
- `imageUrl`: primary image URL for detail rendering
- `ingredients`: array of ingredient objects (see below)
- `updatedAt`: last known menu contract update timestamp (ISO-8601)

## Nested ingredient representation

Each `ingredients[]` item must provide a lightweight, order-context view (not raw inventory internals):

- `ingredientId`: stable API identifier
- `name`: display name
- `quantity`: human-facing quantity value
- `unit`: quantity unit label (for example `piece`, `g`, `ml`, `serving`)
- `isOptional`: whether ingredient is optional/customizable in client UI

Contract constraints:

- ingredient objects are subordinate to the menu detail contract and are not introduced here as standalone top-level resources.
- no persistence-driven fields (table/column names, foreign keys, ORM metadata) are exposed.

## Detail-only vs list-only field boundaries

To stay aligned with A2.1 list performance/clarity goals:

### Shared across list and detail

Field names are contract-stable across A2.1 listing and A2.2 detail responses; use `menuId` and `summary` exactly (no aliasing to `id`, `itemId`, or `shortDescription`).

- `menuId`
- `name`
- `summary`
- `price`
- `isAvailable`
- `imageUrl`

### Detail-only fields (must not be required in list payload)

- `description`
- `ingredients[]`
- `updatedAt`

### List-only concerns (must remain out of detail-specific modeling)

- list transport metadata such as pagination/sort/filter envelope fields
- collection-level counters/links used only for list traversal

## Non-goals for this bead

- no controller/service/repository implementation
- no database schema or entity annotation decisions
- no final decision on ingredient customization command semantics (add/remove/replace behavior), which belongs to later order-focused contracts
