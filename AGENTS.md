# AGENTS.md

## Repo baseline (for low-context agents)

- Repository: `fast-eat-backend-springboot`
- Current integration branch: `develop`
- Build system: Maven (`pom.xml`)
- Runtime: Spring Boot backend
- Current architecture rule: **single-module-first** (do not split modules yet)
- Java package root: `com.gerolori.fasteat`
- Main app class: `src/main/java/com/gerolori/fasteat/FasteatApplication.java`

### Current top-level files/directories (expected)

- Source: `src/`
- Build output: `target/`
- Build tools: `mvnw`, `mvnw.cmd`, `.mvn/`
- Docs: `README.md`, `DEVELOPMENT.md`, `AGENTS.md`
- Tracking: `.beads/` (tracked issue graph), `.gitignore`

## Working rules

1. Start on `develop` unless explicitly told otherwise.
2. Keep changes scoped to the assigned bead/task.
3. Keep implementation inside current single-module layout.
4. Do not rely on online lookup if local repo/docs/tooling are sufficient.
5. Verification defaults after changes:
   - docs/beads only: `git status --short --branch`, `br list --no-db --no-auto-flush`, `br count --no-db --no-auto-flush --by-type`
   - code touched: add narrowest relevant build/test command.

## Package/branch conventions

- Production code: `src/main/java/com/gerolori/fasteat/...`
- Tests: `src/test/java/com/gerolori/fasteat/...`
- Keep package boundaries explicit now to enable later module extraction with minimal churn.

## Beads conventions (repo-local)

- CLI: `br.exe`
- Required on this Windows setup for mutation commands:
  - `--no-db --no-auto-flush`
- Source of truth: `.beads/issues.jsonl`
- Local `beads.db*` artifacts are incidental cache/runtime files; JSONL remains authoritative.

## Label taxonomy

### Workstream labels

- `track:planning`
- `track:foundation`
- `track:domain`
- `track:security`
- `track:api`
- `track:orders`
- `track:platform`
- `track:testing`
- `track:late-split`

### Execution/state labels

- `ready`
- `blocked`
- `parallel`
- `docs`
- `external`

### Cross-repo impact labels (external tracking)

- `impact:architecture`, `repo:architecture`
- `impact:kotlin`, `repo:kotlin-android-client`
- `impact:react-native`, `repo:react-native-client`

## Dependency conventions

- Use `blocks` edges only for strict prerequisites.
- Parent/child hierarchy models scope, while `blocks` models ordering.
- If a task can run concurrently after prerequisites, add label `parallel`.
- External tracking epics must include `external` + impact/repo labels and an `external-ref` value.

## Detailed workstream mapping in Beads

Epics: `P0`, `F1`, `D1`, `S1`, `A1`, `A2`, `O1`, `P1`, `T1`, `L1`, `X1`, `X2`, `X3`

- `P0` (track:planning): audit Beads state and align planning docs
- `F1` (track:foundation): single-module backend baseline
- `D1` (track:domain): domain contract and persistence model
- `S1` (track:security): security and authentication baseline
- `A1` (track:api): user profile API
- `A2` (track:api): menu API
- `O1` (track:orders): order workflow API
- `P1` (track:platform): platform, devex, and observability
- `T1` (track:testing): testing and quality gate
- `L1` (track:late-split): late-stage module split planning
- `X1` (track:planning + external): architecture repo tracking
- `X2` (track:foundation + external): Kotlin Android client tracking
- `X3` (track:api + external): React Native client tracking

Required detailed task fan-out:

- `P0.1..P0.4`
- `F1.1..F1.5`
- `D1.1..D1.8`
- `S1.1..S1.6`
- `A1.1..A1.4`
- `A2.1..A2.4`
- `O1.1..O1.6`
- `P1.1..P1.6`
- `T1.1..T1.5`
- `L1.1..L1.4`
- `X1.1..X1.4`
- `X2.1..X2.4`
- `X3.1..X3.4`
