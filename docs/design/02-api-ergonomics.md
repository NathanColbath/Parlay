# Engine API Ergonomics - Developer API Contract

Owner: Game & Technical Designer. Co-owner: UX & Product Designer.
Status: **API contract, frozen for MVP (STR-13).** Supersedes the open
questions in the STR-3 draft. Where this document and an implementation
differ, this document wins until it is amended by an ADR.

This is the "what it should feel like to write a game with it" contract. If
building Gem Dash requires boilerplate that is not game logic, that is an
ergonomics bug.

> **Verified snippets.** The section 2 and section 3 code below is compiled by
> the build as a fixture in the `engine-api` module
> (`engine-api/src/test/java/io/stratixops/...`). Run
> `./gradlew :engine-api:test` (Windows: `gradlew.bat :engine-api:test`) to
> prove the snippets still compile and that the config/axis semantics hold.
> The engine runtime itself lands in the core/render modules (STR-2, STR-5,
> STR-6); this module freezes the surface coders implement against.

---

## 1. Design principles & frozen decisions

1. **One obvious entry point.** A developer implements a lifecycle and calls
   `Engine.run(...)`. No factory graph, no service locator, no manual thread
   setup.
2. **Sane defaults, opt-in depth.** `EngineConfig.builder()` works with almost
   nothing set. Every advanced knob (vsync, filtering, fps cap) has a default.
3. **Context is passed in, not fetched globally.** `onCreate(ctx)`,
   `onUpdate(dt)`, `onRender(r, alpha)` carry `Assets`, `Input`, `Audio`, the
   logical viewport and time. No hidden singletons, no statics a student has to
   discover.
4. **The engine owns the hard parts.** Camera follow, input axis resolution,
   animation timing, audio buses should be one call each.
5. **Fail fast on configuration, degrade on content.** Invalid config or action
   maps throw with the offending file/key/value. Missing runtime content (a
   sprite or sound) logs a clear path and degrades to a magenta placeholder or
   silence; it never crashes.
6. **No nulls for "missing".** Use `Optional`, empty collections, or explicit
   exceptions on registration. Returning `null` is a last resort.
7. **Units are explicit and consistent.** Time in seconds (`float dt`), angles
   in radians, colors `0xRRGGBBAA`, positions in world pixels.
8. **IDEs must help.** Small, discoverable types; no wildcard `Object` params;
   builder methods return `this`; JavaDoc on everything public.
9. **Allocation-light hot path.** Reuse `Vec2`/buffers in update/render; the
   per-frame `Vec2` returned by `Input.axis*` is reused, so copy before
   retaining.
10. **The simplest game is < 50 lines.** Count it as an acceptance test.

### Frozen decisions (were "open questions")

| Question | Decision |
|---|---|
| Package roots | Engine `io.stratixops.engine`, sample `io.stratixops.gemdash`. Gradle group `io.stratixops`, root project `stratix-engine`. **No `com.stratixops`.** |
| JDK | **JDK 17 (LTS)**. Matches the Gradle toolchain, CI matrix, and the local dev environment. Do not document 21. |
| Logical viewport | **640x360**, integer-scaled and letterboxed into the physical window. |
| Renderer model | **Immediate-mode** (`r.draw(...)` per call) for MVP simplicity; batching is an implementation detail behind `Renderer`. |
| Angle units | **Radians.** |
| Text | `Renderer.drawText(...)` with a built-in bitmap font ships in MVP. |
| `Vec2` | **Mutable**, with documented ownership (per-frame instances are reused). |
| Math helpers | Engine ships the tiny helpers (`Vec2`, `clamp`, `lerp`). **Collision stays sample-side.** |
| Gamepad | **Best-effort P1.** Keyboard-first if it threatens the milestone. |

### Public API surface (types frozen here)

- Entry: `Engine.run(GameLifecycle, EngineConfig)`
- Config: `EngineConfig.builder()` -> `title`, `windowSize`, `logicalViewport`,
  `resizable`, `vsync`, `build`
- Lifecycle: `GameLifecycle.onCreate(EngineContext)` / `onUpdate(float)` /
  `onRender(Renderer, float alpha)` / `onDispose()`
- Context: `EngineContext.assets()`, `input()`, `audio()`, `viewport()`
- Assets: `Assets.sprite(path)`, `atlas(path)`, `font(path)`
- Input: `Input.bind(ActionMap)`, `pressed(action)`, `down(action)`,
  `axis(action)`, `axisNormalized(action)`; `ActionMap.def().axis(...).button(...).build()`
- Audio: `Audio.music(path, volume)`, `toggleMute()`, `setMuted(boolean)`,
  `isMuted()`; `Music.loop()`, `stop()`, `setVolume(float)`
- Render: `Renderer.clear(rgba)`, `draw(sprite, x, y)`, `drawText(text, x, y)`,
  `pushCamera(cam)`, `popCamera()`; `Camera2D(Vec2)` + `follow(target, dt)`
