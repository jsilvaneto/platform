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
| **Fase 7: Wallet 100% Pessoal, Contas a Pagar & Cartões** | Contexto pessoal único, ExpenseNature, Itens de despesa, Forecast semafórico e Faturas | **100% CONCLUÍDO** (v1.4.0) |
| **Fase 8: Grande Update das 4 Telas Centrais** | Hero Executivo, Privacidade, SegmentedTabs, Batch Actions, Apple Card View e Cronogramas | **100% CONCLUÍDO** (v1.4.1) |
| **Fase 9: Estatísticas & Inteligência Financeira 360°** | 3 Eixos Temporais (Passado, Presente e Futuro), Curva de Desoneração, Rigidez Orçamentária e Tomada de Decisão | **100% CONCLUÍDO** (v1.4.2) |
| **Fase 10: Excelência Operacional, UX & Lembretes Locais** | Transições Cinemáticas, Desfazer Baixa (Undo), Duplicação de Despesas e Notificações de Vencimento | **100% CONCLUÍDO** (v1.4.3) |
| **Fase 11: Sincronização em Nuvem (API Bidirecional)** | Backend remoto, motor de sync offline-first, backup automático no banco de dados | **PLANEJADA (Fase 2)** |

---

## 📋 Checklist de Funcionalidades Implementadas

### Fundação, Arquitetura e MVI
- [x] Configuração raiz do Gradle com Kotlin DSL (`build.gradle.kts` e `settings.gradle.kts`).
- [x] Version Catalog centralizado em `gradle/libs.versions.toml`.
- [x] Arquitetura Clean Architecture: Presentation, Domain, Data e Core.
- [x] Padrão **MVI (Model-View-Intent)** implementado com `UiState`, `UiAction` e `UiEffect` (Channel bufferizado).
- [x] Operação **100% Offline-First**: O Room Database é a única fonte da verdade e o app opera sem internet.
- [x] Injeção de dependências com Dagger Hilt (`@HiltAndroidApp`, `AppModule`, `RepositoryModule`).
- [x] Persistência local com Room Database (`PlatformDatabase` v7) com transações atômicas (`database.withTransaction`).
- [x] Persistência de configurações e preferências via **AndroidX DataStore** (`PreferencesManager`).
- [x] Bloqueio e segurança com **AndroidX Biometric** (`BiometricAuthManager`, `BiometricLockOverlay`).
- [x] Design System Material 3 com Dark Mode nativo (`PlatformTheme`, `Color`, `Type`).
- [x] Navegação moderna com Navigation Compose e menu lateral despoluído (`AppDrawer.kt`, `NavGraph.kt`).

### Wallet 100% Pessoal & Contas a Pagar (v1.4.0)
- [x] **Contexto Único Pessoal**: Remoção de qualquer seletor ou campo de perfil corporativo (`profileType`, `PESSOAL`/`EMPRESA`).
- [x] **Foco em Contas a Pagar**: Cadastro ágil com 3 campos mandatórios (Descrição, Valor em centavos `Long`, Vencimento `Long`), com Categoria/Item opcionais (`NewExpenseScreen`).
- [x] **Natureza do Gasto (ExpenseNature)**: 4 pilares (`OBRIGATORIO`, `NECESSARIO`, `DESEJA`, `NENHUM`), herança estrita da categoria para o item de despesa.
- [x] **Itens de Despesa**: Depreciação de subcategorias e adoção de `expense_items` vinculados à categoria mãe, com tela dedicada `ExpenseItemsScreen`.
- [x] **Dashboard com Efeito Cascata em Tempo Real**: `Total Previsto = Contas Pendentes + Faturas Abertas/Fechadas do Mês` calculado via Flow reativo (`CalculateMonthlyForecastUseCase`).
- [x] **Agrupamento Semafórico de Urgência**: 🔴 Atrasadas, 🟡 Vence Hoje, ⚪ Próximos 7 Dias e 🟢 Pagas no Mês (colapsável).
- [x] **Baixa com 1 Toque**: Ação direta de pagamento na conta/fatura com alteração de status para `PAGO` no banco e dedução instantânea do Total Previsto via Flow.
- [x] **Gestão de Cartões de Crédito & Extrato de Faturas**: Cálculo de limite disponível (`CreditCardCalculator`), fechamento por dia de corte (`closingDay`), status de faturas e liquidação em 1 toque na `CreditCardsScreen`.
- [x] **Lançamento Focado em Itens (Item-Centric Form)**: Item de despesa como protagonista (1º campo obrigatório), herança automática de título e descrição convertida em observações adicionais opcionais no final do formulário (`NewExpenseScreen`).
- [x] **Design System Premium Minimalista**:
  - Eliminação de elevação tonal com `surfaceTint = Color.Transparent` em Dark e Light Mode (zero vazamento de roxo/lavanda default M3).
  - Remoção de 100% das cores literais hardcoded em telas (`Color.White`, `Color.Black`, hexadecimais soltos), substituídas por tokens semânticos de `Theme.kt`, `Color.kt` e `MaterialTheme.colorScheme`.
  - Simetria rigorosa (grid 4/8/12/16/20/24 dp, cantos de 16.dp para cards e 10.dp para botões/inputs).
  - Unificação de FABs 100% circular (`CircleShape`) em todas as 9 telas financeiras e componentes centrais reutilizáveis (`PlatformEmptyState`, `PlatformAvatar`).
  - Higienização visual estrita: remoção de botões de exclusão inline, eliminação de emojis informais e substituição do código zumbi `DashboardScreen.kt`.
