# fast-35k.1 — Mandatory Unit and Integration Test Scope (T1.1)

This bead defines minimum required test scope for backend quality gates.

## Scope categories

## 1) Unit test minimums

- Business-rule validation (profile/menu/order contract rules)
- DTO validation and mapping logic
- Error-category selection logic (where centralized)
- State-transition guard logic for order statuses

## 2) Integration test minimums

- Spring context bootstrap (`@SpringBootTest` baseline)
- Security filter behavior for protected vs public routes
- End-to-end error envelope consistency (`error`, `status`, `path`, `traceId` when present)
- Persistence interaction paths once repositories/services are implemented

## Mandatory contract coverage focus

- `/users/me` principal-scoped semantics
- `/menus` and `/menus/{menuId}` field-name stability
- Order state and transition rules from `fast-2p4.6`
- S1.4 auth/authz status-code boundaries (`401` vs `403`)

## Out-of-scope for this bead

- Exact test class names/layout
- Coverage percentage target enforcement logic
- CI pipeline implementation details
