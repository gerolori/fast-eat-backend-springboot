# fast-j65.4 — Freeze Shared DTO, Pagination, and Error-Envelope Rules (I0.4)

This bead freezes shared transport-contract rules used across auth/profile/restaurant/menu/order APIs for MVP.

Cross-reference:

- `docs/beads/fast-kql.2-profile-validation-and-error-semantics.md`
- `docs/beads/fast-rer.1-menu-listing-api-surface.md`
- `docs/beads/fast-2lk.4-security-error-handling-contract.md`
- `docs/beads/fast-3q7.2-structured-logging-and-correlation-ids.md`

## Shared DTO rules (MVP)

- Request DTOs are input-only and must not accept server-managed fields (IDs, roles, audit metadata, ownership bindings).
- Response DTOs are contract-owned projections (no direct entity serialization commitment).
- Canonical identifier fields remain explicit (`userId`, `restaurantId`, `menuId`, `orderId`) and must not be silently aliased.

## Pagination contract freeze

Collection endpoints that paginate must use:

- `page` (integer, zero-based)
- `size` (integer)
- response fields: `items`, `page`, `size`, `totalItems`, `totalPages`, `hasNext`, `hasPrevious`

Default direction (unless endpoint-specific override is documented):

- `page=0`
- `size=20`

## Error-envelope freeze

Canonical error envelope for MVP:

```json
{
  "error": "STRING_CODE",
  "message": "human-readable summary",
  "status": 400,
  "path": "/request/path",
  "timestamp": "2026-04-20T12:00:00Z",
  "traceId": "uuid-or-equivalent"
}
```

Rules:

- `timestamp` is ISO-8601 UTC (`Z`) format.
- `traceId` is required for operational correlation.
- `correlationId` may be accepted/propagated at platform level, but transport error payload uses `traceId` as canonical field name.

## Validation response behavior

- Validation failures must use the shared error envelope and stable error codes.
- Endpoint-specific validation detail may be included additively, but must not remove the canonical envelope fields.

## Superseded planning assumptions

This bead supersedes earlier partial/per-endpoint-only error and pagination assumptions by making these rules common contract requirements across MVP APIs.

## Non-goals

- No final catalog of all endpoint-specific validation codes.
- No serialization library/framework implementation details.
- No runtime logging pipeline specification beyond trace/correlation alignment.
