# ADR-0003: Game loop timing and threading model

- Status: Accepted
- Date: 2026-10-05
- Owner: Lead Engineer
- Relates to: STR-2 (spec), STR-5 (core runtime), STR-10 (core tests)
- Implements: `docs/design/01-engine-features.md` F2 / AC2.1–AC2.4

## Context

The engine must deliver frame-rate-independent gameplay (AC2.1) and recover from
stalls without a "spiral of death" (AC2.2), while keeping the implementation
deterministic and testable headless (STR-10). Rendering is immediate-mode and
expects interpolated `alpha` (ADR-0001). OpenGL contexts are main-thread-bound.

## Decisions

1. **Fixed-timestep with render interpolation**, accumulator-clamped:

   ```
   FIXED_DT = 1/60s;  MAX_FRAME = 0.25s
   accumulator += min(frameTime, MAX_FRAME)
   while (accumulator >= FIXED_DT) game.onUpdate(FIXED_DT)
   game.onRender(renderer, accumulator / FIXED_DT)
   ```

   `alpha ∈ [0,1)` is forwarded to draws by the game.
2. **Single-threaded.** Window creation, input polling, update, render, and asset
   loading all run on the main (GL) thread. OpenAL's internal threads are the only
   concurrency and are encapsulated by `AudioDevice`. No gameplay/worker threads
   in the MVP.
3. **Synchronous asset loading.** Classpath assets decode fast enough; async
   streaming is post-MVP and would complicate the loop.
4. **Pause is sample-owned.** The engine provides continuous render with no update
   while the game chooses to skip logic; `Engine.setPaused` is deferred.
5. **Shutdown:** stop audio, dispose assets/GL, destroy window, exit 0.

## Consequences

- `engine-core` can expose a pure `GameLoop` driven by an injected clock and
  frame source, so AC2.1/AC2.2/AC2.3 are unit-testable headless with fake time.
- The renderer only consumes `alpha`; it never owns timing.
- A 2 s stall costs at most `MAX_FRAME` of catch-up and then recovers.
- No thread-safety requirements on engine state; simpler and safer for the MVP.

## Alternatives considered

- **Variable timestep (`dt` = real frame time).** Simplest, but physics/collision
  (sample-side) and the AC2.1 ±2% speed requirement across 30/144 Hz are harder
  to guarantee. Rejected.
- **Multi-threaded update/render.** Unnecessary for a 2D MVP and would add
  synchronization risk; rejected by "correct over clever".
- **Fixed timestep without interpolation.** Would visibly step at 144 Hz; rejected.
