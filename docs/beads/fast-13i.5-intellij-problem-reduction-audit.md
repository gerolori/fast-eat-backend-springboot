# fast-13i.5 IntelliJ problem-reduction residual audit

Local verification after formatting and test-setup cleanup:

- `mvn verify` passes with Spotless, Checkstyle, SpotBugs, tests, and coverage gates all green.
- Deprecated/noisy test setup using `MockitoAnnotations.openMocks(this)` was replaced by `@ExtendWith(MockitoExtension.class)` in affected controller tests.
- Timestamp audit test no longer relies on `Thread.sleep(...)`.

Residual non-failing log noise observed during integration tests:

- Spring Security generated development password banner.
- SpringDoc warning about `/v3/api-docs` and `/swagger-ui.html` defaults.
- Spring Data Redis repository assignment info logs (JPA repositories being skipped by Redis scanning).
- Mockito inline mock-maker self-attach/JDK dynamic-agent warning.

These residual items do not fail Maven quality gates and were not changed here because they require runtime/test-plugin policy decisions outside this lane.
