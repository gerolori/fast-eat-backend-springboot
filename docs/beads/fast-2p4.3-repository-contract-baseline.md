# fast-2p4.3 — Repository Contract Baseline (MVP)

## Purpose and scope
- Define a practical repository contract baseline that follows:
  - `fast-2p4.1-domain-entity-inventory.md`
  - `fast-2p4.2-persistence-naming-and-mapping.md`
- Unblock upcoming `User/Menu/Order` repository interface creation with clear, minimal contract boundaries.
- Keep this bead contract-focused only (no repository implementation, no service orchestration design).

## Repository boundary intent

Repositories in this MVP are **domain persistence ports** for aggregate roots. Their job is to expose persistence-oriented access patterns for entities, not business workflows.

Direction:
- Service layer -> repository interfaces -> persistence.
- Repositories return/domain entities and persistence-safe primitives only.
- Business decisions and cross-aggregate orchestration stay in services.

## Aggregate/root repository responsibilities

### User repository (root: `User`)
- Owns user aggregate persistence reads/writes by ID.
- Provides identity/existence checks needed by authenticated flows.
- Provides ownership anchor lookups used by other aggregates (for example, verifying user presence before order ownership checks).

### Menu repository (root: `Menu`)
- Owns menu aggregate persistence reads/writes by ID.
- Provides publish/browse-oriented retrieval patterns required for menu listing/detail screens.
- Exposes availability-filterable retrieval patterns that remain purely data-access level.

### Order repository (root: `Order`)
- Owns order aggregate persistence reads/writes by ID.
- Provides user-owned order retrieval patterns (history/status views) with explicit ownership filters.
- Supports status-oriented filtering where criteria are persistence predicates, not business process orchestration.

### Ingredient note
- Ingredient persistence may exist as its own repository when needed.
- Until a dedicated use case requires direct ingredient-level access, ingredient traversal can remain behind menu/order aggregate data access.

## Baseline query contract categories

Repository contracts may include query methods for:
- **Find-by-ID:** canonical entity retrieval by identifier.
- **Existence checks:** boolean checks for IDs or unique keys.
- **Ownership checks:** constrained queries that enforce root ownership in data access (e.g., order belongs to user).
- **Pagination:** pageable list retrieval for user/order/menu listing scenarios.
- **Sorting:** deterministic ordering tied to persisted columns (e.g., created time, display name).

Repository contracts should avoid embedding business narratives in method intent. Query semantics should describe **data predicates**, not workflow steps.

## What belongs in repositories vs service layer

### Belongs in repository contracts
- Single-aggregate persistence access.
- Predicate-based filtering tied to persisted fields.
- Existence and ownership guard queries.
- Pagination/sorting parameters and return shapes.

### Belongs in service layer (not repository contracts)
- Multi-step business orchestration.
- Cross-aggregate transactional decisions beyond simple ownership/predicate checks.
- Policy evaluation (authorization/business rules) that combines multiple repositories or external systems.
- API request/response shaping.

## Query-boundary direction

- Repository methods are defined from **domain service needs toward persistence predicates**.
- Services request data via repository contracts; repositories do not call services.
- Upward leakage is disallowed: repository signatures must not reference controller models, DTOs, or transport concerns.
- Downward leakage is disallowed: controllers should not bypass services to express persistence query composition directly.

## Explicit exclusions from repository contracts

Do **not** include the following in repository interfaces:
- Controller concerns (HTTP status, path/query parameter semantics, response envelopes).
- API DTOs or request/response objects.
- Business use-case orchestration methods (example anti-pattern: `placeOrderAndNotify(...)`).
- Presentation-driven projections unless a later bead explicitly introduces query/read-model patterns.

## Contract guidance for upcoming `User/Menu/Order` interfaces

When defining concrete repository interfaces next:
- Include at minimum one find-by-ID path per root.
- Include explicit existence checks where service guard clauses depend on them.
- For `Order`, include ownership-constrained access paths (user + order identity).
- For list endpoints, prefer pageable contracts with explicit sort direction rather than ad-hoc in-memory sorting.
- Keep names aligned with entity terminology from `fast-2p4.1` and package boundaries from `fast-2p4.2`.

## Non-goals for this bead
- No Java interface definitions yet.
- No Spring Data method signatures or query annotations yet.
- No custom query optimization strategy.
- No service-layer workflow implementation.
