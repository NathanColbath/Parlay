# Operations Runbook

Owner: DevOps Engineer. Recurring operations and incident playbooks. Keep this
current: any time a manual step is discovered, add it here.

Reference docs: [branching](branching-strategy.md) · [CI/CD](ci-cd.md) ·
[releases](release-process.md) · [secrets](secrets-and-environments.md).

---

## 1. Bootstrap the repository (once, after GitHub authorization)

**When:** the user has granted GitHub access and a repo target exists.
**Script:** `scripts/github/bootstrap.ps1` / `scripts/github/bootstrap.sh`.

What it does (idempotent, safe to re-run):

1. Confirms `gh auth status` is healthy.
2. Creates `stratixops/stratix-engine` (private) if missing, or verifies an
   existing target.
3. Adds/updates `origin` and pushes `main` plus tags.
4. Applies `main` branch protection: PR required, 1 approval, linear history,
   the three required checks, no force-push/delete.
5. Creates the `release` environment with required reviewers.
6. Records a summary.

Verify: `gh repo view stratixops/stratix-engine` and open the Actions tab; CI
should run on the bootstrap push.

## 2. Cut a release

**When:** `main` is green and the milestone is ready.
**Steps:** `scripts/release/release.ps1 -Version X.Y.Z` (or `.sh X.Y.Z`). See
[release-process.md](release-process.md) for the full contract.
**Verify:** the `Release` workflow succeeds and a GitHub Release exists for the
tag with notes and archives attached.

## 3. Hotfix a released version

1. `git fetch --tags && git switch -c hotfix/STR-n-<slug> vX.Y.Z` (the released
   tag, not `main`).
2. Fix, open a PR to `main`, pass required checks, merge.
3. If `main` moved, merge `main` into the hotfix branch and re-run CI.
4. Tag the patch: `scripts/release/release.ps1 -Version X.Y.(Z+1)`.

## 4. Investigate a failing CI run

1. Open the failing workflow; identify the job (`build-and-test`, `gpu-tests`).
2. Download the `junit-xml-*` and `coverage-*` artifacts — they upload even on
   failure.
3. Classify:
   - **Real test failure** → file/assign to the owning coder via QA; do not
     weaken the gate.
   - **Flaky (passes on retry, no code change)** → open a bug with the run URL,
     mark `flaky`, and note it in the PR.
   - **Infrastructure (runner/cache/network)** → re-run once; if persistent,
     escalate to DevOps.
4. **Never** merge with red required checks or disable a required check to
   unblock a merge. Fix forward or revert.

## 5. Rotate a secret

1. Create the new credential at the provider.
2. Update the value in the correct store (GitHub env or Paperclip) — see
   [secrets](secrets-and-environments.md).
3. Trigger a run that uses it and confirm success.
4. Revoke the old credential at the provider.
5. Note the rotation date in the PR/issue. No value is ever written to git.

## 6. Secret exposure (incident)

1. **Revoke** the exposed credential immediately at the provider.
2. **Rotate** a replacement and update the store.
3. **Audit** usage via provider logs and the Paperclip secret access events
   (`GET /api/secrets/{id}/access-events`).
4. **Remove** the value from history if it was committed; if the credential is
   revoked, history rewrite is optional but preferred.
5. **Document** a short incident note: what leaked, blast radius, time to
   revoke, follow-up. Escalate infrastructure/cost risk to the Chief of staff.

## 7. Update the Gradle wrapper

```sh
./gradlew wrapper --gradle-version <x.y.z> --distribution-type bin
```

Commit the changed `gradle/wrapper/*` and `gradlew*`. CI wrapper validation will
reject a mismatched wrapper, which is the intended guard.

## 8. Change required status checks

1. Change the job name/behavior in `.github/workflows/ci.yml` (coordinate with
   QA — they own the gate).
2. Update the branch protection rule (script or Settings → Branches) and
   [ci-cd.md](ci-cd.md) in the **same PR**, so the docs and enforcement never
   diverge.

## 9. Add or remove a collaborator / access change

- Team access is managed on the GitHub org/team, not per-file.
- Grant the **least** role that works: coders → write, QA/DX → write, external
  reviewers → read.
- Record access decisions on the relevant issue. Escalate cost/access risks to
  the Chief of staff.

## 10. Branch cleanup

Merged short-lived branches are deleted on merge (repo setting: *Automatically
delete head branches*). Weekly, prune stale remote branches:

```sh
git fetch --prune
git branch -r --merged origin/main
```

Do not delete `main` or `hotfix/*` branches with unreleased tags.

## 11. Where to look

| Need | Where |
|---|---|
| Pipeline status | GitHub → Actions |
| Quality gate definition | `build.gradle.kts` / `gradle.properties` (QA) |
| Branch rules | GitHub → Settings → Branches; bootstrap script |
| Secret inventory | [secrets-and-environments.md](secrets-and-environments.md) |
| This runbook | `docs/devops/runbook.md` |
