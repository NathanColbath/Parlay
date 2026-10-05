# ADR-0009: `engine-core` scene model reconciliation (canonical GameObject model)

- Status: Accepted (Lead Engineer ruling, STR-32)
- Date: 2026-10-05
- Owner: Lead Engineer
- Relates to: STR-32, STR-21, STR-5, STR-19
- Extends: ADR-0004 (module layout), ADR-0007 (behaviour binding), ADR-0008
  (editor module boundaries, which already assigns `io.stratixops.engine.scene`
  to `engine-core`)

## Context

Two scene models landed concurrently and both claimed the package
`io.stratixops.engine.scene` in `engine-core`, so merging them as-is is a
compile collision:

1. **STR-21 (committed on `feat/str-21-scene-serialize`, base `design/str-20-design-system`
   @ `4431683`).** The Unity-shaped `GameObject` / `Component` / `Behaviour`
   model plus built-ins (`Transform`, `SpriteRenderer`, `Animator`, `Camera`,
   `AudioSource`, `Collider2D`), inspection descriptors, the single
   `SceneRenderer` submission path (ADR-0007/ADR-0008), and the
   `engine-serialize` round-trip. `Scene` is a **final** class holding root
   `GameObject`s and driving `update`/`fixedUpdate`/`lateUpdate`/`dispose`.
2. **STR-5 (untracked on `feat/str-22-engine-imgui`, base `2717a6f` / STR-2).**
   The MVP-era `Entity` interface, an **abstract** `Scene` with
   `create(EngineContext)` / `render(Renderer, float)` hooks, and a
   `SceneManager` that performs deferred, one-at-a-time scene transitions.

The classes `Scene` collide by simple name, and `SceneManager`/`Entity` only
exist in one model. The shared checkout was also branch-switched mid-run by
another agent, which is what let the two models diverge without either side
noticing.

## Decision

### 1. The STR-21 GameObject model is canonical

`io.stratixops.engine.scene` in `engine-core` is owned by the STR-21 model:
`GameObject`, `Component`, `Behaviour`, the P0 built-ins, `Guid`/refs,
`Scene` (final, root-object list + lifecycle), and `SceneRenderer` (the single
submission path, ADR-0008 §2). This is consistent with ADR-0008, which already
places the scene model in `engine-core` and puts drawing through a core-side
`SceneRenderer` over the frozen `engine-api` `Renderer`.

### 2. The MVP `Entity` / abstract `Scene` / `SceneManager` are retired

`Entity`, the abstract `Scene` lifecycle, and `SceneManager` are **not** part of
the canonical `engine-core`. They are deleted rather than adapted:

- The abstract `Scene` is replaced outright by the final GameObject `Scene`.
- `Entity` has no counterpart and no consumer outside `engine-core`'s own
  tests; gameplay composition is now `GameObject` + `Component`/`Behaviour`.
- `SceneManager` (deferred transitions) is **deferred, not deleted by ADR**:
  the MVP has no scene-switching requirement, and ADR-0008 §5 gives Play-mode
  scene handling to `engine-editor`. If transitions are needed, they return as
  an additive `SceneManager` over the canonical `Scene` under a new ADR; they
  must not reintroduce a second `Scene` type.

### 3. `GameLoop` is retained

`io.stratixops.engine.runtime.GameLoop` (ADR-0003 fixed timestep) is
model-agnostic: it drives a `Step` callback and knows nothing about `Scene`.
It is retained unchanged and is the runtime's timing owner.

### 4. Branch base and merge order

- The STR-19 ADRs (`4431683`: ADR-0005–0008, `02-editor-architecture.md`) land
  **before** STR-21; STR-21 builds on them.
- The STR-5 core runtime (loop, `EngineContext`, input/audio semantics, math,
  and the SPI-backed fakes) is **rebased onto the ADR base** and merged with
  the STR-21 model on the single integration branch
  `feat/str-32-scene-reconcile`. Scene/lifecycle therefore have a single owner
  (`engine-core`), and `engine-serialize` sits above it.

### 5. Workspace isolation

The shared checkout must not be branch-switched by more than one agent at a
time. Agents work in **per-agent `git worktree`s** (or serialized writes to the
shared tree). `settings.gradle.kts` auto-discovers modules, so worktrees need no
extra wiring; `git worktree list` is the source of truth for who owns which
branch.

## Consequences

- One `io.stratixops.engine.scene` model; no split package, no compile
  collision. `gradlew check` is green on `feat/str-32-scene-reconcile` with the
  STR-21 model and the STR-5 runtime together (engine-core, engine-serialize,
  engine-render, engine-imgui, engine-desktop, samples, qa-smoke).
- Runtime tests that used the MVP `Scene` are re-expressed against the
  canonical model (for example `AssetFailureTest` now builds a `Scene` +
  `GameObject` + `SpriteRenderer` and renders through `SceneRenderer`).
- `engine-core` stays headless-testable and free of GL/JSON/ImGui, per
  ADR-0008.
- The editor track inherits a single scene model; the Play/Scene views share
  `SceneRenderer` and cannot drift.

## Alternatives considered

- **Keep the MVP `Entity`/`SceneManager` and adapt the GameObject model onto
  it.** Rejected: it discards the committed STR-21 editor-facing model
  (inspection, serialization, built-ins) and contradicts ADR-0008.
- **Rename one `Scene` (for example MVP `Scene` -> `AbstractScene`).** Rejected:
  leaves two competing scene/lifecycle models in one package and defers the
  real conflict.
- **Move the GameObject model to `engine-serialize` or `engine-editor`.**
  Rejected: inverts the dependency direction and forces runtime users through
  JSON/UI (ADR-0008 alternatives).
- **Keep `SceneManager` now.** Rejected as scope creep: no MVP requirement, and
  it couples scene switching to core before the editor's Play mode is built.
