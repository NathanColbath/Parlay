# Changelog

All notable changes to the StratixOps engine are recorded here.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).
Released sections are managed by the release script; add entries under
`Unreleased` only. See [docs/devops/release-process.md](docs/devops/release-process.md).

## [Unreleased]

### Added

- Repository operations: branching strategy, CI/CD pipelines, tag-driven release
  automation, secret/environment policy, and runbook (`docs/devops/`).
- Getting-started guide `docs/GETTING_STARTED.md` with OS-safe commands and a
  success/failure state for every step.
- API index `docs/API.md` (generated JavaDoc is the source of truth).
- Onboarding template `templates/hello-engine` with working `run` and
  `runSample` Gradle tasks, the hello starter, placeholder assets, and example
  `controls.json` / `settings.json`.
- Root `README.md` quickstart.

### Changed

- Onboarding flow aligned with the UX review (STR-14): JDK 17 across docs and
  repo, one template name (`hello-engine`) and asset (`images/logo.png`), a
  bind-actions step before movement, expanded troubleshooting, and a warm-run
  (not cold-start wall-clock) performance criterion.

<!--
Sections used when releasing:
### Added
### Changed
### Fixed
### Removed
-->
