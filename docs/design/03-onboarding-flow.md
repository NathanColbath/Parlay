# Getting-Started / Onboarding Flow Outline

Owner: Game & Technical Designer. Co-owner: UX & Product Designer.
Implementation of the guide/build is owned by the DX & Build Engineer; this
document defines the required flow, content, and acceptance criteria.

Goal: **a developer with the JDK installed reaches a running Gem Dash, then their
own modified sprite, in under 10 minutes, without reading engine internals.**

> **Implemented in [`../GETTING_STARTED.md`](../GETTING_STARTED.md)** (STR-14).
> This document is the spec; the guide is the executable version. Where they
> differ, the guide is what users run.

---

## 1. Audience & assumptions

- Comfortable with Java basics; not assumed to know OpenGL/LWJGL/Gradle.
- Has a terminal and internet (first build downloads deps).
- Does not read engine source to get started.

## 2. Prerequisites (state exactly, once)

- **JDK 17** (LTS). Link to Adoptium 17.
  - Aligned to the repo toolchain (`build.gradle.kts`), the CI matrix, and the QA
    quality gate - all pin 17. (The earlier draft here said 21; the Lead
    Engineer's spec may revisit this, in which case the repo and this doc change
    together - see STR-2.)
- No Gradle install required - use the included wrapper (`gradlew` / `gradlew.bat`).
- OS support: Windows/macOS/Linux; GPU with OpenGL 3.3+.

## 3. The golden path (must be fewer than 6 commands)

The template is named **`hello-engine`**, lives at `templates/hello-engine/` in
the monorepo, and is also published as `hello-engine-<version>.zip` on the
releases page.

Windows (cmd / PowerShell):

```bat
git clone https://github.com/stratixops/stratix-engine.git
cd stratix-engine\templates\hello-engine
gradlew.bat run          :: "Hello Engine" window: one sprite + text
gradlew.bat runSample    :: the full Gem Dash game
REM edit src\main\java\io\stratixops\hello\MyGame.java, then:
gradlew.bat run
```

macOS / Linux:

```sh
git clone https://github.com/stratixops/stratix-engine.git
cd stratix-engine/templates/hello-engine
./gradlew run            # "Hello Engine" window: one sprite + text
./gradlew runSample      # the full Gem Dash game
# edit src/main/java/io/stratixops/hello/MyGame.java, then:
./gradlew run
```

These commands are copy-pasteable as-is; `./gradlew` does **not** work in Windows
`cmd`/PowerShell, which is why both forms are shown. The template's build defines
working `run` and `runSample` tasks.

Acceptance criteria (onboarding)
- ON1. A clean clone builds and runs on Windows, macOS, and Linux with only a
  JDK + wrapper; no `--add-opens` or manual native setup in the happy path.
- ON2. `./gradlew run` shows the hello-sprite window **warm** (dependencies
  already downloaded) in **< 10 s**. A **cold** start (first-ever dependency
  download) must "complete without intervention" - it is not held to a
  wall-clock limit, because first-time downloads depend on the link.
- ON3. `./gradlew runSample` runs Gem Dash to the title screen.
- ON4. The first 10 minutes require no engine source reading.
- ON5. The DX engineer reproduces ON1-ON4 on all three OSes and posts evidence
  (screenshot/log) before this doc is considered validated. Tracked by STR-12,
  which is blocked until the engine and sample modules land.

## 4. Guide outline (`GETTING_STARTED.md`)

