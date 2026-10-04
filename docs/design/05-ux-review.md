# UX Review - API Ergonomics, Onboarding & Player-Facing Clarity

Issue: STR-8 (child of STR-3). Owner: UX & Product Designer.
Reviewers consulted for ownership: Game & Technical Designer, Lead Engineer,
DX & Build Engineer. Status: findings for action. Not an architecture change.

Scope reviewed
- `02-api-ergonomics.md` (AR)
- `03-onboarding-flow.md` (ON)
- `00-sample-game-design.md` (SG) - player-facing clarity only
- Cross-checked against `01-engine-features.md` (EF), `04-tooling-editor-ux.md`
  (TE), and the current repo build (`build.gradle.kts`, `settings.gradle.kts`).

Method
- Read every doc end-to-end, then walked the onboarding golden path as a
  first-time Java developer, and walked the sample game as a first-time player
  with no instructions beyond on-screen text (SG acceptance criterion).
- Every finding names doc + section + the concrete change.

Priority key: **BLOCKER** = blocks AC / player cannot proceed; **SHOULD** = fix
before MVP acceptance; **NICE** = polish, schedule if room.

---

## 1. Blockers

### B1. Retry / replay key is contradictory (player cannot retry reliably)
- Refs: SG `§3 Controls` vs SG `§7 Feel and juice` / `§2.6`.
- Problem: the controls table binds "Confirm / Retry" to `Space / Enter`, but
  the game-over prompt says "Press R to retry" and the final screen says "press
  R to replay". `R` is not in the binding table at all.
- Change: freeze retry/replay semantics in SG `§3`:
  - `Confirm` (`Space`/`Enter`) = start, advance level-complete tally, confirm.
  - `Retry` = `R` (new explicit row), used on game over, pause, and final replay.
  - Update every prompt in SG `§2.6`, `§7` to match.

### B2. HUD has no spec and does not expose gem progress
- Refs: AR `§3` (`hud.render(r, timeLeft, hearts, score)`) vs SG `§2.3`.
- Problem: the portal only activates when all gems are collected, but the HUD
  data passed never includes gems collected/total. The player cannot tell how
  far from completion they are - acute in L3 (12 gems across sectors). SG has
  no HUD layout section, and no level-start goal prompt, yet SG acceptance says
  the player needs no instructions "beyond the on-screen prompt".
- Change (G&TD): add SG `§7.1 HUD & on-screen prompts` with the layout in
  Appendix A; extend the HUD data to include `gemsCollected` / `gemsTotal`;
  add a level-start toast "Collect all N gems!" (2.5 s) and a
  "Portal active!" toast (1.5 s).

### B3. Title / level-complete / game-over / final screens are unspecified
- Refs: SG `§9` (lists "title screen, level complete, game over, final screen"
  in scope) vs the body, which only gives one-line hints in `§7`.
- Problem: no layout, no copy, no states for four of the six screens AC3 asks
  for. Coders would invent them, and clarity/consistency would drift.
- Change (G&TD): add SG `§7.2 Screens` using the concrete spec in Appendix A
  (zones, exact copy, entry/exit states, auto-advance fallbacks).

### B4. The Gem Dash API snippet does not compile, so AR `§6` validation cannot pass
- Ref: AR `§3` (and `§6` "scale check: must be buildable").
- Problems (each is a concrete compile/understanding failure):
  - `engine.audio().toggleMute()` - `engine` is never declared; `onUpdate` has
    no `EngineContext` parameter.
  - `paused` is read/written but never declared.
  - `nextLevel()` is called but never defined.
  - `onRender(Renderer r, float alpha)` - `alpha` is never explained anywhere
    (AR `§1` omits it; ON `§4` step 4 does not explain it).
  - `Palette.BG` origin undefined (sample-owned or engine?).
  - `ctx.viewport()` is used but AR `§1.3` lists only Assets/Input/Audio/time.
- Change (Lead Engineer + G&TD): either make `§3` compile - store
  `private Audio audio = ctx.audio();`, declare `private boolean paused;`,
  define `nextLevel()`, document `alpha` as the interpolation factor the engine
  supplies and the developer forwards to `render`, state Palette ownership -
  or split into a clearly-marked pseudo-code sketch plus a small compiling
  excerpt used by the `§6` check.

### B5. The onboarding golden path is not executable by a first-timer
- Ref: ON `§3` (golden path) + `§5` (template) + repo build.
- Problems:
  - `./gradlew run` fails in Windows `cmd`/PowerShell (must be `gradlew.bat`);
    ON `§2` names both but every command uses the `./gradlew` form.
  - Template source is undefined: `git clone <template>` placeholder, folder
    `gem-dash`, but `§5` calls it `templates/hello-engine`. Four names, no URL.
  - `run` and `runSample` Gradle tasks do not exist in the current harness
    (`build.gradle.kts` has no `application` plugin or run tasks), so ON2/ON3
    cannot be met until the template lands.
  - ON `§2` says JDK 21; the repo harness pins Java 17
    (`build.gradle.kts` toolchain).
