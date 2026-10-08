# Script de verificação de conformidade com o Design System
# Garante que nenhuma cor literal (Color(0x...), Color.White, Color.Black, etc.) seja declarada fora de presentation/theme.

$ErrorActionPreference = "Stop"

$rootDir = Split-Path -Parent $PSScriptRoot
$presentationDir = Join-Path $rootDir "app\src\main\java\com\platform\app\presentation"
$themeDir = Join-Path $presentationDir "theme"

$forbiddenPatterns = @(
    '\bColor\(0x[0-9a-fA-F]+\)',
    '\bColor\.(White|Black|Red|Green|Blue|Yellow|Cyan|Magenta|Gray|DarkGray|LightGray)\b'
)

$violations = @()

Get-ChildItem -Path $presentationDir -Filter "*.kt" -Recurse | ForEach-Object {
    $file = $_
    if (-not $file.FullName.StartsWith($themeDir)) {
        $lines = Get-Content -Path $file.FullName
        for ($i = 0; $i -lt $lines.Count; $i++) {
            $line = $lines[$i]
            $trimmed = $line.Trim()
            if (-not $trimmed.StartsWith("//") -and -not $trimmed.StartsWith("/*") -and -not $trimmed.StartsWith("*")) {
                foreach ($pattern in $forbiddenPatterns) {
                    if ($line -match $pattern) {
                        $relPath = $file.FullName.Substring($rootDir.Length + 1)
                        $violations += "$($relPath):$($i + 1): $trimmed"
                        break
                    }
                }
            }
        }
    }
}

if ($violations.Count -gt 0) {
    Write-Host "FAILED: Encontradas $($violations.Count) violação(ões) de cor literal fora de presentation/theme:" -ForegroundColor Red
    foreach ($v in $violations) {
        Write-Host "  - $v" -ForegroundColor Red
    }
    Write-Host "`nRegra: Cores literais são proibidas fora do pacote theme." -ForegroundColor Yellow
    Write-Host "Utilize MaterialTheme.colorScheme.* ou tokens em PlatformColorPalette / ThemePreviewColors / CardSkinColors." -ForegroundColor Yellow
    exit 1
} else {
    Write-Host "SUCCESS: Todas as telas em presentation respeitam estritamente os tokens do tema (0 violações encontradas)." -ForegroundColor Green
    exit 0
}
