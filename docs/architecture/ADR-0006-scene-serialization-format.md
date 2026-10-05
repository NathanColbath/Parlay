# ADR-0006: Scene / prefab / settings serialization — versioned JSON + GUID `.meta` sidecars

- Status: Accepted (freezes the persistence format)
- Date: 2026-10-05
- Owner: Lead Engineer
- Relates to: STR-19, STR-18 `spec` §6/§9, ADR-0001 (API), ADR-0007 (field
  metadata), ADR-0008 (`engine-serialize` boundary), STR-21 (implementation)
- Explicitly *not*: Unity YAML.

## Context

Scenes, prefabs, and project settings must be authored, diffed, reviewed, and
round-tripped without corruption. The engine is Java and already uses a small
JSON file for atlas metadata; the public API is backend-neutral with no
serialization dependency today. Assets need a **stable identity** that survives
rename/move (`.meta` + GUID, the Unity idea), and unknown content must never
crash the editor. The goal is Unity's *workflow*, not a source-level clone of
Unity's YAML serializer.

Forces:

- Human-diffable, review-friendly text; no binary blobs for structured data.
- Forward/backward compatibility: a newer project must warn, not corrupt.
- Rename/move must preserve identity; references must not break.
- Unknown component/field types must degrade gracefully (placeholder), not crash.
- `engine-core` must stay dependency-light and headless-testable — it must not
  pull a JSON stack.

## Decision

1. **JSON for all structured assets.** Scenes (`.scene`), prefabs (`.prefab`), and
   project settings (`ProjectSettings/*.json`) are versioned JSON. **Explicitly
   not Unity YAML** — JSON is already the engine's metadata idiom, has mature
   tooling, and is trivially diffable.
2. **`engine-serialize` owns all JSON and `.meta` handling.** The only new
   dependency is a pinned JSON token library (**Gson**, `com.google.code.gson`),
   confined to `engine-serialize`. `engine-api`, `engine-core`, and
   `engine-render` carry **no** JSON dependency. Object mapping is
   *metadata-driven* (ADR-0007 descriptors), not Gson's reflective auto-binding,
   so versioning, stable type ids, and unknown-type handling stay explicit and
   testable.
3. **Versioned envelope.** Every structured file starts with an envelope:

   ```json
   {
     "schema": "stratix.scene",
     "version": 1,
     "guid": "b3f1c2e0-9a44-4f1e-8b2d-0f6a5c7d9e01",
     "content": { }
   }
   ```

   `schema` identifies the kind (`stratix.scene`, `stratix.prefab`,
   `stratix.settings.window`, ...). `version` is a monotonically increasing int.
   The loader supports the current and previous versions via registered
   migrations; a **newer** version than the build supports is a fail-fast error
   naming the file and the version. An older version is migrated in memory and
   written back only on an explicit save.
4. **Stable component type ids.** Each component type registers a stable
   `typeId` string (e.g. `transform`, `spriteRenderer`, `camera`,
   `behaviour:io.stratixops.gemdash.PlayerController`). Files store `typeId`, not a
   fully-qualified class name for engine built-ins, so refactors do not silently
   break user data. Unknown `typeId` deserializes to an `UnsupportedComponent`
   placeholder that **preserves the raw JSON** and round-trips losslessly; it is
   logged, shown as a warning in the Inspector, and never crashes the load.
5. **Graph shape.** A scene's `content` holds a flat list of GameObjects; each
   carries its own `guid`, `name`, `active`, `layer`, `tags`, an ordered
   `components` array, and a `children` list of child GUIDs. Parenting is by GUID,
   not nesting, so writes are stable and cycles are impossible.

   ```json
   {
     "schema": "stratix.scene",
     "version": 1,
     "guid": "…",
     "content": {
       "name": "First Light",
       "background": "#1b1b2fff",
       "gameObjects": [
         {
           "guid": "…",
           "name": "Player",
           "active": true,
           "layer": 0,
           "tags": ["Player"],
           "children": [],
           "components": [
             { "typeId": "transform",
               "enabled": true,
               "properties": { "position": { "x": 40.0, "y": 80.0 } } },
             { "typeId": "behaviour:io.stratixops.gemdash.PlayerController",
               "enabled": true,
               "properties": { "speed": 90.0 } }
           ]
         }
       ]
     }
   }
   ```

