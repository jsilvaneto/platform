# Status do Projeto (STATUS.md)

Este documento registra o checklist de funcionalidades, fases de implementação e governança de releases do aplicativo móvel **Platform**.

---

## 🚀 Fases do Projeto

| Fase | Escopo | Status |
| :--- | :--- | :--- |
| **Fase 1: Fundação Mobile & Governança** | Clean Architecture, Gradle Kotlin DSL, Version Catalog, Hilt, Room, Compose BOM, Material 3 e IA | **100% CONCLUÍDO** |
| **Fase 2: Módulos de Domínio & Features** | Implementação de fluxos completos de autenticação, telas de perfil e listagens complexas | **EM ANDAMENTO** |
| **Fase 3: Otimizações & Release Google Play** | ProGuard/R8, CI/CD com Fastlane ou GitHub Actions e assinatura de bundle AAB | **PLANEJADO** |

---

## 📋 Checklist de Funcionalidades

### Fase 1: Fundação Mobile Full-Stack
- [x] Configuração raiz do Gradle com Kotlin DSL (`build.gradle.kts` e `settings.gradle.kts`).
- [x] Version Catalog centralizado em `gradle/libs.versions.toml`.
- [x] Arquitetura Clean Architecture dividida em Presentation, Domain, Data e Core.
- [x] Injeção de dependências desacoplada com Dagger Hilt (`@HiltAndroidApp`, `AppModule`, `RepositoryModule`).
- [x] Configuração de persistência local com Room Database (`PlatformDatabase`, `ItemDao`, `ItemEntity`).
- [x] Configuração de comunicação com APIs REST via Retrofit 2 + OkHttp Logging Interceptor.
- [x] Camada de UI reativa em Jetpack Compose com Material 3 (`PlatformTheme`, `Color`, `Type`, `PlatformAppBar`).
- [x] Suporte nativo a Tema Claro e Tema Escuro (*Dark Mode*).
- [x] Navegação reativa com Navigation Compose e rotas tipadas (`Screen.kt`, `NavGraph.kt`).
- [x] Tela principal `HomeScreen` com suporte a estados de carregamento, vazio, erro e inclusão de itens com `ModalBottomSheet`.
- [x] Suíte de testes unitários com MockK, Turbine e Coroutines Test (`GetItemsUseCaseTest`, `HomeViewModelTest`).
- [x] Script de automação de versionamento com incremento de `versionCode` e `versionName` (`scripts/bump-version.sh`).
- [x] Catálogo de regras e skills para agentes de IA em `.agents/` e manuais em `.ai/`.

### Fase 2: Features de Produção
- [ ] Integração de autenticação segura e armazenamento de tokens no EncryptedSharedPreferences / DataStore.
- [ ] Implementação de sincronização em segundo plano via WorkManager.
- [ ] Notificações push via Firebase Cloud Messaging (FCM).
- [ ] Instrumentação de testes de tela Compose (`ComposeTestRule`).
