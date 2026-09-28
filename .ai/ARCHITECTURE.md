# Arquitetura Técnica do Sistema Android (.ai/ARCHITECTURE.md)

Este documento é a **referência técnica primária e canônica** da arquitetura do aplicativo móvel **Platform**. Ele orienta desenvolvedores e **Agentes de IA** sobre o fluxo de dados, contratos, persistência e injeção de dependências.

---

## 1. Topologia da Aplicação (100% Offline-First & MVI)

O aplicativo segue os princípios oficiais da Google de **Modern Android Architecture (MAD)** combinados com **Clean Architecture** e o padrão **MVI (Model-View-Intent)**:

```mermaid
graph TD
    subgraph PresentationLayer ["Presentation Layer (Jetpack Compose + MVI)"]
        Activity["MainActivity (@AndroidEntryPoint)"] --> Drawer["AppDrawer (ModalNavigationDrawer)"]
        Drawer --> Nav["NavGraph (Navigation Compose)"]
        Nav --> Screens["DashboardScreen / BillsScreen / RecurringScreen / ..."]
        Screens -->|UiAction (onAction)| ViewModels["ViewModels (@HiltViewModel)"]
        ViewModels -->|StateFlow imutável| Screens
        ViewModels -->|UiEffect Channel (receiveAsFlow)| Screens
    end

    subgraph DomainLayer ["Domain Layer (100% Kotlin Puro)"]
        ViewModels -->|Invoca| UseCases["CreateBillUseCase / GetFinancialDashboardUseCase / ..."]
        UseCases -->|Chama contrato| RepoInterface["FinancialRepository / BudgetRepository / GoalRepository"]
    end

    subgraph DataLayer ["Data Layer (Single Source of Truth Local)"]
        RepoImpl["FinancialRepositoryImpl"] -.->|Implementa| RepoInterface
        RepoImpl -->|database.withTransaction| DAOs["BillDao / BillInstallmentDao / CategoryDao / ..."]
        DAOs --> DB[("SQLite Local (PlatformDatabase)")]
        DataStore[("Preferences DataStore (Configurações Locais)")]
    end

    subgraph DiLayer ["Dependency Injection (Dagger Hilt)"]
        AppModule["AppModule (Room, DataStore, NetworkMonitor, Dispatchers)"]
        RepoModule["RepositoryModule (Repository Binds)"]
    end
```

---

## 2. Stack Tecnológica e Bibliotecas Principais

- **Linguagem**: Kotlin 1.9.23+ com Kotlin DSL no Gradle.
- **UI Toolkit**: Jetpack Compose com Material 3 (`compose-bom:2024.04.00`).
- **Padrão de UI**: MVI com `UiState`, `UiAction` e `UiEffect` (Channel bufferizado).
- **Injeção de Dependências**: Dagger Hilt 2.51 com KSP (Kotlin Symbol Processing).
- **Persistência Local (Offline-First)**: Room 2.6.1 com Coroutines e Flow, e AndroidX DataStore Preferences 1.0.0.
- **Segurança e Biometria**: AndroidX Biometric com autenticação por impressão digital ou PIN.
- **Concorrência**: Kotlin Coroutines e StateFlow/SharedFlow.
- **Testes Unitários**: JUnit 4, MockK e Turbine.

---

## 3. Divisão de Camadas e Responsabilidades

```
app/src/main/java/com/platform/app/
├── core/                     # Utilitários globais e infraestrutura
│   ├── connectivity/         # NetworkMonitor reativo
│   ├── dispatcher/           # DispatcherProvider para desacoplamento de IO/Main
│   ├── mvi/                  # Interfaces marcadoras UiState, UiAction, UiEffect
│   ├── preferences/          # PreferencesManager usando AndroidX DataStore
│   ├── security/             # BiometricAuthManager
│   └── util/                 # CurrencyUtils, DateUtils, CurrencyVisualTransformation
├── data/                     # Camada de dados e persistência offline
│   ├── local/
│   │   ├── dao/              # BillDao, BillInstallmentDao, CategoryDao, ContactDao, etc.
│   │   ├── entity/           # BillEntity, BillInstallmentEntity, CategoryEntity, etc.
│   │   └── PlatformDatabase  # Room Database central
│   └── repository/           # FinancialRepositoryImpl, BudgetRepositoryImpl, GoalRepositoryImpl
├── domain/                   # Camada de negócio pura (100% Kotlin puro, sem dependências Android)
│   ├── model/                # Bill, BillInstallment, Category, Subcategory, FinancialAccount, Budget, Goal
│   ├── repository/           # Interfaces de repositório (inversão de dependência)
│   └── usecase/              # CreateBillUseCase, CalculateInstallmentsUseCase, etc.
├── di/                       # Injeção de dependências Hilt
│   ├── AppModule.kt          # Provimento de Room, DataStore, NetworkMonitor e Dispatchers
│   └── RepositoryModule.kt   # Binds de repositórios para o grafo de dependências
└── presentation/             # Camada de interface e interação visual (Compose)
    ├── navigation/           # Screen.kt, NavGraph.kt, AppDrawer.kt
    ├── theme/                # Color, Type, Theme com suporte a Dark Mode
    ├── components/           # PlatformAppBar, PlatformCard, PlatformStatusChip
    ├── dashboard/            # DashboardScreen (KPIs do Mês, Próximos 7 dias, Gráficos)
    ├── bills/                # BillsScreen (Lançamentos com busca inline animada e filtros)
    ├── recurring/            # RecurringInstallmentsScreen (Assinaturas e parcelamentos)
    ├── statistics/           # StatisticsScreen (Análises históricas e projeções)
    ├── budgets/              # BudgetsScreen (Tetos de gastos por categoria)
    ├── goals/                # GoalsScreen (Metas financeiras)
    ├── contacts/             # ContactsScreen & ContactDetailScreen
    ├── management/           # ManagementScreen (Cadastros Base: Contas, Formas de Pagamento e Categorias)
    └── settings/             # SettingsScreen (Biometria, Tema e Sobre)
```

---

## 4. Ciclo de Vida do Padrão MVI no Aplicativo

1. **Renderização de Estado (`UiState`)**:
   - A tela Compose observa o `StateFlow` exposto pela ViewModel (`val uiState by viewModel.uiState.collectAsState()`).
2. **Disparo de Intenção (`UiAction`)**:
   - Qualquer interação do usuário (clique, busca textual, alternância de pagamento de parcela) envia uma ação tipada para a ViewModel: `onAction(BillsUiAction.TogglePayment(inst))`.
3. **Processamento Assíncrono Local**:
   - A ViewModel processa a ação dentro de `viewModelScope.launch` sem bloquear a thread principal, invocando Casos de Uso ou Repositórios com transações atômicas no Room.
4. **Efeito Colateral Volátil (`UiEffect`)**:
   - Mensagens transitórias (Snackbars) ou navegação trafegam através do canal `_effectChannel.send(...)` e são capturadas na UI via `LaunchedEffect`.
