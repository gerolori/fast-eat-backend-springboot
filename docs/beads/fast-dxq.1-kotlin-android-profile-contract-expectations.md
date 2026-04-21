# fast-dxq.1 — Kotlin Android Profile Contract Expectations (X2.1)

This bead maps the **current backend implementation** on `develop` (base SHA `82128f81ed5185ff29c43cf32778d318b2f18c80`) to Kotlin Android expectations for profile flows.

References:

- `src/main/java/com/gerolori/fasteat/web/users/UserProfileController.java`
- `src/main/java/com/gerolori/fasteat/web/users/UserProfileService.java`
- `src/main/java/com/gerolori/fasteat/web/users/dto/UserProfileResponse.java`
- `src/main/java/com/gerolori/fasteat/web/error/GlobalApiExceptionHandler.java`
- `docs/beads/fast-kql.4-profile-api-contract-examples.md`

## Endpoint surface used by Kotlin

- `GET /users/me` (auth required)
- `PATCH /users/me` (auth required, partial update)
- `PATCH /users/me/subscription` (auth required)

## Response model expectations

`UserProfileResponse` currently returns:

- `id: UUID`
- `email: String`
- `displayName: String`
- `phoneNumber: String`
- `paymentProfile: { paymentProvider, paymentMethodReference, paymentLast4 } | null`
- `subscription: { enabled: Boolean }`
- `roles: Set<RoleName>`
- `createdAt: Instant`
- `updatedAt: Instant`

Kotlin model guidance:

- Keep `paymentProfile` nullable.
- Keep `subscription.enabled` non-null Boolean.
- Parse `roles` as a set/list of enums with unknown-safe fallback.

## PATCH `/users/me` write rules

Allowed fields:

- `displayName`
- `phoneNumber`
- `paymentProvider`
- `paymentMethodReference`
- `paymentLast4`

Rejected patterns:

- Empty object -> `422 PROFILE_EMPTY_UPDATE`
- Restricted fields (`id`, `roles`, `password`, `subscription*`, audit fields, etc.) -> `422 PROFILE_RESTRICTED_FIELD`
- Unknown field names -> `422 PROFILE_UNKNOWN_FIELD`
- Invalid value shapes/content -> `400/422 PROFILE_INVALID_VALUE` depending on case

Payment-profile pair rule:

- Any update touching payment profile requires both `paymentProvider` and `paymentMethodReference` to be present after merge; otherwise `422 PROFILE_INVALID_VALUE`.

## Error envelope Kotlin should standardize on

For profile/security failures, backend emits:

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

Auth errors for these endpoints are `401` with `AUTH_MISSING_TOKEN` or `AUTH_INVALID_TOKEN`.

## Compatibility notes

- `/users/me` remains principal-scoped; Kotlin should never send user-selected IDs for profile access.
- `PATCH /users/me/subscription` is the only subscription write route; do not infer subscription writes from generic profile patch.
- `traceId` should be propagated to Android logs/crash reports for backend support handoff.
