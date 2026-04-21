# fast-31f.2 — React Native Catalog/Discovery Contract Mapping (X3.2)

This bead maps the **currently implemented catalog/discovery contract** for React Native consumers.

Primary backend sources:

- `src/main/java/com/gerolori/fasteat/web/menu/MenuController.java`
- `src/main/java/com/gerolori/fasteat/web/menu/MenuBrowseService.java`
- `src/main/java/com/gerolori/fasteat/web/restaurant/RestaurantBrowseController.java`
- `src/main/java/com/gerolori/fasteat/security/SecurityRouteMatcher.java`

## Public discovery endpoints (no bearer token required)

| Endpoint | Auth | Notes |
|---|---|---|
| `GET /restaurants?page={n}&size={n}` | Public | Only visible restaurants are returned. |
| `GET /restaurants/{restaurantId}` | Public | Hidden/non-visible restaurants resolve as not found. |
| `GET /menus?...filters` | Public | Supports text, category, availability, price, location and sort filters. |
| `GET /menus/{menuId}` | Public | Returns menu detail + ingredients list. |
| `GET /menus/{menuId}/ingredients` | Public | Ingredient-only projection for the same visibility constraints. |

## `GET /menus` query contract implemented now

Supported query params:

- `q`
- `category` (repeatable)
- `available` (boolean)
- `minPrice`, `maxPrice` (decimal)
- `lat`, `lng`, `radiusKm`
- `sortBy`, `sortDir`
- `page` (default `0`, min `0`)
- `size` (default `20`, min `1`)

Default sort behavior when omitted:

1. `q` present -> `relevance` (descending)
2. else if `lat` and `lng` present -> `distance`
3. else -> `name`

RN implications:

- If location context is sent, list items can include `distanceKm` (nullable).
- `category` should be serialized as repeated query keys.

## DTO fields to align in RN models

`MenuListItemResponse`:

- `menuId`, `name`, `summary`, `category`
- `price.amount`, `price.currency`
- `imageUrl`
- `isAvailable`
- `status` (`AVAILABLE` | `SOLD_OUT` | `INACTIVE`)
- `rating`, `ratingCount`
- `distanceKm` (nullable)

`MenuDetailResponse` adds:

- `description`
- `ingredients[]`
- `updatedAt`

Restaurant list/detail include location fields at detail level (`latitude`, `longitude`) and metadata (`city`, `state`, `country`, rating counters).

## Visibility and availability semantics to respect

- Menus from non-visible restaurants are excluded from list and detail lookups.
- Menu status derives from menu + restaurant flags:
  - `INACTIVE`: menu inactive OR restaurant unavailable
  - `SOLD_OUT`: menu active but not available
  - `AVAILABLE`: active and orderable

This lets RN present both quick availability (`isAvailable`) and richer state (`status`) without extra transformations.
