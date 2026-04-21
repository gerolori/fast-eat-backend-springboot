# fast-1k1.5 — Seed, Fixture, and CI Verification Baseline (T1.5)

This bead establishes the minimal baseline lane for deterministic seed/fixture behavior and CI verification.

## Seed and fixture baseline

- Keep deterministic bootstrap in `local` and `test` profiles via `DemoDataBootstrap`.
- Preserve seeded identities used by tests and local bootstrap:
  - `admin@fasteat.local`
  - `owner@fasteat.local`
  - `customer@fasteat.local`
- Preserve seeded catalog/order fixture anchors:
  - Restaurants: `Fast Eat Downtown Kitchen`, `Fast Eat Uptown Kitchen`
  - Menus: `Classic Burger Combo`, `Chicken Rice Bowl`, `Creamy Mushroom Pasta`
  - Order statuses: all canonical `OrderStatus` values are represented.

## Verification baseline command

- Canonical baseline command: `./mvnw.cmd test`
- Rationale: uses wrapper-pinned Maven, executes the current unit + integration test baseline in this repository.

## CI baseline lane

- Add a narrow GitHub Actions workflow at `.github/workflows/ci-baseline.yml`.
- Trigger on `push` and `pull_request`.
- Run on `windows-latest` and execute only the baseline command:
  - `./mvnw.cmd test`

## Scope guardrails

- No refactor of seed/bootstrap architecture.
- No new fixture framework.
- No expansion to multi-job CI matrix in this bead.
