# ADR-0007: `Behaviour` script binding & reflection-driven Inspector mapping

- Status: Accepted
- Date: 2026-10-05
- Owner: Lead Engineer
- Relates to: STR-19, STR-18 `spec` §6/§7/§11, ADR-0001 (API), ADR-0006
  (serialization), ADR-0008 (module boundaries), STR-21 / STR-23 (implementation)
- Unity analogue: `MonoBehaviour` + serialized fields + auto-drawn Inspector.

## Context

The editor must let a developer write Java gameplay classes that attach to
GameObjects, expose selected fields, and have the Inspector draw controls for
those fields automatically — the `MonoBehaviour` experience. Java has no C#
attributes; annotations plus reflection are the idiomatic substitute. The design
must be **safe** (no arbitrary code from data), **fast enough** (no per-frame
reflection), **serializable** (ADR-0006), and **headless-testable**.

Forces:

- User classes live in the project (`Assets/Scripts`), compiled onto the project
  classpath; the editor must discover them.
- Field metadata must be identical for serialization and the Inspector, or the
  two will drift.
- Unknown/missing script types must degrade, never crash (ADR-0006).
- Reflection must be bounded to the user's own components; engine internals are
  never reflectively mutated.

## Decision

1. **`Behaviour` is a `Component`.** `io.stratixops.engine.scene.Behaviour extends
   Component` and lives in `engine-core`. Users extend it. Lifecycle callbacks
   mirror Unity: `awake -> onEnable -> start -> (fixedUpdate)* -> update ->
   lateUpdate -> onDisable -> onDestroy`. The editor calls them in Play mode; the
   runtime drives them through the scene.
2. **Field exposure convention.** A field is serialized and shown in the
   Inspector if it is **public, non-static, non-transient, non-final** *or* is
   annotated `@SerializeField`. `@NonSerialized` on a public field opts out.
   `static`, `final`, and fields whose types are not serializable (see below) are
   excluded. This matches the Unity mental model while allowing private fields to
   be exposed deliberately.
3. **Supported property kinds (Phase 1):** `boolean`, `byte`/`short`/`int`/`long`,
   `float`/`double`, `String`, `enum`, `Vec2`, packed color (`int` via
   `@Color`), asset references (`Sprite`/`TextureAtlas`/`Audio` by GUID),
   `GameObject` reference (GUID), prefab reference (GUID), and `List<T>` of the
   above. Unsupported kinds are **omitted with a warning** at descriptor-build
   time (they do not fail the build) and shown read-only in the Inspector as
   "unsupported field".
4. **One cached descriptor model, shared by serialize + editor.** `engine-core`
   exposes `io.stratixops.engine.inspect.*`:
   - `ComponentDescriptor` — per component class: stable `typeId`, display name,
     category, ordered `FieldDescriptor`s.
   - `FieldDescriptor` — field name, display name, `PropertyKind`, declaring
     order, default value, and presentation hints (`@Header`, `@Range`,
     `@Tooltip`, `@Order`).
   - `ComponentRegistry` — the authoritative `typeId <-> class` map used by the
     loader, the serializer, and the Inspector.
   Descriptors are built **once per class** via reflection and cached; the frame
   loop performs **no reflection**.
5. **Deterministic ordering.** Field order is explicit: `@Order` when present,
   otherwise declaration order captured at build time and stored in the
   descriptor (never re-derived per run). This guarantees stable Inspector layout
   and stable JSON key order (ADR-0006).
6. **Inspector mapping is a registry of property drawers.** `engine-editor` owns
   a `PropertyDrawer` registry keyed by `PropertyKind` (number drag/scrub, slider
   for `@Range`, checkbox, text, enum combo, `Vec2` pair, color swatch, asset
   slot, object/prefab slot, list editor). `engine-imgui` supplies the generic
   widgets; `engine-editor` resolves a drawer per field and falls back to a
   read-only row with a warning for unknown kinds. Custom drawers can be
   registered later (P2). This keeps `engine-imgui` free of scene/domain types.
7. **Script discovery & binding.** `engine-core` provides a `ScriptRegistry` that
   discovers `Behaviour` subclasses on the project classpath (the project's
   `Assets/Scripts` compiles into the project classpath). Attachment stores the
   stable `typeId` `behaviour:<fully-qualified-name>`. On load:
   - The class is resolved from the registry. If **missing**, the component
     becomes an `UnsupportedComponent` placeholder (ADR-0006), preserving its
     serialized values and logging a clear error; the scene still loads.
   - The instance is created via a **public/no-arg constructor** (required for a
     `Behaviour`); its absence is a fail-fast registration error naming the class.
   - Serialized values are applied through the `FieldDescriptor`s (so private
     `@SerializeField` fields work), after construction and before `awake`.
8. **Hot reload is P2, not Phase 1.** The documented seam: load changed classes in
   an isolated `URLClassLoader` and remap state by field name/kind where types
   match; otherwise prompt before reloading. Phase 1 discovers scripts at editor
   start; no live recompile.
9. **Safety and failure policy.**
   - Reflection is bounded to the user's component classes and their declared
     fields; engine-internal state is never reflectively mutated.
   - Script *names* come from data, but no code is loaded from data files — only
     from the project classpath.
   - `setAccessible(true)` is used only for `@SerializeField`/public fields of
     user components (classpath, no JPMS module boundaries in Phase 1).
   - Programmer errors (missing no-arg ctor, duplicate `typeId`) fail fast at
     registration; content errors (missing class/asset) degrade to placeholders.

## Consequences

- New public-to-engine-modules surface in `engine-core`: `Component`,
  `Behaviour`, the `inspect` descriptors, and the annotations
  (`@SerializeField`, `@NonSerialized`, `@Header`, `@Range`, `@Tooltip`,
  `@Order`, `@Color`). `engine-api` is unchanged.
- `engine-serialize` consumes the same descriptors to read/write fields, so the
  Inspector and the file format can never disagree about what is serialized.
- `engine-imgui` stays domain-agnostic; `engine-editor` owns the
  descriptor -> widget mapping.
- Reflection cost is paid once per type, not per frame; performance is not a
  Phase 1 risk.
- Tests: descriptor build (kinds, defaults, ordering), serializer round-trip of a
  fixture `Behaviour`, missing-class -> placeholder, missing no-arg ctor ->
  fail-fast, and drawer mapping against a mock ImGui context (headless).
- Java generics/object references are handled deliberately (`List<T>`, GUID refs);
  arbitrary generic graphs are out of scope.

## Alternatives considered

- **Annotation-only exposure (no public-field convention).** Cleaner, but less
  Unity-like and forces boilerplate on every field. Rejected; the convention is
  configurable if a project prefers annotation-only.
- **Reflection on every frame (draw straight from `Field`).** Simplest to write,
  but a correctness/perf hazard at editor scale; descriptors cached once are
  cheap. Rejected.
- **Code generation / annotation processor for descriptors.** Faster at runtime,
  but adds a build step and complexity for Phase 1; reflection + caching is
  sufficient and simpler. Deferred.
- **A separate scripting language (e.g. Groovy/Lua).** Out of scope and not
  requested; Java classes are the Unity-C# equivalent here. Rejected.
- **Reflectively serializing object graphs directly.** Cyclic graphs, unstable
  schemas, and unknown-type crashes. Rejected in favour of GUID references
  (ADR-0006).
