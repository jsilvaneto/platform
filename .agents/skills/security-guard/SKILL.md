---
name: security-guard
description: >-
  Use this skill to enforce Android security best practices, secret isolation in local.properties,
  ProGuard/R8 obfuscation, and secure data storage.
---

# Skill: Security Guard (Segurança no Android)

Esta skill define as diretrizes mandatórias de segurança para o aplicativo móvel **Platform**.

## 1. Gestão de Segredos e Chaves de API
- **Proibição de Chaves Hardcoded**:
  - NUNCA declare chaves de API, senhas ou tokens privados diretamente no código Kotlin ou em arquivos XML.
  - Armazene credenciais sensíveis no arquivo `local.properties`:
    ```properties
    API_KEY=meu_segredo_local
    ```
  - Injete no `buildConfigField` do `build.gradle.kts`:
    ```kotlin
    val apiKey = project.rootProject.file("local.properties").let { file ->
        if (file.exists()) {
            java.util.Properties().apply { load(file.inputStream()) }.getProperty("API_KEY") ?: ""
        } else ""
    }
    buildConfigField("String", "API_KEY", "\"$apiKey\"")
    ```

## 2. Armazenamento Seguro no Aparelho
- Para dados sensíveis (tokens JWT de autenticação), utilize **EncryptedSharedPreferences** ou **Proto DataStore** com criptografia do Android Keystore.
- O Room Database deve conter apenas dados apropriados para cache offline.

## 3. Comunicação Segura
- Todas as comunicações HTTP devem utilizar estritamente protocolo seguro `https://`.
- Mantenha certificados TLS atualizados e ative Certificate Pinning em ambientes de alta segurança.

## 4. Ofuscação e Proteção de Código
- Em builds de release, mantenha o ProGuard/R8 habilitado (`isMinifyEnabled = true` e `isShrinkResources = true`).
