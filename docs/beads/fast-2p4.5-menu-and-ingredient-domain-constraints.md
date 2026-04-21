# fast-2p4.5 — Menu and Ingredient Domain Constraints (D1.5)

This bead defines the **menu and ingredient domain contract constraints** that follow:

- entity inventory in `docs/beads/fast-2p4.1-domain-entity-inventory.md`
- persistence naming/mapping direction in `docs/beads/fast-2p4.2-persistence-naming-and-mapping.md`
- repository contract baseline in `docs/beads/fast-2p4.3-repository-contract-baseline.md`
- DTO/entity mapping boundary rules in `docs/beads/fast-2p4.7-dto-to-entity-mapping-boundaries.md`

Scope is intentionally domain-contract level (not schema/repository/API implementation detail) and is intended to unblock upcoming order-state and menu API work.

## Menu–Ingredient relationship rules

- `Menu` is the aggregate surface for customer-facing catalog decisions.
- `Ingredient` is a reusable domain component that can be associated with multiple menus.
- A menu depends on one-or-more ingredient associations to define what is required to fulfill that menu item.
- An ingredient may be shared by many menus; a menu may reference many ingredients.
- Relationship semantics are dependency-oriented: menu fulfillment depends on ingredient viability.

Direction:

- The menu side governs which ingredient references are part of a menu definition.
- The ingredient side remains reusable inventory-facing data, not a container of menu definitions.

## Ingredient reuse vs containment/reference direction

- Ingredient identity is global within the domain inventory and is reused across menus.
- Menus **reference ingredients**; they do not own independent duplicated ingredient records per menu.
- Removing or changing one menu must not implicitly delete or mutate shared ingredient identity used by other menus.
- Lifecycle and stock/availability changes for an ingredient conceptually propagate to all menus that reference it.
- Menu-level customization (if needed later) should be represented as association/context data, not by cloning ingredient identity.

## Mutable vs immutable menu field direction

### Immutable after creation

- `id` (stable UUID-backed identity baseline from D1.2 direction)
- creation-time audit metadata baseline (`created_at` direction)
- canonical origin attributes that anchor menu identity lineage (exact field names deferred)

### Mutable under controlled updates

- customer-visible descriptive metadata (for example name/description/category-like labels)
- pricing/display-facing fields that are expected to evolve over time
- ingredient association composition for a menu (add/remove/replace semantics defined by service-layer contract later)
- publishability/availability-facing flags and update-time metadata (`updated_at` direction)

Rule: mutable fields remain guarded by validation and authorization policy; mutability does not imply unconstrained client control.

## Availability and publishability direction

- **Publishability** answers: should this menu be exposed to customer browse surfaces at all?
- **Availability** answers: if exposed, is it currently fulfillable for ordering?
- Exposure direction is conservative: a menu is customer-visible only when it is both publishable and currently available according to domain policy.
- Ingredient viability constrains menu availability, but does not itself imply publishability.
- Unpublished menus are excluded from customer-facing list/detail exposure regardless of ingredient state.

## List/detail retrieval expectations (contract-level)

The following expectations are data-contract direction only (not endpoint/repository signature detail):

- Customer menu **list retrieval** should return only menus eligible for customer exposure under publishability/availability policy.
- List retrieval should support deterministic ordering and pagination semantics at the contract level for stable client behavior.
- Customer menu **detail retrieval** should resolve a specific menu identity only when that menu remains exposure-eligible.
- Admin/operational retrieval contexts may require broader visibility (for example unpublished/unavailable records), but must be explicit and role-governed.
- Ingredient data included in list/detail views should reflect reference semantics (shared ingredient identity), not imply menu-owned ingredient duplication.

## Cross-bead alignment notes

- Aligns with D1.1 by preserving `Menu` as catalog root and `Ingredient` as dependent inventory-facing component.
- Aligns with D1.2 by keeping identifier/audit/persistence conventions directional and implementation-agnostic.
- Aligns with D1.3 by expressing retrieval intent as persistence predicate categories, not business workflow orchestration.
- Aligns with D1.4 user invariants by mirroring immutable-vs-mutable and controlled-update guardrail style for another aggregate.
- Aligns with D1.7 mapping boundaries by keeping DTO/API shaping concerns outside entity constraint definition.

## Non-goals for this bead

- No JPA entity annotations or relationship mapping strategy.
- No schema/table/join modeling details.
- No repository interface method definitions.
- No controller/service/API endpoint contract implementation.
- No order-state transition algorithm definition (only dependency constraints relevant to order/menu work).
