# fast-2lk.3 — JWT Claim and Token Lifecycle Contract (S1.3)

This bead defines the scoped JWT contract needed to continue protected API work after:

- `docs/beads/fast-2lk.1-auth-flow-baseline.md` (auth flow baseline)
- `docs/beads/fast-2lk.2-authorization-model.md` (role/ownership policy)

It is a **contract/policy document only**. It does not implement Spring Security or JWT library code.

## Scope and intent

- Define the minimum JWT access-token claim set required for principal resolution and authorization checks.
- Define access-token lifecycle expectations for MVP.
- Define refresh-token expectations and current status explicitly.
- Define validation responsibilities and trust boundaries between clients and backend.

## Inputs from current config placeholders

Current placeholders in runtime config:

- `fasteat.security.jwt.secret`
- `fasteat.security.jwt.expiration-ms`
- `fasteat.security.jwt.issuer`

This contract uses those placeholders as the source of truth for signing secret, access-token TTL, and issuer identity.

## Access-token claim contract (minimum)

The backend access token must include at least the following claims:

| Claim | Required | Meaning | Notes |
|---|---|---|---|
| `sub` | Yes | Stable authenticated user identifier | Primary source for `principal.userId` used by `/users/me` and user/order ownership checks. |
| `roles` | Yes | Effective role set for caller | Array/list containing `CUSTOMER` and/or `ADMIN` per S1.2; used for endpoint authorization. |
| `iss` | Yes | Token issuer identity | Must match configured `fasteat.security.jwt.issuer`. |
| `iat` | Yes | Issued-at timestamp | Used for temporal validation/audit context. |
| `exp` | Yes | Expiration timestamp | Must be derived from `fasteat.security.jwt.expiration-ms`. |

### Optional claims (not required for A1.1/A2.1 unblock)

- `nbf` (not-before), if strict activation windows are needed.
- `jti` (token identifier), if revocation/blacklist mechanics are introduced later.
- `aud` (audience), if multiple audiences are introduced later.

Clients must not rely on optional claims until explicitly promoted to required in a later bead.

## Principal resolution contract

Resolved authentication context must map consistently:

- `principal.userId` ← `sub`
- `principal.roles` / authorities ← `roles`
- authenticated state ← successful signature + issuer + temporal validation

Controllers/services performing authorization and ownership checks must read identity from resolved principal/context, not directly from request payload/header fields.

## Access-token lifecycle contract (MVP)

1. Access token is issued on successful credential authentication under `/auth/...`.
2. Client sends token as `Authorization: Bearer <token>` for protected endpoints.
3. Token is valid until `exp`; TTL is controlled by `fasteat.security.jwt.expiration-ms`.
4. Expired access tokens are rejected (unauthenticated result for protected routes).
5. No sliding expiration for access tokens in MVP (token lifetime does not auto-extend on use).

### Environment defaults currently implied

- Local/dev default access-token TTL placeholder: `3600000` ms (1 hour).
- Test profile default access-token TTL placeholder: `60000` ms (60 seconds).

These values are implementation inputs and may be tuned by environment variables without changing claim semantics.

## Refresh-token expectations and current status

Refresh-token support is **not part of the current required implementation baseline** for S1.3.

- This bead does **not** require issuing refresh tokens yet.
- This bead does **not** require a `/auth/refresh` endpoint yet.
- For current protected API unblock (A1.1/A2.1), re-authentication after access-token expiry is an acceptable baseline behavior.

When refresh support is implemented later, it must be defined by a dedicated contract covering storage/rotation/revocation behavior and endpoint semantics.

## Validation responsibilities and trust boundaries

### 1) Backend security boundary

Before treating a request as authenticated, backend validation must ensure:

- token signature is valid using trusted server-side secret material
- `iss` matches configured issuer
- required claims (`sub`, `roles`, `iat`, `exp`) are present
- temporal checks pass (`exp` not elapsed; `nbf`/`iat` handling if present)

If any required check fails, request must not be treated as authenticated.

### 2) Client trust boundary

- Client-provided JWT content is untrusted until backend validation succeeds.
- Caller-supplied user identifiers in body/path/query must not override principal identity from validated token.
- Caller-supplied role values in payload/header/query must not influence authorization decisions.

### 3) Authorization trust boundary

- Route protection and ownership checks consume only validated principal context.
- Authorization decisions must align with S1.2 (`CUSTOMER`/`ADMIN`, ownership and anti-escalation rules).

## Out of scope / deferrals

- Spring Security filter/provider wiring and concrete JWT library code
- Concrete refresh-token persistence model, rotation, and revocation mechanics
- Detailed security error response contract (deferred to S1.4)

## Unblock statement for downstream beads

This contract is the minimum JWT-policy baseline needed so A1.1 and A2.1 protected API work can:

- resolve current user from `sub`
- enforce role checks from `roles`
- rely on issuer + temporal claim validation behavior

without waiting for full refresh-token implementation.
