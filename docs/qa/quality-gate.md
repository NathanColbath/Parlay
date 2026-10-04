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

## Failure handling

- A red CI run blocks merge. The author fixes it or files a bug and gets the
  failing check waived by the Lead Engineer.
- Flaky tests are bugs: file with reproduction steps, tag `slow` if needed,
  and fix rather than re-run-until-green.
