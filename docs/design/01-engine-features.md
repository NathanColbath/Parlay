# Required Engine Features & Acceptance Criteria

Owner: Game & Technical Designer. Co-owner: UX & Product Designer.
This is the gameplay-facing capability contract the sample game ("Gem Dash")
needs. It is **not** an architecture decision - module/package layout and the
rendering backend (thin LWJGL vs LibGDX) are owned by the Lead Engineer. These
are the capabilities and the observable behavior coders must satisfy.

Priority key: **P0** = MVP blocker, **P1** = MVP-required but can land after the
core path, **P2** = MVP-nice.

---

## F1. Application, window & lifecycle (P0)

The engine can create a desktop window, run a game, and shut down cleanly.

Requirements
- Create a window with title, width, height, resizable, vsync on/off.
- A fixed logical viewport (640x360) letterboxed/integer-scaled into the window.
- Lifecycle hooks: create -> update -> render -> dispose.

Acceptance criteria
- AC1.1 `Engine.run(lifecycle, config)` opens a 1280x720 window titled per config
  in < 2 s on a clean machine, and `Alt+F4`/window-close exits with code 0.
- AC1.2 Resizing the window keeps 16:9 content (letterbox bars), never stretches.
- AC1.3 `dispose()` runs on exit and releases native/GPU resources; no leaked
  threads after exit (process terminates promptly).
- AC1.4 No mandatory init order for the developer beyond implementing the
  lifecycle interface.

## F2. Game loop & timing (P0)

Deterministic, frame-rate independent updates.

Requirements
- Provide delta time in seconds (`float dt`) to update.
- Target 60 FPS; support 30/60/144 Hz displays without gameplay speed change.
- Pause: stop updates, keep rendering.

Acceptance criteria
- AC2.1 An entity moved at 160 px/s travels 160 px +/- 2% over 1 s at 60, 30 and
  144 Hz (frame-rate independence).
- AC2.2 No "spiral of death": a 2 s render stall does not cascade into unbounded
  catch-up; the loop recovers.
- AC2.3 `Engine.setPaused(true)` freezes updates (dt not applied) while render
  continues; unpause resumes without a time jump.
- AC2.4 A debug overlay can display current FPS and frame time.

## F3. Sprites & textures (P0)

Draw images with transforms and layering.

Requirements
- Load PNG/JPG from classpath and/or filesystem.
- Draw with position, origin/anchor, scale, rotation (radians/degrees, pick one
  and document), flip X/Y, opacity, tint/color modulation.
- Explicit draw order (z/layer or call order).
- Texture atlas: load a packed image + region metadata; draw sub-regions.
- Filtering option (nearest for crisp 1x art); blending correct (alpha).

Acceptance criteria
- AC3.1 Draw 500 sprites at 60 FPS at 1280x720 on integrated graphics.
- AC3.2 Alpha and color tint produce correct blending; no z-fighting; draw order
  is stable and explicit.
- AC3.3 Atlas sub-regions do not bleed neighboring pixels at 1x with nearest
  filtering.
- AC3.4 A missing texture logs a clear, actionable error with the asset path and
  does not crash the app (falls back to a magenta placeholder is acceptable).
- AC3.5 `pushCamera`/`popCamera` (see F7) plus world<->screen transforms keep
  sprites pixel-aligned at integer scale.

## F4. Animation (P0)

Frame-based animation driven by the loop.

Requirements
- Define an animation as an ordered list of atlas regions + fps + loop mode
  (loop / pingpong / once) + optional per-frame event.
- Update by `dt`; expose `finished` and current frame.
- Switching animations resets or retains time (both supported; document).

Acceptance criteria
- AC4.1 A 4-frame animation at 8 FPS completes a cycle in 0.5 s +/- 1 frame.
- AC4.2 `once` fires a `finished` callback exactly once; `loop` never fires it.
- AC4.3 Switching from walk to idle shows the new first frame within one update
  (no stale frame).
- AC4.4 Two entities can run independent instances of the same animation without
  shared-state bugs.

## F5. Input (P0)

Keyboard + mouse; optional gamepad (P1).

Requirements
- Keyboard: `isDown`, `wasPressed`, `wasReleased` per key.
- Mouse: position (in world and screen space), buttons.
- Action map: named actions bound to multiple keys/buttons; rebindable from a
  config file; resolve an analog `Vec2` axis for movement.
- Poll-based, queried during update; no lost presses at 60 FPS.
- Gamepad (P1): left stick/D-pad, face buttons, Start.

Acceptance criteria
- AC5.1 Holding two movement keys yields diagonal movement, normalized, with no
  diagonal speed boost.
- AC5.2 WASD and Arrow keys both drive the default Move action; remapping in
  `controls.json` takes effect on next launch (or live reload, documented).
- AC5.3 A quick tap (< 1 frame) between updates is still observed by
  `wasPressed` (edge buffering).
