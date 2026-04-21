# fast-31f.1 — React Native Profile Contract Mapping (X3.1)

This bead maps the **currently implemented backend contract** for profile flows to React Native client expectations.

Primary backend sources:

- `src/main/java/com/gerolori/fasteat/web/users/UserProfileController.java`
- `src/main/java/com/gerolori/fasteat/web/users/UserProfileService.java`
- `src/main/java/com/gerolori/fasteat/web/users/dto/UserProfileResponse.java`
- `src/main/java/com/gerolori/fasteat/web/error/GlobalApiExceptionHandler.java`

## Implemented profile endpoints (current)

| Endpoint | Auth | Notes for RN client |
|---|---|---|
| `GET /users/me` | Required (`Bearer`) | Principal-scoped profile read. No caller-provided `userId`. |
| `PATCH /users/me` | Required (`Bearer`) | Partial patch with explicit allow-list field validation. |
| `PATCH /users/me/subscription` | Required (`Bearer`) | Updates `subscription.enabled` only (`{ "enabled": boolean }`). |

## Response shape to model in React Native

`UserProfileResponse` currently includes:

- `id` (UUID)
- `email` (string)
- `displayName` (string)
- `phoneNumber` (string)
- `paymentProfile` (nullable object)
  - `provider`
  - `methodReference`
  - `last4` (nullable)
- `subscription` (object)
  - `enabled` (boolean)
- `roles` (set of enum values)
- `createdAt` (ISO instant)
- `updatedAt` (ISO instant)

RN integration note: older examples that omit `paymentProfile` and `subscription` are incomplete versus runtime DTOs.

## Patch semantics (implemented)

Allowed fields in `PATCH /users/me` payload:

- `displayName`
- `phoneNumber`
- `paymentProvider`
- `paymentMethodReference`
- `paymentLast4`

Important validation behavior for mobile UX:

- Empty object -> `422 PROFILE_EMPTY_UPDATE`
- Restricted fields (e.g., `roles`, `id`, `subscription`) -> `422 PROFILE_RESTRICTED_FIELD`
- Unknown field -> `422 PROFILE_UNKNOWN_FIELD`
- Invalid value format/length -> `422 PROFILE_INVALID_VALUE`
- Non-object payload -> `400 PROFILE_INVALID_VALUE`

Payment profile pair rule:

- Any payment profile update requires both `paymentProvider` and `paymentMethodReference` to be present after merge.
- `paymentLast4` is optional but must be exactly 4 digits when provided.

## Error envelope alignment for RN

Profile and security errors use shared API envelope:

```json
{
  "error": "PROFILE_INVALID_VALUE",
  "message": "phoneNumber format is invalid",
  "status": 422,
  "path": "/users/me",
  "timestamp": "2026-04-21T09:40:00Z",
  "traceId": "2f8a0b43-7f20-4103-82a4-7d419c63e1c0",
  "details": [
    { "field": "phoneNumber", "reason": "invalid format" }
  ]
}
```

RN should surface `error` + `message` to users and attach `traceId` in support logs.
