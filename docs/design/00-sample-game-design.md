# Sample Game Design - "Gem Dash"

Owner: Game & Technical Designer. Co-owner: UX & Product Designer.
Status: MVP design, buildable. Last updated: 2026-10-04.

## 1. Pitch

**Gem Dash** is a single-player, top-down arcade collectathon. The player guides
*Pip*, a tiny round explorer, through a neon dusk garden. Collect every gem,
dodge patrolling *spikebots*, and escape through the portal before the timer
runs out. Three short handcrafted levels, one sitting.

- Genre: arcade collectathon, top-down, no combat.
- Session length: 3-6 minutes.
- Players: 1, local.
- Platforms: desktop (Windows/macOS/Linux) via the engine MVP.

### Why this game (engine showcase rationale)

| Engine feature | How the sample exercises it |
|---|---|
| Camera follow + bounds | Levels (1280x960 / 1600x1216) are larger than the 640x360 viewport, so the camera must scroll and clamp. |
| Sprites + atlas | Tiled floor, player, gems, enemies, portal, HUD icons - all from one atlas. |
| Animation | 4-frame walk cycles, gem spin, enemy walk, portal pulse, pickup pop. |
| Input | 4-way/8-way movement from two key sets + optional gamepad, rebindable. |
| Audio | Pickup/hurt/footstep/complete SFX, per-level music loop, master volume + mute. |
| Scene/entities | Level load, spawn, update, render, tear down, retry. |
| Simple game math | Circle overlap, timer, scoring - the sample owns this (see Scope). |

## 2. Core loop

1. Level starts. Timer = level time limit. Player spawns at `P`.
2. Player moves in 8 directions at constant speed.
3. Touch a gem -> collect: score, SFX, pop juice. When all gems are collected the
   portal activates.
4. Touch a spikebot -> lose 1 heart, gain 1.5s invulnerability, get knocked back,
   hitstop + red flash + shake. At 0 hearts -> **Game Over** (retry level).
5. Touch the **active** portal -> **Level Complete**: slow-mo flourish, tally
   (gems + time bonus), next level.
6. After level 3 -> **Final screen**: total score, per-level times, "press R to
   replay".

Win = finish all 3 levels. Lose = run out of hearts (timer running out is a soft
fail: you can still finish, but no time bonus, see Tuning).

## 3. Controls (default bindings)

| Action | Keyboard | Gamepad (best-effort MVP) |
|---|---|---|
| Move | WASD **or** Arrow keys | Left stick / D-pad |
| Confirm / Retry | Space / Enter | A / Cross |
| Pause | Esc / P | Start |
| Mute | M | - |

- All bindings remappable from `controls.json` (see API/onboarding docs).
- Movement is analog-aware on gamepad, digital on keyboard; both pass through the
  same normalized axis helper.

## 4. Entities and tuning numbers

All values are the design intent; coders should treat them as the starting
constants. Units: pixels (world space), seconds.

### Player "Pip"
- Collision: circle, radius 10 (drawn 20x20).
- Speed: 160 px/s, normalized diagonals (no diagonal speed boost).
- Acceleration: instant (arcade feel), optional 80 px/s^2 smoothing in polish.
- Invulnerability after hit: 1.5s, blink at 8 Hz.
- Knockback: 220 px/s decaying to 0 over 0.25s.
- Health: 3 hearts.
- Facing: 8-way, drives walk animation direction (down/up/side + mirrors).

### Gem
- Collision: circle radius 6 (drawn 16x16).
- Pickup trigger: distance <= 10 + 6 + 6 (player r + gem r + 6 grace).
- Value: L1 = 10, L2 = 20, L3 = 30.
- Idle: spin animation, gentle bob +/-2 px at 1.5 Hz.

### Spikebot
- Collision: circle radius 11 (drawn 24x24), contact triggers damage.
- Patrol speed: 60 px/s.
- Movement: waypoint ping-pong along authored path; turns instantly at ends.
- Contact cooldown: after a hit, player cannot be hit again until invuln ends.
- No combat; enemies cannot be destroyed. Avoidance only.

### Portal
- Inactive until all gems collected; drawn dim + grey.
- Active: 48x48, pulsing at 1.5 Hz (see Accessibility - stays under 3 Hz).
- Completion trigger: player center within 24 px of portal center.

### Camera
- Viewport: 640x360 logical, integer-scaled to window (1280x720 default).
- Follow: target = player center.
- Deadzone: 64x36 (10% of viewport w/h).
- Smoothing: exponential, time constant ~0.12s (frame-rate independent).
- Look-ahead: 0.15 * velocity clamped to 48 px, optional polish.
- Bounds: clamp camera so the view never shows outside level bounds.
- Screen shake: additive offset only to render transform; never affects gameplay
  coordinates or collision.

