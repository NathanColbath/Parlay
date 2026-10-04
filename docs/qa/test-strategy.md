# Test Strategy — StratixOps Engine MVP

Owner: QA Engineer. Status: active.

This document describes how we test the engine, the tools we use, and how to
add tests. It is deliberately decoupled from engine internals — it works with
whatever module layout the Lead Engineer's spec (STR-2) defines.

## Tooling

| Concern            | Choice                                            |
| ------------------ | ------------------------------------------------- |
| Build              | Gradle (wrapper pinned to `gradle/wrapper/...`)   |
| Language/toolchain | Java 17                                           |
| Test framework     | JUnit 5 (Jupiter) + AssertJ                       |
| Coverage           | JaCoCo 0.8.12; XML + HTML reports                 |
| CI                 | GitHub Actions (`.github/workflows/ci.yml`)       |

Test dependencies are declared once in the root `build.gradle.kts` and apply to
every module, so a new module only needs a `build.gradle.kts` to get the whole
test stack.

## Module discovery

`settings.gradle.kts` auto-discovers any top-level directory that contains a
Gradle build file. Engine modules light up automatically once they land; there
is no central list to keep in sync.

## Test taxonomy

Tests are classified with JUnit 5 tags.

| Tag    | Meaning                                                            | Runs where                        |
| ------ | ----------------------------------------------------------------- | --------------------------------- |
| (none) | Pure unit/integration tests: no display, no GPU, deterministic.    | Every CI job, every change.       |
| `gpu`  | Display- or GPU-bound tests (real window/context, framebuffer I/O).| `gpu-tests` job only (xvfb).      |
| `slow` | Tests over ~2s that do not need a GPU.                             | Per-PR; may be split later.       |

Rules:

- Default CI excludes `gpu` so headless runners stay green.
- GPU tests run on Linux under `xvfb-run` with `-Pstratix.qa.gpuTests=true`.
- A test that cannot run headless and is not tagged `gpu` is a bug.

## What the engine team must test (acceptance)

- **Core runtime (STR-5):** game-loop timing/accumulator behavior, fixed vs
  variable step, scene/entity lifecycle, input mapping and edge cases (key
  down/up, repeat), asset loading (missing/corrupt assets), audio state
  machine. Prefer injecting a fake clock and a headless input source.
- **Rendering (STR-6):** camera transforms/coordinate conversion, sprite
  batching (sort order, batch flush boundaries), texture atlas UV lookup,
  animation frame timing. Put OpenGL-touching cases behind the `gpu` tag;
  test the pure math/geometry untagged.
- **Sample game:** an end-to-end smoke that boots the sample, advances N frames
  headlessly, and asserts it terminates cleanly. Owned jointly with DX.

## Determinism requirements

- No wall-clock sleeps in untagged tests. Use injected clocks or `advance()`.
- No network. Assets used by tests live in `src/test/resources`.
- No order dependence; tests must pass in parallel (`maxParallelForks` is set).

## Adding a test

```java
package io.stratixops.engine.core;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class GameLoopTest {
    @Test
    void advancesByFixedStep() {
        // ...
        assertThat(/* ... */).isEqualTo(/* ... */);
    }
}
```

Then run `./gradlew check`. Coverage for any module with `src/main/java` must
meet the floor documented in `quality-gate.md`.

## Rendering test approach (headless-first)

1. Keep transform/geometry/batching math in plain classes with no GL calls and
   unit-test those untagged.
2. Wrap the GL/context surface behind an interface so tests can substitute a
   fake, or use an offscreen framebuffer.
3. Tag any test that needs a real GL context `gpu`; run it in the CI
   `gpu-tests` job under xvfb.
