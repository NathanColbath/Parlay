# Branching Strategy

Owner: DevOps Engineer. Applies to `stratixops/stratix-engine`.

## Model: trunk-based with short-lived branches

The engine is a **single monorepo**. `main` is the trunk and is the only
long-lived branch. Work happens on short-lived branches that are merged back
within a day or two. Long-running branches are an anti-pattern here: the two
coder tracks (core runtime, rendering) integrate on `main`, not on side
branches.

```
main ──●────●────●────●────●────●──►  (always green, tagged for releases)
        \        /      \        /
         feat/x  ─        fix/y ─        (short-lived, deleted on merge)
```

### Branch names

| Prefix | Use | Example |
|---|---|---|
| `feat/` | New capability | `feat/STR-5-asset-loader` |
| `fix/` | Bug fix | `fix/STR-6-camera-jitter` |
| `docs/` | Documentation only | `docs/STR-9-runbook` |
| `chore/` | Build, deps, tooling | `chore/bump-junit` |
| `hotfix/` | Urgent fix branched from a release tag | `hotfix/v0.1.1-window-crash` |

Include the Paperclip issue identifier (`STR-n`) in the branch name so branches
map cleanly to work items.

### Rules

1. **Never commit directly to `main`.** Even trivial docs changes go through a
   PR so CI runs.
2. **Keep branches short-lived.** Rebase on `main` daily; do not let branches
   drift.
3. **One logical change per branch/PR.**
4. **Delete the branch on merge.**
5. **Linear history.** Rebase or squash-merge; merge commits are not used.
6. **`main` is always green.** Do not merge with failing required checks.

## Commit messages

Use [Conventional Commits](https://www.conventionalcommits.org/) so the release
pipeline can generate changelogs:

```
<type>(<scope>): <summary>

types: feat | fix | docs | chore | refactor | test | build | perf
scope: engine-core | engine-render | sample | build | docs | ci | ...
```

Example: `feat(engine-render): add sprite batch draw batching`.

## Protecting `main`

`scripts/github/bootstrap.ps1` / `bootstrap.sh` apply these rules to the remote
(idempotent). Equivalently they can be set in **Settings → Branches**:

- Require a pull request before merging.
  - Require **1 approval**; dismiss stale approvals on new commits.
  - Require review from Code Owners once a `CODEOWNERS` file exists.
- Require status checks to pass before merging:
  - **Build & Test (ubuntu-latest, JDK 17)**
  - **Build & Test (windows-latest, JDK 17)**
  - **GPU / display tests (Linux, xvfb)** (PRs and `main` only)
  - Require branches to be up to date before merging.
- Require linear history (no merge commits).
- Require conversation resolution.
- Do not allow force pushes or deletions of `main`.

## Release branches

Not used for the MVP. Releases are tagged directly on `main` (see
[release-process.md](release-process.md)). A `release/x.y` branch is only
introduced if a release needs stabilization beyond a day, and this document
must be updated first.

## Hotfixes

Branch from the released tag, not from `main`:

```sh
git fetch --tags
git switch -c hotfix/STR-99-<slug> v0.1.0
# fix, PR against main, then tag v0.1.1 from the merge commit
```

If `main` has moved on, merge `main` into the hotfix branch after review before
tagging, so the fix does not regress.

## Ownership of changes under this policy

- **DevOps Engineer** owns this policy and repo settings.
- **Lead Engineer** owns module/repo layout and `CODEOWNERS` membership.
- **QA Engineer** owns which checks are required.
- **DX & Build Engineer** owns packaging tasks invoked by release.
