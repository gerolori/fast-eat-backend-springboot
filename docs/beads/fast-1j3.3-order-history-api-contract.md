# fast-1j3.3 — Order History API Contract (O1.3)

This bead defines retrieval of a caller's order history.

## Endpoint

- `GET /orders/me`
- Auth required
- Result set is implicitly owner-scoped from principal identity

## Query parameters

All optional unless stated:

- `status`: one of the canonical order states
- `from`: ISO-8601 timestamp (inclusive lower bound on creation time)
- `to`: ISO-8601 timestamp (inclusive upper bound on creation time)
- `page`: zero-based page index, default `0`
- `size`: page size, default `20`

## Response example

```json
{
  "items": [
    {
      "orderId": "77f31f22-4d1f-4f8f-9158-2cce1eb8ca7d",
      "status": "COMPLETED",
      "total": { "amount": "25.00", "currency": "USD" },
      "createdAt": "2026-04-19T18:30:00Z",
      "completedAt": "2026-04-19T19:10:00Z"
    },
    {
      "orderId": "11a2f0fd-2580-4d5f-95eb-fb3f3a24595f",
      "status": "CANCELLED",
      "total": { "amount": "12.50", "currency": "USD" },
      "createdAt": "2026-04-20T11:10:00Z",
      "cancelledAt": "2026-04-20T11:30:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalItems": 2,
  "totalPages": 1,
  "hasNext": false,
  "hasPrevious": false
}
```

## Contract rules

- Non-admin callers never use this route to query other users' orders.
- `status` filter uses exact enum values from `fast-2p4.6`.
- History is immutable from read perspective; this endpoint does not mutate state.

## Error categories

- `401` for unauthenticated access
- `422` for invalid query format/range (for example malformed `from` timestamp)

This is a spec document only and does not claim runtime implementation exists yet.
