# fast-kql.4 — Profile API Contract Examples (A1.4)

This bead provides concrete request/response examples for the `/users/me` contract defined in:

- `docs/beads/fast-kql.1-user-profile-api-surface-and-endpoint-set.md`
- `docs/beads/fast-kql.2-profile-validation-and-error-semantics.md`
- `docs/beads/fast-kql.3-profile-authorization-and-ownership-checks.md`
- `docs/beads/fast-2lk.4-security-error-handling-contract.md`

## Endpoint examples

### 1) Get current profile

`GET /users/me`

```json
{
  "id": "a6d9f4fd-97b5-4b91-bf4e-3f7a7c1ad3be",
  "email": "customer@example.com",
  "displayName": "Giulia Rossi",
  "phoneNumber": "+39-333-1234567",
  "roles": ["CUSTOMER"],
  "createdAt": "2026-04-01T09:00:00Z",
  "updatedAt": "2026-04-20T10:15:00Z"
}
```

### 2) Partial update for mutable profile fields

`PATCH /users/me`

Request:

```json
{
  "displayName": "Giulia R.",
  "phoneNumber": "+39-333-0000000"
}
```

Response (`200`):

```json
{
  "id": "a6d9f4fd-97b5-4b91-bf4e-3f7a7c1ad3be",
  "email": "customer@example.com",
  "displayName": "Giulia R.",
  "phoneNumber": "+39-333-0000000",
  "roles": ["CUSTOMER"],
  "createdAt": "2026-04-01T09:00:00Z",
  "updatedAt": "2026-04-20T11:00:00Z"
}
```

## Rejection examples

### Restricted-field write attempt

`PATCH /users/me` with `role` or `id` in payload is rejected.

```json
{
  "error": "PROFILE_RESTRICTED_FIELD",
  "message": "Field 'roles' is not writable in profile updates",
  "status": 422,
  "path": "/users/me",
  "timestamp": "2026-04-20T11:01:00Z",
  "traceId": "cb2e33e2-9bc2-47ca-9ff5-bf1903f1f0db",
  "details": [
    { "field": "roles", "reason": "restricted field" }
  ]
}
```

### Authentication failure

```json
{
  "error": "AUTH_MISSING_TOKEN",
  "message": "Bearer token is required",
  "status": 401,
  "path": "/users/me",
  "timestamp": "2026-04-20T11:02:00Z",
  "traceId": "cb2e33e2-9bc2-47ca-9ff5-bf1903f1f0db"
}
```

## Compatibility notes

- `/users/me` remains principal-scoped for both `CUSTOMER` and `ADMIN`; it never accepts caller-selected `userId`.
- `traceId` follows the correlation direction from `fast-3q7.2` (same logical request identifier as `correlationId`).
- This bead is contract-only and does not imply that profile controllers are already implemented in this repository.
