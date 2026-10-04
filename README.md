# StratixOps Engine

A Java 2D game engine for desktop. Sprites, animation, input, audio, and a
first-class camera - with a playable sample game, **Gem Dash**. Pre-MVP.

## Quickstart

The onboarding template is `hello-engine`. You need **JDK 17**; the Gradle
wrapper is bundled, so no Gradle install.

```sh
git clone https://github.com/stratixops/stratix-engine.git
cd stratix-engine/templates/hello-engine
./gradlew run          # macOS/Linux. Windows: gradlew.bat run
./gradlew runSample    # macOS/Linux. Windows: gradlew.bat runSample
```

`run` opens the "Hello Engine" window (one sprite, one line of text).
`runSample` opens the full Gem Dash game.

**Full walkthrough: [`docs/GETTING_STARTED.md`](docs/GETTING_STARTED.md)** -
zero to a running game, with a success and a failure state for every step.

## Repository layout

```
engine-api/             frozen public API contract (STR-13)
templates/hello-engine/ onboarding starter project (run + runSample)
samples/gem-dash/       full sample game source (landing)
docs/                   design specs, getting-started, API index, devops, QA
qa-smoke/               CI/quality-gate smoke module
```

## Docs

| Where | What |
|---|---|
| [docs/GETTING_STARTED.md](docs/GETTING_STARTED.md) | The golden path + troubleshooting. |
| [docs/API.md](docs/API.md) | Gameplay API index; generated JavaDoc is the source of truth. |
| [docs/design/](docs/design/README.md) | Sample design, engine features, API ergonomics, onboarding, UX review. |
| [docs/devops/](docs/devops/README.md) | Branching, CI/CD, releases, secrets. |
| [docs/qa/](docs/qa/) | Test strategy and the MVP quality gate. |

## Build the engine itself

```sh
./gradlew check        # macOS/Linux. Windows: gradlew.bat check
```

See [CONTRIBUTING.md](CONTRIBUTING.md).