- [x] **Validação com Testes Unitários Locais**:
  - `NewExpenseValidationTest`: 6 testes unitários aprovados (campos obrigatórios, herança de título pelo item, cálculo de parcelas e natureza).
  - `CreditCardManagementTest`: 4 testes aprovados (fechamento, limites e dependências de exclusão).
  - `BillCalculationTest`, `ExpenseItemNatureTest`, `InvoiceClosingTest`: Todos aprovados com 100% de sucesso.

### Backup & Recuperação de Dados (v1.3.0)
- [x] DTO unificado `BackupDataDto` com versionamento e timestamp.
- [x] Restauração atômica via transação Room (`database.withTransaction`) com preservação de integridade referencial.
- [x] Integração com Storage Access Framework (SAF) nativo (`CreateDocument` e `OpenDocument`).
- [x] Compartilhamento direto com mensageiros via `FileProvider` (`file_paths.xml`).
- [x] Painel de status com indicador de data/hora do último backup e diálogo de advertência prévia para restaurações.

### Excelência Operacional, UX & Lembretes Locais (v1.4.3)
- [x] **Transições Cinemáticas no NavGraph**: Animações fluidas entre destinos (`slideIntoContainer` + `fadeIn`/`fadeOut`).
- [x] **Mecanismo de Desfazer (Undo)**: Reversão imediata de pagamentos de contas e faturas com 1 clique na `HomeScreen`.
- [x] **Reabertura Atômica de Faturas (`reopenInvoice`)**: Restauração consistente de faturas e parcelas vinculadas no Room.
- [x] **Duplicação Ágil de Despesas**: Ação no menu do BottomSheet de detalhes clonando todos os dados da despesa na `NewExpenseScreen`.
- [x] **Notificações Locais Offline de Vencimentos**: Lembretes inteligentes para despesas e faturas que vencem no dia (`DueReminderManager`, `DueReminderReceiver`).
- [x] **Suíte de Testes Automatizados**: Novos testes `HomeViewModelTest` e `NewExpenseViewModelTest` com 100% de sucesso.

### Gestão & Edição de Itens de Despesa (v1.4.4)
- [x] **Edição Completa de Itens de Despesa**: Fluxo reativo para alteração de nome e categoria vinculada (`AddEditExpenseItemBottomSheet`).
- [x] **Padrão de Detalhes Universal (`ExpenseItemDetailBottomSheet`)**: ModalBottomSheet de detalhes acionado por toque no card com avatar, vínculos de categoria, badge de natureza e menu de 3 pontos (`MoreVert`).
- [x] **Acesso Direto à Edição em Nova Despesa (`NewExpenseScreen`)**: Atalho "Gerenciar Itens (Criar / Editar)" no menu dropdown e botão de edição rápida de 1 toque no chip de natureza herdada.
- [x] **Suíte de Testes Automatizados**: Novos testes unitários aprovados em `ExpenseItemsViewModelTest.kt` cobrindo carga, filtros, salvamento de edição e exclusão.

