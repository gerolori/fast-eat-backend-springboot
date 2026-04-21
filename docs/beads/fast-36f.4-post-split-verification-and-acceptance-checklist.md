# fast-36f.4 — Post-Split Verification and Acceptance Checklist (L1.4)

> Future-state planning checklist. Applies only when a module split is actually executed.

## Acceptance checklist (future split)

### Build and runtime

- [ ] Multi-module build succeeds from clean checkout.
- [ ] `mvnw.cmd verify` succeeds across all modules.
- [ ] Local run path still works for backend application entrypoint.

### Contract behavior parity

- [ ] `/users/me` behavior unchanged (principal-scoped semantics).
- [ ] `/menus` and `/menus/{menuId}` payload field names unchanged.
- [ ] Order status values/transitions match D1.6.
- [ ] Error envelopes still include expected stable fields and traceability metadata.

### Security and observability parity

- [ ] `401`/`403` category boundaries remain intact.
- [ ] Correlation/trace propagation unchanged (`X-Correlation-Id`, `traceId`).
- [ ] No new sensitive logging regressions introduced.

### Operational safety

- [ ] Rollback plan tested or rehearsed.
- [ ] Ownership of each module documented.
- [ ] Open defects from split are triaged and tracked.

## Current-state reminder

The backend is currently single-module; this checklist is deliberately pre-work for future L1 execution.
