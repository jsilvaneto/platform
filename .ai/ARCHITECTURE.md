# Arquitetura Técnica do Sistema Android (.ai/ARCHITECTURE.md)

Este documento é a **referência técnica primária e canônica** da arquitetura do aplicativo móvel **Platform**. Ele orienta desenvolvedores e **Agentes de IA** sobre o fluxo de dados, contratos, persistência e injeção de dependências.

---

## 1. Topologia da Aplicação (100% Offline-First & MVI)

O aplicativo segue os princípios oficiais da Google de **Modern Android Architecture (MAD)** combinados com **Clean Architecture** e o padrão **MVI (Model-View-Intent)**:

```mermaid
graph TD
    subgraph PresentationLayer ["Presentation Layer (Jetpack Compose + MVI)"]
        Activity["MainActivity (@AndroidEntryPoint)"] --> Nav["NavGraph (Navigation Compose)"]
        Nav --> Screen["HomeScreen (Composable UI)"]
        Screen -->|UiAction (onAction)| ViewModel["HomeViewModel (@HiltViewModel)"]
        ViewModel -->|StateFlow imutável| Screen
        ViewModel -->|UiEffect Channel (receiveAsFlow)| Screen
    end

    subgraph DomainLayer ["Domain Layer (Kotlin Puro)"]
        ViewModel -->|Invoca| UseCase["GetItemsUseCase"]
        UseCase -->|Chama contrato| RepoInterface["ItemRepository (Interface)"]
    end

    subgraph DataLayer ["Data Layer (Single Source of Truth Local)"]
        RepoImpl["ItemRepositoryImpl"] -.->|Implementa| RepoInterface
        RepoImpl -->|Queries reativas Flow| Dao["ItemDao (Room)"]
        Dao --> DB[("SQLite Local (PlatformDatabase)")]
        DataStore[("Preferences DataStore (Configurações Locais)")]
    end

    subgraph FutureExtension ["Extensão Futura (Sincronização Remota)"]
        Api["PlatformApiService (Retrofit)"] -.-> Backend[("API Remota")]
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
- **Monitoramento de Conectividade**: `NetworkMonitor` reativo com `ConnectivityManager.NetworkCallback`.
- **Concorrência**: Kotlin Coroutines e StateFlow/SharedFlow.
- **Testes Unitários**: JUnit 4, MockK (mocking de coroutines e dispatchers) e Turbine (testes reativos de StateFlow e Channels).

---

## 3. Divisão de Camadas e Responsabilidades

```
app/src/main/java/com/platform/app/
├── core/                     # Utilitários globais e abstrações de infraestrutura
│   ├── connectivity/         # NetworkMonitor e ConnectivityNetworkMonitor (Flow<Boolean>)
│   ├── dispatcher/           # DispatcherProvider para desacoplamento de Dispatchers.IO/Main
│   ├── mvi/                  # Interfaces marcadoras UiState, UiAction, UiEffect
│   ├── preferences/          # PreferencesManager usando AndroidX DataStore
│   └── util/                 # Resource<T> (Success, Error, Loading)
├── data/                     # Camada de dados e persistência offline
│   ├── local/
│   │   ├── dao/              # Interfaces Room @Dao com queries @Query e @Insert
│   │   ├── entity/           # Classes @Entity com mapeamento bidirecional toDomain()
│   │   └── PlatformDatabase  # Classe abstrata @Database do Room (SQLite)
│   ├── remote/               # Clientes e DTOs preparados para futura sincronização
│   │   ├── dto/              # Data Transfer Objects serializados via Gson
│   │   └── PlatformApiService# Interface Retrofit de endpoints HTTP
│   └── repository/           # Implementação concreta ItemRepositoryImpl operando sobre o Room
├── domain/                   # Camada de negócio pura (100% Kotlin puro, sem dependências Android)
│   ├── model/                # Data classes imutáveis de negócio (PlatformItem)
│   ├── repository/           # Interfaces de repositório (inversão de dependência)
│   └── usecase/              # Interactors de responsabilidade única (GetItemsUseCase)
├── di/                       # Injeção de dependências Hilt
│   ├── AppModule.kt          # Provimento de Room, DataStore, NetworkMonitor, Retrofit e Dispatchers
│   └── RepositoryModule.kt   # Binds de repositórios para o grafo de dependências
└── presentation/             # Camada de interface e interação visual
    ├── components/           # Componentes Compose reutilizáveis (PlatformAppBar, etc.)
    ├── navigation/           # Screen.kt e NavGraph.kt
    ├── theme/                # Color, Type, Theme com suporte a Dark Mode e Dynamic Color
    └── home/                 # Tela inicial modular (Screen, ViewModel, UiState, UiAction, UiEffect)
```

---

## 4. Ciclo de Vida do Padrão MVI no Aplicativo

1. **Renderização de Estado (`UiState`)**:
   - A tela Compose observa o `StateFlow` exposto pela ViewModel (`val uiState by viewModel.uiState.collectAsState()`).
2. **Disparo de Intenção (`UiAction`)**:
   - Qualquer interação do usuário (clique, busca textual, alternância de conclusão de tarefa) envia uma ação tipada para a ViewModel: `onAction(HomeUiAction.ToggleItemCompletion(item))`.
3. **Processamento Assíncrono Local**:
   - A ViewModel processa a ação dentro de `viewModelScope.launch` sem bloquear a thread principal, invocando o repositório local.
   - O Room persiste a alteração e emite a nova lista atualizada via `Flow`.
4. **Efeito Colateral Volátil (`UiEffect`)**:
   - Quando uma notificação (Snackbar) ou navegação é disparada, ela trafega através do canal `_effectChannel.send(...)`.
   - O composable captura o efeito via `LaunchedEffect(uiEffect) { uiEffect.collect { ... } }`, garantindo que não ocorra re-execução em rotações de tela.