### Baixa de Pagamento Retroativa & Data Real de Pagamento (v1.4.5)
- [x] **Campo `actualPaymentDate` no Domínio e Room**: Adicionado campo opcional `actualPaymentDate: Long?` em `BillInstallment` e `BillInstallmentEntity`.
- [x] **Migração de Banco de Dados Room (`PlatformDatabase` v10)**: Migração `MIGRATION_9_10` (`ALTER TABLE bill_installments ADD COLUMN actualPaymentDate INTEGER DEFAULT NULL`) registrada e injetada no `AppModule`.
- [x] **Diálogo Material 3 de Baixa (`ConfirmPaymentDialog`)**: Confirmação visual de pagamento com atalhos "Hoje", "No Vencimento" e `DatePickerDialog` customizado integrado em todas as telas de liquidação.
- [x] **Cálculo da Taxa de Pontualidade (`GetFinancialDashboardUseCase`)**: Métrica `onTimePaymentRate` atualizada para comparar `actualPaymentDate` (fallback `paidAt`) com `dueDate`, eliminando falsos atrasos em baixas retroativas.
- [x] **Suíte de Testes Automatizados**: Cobertura expandida em `ToggleInstallmentPaymentUseCaseTest`, `GetFinancialDashboardUseCaseTest`, `BillsViewModelTest` e `HomeViewModelTest`.
- [x] **Decisão Arquitetural Documentada**: Criada [ADR 020](.ai/DECISIONS/020-data-real-pagamento-e-baixa-retroativa.md).

### Rigidez Orçamentária Real 50-30-20 & Metas (Poupança) (v1.5.0)
- [x] **Integração Holística de Metas Financeiras como Poupança (`GetFinancialDashboardUseCase`)**: Injeção de `GoalRepository` e cálculo reativo dos aportes do mês (`goalRepository.getMonthlyContribution`), somando como poupança ao lado da distribuição de `ExpenseNature`.
- [x] **Base de Cálculo Orçamentária Unificada**: Orçamento total integrado $\text{Total Orçado} = \text{Total Contas} + \text{Aportes em Metas}$, com percentuais somando exatamente 100%.
- [x] **Motor de Diagnóstico Multifaixas (`BudgetRigidityCalculator`)**: 8 estados de rigidez (`VAZIO`, `SOBRECARREGADO`, `ENGESSADO`, `ESTILO_DE_VIDA_ELEVADO`, `SEM_POUPANCA`, `NAO_CLASSIFICADO`, `EQUILIBRADO`, `EXCELENTE`), eliminando falsos positivos de "Excelente" quando Deseja e Poupança estiverem zerados.
- [x] **Histórico de Aportes no Room & Migração v11 (`PlatformDatabase`)**: Tabela `goal_contributions` com migração `MIGRATION_10_11`, chave estrangeira em cascata e integração nos backups SAF JSON (`BackupDataDto` e `BackupRepositoryImpl`).
- [x] **Card Visual Aprimorado (`NatureDistributionCard`)**: Diagnóstico semafórico contextual com títulos em destaque, barra dedicada para "Poupança (Metas)" em esmeralda (`#10B981`) e rodapé com total orçado.
- [x] **Suíte de Testes Automatizados**: Novos testes em `GetFinancialDashboardUseCaseTest` e `BudgetRigidityCalculatorTest` cobrindo 0% Deseja/Poupança, 50-30-20 real e todas as fronteiras de classificação com 100% de sucesso.
- [x] **Decisão Arquitetural Documentada**: Criada [ADR 021](.ai/DECISIONS/021-rigidez-orcamentaria-50-30-20-com-metas.md).

