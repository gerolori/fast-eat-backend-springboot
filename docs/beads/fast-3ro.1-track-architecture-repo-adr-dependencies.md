# fast-3ro.1 — Track Architecture Repo ADR Dependencies (X1.1)

This document captures architecture-repo decisions that are strict prerequisites for backend implementation sequencing.

## Backend baseline this depends on

- Current backend is a **single-module Spring Boot** application (`DEVELOPMENT.md`).
- Package boundaries (`web`, `domain`, `security`, `platform`) are organizational, not Maven modules.
- API/domain/security behavior is currently documented in `docs/beads/*` contract notes from the planning refresh lanes.

## ADR dependency register (architecture repo → backend)

| Dependency area | Architecture decision needed | Why backend is blocked without it | Backend surfaces affected |
| --- | --- | --- | --- |
| API versioning policy | Freeze compatibility policy for `/api/v1` change classes and deprecation windows | Backend cannot safely tighten validation/error semantics without cross-repo compatibility rules | `web` controllers, API contract docs (`A1`, `A2`, `O1`) |
| Auth token lifecycle | Confirm token TTL/refresh/revocation contract and claim stability | Security behavior and client interoperability depend on one canonical token contract | `security` package, auth endpoints, security contract docs (`S1`) |
| Error envelope canonicalization | Confirm stable error body shape and code taxonomy | Backend exception mapping needs one canonical envelope to avoid client-specific drift | global exception handlers, all public APIs |
| Order workflow state authority | Confirm canonical order state names and transition ownership | Backend workflow/service logic must match architecture state machine exactly | order domain/services and order API docs (`O1`) |
| Location/distance semantics | Confirm geo precision, units, and rounding rules | Menu availability and delivery checks require shared spatial semantics across repos | menu availability and order eligibility rules |

## Consumption guidance

For each architecture ADR merged upstream, mirror the result in this repository by updating the corresponding Beads docs lane before code-level changes.

## Handoff notes for architecture maintainers

When publishing/updating ADRs, include:

1. Effective date/version of the decision.
2. Explicit backward-compatibility statement.
3. Which backend contract doc(s) must be refreshed.

Without these fields, backend planning cannot reliably mark dependencies as unblocked.