### Timing / scoring
- Level time limits: L1 = 60s, L2 = 75s, L3 = 90s.
- Time bonus on completion: `floor(remaining_seconds) * 5`.
- Gem pickup: +value, increments a combo-free running total.
- Score is carried across levels; final screen sums it.

## 5. Level design

### Legend (level text format)

```
.  floor (walkable)
#  hedge / wall (blocks movement)
G  gem spawn
S  spikebot spawn
P  player start
E  portal
~  void / water (impassable, no floor tile)
```

The sample loader reads this ASCII grid plus a small JSON header (name, time
limit, music track, and per-tile palette). See `docs/design/04-tooling-editor-ux.md`
for the format spec. Enemies move between `S` markers in authored order, or use
an explicit `patrol` JSON list when a path must cross open floor (preferred for
L2/L3).

### Level 1 - "First Light" (teach move + collect)

- Grid: 40x30 tiles (1280x960). Time: 60s. Gems: 6. Spikebots: 2.
- Layout: mostly open room, low threat, portal top-right. Two spikebots patrol
  simple horizontal lines on the lower and right thirds.
- Intent: player learns movement, pickup juice, and that all gems open the portal.

```
##############################  # (40 wide; shown wrapped for readability)
#..........................G.#
#.G..........................#
#........########............#
#........#......#............#
#........#..G...#....S.......#
#........#......#............#
#........########............#
#............................#
#....P...............G.......#
#............................#
#..S.....................G...#
#..........................GE#
##############################
```

### Level 2 - "Cross Traffic" (teach timing + dodging)

- Grid: 40x30. Time: 75s. Gems: 9. Spikebots: 4.
- Layout: cross-shaped corridors with a central arena; spikebots sweep across the
  arena on perpendicular paths, forcing the player to weave. Gap timing matters.
- Intent: introduce risk, multiple enemies, and the "wait for the gap" beat.

Structure (schematic):

```
######################
#.........#..........#
#..####...#...####...#
#..#..#...#...#..#...#
#..#..#...#...#..#...#
#..####...G...####...#
#.........#..........#
#####.###.#.###.######
#.........#..........#
#...S.....#.....S....#
#.........#..........#
#........P#..........#
#....S....#....S.....#
#.........#.....G....#
#.........#.....E....#
######################
```

### Level 3 - "The Gauntlet" (mastery)

- Grid: 50x38 tiles (1600x1216). Time: 90s. Gems: 12. Spikebots: 6.
- Layout: a loose maze with three chokepoints, each guarded by a sweeping bot.
  Gems are split into sectors, so routing efficiency is the skill.
- Intent: showcase camera bounds + a larger world + route planning under time.

Structure (schematic):

```
##################################################
#....G....#####....G.....#####....G..............#
#.........####..........#####..........####......#
#..####...####....####..#####...####...####..G...#
#..####...........####.........####..............#
#.................####.........####.....S........#
#....S............####..G......####..............#
#.........####....####.........####..............#
#..G......####....####....S....####....####......#
#.........####....####.........####....####..G...#
#.........####....####.........####..............#
#..####...####....####..G......####..............#
#..####...####....####.........####..............#
#.........####....####.........####....S.........#
#..G......####....####..G......####..............#
#.........####....####.........####.........####..#
#....S....####....####.........####.........####..#
############################...####################
#..........................G.P..................#
#.........####....####.........####..............#
#..G......####....####..G......####....####......#
#.........####....####.........####....####..G...#
#....S....####....####.........####..............#
#.........####....####....S....####..............#
#.........####....####.........####..............#
#..G......####....####.........####....####......#
#.........####....####.........####....####......#
#.........####....####.........####..............#
#....S....####....####.........####..............#
#.........####....####.........####..............#
#.............E.................S................#
##################################################
```

Maps above are intent/schematic. Coders may adjust exact tiles to keep patrol
paths fair; the acceptance criteria (below) are the contract, not these exact
glyphs.

## 6. Art direction

**Mood:** neon dusk garden. Calm, luminous, inviting; danger is coral-red and
reads instantly against the cool world.

**Palette**

| Name | Hex | Use |
|---|---|---|
| bg-deep | `#1B1B2F` | clear color, void |
| ground | `#2E2E4A` | floor tiles |
| ground-accent | `#3D2E5C` | alternating/edge tiles |
| hedge | `#243B3A` | walls, with mint top edge |
| pip | `#7CF6C8` | player body |
| outline | `#12212A` | all entity outlines (2 px) |
| gem | `#58E0FF` | gem body, white highlight |
| spike | `#FF5C7A` | enemy body, glowing eye |
| portal-off | `#4A4A6A` | inactive portal |
| portal-on | `#FFD166` | active portal pulse |
| ui-text | `#EAF6FF` | HUD text |
| ui-danger | `#FF5C7A` | low-hearts / timer warning |

