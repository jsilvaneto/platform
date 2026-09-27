# Status do Projeto (STATUS.md)

Este documento registra o checklist de funcionalidades, fases de implementação e governança de releases do aplicativo móvel **Platform**.

---

## 🚀 Fases do Projeto

| Fase | Escopo | Status |
| :--- | :--- | :--- |
| **Fase 1: Fundação Mobile & Governança** | Clean Architecture, Gradle Kotlin DSL, Version Catalog, Hilt, Room, Compose BOM, Material 3 e IA | **100% CONCLUÍDO** |
| **Fase 2: Arquitetura 100% Offline-First & MVI** | Operação 100% local com Room, DataStore, MVI (UiAction/UiEffect), busca e filtros locais | **100% CONCLUÍDO** |
| **Fase 3: Módulo Financeiro (Contas & Parcelas)** | Contas Avulsas, Parceladas (divisão de centavos), Recorrentes, Dashboard KPIs e Categorias | **100% CONCLUÍDO** |
| **Fase 4: Expansões Pessoais & Backup** | Backup local de dados (JSON/SQLite), relatórios de fluxo de caixa e novos módulos | **PLANEJADO** |
| **Fase 5: Sincronização Remota Opcional** | Sincronização em nuvem opcional (WorkManager/API) e otimizações ProGuard/R8 | **PLANEJADO** |

---

## 📋 Checklist de Funcionalidades

### Fase 1 & 2: Fundação Mobile & Offline-First (MVI)
- [x] Configuração raiz do Gradle com Kotlin DSL (`build.gradle.kts` e `settings.gradle.kts`).
- [x] Version Catalog centralizado em `gradle/libs.versions.toml`.
- [x] Arquitetura Clean Architecture dividida em Presentation, Domain, Data e Core.
- [x] Padrão **MVI (Model-View-Intent)** implementado com `UiState`, `UiAction` e `UiEffect` (Channel bufferizado).
- [x] Operação **100% Offline-First**: O Room Database é a única fonte da verdade e o app opera perfeitamente sem internet.
- [x] Injeção de dependências desacoplada com Dagger Hilt (`@HiltAndroidApp`, `AppModule`, `RepositoryModule`).
- [x] Persistência local com Room Database (`PlatformDatabase`).
- [x] Persistência de configurações locais e preferências via **AndroidX DataStore** (`PreferencesManager`).
- [x] Monitor reativo de conectividade em segundo plano (`NetworkMonitor` com `ConnectivityManager.NetworkCallback`).
- [x] Camada de UI reativa em Jetpack Compose com Material 3 (`PlatformTheme`, `Color`, `Type`, `PlatformAppBar`).
- [x] Suporte nativo a Tema Claro e Tema Escuro (*Dark Mode*).
- [x] Navegação reativa com Navigation Compose e rotas tipadas (`Screen.kt`, `NavGraph.kt`).
- [x] Script de automação de versionamento com incremento de `versionCode` e `versionName` (`scripts/bump-version.sh`).
- [x] Catálogo de regras e skills para agentes de IA em `.agents/` e manuais em `.ai/`.

### Fase 3: Módulo Financeiro Pessoal (Contas a Pagar)
- [x] Utilitário monetário `CurrencyUtils` em centavos inteiros (`amountCents: Long`) sem erros de ponto flutuante.
- [x] Utilitário de datas `DateUtils` para manipulação de vencimentos e meses.
- [x] Modelagem relacional no Room: `CategoryEntity`, `BillEntity` e `BillInstallmentEntity` com cascade delete e índices.
- [x] Auto-seeding inteligente de categorias padrão (Moradia, Alimentação, Transporte, Assinaturas, etc.).
- [x] Algoritmo matemático de divisão precisa de centavos para contas parceladas (`CalculateInstallmentsUseCase`).
- [x] Suporte a Contas Avulsas (`SINGLE`), Compras Parceladas (`INSTALLMENT`) e Assinaturas Recorrentes (`RECURRING`).
- [x] Tela **Dashboard Financeiro** com seletor de mês, cards de KPIs (Total, Pago, Pendente, Vencido), alerta de próximos vencimentos e distribuição por categoria.
- [x] Tela **Contas a Pagar** com listagem de parcelas, busca instantânea, filtros por tipo e status, liquidação rápida com 1 toque e modal de cadastro de contas.
- [x] Tela **Categorias** para visualização e criação de novas categorias com seletor de cores.
- [x] Barra inferior de navegação do Material 3 (`BottomNavBar`) integrando as abas Dashboard, Contas e Categorias.
- [x] Suíte de testes unitários de domínio financeiro (`CalculateInstallmentsUseCaseTest`, `BillsViewModelTest`).

### Fases Futuras (Planejado)
- [ ] Exportação e importação de backup local de dados (JSON/SQLite).
- [ ] Relatórios anuais consolidados e previsões de gastos.
- [ ] Sincronização remota opcional via WorkManager quando o usuário optar por conectar um servidor.
