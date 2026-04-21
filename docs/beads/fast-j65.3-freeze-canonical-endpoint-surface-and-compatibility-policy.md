# fast-j65.3 — Freeze Canonical Endpoint Surface and Compatibility Policy (I0.3)

This bead freezes the canonical JWT-era endpoint families and compatibility policy for MVP.

Cross-reference API/security beads:

- `docs/beads/fast-2lk.1-auth-flow-baseline.md`
- `docs/beads/fast-kql.1-user-profile-api-surface-and-endpoint-set.md`
- `docs/beads/fast-rer.1-menu-listing-api-surface.md`
- `docs/beads/fast-1j3.1-order-creation-api-contract.md`

## Canonical endpoint families (MVP)

- `/auth/...`
- `/users/me`
- `/restaurants` (and `/restaurants/{restaurantId}` where applicable)
- `/menus` (and `/menus/{menuId}`)
- `/orders` (including principal-scoped order operations)

Pluralized resource naming is canonical for collection/detail resources.

## Compatibility policy

- Canonical routes above are the only required contract surface for MVP-forward client/backend integration.
- Legacy route aliases are not required runtime guarantees.
- Any temporary aliases must be treated as migration-only and removable.

## Explicit legacy rejections

The following legacy assumptions are superseded and non-canonical:

- singular `/user` or `/menu` route families as required API surface
- legacy `sid` compatibility as required runtime auth mechanism

JWT bearer + canonical route families are the required direction.

## Versioning and naming stability rules

- Do not rename canonical endpoint families in MVP once published.
- Identifier naming stays stable by contract (`userId`, `restaurantId`, `menuId`, `orderId`; no alias swapping such as `id`/`itemId` for canonical docs).
- New optional endpoints may be added, but must not change the semantics of existing canonical paths.

## Superseded planning assumptions

This bead supersedes prior planning/checklist assumptions that used legacy route naming as baseline references (for example legacy `/menu` and `/api/user/**` style assumptions).

## Non-goals

- No implementation/migration rollout sequencing.
- No deprecation timeline calendar.
- No transport/version-prefix prescription beyond endpoint-family stability.