### Extensão Contínua de Recorrências FOREVER (v1.6.0)
- [x] **Geração Dinâmica de Próximas Ocorrências (`CalculateInstallmentsUseCase`)**: Método `generateNextRecurringInstallments` para projetar lotes seguintes de compromissos recorrentes contínuos.
- [x] **Preservação de Vencimento e Prevenção de Desvios de Calendário**: Cálculo ancorado na primeira data de vencimento com passo estrito, eliminando degradação do dia do mês após meses curtos (ex: fevereiro).
- [x] **Caso de Uso de Extensão Automática (`ExtendRecurringBillsUseCase`)**: Caso de uso de domínio puro (`domain/usecase/`) avaliando limiares $N$ por frequência (`MONTHLY` $\le 3$, `DAILY` $\le 7$, `WEEKLY` $\le 4$, `YEARLY` $\le 1$) com laço auto-recuperativo e idempotência garantida.
- [x] **Persistência Transacional no Room (`FinancialRepositoryImpl` & `BillInstallmentDao`)**: Inserção em lote de parcelas e atualização simultânea de `recurrenceEndDate` e `totalInstallments` na tabela `bills`.
- [x] **Ciclo de Execução Periódica & em Segundo Plano**: Invocação transparente na abertura do `DashboardViewModel`, `BillsViewModel` e `RecurringInstallmentsViewModel`, além da execução matinal offline via `DueReminderReceiver` (`AlarmManager`) e `MainActivity`.
- [x] **Suíte de Testes Automatizados**: Novos testes em `ExtendRecurringBillsUseCaseTest` e `CalculateInstallmentsUseCaseTest` com 100% de aprovação.
- [x] **Decisão Arquitetural Documentada**: Criada [ADR 022](.ai/DECISIONS/022-extensao-continua-recorrencias-forever.md).

### Semântica e Hierarquia Visual na Visão Mensal (v1.6.1)
- [x] **Rótulo Semântico "Restante a Pagar no Mês"**: Renomeação no `ForecastImpactCard` em `HomeScreen.kt`, eliminando ambiguidade sobre orçamento total versus saldo ainda em aberto.
- [x] **Hierarquia Visual Reforçada para Quitação Completa**: Quando todas as contas do mês estiverem pagas (`R$ 0,00`), o badge de sucesso "Tudo quitado" com ícone de confirmação aparece antes do valor monetário, que ganha tonalidade esmeralda de sucesso (`SuccessGreen`).

### Tipificação de Contatos e Migração Room v12 (v1.7.0)
- [x] **Enum de Domínio Puro (`ContactType`)**: Enum com `PESSOA_FISICA`, `FORNECEDOR`, `ORGAO_PUBLICO` incorporado a `Contact` e mapeado em `ContactEntity`.
- [x] **Migração de Schema Room (`PlatformDatabase` v12)**: `MIGRATION_11_12` adicionando coluna `type TEXT NOT NULL DEFAULT 'FORNECEDOR'` na tabela `contacts`.
- [x] **Filtros e Agrupamento em `ContactsScreen`**: Barra de chips com contadores dinâmicos (`ContactTypeFilterRow`) e agrupamento por seções (`ContactSectionHeader`).
- [x] **Indicadores Visuais de Tipo**: `ContactTypeBadge` com ícones temáticos (`Person`, `Business`, `AccountBalance`) e avatar com cor temática.
- [x] **Seletor de Tipo no Formulário**: Componente de seleção ergonômico no `AddContactBottomSheet` e exibição na tela de detalhes.
- [x] **Suíte de Testes Automatizados**: Testes unitários em `ContactsViewModelTest` cobrindo filtros por tipo e buscas textuais combinadas.
- [x] **Decisão Arquitetural Documentada**: Criada [ADR 023](.ai/DECISIONS/023-tipificacao-contatos-e-migracao-room.md).

