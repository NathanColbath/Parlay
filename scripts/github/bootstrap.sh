#!/usr/bin/env bash
#
# Bootstrap the StratixOps engine GitHub repository and its protection rules.
# Idempotent: safe to re-run. Requires an authenticated GitHub CLI (`gh`).
#
# Usage:
#   scripts/github/bootstrap.sh [--owner OWNER] [--repo REPO] [--public]
#
# Env:
#   REVIEWER_IDS  Comma-separated GitHub user IDs for the release environment
#                 required reviewers (optional). Find IDs with:
#                   gh api users/<login> --jq .id
#
set -euo pipefail

OWNER="${STRATIX_OWNER:-stratixops}"
REPO="${STRATIX_REPO:-stratix-engine}"
VISIBILITY="--private"
BRANCH="main"

while [ "$#" -gt 0 ]; do
  case "$1" in
    --owner) OWNER="$2"; shift 2 ;;
    --repo) REPO="$2"; shift 2 ;;
    --public) VISIBILITY="--public"; shift ;;
    *) echo "unknown argument: $1" >&2; exit 2 ;;
  esac
done

command -v gh >/dev/null 2>&1 || { echo "ERROR: gh CLI not found." >&2; exit 1; }
gh auth status >/dev/null 2>&1 || {
  echo "ERROR: gh is not authenticated. Run 'gh auth login' (or grant the Paperclip GitHub connection) first." >&2
  exit 1
}

FULL="${OWNER}/${REPO}"
echo "==> Target repository: ${FULL}"

echo "==> Creating repository if missing"
if gh repo view "$FULL" >/dev/null 2>&1; then
  echo "    exists already"
else
  gh repo create "$FULL" "$VISIBILITY" --disable-wiki
  echo "    created"
fi

echo "==> Configuring origin"
if git remote get-url origin >/dev/null 2>&1; then
  git remote set-url origin "https://github.com/${FULL}.git"
else
  git remote add origin "https://github.com/${FULL}.git"
fi
git remote -v

echo "==> Pushing ${BRANCH} and tags"
git push -u origin "$BRANCH"
git push origin --tags || true

echo "==> Enabling automatic head-branch deletion"
gh api -X PATCH "repos/${FULL}" -f delete_branch_on_merge=true >/dev/null

echo "==> Applying ${BRANCH} branch protection"
PROTECTION_FILE="$(mktemp)"
cat > "$PROTECTION_FILE" <<'JSON'
{
  "required_status_checks": {
    "strict": true,
    "contexts": [
      "Build & Test (ubuntu-latest, JDK 17)",
      "Build & Test (windows-latest, JDK 17)",
      "GPU / display tests (Linux, xvfb)"
    ]
  },
  "enforce_admins": false,
  "required_pull_request_reviews": {
    "dismiss_stale_reviews": true,
    "require_code_owner_reviews": false,
    "required_approving_review_count": 1
  },
  "restrictions": null,
  "required_linear_history": true,
  "allow_force_pushes": false,
  "allow_deletions": false,
  "required_conversation_resolution": true
}
JSON
gh api -X PUT "repos/${FULL}/branches/${BRANCH}/protection" \
  -H "Accept: application/vnd.github+json" \
  --input "$PROTECTION_FILE" >/dev/null
rm -f "$PROTECTION_FILE"
echo "    protected"

echo "==> Ensuring release environment"
ENV_JSON="$(mktemp)"
if [ -n "${REVIEWER_IDS:-}" ]; then
  REVIEWERS_JSON="$(printf '%s' "${REVIEWER_IDS}" | tr ',' '\n' | sed 's/[[:space:]]//g' | grep -v '^$' \
    | jq -R '{type: "User", id: (. | tonumber)}' | jq -s '.')"
  printf '{"reviewers":%s}' "${REVIEWERS_JSON}" > "$ENV_JSON"
  echo "    configuring required reviewers: ${REVIEWER_IDS}"
else
  printf '{}' > "$ENV_JSON"
  echo "    creating without required reviewers (set REVIEWER_IDS to add them)"
fi
gh api -X PUT "repos/${FULL}/environments/release" \
  -H "Accept: application/vnd.github+json" --input "$ENV_JSON" >/dev/null || \
  echo "    WARN: could not configure environment; set it up in Settings -> Environments."
rm -f "$ENV_JSON"

echo
echo "Done. ${FULL} is bootstrapped."
echo "Next: open Actions and confirm CI is green on ${BRANCH}."
