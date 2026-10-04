# Tooling & Editor UX Direction

Owner: Game & Technical Designer. Co-owner: UX & Product Designer.
Status: MVP direction + post-MVP roadmap. Keeps the MVP small while leaving a
clean path to an editor.

---

## 1. Guiding decision

**The MVP ships no visual editor.** Instead it ships three cheap, high-value
tools that make Gem Dash (and any first game) pleasant to build and debug:

1. **A human-editable level format** so designers can author without code.
2. **A runtime debug overlay** for iteration and playtesting.
3. **Hot-reload** of levels/assets in dev builds.

A scene/level editor is explicitly post-MVP. The MVP's job is to prove the engine
and the sample; an editor is a product of its own.

## 2. Level format (MVP)

Plain text, diff-friendly, editable in any text editor.

Extension: `.lvl`. Structure: an optional JSON header, a separator, then an ASCII
tile grid.

```text
--- gemdash-level/1
{
  "name": "First Light",
  "timeLimit": 60,
  "music": "music/level1.ogg",
  "biome": "garden",
  "patrols": [
    { "id": "S1", "path": [[6,13],[18,13]], "speed": 60 },
    { "id": "S2", "path": [[26,5],[26,12]],  "speed": 60 }
  ]
}
---
##############################
#..........................G.#
#.G..........................#
#........########............#
#........#......#............#
#........#..G...#....S1......#
#........#......#............#
#........########............#
#............................#
#....P...............G.......#
#............................#
#..S2....................G...#
#..........................GE#
##############################
```

Tile legend

| Char | Meaning | Collision |
|---|---|---|
| `.` | floor | walkable |
| `#` | hedge/wall | solid |
| `~` | void/water | solid |
| `P` | player start | - |
| `G` | gem spawn | - |
| `S`, `S1`, `S2` | spikebot spawn/patrol anchor | - |
| `E` | portal | - |

Rules
- Grid cells are 32x32 world pixels.
- Coordinates in `patrols` are `[tileX, tileY]` corners; a bot ping-pongs along
  the polyline at the given speed.
- Unknown chars or malformed JSON -> fail fast with line/column and the bad token.
- Schema is versioned in the header (`gemdash-level/1`) so it can evolve.

Acceptance criteria
- LT1. A designer with no Java experience can change gem positions, add a bot
  patrol, and change the time limit by editing only the `.lvl` file.
- LT2. A malformed file reports the exact line/column and does not crash.
- LT3. The same file loads on all platforms (UTF-8, `\n` and `\r\n`).

## 3. Debug overlay (MVP)

Toggle keys (dev builds; compiled out or no-op in release if desired):

| Key | Overlay |
|---|---|
| `F1` | Toggle all debug rendering |
| `F2` | Collider/bounds wireframes |
| `F3` | Performance HUD: FPS, frame time, entity count, draw calls, camera (x,y) |
| `F4` | Grid + tile coordinates |
| `F5` | God mode (no damage) + timer freeze (playtest aid) |
| `F6` | Capture screenshot to `screenshots/` |

Acceptance criteria
- DBG1. Overlays toggle without reload and with < 5% frame-time impact.
- DBG2. `F2` wireframes match the actual shapes used for collision (no drift
  between debug and gameplay).
- DBG3. God mode / timer freeze never ships enabled by default in release.

## 4. Hot-reload (dev builds, P2)

- Watch `levels/`, `images/`, `audio/`; on change, reload the affected asset.
- Level reload preserves player/scene state where possible, or does a clean
  restart of the level (documented).
- Acceptance: HR1. Editing a `.lvl` file or PNG updates the running dev game
  within 1 s. HR2. A broken edit keeps the last good asset and shows the error,
  rather than crashing.

## 5. Asset pipeline conventions

- One texture atlas per game (`atlas.png` + `atlas.json`); document the packer
  used (e.g., libGDX TexturePacker or a simple tool) or a minimal internal one.
- Audio: WAV for short SFX, OGG for music.
- Naming: lowercase, `type_name_state` (e.g., `char_pip_walk_down`, `fx_gem_spin`).
- Keep source art (`.aseprite`/`.svg`) in a `art-src/` folder; ship only exported
  PNG in the build.

## 6. Post-MVP editor roadmap (direction, not commitment)

Priority order once the MVP is done:

1. **Tiled import** (`.tmx`/`.json`) - many 2D devs already use Tiled; cheap win
   vs building an editor.
2. **Scene/level editor** built on the engine's own renderer: tile paint, entity
   place, patrol path draw, properties panel, live play. This is the classic
   "unity-lite" step.
3. **Asset browser + preview**, animation preview and event tagging.
4. **UI/HUD editor**, theming.

Non-goals until after the editor exists: shader/effect editor, 3D, animation
state-machine editor, code editor, cloud collaboration.

## 7. Rationale

- Designers need to iterate on levels without a coder, but a full editor is the
  single biggest scope risk in the MVP. The `.lvl` format + hot-reload delivers
  ~80% of the iteration value for ~5% of the cost.
- A debug overlay is the cheapest playtest instrument and pays for itself during
  the sample's feel iteration.
- Keeping the editor out of MVP protects the milestone while the format and debug
  hooks keep the door open.

## 8. Open questions

1. Are dev-only overlays acceptable in the shipped sample, or must they be
   gated by a `--dev` flag? Design preference: available but off by default.
2. Who owns the atlas packer (Rendering coder vs DX engineer)?
3. Should the `.lvl` header stay JSON or move to TOML/YAML? Design preference:
   JSON (already needed for atlas metadata, no new dependency).