### Tipificação de Contas Financeiras e Migração Room v13 (v1.8.0)
- [x] **Enum de Domínio Puro (`FinancialAccountType`)**: Enum com `CORRENTE`, `CARTEIRA`, `POUPANCA`, `INVESTIMENTO`, labels amigáveis e resolução resiliente de aliases históricos (`fromString`).
- [x] **Substituição de String Livre por Enum**: Campo `accountType` tipado em `FinancialAccount`, `FinancialAccountEntity` e na métrica de dashboard `AccountSpend`.
- [x] **Migração de Schema Room (`PlatformDatabase` v13)**: `MIGRATION_12_13` normalizando strings legadas (`CHECKING`, `CASH`, `SAVINGS`, `Conta Corrente`, etc.) para os nomes canônicos do enum.
- [x] **Conversor de Tipos Room (`FinancialAccountTypeConverter`)**: Conversor seguro com fallback contra inconsistências registrado no Room.
- [x] **Interface e Experiência do Usuário (UI/UX)**:
  - `ManagementScreen`: badges e detalhes exibindo `account.accountType.displayName`, diálogo `AddEditAccountDialog` com dropdown alimentado por `FinancialAccountType.entries` e ícones dinâmicos.
  - `StatisticsScreen`: card `AccountsDistributionCard` com ícones semânticos contextuais (`AccountBalance`, `Payments`, `Savings`, `TrendingUp`) e rótulo do tipo de conta.
- [x] **Suíte de Testes Automatizados**: Testes em `FinancialAccountTypeTest` e atualização de `ManagementViewModelTest`.
- [x] **Decisão Arquitetural Documentada**: Criada [ADR 024](.ai/DECISIONS/024-enum-financial-account-type-e-migracao-room.md).

### Criptografia AES-256-GCM para Backups com Proteção por Senha (v1.9.0)
- [x] **Criptografia Simétrica AEAD (`BackupCryptoHelper`)**: `AES/GCM/NoPadding` de 256 bits eliminando exportações em texto plano.
- [x] **Derivação de Chave PBKDF2**: `PBKDF2WithHmacSHA256` com 65.536 iterações, sal de 16 bytes e IV de 12 bytes via `SecureRandom`.
- [x] **Envelope de Transporte (`EncryptedBackupDto`)**: Empacotamento versionado em JSON protegendo 100% dos dados financeiros locais.
- [x] **Contratos e Casos de Uso com Senha**: Exigência de senha/PIN em `BackupRepository`, `ExportBackupUseCase` e `RestoreBackupUseCase`.
- [x] **Interface & Modais Interativos (`SettingsScreen`)**: Diálogos ergonômicos `CreateBackupPasswordDialog` e `RestorePasswordDialog` com validações, visibilidade de senha e feedback semântico.
- [x] **Suíte de Testes Automatizados**: Novos testes em `BackupCryptoHelperTest` e atualizações em `BackupRepositoryImplTest` e `SettingsViewModelTest`.
- [x] **Decisão Arquitetural Documentada**: Criada [ADR 025](.ai/DECISIONS/025-criptografia-aes-gcm-backups-protegidos-por-senha.md).

### Centralização de Strings de Interface (UI) e Fonte Única da Verdade (v1.10.0)
- [x] **Objeto Estruturado `AppStrings` (`presentation/common/AppStrings.kt`)**: Fonte única da verdade para constantes textuais de interface com namespaces organizados (`Status`, `Nature`, `AccountType`, `ContactType`, `BillType`, `Actions`, `Home`, `Dialogs`).
- [x] **Espelhamento em Recursos Android (`res/values/strings.xml`)**: Todas as chaves equivalentes adicionadas aos recursos nativos XML do projeto.
- [x] **Substituição 1:1 sem Alterações de Texto**: Mapeamento e substituição de strings hardcoded em `HomeScreen`, `BillsScreen`, `NewExpenseScreen`, `EditInstallmentBottomSheet`, `ConfirmPaymentDialog`, `ContactsScreen`, `ContactDetailScreen`, `ManagementScreen`, `StatisticsScreen`, `CreditCardsScreen`, `ExpenseItemsScreen`, `GoalsScreen`, `BudgetsScreen`, `RecurringInstallmentsScreen`, `AdjustInstallmentDialog` e `SettingsScreen`.
- [x] **Suíte de Testes Automatizados**: Criado `AppStringsTest` garantindo integridade das constantes de interface e prevenindo regressões. 100% dos testes unitários passando.

