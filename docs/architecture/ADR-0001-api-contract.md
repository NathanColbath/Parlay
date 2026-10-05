# ADR-0001: Public API contract for the engine MVP

- Status: Accepted (amended in part by ADR-0002)
- Date: 2026-10-04
- Owner: Lead Engineer
- Supersedes: open questions in `docs/design/02-api-ergonomics.md` (STR-3 draft)
- Relates to: STR-13 (UX review B4, S1-S6, S11), STR-2 (spec & architecture)
- Amendment: ADR-0002 adds `Audio.sfx`/`Sfx` and `Renderer.fillRect`; all other
  decisions here remain in force.

## Context

The design set ships a flagship Gem Dash API snippet that does not compile and
leaves several semantics ambiguous. Coders cannot build against it until the
public surface is frozen. The repository already hosts a QA build harness with
group `io.stratixops`, root project `stratix-engine`, and a JDK 17 toolchain.

## Decisions

1. **Naming.** Gradle group `io.stratixops`; root project `stratix-engine`;
   engine package root `io.stratixops.engine`; sample package root
   `io.stratixops.gemdash`. `com.stratixops.*` is dropped (no owned domain; the
   repo and CI already pin `io.stratixops`).
2. **JDK.** JDK 17 (LTS): matches the Gradle toolchain, CI matrix, and the local
   environment. Onboarding docs must stop saying 21.
3. **Config.** `windowSize(w, h)` (physical, default 1280x720) and
   `logicalViewport(w, h)` (logical render target, default 640x360) are separate
   builder calls. The viewport is integer-scaled and letterboxed into the
   window.
4. **Axis.** `Input.axis(action)` preserves analog magnitude; digital key/D-pad
   contributions are normalized as a group (no diagonal speed boost).
   `Input.axisNormalized(action)` forces unit length (or zero), discarding
   analog magnitude.
5. **Action registration.** Re-registering an action **merges** bindings;
   duplicate keys are idempotent. This is why WASD + arrows can both drive
   `"Move"`.
6. **Render interpolation.** `onRender(Renderer, float alpha)` receives the
   engine-supplied interpolation factor in `[0,1)`, which the developer forwards
   to entity/scene render for smooth motion.
7. **Palette ownership.** `Palette` is sample-owned; the engine knows only
   packed `0xRRGGBBAA` colors.
8. **Missing assets.** Configuration/registration errors fail fast with the
   offending value; runtime content loads log a clear path and degrade (magenta
   placeholder / silence) without crashing.
9. **Renderer model / units / math.** Immediate-mode renderer; radians; mutable
   `Vec2` with documented per-frame reuse; engine ships tiny math helpers,
   collision stays sample-side.

## Compiling fixture

The contract is executable: `engine-api/` holds the public interfaces plus the
section 2 and section 3 snippets from `docs/design/02-api-ergonomics.md` as test
sources. `./gradlew :engine-api:test` proves the snippets compile and asserts
the config defaults and action-merge semantics. The module disables the MVP
line-coverage gate because it is an interface/fixture module with no runtime.

## Consequences

- Coders (STR-5, STR-6) implement the `engine-api` surface; divergences need a
  new ADR.
- Onboarding/template (STR-14) aligns to JDK 17 and `io.stratixops.*`.
- `docs/design/00-sample-game-design.md` and `docs/design/README.md` drop the
  `com.stratixops` proposal.

## Alternatives considered

- **JDK 21.** Newer LTS, but would churn the QA harness, CI matrix, and every
  developer's `JAVA_HOME` for no MVP benefit.
- **Single `viewport(w,h)`.** Rejected: conflates physical window and logical
  target, breaking integer scaling and letterboxing.
- **Replace semantics for duplicate actions.** Rejected: forces callers to
  concatenate key lists and makes the common WASD+arrows case awkward.
