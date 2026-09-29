# Diretrizes e Manual Operacional para Agentes de IA (AGENT_RULES.md)

Este documento estabelece as diretrizes mandatórias de desenvolvimento, padrões arquiteturais e governança de contexto para qualquer **Agente de IA** atuando no projeto **Platform (Android App)**.

---

## 1. Visão Geral da Arquitetura (Android Clean Architecture 100% Offline-First)

O aplicativo adota Clean Architecture combinada com o padrão MVI (Model-View-Intent), Jetpack Compose (Material 3), persistência local exclusiva via Room e injeção de dependências com Dagger Hilt:

```mermaid
graph TD
    UI["Jetpack Compose UI (Screens, Components, Theme)"] -->|UiAction (onAction)| VM["ViewModels (@HiltViewModel)"]
    VM -->|StateFlow imutável| UI
    VM -->|UiEffect Channel (receiveAsFlow)| UI
    VM -->|Injeta & Executa| UC["UseCases / Interactors (domain/usecase/)"]
    UC -->|Chama Interfaces| Repo["Repository Contracts (domain/repository/)"]
    RepoImpl["Repository Impl (data/repository/)"] -.->|Implementa| Repo
    RepoImpl -->|Local Caching / Flow| Room["Room Database (data/local/)"]
    RepoImpl -->|Configurações Locais| DataStore[("Preferences DataStore")]
```

### 1.1 Divisão Estrita de Camadas
- `app/src/main/java/com/platform/app/presentation/`:
  - Camada de exibição pura e gerenciamento de estado de tela.
  - Composta por **Composables** (`bills/`, `dashboard/`, `contacts/`, `management/`, `budgets/`, `goals/`, `recurring/`, `settings/`, `components/`), **ViewModels** (anotados com `@HiltViewModel`), **UiState** imutáveis e temas Material 3 (`theme/`).
  - **Proibido**: Fazer chamadas diretas de banco de dados ou I/O dentro de Composables.
- `app/src/main/java/com/platform/app/domain/`:
  - O coração das regras de negócio do aplicativo de Contas a Pagar.
  - Contém **modelos de domínio puros** (`model/`), interfaces de repositórios (`repository/`) e casos de uso de responsabilidade única (`usecase/`).
  - **Proibido**: Dependências de frameworks Android (como Context, Views, Room). Código 100% puro Kotlin.
- `app/src/main/java/com/platform/app/data/`:
  - Implementação concreta de acesso a dados e persistência offline.
  - Contém **Room DAOs e Entities** (`local/`) e implementações de repositórios (`repository/`).
  - Responsável por mapear Entities para modelos de domínio (`toDomain()`).
- `app/src/main/java/com/platform/app/di/`:
  - Módulos Dagger Hilt (`@Module`, `@InstallIn(SingletonComponent::class)`) para injeção e provimento de dependências singleton (`AppModule.kt`) e vinculação de repositórios (`RepositoryModule.kt`).
- `app/src/main/java/com/platform/app/core/`:
  - Utilitários globais (`CurrencyUtils`, `DateUtils`), despachantes de coroutines (`DispatcherProvider`), preferências (`PreferencesManager`) e autenticação biométrica (`BiometricAuthManager`).

---

## 2. Regras Mandatórias de Código

1. **Operação 100% Offline-First**:
   - Todo dado gerado no app deve ser lido e gravado exclusivamente no banco local **Room** e **DataStore**. Nenhuma funcionalidade pode depender de resposta de rede para funcionar. O aplicativo opera integralmente em modo avião.
2. **Escopo Focado em Contas a Pagar & Despesas**:
   - O sistema NÃO trata receitas ou salários.
   - Contas bancárias (`FinancialAccount`) atuam estritamente como **contas de referência** para indicar o meio de pagamento ou a conta debitada, sem controle de saldo contábil.
