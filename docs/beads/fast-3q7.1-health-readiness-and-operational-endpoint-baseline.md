# fast-3q7.1 — Health/readiness and operational endpoint baseline (P1.1)

## Purpose

Define a **spec-first operational contract** for health/readiness behavior in the current single-module Spring Boot backend.

This baseline is intentionally documentation-first: it sets semantics, visibility rules, and profile/context expectations so later platform work (Actuator wiring, logging, metrics, alerts, runbooks) can implement against a stable contract.

## Scope boundary

### In scope

- Contract-level meaning of **health** vs **readiness**.
- Minimal endpoint direction for operational checks in this repository.
- Profile/context exposure policy for local, test, and deployed environments.
- Constraints that keep the baseline aligned with existing foundation/profile decisions (`fast-2wv.1` to `fast-2wv.5`).

### Out of scope (deferred)

- Full Spring Actuator implementation details.
- Security filter-chain/runtime authorization changes.
- Detailed liveness probes, custom health indicators, and dependency-specific checks.
- Production alert thresholds, dashboard implementation, and incident automation.

## Baseline intent: health vs readiness

### Health

Health answers: **"Is this process alive and able to respond at a basic level?"**

Baseline expectation:

- Should be cheap, stable, and independent from optional external dependencies where possible.
- Intended for uptime/process-level observation and coarse availability checks.

### Readiness

Readiness answers: **"Can this instance safely receive normal traffic now?"**

Baseline expectation:

- May reflect critical dependency availability (for example primary datastore connectivity) when those dependencies are required for normal request serving.
- Intended for deployment orchestration and traffic-gating decisions.

### Contract rule

- **Not ready** does not automatically imply process crash.
- Health and readiness remain separate signals even if later implementations share underlying checks.

## Minimal operational endpoint direction (single-module baseline)

The backend should converge on a minimal operational surface with distinct semantics:

- a health-oriented signal endpoint (process/aliveness intent)
- a readiness-oriented signal endpoint (traffic-acceptance intent)

Implementation mechanism is intentionally open for now (e.g., Actuator, thin controller bridge, or equivalent), but the semantic split above is mandatory.

For this baseline phase, avoid introducing broad operational endpoint catalogs; only define the minimal health/readiness contract needed by platform follow-up tasks.

## Exposure policy by context

Aligned with current profile baseline (`local` default, explicit `test`, and deploy-specific runtime configuration):

### Local development (`local` profile default)

- Expose minimal health/readiness signals suitable for local debugging and startup verification.
- Keep payloads minimal and non-sensitive (status + concise reason where needed).
- Do not expose secrets, credentials, token material, stack traces, or full dependency configuration.

### Test context (`test` profile)

- Preserve deterministic behavior for CI/tests; readiness semantics must not introduce flaky dependence on developer-local infrastructure.
- Prefer hermetic checks consistent with the test-profile contract from `fast-2wv.2`/`fast-2wv.3`.
- Test-facing signal payloads remain minimal and stable for assertions.

### Deployed environments (staging/production-like)

- Expose only the minimal operational signals required by orchestrators/monitors.
- Keep detailed diagnostic internals withheld by default; route deeper diagnostics through controlled observability channels (structured logs, metrics, tracing, secured admin tooling).
- Treat readiness as authoritative for routing decisions; do not use broad public diagnostics as a substitute for internal telemetry.

## Security and information disclosure baseline

- Operational endpoints are **status interfaces**, not debugging/data-dump interfaces.
- Response contract should prioritize:
  - low-cardinality status values,
  - predictable machine-parsable structure,
  - no sensitive configuration leakage.
- Any future detailed health component breakdown must be explicitly access-controlled and reviewed against security posture.

## Contract fit with existing foundation docs

This baseline is designed to remain compatible with current foundations:

- Single-module-first architecture remains unchanged (`fast-2wv.1`).
- Profile split and env-backed configuration strategy remain authoritative (`fast-2wv.2`, `fast-2wv.3`).
- No dependency/toolchain expansion is required by this doc-only baseline (`fast-2wv.5`).

## Follow-up guidance for platform/observability work

Subsequent P1 tasks can implement this contract incrementally by:

1. wiring explicit health/readiness endpoints with the semantic split preserved,
2. adding structured logging around readiness transitions,
3. introducing metrics/alerts tied to readiness degradation and recovery,
4. documenting runbook behavior for "healthy but not ready" states.

These follow-ups should keep operational signal contracts stable so consuming infra and dashboards do not churn.
