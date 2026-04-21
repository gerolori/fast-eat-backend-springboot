# fast-j65.5 — Freeze Media, Location, and Payment-Profile Strategy (I0.5)

This bead freezes cross-cutting MVP strategy for media delivery, location-aware browsing, and payment-profile scope.

Cross-reference:

- `docs/beads/fast-rer.1-menu-listing-api-surface.md`
- `docs/beads/fast-rer.2-menu-detail-and-ingredient-api-contracts.md`
- `docs/beads/fast-kql.1-user-profile-api-surface-and-endpoint-set.md`

## Media strategy (MVP)

- API contracts expose media as URL/reference fields (for example `imageUrl`).
- Binary media upload/streaming endpoints are out of scope for MVP contract freeze.
- Clients must treat media URLs as opaque references and not derive storage internals from path shape.

## Location strategy (MVP)

- Location-aware discovery is query-driven on canonical browse surfaces (`/menus`), using `lat`, `lng`, and optional `radiusKm`.
- Distance-aware sorting/filtering remains opt-in via query parameters.
- When location context is absent, browse behavior remains valid and non-location-filtered.
- No hard dependency on live geolocation tracking is introduced into core auth/order flows.

## Payment-profile strategy (MVP)

- MVP supports payment-profile metadata direction only (profile-level references), not direct card/PAN processing contracts.
- Raw sensitive card data (PAN/CVV/full-track equivalents) is out of scope for API DTO persistence/echo semantics.
- Payment profile fields, where present, must be tokenized/provider-reference oriented and safe for non-PCI transport contracts.
- Order placement/payment orchestration may reference stored payment-profile identity, but gateway-specific charge workflows are deferred.

## Superseded planning assumptions

This bead supersedes ambiguous prior planning by explicitly freezing:

- media as URL/reference contract (not binary-transfer contract),
- location as optional query-driven browse behavior,
- payment profile as tokenized metadata scope rather than full payment-processing payload scope.

## Non-goals

- No CDN/storage-provider selection.
- No geospatial indexing/schema design.
- No PSP/gateway integration protocol details.
