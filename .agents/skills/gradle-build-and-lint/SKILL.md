---
name: gradle-build-and-lint
description: >-
  Use this skill to execute Gradle tasks, build APKs, run unit tests,
  and check Android lint reports.
---

# Skill: Gradle Build & Lint

Esta skill centraliza os comandos oficiais para validar a saúde e integridade do projeto Android.

## 1. Comandos de Compilação e Build
- **Compilar APK de Debug**:
  ```bash
  ./gradlew assembleDebug
  ```
- **Compilar e verificar código sem gerar APK final**:
  ```bash
  ./gradlew check
  ```
- **Limpar build cache**:
  ```bash
  ./gradlew clean
  ```

## 2. Testes e Qualidade de Código
- **Executar todos os testes unitários da JVM**:
  ```bash
  ./gradlew test
  ```
- **Executar testes unitários do módulo app com relatório detalhado**:
  ```bash
  ./gradlew :app:testDebugUnitTest --info
  ```
- **Executar análise estática com Android Lint**:
  ```bash
  ./gradlew lintDebug
  ```

## 3. Gestão de Versão
- Elevar versão de patch: `./scripts/bump-version.sh patch`
- Elevar versão minor: `./scripts/bump-version.sh minor`
- Elevar versão major: `./scripts/bump-version.sh major`
