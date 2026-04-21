# fast-kql.3 — Profile Authorization and Ownership Checks (A1.3)

This bead defines the **authorization contract** for profile read/update behavior in the A1 profile API stream.

It aligns with:

- S1.2 authorization model (`docs/beads/fast-2lk.2-authorization-model.md`)
- S1.4 security error handling contract (`docs/beads/fast-2lk.4-security-error-handling-contract.md`)
- D1.4 user domain invariants (`docs/beads/fast-2p4.4-user-domain-constraints-and-invariants.md`)
- A1 direction for current-user profile API shape (`/users/me` baseline)

This document is contract/policy level only. It does **not** define framework wiring, annotations, or filter implementation details.

## Scope and intent

- Define who may read/update profile data in MVP.
- Define ownership semantics for current-user profile endpoints.
- Prevent privilege escalation through payload/path tampering.
- Keep behavior consistent with S1.4 status/error semantics.

## Protected profile endpoint baseline

- `GET /users/me` is a protected endpoint and requires authenticated principal context.
- `PATCH /users/me` (or equivalent self-profile update endpoint) is protected and requires authenticated principal context.
- The target user for `/users/me` read/update is always derived from principal identity (`principal.userId`-equivalent), never from request body/query/path user identifiers.

Unauthenticated access to protected profile endpoints maps to `401` categories from S1.4.

## Self-only profile access/update rules

For non-admin callers (default `CUSTOMER` flows):

- Read scope is self-only (`/users/me` resolves to caller identity only).
- Update scope is self-only for explicitly mutable profile fields.
- Attempts to influence target identity (for example `userId` in payload/path/query) must not change effective target user.
- Update behavior must preserve D1.4 invariants (immutable identity fields, role constraints, trusted security-field handling).

## Admin override behavior

MVP baseline keeps `/users/me` strictly principal-scoped even for `ADMIN`.

- `ADMIN` calling `/users/me` reads/updates the admin's own profile only.
- No implicit "act as another user" behavior is allowed through `/users/me`.

If future A1/Admin management endpoints are introduced (for example `/users/{id}`), they may define explicit admin override policies separately. Those policies must remain distinct from `/users/me` semantics.

## Anti-escalation and field-level constraints

- Role assignment/removal is not part of self-profile update surface.
- Client-supplied role/authority/admin flags and other restricted/unknown fields in profile payloads must be rejected (strict reject semantics per A1.2), and must never grant privilege.
- Sensitive/security-governed attributes (for example password hash storage concerns, account status governance flags) are outside generic self-profile mutation unless explicitly defined by dedicated flows.
- Authorization decisions must be based on authenticated principal context and trusted server-side data, not caller-declared claims.

## Ownership and denial semantics

- Ownership for profile operations is defined as `targetUserId == principal.userId` for `/users/me`.
- Any policy denial after successful authentication (insufficient role/ownership rule violation on future non-`/me` endpoints) maps to `403` categories from S1.4.
- Do not return security-error shapes that imply token invalidity when the real condition is authorization denial.

## Contract-level examples

- `GET /users/me` with missing/invalid/expired token → `401` (S1.4 auth categories).
- `PATCH /users/me` with valid auth and allowed fields → evaluated as self-update for principal user.
- `PATCH /users/me` payload attempts `role: ADMIN` or foreign `userId` override → rejected per strict update contract; no escalation.
- Future `PATCH /users/{id}` by non-owner non-admin (if introduced) → `403` ownership/role denial.

## Explicit non-goals

- No controller/service/security-filter implementation details.
- No framework-specific method-security expression design.
- No final DTO field list; only authorization/ownership contract direction.
- No expansion of role model beyond S1.2 baseline (`CUSTOMER`, `ADMIN`).