### Remoção de Tabela Legada, Resíduo Zero e Migração Room v14 (v1.11.0)
- [x] **Política de Resíduo Zero (.agents/rules/test_data_cleanup.md)**: Eliminação física de artefatos obsoletos e tabelas legadas órfãs.
- [x] **Expurgo de Código Morto**: Exclusão de `TransactionEntity.kt` e `TransactionDao.kt`, remoção do provider `@Provides` no `AppModule.kt`, e remoção de referências em `BackupRepositoryImpl` e `BackupDataDto`.
- [x] **Migração Room v14 com `DROP TABLE` Físico**: `PlatformDatabase` elevado para versão 14 e inclusão de `MIGRATION_13_14` (`DROP TABLE IF EXISTS transactions`), eliminando índices e tabelas fantasmas no SQLite local.
- [x] **Suíte de Testes Automatizados**: Criado `PlatformDatabaseMigrationTest` validando a execução do comando de drop, atualização de `BackupRepositoryImplTest` e 100% dos testes unitários verdes.
- [x] **Decisão Arquitetural Documentada**: Criada [ADR 026](.ai/DECISIONS/026-remocao-tabela-transactions-e-migracao-room-v14.md).

### Limpeza de Typealias Obsoleto (v1.11.1)
- [x] **Expurgo de Código Morto Residual**: Remoção de `AppDatabase.kt` (`typealias AppDatabase = PlatformDatabase`), eliminando código morto de compatibilidade sem uso no app.
- [x] **Zero Efeito Colateral**: 100% dos testes unitários e compilação validados.

### Remoção de `syncStatus` Residual, Resíduo Zero e Preparação Fase 11 (v1.12.0)
- [x] **Política de Resíduo Zero & Eliminação de Código Zumbi**: Expurgo do campo `syncStatus` de `Category`, `ExpenseItem`, `CreditCard`, `CreditCardInvoice` e respectivas entidades Room.
- [x] **Arquitetura Anti-Especulativa (YAGNI)**: Decisão consciente de conceber o protocolo da Fase 11 (Outbox, Tombstones, API + Postgres) do zero em vez de flags estáticas pontuais.
- [x] **Migração de Schema Room v15 (`PlatformDatabase`)**: `MIGRATION_14_15` com recriação atômica de 4 tabelas sob `PRAGMA foreign_keys = OFF / ON` para retrocompatibilidade universal com qualquer SQLite nativo.
- [x] **Suíte de Testes Automatizados**: Atualizado `PlatformDatabaseMigrationTest` validando a migração 14 -> 15. 100% dos testes aprovados.
- [x] **Decisão Arquitetural Documentada**: Criada [ADR 027](.ai/DECISIONS/027-remocao-sync-status-residuo-zero-preparacao-fase-11.md).

