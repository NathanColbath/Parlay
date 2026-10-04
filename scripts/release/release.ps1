# Cut a release: validate, promote the changelog, bump VERSION, tag, push.
# Pushing the annotated tag triggers .github/workflows/release.yml.
#
# Usage:
#   scripts/release/release.ps1 -Version 0.1.0 [-Next 0.1.1-SNAPSHOT] [-NoVerify] [-DryRun]
#
[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][string]$Version,
    [string]$Next,
    [switch]$NoVerify,
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"

function Fail([string]$msg) { Write-Error $msg; exit 1 }
function Info([string]$msg) { Write-Host "==> $msg" }

if ($Version -like "*-SNAPSHOT") { Fail "refusing to release a -SNAPSHOT version: $Version" }
if ($Version -notmatch '^[0-9]+\.[0-9]+\.[0-9]+(-[0-9A-Za-z.]+)?$') {
    Fail "version must be MAJOR.MINOR.PATCH (optionally with a pre-release suffix), got: $Version"
}

$Tag = "v$Version"

if (-not $Next) {
    $base = ($Version -split '-')[0]
    $parts = $base.Split('.')
    $Next = "{0}.{1}.{2}-SNAPSHOT" -f $parts[0], $parts[1], ([int]$parts[2] + 1)
}

$root = (git rev-parse --show-toplevel 2>$null)
if ($LASTEXITCODE -ne 0 -or -not $root) { Fail "not inside a git repository" }
Set-Location $root

Info "Repository : $root"
Info "Release    : $Tag"
Info "Next dev   : $Next"

$dirty = (git status --porcelain)
if ($dirty) { Fail "working tree is dirty; commit or stash first" }

$branch = (git rev-parse --abbrev-ref HEAD)
if ($branch -ne "main") { Fail "releases are cut from main (currently on '$branch')" }

$hasOrigin = $true
git remote get-url origin *> $null
if ($LASTEXITCODE -ne 0) { $hasOrigin = $false }

if ($hasOrigin) {
    Info "Fetching origin"
    git fetch --tags --quiet origin 2>$null
    git rev-parse --verify --quiet "origin/main" *> $null
    if ($LASTEXITCODE -eq 0) {
        $head = (git rev-parse HEAD).Trim()
        $remote = (git rev-parse origin/main).Trim()
        if ($head -ne $remote) { Fail "local main is not in sync with origin/main; pull/rebase first" }
    }
} else {
    Info "No origin remote configured; skipping sync check (local-only release)"
}

git rev-parse --verify --quiet "refs/tags/$Tag" *> $null
if ($LASTEXITCODE -eq 0) { Fail "tag $Tag already exists" }

if (-not $NoVerify) {
    Info "Running build + quality gate (./gradlew build)"
    & ".\gradlew.bat" build --init-script gradle/release-init.gradle.kts "-Pstratix.version=$Version" --no-daemon --stacktrace
    if ($LASTEXITCODE -ne 0) { Fail "build/quality gate failed; release aborted" }
}

if ($DryRun) {
    Info "Dry run: no files changed, no commit, no tag, no push."
    exit 0
}

$date = (Get-Date -Format "yyyy-MM-dd")

Info "Updating VERSION"
Set-Content -Path VERSION -Value $Version -NoNewline
Add-Content -Path VERSION -Value ""

Info "Promoting CHANGELOG Unreleased -> $Version"
$lines = Get-Content CHANGELOG.md
$out = New-Object System.Collections.Generic.List[string]
$done = $false
foreach ($line in $lines) {
    $out.Add($line)
    if (-not $done -and $line -match '^## \[Unreleased\]') {
        $out.Add("")
        $out.Add("## [$Version] - $date")
        $done = $true
    }
}
Set-Content -Path CHANGELOG.md -Value $out
if (-not (Select-String -Path CHANGELOG.md -Pattern ("^## \[" + [regex]::Escape($Version) + "\]") -Quiet)) {
    Fail "failed to add CHANGELOG section for $Version"
}

Info "Committing release"
git add VERSION CHANGELOG.md
git commit -m "chore(release): $Tag" | Out-Null

Info "Tagging $Tag"
git tag -a $Tag -m "StratixOps Engine $Tag"

Info "Beginning next development version $Next"
Set-Content -Path VERSION -Value $Next -NoNewline
Add-Content -Path VERSION -Value ""
git add VERSION
git commit -m "chore: begin $Next" | Out-Null

if ($hasOrigin) {
    Info "Pushing main and $Tag"
    git push origin main
    git push origin $Tag
} else {
    Info "No origin remote; release commit and tag created locally."
}

Info "Done. $Tag released; the Release workflow will build and publish it."
