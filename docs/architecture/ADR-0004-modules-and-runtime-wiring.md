# ADR-0004: Module layout and runtime wiring (service-loaded `EngineRuntime`)

- Status: Accepted
- Date: 2026-10-05
- Owner: Lead Engineer
- Relates to: STR-2 (spec), STR-5, STR-6, STR-7, ADR-0001, ADR-0002

## Context

`engine-api` freezes `Engine.run(GameLifecycle, EngineConfig)` as a static entry
point, but the runtime implementation must live in modules that ship after it.
Two coders (core, rendering/DX) must be able to work in parallel without
depending on each other. `settings.gradle.kts` auto-discovers top-level modules.

## Decisions

1. **Four engine modules** with a strict acyclic dependency direction:
   `engine-api` (frozen surface + internal SPI) ← `engine-core` and
   `engine-render`; both ← `engine-desktop` (bootstrap; the runnable artifact).
   The sample and onboarding template depend only on `engine-desktop`.
2. **`Engine.run` resolves a provider via `ServiceLoader`.** `engine-desktop`
   registers `DesktopEngineRuntime` in `META-INF/services/io.stratixops.engine.spi.EngineRuntime`.
   `Engine.run` finds the single provider and delegates; if none is present it
   throws a clear `IllegalStateException` naming the missing artifact. This keeps
   one entry point, keeps `engine-api` backend-free, and gives the future web
   backend (ADR-0002) the same seam.
3. **Internal SPI in `io.stratixops.engine.spi`**: `EngineRuntime`, `InputDevice`,
   `AudioDevice`. Backend-neutral; not developer API; changeable by ADR.
   `engine-core` implements the semantics (`DefaultInput`, `DefaultAudio`, loop,
   scene), `engine-render` implements the raw devices and graphics, and
   `engine-desktop` wires them.
4. **Assets live in `engine-render`** (graphics concern) and are supplied to the
   `EngineContext` assembled in `engine-desktop`.
5. **Auto-discovery plus one explicit include.** Keep `settings.gradle.kts`
   auto-discovery for top-level engine modules; add `include("samples:gem-dash")`
   for the nested sample and remove `samples` from the ignore list.

## Consequences

- Core and rendering are developed and tested in parallel against `engine-api`
  (core headless; render `gpu`-tagged). Integration is confined to
  `engine-desktop`, a thin module.
- No split packages: every type lives in exactly one module.
- Test strategy maps cleanly: core tests use fakes (STR-10); render tests are
  tagged `gpu` (STR-11); sample smoke boots through `engine-desktop` (STR-12).
- A new backend (web) is additive: implement the SPI and register it.

## Alternatives considered

- **Two modules (`engine-api`, one runtime).** Blocks parallel work and mixes two
  ownership domains. Rejected.
- **`Engine.run` reflection / hard-coded class name.** Fragile, and a hard
  dependency from `engine-api` to a bootstrap class it cannot see. Rejected.
- **`engine-render` depends on `engine-core` (bootstrap in render).** Fewer
  modules, but serializes the two coders and puts graphics above logic.
  Rejected in favor of the thin `engine-desktop` seam.