**Style rules**
- Flat vector shapes with a 2 px dark outline; soft outer glow on emissive things
  (gems, portal, enemy eye).
- Render at 1x for a 640x360 logical viewport, nearest-neighbor filtering for
  crisp integer scaling. No sub-pixel texture blur.
- Do not rely on color alone: gems are round + bright, enemies are angular/spiked
  and move; timers use both color and text.

**Asset sizes**
- Tiles 32x32. Player frame 24x24 (drawn 20x20). Gem 16x16. Enemy 24x24.
  Portal 48x48. HUD icons 16x16.
- One atlas (`atlas.png` + `atlas.json`) at 1x, plus one music loop and SFX WAVs.
- Font: one bitmap/pixel font, 8 px cap height (glyphs 8x8), plus a 16 px title
  size or a 2x scale of the same font. Keep to one typeface.

**Animation frame counts**

| Sheet | Frames | FPS | Loop |
|---|---|---|---|
| Pip walk, per direction | 4 | 8 | loop |
| Pip idle | 2 | 3 | loop |
| Gem spin | 4 | 6 | loop |
| Spikebot walk | 2 | 4 | loop |
| Portal | 4 | 8 | loop |
| Pickup pop | 4 | 20 | once |

## 7. Feel and juice (acceptance-relevant polish)

- **Pickup:** 80 ms scale pop (1.0 -> 1.25 -> 1.0), 4-particle sparkle burst,
  rising blip SFX, score counter ticks up.
- **Hurt:** 120 ms hitstop, 0.25 s full-screen red flash (alpha 0.35 max),
  6 px screen shake decaying over 0.3 s, knockback, Pip blinks.
- **Portal activation:** portal brightens, one rising chime, gentle rays.
- **Level complete:** 0.4 s slow-mo (time scale 0.4), tally animates, SFX;
  music ducks to 50% for 300 ms.
- **Footsteps:** every 0.28 s while moving (subtle, not fatiguing).
- **Camera:** slight lead in movement direction (look-ahead), smooth, clamped.
- **Pause:** freeze update, dim scene, show "Paused - Esc to resume".
- **Game over:** vignette + desaturation, "Press R to retry".

All juice must be *additive to render/camera transforms only* - it must never
change collision, gameplay positions, or inputs. Deterministic and cheap.

## 8. Accessibility & quality bar

- Max flash frequency 3 Hz (portal 1.5 Hz, hurt flash single pulse).
- Colorblind-safe: shape + motion distinguish gems from enemies, not color alone.
- Volume sliders + mute; persist in `settings.json`.
- Pause anywhere; retry is one key.
- Time warning at <= 10 s: timer text turns `ui-danger` and gently pulses
  (text + color, no strobing).
- Stable 60 FPS on integrated graphics at 1280x720 with all sprites on screen.

## 9. Scope

**In MVP**
- 3 levels as above, title screen, level complete, game over, final screen.
- All six action groups, all listed SFX/music, full camera behavior, all juice.
- Sample-owned gameplay: collision overlap tests, patrol logic, timer, scoring,
  state machine. The engine provides sprites/animation/input/audio/camera; it
  does **not** need a physics or tilemap-collision subsystem for MVP.

**Out of MVP (explicit non-goals)**
- Combat, power-ups, bosses, procedural levels, multiplayer, save files,
  online leaderboards, localization, editor, physics engine, pathfinding,
  particle-system framework (sample rolls its own tiny particle list).

**Open scope questions / escalations**
1. Does the engine team want a small shared math helper (Vec2, circle overlap,
   clamp/lerp) or should the sample vendor its own? Design preference: engine
   ships tiny `Vec2`/`MathUtil`; collision stays sample-side.
2. Confirm logical resolution 640x360 + integer scaling with Rendering coder.
   Alternative: 960x540. Either is fine; pick one and freeze it.
3. Package/root names confirmed: `io.stratixops.engine` (engine) and
   `io.stratixops.gemdash` (sample); group `io.stratixops`. See
   `docs/architecture/ADR-0001-api-contract.md`.
4. Gamepad support is "best-effort" in MVP. If it risks the milestone, keyboard
   ships first and gamepad follows as a child issue.

## 10. Acceptance criteria (sample game)

A build is accepted when a player, with no instructions beyond the on-screen
prompt, can:

1. Add "First Light" within 60 s and finish it; the camera follows smoothly and
   never shows outside the level.
2. Collect gems and see/hear immediate feedback; the portal visibly activates
   only after the last gem.
3. Take damage from a spikebot and see knockback + invulnerability; lose all
   hearts and reach a retryable game-over screen.
4. Finish all 3 levels and see a correct total score and time bonus.
5. Pause and resume; mute; retry a level; quit cleanly.
6. Run at a stable 60 FPS at 1280x720 on integrated graphics.
7. No crashes across a full playthrough; no missing-asset silent failures.
