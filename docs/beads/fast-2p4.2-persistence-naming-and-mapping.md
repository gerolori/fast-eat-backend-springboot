# fast-2p4.2 — Persistence Naming and Mapping Conventions (MVP)

## Purpose and scope
- Establish persistence conventions that directly follow the entity inventory in `fast-2p4.1-domain-entity-inventory.md`.
- Provide implementation direction for upcoming entity/repository work (`fast-2p4.3`) and persistence wiring (`fast-2p4.7`).
- Keep this document as conventions-only (no schema dump, no entity/repository implementation).

## Package placement conventions (`com.gerolori.fasteat.domain`)

Use explicit package boundaries to keep persistence internals cohesive and isolated from API/DTO concerns.

- `com.gerolori.fasteat.domain.entity`
  - JPA entities only (`UserEntity`, `MenuEntity`, `IngredientEntity`, `OrderEntity`, shared base/audit entity).
- `com.gerolori.fasteat.domain.repository`
  - Spring Data repository interfaces only.
- `com.gerolori.fasteat.domain.enums`
  - Persistence-relevant enums (for example `UserRole`, `OrderStatus`).

Direction:
- Entity classes remain persistence models, not API contracts.
- Repositories depend on entities/enums in `domain`; API-layer types must not be referenced by repository signatures.

## Table naming conventions

- Use `snake_case` lowercase plural table names.
- Baseline names from D1.1 inventory:
  - `users`
  - `menus`
  - `ingredients`
  - `orders`
- Join/association tables (if required later) also use `snake_case` and explicit entity semantics (example style: `menu_ingredients`).

## Column naming conventions

- Use `snake_case` lowercase for all columns.
- Primary key column name: `id`.
- Foreign key columns are `<referenced_entity_singular>_id`:
  - Example direction: `user_id`, `menu_id`, `ingredient_id`, `order_id`.
- Audit columns use fixed names:
  - `created_at`
  - `updated_at`

## Primary key / foreign key direction

- Each root table owns a single primary key column named `id`.
- Child/dependent table carries the foreign key column pointing to parent `id`.
- Constraint naming style (when explicitly named):
  - Primary key: `pk_<table_name>`
  - Foreign key: `fk_<from_table>_<to_table>`
- Keep FK direction readable from the child side (the column lives where the dependency lives).

## Identifier strategy direction

- UUID is the baseline identifier strategy for new persisted entities.
- Store and map entity identifiers consistently as UUID-backed IDs.
- Do not mix numeric surrogate IDs and UUID IDs within this MVP domain set unless a later bead explicitly introduces an exception.

## Enum persistence direction

- Persist enums as string values (name-based), not ordinal indexes.
- String persistence is required for forward-safe enum evolution (adding/reordering values must not corrupt stored meaning).

## Relationship ownership and mapping guidance

- Default to minimal and unidirectional mappings.
- Introduce bidirectional mappings only when a concrete use case requires navigation from both sides.
- Keep ownership on the side that writes/owns the foreign key to reduce accidental update complexity.
- Avoid convenience relationships that are not required by current use cases.

## Persistence/API decoupling rule

- DTOs, request/response shapes, and controller contracts must not leak into domain persistence internals.
- Entities and repositories stay independent from API package types.
- Mapping between API-layer DTOs and persistence entities should occur in service/adapter boundaries, not inside entities/repositories.

## Non-goals for this bead

- No full schema definition.
- No JPA entity class implementation.
- No repository interface implementation.
- No migration scripts.
