# fast-j65.2 — Promote Restaurant to First-Class Aggregate (I0.2)

This bead freezes the MVP domain direction that makes `Restaurant` a first-class aggregate.

Cross-reference domain/API beads:

- `docs/beads/fast-2p4.1-domain-entity-inventory.md`
- `docs/beads/fast-2p4.8-domain-contract-summary.md`
- `docs/beads/fast-rer.1-menu-listing-api-surface.md`

## Frozen aggregate decision

- `Restaurant` is an MVP aggregate root.
- `Menu` is a child resource owned by `Restaurant` at domain level.
- Menu identity remains stable (`menuId`), with restaurant ownership represented explicitly in domain/application boundaries.

## Frozen ownership/actor model

- `ADMIN` is the only role allowed to create/update/manage restaurant-owned catalog resources.
- `CUSTOMER` remains read/order focused and does not own restaurant-management operations.
- Ownership enforcement for restaurant-managed resources must be server-derived from trusted principal context.

## API-facing implications (contract-level only)

- Restaurant-facing surfaces are now part of canonical MVP API direction.
- Menu contracts remain valid but are interpreted under restaurant ownership (not global ownerless catalog assumptions).
- Existing `/menus` consumer contracts remain stable while internal/resource modeling recognizes restaurant parentage.

## Superseded planning assumptions

This bead explicitly supersedes prior planning that treated:

- `Menu` as the top-level catalog aggregate, and
- `Restaurant` as deferred/not part of MVP.

Specifically superseded source assumptions:

- `docs/beads/fast-2p4.1-domain-entity-inventory.md` ("Menu remains the top-level catalog entity for now" and "Restaurant is explicitly deferred")
- `docs/beads/fast-2p4.8-domain-contract-summary.md` ("Restaurant remains intentionally deferred for MVP")

## Non-goals

- No schema/migration details for restaurant persistence.
- No controller/service implementation.
- No final restaurant DTO field inventory beyond ownership/aggregate direction.
