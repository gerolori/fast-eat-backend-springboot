# fast-3q7.6 — DevEx Backlog (P1.6)

This bead records non-blocking developer experience improvements for later execution.

## Backlog items (prioritized)

1. **Add lightweight command wrapper scripts (P2)**
   - Goal: one-command local run/test shortcuts.
   - Constraint: keep wrapper thin over `mvnw.cmd`.

2. **Add request logging middleware/filter baseline (P2)**
   - Goal: guaranteed request start/end logs with `correlationId`.
   - Align with `fast-3q7.2`/`fast-3q7.4`.

3. **Add error code registry doc (P2)**
   - Goal: one place mapping error categories to status and meaning.
   - Include auth/authz + profile/menu/order domains.

4. **Introduce CI doc-check gate (P3)**
   - Goal: fail CI on broken markdown links/anchors in docs/beads.

5. **Add developer onboarding smoke script (P3)**
   - Goal: single local script for `clean verify` plus health check.

## Explicitly not in current scope

- No mandatory module split.
- No observability platform rollout.
- No production infrastructure changes.

## Done criteria for each backlog item

- Clear owner and acceptance checklist.
- Verified commands in current repo state.
- Documentation updated with truthful, non-aspirational wording.