3. **Padrão MVI com Canal de Efeitos (`UiEffect`)**:
   - A tela emite intenções via `UiAction` (`onAction(action)`).
   - Efeitos transitórios (Snackbars, navegação, diálogos) NUNCA devem residir no `UiState`. Trafegam através de um `Channel<UiEffect>(Channel.BUFFERED)` exposto como Flow e consumido na UI via `LaunchedEffect`.
4. **Valores Monetários em Centavos (`amountCents: Long`)**:
   - Proibido uso de `Float` ou `Double` para cálculos ou armazenamento de dinheiro. Use sempre inteiros em centavos (`amountCents: Long`).
5. **Transações Atômicas Obrigatórias (`database.withTransaction`)**:
   - Gravações compostas (como inserção de conta e parcelas) devem rodar sempre dentro de uma transação atômica do Room.
6. **Elegância Visual & Anti-Gigantismo**:
   - Todas as telas devem seguir as diretrizes da skill `ui-elegance-and-proportions`: busca inline animada na `TopAppBar`, tipografia equilibrada, cantos arredondados suaves e FAB com ícone vetorial (`Icons.Default.Add`).
7. **Material 3 & Dark Mode Obrigatório**:
   - Todas as telas e componentes visuais DEVEM suportar nativamente **Modo Claro** e **Modo Escuro** utilizando as cores semânticas do `MaterialTheme.colorScheme` (ex: `surface`, `background`, `onSurface`, `primary`).
8. **Política de Resíduo Zero em Testes**:
   - Testes devem utilizar banco em memória (`Room.inMemoryDatabaseBuilder`) e mocks isolados com `Dispatchers.setMain(testDispatcher)`.

---

## 3. Instruções para o Agente: Como Adicionar uma Nova Feature

Ao receber uma demanda para implementar uma nova funcionalidade, siga este fluxo:

### Passo 1: Domínio Puro
1. Defina o modelo em `domain/model/<Feature>.kt`.
2. Adicione os métodos necessários na interface `domain/repository/<Feature>Repository.kt`.
3. Crie os casos de uso em `domain/usecase/Get<Feature>UseCase.kt`.

### Passo 2: Camada de Dados Local (Room)
1. Crie a entidade Room em `data/local/entity/<Feature>Entity.kt` e o DAO em `data/local/dao/<Feature>Dao.kt`.
2. Implemente o repositório em `data/repository/<Feature>RepositoryImpl.kt`.

### Passo 3: Injeção de Dependências
1. Adicione a entidade no `PlatformDatabase.kt` e proveja o DAO no `di/AppModule.kt`.
2. Registre o binding do novo repositório em `di/RepositoryModule.kt`.

### Passo 4: Camada de Apresentação (Compose + ViewModel)
1. Crie o estado imutável em `presentation/<feature>/<Feature>UiState.kt`.
2. Crie a `presentation/<feature>/<Feature>ViewModel.kt` anotada com `@HiltViewModel`.
3. Crie a tela Compose em `presentation/<feature>/<Feature>Screen.kt` com suporte a Light/Dark theme.
4. Adicione a rota em `presentation/navigation/Screen.kt` e registre o composable no `NavGraph.kt`.

### Passo 5: Testes e Validação
1. Crie testes unitários para os casos de uso e ViewModel em `app/src/test/`.
2. Garanta 100% de sucesso executando `.\gradlew testDebugUnitTest`.

### Passo 6: Governança, Versionamento e Registro (OBRIGATÓRIO)
1. **Atualização de Versão**: Incremente o SemVer através do script `.\scripts\bump-version.ps1 <versão>` (ou `bump-version.sh`). Proibido entregar entregáveis sem versionamento explícito.
2. **Histórico no CHANGELOG.md**: Registre a versão, data e todos os pontos implementados/alterados na seção correspondente.
3. **Notas de Versão no App**: Atualize o `ReleaseNotesDialog` em `SettingsScreen.kt` caso a entrega altere fluxos ou adicione recursos visíveis ao usuário.
4. **Status e Decisões**: Atualize `.ai/STATUS.md` e registre novos ADRs em `.ai/DECISIONS/` para mudanças arquiteturais ou novos padrões.
5. **Manutenção de Skills**: Se um novo padrão de UI ou arquitetura for aprovado (ex: BottomSheet universal, 3 pontos, SAF), sincronize o catálogo de skills (`.agents/skills/`).

