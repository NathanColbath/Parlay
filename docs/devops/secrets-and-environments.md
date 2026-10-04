# Secrets & Environment Management

Owner: DevOps Engineer. Never commit a secret to this repository.

## Principles

1. **No secrets in the repo, build outputs, or logs.** Only `.env.example`-style
   placeholders with no real values are ever committed.
2. **Least privilege.** A workflow gets the narrowest token scope that works.
3. **Separation by environment.** CI and release use different scopes; release
   is gated.
4. **Rotate on a schedule and on suspicion.** See [runbook.md](runbook.md).
5. **Prefer short-lived credentials.** Where a provider supports OIDC federation,
   use it instead of a long-lived token.

## Two secret stores

| Store | Used by | Notes |
|---|---|---|
| **GitHub Actions secrets / environments** | CI and release workflows | Only place CI/CD reads secrets at runtime. |
| **Paperclip secret store** | Agents and runtimes | Company/agent secrets; referenced by name, never printed. |

A secret lives in exactly one store per consumer. Do not duplicate a value into
both unless the consumers genuinely differ, and record the duplication in the
table below.

## GitHub Environments

Create these in **Settings → Environments** (the bootstrap script does this):

| Environment | Purpose | Protection rules |
|---|---|---|
| `ci` | Ordinary CI. | No secrets today. Deployment branches: all. |
| `release` | Tag-driven releases. | **Required reviewers** (Lead Engineer + DevOps), deployment branches limited to `main` and tags matching `v*`. |

Environment-scoped secrets are only exposed to jobs that declare
`environment: release`, so a compromised test job cannot read release
credentials.

## Secret catalog

Required for the MVP: **none.** CI and release authenticate with the automatic
`GITHUB_TOKEN` (`contents: read` for CI, `contents: write` for the release job
only). The table is the register for anything added later.

| Name | Scope | Purpose | Required now? | Store |
|---|---|---|---|---|
| `GITHUB_TOKEN` | per-run | Checkout, upload artifacts, create release. | automatic | GitHub (built-in) |
| `MAVEN_CENTRAL_USERNAME` | `release` env | Publish engine artifacts. | no (post-MVP) | GitHub env |
| `MAVEN_CENTRAL_PASSWORD` | `release` env | Publish engine artifacts. | no (post-MVP) | GitHub env |
| `GPG_SIGNING_KEY` | `release` env | Sign published artifacts. | no (post-MVP) | GitHub env |
| `GPG_SIGNING_PASSPHRASE` | `release` env | Unlock signing key. | no (post-MVP) | GitHub env |
| `CODECOV_TOKEN` | repo secret | Upload coverage to Codecov. | no (optional) | GitHub repo |

Paperclip-held secrets (referenced by key from an agent's environment) are
registered via `POST /api/companies/{companyId}/secrets`. Values are write-only
from the agent's perspective; use
`GET /api/agents/me/secrets/{key}/value` only when a run genuinely needs to read
one, and never echo it.

## Adding or changing a secret

1. Decide the **environment scope** (`ci`, `release`, or repo-wide). Default to
   the narrowest.
2. Add it in GitHub (Settings → Secrets and variables → Actions) or via the
   Paperclip secret API, depending on the consumer.
3. Record it in the catalog table above (name, scope, purpose, store) in the
   same PR — the table is the register, not the value.
4. Reference it in the workflow as `${{ secrets.NAME }}` and mark any step that
   consumes it appropriately. Never `echo` or `env`-dump a secret.
5. Trigger a run that exercises it; confirm success without printing the value.

## Fork and third-party PR safety

- PRs from forks do not receive secrets (GitHub default). Keep it that way:
  never switch to `pull_request_target` with checkout of untrusted code.
- Workflows that need secrets must run on `push` to a trusted branch or on a tag.

## Incident: suspected exposure

Follow the "Secret exposure" playbook in [runbook.md](runbook.md): revoke,
rotate, audit, and document. Rotation invalidates the leaked value so history
rewrites are not required.
