# Engine MVP — task breakdown

- Owner: Lead Engineer (STR-2)
- Source spec: [`00-engine-mvp-spec.md`](00-engine-mvp-spec.md)
- Date: 2026-10-05

Every task names its owner, the module(s) it touches, its dependencies, and
testable acceptance criteria. The Lead Engineer reviews all merged code. Do not
change architecture without a new ADR.

Legend: **[EXISTING]** = already on the board; **[NEW]** = created from this spec.

---

## Core Engine Coder

### [EXISTING] STR-5 — Core runtime implementation (`engine-core`)
Dependencies: STR-2 (this spec). No dependency on rendering.
Deliverables
- `GameLoop`: fixed-timestep accumulator + `alpha`, `MAX_FRAME` clamp (ADR-0003).
- Lifecycle wiring: `onCreate → (update*/render) → onDispose`.
- `EngineContext` implementation (`assets`, `input`, `audio`, `viewport`).
- Scene/entity model: `Scene` with `create/update/render/dispose`, entity list;
  load/transition at runtime; retry resets to initial state (F8).
- `DefaultInput` over `InputDevice`: merged `ActionMap`, buffered `pressed`,
  `down`, `axis`/`axisNormalized` semantics (F5 / AC5.1–AC5.4).
- `DefaultAudio` over `AudioDevice`: music + `Sfx` pool, voice cap 16, master
  mute, cached handles, graceful no-device (F6).
- Math helpers: `Vec2` ops; add `clamp`/`lerp`/`approach` if time permits.
Acceptance
- `./gradlew :engine-core:test` green; loop/lifecycle/input/audio failure modes
  covered with fakes (headless, no sleeps); module line coverage ≥ 0.60.
- No LWJGL/GL types referenced in `engine-core`.
- Implements the engine-api interfaces exactly; divergences require an ADR.

### [NEW] Engine runtime bootstrap — `engine-desktop`
Owner: Core Engine Coder. Dependencies: STR-5, STR-6.
Deliverables
- `DesktopEngineRuntime implements EngineRuntime`; assembles `DefaultEngineContext`
  from render's `DesktopAssets` and core's `DefaultInput`/`DefaultAudio`.
- `META-INF/services/io.stratixops.engine.spi.EngineRuntime` registration.
- `Engine.run` resolves it (already in `engine-api`); missing provider throws a
  clear `IllegalStateException`.
Acceptance
- A minimal program calling `Engine.run(lifecycle, config)` boots a window via
  the runtime when `engine-desktop` is on the classpath.
- No gameplay/graphics logic in this module; unit tests for the lookup/resolution
  path.

### [EXISTING] STR-10 — Core runtime test coverage
Dependencies: STR-5. Per `docs/qa/test-strategy.md`: loop timing, input edges,
asset failure, audio state machine, no-device degradation. `./gradlew check`
green, coverage ≥ floor. Owned with QA.

---

## Rendering & Graphics Coder

### [EXISTING] STR-6 — 2D rendering pipeline (`engine-render`)
Dependencies: STR-2 (this spec). No dependency on `engine-core`.
Deliverables
- GLFW 3 window/OpenGL 3.3 core context; vsync + software cap (F1).
- Logical 640×360 framebuffer → integer-scaled letterboxed blit (AC1.2).
- `GlRenderer implements Renderer`: `clear`, `draw`, `drawText`, `fillRect`,
  `pushCamera`/`popCamera`; sprite batch with texture-change auto-flush; alpha
  blending; stable call-order draw (F3 / AC3.1–AC3.5).
- `DesktopAssets`: texture decode (`stb_image`), atlas JSON reader + half-texel
  UV insets, built-in bitmap font (F3, §5.3).
- `Camera2D` bounds integration + rendering: world↔screen transform, deadzone,
  frame-rate-independent smoothing, clamping (F7 / AC7.1–AC7.5).
- `GlfwInputDevice` + `OpenAlAudioDevice` implementing the SPI (`engine-render`
  owns device I/O only; semantics are core).
Acceptance
- 500 sprites at 60 FPS at 1280×720 on integrated graphics (AC3.1), no atlas
  bleed (AC3.3), missing texture → magenta + log (AC3.4).