- AC5.4 `Esc` toggles pause exactly once per press (no OS key-repeat spam).
- AC5.5 If no gamepad is present, the app runs normally and gamepad APIs return
  neutral (no crash, no error dialog).
- AC5.6 Mouse position maps correctly through the current camera (screen->world).

## F6. Audio (P0)

SFX + music with volume control.

Requirements
- Load WAV and OGG; play SFX (overlapping, capped voice count) and looped music.
- Channels/buses: master, sfx, music. Volume 0..1, mute. Immediate effect.
- Missing audio device: degrade gracefully.

Acceptance criteria
- AC6.1 At least 8 overlapping SFX play without dropout or crackle.
- AC6.2 Music loops with < 50 ms gap; only one music track at a time (new track
  replaces old with a short crossfade or cut, documented).
- AC6.3 Changing master/music/sfx volume or mute takes effect within one frame.
- AC6.4 A missing audio file or absent device logs a warning and does not crash.
- AC6.5 `M` mute toggles all output; state persists if settings are saved.

## F7. Camera (P0)

A first-class 2D camera - the sample should not hand-roll this.

Requirements
- Position, viewport size, optional zoom; world<->screen transform.
- `follow(target, dt)` with: deadzone, exponential smoothing (frame-rate
  independent), look-ahead (optional), and clamping to world/level bounds.
- Screen shake: additive render-only offset with decay/duration.
- Render scope: `renderer.pushCamera(cam)` / `popCamera()` applies the transform
  to subsequent draws (HUD drawn after pop, or via a screen-space layer).

Acceptance criteria
- AC7.1 With a deadzone of 64x36, the camera does not move while the target
  moves within the deadzone, then follows smoothly (no visible jitter/steps).
- AC7.2 Following is frame-rate independent: identical paths at 30/144 Hz.
- AC7.3 Camera never shows outside level bounds when clamped, even at corners.
- AC7.4 Screen shake offsets rendering only; gameplay/collision coordinates are
  unchanged (verified by comparing positions with/without shake).
- AC7.5 HUD elements drawn in screen space are unaffected by camera transform.

## F8. Scene & entity model (P0)

A minimal, clean game structure.

Requirements
- A scene holds entities and runs lifecycle (`create/update/render/dispose`).
- Assets are loaded once and cached; scenes request assets by path/name.
- Load/transition to another scene/level at runtime.

Acceptance criteria
- AC8.1 Loading the same texture twice returns the cached handle and decodes
  once.
- AC8.2 Transitioning from L1 -> L2 disposes L1 entities and loads L2 without
  leaking (entity count returns to baseline).
- AC8.3 A retry reloads the current level to its initial state exactly.

## F9. Math & utility (P1)

Tiny helpers so game code stays short.

Requirements (preferred; if the engine declines, the sample vendors these)
- `Vec2` (add/sub/scale/length/normalize), `clamp`, `lerp`, `approach`.
- Circle overlap / AABB overlap test.
- `Time` in seconds; simple RNG.

Acceptance criteria
- AC9.1 Movement normalization uses these helpers (no duplicated math in game
  code in more than one place).
- AC9.2 Hot-loop math does not allocate per call, or the docs state the
  allocation policy.

## F10. Debug & observability (P2)

Requirements
- Runtime debug overlay toggle (`F3`): FPS, frame time, entity count, draw calls,
  camera position.
- Collider/bounds wireframe toggle (`F2`) for sprite and collision shapes.
- Hot-reload of levels/assets in dev builds (nice-to-have for iteration).

Acceptance criteria
- AC10.1 `F3` toggles an overlay with no measurable frame-time impact (> 5%).
- AC10.2 `F2` draws collider bounds matching the shapes used by gameplay code.
- AC10.3 (P2) Editing a level file or asset on disk reflects in the running dev
  build within 1 s without restarting.

---

## Cross-cutting acceptance criteria (whole engine)

- CE1. **Clean checkout builds and runs the sample** with `./gradlew runSample`
  (owned with the DX engineer).
- CE2. **Stable 60 FPS** at 1280x720 on integrated graphics with a full Gem Dash
  scene on screen.
- CE3. **No unhandled exceptions or silent asset failures** across a full
  playthrough; all failures are logged with the offending path/cause.
- CE4. **Public API is documented** with JavaDoc for every public type/method in
  the gameplay-facing surface.
- CE5. **All P0 features have automated tests** (unit where feasible, integration
  where not), runnable in CI (owned with QA).

## Ownership notes

- F1-F2, F5-F6, F8-F9: Core Engine Coder.
- F3-F4, F7: Rendering & Graphics Coder.
- F10: split (overlay = Core, collider debug = Rendering).
- F9 may be resolved sample-side if the engine opts out; escalate to Lead
  Engineer if that changes the milestone.
- Getting-started/onboarding validation: DX & Build Engineer + UX & Product
  Designer.
