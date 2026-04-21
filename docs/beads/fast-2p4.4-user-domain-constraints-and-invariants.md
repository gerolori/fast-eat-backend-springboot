# fast-2p4.4 — User Domain Constraints and Invariants (D1.4)

This bead defines the **user domain contract constraints** that follow:

- entity inventory in `docs/beads/fast-2p4.1-domain-entity-inventory.md`
- persistence naming/mapping direction in `docs/beads/fast-2p4.2-persistence-naming-and-mapping.md`
- authorization baseline in `docs/beads/fast-2lk.2-authorization-model.md`

Scope is intentionally domain-contract level (not schema or endpoint implementation) and is intended to support later **A1.1 user profile API planning**.

## User identity and uniqueness invariants

- Each user has a stable domain identity (`id`) that is immutable once created.
- Login identity must be unique system-wide (email/username choice is implementation detail for later beads, but whichever login identifier is chosen must be globally unique).
- Role membership is part of trusted identity context and cannot be inferred from client-supplied payload claims.
- User records must not be duplicated for the same login identity.

## Mutable vs immutable user field direction

### Immutable after creation

- `id` (stable UUID-backed identity baseline)
- account creation timestamp metadata (`created_at` direction from D1.2)
- initial trust/security bootstrap attributes that define account origin (exact fields deferred)

### Mutable under controlled updates

- non-sensitive profile attributes used for display/contact purposes (for example display name and profile-facing contact details)
- last-updated metadata (`updated_at`)
- account status flags only through trusted service/admin paths (not arbitrary self-service mutation)

Rule: mutable fields are still subject to validation and authorization checks; mutability does not imply unrestricted client control.

## Role assignment and anti-escalation constraints

- Default end-user role direction remains `CUSTOMER` (per S1.2 baseline).
- `ADMIN` assignment/removal is privileged and must occur only through trusted admin/governed flows.
- Self-service profile updates must not accept role changes.
- Any request payload field that attempts role elevation must be ignored/rejected by service-layer rules.
- Authorization decisions must always come from authenticated principal context, never from caller-declared role values.

## Profile update constraints (A1.1-facing contract)

- `/users/me`-style updates are principal-scoped; target user identity is resolved from auth context, not body/path user ID input.
- Client-facing profile update contracts should be allowlist-based (explicitly mutable fields only).
- Unknown or restricted fields in update payloads should be rejected or ignored consistently (final behavior to be fixed in API/error-contract beads).
- Updates must preserve domain invariants (identity uniqueness, immutable fields, role constraints).
- Concurrency/conflict handling strategy is deferred, but update flows must avoid silent invariant breakage.

## Sensitive-field handling boundaries

The following boundaries apply at the domain contract level:

- **Password data**
  - Plaintext password is never a persisted domain value.
  - User domain persistence stores only secure password hash representation (BCrypt direction is defined in S1.1).
  - Password change/reset flows are security flows, not generic profile-edit fields.

- **Payment data**
  - Raw payment instrument details are out of scope for the core `User` aggregate in MVP.
  - If payment capabilities are introduced later, user domain should retain only references/aliases needed for business flow, not full sensitive payment payloads.

- **Private personal data**
  - Store only minimum private data required for MVP behavior.
  - Sensitive/private attributes must be excluded from default list/read responses unless explicitly required by use case.
  - Private-data exposure policy is API-contract controlled and must remain consistent with authorization/ownership rules.

## Non-goals for this bead

- No JPA/entity field list finalization.
- No database constraint DDL or migration scripts.
- No controller/service implementation.
- No final error response contract for invalid updates.
