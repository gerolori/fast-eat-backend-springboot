# fast-3ro.3 — Track Architecture Risk Escalations (X1.3)

This document defines architecture-linked risks that can change backend scope or delivery timing, plus escalation thresholds.

## Risk register (architecture dependency view)

| Risk | Observable signal | Impact on backend | Escalation threshold |
| --- | --- | --- | --- |
| ADR churn on core contracts | Multiple revisions to API/security/error ADRs in short interval | Repeated docs updates, delayed code freeze, client incompatibility risk | 2+ conflicting revisions after backend has started consumer implementation |
| Missing canonical decision | Key ADR stream has no accepted record | Backend forced to infer behavior and risks divergence | Any blocking decision still missing when dependent task is ready-to-start |
| Late compatibility reversals | Upstream ADR changes backward-compatibility posture late | Potential breaking API/security behavior across clients | Compatibility change after backend marks dependent contract as stable |
| Cross-repo interpretation mismatch | Architecture text interpreted differently by backend/client repos | Inconsistent endpoint or error behavior across clients | First detected mismatch across repo docs or acceptance tests |
| Unowned upstream dependency | No architecture owner actively tracking the decision | Planning cannot forecast unblock date | Owner missing for >1 planning cycle |

## Escalation path

1. Record risk in backend lane notes (`docs/beads`) with impacted tasks.
2. Notify architecture maintainers with concrete mismatch/blocking example.
3. Request explicit decision statement (accepted/deferred/rejected) and compatibility guidance.
4. Re-sequence backend dependent tasks only after decision is explicit.

## Severity guidance

- **High:** blocks a current backend task or risks breaking existing client contract assumptions.
- **Medium:** does not block today, but can invalidate near-term sequencing.
- **Low:** editorial or timing noise with no immediate contract impact.

Treat High severity risks as release-planning blockers until architecture decision state is clarified.
