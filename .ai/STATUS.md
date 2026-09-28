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
| **Fase 5: Backup Local & Exportação de Dados** | Exportação e restauração local via JSON/SQLite com Android ShareSheet | **EM ANDAMENTO** |

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
- [x] Navegação moderna com Navigation Compose e menu lateral categorizado (`AppDrawer.kt`, `NavGraph.kt`).

### Módulo de Contas a Pagar e Despesas
- [x] Gestão monetária estrita em inteiros de centavos (`amountCents: Long`) via `CurrencyUtils`.
- [x] Utilitário de manipulação temporal e competências mensais via `DateUtils`.
- [x] Modelagem relacional no Room: `BillEntity`, `BillInstallmentEntity`, `CategoryEntity`, `SubcategoryEntity`, `ContactEntity`, `FinancialAccountEntity`, `PaymentMethodEntity`, `BudgetEntity`, `GoalEntity`.
- [x] Auto-seeding inteligente de categorias, formas de pagamento e contas de referência.
- [x] Divisão matemática precisa de centavos com resto na primeira parcela (`CalculateInstallmentsUseCase`).
- [x] Tela **Dashboard Financeiro** remodelada alinhada ao Wallet: KPIs macro (Custo Fixo Recorrente, Saldo Devedor Parcelado, Próximos 7 Dias e Pontualidade), Mês Vigente sem seletores redundantes e alternância de visualização (Diagnóstico vs Curva de Desoneração Preditiva).
- [x] Tela **Registros (Bills)** com busca inline, chips de períodos rápidos (Este Mês, Próximos 30d, Atrasadas, Todas) e eliminação completa de MonthSelector.
- [x] Tela **Recorrentes e Parcelados** com cálculo corrigido de progresso para parcelamentos e visão de compromissos mensais.
- [x] Tela **Estatísticas** com análises comparativas e histórico.
- [x] Tela **Orçamentos (Budgets)** com tetos por categoria e barra de progresso de consumo.
- [x] Tela **Metas (Goals)** com objetivos financeiros de economia.
- [x] Tela **Contatos** com favorecidos, histórico de despesas e atalho de liquidação.
- [x] Tela **Cadastros Base (Management)** em abas: Contas de Referência, Formas de Pagamento e Categorias/Subcategorias.

### Governança e Skills de IA
- [x] Saneamento completo de código zumbi: remoção de todo o CRUD de `PlatformItem` e `PlatformApiService`.
- [x] Remoção de telas órfãs (`CategoriesScreen`, `BottomNavBar`).
- [x] Catálogo de 10 skills ativas em `.agents/skills/` (incluindo `room-database-and-migrations` e `offline-backup-and-export`).
- [x] Alinhamento canônico de manuais em `.ai/` e `AGENT_RULES.md`.

---

## 🎯 Próximos Passos
- [ ] Implementar fluxo de exportação de dados em JSON local e restauração via SAF.
- [ ] Otimizar queries agregadas de dashboard no `BillInstallmentDao`.
