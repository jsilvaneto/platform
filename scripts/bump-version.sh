#!/usr/bin/env bash
set -e

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
VERSION_FILE="$PROJECT_ROOT/VERSION"
APP_BUILD_GRADLE="$PROJECT_ROOT/app/build.gradle.kts"
CHANGELOG_FILE="$PROJECT_ROOT/CHANGELOG.md"

CURRENT_VERSION=$(cat "$VERSION_FILE" 2>/dev/null | tr -d ' \r\n' || echo "1.0.0")

if [ -z "$1" ]; then
    echo "================================================================="
    echo "  🏷️  SCRIPT DE ATUALIZAÇÃO DE VERSÃO ANDROID (BUMP VERSION)"
    echo "================================================================="
    echo "  Versão atual (versionName): v$CURRENT_VERSION"
    echo ""
    echo "  Uso:"
    echo "    ./scripts/bump-version.sh patch    (ex: 1.0.0 -> 1.0.1)"
    echo "    ./scripts/bump-version.sh minor    (ex: 1.0.0 -> 1.1.0)"
    echo "    ./scripts/bump-version.sh major    (ex: 1.0.0 -> 2.0.0)"
    echo "    ./scripts/bump-version.sh 1.2.0    (define versão exata)"
    echo "================================================================="
    exit 1
fi

IFS='.' read -r MAJOR MINOR PATCH <<< "$CURRENT_VERSION"
TARGET="$1"

if [ "$TARGET" = "patch" ]; then
    NEW_PATCH=$((PATCH + 1))
    NEW_VERSION="$MAJOR.$MINOR.$NEW_PATCH"
elif [ "$TARGET" = "minor" ]; then
    NEW_MINOR=$((MINOR + 1))
    NEW_VERSION="$MAJOR.$NEW_MINOR.0"
elif [ "$TARGET" = "major" ]; then
    NEW_MAJOR=$((MAJOR + 1))
    NEW_VERSION="$NEW_MAJOR.0.0"
else
    NEW_VERSION="$TARGET"
fi

echo ">> Elevando versão do app: v$CURRENT_VERSION -> v$NEW_VERSION"

# 1. Atualiza arquivo VERSION
echo "$NEW_VERSION" > "$VERSION_FILE"
echo "   [✓] Atualizado: VERSION -> $NEW_VERSION"

# 2. Atualiza app/build.gradle.kts (versionCode e versionName)
if [ -f "$APP_BUILD_GRADLE" ]; then
    CURRENT_CODE=$(grep -E 'versionCode\s*=\s*[0-9]+' "$APP_BUILD_GRADLE" | head -n1 | grep -o -E '[0-9]+' || echo "1")
    NEW_CODE=$((CURRENT_CODE + 1))
    sed -i -E "s/versionCode\s*=\s*[0-9]+/versionCode = $NEW_CODE/" "$APP_BUILD_GRADLE"
    sed -i -E "s/versionName\s*=\s*\"[^\"]+\"/versionName = \"$NEW_VERSION\"/" "$APP_BUILD_GRADLE"
    echo "   [✓] Atualizado: app/build.gradle.kts -> versionCode=$NEW_CODE, versionName=\"$NEW_VERSION\""
fi

# 3. Insere esqueleto no CHANGELOG.md caso não exista
TODAY=$(date +"%Y-%m-%d")
if ! grep -q "## \[$NEW_VERSION\]" "$CHANGELOG_FILE"; then
    echo "   [*] Adicionando esqueleto da versão $NEW_VERSION ao CHANGELOG.md..."
    TEMP_CHANGELOG=$(mktemp)
    awk -v ver="$NEW_VERSION" -v dt="$TODAY" '
        /^## \[/ && !inserted {
            print "## [" ver "] - " dt "\n"
            print "### 🚀 Melhorias"
            print "- Descreva as melhorias do aplicativo aqui...\n"
            print "### 🛠️ Correções"
            print "- Descreva as correções aqui...\n"
            print "---\n"
            inserted = 1
        }
        { print }
    ' "$CHANGELOG_FILE" > "$TEMP_CHANGELOG"
    mv "$TEMP_CHANGELOG" "$CHANGELOG_FILE"
    echo "   [✓] Template adicionado ao CHANGELOG.md"
fi

echo ""
echo "================================================================="
echo "  ✅ Versão v$NEW_VERSION (code $NEW_CODE) configurada com sucesso!"
echo "================================================================="
echo "  Próximos passos sugeridos:"
echo "  1. Edite o CHANGELOG.md detalhando as novidades da versão."
echo "  2. Faça o commit e crie a tag no Git:"
echo "     git add VERSION app/build.gradle.kts CHANGELOG.md"
echo "     git commit -m \"chore(release): v$NEW_VERSION\""
echo "     git tag -a \"v$NEW_VERSION\" -m \"Release v$NEW_VERSION\""
echo "     git push origin main --tags"
echo "================================================================="
