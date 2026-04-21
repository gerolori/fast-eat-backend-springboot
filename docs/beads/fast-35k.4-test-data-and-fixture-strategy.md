# fast-35k.4 — Test Data and Fixture Strategy (T1.4)

This bead defines a simple fixture strategy for deterministic tests.

## Fixture principles

- Keep fixtures minimal, explicit, and scenario-oriented.
- Prefer API-contract-shaped values over entity-internal coupling.
- Reuse canonical IDs/status values across tests for readability.

## Recommended fixture sets

### Users

- `customerA`, `customerB`, `adminA`
- Stable principal IDs for ownership tests

### Menus

- One available menu
- One unavailable menu
- Shared monetary shape in every menu fixture:
  - `price.amount` string decimal
  - `price.currency` string code

### Orders

- One order in each canonical status (`PENDING`, `CONFIRMED`, `PREPARING`, `READY_FOR_PICKUP`, `COMPLETED`, `CANCELLED`)
- Ownership split between customer fixtures for authz tests

## Fixture layering

- Unit tests: in-memory object builders/factories.
- Integration tests: seed script or dedicated test configuration, deterministic and resettable.

## Anti-patterns to avoid

- Randomized fixture values without seed control.
- Hidden shared mutable fixtures causing order-dependent tests.
- Mismatch between fixture field names and API contract names.
