# Quality Gate — MVP

Owner: QA Engineer (with the Lead Engineer). Status: active.

A change passes the quality gate when **all** of the following are true.

## Required

1. **CI green on every change.** Both `Build & Test` matrix jobs
   (Ubuntu + Windows, JDK 17) pass on the latest commit of the branch.
2. **`./gradlew check` passes locally.** This runs compilation, all untagged
   tests, and the coverage verification.
3. **Coverage floor met.** Every module that ships `src/main/java` must have a
   JaCoCo **line coverage ≥ 0.60**. Configurable via
   `-Pstratix.minLineCoverage=<0..1>`; raising it for critical modules is
   encouraged. A module with fewer than ~50 lines is exempt by judgment.
   GPU-backed modules are scoped as described below.
4. **No new `@Disabled`/skipped tests without a linked bug issue.**
5. **GPU-tagged tests** must pass in the `gpu-tests` job for any change that
   touches rendering, or the author must state why the job is not applicable.
6. **Reproducible from a clean checkout**: `git clean -xdf && ./gradlew check`
   passes (no machine-local state required).

## Not blockers (do not gate the MVP on these)

- Cosmetic/formatting issues once the above pass.
- Performance polish beyond the sample running at an acceptable frame rate.
- Missing docs for internal-only APIs.

## Coverage guidance

- Line coverage is a floor, not a goal. Assert behavior, not just execution.
- Critical paths (game loop timing, input edges, asset failure modes) should
  have explicit edge-case tests even if overall coverage already exceeds 0.60.

## GPU-backed module coverage

Some modules ship native/GL glue (window/context, renderer, batcher, textures,
atlas/font loading, raw GLFW/OpenAL devices) that can only execute with a
display and a live GL context. Those cases are tagged `gpu` and are excluded
from the default headless `check`. Coverage for such a module is **scoped to
the suite that actually ran**, so the floor is enforced on real evidence:

- **Headless — `./gradlew check` (the required Build & Test matrix jobs).** The
  module's coverage rule measures only the headless-testable classes; native/GL
  glue is excluded. This keeps the required matrix deterministic on runners
  with no display.
- **GPU — `./gradlew check -Pstratix.qa.gpuTests=true` (the `gpu-tests` job
  under xvfb).** The GL classes now execute, so they are measured too: the GPU
  measurement is the headless-testable set **plus** the GL renderer, textures,
  framebuffers, shaders, batching, atlas/font loading, and viewport code, and
  it must meet the floor. `gpu-tests` runs on PRs and `main`, so GPU coverage
  is enforced on every change that can merge.

The scope lives in the module's `build.gradle.kts` and switches on the
`stratix.qa.gpuTests` flag, so it can never diverge from the active suite.
`engine-render` is the reference implementation; a new GPU-backed module should
follow the same pattern. Measured baselines for `engine-render` (JDK 17,
JaCoCo 0.8.12): **0.920 headless**, **0.684 GPU** — both above the 0.60 floor.

**Device I/O exclusion (tracked by STR-11).** `platform/**` —
`GlfwWindow`, `GlfwInputDevice` (50 lines), and `OpenAlAudioDevice` (168 lines) —
is raw device I/O. `GlfwInputDevice` and `OpenAlAudioDevice` have no automated
coverage at all today, so the whole `platform/**` package (273 lines) is
excluded from **both** measurements. The 0.684 GPU number is what remains once the raw device glue is
removed; it is the enforceable gate for the GL rendering code. Adding
`gpu`-tagged smoke tests (input callback injection, audio open/no-device
degrade) is tracked under STR-11; until then the exclusion is explicit and
intentional, not an oversight.

**No "full class set" claim.** Measuring literally every class at once is
**not** reproducible today: with the device classes included the GPU run
measures 0.555, below the floor. Do not promise 0.646 or any full-set number
without re-running `jacocoTestReport` and updating these baselines.

## Failure handling

- A red CI run blocks merge. The author fixes it or files a bug and gets the
  failing check waived by the Lead Engineer.
- Flaky tests are bugs: file with reproduction steps, tag `slow` if needed,
  and fix rather than re-run-until-green.
