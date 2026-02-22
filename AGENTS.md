# AGENTS.md

Agent operating guide for the Welltale repository.
Use this as the default execution and coding policy unless a task explicitly says otherwise.

## Project Snapshot
- Language: Java 25
- Build tool: Gradle (Kotlin DSL)
- Modding platform: Hytale Modding API (`hytale-mod` plugin)
- Packaging: JAR via `jar` / `build`
- Entry point: `fr.welltale.Welltale`
- Main architecture: ECS systems + repositories + JSON file loaders + event handlers

## Rule Files Check (Cursor/Copilot)
- `.cursor/rules/`: not present
- `.cursorrules`: not present
- `.github/copilot-instructions.md`: not present
- `CLAUDE.md`: present and should be treated as project-specific guidance
- If Cursor/Copilot rule files are added later, treat them as higher-priority local agent rules.

## Environment and Wrapper
- On Windows, use `gradlew.bat ...`
- On macOS/Linux, use `./gradlew ...`
- In this repository, examples below use Windows-friendly commands.

## Build / Verify Commands

### Core build commands
- `gradlew.bat clean` — delete build outputs
- `gradlew.bat assemble` — compile/package without full verification flow
- `gradlew.bat build` — full build including tests/checks
- `gradlew.bat clean build` — clean rebuild
- `gradlew.bat jar` — build plugin JAR
- `gradlew.bat sourcesJar` — build source JAR

### Verification commands
- `gradlew.bat check` — run all configured verification tasks
- `gradlew.bat test` — run tests
- `gradlew.bat testClasses` — compile test sources only

### Running the mod/server
- `gradlew.bat runServer` — run Hytale server with mod
- `gradlew.bat decompileServer` — decompile Hytale server for dev lookup
- `gradlew.bat syncAssets` — sync runtime-edited assets back to source
- Note: `runServer` is wired to auto-run `syncAssets` after shutdown.

### Discovery/help
- `gradlew.bat tasks --all` — list tasks
- `gradlew.bat dependencies` — inspect dependency graph
- `gradlew.bat javaToolchains` — verify Java toolchain resolution

## Running a Single Test (Important)
Gradle supports targeted test execution via `--tests`.

- Single test class:
  - `gradlew.bat test --tests "fr.welltale.level.XPTableTest"`
- Single test method:
  - `gradlew.bat test --tests "fr.welltale.level.XPTableTest.getLevelForXP_returnsExpectedLevel"`
- Pattern match:
  - `gradlew.bat test --tests "*XPTableTest"`
  - `gradlew.bat test --tests "fr.welltale.level.*"`

Notes:
- You can pass multiple `--tests` filters in one command.
- This repo currently has no committed test sources; targeted commands still apply once tests are added.

## Lint / Formatting Status
- No dedicated lint plugin is currently configured (no Checkstyle/PMD/Spotless/ErrorProne config found).
- Formatting is convention-based; keep changes consistent with surrounding code.
- Minimum verification baseline before finishing a task: `gradlew.bat build` (or at least `test` + compile).

## Source Layout
- Java code: `src/main/java/fr/welltale/...`
- Resources/UI/assets: `src/main/resources/...`
- Runtime JSON data (generated/read at runtime): `./mods/welltale/*.json`

## Architecture Conventions
- Keep systems modular by feature (`characteristic`, `level`, `clazz`, `mob`, `player`, `spell`, `rank`).
- Prefer established layering:
  - Data model
  - Repository interface + JSON-backed implementation
  - File loader (create defaults if missing)
  - ECS/Event systems + handlers
- Register systems/events/components in plugin setup (`Welltale.setup()`).

## Java Style Guidelines

### Formatting
- Use 4-space indentation (no tabs).
- Keep braces on same line for class/method/control declarations.
- Use concise guard clauses for invalid/null conditions.
- Avoid unrelated reformatting in touched files.

### Imports
- Prefer explicit imports; avoid new wildcard imports.
- Keep import groups tidy (JDK, third-party, project-local), separated when useful.
- Remove unused imports.

### Naming
- Packages: lowercase (existing root `fr.welltale`).
- Classes/interfaces/enums: PascalCase.
- Methods/fields/local vars: camelCase.
- Constants: `UPPER_SNAKE_CASE` (`static final`).
- ECS processor classes: suffix with `System`.
- Event handlers/events: suffix with `Handler` / `Event`.
- Repository contracts/impls: `XRepository`, `JsonXRepository`, `JsonXFileLoader`.

### Types and nullability
- Prefer explicit types at API boundaries and public methods.
- `var` is acceptable for obvious local inference; avoid when it hides intent.
- Keep nullability annotations consistent with file-local convention:
  - Existing code uses both `javax.annotation` and `org.jspecify` in different files.
  - Do not mix annotation ecosystems arbitrarily within a single new API surface.
- Use `@Nullable` for possibly absent returns instead of sentinel objects.

### Collections and concurrency
- Use standard collections interfaces in signatures (`List`, `Map`) unless concrete type is required.
- For shared mutable state across scheduler/system ticks, use thread-safe structures (e.g., `ConcurrentHashMap`) where needed.

### Error handling
- Fail fast on invalid inputs (guard + return/throw).
- In repository/domain operations, follow existing contract patterns (checked exceptions in interfaces where already established).
- In event/system code, avoid throwing when a safe early return is sufficient.
- Log operational failures with context (`logger.atSevere().log("[AREA] message")`).
- Do not swallow exceptions silently.

### Logging
- Use `HytaleLogger` (`atInfo`, `atSevere`, etc.) consistently.
- Include enough context (system/feature and actor IDs when relevant).
- Keep player-facing chat messages concise and actionable.

### Comments and docs
- Keep comments minimal and high-value.
- Prefer self-explanatory names over verbose comments.
- Add comments for non-obvious formulas or gameplay rules only.

## Data and Serialization Guidelines
- JSON-backed models should remain Jackson-compatible.
- Preserve no-args constructors where deserialization requires them.
- When adding model fields:
  - consider default values/migration behavior for existing JSON files,
  - update file loaders/examples if needed,
  - verify repository read/write still works.

## Hytale/ECS-Specific Practices
- Validate entity/component refs before use (`isValid`, null checks).
- Keep damage/stat math deterministic and centralized in relevant systems.
- Clamp bounded gameplay values (e.g., resistances/cooldowns) to safe ranges.
- Register component types once and set static component type holders appropriately.

## Agent Change Discipline
- Keep edits scoped to the requested task.
- Prefer incremental, reviewable changes over broad rewrites.
- Do not change Gradle/plugin versions unless required by the task.
- If you add tests, keep them deterministic and fast.
- Before handoff, run the narrowest relevant verification command, then broader checks when practical.

## Suggested Pre-Handoff Checklist
- Code compiles (`gradlew.bat compileJava` or `build`).
- Tests relevant to change pass (`gradlew.bat test --tests ...` when tests exist).
- No accidental file/path changes in generated asset locations.
- New APIs/classes follow naming and nullability conventions above.
- Logging and error paths are explicit and non-silent.
