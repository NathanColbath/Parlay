# ADR-0002: Desktop rendering backend — thin LWJGL 3, plus two additive API items

- Status: Accepted
- Date: 2026-10-05
- Owner: Lead Engineer
- Relates to: STR-2 (spec), STR-6 (rendering), ADR-0001 (API contract)
- Amends: ADR-0001 for `Renderer.fillRect` and `Audio.sfx`/`Sfx` only

## Context

The MVP needs a 2D renderer on desktop. `docs/design/01-engine-features.md`
explicitly leaves the choice to the Lead Engineer: **thin LWJGL/OpenGL vs
LibGDX**. Constraints:

- The public API is a purpose-built immediate-mode, backend-neutral surface
  (`engine-api`, ADR-0001) that must not leak a third-party object model.
- The onboarding docs (`GETTING_STARTED.md`) already commit to OpenGL 3.3 and
  document LWJGL native/classifier troubleshooting.
- Reliability is the priority; a small team must ship a playable sample.
- Extensibility to a web target is desirable but is **not** an MVP requirement.
- The built sample design needs solid-color rectangles (hurt flash, letterbox,
  particles) and one-shot SFX (pickup/hurt/complete).

## Decisions

1. **Thin LWJGL 3 + OpenGL 3.3 core** is the desktop backend. GLFW 3 for the
   window/context, a single custom sprite batcher, `stb_image` for textures,
   `stb_vorbis`/OpenAL for audio. No LibGDX.
2. **Backend isolation.** All LWJGL/GL/GLFW/OpenAL usage stays in
   `engine-render`; `engine-api` and `engine-core` stay backend-neutral. A future
   `engine-web` backend can implement the same `EngineRuntime` SPI (ADR-0004).
3. **Two additive API items (v1.1)** required by the P0 acceptance criteria and
   the sample design, and permitted because they are backward-compatible:
   - `Renderer.fillRect(float x, float y, float w, float h, int rgba)` — solid
     quad for flash/bars/debug.
   - `Audio.sfx(String path) -> Sfx` with `Sfx.play()/stop()/setVolume(float)`,
     overlapping and voice-capped.
   Everything else in the docs' aspirational surface (Animator, released/isDown,
   Engine.setPaused, audio buses, sprite tint/rotation/scale, camera zoom/shake)
   is deferred (§3.8 of the spec).

## Consequences

- `engine-render` owns window/context, batching, atlas/texture/font loading, the
  built-in bitmap font, and the raw device implementations.
- Rendering tests that need a GL context are tagged `gpu` (STR-11); the batcher
  math and camera transforms are unit-tested headless.
- LWJGL natives must be declared for win/linux/macos/macos-arm64 (STR-7). This
  is isolated to the build and matches existing troubleshooting docs.
- No web target in the MVP; the SPI boundary is the seam for it later.
- STR-5/STR-6 implement `Sfx` and `fillRect`; ADR-0001's frozen surface is
  otherwise unchanged and the compiling fixture still holds.

## Alternatives considered

- **LibGDX layer.** Mature and gives a web target, but either leaks
  `com.badlogic.*` into the frozen API or requires a full wrapper whose behavior
  we must still define; it also contradicts the already-published LWJGL/OpenGL
  3.3 onboarding docs. Rejected for the MVP; kept as a possible *web* backend
  later, which the SPI permits.
- **Thin LWJGL but also adding Animator/tint/mouse now.** Rejected: scope creep.
  Those are P1/design extras the sample can do sample-side; adding them would
  delay the core path.