- Change: (a) OS-specific commands (`gradlew.bat run` on Windows,
  `./gradlew run` elsewhere); (b) name the template artifact, folder, and clone
  URL, and use it consistently; (c) DX & Build Engineer adds the `run` /
  `runSample` tasks to the template; (d) Lead Engineer picks JDK 17 or 21 and
  both docs/repo agree.

---

## 2. Should

### S1. `input.axis()` normalization contradicts analog movement
- Refs: AR `§3` ("`axis("Move")` returns a normalized `Vec2`") vs SG `§3`
  ("Movement is analog-aware on gamepad").
- Problem: normalizing discards gamepad magnitude, so analog speed control is
  impossible.
- Change: define `axis(name)` = raw vector (magnitude preserved) and
  `axisNormalized(name)` for the common digital case; keyboard diagonals are
  normalized inside the engine. Update both docs.

### S2. Config conflates window size and logical viewport
- Refs: AR `§2` (`.viewport(640, 360)`) vs EF F1 (1280x720 window, 640x360
  logical, integer-scaled).
- Change: `.windowSize(w, h)` (physical, default 1280x720) and
  `.logicalViewport(w, h)` (default 640x360); document integer scaling.

### S3. Sprite/text anchor and position semantics are undefined
- Ref: AR `§2`/`§3` ("centered by anchor" assumes a default that is never
  stated).
- Change: document defaults (recommend center for `draw`, top-left for
  `drawText`) and expose an anchor/origin argument; add to the AR `§4` table.

### S4. Missing-asset policy contradicts itself
- Refs: AR `§1.5` "fail fast" vs AR `§4` table + EF AC3.4 "magenta placeholder,
  do not crash".
- Change: split the policy explicitly - registration/config errors fail fast;
  runtime content load logs a clear path + degrades (placeholder / silent
  audio). Put it in one place and cross-link.

### S5. `Palette` is used before it is defined
- Ref: AR `§3` (`Palette.BG`) vs AR `§2` (raw hex `0x1B1B2FFF`).
- Change: state Palette is sample-owned and show its definition in the sample
  (`samples/gem-dash`), or make it an engine constant - pick one.

### S6. Registering the same action twice is undefined
- Ref: AR `§3` (`.axis("Move", WASD).axis("Move", arrows)`).
- Change: document merge vs replace, or use a single
  `.axis("Move", W, A, S, D, UP, LEFT, DOWN, RIGHT)` call.

### S7. Movement step introduces input with no prior action-map step
- Refs: ON `§4` step 6 uses `input.axis("Move")`; ON `§4` style rule says "No
  forward references to concepts introduced later".
- Change: add a short step before movement ("Bind actions") or fold
  `controls.json`/`ActionMap` explanation into the movement step.

### S8. Troubleshooting misses common first-run failures
- Ref: ON `§6`.
- Missing: `chmod +x gradlew` (Linux/macOS), corporate proxy for Gradle
  downloads, Windows SmartScreen/firewall prompt, wrong `JAVA_HOME` when both
  17 and 21 are installed.
- Change: add these four entries with the exact symptom and fix.

### S9. `docs/API.md` "(or generated JavaDoc)" leaves DX undecided
- Ref: ON `§7`.
- Change: pick one. Recommend generated JavaDoc as the source of truth plus a
  short hand-written `docs/API.md` index that links the 10 recipes.

### S10. Cold-start 60 s criterion is flaky
- Ref: ON `§3` ON2.
- Problem: first-time dependency downloads can exceed 60 s on slow links.
- Change: keep warm-run <10 s as the hard check; make cold run "completes
  without intervention", not a wall-clock.

### S11. Package roots disagree across docs and repo
- Refs: AR `§7.1` + SG `§9.3` propose `com.stratixops.*`; repo uses group
  `io.stratixops`, root `stratix-engine`.
- Change: Lead Engineer confirms one root; update AR/SG/repo together.

### S12. Hello-world asset name disagrees with the template
- Refs: AR `§2` loads `images/logo.png`; ON `§5` template ships
  `images/player.png`.
- Change: use one asset name in both so step 5's "change one image path" is
  literally true.

---

## 3. Nice

- N1. AR `§1.10`: make the "< 50 lines" rule an automated snippet-count check
  in CI, not a prose target.
- N2. SG `§7` pause: show mute state and a "R - Restart level" hint on the
  pause overlay.
- N3. SG HUD: specify timer format `M:SS`, countdown direction, and an 8 px
  HUD safe margin at 640x360.
- N4. SG `§7` level complete: show a 3-row tally (Gems x N, Time bonus, Total)
  with a `Confirm` prompt and a ~4 s auto-advance fallback.
