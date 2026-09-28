---
name: security-guard
description: >-
  Use this skill to enforce Android security best practices, secret isolation in local.properties,
  biometric authentication, and secure device data storage.
---

# Skill: Security Guard (Segurança no Android)

Esta skill define as diretrizes de segurança e integridade de dados para o aplicativo **Platform**.

---

## 1. Proteção de Acesso por Biometria
- O app conta com bloqueio nativo via `BiometricAuthManager` utilizando a API oficial do `androidx.biometric`.
- O estado de ativação é persistido no `PreferencesManager` (DataStore).
- Ao habilitar a biometria, o aplicativo apresenta uma tela de bloqueio (`BiometricLockOverlay`) impedindo a visualização dos dados até a autenticação por impressão digital, reconhecimento facial ou PIN/senha do dispositivo.

---

## 2. Isolamento de Arquivos Locais e Segredos
- **Proibição de Chaves Hardcoded**: NUNCA declare credenciais ou dados sensíveis em arquivos versionados.
- Arquivos de configuração de máquina (`local.properties`) devem estar sempre presentes no `.gitignore`.
- Segredos de build devem ser injetados via `buildConfigField`.

---

## 3. Integridade do Banco de Dados Local
- O banco Room reside exclusivamente na sandbox privada do aplicativo (`/data/data/com.platform.app/databases/`), inacessível a outros aplicativos sem permissões root.
- Exports de backup gerados não devem expor tokens ou configurações privadas do sistema operacional.