- Math: `Vec2` (mutable; `length()`, `normalize()`, ...)

## 2. Target "hello sprite" usage (the 10-minute path)

```java
package io.stratixops.gemdash;

import io.stratixops.engine.Engine;
import io.stratixops.engine.EngineConfig;
import io.stratixops.engine.EngineContext;
import io.stratixops.engine.GameLifecycle;
import io.stratixops.engine.Renderer;
import io.stratixops.engine.Sprite;

public final class HelloEngine implements GameLifecycle {
    private Sprite logo;

    @Override public void onCreate(EngineContext ctx) {
        logo = ctx.assets().sprite("images/logo.png");   // cached, fails fast
    }

    @Override public void onRender(Renderer r, float alpha) {
        r.clear(0x1B1B2FFF);
        r.draw(logo, 320, 180);                          // centered by anchor
        r.drawText("Hello, engine!", 8, 8);              // built-in font
    }
}

public final class Main {
    public static void main(String[] args) {
        Engine.run(new HelloEngine(), EngineConfig.builder()
            .title("Hello Engine")
            .windowSize(1280, 720)       // physical window (default)
            .logicalViewport(640, 360)   // logical render target (default)
            .build());
    }
}
```

Target: a newcomer clones the template, changes one image path and one string,
runs it. No other edits.

**Anchor defaults (S3).** `draw(sprite, x, y)` centers the sprite on `(x, y)`
(anchor `0.5, 0.5`); `drawText(text, x, y)` uses the top-left of the first
glyph. Both will grow an optional anchor/origin argument; the two-argument
forms above keep their documented defaults.

## 3. Target usage for Gem Dash (proves the API scales)

This is the shape the sample code should take, trimmed of sample-owned detail.
It **compiles today** against the `engine-api` contract (see the module
fixture). It is deliberately short: game rules only, engine plumbing abstracted.

```java
package io.stratixops.gemdash;

import io.stratixops.engine.Assets;
import io.stratixops.engine.Camera2D;
import io.stratixops.engine.EngineContext;
import io.stratixops.engine.GameLifecycle;
import io.stratixops.engine.Renderer;
import io.stratixops.engine.TextureAtlas;
import io.stratixops.engine.audio.Audio;
import io.stratixops.engine.input.ActionMap;
import io.stratixops.engine.input.Input;
import io.stratixops.engine.input.Key;
import io.stratixops.engine.math.Vec2;

public final class GemDashGame implements GameLifecycle {
    private final int levelIndex;
    private Audio audio;          // stored from ctx; no global engine handle
    private Camera2D camera;
    private Input input;
    private Level level;
    private Player player;
    private Hud hud;
    private boolean paused;       // declared

    public GemDashGame(int levelIndex) { this.levelIndex = levelIndex; }

    @Override public void onCreate(EngineContext ctx) {
        Assets assets = ctx.assets();
        audio = ctx.audio();

        input = ctx.input().bind(ActionMap.def()
            .axis("Move", Key.W, Key.A, Key.S, Key.D)
            .axis("Move", Key.UP, Key.LEFT, Key.DOWN, Key.RIGHT) // merges; 8 keys drive "Move"
            .button("Confirm", Key.SPACE, Key.ENTER)
            .button("Pause", Key.ESCAPE, Key.P)
            .button("Mute", Key.M)
            .build());

        camera = new Camera2D(ctx.viewport()); // logical viewport, e.g. 640x360
        TextureAtlas atlas = assets.atlas("atlas.json");
        level  = LevelLoader.load("levels/level" + (levelIndex + 1) + ".lvl", atlas);
        player = new Player(atlas, level.spawn());
        hud    = new Hud(atlas, assets.font("font.pixel"));
        audio.music("music/level" + (levelIndex + 1) + ".ogg", 0.8f).loop();
    }

    @Override public void onUpdate(float dt) {
        if (input.pressed("Pause")) { paused = !paused; }
        if (paused) { return; }

        Vec2 move = input.axis("Move"); // analog-aware; digital diagonals normalized
        player.update(dt, move, level, input);

        level.update(dt, player);       // gems, enemies, portal
        camera.follow(player.position(), dt);
        if (level.portalActive() && level.playerAtPortal(player)) { nextLevel(); }

        if (input.pressed("Mute")) { audio.toggleMute(); }
    }

    @Override public void onRender(Renderer r, float alpha) {
        r.clear(Palette.BG);
        r.pushCamera(camera);
        level.render(r, alpha);
        player.render(r, alpha);       // sprite + animation inside
        r.popCamera();
        hud.render(r, level.timeLeft(), player.hearts(), level.score());
    }

    private void nextLevel() {         // sample-owned scene transition
        // Unload the current level and create the next one.
    }
}
```

`Palette`, `Level`, `LevelLoader`, `Player`, `Hud` are **sample-owned** types
(defined in `samples/gem-dash`). `Palette` is *not* an engine type; the engine
only knows packed `0xRRGGBBAA` ints.

