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

## Future Module Split Policy

Module extraction is a **future-state decision** tracked under late-split planning work.

Until that work is explicitly scheduled:

- do not introduce multi-module build structure,
- do not document module-specific build commands,
- do not treat target split names as current implementation.

If/when split work starts, update this document in the same lane so guidance remains truthful.
