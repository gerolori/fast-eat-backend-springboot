# fast-1j3.4 — Order State Transition Business Rules (O1.4)

This bead translates D1.6 state invariants into API-facing business rules.

Reference: `docs/beads/fast-2p4.6-order-state-model-and-transition-invariants.md`

## Canonical transition graph

Allowed transitions:

- `PENDING -> CONFIRMED`
- `PENDING -> CANCELLED`
- `CONFIRMED -> PREPARING`
- `CONFIRMED -> CANCELLED`
- `PREPARING -> READY_FOR_PICKUP`
- `PREPARING -> CANCELLED` (exception path)
- `READY_FOR_PICKUP -> COMPLETED`

Everything else is invalid.

## Actor constraints

- Customer: may request cancellation only, only while order is in cancellable active state.
- Admin: may progress operational states and cancel under governance policy.
- Neither actor may perform backward, skip-ahead, or terminal-escape transitions.

## API-level failure direction

Invalid transition attempts are rejected (not coerced), for example:

- `409 Conflict` with `ORDER_INVALID_TRANSITION`
- or `422 Unprocessable Entity` with same category

Stable category is more important than exact status choice; implementation may standardize one status.

Error shape follows S1.4 baseline (`error`, `message`, `status`, `path`, `timestamp`, optional `traceId`).

## Terminal-state immutability

- Once `COMPLETED` or `CANCELLED`, no further status updates are permitted.
- Cross-terminal switches are forbidden (`COMPLETED -> CANCELLED`, `CANCELLED -> COMPLETED`).

## Example invalid transition response

```json
{
  "error": "ORDER_INVALID_TRANSITION",
  "message": "Cannot transition from PREPARING to CONFIRMED",
  "status": 409,
  "path": "/orders/77f31f22-4d1f-4f8f-9158-2cce1eb8ca7d",
  "timestamp": "2026-04-20T13:10:00Z",
  "traceId": "a0af8ee7-5f54-45ff-a634-fc68d7d90ff0"
}
```

This bead is policy/spec only.