### What `alpha` is (B4)

`onRender(Renderer r, float alpha)` receives the engine-supplied interpolation
factor in `[0, 1)` between the previous and current fixed-update states. The
developer forwards it to entity/scene render calls so motion is smooth at any
display refresh rate; ignore it if the game does not interpolate. The fixture
above shows the forwarding; a render method that ignores it is equally valid.

### Axis semantics (S1/AC2)

- `Input.axis(action)` - movement direction with **analog magnitude
  preserved** (a gamepad stick keeps its deflection, enabling analog speed).
  Key/D-pad contributions are normalized *as a group*, so two orthogonal keys
  give a unit-length diagonal and there is no diagonal speed boost. At rest it
  returns `(0, 0)`. Gem Dash uses this because movement is analog-aware on
  gamepad.
- `Input.axisNormalized(action)` - same direction forced to length `0` or `1`,
  discarding analog magnitude. Use it when a game wants constant speed
  regardless of stick deflection (the common digital case).

Both return a per-frame reused `Vec2`. Both are allocation-free and are read
during `onUpdate`.

## 4. Concrete ergonomics feedback (things to get right)

| Area | Prefer | Avoid | Why |
|---|---|---|---|
| Entry point | `Engine.run(lifecycle, config)` | `new Engine(); engine.init(); engine.start();` in a specific order | Removes ordering bugs and boilerplate. |
| Config | Builder with defaults | 12-arg constructor / raw setters | Discoverable, IDE-friendly. |
| Window vs viewport | `windowSize(w,h)` and `logicalViewport(w,h)` as separate calls | One `viewport()` that conflates both | Physical window and logical render target are independent; viewport is integer-scaled. |
| Context | Passed into lifecycle | `Engine.getInstance()`, static globals | Testable, no hidden state. |
| Assets | `assets.sprite(path)` cached | `new Texture(path)` per use | Prevents leaks and duplicate decodes. |
| Missing asset | Config errors throw; runtime content -> magenta placeholder / silent + log | silent no-op | Debuggable, but never crash a shipped game. |
| Input | Named actions + `axis()` helper | raw key codes everywhere | Rebindable, no repeat bugs. |
| Action registration | Repeated `.axis(name, ...)` **merges** | Undefined replace semantics | WASD + arrows can drive one action; duplicate keys are idempotent. |
| Analog vs digital | `axis()` (magnitude) / `axisNormalized()` (unit) | One ambiguous `axis()` | Analog speed control vs constant speed are different needs. |
| Input edges | `pressed`/`released` edge-buffered | manual previous-frame tracking | Correct by default. |
| Time | `float dt` seconds | milliseconds / long nanos | Matches every tutorial. |
| Animation | `Animator` object, `play(name)` | manual frame index math | Reusable, testable. |
| Camera | `Camera2D.follow(target, dt)` | manual scroll math per frame | Centralizes feel; consistent. |
| Audio | `audio.sfx(name).play()`, music object | one global `Audio.play()` | Buses, volume, overlap. |
| Draw order | explicit `layer` or call order | implicit sort by insertion | Predictable. |
| Text | built-in bitmap font, `drawText` | require shipping a font first | "Hello engine" must be trivial. |
| Errors | descriptive + path | bare `NullPointerException` | DX quality. |
| Units | documented (seconds, radians) | mixed | Eliminates guesswork. |

## 5. Anti-goals (do not do this to the DX)

- A required base class hierarchy for every game type.
- Forcing the developer to manage native resource release for common assets.
- Requiring a JSON/YAML config for the hello-world.
- A single giant `Engine` god-object with 200 public methods - group
  capabilities (`assets()`, `input()`, `audio()`), don't pile them on.
- Making the camera optional math the developer must re-implement.
- Silent failures for any content path.

## 6. Validation plan

- **Code-complete check (B4):** the `HelloEngine` snippet (section 2) must
  compile and run against the real API with no changes other than the asset
  path. Today it compiles against the frozen `engine-api` contract; `./gradlew
  :engine-api:test` is the automated check.
- **Scale check (B4):** the Gem Dash excerpt (section 3) must be buildable -
  verified by the same module fixture. If something here is missing, that is an
  API gap to raise with the Lead Engineer.
- **Semantics check:** `EngineConfigTest` asserts the window/viewport split and
  defaults; `ActionMapTest` asserts duplicate-action merge behavior.
- **Walkthrough:** UX & Product Designer and DX & Build Engineer review this doc
  against the actual API before the sample's first playable.
- **Student test:** someone who has not seen the engine should reach a running
  window in <= 10 minutes using only the getting-started doc/onboarding flow.

## 7. Decisions and escalations

All prior open questions are resolved in the table in section 1. New questions
that change the public surface must be raised as an ADR against the Lead
Engineer; do not diverge locally.
