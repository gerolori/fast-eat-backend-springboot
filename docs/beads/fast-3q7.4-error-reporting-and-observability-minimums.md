# fast-3q7.4 — Error Reporting and Observability Minimums (P1.4)

This bead defines minimum observability expectations for API error handling.

References:

- `docs/beads/fast-2lk.4-security-error-handling-contract.md`
- `docs/beads/fast-3q7.2-structured-logging-and-correlation-ids.md`

## Minimum error-reporting contract

All API error responses should include:

- `error`
- `message`
- `status`
- `path`
- `timestamp`
- `traceId` (recommended baseline; should map to request `correlationId`)

## Logging minimums for error events

At minimum, emitted error logs should include:

- `level=ERROR`
- `correlationId`
- `path`
- `status`
- `errorCode` (aligned with response `error` category)
- `message`

Optional when safe and available:

- `actorId`
- `method`
- `durationMs`

## Correlation behavior

- Accept inbound `X-Correlation-Id` when present and safe.
- Otherwise generate one.
- Return it in response header `X-Correlation-Id`.
- Keep error `traceId` equal to active request `correlationId` in current baseline.

## Minimum troubleshooting workflow

1. Client captures `traceId` from error response.
2. Operator/developer searches logs by `correlationId` = `traceId`.
3. Use matched events for timeline and failure diagnosis.

## Non-goals

- No mandatory tracing backend, metrics stack, or SIEM rollout.
- No production runbook/alerting implementation details.
- No change to current single-module architecture.
