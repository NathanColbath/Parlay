# Editor Architecture & Phase 1 Task Breakdown

- Status: Accepted for Phase 1
- Owner: Lead Engineer (STR-19)
- Date: 2026-10-05
- Source: STR-18 `spec`; decisions in ADR-0005 (backend), ADR-0006
  (serialization), ADR-0007 (Behaviour/Inspector), ADR-0008 (module boundaries).
- Supersedes: the "no visual editor" posture of `docs/design/04-tooling-editor-ux.md`
  for the additive editor track. The MVP engine spec (`00-engine-mvp-spec.md`)
  remains in force for the runtime.

This document is the single source of truth for the Phase 1 editor: the frozen
module/interface contracts the coders build against, Play-mode semantics, and the
task breakdown. If this document and a compiled contract disagree, the compiled
contract wins and this document is fixed.

---

## 1. Phase 1 definition of done

A developer can open the OOB project in the editor and, using only the GUI:

1. Create/edit a `GameObject` with `Transform`, `SpriteRenderer`, `Camera`,
   `AudioSource`, and a user `Behaviour`; set its fields in the Inspector.
2. Save the scene as versioned JSON and reload it with identical state.
3. Press Play to run the scene in the Game view, Pause/Step, and exit Play with
   the pre-play editor state restored exactly.
4. Navigate Hierarchy/Inspector/Scene/Game/Project/Console with the OOB docking
   layout and keyboard shortcuts.

Editor-wide acceptance criteria are E1-E10 in the `spec` §13; Phase 1 targets
E1-E6, E8-E10 and holds E7 (60 FPS) as a performance gate.

---

## 2. Runtime + editor at a glance

```
                        +-------------------------------------------+
                        |              engine-editor                |
                        |  Docking shell · panels · gizmos ·        |
                        |  Play control · undo/redo · commands      |
                        +-------------------------------------------+
                          |            |            |
             engine-imgui |   engine-  |  engine-   | engine-core
             (theme, kit) |   serialize|  render    | (Scene, Behaviour,
                          |            | (Render-   |  SceneRenderer,
                          v            v  Target)   v  inspect)
                        +-------------------------------------------+
                        |     engine-api (frozen public + SPI)      |
                        +-------------------------------------------+
```

Frame flow (single window, single GL context, main thread):

```
glfwPollEvents
  -> engine: fixed-step update of the edit-time scene (editor only)
  -> engine-imgui: newFrame()
  -> engine-editor: build panel UI from editor state
  -> engine-editor Scene view: RenderTarget.begin(); SceneRenderer.render(scene, rt);
       RenderTarget.end()  -> present rt.textureId() as ImGui::Image in the Scene panel
  -> engine-editor Game view (Play only): same path with the runtime camera
  -> engine-imgui: endFrame() -> ImGui render pass
  -> swap
```

---

## 3. Frozen interface contracts (Phase 1)

These are the seams coders code against. Types are indicative of intent; exact
signatures live in the compiled sources and JavaDoc.

### 3.1 `engine-core` — scene model (`io.stratixops.engine.scene`)

- `Scene` — owns `List<GameObject>`, name, background color, and an
  `AssetDatabase` handle; `create/update/render/dispose` remains compatible with
  the existing `GameLifecycle`.
- `GameObject` — `Guid guid()`, `String name()`, `boolean active()`,
  `int layer()`, `Set<String> tags()`, ordered `List<Component> components()`,
  `List<GameObject> children()`, `GameObject parent()`.
- `Component` — `boolean enabled()`, `GameObject gameObject()`, lifecycle hooks
  `awake/onEnable/start/update/lateUpdate/fixedUpdate/onDisable/onDestroy`.
- Built-ins (P0): `Transform` (mandatory, unique; local + derived world),
  `SpriteRenderer`, `Animator`, `Camera`, `AudioSource`, `Collider2D` (query
  only), `Behaviour`.
- `Behaviour` — user base class (ADR-0007).
- `SceneRenderer` — the **single submission path**: `render(Scene, Renderer)`.
  Uses only `engine-api`'s `Renderer`; unit-testable with a fake `Renderer`.

### 3.2 `engine-core` — inspection (`io.stratixops.engine.inspect`)

- Annotations: `@SerializeField`, `@NonSerialized`, `@Header`, `@Range`,
  `@Tooltip`, `@Order`, `@Color`.
