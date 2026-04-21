# fast-1j3.5 — Order Error Semantics and Idempotency (O1.5)

This bead standardizes order API error categories and idempotency expectations.

## Error category baseline for order routes

Security categories follow S1.4 unchanged:

- `AUTH_MISSING_TOKEN`, `AUTH_INVALID_TOKEN`, `AUTH_TOKEN_EXPIRED`, `AUTH_MALFORMED_TOKEN` -> `401`
- `AUTHZ_INSUFFICIENT_ROLE`, `AUTHZ_OWNERSHIP_DENIED` -> `403`

Order-domain categories (stable names):

- `ORDER_NOT_FOUND`
- `ORDER_INVALID_TRANSITION`
- `ORDER_MENU_UNAVAILABLE`
- `ORDER_INVALID_ITEM_QUANTITY`
- `ORDER_DUPLICATE_REQUEST`

## Envelope

Order errors use the same envelope shape as security errors:

- `error`, `message`, `status`, `path`, `timestamp`, optional `traceId`, optional `details`

`traceId` maps to request `correlationId` direction from `fast-3q7.2`.

## Idempotency for order creation

For `POST /orders`, clients may send `Idempotency-Key` header.

Expected behavior:

1. Same authenticated caller + same idempotency key + semantically same request body
   -> return same logical order result (no duplicate active order creation).
2. Same caller + same key + materially different body
   -> reject with `ORDER_DUPLICATE_REQUEST` (typically `409`).
3. Missing key
   -> request is processed normally (no guaranteed deduplication contract).

## Idempotency scope and retention (contract direction)

- Scope key uniqueness at least by authenticated principal.
- Retention window is implementation-defined and should be documented when implemented.
- Idempotency is targeted at create operations, not generic GET endpoints.

## Example duplicate response

```json
{
  "error": "ORDER_DUPLICATE_REQUEST",
  "message": "This request has already been processed",
  "status": 409,
  "path": "/orders",
  "timestamp": "2026-04-20T13:20:00Z",
  "traceId": "0f98a184-2458-49d1-bd80-bf5e0c8caa02"
}
```

This bead remains contract-only for current single-module backend.
