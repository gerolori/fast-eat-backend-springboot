# fast-36f.3 — Phased Extraction Order and Rollback Strategy (L1.3)

> Future-state planning document. This does not change current single-module runtime.

This bead defines a conservative extraction sequence and rollback approach.

## Suggested phased extraction order (future)

1. **Extract common/shared utilities first**
   - Lowest behavioral risk.
2. **Extract API boundary next**
   - Keep contract surface explicit while services remain local.
3. **Extract domain/service layer**
   - Preserve order/profile/menu business invariants.
4. **Extract persistence layer**
   - Move entities/repositories with strict mapping parity checks.
5. **Extract security module last**
   - Highest integration risk; perform after stable internal boundaries exist.

## Rollback strategy

- Keep each extraction in small, reversible commits.
- Maintain branch-level checkpoints after each phase.
- If verification fails, revert current phase only; keep previous successful phases.
- Do not proceed to next phase until `mvnw.cmd verify` is stable.

## Safety constraints

- Preserve external API behavior (`/users/me`, `/menus`, `/orders`).
- Preserve error envelope and `traceId`/correlation conventions.
- Preserve order state semantics from D1.6.
