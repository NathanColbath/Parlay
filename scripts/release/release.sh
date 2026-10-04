#!/usr/bin/env bash
#
# Cut a release: validate, promote the changelog, bump VERSION, tag, push.
# Pushing the annotated tag triggers .github/workflows/release.yml.
#
# Usage:
#   scripts/release/release.sh <version> [--next <next-dev-version>] [--no-verify] [--dry-run]
#
# Examples:
#   scripts/release/release.sh 0.1.0
#   scripts/release/release.sh 0.2.0-rc.1
#   scripts/release/release.sh 0.1.0 --no-verify --dry-run
#
set -euo pipefail

die() { echo "ERROR: $*" >&2; exit 1; }
info() { echo "==> $*"; }

[ "$#" -ge 1 ] || die "usage: release.sh <version> [--next <v>] [--no-verify] [--dry-run]"

VERSION="$1"; shift
NEXT=""
VERIFY=1
DRY_RUN=0

while [ "$#" -gt 0 ]; do
  case "$1" in
    --next) NEXT="$2"; shift 2 ;;
    --no-verify) VERIFY=0; shift ;;
    --dry-run) DRY_RUN=1; shift ;;
    *) die "unknown argument: $1" ;;
  esac
done

case "$VERSION" in
  *-SNAPSHOT) die "refusing to release a -SNAPSHOT version: $VERSION" ;;
esac
echo "$VERSION" | grep -Eq '^[0-9]+\.[0-9]+\.[0-9]+(-[0-9A-Za-z.]+)?$' \
  || die "version must be MAJOR.MINOR.PATCH (optionally with a pre-release suffix), got: $VERSION"

TAG="v${VERSION}"

# Default next development version: bump the patch.
if [ -z "$NEXT" ]; then
  BASE="${VERSION%%-*}"
  MAJOR="${BASE%%.*}"
  REST="${BASE#*.}"
  MINOR="${REST%%.*}"
  PATCH="${REST#*.}"
  NEXT="${MAJOR}.${MINOR}.$((PATCH + 1))-SNAPSHOT"
fi

ROOT="$(git rev-parse --show-toplevel 2>/dev/null)" || die "not inside a git repository"
cd "$ROOT"

info "Repository : $ROOT"
info "Release    : $TAG"
info "Next dev   : $NEXT"

git diff --quiet && git diff --cached --quiet || die "working tree is dirty; commit or stash first"
BRANCH="$(git rev-parse --abbrev-ref HEAD)"
[ "$BRANCH" = "main" ] || die "releases are cut from main (currently on '$BRANCH')"

if git remote get-url origin >/dev/null 2>&1; then
  info "Fetching origin"
  git fetch --tags --quiet origin || true
  if git rev-parse --verify --quiet "origin/main" >/dev/null; then
    if [ "$(git rev-parse HEAD)" != "$(git rev-parse origin/main)" ]; then
      die "local main is not in sync with origin/main; pull/rebase first"
    fi
  fi
else
  info "No origin remote configured; skipping sync check (local-only release)"
fi

git rev-parse --verify --quiet "refs/tags/${TAG}" >/dev/null && die "tag ${TAG} already exists"

if [ "$VERIFY" -eq 1 ]; then
  info "Running build + quality gate (./gradlew build)"
  ./gradlew build --init-script gradle/release-init.gradle.kts -Pstratix.version="$VERSION" --no-daemon --stacktrace
fi

if [ "$DRY_RUN" -eq 1 ]; then
  info "Dry run: no files changed, no commit, no tag, no push."
  exit 0
fi

DATE="$(date +%Y-%m-%d)"

info "Updating VERSION"
printf '%s\n' "$VERSION" > VERSION

info "Promoting CHANGELOG Unreleased -> $VERSION"
TMP="$(mktemp)"
awk -v ver="$VERSION" -v date="$DATE" '
  { print }
  /^## \[Unreleased\]/ && !done { print ""; print "## [" ver "] - " date; done = 1 }
' CHANGELOG.md > "$TMP"
mv "$TMP" CHANGELOG.md
grep -q "^## \[${VERSION}\]" CHANGELOG.md || die "failed to add CHANGELOG section for ${VERSION}"

info "Committing release"
git add VERSION CHANGELOG.md
git commit -m "chore(release): ${TAG}"

info "Tagging ${TAG}"
git tag -a "$TAG" -m "StratixOps Engine ${TAG}"

info "Beginning next development version ${NEXT}"
printf '%s\n' "$NEXT" > VERSION
git add VERSION
git commit -m "chore: begin ${NEXT}"

if git remote get-url origin >/dev/null 2>&1; then
  info "Pushing main and ${TAG}"
  git push origin main
  git push origin "$TAG"
else
  info "No origin remote; release commit and tag created locally."
fi

info "Done. ${TAG} released; the Release workflow will build and publish it."
