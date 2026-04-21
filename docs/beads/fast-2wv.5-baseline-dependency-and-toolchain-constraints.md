# fast-2wv.5 baseline dependency and toolchain constraints

## Scope

This note captures the **current baseline only** for reproducible local build and runtime behavior in the single-module backend.

## Toolchain baseline constraints

- Java baseline is `17` (`pom.xml` → `<java.version>17</java.version>`).
- Maven must be run via the wrapper scripts in-repo (`mvnw` / `mvnw.cmd`) to keep local versions consistent.
- Maven Wrapper is pinned to Apache Maven `3.9.12` (`.mvn/wrapper/maven-wrapper.properties`).
- Spring Boot baseline comes from parent `org.springframework.boot:spring-boot-starter-parent:4.0.3`.

## Baseline dependency constraints currently in repo

- Core runtime starters are currently:
  - `spring-boot-starter-webmvc`
  - `spring-boot-starter-data-jpa`
  - `spring-boot-starter-data-redis`
  - `spring-boot-starter-security`
  - `spring-boot-starter-validation`
- Runtime database driver baseline is PostgreSQL (`org.postgresql:postgresql`, runtime scope).
- JWT libraries are pinned at `io.jsonwebtoken` version `0.12.6` (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`).
- OpenAPI docs baseline is `org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.15`.
- Lombok is present as optional dependency + compiler annotation processor path.

## Test/build execution assumptions

- Unit/standard test execution uses Surefire `3.5.5` and includes `**/*Test.java`.
- Integration-test phase uses Failsafe `3.5.5` and includes `**/*IntegrationTest.java`.
- Existing bootstrap smoke test uses `@SpringBootTest` + `@ActiveProfiles("test")` (`FasteatApplicationTests`).

## Runtime and profile constraints for local reproducibility

- Default runtime profile is `local` unless overridden (`SPRING_PROFILES_DEFAULT`).
- Shared datasource keys are env-backed in `application.properties`:
  - `FASTEAT_DB_URL`, `FASTEAT_DB_USERNAME`, `FASTEAT_DB_PASSWORD`, `FASTEAT_DB_DRIVER`
- Local profile baseline (`application-local.properties`) expects PostgreSQL at:
  - `jdbc:postgresql://localhost:5432/fasteat`
  - default username/password fallback: `postgres` / `postgres`
- Test profile baseline (`application-test.properties`) expects in-memory H2 configured in PostgreSQL compatibility mode:
  - `jdbc:h2:mem:fasteat-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`
  - `spring.jpa.hibernate.ddl-auto=create-drop`
- Shared JWT settings are env-backed in baseline config; test profile provides separate test defaults.

## Related context

- F1.4 bootstrap smoke-check flow is related context for running baseline startup/tests, but this bead only records constraints and does not redefine that flow.