6. **References are GUID-keyed and never nested.** Object references use
   `{ "$ref": "<gameObject-guid>" }` (GameObject/prefab instance) and asset
   references use `{ "$asset": "<asset-guid>" }` (sprite, atlas, audio, font).
   `null` is a valid empty reference. This keeps files acyclic and diff-stable.
7. **`.meta` sidecars with GUIDs.** Every asset has a sibling
   `<name>.<ext>.meta`:

   ```json
   {
     "guid": "…",
     "importer": "png",
     "importerVersion": 1,
     "settings": { "filter": "nearest", "sprite": true }
   }
   ```

   - On first import, if no sidecar exists, a **UUIDv4** GUID is generated and the
     sidecar written; the asset is logged as newly imported.
   - Rename/move keep identity because the sidecar moves with the asset. The GUID
     — not the path — is the canonical key in scenes/prefabs/refs.
   - Deleting the sidecar and re-importing yields a new identity (documented).
     A missing sidecar for an existing asset is regenerated with a warning.
   - `importerVersion` drives re-import when an importer changes; changing it
     never changes the GUID.
8. **Deterministic, diff-friendly output.** Stable key order, 2-space indent,
   LF newlines, locale-independent number formatting, one property per line.
   Collections serialize in declaration/registration order. This keeps
   scene/prefab diffs small and reviewable (acceptance criterion E9).
9. **Atomic writes.** Save writes to a temp file in the same directory, flushes,
   then atomically moves it over the target; a failed save never leaves a partial
   file. A single backup (`.bak`) of the previous revision is kept for scenes and
   prefabs.
10. **Settings are separate, individually versioned files** under
    `ProjectSettings/` (window, input, tags & layers, quality, theme). Each is a
    `stratix.settings.*` envelope. Opening a project with an unsupported settings
    version warns and keeps defaults; it never silently rewrites user data.
11. **Not serialized:** derived/cached state (world transform), transient
    runtime handles, and fields marked `@NonSerialized` (ADR-0007). Files stay
    minimal and stable.

## Consequences

- `engine-serialize` depends on `engine-core` (for the model and field
  descriptors) and adds Gson. Dependency direction is one-way: model/descriptors
  never know about JSON.
- Round-trip tests are mandatory (STR-21/STR-25): model -> JSON -> model equality,
  unknown-type preservation, GUID stability across rename/move, and version
  migration.
- The existing `.lvl` ASCII format from the MVP sample is a **legacy path**; the
  sample migrates to `.scene` + prefabs (tracked as STR-24). `.lvl` is not
  extended.
- Unity YAML tooling/import is out of scope; there is no attempt to read Unity
  `.unity`/`.prefab`/`.asset` files.
- Asset identity is now GUID-based everywhere; the Project panel keys off the
  asset database (`AssetDatabase` in `engine-serialize`), not raw paths.

## Alternatives considered

- **Unity YAML.** Maximum conceptual parity, but a bespoke emitter/parser (a
  large, risky subsystem), poor fit for Java tooling, and not required by the
  "mirror the workflow" directive. Rejected.
- **Gson reflective auto-binding of component classes.** Fast to write, but it
  makes the on-disk schema a direct mirror of class names/fields, so refactors
  break user data, unknown types fail hard, and versioning is ad hoc. Rejected in
  favour of explicit metadata-driven mapping.
- **Binary format (e.g. MessagePack, Java serialization).** Not diffable, brittle
  across versions, and unreadable to the user. Rejected.
- **TOML/YAML for settings.** Adds a second format and dependency for no benefit;
  JSON is already required for structured assets and atlas metadata. Rejected.
- **Path-keyed assets (no `.meta`/GUID).** Breaks references on rename/move and
  makes deterministic re-import impossible. Rejected.
