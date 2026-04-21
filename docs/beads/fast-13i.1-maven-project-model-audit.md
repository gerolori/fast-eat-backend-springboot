# fast-13i.1 Maven/project-model unresolved-symbol audit

Findings from local Maven model inspection:

- The build declared nonstandard Spring Boot starter coordinates (`spring-boot-starter-webmvc` and several `spring-boot-starter-*-test` artifacts), which can break IDE import/indexing in environments that do not resolve those aliases the same way.
- Jackson/JJWT class resolution was fragile because runtime-only JJWT parts were declared without explicit runtime scope, and there was no direct Jackson databind declaration in the model.
- Surefire/Failsafe include syntax used scalar values instead of `<include>` entries, which is easy for IDE/build-tool integrations to misread.

Action taken in this lane:

- Normalize starter/test dependency coordinates to canonical artifacts.
- Keep JJWT API on compile classpath and mark `jjwt-impl`/`jjwt-jackson` as runtime.
- Add explicit `jackson-databind` dependency and baseline quality plugins in Maven verify flow.
