# Changelog - Platform (Android App)

Todas as alterações notáveis neste projeto serão documentadas neste arquivo.
O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/) e este projeto segue [Versionamento Semântico](https://semver.org/lang/pt-BR/).

---

## [1.0.0] - 2026-09-26

### 🚀 Melhorias
- Fundação da arquitetura base do aplicativo Android em Clean Architecture (Presentation, Domain, Data, Core).
- Configuração do Android Gradle Plugin com Kotlin DSL (`build.gradle.kts` e Version Catalog `libs.versions.toml`).
- Integração da stack moderna de UI com Jetpack Compose BOM, Material 3 e suporte a Tema Claro e Escuro.
- Injeção de dependências desacoplada preparada com Hilt.
- Configuração de persistência local com Room Database e comunicação remota com Retrofit/OkHttp.
- Implementação de fluxos reativos com Kotlin Coroutines e StateFlow.
- Estrutura completa de governança de IA (`AGENT_RULES.md`, `.ai/` e `.agents/` com skills e regras).
- Automação de versionamento móvel sincronizando `versionCode`, `versionName` e `CHANGELOG.md`.

---