- `ComponentDescriptor`, `FieldDescriptor`, `PropertyKind`, `ComponentRegistry`
  (stable `typeId <-> Class`; built once per class, cached; ADR-0007).
- Duplicate `typeId` and missing no-arg ctor are fail-fast registration errors.

### 3.3 `engine-serialize` (`io.stratixops.engine.serialize`)

- `SceneIO`: `Scene load(Path)` / `void save(Scene, Path)`.
- `PrefabIO`: `Prefab load(Path)` / `void save(Prefab, Path)`.
- `SettingsIO`: typed loaders for `ProjectSettings/*.json`.
- `AssetDatabase`: resolve `Guid -> Asset`, import assets, manage `.meta`
  sidecars and the importer registry.
- `Guid`, `MetaFile`, `Migration` (versioned envelope handling per ADR-0006).
- Round-trip and unknown-type-preservation are contractual.

### 3.4 `engine-render` — editor-facing additions

- `RenderTarget`: `begin()`, `end()`, `int textureId()`, `int width()`,
  `int height()`; drawing into it goes through the `Renderer` contract.
- Shared window/context handle consumed by `engine-imgui`.
- `engine-render` gains **no** dependency on `engine-core`.

### 3.5 `engine-imgui` (`io.stratixops.engine.imgui`)

- `ImGuiLayer`: `init(...)`, `newFrame()`, `endFrame()`, `render()`, `dispose()`.
- `Theme`: maps design tokens to ImGui style + custom draw; dark + light.
- Widgets: tree row, property row, searchable create menu, asset grid/breadcrumb,
  command palette, toasts, context menus, drag-and-drop affordances; font atlas
  (UI + mono + icon set, DPI-aware).
- Headless-safe: rendering can be exercised against a mock context for tests.

### 3.6 `engine-editor` (`io.stratixops.engine.editor`)

- Docking shell with the OOB layout; `Window` menu; savable layouts.
- Panels: Hierarchy, Inspector, Scene, Game, Project, Console.
- `PropertyDrawer` registry (descriptor -> widget); object/prefab pickers.
- Command stack (undo/redo) covering all edits; selection/duplicate/copy/paste.
- Play controller: Play/Pause/Step + state snapshot/restore.

---

## 4. Play-mode semantics

- Entering Play: serialize the open scene state to an in-memory snapshot; then
  instantiate a runtime scene. Edits made during Play are visually flagged and
  reverted on exit (Unity behavior). Exit Play restores the snapshot **exactly**
  (E5).
- Play runs the same `SceneRenderer` and component lifecycle as the standalone
  runtime, against the editor's GL context; the Game view shows the runtime
  camera, the Scene view keeps the editor camera.
- Pause stops updates but keeps rendering; Step advances exactly one fixed
  tick (1/60 s per ADR-0003).
- Scripts are discovered at editor start (no hot reload in Phase 1).

---

## 5. Phase 1 task breakdown

The seeded children from STR-18 are the inputs. Below is the refined breakdown
with owners, dependencies, and testable acceptance criteria. **[NEW]** marks a
task created by this architecture; the rest are refinements of existing issues.

Legend: **Core** = Core Engine Coder · **Render** = Rendering & Graphics Coder ·
**UX** = UX & Product Designer · **DX** = DX & Build Engineer ·
**QA** = QA Engineer · **DevOps** = DevOps Engineer.

### STR-19 (this issue) — ADRs & module boundaries — Lead
Deliverables: ADR-0005/0006/0007/0008, this document, refined children.
DoD: ADRs accepted; boundaries frozen; STR-21 and STR-22 unblocked. **Gate.**

### STR-20 — Editor design system spec — UX (parallel, already in progress)
Deliverables: `design-system` tokens (dark+light), widget-kit spec, panel UX,
accessibility. DoD: the ImGui-kit coder can implement without inventing visual
decisions; tokens map 1:1 to theme code.
Depends on: nothing (parallel with STR-19).

### STR-21 — `engine-core` + `engine-serialize`: scene model + versioned JSON — Core
Refined deliverables:
- Model + inspection: `GameObject`, `Component`, `Transform`, built-ins,
  `Behaviour`, `inspect` descriptors + `ComponentRegistry` (ADR-0007).
- `SceneRenderer` single submission path (ADR-0008 §3.1) with a fake-`Renderer`
  unit test.
