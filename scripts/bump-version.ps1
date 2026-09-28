param (
    [Parameter(Position=0, Mandatory=$false)]
    [string]$Target
)

$ProjectRoot = Resolve-Path "$PSScriptRoot\.."
$VersionFile = Join-Path $ProjectRoot "VERSION"
$ChangelogFile = Join-Path $ProjectRoot "CHANGELOG.md"

$CurrentVersion = if (Test-Path $VersionFile) {
    (Get-Content $VersionFile -Raw).Trim()
} else {
    "1.0.0"
}

if (-not $Target) {
    Write-Host "=================================================================" -ForegroundColor Cyan
    Write-Host "  🏷️  SCRIPT DE ATUALIZAÇÃO DE VERSÃO ANDROID (POWERSHELL)" -ForegroundColor Cyan
    Write-Host "=================================================================" -ForegroundColor Cyan
    Write-Host "  Versão atual (versionName): v$CurrentVersion"
    Write-Host ""
    Write-Host "  Uso:"
    Write-Host "    .\scripts\bump-version.ps1 patch    (ex: 1.2.0 -> 1.2.1)"
    Write-Host "    .\scripts\bump-version.ps1 minor    (ex: 1.2.0 -> 1.3.0)"
    Write-Host "    .\scripts\bump-version.ps1 major    (ex: 1.2.0 -> 2.0.0)"
    Write-Host "    .\scripts\bump-version.ps1 1.3.0    (define versão exata)"
    Write-Host "=================================================================" -ForegroundColor Cyan
    exit 1
}

$parts = $CurrentVersion.Split('.')
$major = [int]$parts[0]
$minor = [int]$parts[1]
$patch = if ($parts.Length -gt 2) { [int]$parts[2] } else { 0 }

switch ($Target.ToLower()) {
    "patch" {
        $patch++
        $NewVersion = "$major.$minor.$patch"
    }
    "minor" {
        $minor++
        $NewVersion = "$major.$minor.0"
    }
    "major" {
        $major++
        $NewVersion = "$major.0.0"
    }
    default {
        $NewVersion = $Target.Trim()
    }
}

Write-Host ">> Elevando versão do app: v$CurrentVersion -> v$NewVersion" -ForegroundColor Green

# 1. Atualiza arquivo VERSION
Set-Content -Path $VersionFile -Value $NewVersion -NoNewline
Write-Host "   [✓] Atualizado: VERSION -> $NewVersion" -ForegroundColor Green

# 2. Insere esqueleto no CHANGELOG.md caso não exista
$Today = Get-Date -Format "yyyy-MM-dd"
$ChangelogContent = Get-Content -Path $ChangelogFile -Raw
if ($ChangelogContent -notmatch "## \[$NewVersion\]") {
    $Template = @"
## [$NewVersion] - $Today

### 🚀 Melhorias
- Descreva as melhorias do aplicativo aqui...

### 🛠️ Correções
- Descreva as correções aqui...

---

"@
    $NewChangelog = $ChangelogContent -replace "(?m)(^## \[)", "$Template`$1"
    Set-Content -Path $ChangelogFile -Value $NewChangelog
    Write-Host "   [✓] Template adicionado ao CHANGELOG.md" -ForegroundColor Green
}

Write-Host "=================================================================" -ForegroundColor Cyan
Write-Host "  ✅ Versão v$NewVersion configurada com sucesso!" -ForegroundColor Green
Write-Host "  O Gradle lerá automaticamente de VERSION no próximo build/execução."
Write-Host "=================================================================" -ForegroundColor Cyan
