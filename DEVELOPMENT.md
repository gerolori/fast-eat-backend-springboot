# Development Guide - Spring Boot Backend

This guide documents the **current** backend development reality in this repository.

## Current State (Truthful Baseline)

- Build tool: Maven wrapper (`mvnw.cmd` on Windows)
- Runtime: single Spring Boot application module
- Package root: `com.gerolori.fasteat`
- Main class: `src/main/java/com/gerolori/fasteat/FasteatApplication.java`
- Tracking: Beads JSONL in `.beads/issues.jsonl`

The project is intentionally **single-module-first**. Keep implementation inside this layout unless a dedicated late-split task explicitly says otherwise.

## Repository Layout

```text
fast-eat-backend-springboot/
├── src/
├── .beads/
├── AGENTS.md
├── DEVELOPMENT.md
├── README.md
├── pom.xml
├── mvnw
└── mvnw.cmd
```

## Package Conventions (Single Module)

Keep code in `src/main/java/com/gerolori/fasteat/...` with package-level boundaries such as:

- `web` for HTTP controllers and API DTOs
- `domain` for entities and repositories
- `security` for authentication/authorization components
- `platform` and `config` for operational/framework concerns

These are package boundaries only, not separate Maven modules.

## Local Commands (Windows)

From repository root:

- Run app: `mvnw.cmd spring-boot:run`
- Unit tests: `mvnw.cmd test`
- Full verification: `mvnw.cmd verify`
- Clean verification: `mvnw.cmd clean verify`

Use Maven wrapper commands in docs and automation to avoid environment drift.

## Workflow Expectations

1. Start from `develop` (or a task branch/worktree created from it).
2. Keep each change scoped to the assigned Beads task.
3. For docs-only updates, run docs-appropriate verification (for example: `git status --short --branch`).
4. For code changes, run the narrowest relevant Maven command before handoff.

## Lint/Quality Gate Policy (Current + Ratchet Path)

Current enforcement is through `mvnw.cmd verify` in Maven lifecycle:

- Spotless (`spotless:check`) for formatting discipline.
- Checkstyle (`maven-checkstyle-plugin:check`) for style rules.
- SpotBugs (`spotbugs:check`) for high-threshold static analysis.
- JaCoCo coverage check (line coverage minimum configured in `pom.xml`).

Policy for this lane:

- Today: keep `verify` green for code-impacting work; docs-only work can use docs/beads checks.
- Immediate ratchet rule: new or touched files must satisfy existing gates without weakening plugin settings.
- Future ratchet steps (stricter rules, threshold increases, suppression burn-down) happen in dedicated follow-up tasks, each keeping mainline green.
- Avoid broad cleanup-only churn in feature lanes; do cleanup in focused lint-hardening tasks.

This keeps quality checks enforceable now while allowing controlled tightening later.

## Redis Upgrade Path (Deferred by Policy)

Redis is intentionally deferred in the current baseline:

- Primary runtime path stays PostgreSQL-first.
- `docker-compose.yml` does not currently run a Redis service.
- No feature should require Redis availability in current local/CI default flows.

If Redis is introduced later, do it as a scoped follow-up lane:

1. Add runtime surface deliberately (Compose service, env contract, and profile documentation).
2. Add explicit Spring configuration for the first approved use case (for example cache or session concerns).
3. Add verification coverage for the Redis-backed behavior while preserving a safe fallback path.
4. Update docs/beads artifacts in the same lane so default expectations remain truthful.

## Local Pre-Push Quality Check Guidance

Before pushing a branch with code/config changes, run the same baseline quality gate used in CI:

- `mvnw.cmd -B verify`

For container-related changes (for example Dockerfile or runtime profile wiring), also run a local container build validation:

- `docker build --file Dockerfile --tag fast-eat-backend:local-check .`

If either check fails, fix locally before pushing to avoid CI failures.

## Required Checks and Branch Protection Policy

For protected integration branches (for example `develop`/`main`), require pull requests and require the CI Baseline workflow checks to pass:

- `verify`
- `container-build-validation`

Keep the required-check list aligned with `.github/workflows/ci-baseline.yml` job names.

## Future Module Split Policy

Module extraction is a **future-state decision** tracked under late-split planning work.

Until that work is explicitly scheduled:

- do not introduce multi-module build structure,
- do not document module-specific build commands,
- do not treat target split names as current implementation.

If/when split work starts, update this document in the same lane so guidance remains truthful.
