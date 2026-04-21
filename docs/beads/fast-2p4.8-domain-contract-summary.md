# fast-2p4.8 — Domain Contract Summary for API Teams (D1.8)

## Purpose

Provide a concise, implementation-facing domain contract summary for upcoming API workstreams:
- `A1` (profile APIs)
- `A2` (menu APIs)
- `O1` (order workflow APIs)
- external client consumers that need stable contract direction

This document consolidates D1.1–D1.7 and favors cross-reference over re-specification.

## Source map (D1.1–D1.7)

- D1.1: `docs/beads/fast-2p4.1-domain-entity-inventory.md`
- D1.2: `docs/beads/fast-2p4.2-persistence-naming-and-mapping.md`
- D1.3: `docs/beads/fast-2p4.3-repository-contract-baseline.md`
- D1.4: `docs/beads/fast-2p4.4-user-domain-constraints-and-invariants.md`
- D1.5: menu/ingredient constraints (`fast-2p4.5`)
- D1.6: order state model and transition invariants (`fast-2p4.6`)
- D1.7: `docs/beads/fast-2p4.7-dto-to-entity-mapping-boundaries.md`

## Aggregate roots and supporting types

From D1.1 inventory, API design should treat these as the MVP domain baseline:

- **Aggregate roots:** `User`, `Menu`, `Order`
- **Supporting domain entity:** `Ingredient` (menu/inventory-facing dependency)
- **Supporting enums/types:** `UserRole`, `OrderStatus`, shared audit/base metadata, deferred value objects (money/quantity/id wrappers)

Scope note:
- `Restaurant` remains intentionally deferred for MVP.

## Invariant baseline for API teams

Primary invariant source is D1.4 (user), with D1.5/D1.6 defining menu+ingredient and order-transition constraints.

Contract direction to preserve across A1/A2/O1:

- **Identity invariants:** stable immutable root identity; no duplicate login identity for users.
- **Role/authority invariants:** role escalation is privileged-only; authorization context comes from trusted principal state, not caller-declared role fields.
- **Ownership invariants:** ownership-sensitive links (especially user->order) must be server-resolved/enforced.
- **Mutation invariants:** update operations are allowlist-based and must preserve immutable/system-managed fields.
- **State-transition invariants:** order status transitions must be validated against explicit transition rules (D1.6 scope).
- **Menu/ingredient constraints:** availability and composition constraints must be enforced as domain rules, not client assumptions (D1.5 scope).

## Persistence boundaries (D1.2)

Persistence conventions are part of the contract baseline and should remain opaque to transport contracts:

- Persistence models live under `com.gerolori.fasteat.domain` (`entity`, `repository`, `enums`).
- Entity/repository layer is persistence-centric only; no API DTO types in signatures.
- Naming/identifier direction is standardized (snake_case table/column conventions, UUID baseline IDs, enum-as-string persistence).
- Minimal/unidirectional relationship mappings are preferred unless use-case pressure requires more.

For API planning: assume persistence identity/audit mechanics exist, but keep endpoint contracts decoupled from direct schema expression.

## Repository contract direction (D1.3)

Repository interfaces are persistence ports for aggregate roots.

- Direction is **service -> repository -> persistence**.
- Repository methods express data predicates (find/exist/ownership/pagination/sorting), not business workflows.
- Multi-step orchestration, policy decisions, and cross-aggregate business logic stay in services.
- Controller concerns and transport DTOs are explicitly out of repository scope.

This keeps A1/A2/O1 use-case logic composable without leaking transport concerns into persistence contracts.

## DTO/entity mapping boundaries (D1.7)

Mapping is an application/service-layer responsibility:

- Controllers exchange DTOs.
- Services (or dedicated mappers owned by that layer) map DTO <-> entity.
- Entities/repositories remain DTO-agnostic.

Operational boundary reminders:

- Create flows do not accept client-controlled entity IDs/audit fields.
- Update flows patch allowlisted mutable fields on loaded entities (no blind full replacement).
- Read flows return response DTOs only (never raw entities from controllers).
- Ownership, role/status, and privileged transitions require trusted server-side resolution/validation before mapping.

## API-facing application checklist (A1/A2/O1)

Use this as a quick gate before finalizing endpoints:

1. Endpoint contract does not expose or depend on internal entity shape.
2. Identity, ownership, and role constraints are enforced in service logic.
3. Repository calls remain predicate-oriented and aggregate-scoped.
4. DTO mapping happens outside entities/repositories.
5. Menu/ingredient constraints (D1.5) and order transition rules (D1.6) are treated as mandatory domain validations for related endpoints.

## Non-goals

- No new domain rules beyond D1 source beads.
- No code, schema, or migration specification.
- No replacement of detailed D1 documents; this is a consolidation layer only.
