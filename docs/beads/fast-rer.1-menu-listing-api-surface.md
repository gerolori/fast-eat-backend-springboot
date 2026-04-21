# fast-rer.1 — Menu Listing API Surface (A2.1)

This bead defines the **public-read menu listing API contract** for MVP.

It aligns with:

- `docs/beads/fast-2lk.2-authorization-model.md` (public menu-read baseline)
- `docs/beads/fast-2lk.3-jwt-claim-and-token-lifecycle-contract.md` (JWT/principal baseline for protected routes)

This is a contract document only. It does **not** implement controllers/services/repositories.

## Scope and intent

- Define the listing surface for menu discovery.
- Standardize collection/detail path naming on **`/menus`**.
- Define query contract for filter/sort/pagination at API level.
- Define the listing-card response shape (not full detail payload).

## Canonical endpoint naming for A2

Collection/detail endpoints are standardized as:

- `GET /menus` (listing/search)
- `GET /menus/{menuId}` (detail; payload contract deferred to A2.2)

`/menu` and `/menu/{itemId}` should be treated as older naming references in prior docs; A2 contracts use `/menus` paths.

## `GET /menus` contract

### Access behavior

- **Public-read** endpoint in MVP (no bearer token required).
- Behavior remains compatible with S1.2 low-friction menu discovery direction.
- If a caller sends `Authorization: Bearer ...`, listing semantics are unchanged in A2.1 (no role/persona-specific menu shaping is defined here).

### Query parameters (list/filter/sort/pagination)

All parameters are optional unless noted.

#### Text/search

- `q` (string): free-text search term applied to menu title/name and summary fields.

#### Filter parameters

- `category` (string or repeated string): filter by menu category key(s).
- `available` (boolean): availability filter.
  - `true` = only currently available menus.
  - `false` = only currently unavailable menus.
  - omitted = include both.
- `minPrice` (number): inclusive lower price bound.
- `maxPrice` (number): inclusive upper price bound.
- `lat` (number) and `lng` (number): caller location coordinates used for distance-aware filtering/sorting when present.
- `radiusKm` (number): maximum distance from (`lat`,`lng`) to include, in kilometers. Ignored when coordinates are absent.

#### Sort parameters

- `sortBy` (string): field to sort by. Allowed baseline values:
  - `relevance` (default when `q` is provided)
  - `distance` (when location inputs are present)
  - `price`
  - `rating`
  - `name`
- `sortDir` (string): `asc` or `desc`.
  - Default direction is field-dependent and implementation-defined for now.

#### Pagination parameters

- `page` (integer, zero-based): page index.
- `size` (integer): page size.

Baseline defaults (if omitted):

- `page=0`
- `size=20`

## Listing response contract (card shape only)

`GET /menus` returns a paginated collection of listing cards.

### Response shape

```json
{
  "items": [
    {
      "menuId": "string",
      "name": "string",
      "summary": "string",
      "category": "string",
      "price": {
        "amount": "12.50",
        "currency": "string"
      },
      "imageUrl": "string",
      "isAvailable": true,
      "rating": 0,
      "ratingCount": 0,
      "distanceKm": 0
    }
  ],
  "page": 0,
  "size": 20,
  "totalItems": 0,
  "totalPages": 0,
  "hasNext": false,
  "hasPrevious": false
}
```

### Field notes

- `menuId`: stable menu identifier used by `GET /menus/{menuId}`.
- `summary`: listing teaser text; full menu detail/ingredients are out of scope for A2.1.
- `price`: shared monetary object shape with `amount` (string decimal) and `currency`.
- `imageUrl`: optional image pointer for card rendering.
- `distanceKm`: present when location context is available; may be omitted/null otherwise.
- `rating` and `ratingCount`: summary-only listing fields; rating source mechanics are out of scope here.

## Availability and filter rules (contract-level)

- Availability filtering is controlled by `available` query parameter and represented by `isAvailable` per card.
- `available=true` must exclude unavailable menus from results.
- `available=false` must exclude available menus from results.
- When `available` is omitted, both states may appear.
- When location filters are supplied (`lat`,`lng`,`radiusKm`), only menus within the radius are eligible.
- Filters compose with AND semantics (e.g., category + price + availability + location).

## Deferred details / non-goals

- Full menu detail payload shape (`GET /menus/{menuId}`) is deferred to A2.2.
- Ingredient representation is deferred to A2.2.
- Validation edge-case specifics (parameter bounds, normalization, coercion) are intentionally light here and should be finalized with implementation/error-contract tasks.
- Detailed error body schema/messages remain aligned with S1.4 security/error contract direction and later API-error beads.
