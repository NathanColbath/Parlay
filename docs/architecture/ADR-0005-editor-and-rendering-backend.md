# ADR-0005: Editor & rendering backend — thin LWJGL 3 + OpenGL 3.3 core + `imgui-java`

- Status: Accepted (freezes the backend for the editor)
- Date: 2026-10-05
- Owner: Lead Engineer
- Relates to: STR-19 (this decision), STR-18 `spec`, ADR-0002 (MVP backend),
  ADR-0004 (module wiring), ADR-0008 (editor module boundaries)
- Supersedes: `ADR-0005-editor-backend-recommendation.md` (the earlier *Proposed*
  recommendation for the editor track) and the open backend question in ADR-0002
  for the **editor** track. ADR-0002 remains in force for the runtime track.

## Context

STR-18 directs a desktop 2D engine whose editor mirrors Unity's workflow and uses
Dear ImGui, with a designed (not default) UI. The `spec` recommends *thin LWJGL +
OpenGL 3.3 core + `imgui-java` (docking)* but leaves the final call to this ADR.
ADR-0002 already froze **thin LWJGL 3 + OpenGL 3.3 core** for the runtime, with
all LWJGL/GL usage confined to `engine-render` and the public surface kept
backend-neutral. The editor is additive on top of that runtime.

Forces:

- The editor must share **one GL context** between the scene renderer and the
  ImGui overlay; a second toolkit with its own context/event loop is a large risk.
- The docking layout needs Scene/Game views **embedded inside dockable windows**,
  not separate OS windows.
- Reliability first: a small team, one pinned binding, minimal native surface.
- Headless testability: panel logic must run in CI without a GPU where possible.
- The published onboarding docs and `EngineRuntime` SPI already commit to
  LWJGL/OpenGL 3.3; diverging would strand ADR-0002, ADR-0004, and `GETTING_STARTED`.

## Decision

1. **Backend (frozen): thin LWJGL 3.3.x + OpenGL 3.3 core + GLFW 3**, identical to
   ADR-0002. The editor uses the *same* window/context abstraction that
   `engine-render` owns; there is no second window or event loop.
2. **ImGui binding (frozen): `imgui-java`** (SpaiR,
   `io.github.spair:imgui-java-binding`, `imgui-java-lwjgl3`,
   `imgui-java-natives-<platform>`), pinned to **one exact version with the
   docking branch** in `gradle.properties`. ImGui usage is confined to
   `engine-imgui`; no ImGui type appears in `engine-api`, `engine-core`, or any
   public signature.
3. **Single context, single overlay pass.** `engine-render` owns the GLFW window
   and GL context and runs the frame. Each frame, after scene/Game rendering,
   ImGui renders as the **final overlay pass** on the same context.
4. **Scene/Game views render offscreen and are presented as ImGui images.**
   `engine-render` renders the world into a color `RenderTarget` (FBO, nearest
   sampling, the existing 640x360 logical viewport preserved); `engine-imgui`
   presents that texture with `ImGui::Image` inside the dockable Scene and Game
   windows. This keeps ImGui layout decoupled from world rendering and lets the
   Scene and Game views present the same frame from different cameras without
   duplicating the render path.
5. **One submission path.** The retained scene renderer (`engine-core`
   `SceneRenderer`, ADR-0008) emits draws through the frozen `Renderer` API, so
   the runtime Game view and the editor Scene view render through exactly the
   same code. This directly addresses DBG2 (editor/runtime divergence) in the
   design docs.
6. **LibGDX is rejected for the editor.** It would either leak `com.badlogic.*`
   into the frozen surface or force a full wrapper, ships its own window/event
   loop that fights `imgui-java`'s LWJGL3 backend, and contradicts the
   already-published LWJGL/OpenGL 3.3 onboarding path.
7. **Main-thread only.** Editor, ImGui, input, update, and render run on the main
   GL thread. No editor worker threads in Phase 1.

## Consequences

- `engine-render` must expose a small internal SPI to the editor layer:
  a `RenderTarget` (begin/end, `textureId()`, `width()`, `height()`), a GL texture
  handle suitable for `ImGui::Image`, and access to the shared window/context.
  This is new Phase 1 work for the Rendering coder (see ADR-0008 and the Phase 1
  breakdown). It does **not** change `engine-api`.
- `engine-imgui` depends on `engine-render` (shared GL context + GL loader) and
  pins the ImGui natives for win/linux/macos/macos-arm64, mirroring the LWJGL
  native policy from ADR-0002.
- The frame order becomes: `glfwPollEvents -> new ImGui frame -> update editor
  state -> render scene into RenderTarget -> render runtime Game frame ->
  ImGui panels (present RenderTarget) -> ImGui render -> swap`.
- Editor builds inherit the ADR-0002 desktop-only trade-off. The `EngineRuntime`
  SPI remains the seam for a future web backend; the editor itself is desktop-only.
- **Headless testability.** `engine-editor` panel logic is driven by plain state
  and a mock ImGui context; `engine-imgui` provides a headless-safe render path.
  GL-touching tests are `gpu`-tagged and opt-in, per the existing QA gate.
- A future non-LWJGL host (e.g. a platform module) would replace
  `engine-render`'s window/context provider; the ImGui layer stays unchanged.

## Alternatives considered

- **LibGDX + `imgui-java` in a separate GLFW window.** Rejected: two contexts,
  focus/z-order problems, foreign object model, contradicts ADR-0002.
- **Swing/JavaFX editor embedding engine frames.** Rejected: a second UI toolkit
  with no pixel parity, extra interop glue, and no docking equivalent; fails the
  "clean, well-organized UI" requirement more expensively than ImGui.
- **`imgui-java` vs `jimgui` / raw LWJGL ImGui bindings.** `imgui-java` is
  maintained, provides first-class LWJGL3 + OpenGL3 backends, ships the docking
  branch, and has published natives for all supported OSes. The raw bindings push
  native management onto us for no benefit.
- **Render the Scene view with a viewport/scissor in the main framebuffer.** The
  docking layout moves and resizes panels dynamically and panels may overlap or
  float, so a fixed scissor region cannot track a docked window. An offscreen
  target presented as an ImGui image is the robust choice.
