# CI/CD Pipelines

Owner: DevOps Engineer. Pipeline *content* for tests/quality gate is owned by
QA; pipeline *infrastructure* (runners, triggers, caching, permissions,
publishing) is owned here.

## Pipelines

| Workflow | Trigger | Purpose |
|---|---|---|
| [`.github/workflows/ci.yml`](../../.github/workflows/ci.yml) | every `push`, every `pull_request` | Build + test + enforce the QA quality gate on Linux and Windows. |
| [`.github/workflows/release.yml`](../../.github/workflows/release.yml) | tag `v*.*.*` (and manual dispatch) | Build the release, create a GitHub Release, attach distributions. |

## CI contract

- **Runner matrix:** `ubuntu-latest` and `windows-latest`, JDK 17 (Temurin).
  The engine targets desktop, so both OSes are first-class.
- **Command:** `./gradlew check` — this runs the QA harness, which owns the
  coverage floor and tag filtering. CI does not redefine the quality gate; it
  invokes it.
- **Wrapper validation** runs before anything else, so a tampered wrapper fails
  fast.
- **Gradle caching** via `gradle/actions/setup-gradle`.
- **Concurrency:** one run per ref; superseded runs are cancelled.
- **Artifacts (always uploaded, even on failure):** HTML test reports, JUnit
  XML, JaCoCo reports. Retention is the GitHub default (90 days).
- **GPU/display tests** are tagged `gpu`, excluded by default, and run under
  `xvfb` only on PRs and `main` to keep pushes fast. That job invokes `check`
  (not bare `test`), so GPU-backed modules are coverage-verified against their
  full class set (`docs/qa/quality-gate.md` → "GPU-backed module coverage").

### Required status checks

Branch protection on `main` requires exactly these check names:

- `Build & Test (ubuntu-latest, JDK 17)`
- `Build & Test (windows-latest, JDK 17)`
- `GPU / display tests (Linux, xvfb)`

If a job name changes, update the branch protection rule in the same PR.

## Release contract

Release is **tag-driven**: pushing an annotated tag matching `v*.*.*` starts it.
No long-lived release branch is needed for the MVP.

1. Re-run the full build + quality gate.
2. Run the packaging task provided by DX (`assembleDist`, pending STR-7); if the
   task is absent the workflow still produces the sources/JARs it can build and
   records a warning.
3. Create the GitHub Release with generated notes plus `CHANGELOG.md` for the
   version, and attach distribution archives.

Version and artifact naming are described in
[release-process.md](release-process.md).

## Permissions and security

- Workflows default to the minimum token permission and elevate per job.
  `ci.yml` is `contents: read`; only the release job gets `contents: write`.
- No third-party action is used without a pinning decision; current actions are
  the official `actions/*` and `gradle/actions/*` releases.
- Secrets are **never** echoed. See
  [secrets-and-environments.md](secrets-and-environments.md).
- Fork PRs do not receive secrets (GitHub default); this is intentional.

## Ownership boundaries

- **QA Engineer:** what `check` means, the coverage floor, test tagging, and
  which checks are required.
- **DX & Build Engineer:** the packaging/distribution Gradle tasks the release
  workflow calls (STR-7), and publishing targets.
- **DevOps Engineer:** triggers, runners, caching, branch protection, release
  mechanics, secret wiring, and this document.
