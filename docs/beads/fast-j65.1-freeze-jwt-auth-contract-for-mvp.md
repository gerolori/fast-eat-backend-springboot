# fast-j65.1 — Freeze JWT Auth Contract for MVP (I0.1)

This bead freezes the MVP JWT/auth contract direction and is authoritative for implementation planning.

Cross-reference baseline/security beads:

- `docs/beads/fast-2lk.1-auth-flow-baseline.md`
- `docs/beads/fast-2lk.2-authorization-model.md`
- `docs/beads/fast-2lk.3-jwt-claim-and-token-lifecycle-contract.md`
- `docs/beads/fast-2lk.4-security-error-handling-contract.md`

## Frozen MVP auth surface

- `POST /auth/login`
- `POST /auth/register`
- `POST /auth/refresh`
- `POST /auth/logout`

All protected APIs continue using `Authorization: Bearer <accessToken>`.

## Frozen token strategy

- **Two-token model:** short-lived access token + longer-lived refresh token.
- Access token is used for API authorization.
- Refresh token is used only at refresh/logout boundaries.
- Logout semantics are refresh-token/session invalidation semantics for MVP.

## Frozen JWT claim and principal mapping baseline

Required access-token claims for MVP:

- `sub`: stable user identifier
- `roles`: effective role set (contains `CUSTOMER` and/or `ADMIN`)
- `iat`, `exp`: issued-at and expiry

Optional claim direction (implementation-controlled):

- `sid` may be emitted as internal session metadata but is not required by client contracts.

Principal mapping rules:

- `principal.userId` must be derived from validated `sub`.
- Effective authorization roles must be derived from validated token claims (not request payloads/headers).
- Caller-supplied user IDs must never override principal identity.

## Login/register/refresh/logout contract decisions

- `login` accepts credential payload and returns auth tokens plus principal summary.
- `register` creates a new end-user identity with default `CUSTOMER` role semantics.
- `refresh` rotates/reissues access token using valid refresh token context.
- `logout` invalidates refresh context so future refresh attempts fail.

Exact field-level DTO naming remains implementation-owned, but these behaviors are fixed.

## Security compatibility policy (auth-specific)

- JWT bearer auth is canonical for MVP.
- Legacy sid-only auth compatibility is **not** a required runtime behavior.
- If temporary sid adapters exist during migration, they are non-canonical and may be removed without contract break once JWT clients are aligned.

## Superseded planning state

This bead supersedes earlier deferred/auth-placeholder planning by making token lifecycle and logout semantics mandatory MVP contract direction (instead of deferring them).

## Non-goals

- No Spring Security implementation details.
- No persistence/schema details for token/session storage.
- No cryptographic algorithm/key-management rollout plan.