- N5. SG `§5` L1: add a subtle floor decal pointing to the first gem (teaches
  pickup without text).
- N6. ON `§4`: add a "what you should see" (terminal + window) line per step so
  each step is verifiable, per the style rule.

---

## 4. Onboarding step-by-step audit (AC2)

Walked as a first-time Java developer on Windows and macOS/Linux.

| # | Step (ON `§3`) | What the dev does | Where they get stuck | Fix |
|---|---|---|---|---|
| 1 | Get template | unzip / clone | No URL, no folder name | B5 |
| 2 | `cd gem-dash` | enter folder | Folder name differs from `templates/hello-engine` | B5 |
| 3 | `./gradlew run` | build + hello window | Windows wrapper form; long cold download; no stated expected output | B5, S10, N6 |
| 4 | `./gradlew runSample` | full Gem Dash | Task undefined; unclear if same project | B5 |
| 5 | Edit `MyGame.java` | change asset/string | Elided package path; asset name mismatch | B5, S12 |
| 6 | `./gradlew run` | see change | Windows wrapper form again | B5 |
| - | Prereqs | install JDK | 21 vs repo 17 | B5(d) |

Empty/error states required but currently unspecified (ON has only a prose
troubleshooting list):
- Step 3 first run: show expected success lines (e.g., `BUILD SUCCESSFUL`,
  window title) and the exact failure text for a missing GL 3.3 context with a
  link to ON `§6`.
- Step 5: if the edited asset path is wrong, AR's missing-asset policy (S4)
  must produce the documented on-screen fallback, and ON must show it.

## 5. Screen & HUD spec proposal (AC3) - see Appendix A

Covers all six required surfaces: title, in-game HUD, level complete, game
over, pause, final score. All prompts use the bindings frozen in B1. HUD and
screens draw in screen space after `popCamera` (EF AC7.5).

---

## Appendix A - Proposed screen/HUD spec (for G&TD to adopt or amend)

Coordinates assume 640x360 logical, 8 px safe margin.

| Surface | Layout & copy | States / transitions |
|---|---|---|
| **Title** | Centered game name at 16 px; below: `Press SPACE / ENTER to start`; controls line `WASD/Arrows move  -  SPACE confirm  -  Esc pause  -  M mute`; small version string bottom-right. | Default only. Any Confirm -> Level 1. |
| **In-game HUD** | Top-left: 3 heart icons (16 px, empty/dim on loss). Top-center: `TIME M:SS`. Top-right: `GEMS n/N` and `SCORE n`. Toasts center: "Collect all N gems!" (2.5 s at start), "Portal active!" (1.5 s on activation). | Timer <= 10 s: text `ui-danger` + gentle pulse (no strobe). Hurt: hearts animate out. |
| **Pause** | Dim 60% black; `PAUSED`; `Esc / P - resume`, `R - restart level`, `M - Muted/Unmuted`. Scene frozen. | Esc/P toggles. R restarts current level. |
| **Level complete** | Panel: `LEVEL N COMPLETE`; rows `Gems  x N   = pts`, `Time bonus = pts`, `TOTAL = pts`; prompt `Press SPACE / ENTER to continue`. | Auto-advance after ~4 s if no input. Music ducks (SG `§7`). |
| **Game over** | Desaturate + vignette; `GAME OVER`; `SCORE n`; `Press R to retry`; secondary `Esc for title`. | R reloads level; Esc to title. |
| **Final** | `ALL LEVELS COMPLETE`; `TOTAL SCORE n`; per-level times list; `Press R to replay`. | R returns to title/first level. |

All six surfaces must stay within accessibility rules SG `§8` (no >3 Hz
flashing, shape+text not color alone, contrast against `bg-deep`).

---

## 6. Escalations / ownership

- **Lead Engineer:** B4 (snippet contract), B5(d) (JDK 17 vs 21), S1, S2, S6,
  S11.
- **Game & Technical Designer:** B1, B2, B3, S4, S5, S12.
- **DX & Build Engineer:** B5(a-c), S7, S8, S9, S10, S12.
- **Chief of staff:** confirm whether the Appendix A screen/HUD spec counts as
  scope growth; UX reads it as *clarity* within existing scope (no new
  gameplay, no new engine feature).

## 7. Verdict

The design set is strong and unusually honest about DX goals. It is not yet
"coders can build without further clarification" (STR-3's own definition of
done): four of six player-facing screens are unspecified (B3), the HUD hides
gem progress (B2), the retry key contradicts the controls table (B1), and the
flagship Gem Dash API snippet does not compile (B4). Onboarding has a clear
target but the golden path cannot be run as written (B5). Fixing the blockers
is small - it is writing down decisions already implied - and it is the
difference between a spec and a pick-up-and-play experience.
