# API Reference

The engine's gameplay-facing surface, with a link to a runnable recipe for each
area.

**Source of truth.** The generated JavaDoc is authoritative for exact
signatures: run `./gradlew javadoc` (Windows: `gradlew.bat javadoc`) and open
`build/docs/javadoc/index.html`. This page is a hand-written index that points
at the recipes in [`GETTING_STARTED.md`](GETTING_STARTED.md); it is intentionally
short. If the two disagree, the JavaDoc and the source win - then fix this page.

> API status: `engine-api` (STR-13) is the frozen public contract, package
> `io.stratixops.engine`. The names below - `windowSize`/`logicalViewport`,
> `axis`/`axisNormalized`, and the missing-asset policy - are confirmed by it.
> Runtime/rendering implementations land behind the interface (STR-5/STR-6).
> Generated JavaDoc remains the source of truth.

## Entry point

| Type | What it does | Recipe |
|---|---|---|
| `Engine.run(GameLifecycle, EngineConfig)` | Starts the window and the loop. The only entry point. | [hello game](GETTING_STARTED.md#4-your-first-game) |
| `GameLifecycle` | `onCreate`, `onUpdate`, `onRender`, `onDispose`. One interface, no base class. | [lifecycle](GETTING_STARTED.md#4-your-first-game) |
| `EngineConfig.builder()` | Window size, logical viewport, title, vsync. Everything has a default. | [Main.java](../templates/hello-engine/src/main/java/io/stratixops/hello/Main.java) |

## Context: `EngineContext`

Passed into every lifecycle hook - no globals, no service locator.

| Accessor | Returns | Used for |
|---|---|---|
| `ctx.assets()` | `Assets` | Load/cache sprites, textures, atlases, fonts. |
| `ctx.input()` | `Input` | Actions, axes, keyboard/mouse/gamepad. |
| `ctx.audio()` | `Audio` | Music, SFX, volumes, mute. |
| `ctx.viewport()` | `Vec2` | Logical viewport size (640x360 by default). |

## Assets

| Call | Notes | Recipe |
|---|---|---|
| `assets.sprite(path)` | Cached; fails fast with the offending path. | [add a sprite](GETTING_STARTED.md#5-add-your-own-sprite) |
| `assets.atlas(path)` | Packed image + region metadata. | [animations](GETTING_STARTED.md#10-animations) |
| `assets.font(name)` | Built-in bitmap font; text works without shipping a font. | [first game](GETTING_STARTED.md#4-your-first-game) |

## Input

| Call | Notes | Recipe |
|---|---|---|
| `ActionMap.def().axis(name, keys...)` | Named action bound to one or more keys; repeated names merge. | [bind actions](GETTING_STARTED.md#6-bind-actions) |
| `input.axis(name)` | Magnitude-preserving `Vec2` (use for analog sticks). | [movement](GETTING_STARTED.md#7-add-movement) |
| `input.axisNormalized(name)` | Unit-length `Vec2` for the common digital case. | [movement](GETTING_STARTED.md#7-add-movement) |
| `input.pressed(name)` / `released(name)` | Edge-buffered; one press fires once. | [movement](GETTING_STARTED.md#7-add-movement) |
| `input.isDown(key)` | Raw key state. | - |

## Time & loop

| Call | Notes |
|---|---|
| `onUpdate(float dt)` | `dt` is seconds since the previous frame. Frame-rate independent. |
| `onRender(Renderer r, float alpha)` | `alpha` in [0,1) interpolates between the last two updates; forward it to draws. |
| `Engine.setPaused(boolean)` | Freezes updates, keeps rendering. |

## Rendering

| Call | Notes | Recipe |
|---|---|---|
| `r.clear(rgba)` | Colors are `0xRRGGBBAA`. | [first game](GETTING_STARTED.md#4-your-first-game) |
| `r.draw(sprite, x, y)` | Centered by anchor; draws are immediate-mode, in call order. | [add a sprite](GETTING_STARTED.md#5-add-your-own-sprite) |
| `r.drawText(text, x, y)` | Built-in font, top-left anchored. | [first game](GETTING_STARTED.md#4-your-first-game) |
| `r.pushCamera(cam)` / `r.popCamera()` | Brackets world drawing; after `pop`, draws are screen-space (HUD). | [camera](GETTING_STARTED.md#8-add-a-camera) |

## Camera: `Camera2D`

| Call | Notes | Recipe |
|---|---|---|
| `new Camera2D(viewport)` | Position, zoom, world<->screen transforms. | [camera](GETTING_STARTED.md#8-add-a-camera) |
| `camera.follow(target, dt)` | Deadzone + smoothing + level clamping in one call. | [camera](GETTING_STARTED.md#8-add-a-camera) |

## Audio

| Call | Notes | Recipe |
|---|---|---|
| `audio.sfx(path).play()` | Overlapping, voice-capped. | [sound](GETTING_STARTED.md#9-add-sound) |
| `audio.music(path, volume).loop()` | One track at a time; new track replaces the old. | [sound](GETTING_STARTED.md#9-add-sound) |
| `audio.setVolume(bus, v)` / `toggleMute()` | Buses: master, music, sfx. Takes effect next frame. | [sound](GETTING_STARTED.md#9-add-sound) |

## Animation: `Animator`

| Call | Notes | Recipe |
|---|---|---|
| `Animator.of(atlas, name, fps, mode)` | `mode`: loop / pingpong / once. | [animations](GETTING_STARTED.md#10-animations) |
| `animator.play(name)` / `update(dt)` | Switching shows the new first frame within one update. | [animations](GETTING_STARTED.md#10-animations) |

## Math: `Vec2`

`Vec2` (add/sub/scale/length/normalize), `clamp`, `lerp`, `approach`. Hot-loop
math does not allocate per call.

## Full JavaDoc

```sh
./gradlew javadoc             # macOS / Linux
gradlew.bat javadoc           # Windows
```

Output: `build/docs/javadoc/index.html`.
