# Status do Projeto (STATUS.md)

Este documento registra o checklist de funcionalidades, fases de implementação e governança de releases do aplicativo móvel **Platform**.

---

## 🚀 Fases do Projeto

| Fase | Escopo | Status |
| :--- | :--- | :--- |
| **Fase 1: Fundação Mobile & Governança** | Clean Architecture, Gradle Kotlin DSL, Version Catalog, Hilt, Room, Material 3 e IA | **100% CONCLUÍDO** |
| **Fase 2: Arquitetura 100% Offline-First & MVI** | Operação 100% local com Room, DataStore, MVI, biometria e navegação AppDrawer | **100% CONCLUÍDO** |
| **Fase 3: Módulo de Contas a Pagar & Planejamento** | Contas Avulsas, Parceladas, Recorrentes, Orçamentos, Metas, Estatísticas e Cadastros Base | **100% CONCLUÍDO** |
| **Fase 4: Saneamento Arquitetural & Skills de IA** | Limpeza de código zumbi/boilerplate, correção de bugs de recorrência, transações e skills | **100% CONCLUÍDO** |
| **Fase 5: Backup Local & Exportação de Dados** | Exportação e restauração local via JSON com SAF, transações atômicas Room e ShareSheet | **100% CONCLUÍDO** (v1.3.0) |
| **Fase 6: Harmonização Visual Global (4 Etapas)** | Padrão de Detalhes com BottomSheet, 3 pontos, sem botões inline e blocos ergonômicos | **100% CONCLUÍDO** (v1.3.1) |
| **Fase 7: Sincronização em Nuvem (API Bidirecional)** | Backend remoto, motor de sync offline-first, backup automático no banco de dados | **PLANEJADA (Fase 2)** |

---

## 📋 Checklist de Funcionalidades Implementadas

### Fundação, Arquitetura e MVI
- [x] Configuração raiz do Gradle com Kotlin DSL (`build.gradle.kts` e `settings.gradle.kts`).
- [x] Version Catalog centralizado em `gradle/libs.versions.toml`.
- [x] Arquitetura Clean Architecture: Presentation, Domain, Data e Core.
- [x] Padrão **MVI (Model-View-Intent)** implementado com `UiState`, `UiAction` e `UiEffect` (Channel bufferizado).
- [x] Operação **100% Offline-First**: O Room Database é a única fonte da verdade e o app opera sem internet.
- [x] Injeção de dependências com Dagger Hilt (`@HiltAndroidApp`, `AppModule`, `RepositoryModule`).
- [x] Persistência local com Room Database (`PlatformDatabase`) com transações atômicas (`database.withTransaction`).
- [x] Persistência de configurações e preferências via **AndroidX DataStore** (`PreferencesManager`).
- [x] Bloqueio e segurança com **AndroidX Biometric** (`BiometricAuthManager`, `BiometricLockOverlay`).
- [x] Design System Material 3 com Dark Mode nativo (`PlatformTheme`, `Color`, `Type`).
- [x] Navegação moderna com Navigation Compose e menu lateral despoluído (`AppDrawer.kt`, `NavGraph.kt`).

### Módulo de Contas a Pagar e Despesas
- [x] Gestão monetária estrita em inteiros de centavos (`amountCents: Long`) via `CurrencyUtils`.
- [x] Utilitário de manipulação temporal e competências mensais via `DateUtils`.
- [x] Modelagem relacional no Room: `BillEntity`, `BillInstallmentEntity`, `CategoryEntity`, `SubcategoryEntity`, `ContactEntity`, `FinancialAccountEntity`, `PaymentMethodEntity`, `BudgetEntity`, `GoalEntity`.
- [x] Auto-seeding inteligente de categorias, formas de pagamento e contas de referência.
- [x] Divisão matemática precisa de centavos com resto na primeira parcela (`CalculateInstallmentsUseCase`).
- [x] Tela **Dashboard Financeiro**: KPIs macro (Custo Fixo Recorrente, Saldo Devedor Parcelado, Próximos 7 Dias e Pontualidade), visualização preditiva e diagnóstico.
- [x] Tela **Registros (Bills)**: Cards minimalistas, chips de períodos rápidos, busca inline e BottomSheet completo de vínculos e liquidação.
- [x] Tela **Recorrentes e Parcelados**: Cards limpos de contratos e BottomSheet com amortização e quitação interativa por parcela.
- [x] Tela **Estatísticas**: Histórico de pagamentos e pontualidade.
- [x] Tela **Orçamentos (Budgets)**: BottomSheet de comparação teto vs realizado e ajuste rápido de limites.
- [x] Tela **Metas (Goals)**: BottomSheet de objetivos com indicador visual de aportes e progresso.
- [x] Tela **Contatos**: Detalhes do contato com busca automática de CEP, histórico financeiro e exclusão segura nos 3 pontos.
- [x] Telas **Contas, Formas de Pagamento e Categorias**: Acessos diretos independentes no menu lateral e em Configurações, com BottomSheets de detalhes.

### Backup & Recuperação de Dados (v1.3.0)
- [x] DTO unificado `BackupDataDto` com versionamento e timestamp.
- [x] Restauração atômica via transação Room (`database.withTransaction`) com preservação de integridade referencial.
- [x] Integração com Storage Access Framework (SAF) nativo (`CreateDocument` e `OpenDocument`).
- [x] Compartilhamento direto com mensageiros via `FileProvider` (`file_paths.xml`).
- [x] Painel de status com indicador de data/hora do último backup e diálogo de advertência prévia para restaurações.

### Governança e Skills de IA
- [x] Saneamento completo de código zumbi/legado.
- [x] Catálogo de 10 skills ativas em `.agents/skills/`.
- [x] Script de versionamento móvel corrigido e automatizado (`scripts/bump-version.ps1` e `scripts/bump-version.sh`).
- [x] Regra mandatória de governança e sincronização de versão em `.agents/rules/governance_and_versioning.md`.

---

## 🎯 Próximos Passos
- [ ] Fase 2: Estruturação da API remota e sincronização bidirecional offline-first com banco em nuvem.
