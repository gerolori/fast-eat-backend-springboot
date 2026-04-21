# fast-3q7.3 — Developer Workflow and Local Command Shortcuts (P1.3)

This bead documents practical local workflow commands for the current single-module backend.

## Scope

- Local bootstrap and run/test commands.
- Short command matrix for common loops.
- No claim of additional scripts/tools not present in repo.

## Baseline command shortcuts (Windows)

From repo root:

- Run app: `mvnw.cmd spring-boot:run`
- Run tests: `mvnw.cmd test`
- Full verification: `mvnw.cmd verify`
- Clean + verify: `mvnw.cmd clean verify`

## Typical local workflow

1. Pull latest `develop` (or rebase feature branch).
2. Run `mvnw.cmd test` before starting new work.
3. Implement scoped change.
4. Re-run narrowest needed verification (docs-only changes may skip Maven tests).
5. For release-facing changes, run `mvnw.cmd verify`.

## Suggested aliases (optional, local shell profile)

These are optional examples, not repo-managed scripts:

- `fe-run` -> `mvnw.cmd spring-boot:run`
- `fe-test` -> `mvnw.cmd test`
- `fe-verify` -> `mvnw.cmd verify`

## Guardrails

- Keep command guidance truthful to current repo (no Makefile assumptions).
- Prefer Maven wrapper (`mvnw.cmd`) for toolchain consistency.
- Maintain single-module-first reality; do not imply split-module command topology.
