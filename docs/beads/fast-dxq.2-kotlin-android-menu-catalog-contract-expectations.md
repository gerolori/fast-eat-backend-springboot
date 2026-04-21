# fast-dxq.2 — Kotlin Android Menu/Catalog Contract Expectations (X2.2)

This bead maps the **implemented** menu/catalog contract used by Kotlin Android browse flows.

References:

- `src/main/java/com/gerolori/fasteat/web/menu/MenuController.java`
- `src/main/java/com/gerolori/fasteat/web/menu/MenuBrowseService.java`
- `src/main/java/com/gerolori/fasteat/web/menu/dto/*.java`
- `src/main/java/com/gerolori/fasteat/security/SecurityRouteMatcher.java`
- `docs/beads/fast-rer.4-menu-api-examples-and-compatibility-notes.md`

## Endpoint surface (public GET)

These routes are currently public for GET:

- `GET /menus`
- `GET /menus/{menuId}`
- `GET /menus/{menuId}/ingredients`

## Query/pagination contract

`GET /menus` supports:

- Text: `q`
- Category filter: repeated `category`
- Availability: `available`
- Price range: `minPrice`, `maxPrice`
- Location/radius: `lat`, `lng`, `radiusKm`
- Sorting: `sortBy`, `sortDir`
- Paging: `page` (default `0`, min `0`), `size` (default `20`, min `1`)

`MenuListResponse`:

- `items`
- `page`
- `size`
- `totalItems`
- `totalPages`
- `hasNext`
- `hasPrevious`

## Kotlin field expectations

List item fields:

- `menuId`, `name`, `summary`, `category`
- `price: { amount, currency }` (currency currently `USD`)
- `imageUrl`
- `isAvailable`
- `status` in `AVAILABLE | SOLD_OUT | INACTIVE`
- `rating`, `ratingCount`
- `distanceKm` (nullable; only populated when location context exists)

Detail fields:

- `menuId`, `name`, `summary`, `description`
- `price`, `isAvailable`, `status`, `imageUrl`
- `ingredients[]` (`ingredientId`, `name`, `summary`, `imageUrl`, `isAvailable`)
- `updatedAt`

## Semantics Kotlin should preserve

- `isAvailable` is derived from `status == AVAILABLE`; keep both fields in app models because backend returns both.
- Unknown future `status` values should not crash enum parsing; apply unknown-safe fallback.
- Hidden restaurants are filtered out in list and treated as not found on detail/ingredients reads.

## Error/edge behavior

- Missing or invalid IDs on detail routes return `404 RESOURCE_NOT_FOUND`.
- Validation failures (for example invalid `page/size`) return `400 VALIDATION_ERROR` envelope with `details`.
- Error envelope remains `error/message/status/path/timestamp/traceId/(optional details)`.

## Compatibility notes

- Canonical collection route remains pluralized (`/menus`).
- `menuId` is stable identity and should not be remapped to ambiguous `id` in shared client DTOs.
- Client-side paging UI should rely on `hasNext/hasPrevious`, not hand-rolled page math only.
