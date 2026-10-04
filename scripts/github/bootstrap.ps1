# Bootstrap the StratixOps engine GitHub repository and its protection rules.
# Idempotent: safe to re-run. Requires an authenticated GitHub CLI (`gh`).
#
# Usage:
#   scripts/github/bootstrap.ps1 [-Owner stratixops] [-Repo stratix-engine] [-Public]
#
# Optional: set $env:REVIEWER_IDS to a comma-separated list of GitHub user IDs
# for the release-environment required reviewers.

[CmdletBinding()]
param(
    [string]$Owner = $(if ($env:STRATIX_OWNER) { $env:STRATIX_OWNER } else { "stratixops" }),
    [string]$Repo = $(if ($env:STRATIX_REPO) { $env:STRATIX_REPO } else { "stratix-engine" }),
    [switch]$Public
)

$ErrorActionPreference = "Stop"
$Branch = "main"
$Visibility = if ($Public) { "--public" } else { "--private" }

if (-not (Get-Command gh -ErrorAction SilentlyContinue)) {
    throw "gh CLI not found. Install it from https://cli.github.com/."
}

gh auth status *> $null
if ($LASTEXITCODE -ne 0) {
    throw "gh is not authenticated. Run 'gh auth login' (or grant the Paperclip GitHub connection) first."
}

$Full = "$Owner/$Repo"
Write-Host "==> Target repository: $Full"

Write-Host "==> Creating repository if missing"
gh repo view $Full *> $null
if ($LASTEXITCODE -eq 0) {
    Write-Host "    exists already"
} else {
    gh repo create $Full $Visibility --disable-wiki
    Write-Host "    created"
}

Write-Host "==> Configuring origin"
git remote get-url origin *> $null
if ($LASTEXITCODE -eq 0) {
    git remote set-url origin "https://github.com/$Full.git"
} else {
    git remote add origin "https://github.com/$Full.git"
}
git remote -v

Write-Host "==> Pushing $Branch and tags"
git push -u origin $Branch
git push origin --tags

Write-Host "==> Enabling automatic head-branch deletion"
gh api -X PATCH "repos/$Full" -f delete_branch_on_merge=true *> $null

Write-Host "==> Applying $Branch branch protection"
$protection = @{
    required_status_checks = @{
        strict   = $true
        contexts = @(
            "Build & Test (ubuntu-latest, JDK 17)",
            "Build & Test (windows-latest, JDK 17)",
            "GPU / display tests (Linux, xvfb)"
        )
    }
    enforce_admins                = $false
    required_pull_request_reviews = @{
        dismiss_stale_reviews           = $true
        require_code_owner_reviews      = $false
        required_approving_review_count = 1
    }
    restrictions                    = $null
    required_linear_history         = $true
    allow_force_pushes              = $false
    allow_deletions                 = $false
    required_conversation_resolution = $true
}
$protectionFile = New-TemporaryFile
$protection | ConvertTo-Json -Depth 6 | Set-Content -Path $protectionFile -Encoding utf8
gh api -X PUT "repos/$Full/branches/$Branch/protection" `
    -H "Accept: application/vnd.github+json" --input $protectionFile *> $null
Remove-Item $protectionFile -Force
Write-Host "    protected"

Write-Host "==> Ensuring release environment"
$envBody = "{}"
if ($env:REVIEWER_IDS) {
    $reviewers = $env:REVIEWER_IDS.Split(",") | Where-Object { $_.Trim() } | ForEach-Object {
        @{ type = "User"; id = [int]$_.Trim() }
    }
    $envBody = @{ reviewers = @($reviewers) } | ConvertTo-Json -Depth 4
    Write-Host "    configuring required reviewers: $($env:REVIEWER_IDS)"
} else {
    Write-Host "    creating without required reviewers (set `$env:REVIEWER_IDS to add them)"
}
$envFile = New-TemporaryFile
$envBody | Set-Content -Path $envFile -Encoding utf8
gh api -X PUT "repos/$Full/environments/release" `
    -H "Accept: application/vnd.github+json" --input $envFile *> $null
Remove-Item $envFile -Force

Write-Host ""
Write-Host "Done. $Full is bootstrapped."
Write-Host "Next: open Actions and confirm CI is green on $Branch."
