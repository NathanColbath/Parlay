# Release Process & Versioning

Owner: DevOps Engineer. Packaging/distribution tasks are owned by DX; the
quality gate that must pass before a release is owned by QA.

## Versioning: Semantic Versioning

`MAJOR.MINOR.PATCH` ([SemVer](https://semver.org/)). The engine is pre-1.0, so:

- **MINOR** for new engine capability or breaking API change (`0.y` bump).
- **PATCH** for fixes and non-breaking changes.
- Pre-releases use a suffix: `0.2.0-rc.1`.

Development always runs on a `-SNAPSHOT` version. Releases are immutable and
identified by an **annotated git tag**:

```
v0.1.0        # stable
v0.2.0-rc.1   # release candidate
```

### Source of truth

- **A release's version is its git tag.** Nothing else is authoritative.
- [`VERSION`](../../VERSION) holds the current development version
  (`0.1.0-SNAPSHOT`) and is bumped by the release script.
- The release pipeline stamps the tag version onto the Gradle build via
  [`gradle/release-init.gradle.kts`](../../gradle/release-init.gradle.kts)
  (`-Pstratix.version=$(tag without leading v)`), so artifacts, manifests, and
  published coordinates match the tag.

## What a release must satisfy

1. `main` is green: all required CI checks passed on the tagged commit.
2. The QA quality gate passes on the exact commit being tagged.
3. `CHANGELOG.md` has a section for the version.
4. DX packaging tasks (`assembleDist`, pending STR-7) succeed.
5. The release tag is annotated and pushed.

## Cutting a release

Preferred: run the release script, which does steps 1–5 atomically.

Windows:

```powershell
scripts/release/release.ps1 -Version 0.1.0
```

Linux/macOS:

```sh
scripts/release/release.sh 0.1.0
```

The script:

1. Verifies the working tree is clean and you are on `main`.
2. Verifies `main` is up to date with `origin/main`.
3. Bumps `VERSION` to the next `-SNAPSHOT` and promotes `CHANGELOG.md`'s
   `Unreleased` entries to the release version (dated).
4. Commits `chore(release): vX.Y.Z` and creates the annotated tag `vX.Y.Z`.
5. Pushes the commit and tag. **This triggers the release workflow.**

Manual equivalent (if the script cannot run):

```sh
git switch main && git pull --ff-only
# edit VERSION and CHANGELOG.md
git commit -am "chore(release): v0.1.0"
git tag -a v0.1.0 -m "v0.1.0"
git push origin main v0.1.0
```

## What the pipeline does on a tag

[`.github/workflows/release.yml`](../../.github/workflows/release.yml):

1. Validates the wrapper and sets up JDK 17.
2. Runs `./gradlew build check` with the tag stamped in.
3. Runs DX packaging (`assembleDist`) if present.
4. Creates the GitHub Release for the tag, using `CHANGELOG.md`'s version
   section plus auto-generated notes as the body, and attaches built archives.
5. Marks pre-release tags (`-rc.`, `-beta.`, `-alpha.`) as GitHub pre-releases.

Re-running the workflow (manual dispatch) is safe: it updates the existing
release rather than duplicating it. Release jobs are serialized with the
`release` concurrency group.

## Changelog

`CHANGELOG.md` follows [Keep a Changelog](https://keepachangelog.com/). Merge
authors do **not** edit released sections; the release script moves `Unreleased`
entries under the new version. Entries should follow Conventional Commit
phrasing so they group cleanly (Added / Fixed / Changed).

## Hotfix release

1. Branch from the released tag: `hotfix/STR-n-<slug>`.
2. Fix, open a PR to `main`, pass required checks, merge.
3. If `main` moved, merge `main` into the hotfix branch and re-verify.
4. Tag the patch from the merge commit: `scripts/release/release.ps1 -Version 0.1.1`.

## Dependencies for full automation

- DX & Build Engineer must expose a `assembleDist` (or documented) Gradle task
  that produces the distributable sample/game archive. Until then the release
  workflow builds JARs and records a warning.
- GitHub authentication must be granted (see [README.md](README.md)). No release
  signing/publishing secrets are required for the MVP; they are only needed if
  we later publish to Maven Central (see
  [secrets-and-environments.md](secrets-and-environments.md)).