### DAO Dedicado para Goal Contributions e Recálculo Derivado de Aportes (v1.13.0)
- [x] **DAO Dedicado (`GoalContributionDao`)**: Criação de DAO próprio com queries atômicas para inserção, sum por meta/período e buscas filtradas por data e meta.
- [x] **Recálculo Atômico do Cache de Saldo**: `addContribution` persiste a contribuição datada e recalcula `currentAmountCents` derivado diretamente da soma real das contribuições.
- [x] **Granularidade Temporal no Repositório**: Exposição de `getContributionsForPeriod(goalId, startDate, endDate)` para apuração mensal e histórico de metas.
- [x] **Suíte de Testes Automatizados**: Criado `GoalRepositoryImplTest` cobrindo múltiplos aportes em meses diferentes, recálculo e filtros temporais. 100% dos testes unitários passando.
### Remoção do Seed Fictício de Cartões e Centralização de Seeds no Primeiro Uso (v1.14.0)
- [x] **Eliminação de Dados Fictícios de Cartões**: Remoção definitiva do método `seedInitialCreditCardsIfEmpty()` que recriava dados falsos ("Cartão Principal" e "Cartão Secundário").
- [x] **Empty State com CTA em `CreditCardsScreen`**: Exibição de empty state elegante com botão de ação `"Cadastrar primeiro cartão"`.
- [x] **Centralização no Startup (`SeedInitialDataUseCase`)**: Inicialização única no `onCreate` do `PlatformApplication`, aplicando categorias, itens, contatos, contas e formas de pagamento padrão apenas no primeiro uso real.
- [x] **Controle por Flag em `PreferencesManager` (`seeds_applied`)**: Fim da dependência em checagens do tipo "tabela vazia". Se o usuário excluir todos os dados de qualquer tabela, o app respeita e não recria nada.
- [x] **Limpeza nos ViewModels**: Remoção de chamadas concorrentes e redundantes de seeding nos blocos `init` de 7 ViewModels (`HomeViewModel`, `BillsViewModel`, `NewExpenseViewModel`, `ManagementViewModel`, `ExpenseItemsViewModel`, `CreditCardsViewModel`, `DashboardViewModel`).
- [x] **Suíte de Testes Automatizados**: Criado `FinancialRepositoryImplSeedTest` validando a garantia de resíduo zero (deleção total do usuário sem re-criação de registros) e atualização de `ExpenseItemsViewModelTest`. 100% dos testes unitários passando.
### Unificação da Geração de Parcelas no `CreateBillUseCase` (v1.15.0)
- [x] **Extensão de Domínio (`CreateBillUseCase`)**: Inclusão de suporte opcional a `CreditCard?`, `isFirstInstallmentPaid` e `actualPaymentDate`, centralizando a criação de contas e parcelas em uma única fonte da verdade.
- [x] **Reaproveitamento de `CalculateInstallmentsUseCase`**: Datas-base, contagem e divisão exata de centavos com resto na 1ª parcela unificadas para despesas com e sem cartão de crédito.
- [x] **Mapeamento Automático de Faturas**: Associação dinâmica com a competência da fatura via `CreditCardCalculator.determineInvoiceReferenceMonth` e `repository.getOrCreateInvoiceForMonth`, atualizando o vencimento e o `invoiceId` da parcela.
- [x] **Saneamento Arquitetural em `NewExpenseViewModel`**: Remoção de ~150 linhas de duplicação inline, alinhando a ViewModel estritamente com as fronteiras de Clean Architecture.
- [x] **Suíte de Testes Automatizados**: Criado `CreateBillUseCaseTest` cobrindo paridade total de valores, centavos e datas-base entre fluxo com e sem cartão, despesas à vista e recorrentes. 100% dos testes unitários passando.

### Regra Única de Limite de Cartão de Crédito e Faturas Sob Demanda (v1.16.0)
- [x] **Caso de Uso Centralizado (`GetCreditCardSummariesUseCase`)**: Cálculo canônico de limite consumido e disponível de cartões.
- [x] **Regra Canônica de Limite**: `INSTALLMENT` consome o saldo devedor restante de todas as parcelas não pagas; `RECURRING` e `SINGLE` consomem exclusivamente a fatura aberta/fechada atual não paga.
- [x] **Desacoplamento e Correção de ViewModels**: `CreditCardsViewModel` e `NewExpenseViewModel` agora delegam integralmente o cálculo ao use case, corrigindo soma indevida de cartões misturados e duplicações.
- [x] **Faturas Recorrentes Sob Demanda**: `CreateBillUseCase` vincula apenas a 1ª ocorrência à fatura inicial em `RECURRING`, prevenindo a pré-criação desnecessária de 11 faturas futuras vazias no SQLite.
- [x] **Suíte de Testes Automatizados**: Criado `GetCreditCardSummariesUseCaseTest` cobrindo cenários com 1 parcelado 10x, 1 assinatura e 1 compra à vista, além da liberação do limite após quitação de fatura. 100% dos testes unitários aprovados.