- GL-touching tests tagged `gpu` pass in the `gpu-tests` job; camera/batcher/UV
  math unit-tested headless; module coverage ≥ 0.60.
- No GL/LWJGL types in any public signature.

### [EXISTING] STR-11 — Rendering test coverage
Dependencies: STR-6. Camera transforms, batch order/flush boundaries, atlas UV,
animation timing (sample-side animator); GL cases tagged `gpu`.

---

## DX & Build Engineer

### [EXISTING] STR-7 — Build, packaging & getting-started
Dependencies: STR-2 (this spec), STR-5, STR-6.
Deliverables
- LWJGL BOM + `lwjgl`, `lwjgl-glfw`, `lwjgl-opengl`, `lwjgl-stb`, `lwjgl-openal`
  with natives for windows/linux/macos/macos-arm64 in `engine-render`.
- Publish path: `engine-api`, `engine-core`, `engine-render`, `engine-desktop`,
  `gem-dash-sample`; `publishToMavenLocal` works from a clean checkout.
- `samples/gem-dash` module (`io.stratixops:gem-dash-sample`, `Main-Class`
  `io.stratixops.gemdash.Main`); `settings.gradle.kts` explicit include.
- Template dependency switch from `engine-api` to `engine-desktop`; `run` and
  `runSample` still work as documented; packaging verified with QA.
Acceptance
- `git clean -xdf && ./gradlew check` passes; a user clones `templates/hello-engine`
  and runs `run`/`runSample` per `GETTING_STARTED.md` (CE1).

### [NEW] Reconcile `docs/API.md` and `docs/GETTING_STARTED.md` with the frozen surface
Owner: DX & Build Engineer. Dependencies: STR-2 (this spec).
Changes (per §3.8 of the spec)
- Keep `audio.sfx(path).play()` (now real) and `r.fillRect` (now real).
- Replace the engine `Animator` section with the sample-side animation pattern
  over `TextureAtlas.region(...)`; update GETTING_STARTED step 10.
- Remove `input.released`, `input.isDown(key)`, `Engine.setPaused`, and
  `audio.setVolume(bus, v)`; add a short "post-MVP" note.
- Note the y-down world coordinate convention.
Acceptance
- No documented call lacks a matching compiled signature in `engine-api`; doc
  review by the Lead Engineer.

### [EXISTING] STR-12 — Sample game end-to-end smoke + packaging verification
Dependencies: STR-7, STR-6. Headless boot/advance/exit smoke; packaged artifact
launches from a clean checkout; verified on a second clean machine. Owned with QA.

---

## QA Engineer

- [EXISTING] STR-4 — test & CI quality harness (done).
- [EXISTING] STR-10 / STR-11 — coverage follow-ups (above).
- [EXISTING] STR-12 — sample smoke + packaging.

## Designers

- [EXISTING] STR-15 — sample HUD/screens/retry binding spec (Game & Technical
  Designer; currently unassigned, needs board routing). Not on the critical path
  for the runtime, but the sample's acceptance depends on it.
- [EXISTING] STR-16 — UX clarity review of the getting-started docs (UX & Product
  Designer; unassigned). Feeds the doc reconciliation above.

---

## Critical path

```
STR-2 (this spec)
  └─> STR-5 (core) ─┐
  └─> STR-6 (render)─┴─> engine-desktop [NEW] ─> STR-7 (build/sample) ─> STR-12 (smoke)
                                  └─> STR-10 / STR-11 (coverage) alongside
```

## Definition of done (engine MVP)

All P0 features build and run on desktop; the sample is playable end to end;
`./gradlew check` is green with the coverage floor met; `git clean -xdf` reproduces
it; the getting-started path works; the public API is documented and reviewed.

---

> **Editor track (Phase 1):** the additive Unity-parity + ImGui editor is broken
> down separately in [`02-editor-architecture.md`](02-editor-architecture.md),
> with its module boundaries in
> [ADR-0008](ADR-0008-editor-module-boundaries.md). It does not change the MVP
> tasks above.
