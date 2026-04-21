# fast-3q7.2 — Structured Logging Baseline and Correlation IDs (P1.2)

This bead defines a **spec-first logging contract** for baseline local/dev operability.

It aligns with:

- P1.1 intent (operational baseline for runtime health/readiness workflows)
- F1.3/F1.2 logging property baseline in `application*.properties`
- S1.4 error response direction where `traceId` is available for diagnostics

This document sets contract expectations only. It does **not** require full observability stack rollout (log pipeline, distributed tracing backend, SIEM, etc.).

## Scope and intent

- Establish one correlation ID strategy used across request, response, logs, and error payloads.
- Standardize a minimal structured log field baseline for app events.
- Keep conventions small, implementation-friendly, and compatible with current single-module Spring Boot setup.
- Prioritize local/dev troubleshooting and predictable diagnostics.

## Correlation ID strategy (baseline)

### Canonical ID

- Canonical field name: `correlationId`
- Transport header: `X-Correlation-Id`
- Value format: opaque string; UUID format is recommended for generated values.

### Generation and propagation rules

For each incoming HTTP request:

1. If `X-Correlation-Id` is present and non-blank, accept it as the request correlation ID.
2. Otherwise, generate a new correlation ID at request entry.
3. Attach the chosen ID to logging context for the full request lifecycle.
4. Return the same ID in response header `X-Correlation-Id`.

Normalization/safety guidance (baseline):

- Trim leading/trailing whitespace.
- Ignore/replace values that are obviously malformed or excessively long.
- Never fail the request solely because the caller omitted a correlation header.

## Baseline structured log fields

All application logs that represent request lifecycle events or handled errors should include at least:

- `timestamp` — ISO-8601 timestamp
- `level` — log level (`DEBUG`, `INFO`, `WARN`, `ERROR`)
- `service` — application/service identifier (for example `fasteat`)
- `env` — active profile/environment (`local`, `test`, etc.)
- `logger` — logger/source name
- `message` — human-readable event message
- `correlationId` — canonical request correlation ID

For HTTP request/response events, include these additional fields when available:

- `method` — HTTP method
- `path` — request path
- `status` — HTTP response status
- `durationMs` — total request processing time

For authenticated/security-relevant events, include these additional fields when available and safe:

- `actorId` — principal/user ID (if authenticated)
- `outcome` — normalized result (`SUCCESS`, `DENIED`, `FAILED`)
- `errorCode` — stable machine-readable code for failures (aligned with S1.4 categories for auth/authz)

## Request/response/error correlation contract

### Request ingress

- Correlation ID is resolved/generated at the earliest practical request boundary.
- All logs produced while handling that request should carry the same `correlationId`.

### Successful response

- Response should include `X-Correlation-Id` with the resolved/generated value.
- Response body shape for successful endpoints is unchanged by this bead.

### Error response direction

To align with S1.4:

- Error response bodies may continue exposing `traceId`.
- Baseline direction: `traceId` value should equal the active `correlationId` for that request.
- If a future platform layer separates trace and correlation concepts, compatibility mapping must remain explicit.

Example error direction:

```json
{
  "error": "AUTH_INVALID_TOKEN",
  "message": "Authentication token is invalid",
  "status": 401,
  "path": "/users/me",
  "timestamp": "2026-01-01T12:00:00Z",
  "traceId": "9a5c8b2a-2e36-42f3-9bd2-a2e4c7fcefbf"
}
```

In this baseline contract, `traceId` above is the same logical identifier as `correlationId` in logs/headers.

## Logging property baseline alignment

Current config baseline remains authoritative:

- `src/main/resources/application.properties`
  - `logging.level.root=INFO`
  - `logging.level.com.gerolori.fasteat=INFO`
  - `logging.level.org.springframework=INFO`
- `src/main/resources/application-local.properties`
  - local dev verbosity overrides via `FASTEAT_APP_LOG_LEVEL` and SQL log level controls

This bead does not require changing those properties now; it defines the structured field and correlation behavior contract that later implementation should satisfy.

## Explicit non-goals (for P1.2)

- No mandatory migration to a specific JSON encoder or log collector.
- No distributed tracing backend integration.
- No metrics/alerting dashboard rollout.
- No production hardening policy beyond baseline safe behavior needed for local/dev operability.
