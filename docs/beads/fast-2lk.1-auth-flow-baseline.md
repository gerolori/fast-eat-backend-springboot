# fast-2lk.1 — Authentication Flow Baseline (S1.1)

This bead defines the **backend authentication baseline direction** for S1.1.

## Baseline flow (credential login)

1. Client sends credentials to an auth endpoint under `/auth/...` (baseline endpoint shape).
2. Backend validates credentials against stored user identity data.
3. User password verification uses **BCrypt hash comparison** (default implementation direction).
4. On successful authentication, backend returns a **JWT access token**.
5. For protected APIs, client sends `Authorization: Bearer <token>`.
6. Backend resolves token principal and uses it as request identity.

## Baseline endpoint direction

- Auth endpoints live under: **`/auth/...`**
- Protected identity read endpoint target: **`/users/me`**

## Principal identity baseline

Token/auth context should carry enough principal identity to support:

- current-user resolution for `/users/me`
- future role-based checks (to be defined later)
- order ownership checks (user-scoped access)

Minimum expected identity payload direction: stable user identifier (and compatible room for role/scoping claims later).

## Explicit deferrals (out of scope for S1.1)

- **Authorization model** (roles/permissions policy) → deferred to **S1.2**
- **Token claims details, lifecycle, expiration/rotation, refresh semantics** → deferred to **S1.3**
- **Security/auth error contract** (codes/messages/structure) → deferred to **S1.4**

Refresh-token behavior is **intentionally out of scope for S1.1**.