---

## 4. Repositório de Skills e Regras (`.agents/`)

- **Skills Disponíveis (`.agents/skills/<skill-name>/SKILL.md`)**:
  - `financial-domain-guard`: Invariantes de contas a pagar, parcelamentos, recorrências, centavos exatos e contas de referência.
  - `ui-elegance-and-proportions`: Diretrizes de elegância visual, moderação de escala, busca inline, padrão universal de BottomSheet e ações seguras.
  - `android-compose-design-system`: Padrões de interface Material 3, Dark Mode e previews.
  - `add-new-screen-or-feature`: Fluxo oficial de novas features 100% offline-first.
  - `android-testing-suite`: Práticas de testes unitários (MockK, Turbine, JUnit) em MVI e Room.
  - `room-database-and-migrations`: Queries de alta performance em SQLite, `@Transaction` obrigatório e evolução de schema.
  - `offline-backup-and-export`: Estratégia de exportação e restauração local atômica de dados em JSON via SAF e ShareSheet.
  - `gradle-build-and-lint`: Comandos de build, verificação estática e lint.
  - `security-guard`: Diretrizes de biometria e isolamento no aparelho.
  - `token-optimizer`: Protocolo para economia cirúrgica de contexto.
- **Regras Mandatórias (`.agents/rules/*.md`)**:
  - `architecture.md`: Fronteiras invioláveis entre Presentation, Domain e Data.
  - `coding_standards.md`: Convenções idiomáticas de Kotlin, imutabilidade e StateFlow.
  - `test_data_cleanup.md`: Política de Resíduo Zero.
  - `governance_and_versioning.md`: Regra mandatória de incremento de versão, changelog, status e atualização contínua de skills a cada entrega.

---

## 5. Diretrizes Mandatórias de Design System (Android Compose)

Sempre que você criar, ajustar ou refatorar qualquer tela, componente ou ViewModel no projeto Android, siga estritamente as regras abaixo:

1. **Governança e ADRs:**
   - Respeite integralmente o `ADR-018` e os temas definidos em `com.platform.app.presentation.theme.*`.

2. **Tema e Superfícies:**
   - Suporte nativo a Tema Escuro e Claro via `MaterialTheme.colorScheme`.
   - Cards e contêineres devem obrigatoriamente usar cantos arredondados de `16.dp`, fundo em `surface` e borda sutil de `1.dp` (`outline`).
   - Evite sombras pesadas; priorize contraste de cor e borda.

3. **Cores Semafóricas:**
   - Use `UrgentRed` apenas para itens vencidos/atrasados.
   - Use `WarningAmber` apenas para itens que vencem hoje.
   - Use `SuccessGreen` para status pago ou liquidação concluída.
   - Mantenha todo o restante em tons neutros (`onSurface` e `onSurfaceVariant`).

4. **Regras Tipográficas e Textos:**
   - **Nomes de Entidades/Categorias/Contas**: Obrigatório uso de Title Case (ex: "Energia Copel", "Cartão Santander"). Proibido salvar ou exibir em ALL CAPS.
   - **Descrições de Despesas**: Sentence Case (ex: "Manutenção do ar condicionado").
   - **Enums/Constantes Técnicas**: ALL CAPS restrito ao backend/Room (`PESSOAL`, `EMPRESA`, `PENDENTE`).
   - **Valores**: Sempre formatados a partir de `Long` (centavos) com tipografia de destaque (`FontWeight.Bold` ou `FontWeight.SemiBold`).

5. **Ergonomia e UX:**
   - Formulários rápidos devem ter no máximo 3 campos obrigatórios (Descrição, Valor e Vencimento).
   - Telas de listagem devem permitir marcar como pago em 1 toque diretamente no card.

