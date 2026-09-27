# Status do Projeto (STATUS.md)

Este documento registra o checklist de funcionalidades, fases de implementação e governança de releases do aplicativo móvel **Platform**.

---

## 🚀 Fases do Projeto

| Fase | Escopo | Status |
| :--- | :--- | :--- |
| **Fase 1: Fundação Mobile & Governança** | Clean Architecture, Gradle Kotlin DSL, Version Catalog, Hilt, Room, Compose BOM, Material 3 e IA | **100% CONCLUÍDO** |
| **Fase 2: Arquitetura 100% Offline-First & MVI** | Operação 100% local com Room, DataStore, MVI (UiAction/UiEffect), busca e filtros locais | **100% CONCLUÍDO** |
| **Fase 3: Features & Módulos Locais** | Novas telas de anotações, tarefas, categorias e dashboards com persistência Room | **EM ANDAMENTO** |
| **Fase 4: Sincronização Remota & Release** | Sincronização em nuvem opcional (WorkManager/API) e otimizações ProGuard/R8 | **PLANEJADO** |

---

## 📋 Checklist de Funcionalidades

### Fase 1 & 2: Fundação Mobile & Offline-First (MVI)
- [x] Configuração raiz do Gradle com Kotlin DSL (`build.gradle.kts` e `settings.gradle.kts`).
- [x] Version Catalog centralizado em `gradle/libs.versions.toml`.
- [x] Arquitetura Clean Architecture dividida em Presentation, Domain, Data e Core.
- [x] Padrão **MVI (Model-View-Intent)** implementado com `UiState`, `UiAction` e `UiEffect` (Channel bufferizado).
- [x] Operação **100% Offline-First**: O Room Database é a única fonte da verdade e o app opera perfeitamente sem internet.
- [x] Injeção de dependências desacoplada com Dagger Hilt (`@HiltAndroidApp`, `AppModule`, `RepositoryModule`).
- [x] Persistência local com Room Database (`PlatformDatabase`, `ItemDao`, `ItemEntity`).
- [x] Persistência de configurações locais e preferências via **AndroidX DataStore** (`PreferencesManager`).
- [x] Monitor reativo de conectividade em segundo plano (`NetworkMonitor` com `ConnectivityManager.NetworkCallback`).
- [x] Camada de UI reativa em Jetpack Compose com Material 3 (`PlatformTheme`, `Color`, `Type`, `PlatformAppBar`).
- [x] Suporte nativo a Tema Claro e Tema Escuro (*Dark Mode*).
- [x] Navegação reativa com Navigation Compose e rotas tipadas (`Screen.kt`, `NavGraph.kt`).
- [x] Tela principal `HomeScreen` com suporte a estados de carregamento, busca local em tempo real, filtros de status, badges, checkboxes e `ModalBottomSheet`.
- [x] Integração de `SnackbarHost` no `Scaffold` acionado via `HomeUiEffect.ShowSnackbar`.
- [x] Suíte de testes unitários com MockK, Turbine e Coroutines Test (`GetItemsUseCaseTest`, `HomeViewModelTest`).
- [x] Script de automação de versionamento com incremento de `versionCode` e `versionName` (`scripts/bump-version.sh`).
- [x] Catálogo de regras e skills para agentes de IA em `.agents/` e manuais em `.ai/`.

### Fases Futuras (Planejado)
- [ ] Exportação e importação de backup local de dados (JSON/SQLite).
- [ ] Sincronização remota opcional via WorkManager quando o usuário optar por conectar um servidor.
- [ ] Instrumentação de testes visuais de tela Compose (`ComposeTestRule`).
