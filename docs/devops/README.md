# DevOps & Repository Operations

Owner: DevOps Engineer. This folder is the source of truth for how StratixOps
organizes its repositories, gates changes, and ships the engine.

## Status

| Area | State | Notes |
|---|---|---|
| Repository model | Defined | Single monorepo `stratixops/stratix-engine` (trunk-based). |
| Local git repo | Initialized | `main` branch created in the managed workspace. |
| Branching strategy | Documented | [branching-strategy.md](branching-strategy.md) |
| CI (build + test) | In repo | QA-owned `.github/workflows/ci.yml`, runs on every push/PR. |
| Release automation | In repo | [release.yml](../../.github/workflows/release.yml), tag-driven. |
| Secrets & environments | Defined | [secrets-and-environments.md](secrets-and-environments.md) |
| Runbook | Written | [runbook.md](runbook.md) |

## Outstanding: GitHub authorization the user must grant

The engine workspace is **not yet connected to GitHub**. To turn the committed
CI/CD configuration into live pipelines, the user must grant the following.
Until then, all pipelines below are valid but dormant.

1. **Connect GitHub to Paperclip.** Authorize the GitHub app / add a GitHub tool
   connection with `repo` + `workflow` scope for the StratixOps company. This is
   what lets agents create repos, push, and read workflow status.
2. **Provide a repository target.** Either create the org/owner and let the
   bootstrap script create `stratix-engine`, or supply an existing repo URL to
   use as `origin`.
3. **Add release environment secrets (only when releasing).** See
   [secrets-and-environments.md](secrets-and-environments.md). None are required
   to run CI; they are required only for signed/published releases.

Verify connectivity with:

```sh
gh auth status
gh repo view <owner>/stratix-engine
```

Bootstrap the remote once authorized: `scripts/github/bootstrap.ps1` (Windows)
or `scripts/github/bootstrap.sh` (Linux/macOS).

## Index

- [branching-strategy.md](branching-strategy.md) — branches, protection, review.
- [ci-cd.md](ci-cd.md) — pipeline contract and ownership boundaries.
- [release-process.md](release-process.md) — versioning and how to cut a release.
- [secrets-and-environments.md](secrets-and-environments.md) — secret handling.
- [runbook.md](runbook.md) — recurring operations and incident playbooks.