### Otimização SQL de Pagamento e Totais de Faturas (v1.17.0)
- [x] **Queries SQL de Alta Performance (`BillInstallmentDao`)**: Atualização em lote via `UPDATE bill_installments ... WHERE invoiceId = :invoiceId` e agregação direta via `SELECT invoiceId, COALESCE(SUM(amountCents), 0) ... GROUP BY invoiceId`.
- [x] **Delegação ao SQLite & Resíduo Zero de Memória**: Eliminação de `getAllInstallmentsList()` e `getAllInstallments()` com filtros e loops em Kotlin para quitação, reabertura e totais de faturas.
- [x] **Data Real de Pagamento (`actualPaymentDate`)**: Suporte a data real no método `payInvoice` (padrão = hoje), alinhado com a ADR 020 e indicadores de pontualidade.
- [x] **Transações Atômicas Mantidas**: Execução protegida por `database.withTransaction` garantindo integridade entre `credit_card_invoices` e `bill_installments`.
- [x] **Suíte de Testes Automatizados**: Criado `FinancialRepositoryInvoiceTest` validando isolamento por fatura na quitação/reabertura, fallback para data corrente e mapeamento de totais. 100% dos testes unitários verdes (154 testes).

### Modularização e Decomposição de Telas Composable (v1.18.0)
- [x] **Decomposição Modular das 7 Telas Gigantes (Refactor 1:1)**:
  - `HomeScreen.kt`: De 2.215 para 199 linhas (-91%). Componentes: `HomeTopBar.kt`, `HomePanoramaView.kt`, `HomeCalendarView.kt`, `HomeMonthlyView.kt`, `HomePayableComponents.kt`.
  - `ManagementScreen.kt`: De 2.050 para 326 linhas (-84%). Componentes: `AccountsComponents.kt`, `PaymentMethodsComponents.kt`, `CategoriesComponents.kt`, `ManagementDialogs.kt`.
  - `RecurringInstallmentsScreen.kt`: De 2.076 para 457 linhas (-78%). Componentes: `BillPlanCard.kt`, `RecurringBillCard.kt`, `RecurringDetailBottomSheet.kt`, `InstallmentRow.kt`, `TimelineMonthCard.kt`, `RecurringDialogs.kt`.
  - `StatisticsScreen.kt`: De 1.927 para 185 linhas (-90%). Componentes: `MonthNavigationHeader.kt`, `PastStatisticsCards.kt`, `PresentStatisticsCards.kt`, `NatureDistributionCard.kt`, `PaymentMethodDistributionCard.kt`, `FutureStatisticsCards.kt`, `StatisticsCommonComponents.kt`.
  - `SettingsScreen.kt`: De 1.454 para 273 linhas (-81%). Componentes: `SettingsSections.kt`, `ReleaseNotesDialog.kt`, `BackupPasswordDialogs.kt`, `AppearanceBottomSheet.kt`, `SettingsCommonComponents.kt`.
  - `NewExpenseScreen.kt`: De 1.217 para 216 linhas (-82%). Componentes: `ExpenseItemAndAmountCard.kt`, `QuickContactDialog.kt`, `ExpenseRecipientAndDueDateCard.kt`, `ExpensePaymentStatusCard.kt`, `ExpenseCommitmentTypeCard.kt`, `ExpensePaymentMethodCard.kt`.
  - `BillsScreen.kt`: De 1.210 para 598 linhas (-50%). Componentes: `BillsFilterBar.kt`, `BillsMiniKpiBar.kt`, `BillInstallmentItemCard.kt`, `EmptyBillsState.kt`, `BatchDeleteDialog.kt`.
- [x] **Regra de Governança Composable (~600 linhas)**: Registrada no `AGENT_RULES.md` e `.agents/rules/coding_standards.md` a regra mandatória de que nenhum arquivo Composable deve ultrapassar ~600 linhas, promovendo componentização cirúrgica e otimização de contexto para IA.
- [x] **100% de Paridade e Testes Unitários Verdes**: Comportamento visual e de negócio estritamente preservado 1:1, com validação e aprovação de toda a suíte de testes unitários a cada tela.

### Governança e Testes Automatizados
- [x] Cobertura de testes unitários executada com 100% de sucesso via Gradle (`./gradlew testDebugUnitTest`).
- [x] Script de versionamento móvel sincronizado (`scripts/bump-version.ps1` e `scripts/bump-version.sh`).
- [x] Regra mandatória de governança e sincronização de versão em `.agents/rules/governance_and_versioning.md`.

---

## 🎯 Próximos Passos
- [ ] Fase 11: Estruturação da API remota e sincronização bidirecional offline-first com banco em nuvem.
