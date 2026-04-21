# fast-2lk.4 — Security Error Handling Contract (S1.4)

This bead defines the **security error-policy contract** for authentication and authorization failures.

It is aligned with:

- S1.1 auth baseline (`docs/beads/fast-2lk.1-auth-flow-baseline.md`)
- S1.2 authorization model (`docs/beads/fast-2lk.2-authorization-model.md`)
- planned S1.3 JWT lifecycle details (expiration/refresh/claim specifics)

This document is contract-level guidance only. It does **not** introduce concrete exception class hierarchies.

## Scope and intent

- Standardize HTTP status semantics for security failures.
- Define consistent response-shape direction for clients.
- Set expectations for token-related and authorization-related error categories.
- Keep behavior usable across upcoming user/profile, menu, and order APIs.

## Core status semantics

### 401 Unauthorized

Use when the request cannot be treated as authenticated.

Typical reasons:

- missing bearer token
- malformed `Authorization` header or malformed token format
- invalid token signature/claims
- expired token

Meaning: caller identity is absent or not trustworthy for protected access.

### 403 Forbidden

Use when authentication exists, but caller is not allowed to perform the action.

Typical reasons:

- authenticated user lacks required role (for example, non-admin on admin endpoint)
- authenticated user fails ownership check (`resource.ownerUserId != principal.userId`)

Meaning: caller is known, but policy denies requested operation.

## Decision table (baseline)

| Case | Status | Category direction | Notes |
| --- | --- | --- | --- |
| Missing token on protected endpoint | `401` | `AUTH_MISSING_TOKEN` | Include challenge info and indicate bearer token required. |
| Invalid token (signature/issuer/audience mismatch) | `401` | `AUTH_INVALID_TOKEN` | Treat as unauthenticated; do not expose sensitive validation internals. |
| Expired token | `401` | `AUTH_TOKEN_EXPIRED` | Distinguish from generic invalid token for predictable client refresh/re-login behavior. |
| Malformed token/header format | `401` | `AUTH_MALFORMED_TOKEN` | Covers malformed bearer header or token parsing failure. |
| Insufficient role | `403` | `AUTHZ_INSUFFICIENT_ROLE` | Authentication succeeded but role policy denies endpoint. |
| Ownership denied | `403` | `AUTHZ_OWNERSHIP_DENIED` | Authentication succeeded but user is not owner of target resource. |

## Response shape direction (contract-level)

Security errors should use one consistent JSON envelope across controllers and security filters.

Baseline response-body direction:

- `error`: stable machine-readable category/code (from table above)
- `message`: safe, human-readable summary
- `status`: HTTP status number
- `path`: request path
- `timestamp`: server-generated timestamp (ISO-8601 preferred)
- `traceId` (optional but recommended): correlation id for logs/diagnostics

Example direction:

```json
{
  "error": "AUTH_TOKEN_EXPIRED",
  "message": "Authentication token is expired",
  "status": 401,
  "path": "/orders/me",
  "timestamp": "2026-01-01T12:00:00Z",
  "traceId": "6c4b8f2f8d..."
}
```

## Header and messaging expectations

- For `401` responses on protected resources, include `WWW-Authenticate: Bearer` (with safe error metadata when appropriate).
- Keep messages safe-by-default: informative for client handling, but without leaking secret/token internals.
- `403` responses should not imply token invalidity; they should communicate policy denial.

## Alignment expectations for upcoming implementation tasks

- Security config/filter layer should map token parse/validation/authentication failures to the `401` categories above.
- Controller/service authorization guards (role + ownership) should map denials to `403` categories above.
- `/users/me` and order ownership paths must rely on principal-derived identity from S1.1/S1.2, not caller-supplied identity fields.
- Public endpoints (for example `/auth/...` and public menu reads from S1.2) should not emit auth errors unless explicitly configured as protected.

## Explicit deferrals

- Exact JWT claim names and refresh-token mechanics remain in S1.3.
- Concrete exception class tree and framework-specific handler wiring are implementation details for later tasks.
- Final error field naming can be adjusted during implementation **only if** status semantics and category intent are preserved.
