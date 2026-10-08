#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PRESENTATION_DIR="$ROOT_DIR/app/src/main/java/com/platform/app/presentation"
THEME_DIR="$PRESENTATION_DIR/theme"

FORBIDDEN_REGEX='Color\(0x[0-9a-fA-F]+\)|Color\.(White|Black|Red|Green|Blue|Yellow|Cyan|Magenta|Gray|DarkGray|LightGray)'

VIOLATIONS=()

while IFS= read -r file; do
    if [[ "$file" == "$THEME_DIR"* ]]; then
        continue
    fi
    while IFS= read -r line; do
        trimmed="$(echo "$line" | sed -e 's/^[[:space:]]*//')"
        if [[ "$trimmed" =~ ^// ]] || [[ "$trimmed" =~ ^/\* ]] || [[ "$trimmed" =~ ^\* ]]; then
            continue
        fi
        if echo "$line" | grep -qE "$FORBIDDEN_REGEX"; then
            rel_path="${file#"$ROOT_DIR/"}"
            VIOLATIONS+=("$rel_path: $trimmed")
        fi
    done < "$file"
done < <(find "$PRESENTATION_DIR" -type f -name "*.kt")

if [ ${#VIOLATIONS[@]} -gt 0 ]; then
    echo "FAILED: Encontradas ${#VIOLATIONS[@]} violação(ões) de cor literal fora de presentation/theme:"
    for v in "${VIOLATIONS[@]}"; do
        echo "  - $v"
    done
    echo ""
    echo "Regra: Cores literais são proibidas fora do pacote theme."
    echo "Utilize MaterialTheme.colorScheme.* ou tokens em PlatformColorPalette / ThemePreviewColors / CardSkinColors."
    exit 1
else
    echo "SUCCESS: Todas as telas em presentation respeitam estritamente os tokens do tema (0 violações encontradas)."
    exit 0
fi
