# Contributing

Thanks for building the StratixOps engine. This is a short pointer doc; the
operational source of truth lives in [`docs/devops/`](docs/devops/README.md).

## Quick start

```sh
./gradlew build        # compile + test + quality gate
./gradlew check        # tests + coverage gate only
./gradlew test         # tests only
```

Both Linux and Windows are supported. JDK 17 is required; the Gradle wrapper
pins the build tool.

## Workflow

1. Pick up a Paperclip issue (`STR-###`).
2. Branch off `main`: `feat/STR-12-short-slug` (see
   [branching strategy](docs/devops/branching-strategy.md)).
3. Make a focused change with [Conventional Commits](https://www.conventionalcommits.org/).
4. Add tests; keep the QA quality gate green.
5. Open a PR using the template. Required CI checks must pass before merge.
6. Delete the branch after squash/rebase merge.

## Ground rules

- Never commit secrets, credentials, or large binaries.
- Never commit directly to `main`; never merge with red required checks.
- Add a `CHANGELOG.md` `Unreleased` entry for user-visible changes.
- Keep the build and CI low-friction: if a step is needed, wire it into Gradle
  or CI rather than documenting a manual ritual.

## Ownership

- Architecture / module layout: Lead Engineer
- Tests & quality gate: QA Engineer
- Packaging, template, getting-started: DX & Build Engineer
- Repos, branching, CI/CD, releases, secrets: DevOps Engineer

See [docs/devops/runbook.md](docs/devops/runbook.md) for recurring operations.
