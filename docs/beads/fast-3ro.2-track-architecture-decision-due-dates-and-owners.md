# fast-3ro.2 — Track Architecture Decision Due Dates and Owners (X1.2)

This document defines ownership and due-date expectations for architecture decisions that gate backend sequencing.

## Operating assumptions from backend planning refresh

- Backend continues as a single-module service until late-split work is explicitly started.
- External architecture decisions must land early enough to avoid rework in API/security/order lanes.
- Ownership is tracked by role/team in docs; named assignees stay in the external architecture tracker.

## Decision ownership matrix

| Decision stream | Primary owner (architecture repo) | Backend consumer owner | Timing expectation |
| --- | --- | --- | --- |
| API compatibility + versioning ADRs | Architecture API steward | Backend API maintainer | Before any contract-breaking backend change proposal |
| Security/auth lifecycle ADRs | Architecture security steward | Backend security maintainer | Before auth hardening and token behavior changes |
| Error model ADRs | Architecture platform steward | Backend platform/API maintainer | Before introducing new public error codes/envelopes |
| Order workflow/state ADRs | Architecture domain steward | Backend orders/domain maintainer | Before workflow transition or state-model changes |
| Geo semantics ADRs | Architecture domain steward | Backend menu/orders maintainer | Before availability and distance-rule tightening |

## Due-date policy (for dependency planning)

1. **Initial ADR draft** should exist before backend lane starts implementation that consumes it.
2. **Final ADR merge** should be complete before backend moves the consuming task from planning/doc to code.
3. **ADR change after merge** must trigger same-day downstream impact note in backend docs.

## Escalation trigger for ownership/due-date drift

Escalate to architecture and backend maintainers if any blocking ADR has:

- no active owner,
- no target date/window,
- or no compatibility statement after merge.

These conditions indicate planning risk and should be treated as release-sequencing blockers.