- `engine-serialize`: versioned JSON envelopes, stable `typeId`, GUID refs,
  unknown-type `UnsupportedComponent` preservation, `.meta`/GUID + importer
  registry (ADR-0006).
- Round-trip tests: model -> JSON -> model equality; rename/move GUID stability;
  version migration; unknown-type lossless round-trip.
DoD: a scene with all P0 components round-trips save -> load with identical
state; rename/move preserves identity; module coverage >= 0.60.
Depends on: STR-19.

### [NEW] STR-29 — `engine-render`: offscreen `RenderTarget` + editor GL seam — Render
Deliverables: `RenderTarget` (begin/end/`textureId`/size) implementing the
`Renderer` contract; shared window/context handle exposed to `engine-imgui`;
nearest sampling and the 640x360 logical viewport preserved; `gpu`-tagged tests
plus headless math tests.
DoD: an offscreen render of a scene is presentable to ImGui with no distortion;
`engine-render` has no `engine-core` dependency.
Depends on: STR-19. Blocks: STR-23 (Scene/Game views).

### STR-22 — `engine-imgui`: ImGui integration, theme, widget kit — Render
Refined deliverables: `imgui-java` (LWJGL3 + GLFW + OpenGL3, docking) pinned;
frame lifecycle in the shared context; theme from STR-20 tokens (dark + light);
widget kit; DPI-aware font atlas; headless-safe render path.
DoD: a demo window renders every widget at 1x/1.5x/2x with no missing glyphs and
no default grey theme; `./gradlew check` green.
Depends on: STR-19 (module/backend ADRs) and STR-20 (tokens).

### STR-23 — `engine-editor`: docking shell, panels, Play mode, undo/redo — Render
Refined deliverables: docking shell + OOB layout; Hierarchy/Inspector/Scene/
Game/Project/Console; Scene view camera + gizmos + snapping + marquee; Play/
Pause/Step with exact state restore; Inspector auto-draw via `PropertyDrawer`
registry (ADR-0007); undo/redo command stack; Console.
DoD: create/edit/save/reload/play a GameObject with all P0 components from the
GUI; exit-Play restores state; keyboard-reachable.
Depends on: STR-21, STR-22, STR-29.

### STR-24 — OOB template opens and runs + editor packaging — DX
Depends on: STR-23. (Unchanged scope; refine after the shell lands.)

### STR-25 — Editor headless test harness + quality gate — QA
Depends on: STR-23 (harness scaffolding may start earlier). Extend the coverage
gate to `engine-core`, `engine-serialize`, `engine-imgui`, `engine-editor`;
map E1-E10 to automated or documented manual checks.

### STR-26 — Editor CI + release artifact — DevOps
Depends on: STR-23/STR-24. CI green on Linux + Windows; tagged release publishes
the editor distribution; CODEOWNERS per module; ImGui native cache config.

### STR-27 — `engine-desktop` runtime bootstrap — Core (independent)
Already specified; unaffected by the editor track (ADR-0004).

### Critical path

```
STR-19 (ADRs, gate)
  ├─ STR-21 (scene + serialize) ─┐
  ├─ STR-22 (imgui kit) ─────────┼─ STR-23 (editor shell) ─ STR-24/25/26
  ├─ STR-29 (render target) ─────┘
  └─ STR-20 (design system) ──> STR-22
STR-27 (runtime bootstrap) — parallel, independent
```

---

## 6. Risks & mitigations

- **Scope.** A Unity-parity editor is a product; Phase 1 is months of work.
  Mitigate with strict phase gates and a runnable artifact per phase.
- **Editor/runtime divergence.** Mitigated by the single `SceneRenderer` path
  (ADR-0008) and offscreen Game view.
- **ImGui native packaging.** Pin one `imgui-java` version and natives for
  win/linux/macos/macos-arm64; DevOps caches them in CI (STR-26).
- **Reflection edge cases.** Handled by the descriptor model with explicit
  supported kinds and graceful degradation (ADR-0007).
- **Serialization compatibility.** Versioned envelopes + migrations + backups
  (ADR-0006); round-trip tests are mandatory.

## 7. Explicit non-goals (Phase 1)

3D, shader/node graphs, animation state-machine editor, code editor, cloud
collaboration, multi-user editing, mobile/web export, hot script reload
(P2), nested prefabs and prefab overrides (P2), physics simulation (sample-side).
