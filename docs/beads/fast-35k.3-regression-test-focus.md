# fast-35k.3 — Regression Test Focus (T1.3)

This bead defines regression hotspots that must be protected as APIs evolve.

## High-priority regression lanes

1. **Auth vs authz boundary**
   - Protected route without token -> `401`
   - Authenticated but not allowed -> `403`

2. **Profile contract invariants**
   - `/users/me` identity from principal only
   - restricted fields rejected in profile update payload

3. **Menu contract stability**
   - Canonical paths `/menus`, `/menus/{menuId}`
   - shared field names unchanged (`menuId`, `summary`, `price.amount`, `price.currency`)
   - availability semantics preserved (`isAvailable` + `available` filter)

4. **Order lifecycle invariants**
   - Allowed transitions succeed
   - Backward/skip/terminal-escape transitions fail with stable category
   - Owner-scoped access enforced for non-admin callers

5. **Error traceability**
   - Error response includes stable envelope
   - `traceId` present/mapped per correlation contract where configured

## Regression execution guidance

- Run focused tests first for touched domain, then broader `mvnw.cmd test`/`verify`.
- Keep regression coverage aligned with spec documents rather than implementation internals.
