# ADR-0005: Backend recommendation for the Unity-parity + ImGui editor track

- Status: Proposed (recommendation for a track that is **pending board approval**)
- Date: 2026-10-05
- Owner: Lead Engineer
- Relates to: STR-2 (MVP spec), STR-18 (`spec` — Unity-parity + ImGui editor), STR-19 (editor architecture & ADRs, Phase 1)
- Does **not** amend: `00-engine-mvp-spec.md`, ADR-0002, ADR-0004. The MVP baseline is unchanged.

## Context

STR-18 directs that the engine mirror Unity almost 1:1 with a Dear ImGui editor and a
designed (non-default) UI. The Chief of Staff has kept the MVP spec frozen pending
board confirmation and asked the Lead Engineer to record the backend recommendation
in an ADR now, because the choice is **load-bearing for the editor**: STR-18 §5 and
§15 note that an ImGui GL3 backend effectively requires a GLFW + OpenGL context.

This ADR is intentionally an **addendum/recommendation**, not a re-baseline. The
formal editor ADRs (serialization, script binding, module boundaries) are owned by
STR-19 and will supersede/extend this one when the track is approved.

## Recommendation

1. **Keep the thin LWJGL 3 + OpenGL 3.3 core backend** chosen in ADR-0002. It
   composes cleanly with Dear ImGui: `imgui-java` ships an official
   LWJGL3 + GLFW + OpenGL3 backend (`imgui-java-lwjgl3` + `imgui-java-binding`
   with the docking branch), so the editor can render as a **final overlay pass in
   the same GL context** the engine already owns. No second window/context, no
   toolkit bridge.
2. **New editor modules are additive** and keep `engine-api` backend-neutral:
   `engine-serialize` (versioned JSON + GUID `.meta`), `engine-imgui` (binding +
   theme tokens + widget kit), `engine-editor` (panels, docking, gizmos, play
   mode). They depend on `engine-render`/`engine-core`; they do **not** leak into
   the frozen developer API. The `EngineRuntime` SPI (ADR-0004) remains the seam
   between runtime and editor.
3. **Do not adopt LibGDX for the editor track.** It would either leak
   `com.badlogic.*` into the frozen API (rejected in ADR-0002) or force a second
   GLFW + ImGui window with its own context/lifecycle, contradicting the
   single-context overlay the directive implies.
4. **Forward-compatibility asks for the MVP modules** (no re-baseline, only
   constraints on how the MVP is built):
   - Keep the OpenGL context and main loop in `engine-render`/`engine-core` such
     that an ImGui overlay can be appended after the game render call.
   - Keep `engine-core` free of rendering types so the scene/component model can
     grow toward the Unity-parity `GameObject`/`Component` model in place.
   - Do not add a second render submission path in the MVP; the editor's Scene
     view will share the retained submission path (STR-18 §8 risk).

## Consequences

- **If the board approves the Unity-parity track:** this ADR is promoted from
  Proposed to Accepted by STR-19, which then freezes the editor module
  boundaries, the versioned-JSON serializer, and the ImGui/theme integration.
- **If the board keeps the thin MVP only:** this ADR has no effect on the MVP;
  ADR-0002 stands alone and the recommendation is simply available for later.
- Pins one vendored `imgui-java` (docking) version; the editor is headless-testable
  (panels against a mock context) with GPU cases in the existing `gpu` job.
- The MVP's "no editor" non-goal (`00-engine-mvp-spec.md` §1.3) remains true for
  the MVP; the editor is a second, additive track, per STR-18.

## Alternatives considered

- **LibGDX as the editor/runtime backend.** Mature, but the API-leak or
  second-context problems above make it a worse fit than LWJGL for an ImGui
  overlay; rejected for the same reasons as ADR-0002.
- **Defer the backend recommendation until STR-19.** Rejected: the Chief of Staff
  explicitly asked for the recommendation now, and it constrains how the MVP
  modules are built (item 4), so it must pre-date Phase 1 coding.
- **A standalone Qt/Swing editor tool.** Rejected: abandons the "same context,
  same renderer" requirement and adds a second UI stack to maintain.
