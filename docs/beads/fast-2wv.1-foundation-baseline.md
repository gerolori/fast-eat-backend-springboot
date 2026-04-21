# fast-2wv.1 foundation baseline

## Current repo baseline

- The backend is currently a **single-module Spring Boot** Maven project.
- This bead confirms the baseline as-is; no module split is part of this change.

## Canonical Java package root

- Package root: `com.gerolori.fasteat`

## Startup entrypoint

- Main class: `src/main/java/com/gerolori/fasteat/FasteatApplication.java`
- App bootstrap uses `SpringApplication.run(FasteatApplication.class, args)`.

## Baseline config and smoke test files

- Build/config root: `pom.xml`
- Runtime properties baseline: `src/main/resources/application.properties`
- Smoke test path: `src/test/java/com/gerolori/fasteat/FasteatApplicationTests.java` (`contextLoads`)

## Near-term package layout conventions (single module)

Keep code under `src/main/java/com/gerolori/fasteat/...` and organize by concern:

- `config` for framework and infrastructure configuration classes
- `domain` for core business model, repositories, and domain services
- `security` for authentication/authorization configuration and security components

These are package-level boundaries only for now; do not split Maven/Gradle modules at this stage.

## Architecture scope statement

Multi-module architecture is **future-state only**. The current and near-term baseline remains single-module.
