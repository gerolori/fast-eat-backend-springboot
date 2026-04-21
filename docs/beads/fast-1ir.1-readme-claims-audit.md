# fast-1ir.1 — Audit README claims against current and target publish state

## Scope

- Audited artifact: `README.md` (current `develop` baseline in this lane worktree)
- Audit goal: classify README claims as:
  - **true now**
  - **false now but planned**
  - **false/stale and should be removed later**
- This is an audit only (no README rewrite in this task).

## Evidence baseline used

- Repository shape and build/runtime files at lane start:
  - root has `src/`, `pom.xml`, `mvnw*`, `.github/workflows/ci-baseline.yml`
  - root does **not** have `docker-compose.yml`, `Dockerfile`, `Makefile`, `fast-eat-*` module directories
- Dependency and runtime evidence from:
  - `pom.xml`
  - `src/main/resources/application*.properties`
  - `src/main/java/**` mappings/security classes
  - `.beads/issues.jsonl` for planned target-state work (not yet implemented)

## Target publish-state interpretation for this audit

Based on open Beads dependencies referenced from `fast-1ir.*`:

- `fast-1ir.2` will align architecture/build/runtime sections
- `fast-1ir.3` depends on Docker lane (`fast-2xm.2/.4/.5`) and will align Docker sections
- `fast-1ir.4` depends on CI/quality lane (`fast-43x.1/.2/.3/.4`) and will align testing/quality sections

So claims tied to those open lanes are classified as **false now but planned**.

---

## A) True now

1. **Java 17 baseline** (`README.md` stack/prereq sections)
   - Evidence: `pom.xml` -> `<java.version>17</java.version>`
2. **PostgreSQL runtime dependency exists**
   - Evidence: `pom.xml` -> `org.postgresql:postgresql` runtime dependency
3. **Spring Security + JWT authentication are implemented**
   - Evidence: `spring-boot-starter-security` in `pom.xml`; security/JWT classes under `src/main/java/com/gerolori/fasteat/security/`
4. **SpringDoc/OpenAPI dependency is present**
   - Evidence: `org.springdoc:springdoc-openapi-starter-webmvc-ui` in `pom.xml`
5. **Integration tests use Spring application context**
   - Evidence: multiple `@SpringBootTest` classes under `src/test/java/**`
6. **Architecture links to companion repos exist**
   - Evidence: README links are present and internally consistent as references (no local contradiction)

## B) False now but planned

1. **Docker quick start and Docker setup sections are currently actionable**
   - README claims Docker-first flow (`docker-compose up`, Dockerfile/compose snippets, Make targets)
   - Current state: no `Dockerfile`, no `docker-compose.yml`, no `Makefile`
   - Planned evidence: `fast-2xm.2/.4/.5` + `fast-1ir.3` are open and explicitly target this alignment
2. **Testing/quality section implying verify/coverage/quality-gate publish readiness**
   - Current state: CI is baseline `./mvnw.cmd test` only (`.github/workflows/ci-baseline.yml`)
   - Planned evidence: `fast-43x.1/.2/.3/.4` + `fast-1ir.4` are open and explicitly target this alignment
3. **README architecture/build/runtime sections matching current publish profile**
   - Current state: major mismatches (documented below as stale details)
   - Planned evidence: `fast-1ir.2` is open specifically to align these sections

## C) False/stale and should be removed later

1. **Multi-module Maven architecture claim** (`README.md` project structure and stack)
   - Stated modules (`fast-eat-api`, `fast-eat-service`, etc.) do not exist in repo root
   - `pom.xml` has no `<modules>` section; current baseline is single-module
2. **Spring Boot version claim `3.2+`**
   - Current parent version is `spring-boot-starter-parent` `4.0.3`
3. **JWT library claim `Auth0 java-jwt`**
   - Current JWT library is `io.jsonwebtoken` (`jjwt-*`), not Auth0 java-jwt
4. **Base API URL claim `/api/v1`**
   - Controllers map directly to `/auth`, `/users`, `/menus`, `/orders`, etc.
   - No `/api/v1` context-path configured in application properties
5. **Swagger URL claim `/api/docs`**
   - Security route matcher exposes `/swagger-ui/**`, `/swagger-ui.html`, `/v3/api-docs/**`
   - No evidence of `/api/docs` mapping in config/code
6. **MailDev access point claim**
   - No MailDev service/config files are present in the repo baseline
7. **Custom authorization annotations `@Customer` / `@Admin` implemented**
   - No such annotation usages/definitions found in main source
8. **Event-driven processing section (`ApplicationEventPublisher`, `@EventListener`, `@Async`) implemented**
   - No matching usages found in main source
9. **TestContainers listed as active test stack**
   - No TestContainers dependency/usages found; tests use Spring Boot context and H2 test profile
10. **Environment variable names shown as canonical (`JWT_SECRET`, `JWT_EXPIRATION`, etc.)**
    - Current config keys are `fasteat.security.jwt.*` with env wrappers `FASTEAT_*` in `application*.properties`
11. **Database migration guidance implies existing Flyway/Liquibase path**
    - No Flyway/Liquibase dependency or config present in current baseline

---

## Audit outcome summary

- README is materially ahead/aside of actual repository publish state in architecture, Docker, and quality sections.
- Some mismatches are expected to become true only after open platform/testing lanes complete.
- Several claims are stale relative to current single-module + Spring Boot 4 baseline and should be removed/replaced during follow-up README alignment tasks.
