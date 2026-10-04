# Hello Engine

Starter project for the StratixOps 2D engine. A window with one sprite and a line
of text, then the full Gem Dash sample. You need **JDK 17**; the Gradle wrapper is
bundled, so no Gradle install.

Full walkthrough: [`docs/GETTING_STARTED.md`](../../docs/GETTING_STARTED.md).

## 1. Run it

From this directory (`hello-engine`):

Windows (cmd / PowerShell):

```bat
gradlew.bat run
```

macOS / Linux:

```sh
./gradlew run
```

**Success:** the terminal ends with `BUILD SUCCESSFUL` and a 1280x720 window
titled **Hello Engine** opens, showing a centered cyan diamond and
`Hello, engine!` in the top-left. Close the window to exit.

**If it fails:** see [Troubleshooting](../../docs/GETTING_STARTED.md#troubleshooting).
Common first-run errors:
- `Permission denied` (macOS/Linux): `chmod +x gradlew` and retry.
- `Could not find or load main class` / dependency download errors: check the
  proxy/JAVA_HOME entries in the troubleshooting section.

## 2. Run the full sample

Windows: `gradlew.bat runSample` &nbsp;·&nbsp; macOS/Linux: `./gradlew runSample`

**Success:** the Gem Dash title screen opens.

**If it fails with `Could not find io.stratixops:gem-dash-sample`:** the sample
ships with the engine. Build the engine once from the monorepo root
(`./gradlew publishToMavenLocal`), then retry. `run` is unaffected.

## 3. Make it yours

Open `src/main/java/io/stratixops/hello/MyGame.java` and change `TODO 1` (the
image path) or `TODO 2` (the greeting), then run again. Your change appears.

`src/main/resources/` holds assets: `images/logo.png`, `sounds/blip.wav`.
`controls.json` and `settings.json` are examples of the engine's config files.

## Project layout

```
hello-engine/
  build.gradle.kts            run + runSample tasks live here
  src/main/java/.../MyGame.java   your game (3 TODOs)
  src/main/java/.../Main.java     entry point
  src/main/resources/images/      your PNGs
  src/main/resources/sounds/      your WAV/OGG files
  controls.json / settings.json   example config
```
