# fast-35k.2 — CI Verification Command Matrix (T1.2)

This bead defines command-level verification expectations using current repo tooling.

## Baseline matrix

| Scenario | Minimum command | Purpose |
| --- | --- | --- |
| Docs-only changes | `git status --short --branch` + `br list --no-db --no-auto-flush` + `br count --no-db --no-auto-flush --by-type` | Confirm workspace and beads lifecycle state. |
| Normal code changes | `mvnw.cmd test` | Fast baseline validation. |
| Merge/release candidate | `mvnw.cmd verify` | Full unit/integration lifecycle checks configured by Maven. |
| Deep clean validation | `mvnw.cmd clean verify` | Detect stale-artifact masking issues. |

## CI direction

- CI should run at least `mvnw.cmd verify` for code-impacting branches.
- Docs/spec-only branches may use docs/beads checks unless policy requires full verify.
- Any failure must block merge until resolved.

## Command consistency rule

- Prefer Maven wrapper (`mvnw.cmd`) to avoid host Maven version drift.

## Non-goals

- No provider-specific CI YAML changes in this bead.
- No claim about additional linters/tools not present in current repo.