Ordered, each step runnable and verifiable. **Every step states its success
output (terminal and/or window) and its failure state with a fix link** (see the
guide's "Success" / "If it fails" blocks and the Troubleshooting section).

1. **What you'll build** - one screenshot of the hello window and one of Gem Dash.
2. **Prerequisites** - JDK 17, wrapper, OS notes.
3. **Run the sample** - the golden-path commands; expected terminal + window
   output for both `run` and `runSample`.
4. **Your first game** - the `HelloEngine` class from
   `docs/design/02-api-ergonomics.md` section 2, line by line; what each line does.
5. **Add a sprite** - load your own PNG (`images/logo.png` by default), draw it,
   change size/position. Wrong-path failure shows the engine's missing-asset
   message and a link.
6. **Bind actions** - `controls.json` and/or `ActionMap.def().axis("Move", ...)`
   before movement is introduced (fixes the old forward reference: step 6 used
   `input.axis("Move")` with no prior explanation).
7. **Add movement** - `input.axis("Move")` + `dt`; explains frame-rate
   independence in two sentences.
8. **Add a camera** - one line `camera.follow(...)`; show the level scroll.
9. **Add sound** - load and play an SFX; add music; volume/mute.
10. **Animations** - play a 4-frame loop from an atlas.
11. **Project structure** - where assets, code, and levels live.
12. **Next steps** - full Gem Dash source, engine docs, feature list.

Style rules
- Every code block is copy-pasteable and compiles.
- No forward references to concepts introduced later.
- One idea per step; one screenshot or terminal output per step.
- Short paragraphs; command blocks and code blocks over prose.

## 5. Project template (`templates/hello-engine`)

- Name/location: **`hello-engine`**, at `templates/hello-engine/` in the
  monorepo; artifact `hello-engine-<version>.zip`.
- Buildable standalone project using the published engine dependency (module
  names confirmed by STR-2; composite build acceptable while unpublished).
- Working Gradle tasks: **`run`** (the hello starter) and **`runSample`** (the
  full Gem Dash sample).
- `src/main/java/io/stratixops/hello/MyGame.java` with the hello lifecycle and 3
  `TODO` comments pointing at the guide steps.
- `src/main/resources/images/logo.png` and one placeholder sound
  (`sounds/blip.wav`). **`logo.png` is the single hello asset name** across the
  guide, the API doc, and the template (fixes the earlier `player.png` /
  `logo.png` mismatch).
- `controls.json` and `settings.json` examples.
- `README.md` = the first 3 steps.
- Acceptance: `./gradlew run` works out of the box; TODOs are findable; no dead
  files.

## 6. Troubleshooting section (required)

Published in `GETTING_STARTED.md`; each entry has symptom, cause, and fix.

- **Wrapper permission denied** (Linux/macOS): `chmod +x gradlew`.
- **Corporate proxy / Gradle downloads**: `systemProp.*.proxyHost` in
  `gradle.properties`, corporate truststore, allow-listed hosts.
- **Windows SmartScreen / firewall**: "Run anyway" / allow `java.exe`; prefer the
  release zip if downloads are blocked.
- **Wrong `JAVA_HOME` with both 17 and 21 installed**: `javaToolchains`, set
  `JAVA_HOME`, or let Gradle auto-download 17.
- Window doesn't open / crashes on start: check OpenGL 3.3, drivers, remote
  desktop, and that native libs extracted (LWJGL). Include the exact error text.
- Missing native library on Apple Silicon / Linux: platform classifier note.
- No audio device: engine degrades with a warning; how to run muted.
- HiDPI / blurry scaling: integer scaling + nearest filtering note.
- Missing asset error: exact message, case-sensitivity, `build/resources/main/`.
- Sample not found (`Could not find or load main class`): build the engine once
  with `publishToMavenLocal`.

## 7. Where the user-facing docs live

- `README.md` - 30-second summary + 4 commands + link to the guide.
- `docs/GETTING_STARTED.md` - the golden path + troubleshooting.
- Docs strategy: **generated JavaDoc is the source of truth**, with a short
  hand-written `docs/API.md` index that links the recipes. (Decision for the
  earlier "(or generated JavaDoc)" ambiguity.)
- `docs/design/*` - this design set (specs for the team, useful to curious devs).
- `samples/gem-dash/` - the full sample source.

## 8. Onboarding acceptance test (the "student test")

Given one person (preferably not an engine author) who has not seen the engine:
- They hit a running window within 10 minutes using only `GETTING_STARTED.md`.
- They can change the hello sprite and see it.
- They can explain, in their own words, what `dt`, `input.axis`, and
  `camera.follow` do.
- Every confusion they hit becomes an onboarding bug filed against the DX
  engineer (severity: friction). The guide passes when a run has zero
  blocking-confusion notes.

Owners: DX & Build Engineer implements; UX & Product Designer reviews for
clarity; Game & Technical Designer verifies the sample commands.
