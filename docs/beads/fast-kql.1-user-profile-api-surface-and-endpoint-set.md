# fast-kql.1 — User Profile API Surface and Endpoint Set (A1.1)

This bead defines the **A1.1 user-profile API contract surface** for MVP and aligns with:

- user-domain invariants in `docs/beads/fast-2p4.4-user-domain-constraints-and-invariants.md`
- DTO/entity mapping boundaries in `docs/beads/fast-2p4.7-dto-to-entity-mapping-boundaries.md`
- authorization baseline in `docs/beads/fast-2lk.2-authorization-model.md`
- JWT principal direction in `docs/beads/fast-2lk.3-jwt-claim-and-token-lifecycle-contract.md`

This document is **contract-only** (no controller/service implementation).

## Scope

- Define `/users/me` read and update endpoints for authenticated self-service profile access.
- Define contract-level request/response DTO direction.
- Define readable vs mutable vs protected field exposure boundaries.
- Keep validation detail and error-shape detail mostly deferred to `fast-kql.2`.

## Identity and principal resolution baseline

- `/users/me` is principal-scoped only.
- Target identity must be resolved from validated authentication principal context (JWT `sub` -> `principal.userId`).
- Client-supplied identity inputs (for example `userId` in request body/query/path) are not part of this surface and must not drive identity resolution.
- Authorization remains authenticated self-service (`CUSTOMER` or `ADMIN` principal), with anti-escalation behavior from S1.2.

## Endpoint set (A1.1)

### 1) Get current user profile

- **Method/Path:** `GET /users/me`
- **Auth:** required
- **Identity source:** resolved principal (`principal.userId`)
- **Semantics:** return profile view for the authenticated caller.

Response DTO direction: `UserProfileResponse` (server -> client)

### 2) Update current user profile

- **Method/Path:** `PATCH /users/me` (partial update semantics)
- **Auth:** required
- **Identity source:** resolved principal (`principal.userId`)
- **Semantics:** apply allowlisted mutable profile fields for current authenticated user.

Request DTO direction: `UpdateUserProfileRequest` (client -> server)  
Response DTO direction: `UserProfileResponse` (server -> client, post-update view)

No `PUT /users/{id}` or client-targeted arbitrary user update endpoint is introduced in A1.1.

## Contract DTO direction and mapping boundaries

- Controllers operate on request/response DTOs only.
- Service/application boundary maps:
  - `UpdateUserProfileRequest` -> existing `User` entity (selective field patch only)
  - `User` entity -> `UserProfileResponse`
- Repositories remain entity-centric and do not accept API DTOs.
- Update mapping is allowlist-based; unknown/restricted fields are not treated as mutable profile input.

This keeps A1.1 aligned with mapping boundaries from `fast-2p4.7`.

## Field exposure contract (readable vs mutable vs protected)

Field names below are contract-level placeholders/direction for A1.1 and may be finalized in implementation beads.

### Readable fields (`UserProfileResponse`)

- stable user identifier (`id`)
- login/display identity fields intended for caller visibility (for example `email`, `displayName`)
- profile-facing contact fields approved for self-read
- role set as trusted server-derived output (`roles`)
- server-managed audit metadata only if intentionally exposed (for example `createdAt`, `updatedAt`)

Rule: response includes only contract-approved fields; no direct entity serialization.

### Mutable fields (`UpdateUserProfileRequest`)

- allowlisted non-sensitive profile/contact attributes for self-service profile maintenance
  - example direction: `displayName`, `phoneNumber`, `avatarUrl` (if supported)

Rule: update applies only listed mutable fields as partial patch behavior.

### Protected / non-mutable fields (not writable via `/users/me`)

- `id` and any identity binding fields
- role/authority fields (`roles`, role-like variants)
- account status/privilege flags
- password hash or password/security-flow fields
- trusted origin/bootstrap/audit ownership fields
- any ownership linkage fields that could remap resource ownership

Rule: protected fields are server-controlled and not part of A1.1 self-service write surface.

## Authorization and anti-escalation behavior in this surface

- Caller must be authenticated to access both endpoints.
- Identity comes from principal, never caller-declared IDs.
- Role/privilege decisions come from trusted principal roles, never request payload role values.
- Self-service update must not permit privilege escalation or cross-user mutation.

## Validation and error handling status

Detailed validation rules (required/optional fields, format/length constraints) and precise error response contract are intentionally deferred to `fast-kql.2`.

For A1.1 contract planning, only baseline behavior is fixed:

- update surface is allowlisted
- protected fields are excluded from writable contract
- principal-derived identity is mandatory

## Out of scope for A1.1

- Controller/service/repository code
- Spring Security wiring details
- OpenAPI generation specifics
- full validation matrix and error body standardization (deferred to `fast-kql.2`)
