# Changelog - Platform (Android App)

Todas as alteraÃƒÂ§ÃƒÂµes notÃƒÂ¡veis neste projeto serÃƒÂ£o documentadas neste arquivo.
O formato ÃƒÂ© baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/) e este projeto segue [Versionamento SemÃƒÂ¢ntico](https://semver.org/lang/pt-BR/).

## [1.18.0] - 2026-10-07

### 🧱 Modularização e Decomposição de Telas Composable (Refactor 1:1)

- **Decomposição Modular das 7 Maiores Telas do Aplicativo**:
  - `HomeScreen.kt`: De 2.215 para 199 linhas (-91%). Extraídos `HomeTopBar.kt`, `HomePanoramaView.kt`, `HomeCalendarView.kt`, `HomeMonthlyView.kt` e `HomePayableComponents.kt` no pacote `home/components/`.
  - `ManagementScreen.kt`: De 2.050 para 326 linhas (-84%). Extraídos `AccountsComponents.kt`, `PaymentMethodsComponents.kt`, `CategoriesComponents.kt` e `ManagementDialogs.kt` no pacote `management/components/`.
  - `RecurringInstallmentsScreen.kt`: De 2.076 para 457 linhas (-78%). Extraídos `BillPlanCard.kt`, `RecurringBillCard.kt`, `RecurringDetailBottomSheet.kt`, `InstallmentRow.kt`, `TimelineMonthCard.kt` e `RecurringDialogs.kt` no pacote `recurring/components/`.
  - `StatisticsScreen.kt`: De 1.927 para 185 linhas (-90%). Extraídos `MonthNavigationHeader.kt`, `PastStatisticsCards.kt`, `PresentStatisticsCards.kt`, `NatureDistributionCard.kt`, `PaymentMethodDistributionCard.kt`, `FutureStatisticsCards.kt` e `StatisticsCommonComponents.kt` no pacote `statistics/components/`.
  - `SettingsScreen.kt`: De 1.454 para 273 linhas (-81%). Extraídos `SettingsSections.kt`, `ReleaseNotesDialog.kt`, `BackupPasswordDialogs.kt`, `AppearanceBottomSheet.kt` e `SettingsCommonComponents.kt` no pacote `settings/components/`.
  - `NewExpenseScreen.kt`: De 1.217 para 216 linhas (-82%). Extraídos `ExpenseItemAndAmountCard.kt`, `QuickContactDialog.kt`, `ExpenseRecipientAndDueDateCard.kt`, `ExpensePaymentStatusCard.kt`, `ExpenseCommitmentTypeCard.kt` e `ExpensePaymentMethodCard.kt` no pacote `bills/components/`.
  - `BillsScreen.kt`: De 1.210 para 598 linhas (-50%). Extraídos `BillsFilterBar.kt`, `BillsMiniKpiBar.kt`, `BillInstallmentItemCard.kt`, `EmptyBillsState.kt` e `BatchDeleteDialog.kt` no pacote `bills/components/`.
- **Nova Regra de Governança e Qualidade (`AGENT_RULES.md` & `coding_standards.md`)**:
  - Estabelecido teto mandatório de **máximo ~600 linhas por arquivo Composable**. Composables que excederem esse limite devem ser decompostos cirurgicamente em componentes atômicos (cards, seções, dialogs, bottom sheets) dentro do subpacote `components/` da feature correspondente.
- **Validação e Paridade 1:1**:
  - Preservado rigorosamente todo o comportamento, layout e styling visual existente.
  - Suíte completa de testes unitários executada e aprovada a cada tela refatorada (100% dos testes passando).

---

## [1.17.0] - 2026-10-07

### âš¡ OtimizaÃ§Ã£o SQL de Pagamento e Totais de Faturas (`BillInstallmentDao` & `FinancialRepositoryImpl`)

- **Queries SQL de Alta Performance no Room (`BillInstallmentDao`)**:
  - ImplementaÃ§Ã£o da query atÃ´mica de liquidaÃ§Ã£o: `UPDATE bill_installments SET paidAt = :paidAt, actualPaymentDate = :actualPaymentDate, status = :status WHERE invoiceId = :invoiceId` (`updatePaymentByInvoiceId`).
  - ImplementaÃ§Ã£o da query SQL de agregaÃ§Ã£o direta: `SELECT invoiceId, COALESCE(SUM(amountCents), 0) AS totalAmountCents FROM bill_installments WHERE invoiceId IS NOT NULL GROUP BY invoiceId` (`getInvoiceTotals`).
  - EliminaÃ§Ã£o definitiva da sobrecarga de carregar toda a tabela de parcelas em memÃ³ria (`getAllInstallmentsList()` e `getAllInstallments()`) para filtragens lineares em Kotlin nos fluxos de quitaÃ§Ã£o e totais de faturas.
- **Suporte a Data Real de Pagamento na Baixa de Fatura (`actualPaymentDate`)**:
  - `FinancialRepository.payInvoice` agora aceita `actualPaymentDate: Long? = null` (padrÃ£o = hoje via `System.currentTimeMillis()`), garantindo conformidade com a ADR 020 e prevenindo distorÃ§Ãµes no indicador de pontualidade (`onTimePaymentRate`).
  - AÃ§Ãµes de pagamento em `CreditCardsUiAction.PayInvoice` e `HomeUiAction.PayInvoice` estendidas com `actualPaymentDate` opcional.
- **Atomicidade e TransaÃ§Ãµes Mantidas**:
  - `payInvoice` e `reopenInvoice` continuam executando sob `database.withTransaction`, garantindo consistÃªncia relacional entre a tabela de faturas (`credit_card_invoices`) e as parcelas (`bill_installments`).
- **SuÃ­te de Testes Automatizados**:
  - Criado `FinancialRepositoryInvoiceTest` cobrindo detalhadamente:
    - QuitaÃ§Ã£o de fatura com data real customizada marcando exclusivamente as parcelas da prÃ³pria fatura alvo.
    - QuitaÃ§Ã£o de fatura sem data explicitada aplicando fallback seguro para a data corrente (`paidAt == actualPaymentDate`).
    - Reabertura de fatura (`reopenInvoice`) limpando timestamps e redefinindo status para `PENDING` apenas nas parcelas da fatura em questÃ£o.
    - AgregaÃ§Ã£o reativa de totais de faturas via `getInvoiceTotals()` sem carregar relacionamentos desnecessÃ¡rios em memÃ³ria.
  - 100% dos testes unitÃ¡rios (154 testes) aprovados via Gradle.

---

## [1.16.0] - 2026-10-07

### Ã°Å¸â€™Â³ Regra ÃƒÅ¡nica de Limite de CartÃƒÂ£o de CrÃƒÂ©dito e Faturas Sob Demanda

- **CriaÃƒÂ§ÃƒÂ£o do `GetCreditCardSummariesUseCase`**:
  - Centralizado o cÃƒÂ¡lculo do limite consumido e limite disponÃƒÂ­vel de cartÃƒÂµes de crÃƒÂ©dito em uma ÃƒÂºnica fonte da verdade.
  - Implementada a **Regra CanÃƒÂ´nica de Limite**:
    - **`INSTALLMENT` (Parcelados)**: Consome o saldo devedor restante de todas as parcelas nÃƒÂ£o pagas (atuais e futuras contratadas).
    - **`RECURRING` (Assinaturas) e `SINGLE` (Ãƒâ‚¬ vista)**: Consomem exclusivamente o que pertence ÃƒÂ  fatura aberta ou fechada atual nÃƒÂ£o paga, eliminando o erro de projeÃƒÂ§ÃƒÂ£o que consumia antecipadamente o limite de atÃƒÂ© 12 meses futuros de assinaturas.
- **EliminaÃƒÂ§ÃƒÂ£o de CÃƒÂ¡lculos Duplicados nas ViewModels**:
  - `CreditCardsViewModel` e `NewExpenseViewModel` agora consomem diretamente o Flow reativo de `GetCreditCardSummariesUseCase`.
  - Corrigido vazamento na `NewExpenseViewModel`, que somava parcelas de todos os cartÃƒÂµes indistintamente sem isolamento por `cardId`.
- **Faturas Recorrentes Sob Demanda**:
  - `CreateBillUseCase` atualizado para nÃƒÂ£o mais prÃƒÂ©-criar 11 faturas vazias antecipadas no SQLite para compras do tipo `RECURRING`. Apenas a 1Ã‚Âª ocorrÃƒÂªncia do ciclo inicial ÃƒÂ© atrelada de imediato ÃƒÂ  fatura atual, preservando as projeÃƒÂ§ÃƒÂµes futuras de contas a pagar e gerando faturas sob demanda ÃƒÂ  medida que os ciclos chegam.
- **SuÃƒÂ­te de Testes Automatizados**:
  - Criado `GetCreditCardSummariesUseCaseTest` cobrindo detalhadamente o cenÃƒÂ¡rio de um cartÃƒÂ£o com 1 compra parcelada em 10x, 1 assinatura mensal e 1 compra ÃƒÂ  vista, alÃƒÂ©m da liberaÃƒÂ§ÃƒÂ£o proporcional de limite apÃƒÂ³s pagamento de fatura.
  - Atualizados `CreateBillUseCaseTest` e `NewExpenseViewModelTest`.
  - 100% dos testes unitÃƒÂ¡rios (150 testes) aprovados via Gradle.

---

## [1.15.0] - 2026-10-07

### Ã°Å¸Ââ€ºÃ¯Â¸Â UnificaÃƒÂ§ÃƒÂ£o da GeraÃƒÂ§ÃƒÂ£o de Parcelas no `CreateBillUseCase` e Paridade entre CartÃƒÂ£o e Contas Avulsas

- **ExtensÃƒÂ£o do `CreateBillUseCase` com Suporte a CartÃƒÂ£o de CrÃƒÂ©dito**:
  - `CreateBillUseCase` agora recebe opcionalmente a entidade `CreditCard?`, `isFirstInstallmentPaid: Boolean` e `actualPaymentDate: Long?`.
  - Reutiliza integralmente `CalculateInstallmentsUseCase` para o cÃƒÂ¡lculo canÃƒÂ´nico de contagem de parcelas, divisÃƒÂ£o exata de centavos com resto na 1Ã‚Âª parcela e datas-base de ocorrÃƒÂªncia.
  - Quando a despesa for no cartÃƒÂ£o (`creditCard != null`), mapeia cada ocorrÃƒÂªncia para sua respectiva fatura usando `CreditCardCalculator.determineInvoiceReferenceMonth()` e `repository.getOrCreateInvoiceForMonth()`, vinculando `invoiceId` e ajustando o `dueDate` da parcela para o vencimento da fatura.
  - Atualiza atomicamente a `bill` vinculando o `invoiceId` inicial e persistindo conta e parcelas em transaÃƒÂ§ÃƒÂ£o Room.
- **EliminaÃƒÂ§ÃƒÂ£o de CÃƒÂ³digo Duplicado Inline na `NewExpenseViewModel`**:
  - Removido bloco redundante de ~150 linhas em `NewExpenseViewModel.saveExpense()` que recalculava parcelas, datas, resto de centavos e faturas inline.
  - Resolvida a divergÃƒÂªncia de contagem de parcelas (`coerceAtLeast(2)` vs `coerceAtLeast(1)`), restabelecendo o alinhamento estrito com as fronteiras de Clean Architecture.
- **Testes Automatizados de Paridade**:
  - Criado `CreateBillUseCaseTest` validando a paridade absoluta entre os fluxos com e sem cartÃƒÂ£o:
    - Mesma divisÃƒÂ£o exata de centavos e mesmo valor individual por parcela.
    - AlocaÃƒÂ§ÃƒÂ£o consistente do resto de centavos na 1Ã‚Âª parcela.
    - Mesmas datas-base de ocorrÃƒÂªncia utilizadas para geraÃƒÂ§ÃƒÂ£o das faturas correspondentes.
    - Paridade para parcelamentos (`INSTALLMENT`), despesas ÃƒÂ  vista (`SINGLE`) e recorrÃƒÂªncias (`RECURRING`).
    - Tratamento uniforme de status e timestamp de quitaÃƒÂ§ÃƒÂ£o quando a despesa jÃƒÂ¡ ÃƒÂ© criada como paga.
  - Atualizado `NewExpenseViewModelTest` injetando `CreateBillUseCase`.
  - 100% dos testes unitÃƒÂ¡rios validados via Gradle.

---

## [1.14.0] - 2026-10-07

### Ã°Å¸â€™Â³ RemoÃƒÂ§ÃƒÂ£o do Seed FictÃƒÂ­cio de CartÃƒÂµes e CentralizaÃƒÂ§ÃƒÂ£o de Seeds no Primeiro Uso

- **RemoÃƒÂ§ÃƒÂ£o de Dados FictÃƒÂ­cios de CartÃƒÂµes de CrÃƒÂ©dito**:
  - ExcluÃƒÂ­do o seed automÃƒÂ¡tico que criava "CartÃƒÂ£o Principal" e "CartÃƒÂ£o SecundÃƒÂ¡rio" quando a tabela estava vazia.
  - Implementado empty state refinado em `CreditCardsScreen` com botÃƒÂ£o de Call-To-Action (CTA): `"Cadastrar primeiro cartÃƒÂ£o"`.
  - O app agora respeita quando o usuÃƒÂ¡rio exclui todos os cartÃƒÂµes, nunca mais recriando dados fictÃƒÂ­cios sem autorizaÃƒÂ§ÃƒÂ£o.
- **CentralizaÃƒÂ§ÃƒÂ£o da InicializaÃƒÂ§ÃƒÂ£o no Primeiro Uso (Startup)**:
  - Criado o caso de uso `SeedInitialDataUseCase` e centralizado o bootstrap de entidades de referÃƒÂªncia (Categorias, Itens, Contatos, Contas Financeiras e Formas de Pagamento) em `PlatformApplication.onCreate()`.
  - Controle de primeiro uso governado via flag persistente `seeds_applied` no `PreferencesManager` (Jetpack DataStore) em vez de checagens repetitivas de "tabela vazia".
  - Se o usuÃƒÂ¡rio excluir conscientemente todos os registros de qualquer entidade (categorias, contatos, etc.), eles nÃƒÂ£o sÃƒÂ£o recriados ao abrir o app.
- **Desacoplamento e Limpeza nos ViewModels**:
  - Removidas chamadas de seed dos blocos `init` de 7 ViewModels (`HomeViewModel`, `BillsViewModel`, `NewExpenseViewModel`, `ManagementViewModel`, `ExpenseItemsViewModel`, `CreditCardsViewModel` e `DashboardViewModel`).
  - ReduÃƒÂ§ÃƒÂ£o drÃƒÂ¡stica de overhead de I/O de banco e checagens concorrentes desnecessÃƒÂ¡rias na navegaÃƒÂ§ÃƒÂ£o entre telas.
- **Testes Automatizados & GovernanÃƒÂ§a**:
  - Criado `FinancialRepositoryImplSeedTest` validando a garantia de resÃƒÂ­duo zero: mesmo com todas as tabelas vazias, uma vez que a flag `seeds_applied` estÃƒÂ¡ ativa, nenhuma entidade ÃƒÂ© recriada.
  - Atualizado `ExpenseItemsViewModelTest` com foco no carregamento de dados.
  - Registrado [ADR 029](.ai/DECISIONS/029-remocao-seed-cartoes-e-centralizacao-startup-seeds.md).

---

## [1.13.0] - 2026-10-07

### Ã°Å¸Å½Â¯ DAO Dedicado para Goal Contributions e RecÃƒÂ¡lculo Derivado de Aportes

- **DAO PrÃƒÂ³prio (`GoalContributionDao`)**:
  - Criada a interface dedicada `GoalContributionDao` com consultas especializadas para inserÃƒÂ§ÃƒÂ£o, soma por meta (`sumByGoal`), soma por perÃƒÂ­odo (`sumForPeriod`), listagem por meta e perÃƒÂ­odo (`getByGoalForPeriod`), e deleÃƒÂ§ÃƒÂ£o vinculada.
  - Registrado em `PlatformDatabase` e provido como Singleton no container Hilt em `AppModule.kt`.
- **Registro Datado & RecÃƒÂ¡lculo do Cache de Saldo**:
  - `GoalRepository.addContribution`: insere atomicamente a contribuiÃƒÂ§ÃƒÂ£o datada no banco de dados e recalcula o campo cache `currentAmountCents` da meta a partir da soma real de todas as suas contribuiÃƒÂ§ÃƒÂµes (`sumByGoal`), eliminando riscos de inconsistÃƒÂªncia contÃƒÂ¡bil.
  - `saveGoal`: metas criadas com valor inicial geram automaticamente o primeiro registro histÃƒÂ³rico de contribuiÃƒÂ§ÃƒÂ£o com a data de criaÃƒÂ§ÃƒÂ£o da meta.
- **Consulta Granular por Meta e PerÃƒÂ­odo**:
  - Adicionado o mÃƒÂ©todo `getContributionsForPeriod(goalId, startDate, endDate)` em `GoalRepository` e `GoalRepositoryImpl`, viabilizando apuraÃƒÂ§ÃƒÂ£o por meta em intervalos de datas e histÃƒÂ³rico de aportes.
- **Testes Automatizados**:
  - Criado `GoalRepositoryImplTest` com cobertura completa de mÃƒÂºltiplos aportes em meses distintos, filtros temporais e recÃƒÂ¡lculo do cache.
- **DecisÃƒÂ£o Arquitetural Documentada**:
  - Registrada [ADR 028](.ai/DECISIONS/028-dao-dedicado-goal-contributions-e-recalculo-aportes.md).

---

## [1.12.0] - 2026-10-07

### Ã°Å¸Â§Â¹ RemoÃƒÂ§ÃƒÂ£o de `syncStatus` Residual, ResÃƒÂ­duo Zero e MigraÃƒÂ§ÃƒÂ£o Room v15

- **Expurgo de ResÃƒÂ­duo Morto e PreparaÃƒÂ§ÃƒÂ£o para Fase 11**:
  - Removido o campo legado `syncStatus: String = "PENDENTE"` de todos os modelos de domÃƒÂ­nio (`Category`, `ExpenseItem`, `CreditCard`, `CreditCardInvoice`) e entidades Room (`CategoryEntity`, `ExpenseItemEntity`, `CreditCardEntity`, `CreditCardInvoiceEntity`).
  - O campo consistia em uma tentativa histÃƒÂ³rica e fragmentada de sincronizaÃƒÂ§ÃƒÂ£o sem qualquer consumo na lÃƒÂ³gica do aplicativo, que gerava assimetrias em relaÃƒÂ§ÃƒÂ£o ÃƒÂ s entidades principais (`Bill`, `BillInstallment`, `Contact`, `FinancialAccount`, `Goal`, `Budget`).
  - Adotada formalmente a decisÃƒÂ£o arquitetural de conceber a sincronizaÃƒÂ§ÃƒÂ£o bidirecional na nuvem (Fase 11) do zero, com fila de mutaÃƒÂ§ÃƒÂµes transacional (Outbox), controle de exclusÃƒÂ£o (Tombstones) e detecÃƒÂ§ÃƒÂ£o de conflitos, em vez de flags estÃƒÂ¡ticas pontuais.
- **MigraÃƒÂ§ÃƒÂ£o FÃƒÂ­sica no SQLite (`PlatformDatabase` v15)**:
  - Incrementada a versÃƒÂ£o do banco de dados Room para `version = 15`.
  - Implementada e registrada a migraÃƒÂ§ÃƒÂ£o atÃƒÂ´mica `MIGRATION_14_15` com recriaÃƒÂ§ÃƒÂ£o segura de tabelas (`categories`, `expense_items`, `credit_cards`, `credit_card_invoices`) sob `PRAGMA foreign_keys = OFF / ON`, garantindo compatibilidade universal com qualquer versÃƒÂ£o do SQLite nativo.
- **Higiene e CompactaÃƒÂ§ÃƒÂ£o de Backups**:
  - Arquivos de backup JSON locais protegidos por senha agora sÃƒÂ£o gerados sem chaves `syncStatus` redundantes.
- **Testes Automatizados & GovernanÃƒÂ§a**:
  - Atualizado `PlatformDatabaseMigrationTest` validando a execuÃƒÂ§ÃƒÂ£o estrutural da migraÃƒÂ§ÃƒÂ£o `14 -> 15`.
  - Registrada a decisÃƒÂ£o tÃƒÂ©cnica no [ADR 027](.ai/DECISIONS/027-remocao-sync-status-residuo-zero-preparacao-fase-11.md).

---

## [1.11.1] - 2026-10-07

### Ã°Å¸Â§Â¹ Limpeza de Typealias Obsoleto (`AppDatabase`)

- **RemoÃƒÂ§ÃƒÂ£o de CÃƒÂ³digo Morto Residual**:
  - ExcluÃƒÂ­do o arquivo `AppDatabase.kt` contendo `typealias AppDatabase = PlatformDatabase`.
  - O alias de compatibilidade foi introduzido durante o rename inicial da aplicaÃƒÂ§ÃƒÂ£o e nÃƒÂ£o possuÃƒÂ­a nenhum consumidor em nenhuma camada do projeto.
  - EliminaÃƒÂ§ÃƒÂ£o definitiva de ambiguidades conceituais em favor do canÃƒÂ´nico `PlatformDatabase`.

---

## [1.11.0] - 2026-10-07

### Ã°Å¸Â§Â¹ RemoÃƒÂ§ÃƒÂ£o da Tabela Legada `transactions`, ResÃƒÂ­duo Zero e MigraÃƒÂ§ÃƒÂ£o Room v14

- **PrincÃƒÂ­pio do ResÃƒÂ­duo Zero & Limpeza de CÃƒÂ³digo Legado**:
  - Removidos completamente os artefatos `TransactionEntity.kt` e `TransactionDao.kt`, eliminando classes residuais anteriores ÃƒÂ  consolidaÃƒÂ§ÃƒÂ£o do modelo unificado de Contas e Parcelas (`bills` / `bill_installments`).
  - Removido o provider de injeÃƒÂ§ÃƒÂ£o de dependÃƒÂªncia `@Provides @Singleton fun provideTransactionDao(...)` em `AppModule.kt`.
  - Removido o campo `transactions` e sua serializaÃƒÂ§ÃƒÂ£o/deserializaÃƒÂ§ÃƒÂ£o em `BackupDataDto.kt` e `BackupRepositoryImpl.kt`, eliminando nÃƒÂ³s vazios nos arquivos de backup JSON exportados.
- **MigraÃƒÂ§ÃƒÂ£o FÃƒÂ­sica no SQLite (`PlatformDatabase` v14)**:
  - Incrementada a versÃƒÂ£o do banco de dados Room para `version = 14`.
  - Criada e registrada a migraÃƒÂ§ÃƒÂ£o `MIGRATION_13_14` executando `DROP TABLE IF EXISTS transactions`, limpando tabelas, ÃƒÂ­ndices e chaves estrangeiras obsoletas do SQLite dos dispositivos dos usuÃƒÂ¡rios.
- **Testes e Garantia de Qualidade**:
  - Criado `PlatformDatabaseMigrationTest` validando a execuÃƒÂ§ÃƒÂ£o do comando `DROP TABLE IF EXISTS transactions` na migraÃƒÂ§ÃƒÂ£o `13 -> 14`.
  - Atualizado `BackupRepositoryImplTest` para operaÃƒÂ§ÃƒÂ£o sem dependÃƒÂªncias de transaÃƒÂ§ÃƒÂµes legadas.
  - 100% dos testes unitÃƒÂ¡rios da aplicaÃƒÂ§ÃƒÂ£o aprovados.
- **DecisÃƒÂ£o Arquitetural Documentada**:
  - Registrada [ADR 026](.ai/DECISIONS/026-remocao-tabela-transactions-e-migracao-room-v14.md) documentando a polÃƒÂ­tica de ResÃƒÂ­duo Zero e a migraÃƒÂ§ÃƒÂ£o.

---

## [1.10.0] - 2026-10-07

### Ã°Å¸Å’Â CentralizaÃƒÂ§ÃƒÂ£o de Strings de Interface (UI) e Fonte ÃƒÅ¡nica da Verdade (`AppStrings`)

- **Fonte ÃƒÅ¡nica da Verdade (`AppStrings.kt`)**:
  - Criado o objeto estruturado `AppStrings` em `com.platform.app.presentation.common` agrupando constantes textuais de interface de usuÃƒÂ¡rio sem overhead de contexto Android.
  - Sub-namespaces organizados: `Status`, `Nature`, `AccountType`, `ContactType`, `BillType`, `Actions`, `Home`, `Dialogs`.
  - SincronizaÃƒÂ§ÃƒÂ£o espelhada em `res/values/strings.xml` para paridade com recursos nativos do framework Android e suporte nativo a internacionalizaÃƒÂ§ÃƒÂ£o futura.
- **RefatoraÃƒÂ§ÃƒÂ£o 1:1 Sem AlteraÃƒÂ§ÃƒÂµes de Texto**:
  - SubstituiÃƒÂ§ÃƒÂ£o de termos literais repetidos na camada `presentation/` preservando rigorosamente 1:1 todos os caracteres originais:
    - **Status**: "Pendente", "Pago", "Paga", "JÃƒÂ¡ Paga", "A Pagar", "Vencida", "Vencido", "Atrasado", "Liquidado", "Tudo quitado", "100% quitado", "Aberta", "Fechada", "Pausada".
    - **Natureza do Gasto**: "ObrigatÃƒÂ³rio", "NecessÃƒÂ¡rio", "Deseja", "Nenhum", "PoupanÃƒÂ§a", "Natureza do Gasto", rÃƒÂ³tulos e descriÃƒÂ§ÃƒÂµes conceituais.
    - **Tipos de Conta**: "Conta Corrente", "Carteira / Dinheiro", "PoupanÃƒÂ§a", "Investimento".
    - **Tipos de Contato**: "Pessoa FÃƒÂ­sica", "Fornecedor", "Ãƒâ€œrgÃƒÂ£o PÃƒÂºblico", "Pessoa", "Empresa", "PÃƒÂºblico", "Salvar Contato", "Atualizar Contato", "Excluir Contato".
    - **AÃƒÂ§ÃƒÂµes Comuns**: "Salvar", "Salvar AlteraÃƒÂ§ÃƒÂµes", "Atualizar", "Cancelar", "Excluir", "Confirmar", "Voltar", "Filtrar", "Todos".
    - **Dashboard**: "Restante a Pagar no MÃƒÂªs", "Total vencido:".
- **Telas e Componentes Atualizados**:
  - `HomeScreen`: status badges ("Tudo quitado", "100% quitado", "Pago"), "Total vencido:", "Restante a Pagar no MÃƒÂªs".
  - `BillsScreen`: status de pagamento ("Pago"), aÃƒÂ§ÃƒÂµes de exclusÃƒÂ£o em lote ("Excluir", "Cancelar").
  - `NewExpenseScreen`: status chips ("Pendente", "JÃƒÂ¡ Paga"), botÃƒÂµes de diÃƒÂ¡logo rÃƒÂ¡pido ("Salvar", "Cancelar").
  - `EditInstallmentBottomSheet`: tÃƒÂ­tulos e aÃƒÂ§ÃƒÂµes de confirmaÃƒÂ§ÃƒÂ£o/exclusÃƒÂ£o ("Salvar AlteraÃƒÂ§ÃƒÂµes", "Excluir Registro", "Excluir", "Cancelar").
  - `ConfirmPaymentDialog`: aÃƒÂ§ÃƒÂµes de confirmaÃƒÂ§ÃƒÂ£o ("Confirmar", "Cancelar").
  - `ContactsScreen` & `ContactDetailScreen`: chips de tipo ("Pessoa", "Empresa", "PÃƒÂºblico"), badges, filtros agregados ("Todos", "Pessoa FÃƒÂ­sica", "Fornecedor", "Ãƒâ€œrgÃƒÂ£o PÃƒÂºblico"), botÃƒÂµes de aÃƒÂ§ÃƒÂ£o e exclusÃƒÂ£o.
  - `ManagementScreen`: abas e cards de gestÃƒÂ£o com status ("Pendente", "Liquidado", "Pago"), "Natureza do Gasto:", diÃƒÂ¡logos de contas, mÃƒÂ©todos e categorias ("Salvar", "Atualizar", "Cancelar", "Excluir").
  - `StatisticsScreen`: breakdown de status do mÃƒÂªs ("Pago", "Pendente", "Atrasado").
  - `CreditCardsScreen`: badges de fatura ("Aberta", "Fechada", "Paga"), diÃƒÂ¡logo de exclusÃƒÂ£o de cartÃƒÂ£o ("Cancelar", "Excluir").
  - `ExpenseItemsScreen`: chips e descriÃƒÂ§ÃƒÂµes de natureza, botÃƒÂµes do modal ("Salvar", "Salvar AlteraÃƒÂ§ÃƒÂµes", "Cancelar", "Excluir").
  - `GoalsScreen` & `BudgetsScreen`: botÃƒÂµes de salvamento, ediÃƒÂ§ÃƒÂ£o, cancelamento e exclusÃƒÂ£o.
  - `RecurringInstallmentsScreen` & `AdjustInstallmentDialog`: rÃƒÂ³tulos de status ("Pago"), diÃƒÂ¡logos de cancelamento/exclusÃƒÂ£o/pausa ("Excluir", "Cancelar", "Voltar").
  - `SettingsScreen`: diÃƒÂ¡logos de backup criptografado e restauraÃƒÂ§ÃƒÂ£o ("Cancelar").
- **Testes Automatizados**:
  - Criado `AppStringsTest` garantindo que todos os namespaces e constantes de interface possuam os valores contratuais esperados e previnam regressÃƒÂµes acidentais.
  - SuÃƒÂ­te completa de 125+ testes unitÃƒÂ¡rios executada com 100% de sucesso (`BUILD SUCCESSFUL`).
- **SeguranÃƒÂ§a para RefatoraÃƒÂ§ÃƒÂµes Futuras**:
  - Quaisquer futuras renomeaÃƒÂ§ÃƒÂµes conceituais (ex: "Pendente" $\rightarrow$ "A Pagar", "Vencida" $\rightarrow$ "Em Atraso") agora sÃƒÂ£o realizadas de forma pontual e atÃƒÂ´mica em um ÃƒÂºnico arquivo, com zero risco de divergÃƒÂªncia entre telas.

---

## [1.9.0] - 2026-10-07

### Ã°Å¸â€â€™ Criptografia AES-256-GCM para Backups com ProteÃƒÂ§ÃƒÂ£o por Senha/PIN

- **Criptografia SimÃƒÂ©trica Forte (AEAD AES-256-GCM)**:
  - Implementada criptografia simÃƒÂ©trica autenticada (`AES/GCM/NoPadding`) de 256 bits via `BackupCryptoHelper`, eliminando o risco de vazamento de dados em texto plano em backups locais e compartilhamentos via ShareSheet (WhatsApp, E-mail, etc.).
  - DerivaÃƒÂ§ÃƒÂ£o de chaves via `PBKDF2WithHmacSHA256` com 65.536 iteraÃƒÂ§ÃƒÂµes, sal criptogrÃƒÂ¡fico aleatÃƒÂ³rio de 16 bytes e IV aleatÃƒÂ³rio de 12 bytes gerados via `SecureRandom`.
  - Descarte defensivo imediato da senha em memÃƒÂ³ria RAM (`clearPassword()` e preenchimento com zeros).
- **Envelope CriptogrÃƒÂ¡fico Seguro (`EncryptedBackupDto`)**:
  - Dados exportados em envelope versionado contendo parÃƒÂ¢metros do KDF, vetor de inicializaÃƒÂ§ÃƒÂ£o e o criptograma em Base64, protegendo 100% dos dados financeiros contra inspeÃƒÂ§ÃƒÂ£o nÃƒÂ£o autorizada.
- **Contratos e Casos de Uso com Senha**:
  - `BackupRepository`: atualizado para exigir senha em `exportBackupJson(password)` e `restoreBackupFromJson(backupJson, password)`.
  - `ExportBackupUseCase` e `RestoreBackupUseCase`: validaÃƒÂ§ÃƒÂ£o mandatÃƒÂ³ria de preenchimento e integridade.
- **Interface e ExperiÃƒÂªncia do UsuÃƒÂ¡rio (UI/UX)**:
  - `SettingsScreen`: diÃƒÂ¡logos dedicados `CreateBackupPasswordDialog` (criaÃƒÂ§ÃƒÂ£o e confirmaÃƒÂ§ÃƒÂ£o de senha de no mÃƒÂ­nimo 4 caracteres com alertas visuais) e `RestorePasswordDialog` (solicitaÃƒÂ§ÃƒÂ£o de senha para descriptografia e restauraÃƒÂ§ÃƒÂ£o).
  - Tratamento de erro detalhado informando explicitamente quando a senha/PIN fornecido estiver incorreto.
- **Testes Automatizados**:
  - `BackupCryptoHelperTest`: validaÃƒÂ§ÃƒÂ£o de ciclo completo de criptografia/descriptografia, confidencialidade do ciphertext contra vazamento de tokens, integridade contra adulteraÃƒÂ§ÃƒÂ£o e rejeiÃƒÂ§ÃƒÂ£o de senhas incorretas.
  - `BackupRepositoryImplTest`: validaÃƒÂ§ÃƒÂ£o do ciclo exportaÃƒÂ§ÃƒÂ£o $\rightarrow$ restauraÃƒÂ§ÃƒÂ£o com senha correta e rejeiÃƒÂ§ÃƒÂ£o atÃƒÂ´mica com senha incorreta.
  - `SettingsViewModelTest`: testes atualizados com a nova API protegida por senha.
- **GovernanÃƒÂ§a & Arquitetura**:
  - Registrada a [ADR 025: Criptografia SimÃƒÂ©trica AES-256-GCM para Backups com ProteÃƒÂ§ÃƒÂ£o por Senha/PIN](.ai/DECISIONS/025-criptografia-aes-gcm-backups-protegidos-por-senha.md).

---

## [1.8.0] - 2026-10-07

### Ã°Å¸â€™Â³ TipificaÃƒÂ§ÃƒÂ£o de Contas Financeiras (`FinancialAccountType`) & MigraÃƒÂ§ÃƒÂ£o Room v13

- **Enum de DomÃƒÂ­nio Puro (`FinancialAccountType`)**:
  - Criado o enum `FinancialAccountType` (`CORRENTE`, `CARTEIRA`, `POUPANCA`, `INVESTIMENTO`) com rÃƒÂ³tulos amigÃƒÂ¡veis (`displayName`) e resoluÃƒÂ§ÃƒÂ£o de aliases legados (`fromString`).
  - SubstituÃƒÂ­do o campo `accountType: String` livre em `FinancialAccount` pelo enum tipado.
- **EvoluÃƒÂ§ÃƒÂ£o de PersistÃƒÂªncia no Room (`PlatformDatabase` v13)**:
  - Criado `FinancialAccountTypeConverter` para serializaÃƒÂ§ÃƒÂ£o e deserializaÃƒÂ§ÃƒÂ£o no banco SQLite.
  - Implementada a migraÃƒÂ§ÃƒÂ£o `MIGRATION_12_13`, normalizando strings legadas (`CHECKING`, `CASH`, `SAVINGS`, `Conta Corrente`, etc.) para os nomes canÃƒÂ´nicos do enum.
  - Registrada a migraÃƒÂ§ÃƒÂ£o no provider do `AppModule`.
- **Mapeamento em Entidades e Casos de Uso**:
  - `FinancialAccountEntity` atualizado com o enum e mapeamento bidirecional `toDomain()` / `fromDomain()`.
  - MÃƒÂ©trica de dashboard `AccountSpend` atualizada com o campo `accountType: FinancialAccountType`, mapeando rÃƒÂ³tulo e tipo via `GetFinancialDashboardUseCase`.
- **Interface e ExperiÃƒÂªncia do UsuÃƒÂ¡rio (UI/UX)**:
  - `ManagementScreen`: badges e listas de detalhe exibem `account.accountType.displayName`. DiÃƒÂ¡logo de criaÃƒÂ§ÃƒÂ£o e ediÃƒÂ§ÃƒÂ£o (`AddEditAccountDialog`) utiliza dropdown baseado em `FinancialAccountType.entries` com ÃƒÂ­cones temÃƒÂ¡ticos contextuais.
  - `StatisticsScreen`: card `AccountsDistributionCard` exibe ÃƒÂ­cones representativos (`AccountBalance`, `Payments`, `Savings`, `TrendingUp`) e subtÃƒÂ­tulo informativo com o tipo da conta.
- **Testes Automatizados**:
  - Novos testes unitÃƒÂ¡rios em `FinancialAccountTypeTest` cobrindo conversÃƒÂ£o de aliases, display names, fallbacks, `FinancialAccountTypeConverter` e `FinancialAccountEntity`.
  - AtualizaÃƒÂ§ÃƒÂ£o dos testes em `ManagementViewModelTest`.
- **GovernanÃƒÂ§a & Arquitetura**:
  - Registrada a [ADR 024: Enum FinancialAccountType e MigraÃƒÂ§ÃƒÂ£o Room v13](.ai/DECISIONS/024-enum-financial-account-type-e-migracao-room.md).

---

## [1.7.0] - 2026-10-07

### Ã°Å¸â€˜Â¥ TipificaÃƒÂ§ÃƒÂ£o de Contatos (`ContactType`) & MigraÃƒÂ§ÃƒÂ£o Room v12

- **Enum de DomÃƒÂ­nio Puro (`ContactType`)**:
  - Introduzido o enum `ContactType` (`PESSOA_FISICA`, `FORNECEDOR`, `ORGAO_PUBLICO`) com mÃƒÂ©todo utilitÃƒÂ¡rio resiliente `fromString()`.
  - Atualizado o modelo `Contact` para conter `val type: ContactType = ContactType.FORNECEDOR`.
- **EvoluÃƒÂ§ÃƒÂ£o de PersistÃƒÂªncia no Room (`PlatformDatabase` v12)**:
  - Adicionada a coluna `type TEXT NOT NULL DEFAULT 'FORNECEDOR'` na tabela `contacts` via migraÃƒÂ§ÃƒÂ£o `MIGRATION_11_12`.
  - Registrada a migraÃƒÂ§ÃƒÂ£o no `AppModule` preservando a integridade dos dados existentes.
- **Filtros e Agrupamento na Tela de Contatos (`ContactsScreen`)**:
  - Adicionada barra de chips de filtro superior (`ContactTypeFilterRow`) com contadores dinÃƒÂ¢micos para "Todos", "Pessoa FÃƒÂ­sica", "Fornecedor" e "Ãƒâ€œrgÃƒÂ£o PÃƒÂºblico".
  - Agrupamento visual automÃƒÂ¡tico por seÃƒÂ§ÃƒÂµes temÃƒÂ¡ticas (`ContactSectionHeader`) quando a visualizaÃƒÂ§ÃƒÂ£o estiver em "Todos".
  - Filtro exclusivo e direto ao selecionar um tipo especÃƒÂ­fico.
- **Indicadores Visuais de Tipo de Contato (`ContactTypeBadge` & `PlatformAvatar`)**:
  - Chip temÃƒÂ¡tico com ÃƒÂ­cone dedicado (`Person`, `Business`, `AccountBalance`) e paleta semÃƒÂ¢ntica em cada card de contato.
  - Avatar colorido dinamicamente com a identidade visual do tipo.
  - IntegraÃƒÂ§ÃƒÂ£o do badge no cabeÃƒÂ§alho da tela de detalhes (`ContactDetailScreen`).
- **Seletor de Tipo no FormulÃƒÂ¡rio (`AddContactBottomSheet`)**:
  - Seletor ergonÃƒÂ´mico em cartÃƒÂµes para escolha rÃƒÂ¡pida do tipo de contato durante criaÃƒÂ§ÃƒÂ£o ou ediÃƒÂ§ÃƒÂ£o.
- **Testes Automatizados**:
  - Novos testes unitÃƒÂ¡rios em `ContactsViewModelTest` cobrindo filtragem por cada tipo, restauraÃƒÂ§ÃƒÂ£o e busca textual combinada com filtro.
- **GovernanÃƒÂ§a & Arquitetura**:
  - Criada a [ADR 023: TipificaÃƒÂ§ÃƒÂ£o de Contatos (ContactType) e MigraÃƒÂ§ÃƒÂ£o Room v12](.ai/DECISIONS/023-tipificacao-contatos-e-migracao-room.md).

---

## [1.6.1] - 2026-10-07

### Ã°Å¸Å½Â¨ SemÃƒÂ¢ntica e Hierarquia Visual na VisÃƒÂ£o Mensal (`HomeScreen`)

- **RÃƒÂ³tulo SemÃƒÂ¢ntico "Restante a Pagar no MÃƒÂªs"**:
  - Renomeado de "Total Previsto no MÃƒÂªs" para "Restante a Pagar no MÃƒÂªs" no card executivo `ForecastImpactCard`, eliminando a ambiguidade com o total orÃƒÂ§ado e deixando claro que o montante destacado reflete apenas o saldo ainda em aberto.
- **Hierarquia Visual ReforÃƒÂ§ada para QuitaÃƒÂ§ÃƒÂ£o Completa**:
  - Quando todas as contas do mÃƒÂªs estiverem pagas (`R$ 0,00` restante), a UI destaca o badge de sucesso **"Tudo quitado"** com ÃƒÂ­cone de confirmaÃƒÂ§ÃƒÂ£o no topo do card, **antes** da exibiÃƒÂ§ÃƒÂ£o do valor.
  - O valor `R$ 0,00` adota a tonalidade esmeralda de sucesso (`SuccessGreen`), reforÃƒÂ§ando visualmente a conquista de quitaÃƒÂ§ÃƒÂ£o em vez de sugerir ausÃƒÂªncia de dados ou valor zerado neutro.

---

## [1.6.0] - 2026-10-07

### Ã°Å¸â€â€ž ExtensÃƒÂ£o ContÃƒÂ­nua de RecorrÃƒÂªncias FOREVER (Janela Deslizante AutomÃƒÂ¡tica)

- **GeraÃƒÂ§ÃƒÂ£o DinÃƒÂ¢mica de PrÃƒÂ³ximas OcorrÃƒÂªncias (`CalculateInstallmentsUseCase`)**:
  - Novo mÃƒÂ©todo `generateNextRecurringInstallments(bill, existingInstallments, countToAdd)` para gerar parcelas subsequentes sem descontinuidade.
  - CÃƒÂ¡lculo de passos ancorado na primeira parcela para evitar desvios cumulativos de dia de vencimento (ex: preservaÃƒÂ§ÃƒÂ£o estrita do dia 31 apÃƒÂ³s meses menores como fevereiro).
  - Continuidade estrita da numeraÃƒÂ§ÃƒÂ£o das parcelas (`installmentNumber = lastNumber + 1..lastNumber + count`) e atualizaÃƒÂ§ÃƒÂ£o do total projetado.
- **Caso de Uso de ExtensÃƒÂ£o de Janela (`ExtendRecurringBillsUseCase`)**:
  - Novo caso de uso de domÃƒÂ­nio puro (`domain/usecase/ExtendRecurringBillsUseCase`), sem dependÃƒÂªncias do framework Android.
  - AvaliaÃƒÂ§ÃƒÂ£o de limiares por frequÃƒÂªncia:
    - `MONTHLY`: estende em +12 parcelas quando restarem $\le 3$ ocorrÃƒÂªncias futuras ou horizonte $\le 3$ meses.
    - `DAILY`: estende em +30 parcelas quando restarem $\le 7$ ocorrÃƒÂªncias.
    - `WEEKLY`: estende em +26 parcelas quando restarem $\le 4$ ocorrÃƒÂªncias.
    - `YEARLY`: estende em +5 parcelas quando restar $\le 1$ ocorrÃƒÂªncia.
  - LaÃƒÂ§o auto-recuperativo com limite de seguranÃƒÂ§a (`maxBatches = 5`), garantindo projeÃƒÂ§ÃƒÂ£o adequada mesmo apÃƒÂ³s longos perÃƒÂ­odos sem abertura do app.
  - IdempotÃƒÂªncia total e garantia de que contas `BY_OCCURRENCES` e `UNTIL_DATE` permaneÃƒÂ§am estritamente delimitadas.
- **Suporte Transacional no RepositÃƒÂ³rio & DAO (`FinancialRepositoryImpl` & `BillInstallmentDao`)**:
  - Novo mÃƒÂ©todo `addInstallments(bill, newInstallments)` com inserÃƒÂ§ÃƒÂ£o transacional no Room e atualizaÃƒÂ§ÃƒÂ£o do `recurrenceEndDate` e `totalInstallments` na tabela `bills`.
  - Nova consulta detalhada `getInstallmentsWithDetailsByBillId(billId)` em `BillInstallmentDao`.
- **Garantia de ExecuÃƒÂ§ÃƒÂ£o Oportuna e PeriÃƒÂ³dica**:
  - InvocaÃƒÂ§ÃƒÂ£o automÃƒÂ¡tica ao inicializar e atualizar `DashboardViewModel`, `BillsViewModel` (Registros) e `RecurringInstallmentsViewModel` (Assinaturas).
  - ExecuÃƒÂ§ÃƒÂ£o matinal em segundo plano no `DueReminderReceiver` (integrado com o `AlarmManager` existente) e no desbloqueio do app em `MainActivity`.
- **Testes Automatizados**:
  - Nova suÃƒÂ­te de testes unitÃƒÂ¡rios `ExtendRecurringBillsUseCaseTest` validando extensÃƒÂ£o pontual, idempotÃƒÂªncia com janelas cheias, isolamento de contas nÃƒÂ£o-FOREVER e recuperaÃƒÂ§ÃƒÂ£o temporal.
  - Novos testes em `CalculateInstallmentsUseCaseTest` cobrindo `generateNextRecurringInstallments` e resiliÃƒÂªncia a exclusÃƒÂ£o de parcelas antigas.
  - AtualizaÃƒÂ§ÃƒÂ£o dos testes de apresentaÃƒÂ§ÃƒÂ£o em `BillsViewModelTest` e `RecurringInstallmentsViewModelTest`.
- **GovernanÃƒÂ§a & Arquitetura**:
  - CriaÃƒÂ§ÃƒÂ£o da [ADR 022: ExtensÃƒÂ£o ContÃƒÂ­nua de RecorrÃƒÂªncias FOREVER via Janela Deslizante AutomÃƒÂ¡tica](.ai/DECISIONS/022-extensao-continua-recorrencias-forever.md).

---

## [1.5.0] - 2026-10-07

### Ã°Å¸Å½Â¯ Rigidez OrÃƒÂ§amentÃƒÂ¡ria Real 50-30-20 & IntegraÃƒÂ§ÃƒÂ£o de Metas (PoupanÃƒÂ§a)

- **IntegraÃƒÂ§ÃƒÂ£o HolÃƒÂ­stica de Metas Financeiras como PoupanÃƒÂ§a (`GetFinancialDashboardUseCase`)**:
  - InjeÃƒÂ§ÃƒÂ£o de `GoalRepository` no caso de uso do dashboard financeiro.
  - ApuraÃƒÂ§ÃƒÂ£o reativa dos aportes mensais em metas atravÃƒÂ©s do novo mÃƒÂ©todo `goalRepository.getMonthlyContribution(startOfMonth, endOfMonth)`.
  - Base de cÃƒÂ¡lculo orÃƒÂ§amentÃƒÂ¡ria unificada: $\text{Total OrÃƒÂ§ado} = \text{Total Contas do MÃƒÂªs} + \text{Aportes em Metas}$.
  - DistribuiÃƒÂ§ÃƒÂ£o percentual das 4 naturezas (`OBRIGATORIO`, `NECESSARIO`, `DESEJA`, `NENHUM`) e da PoupanÃƒÂ§a agora somam exatamente 100% da capacidade orÃƒÂ§amentÃƒÂ¡ria.
- **Motor de DiagnÃƒÂ³stico Multifaixas de Rigidez (`BudgetRigidityCalculator`)**:
  - EliminaÃƒÂ§ÃƒÂ£o definitiva do falso diagnÃƒÂ³stico de "Excelente" quando Deseja e PoupanÃƒÂ§a estiverem zerados (`SOBRECARREGADO`: 100% comprometido com despesas bÃƒÂ¡sicas, sem margem para estilo de vida ou reservas).
  - 8 estados e faixas estritas de classificaÃƒÂ§ÃƒÂ£o: `VAZIO`, `SOBRECARREGADO`, `ENGESSADO`, `ESTILO_DE_VIDA_ELEVADO`, `SEM_POUPANCA`, `NAO_CLASSIFICADO`, `EQUILIBRADO` e `EXCELENTE`.
  - CritÃƒÂ©rio rigoroso para selo "Excelente": Essenciais $\le 60\%$ (ObrigatÃƒÂ³rio $\le 50\%$), Desejos entre $10\%$ e $35\%$, PoupanÃƒÂ§a $\ge 15\%$ e NÃƒÂ£o Classificados $\le 10\%$.
- **HistÃƒÂ³rico de Aportes no Room & MigraÃƒÂ§ÃƒÂ£o v11 (`PlatformDatabase`)**:
  - CriaÃƒÂ§ÃƒÂ£o da tabela e entidade `goal_contributions` (`id`, `goalId`, `amountCents`, `date`) com chave estrangeira e exclusÃƒÂ£o em cascata.
  - MigraÃƒÂ§ÃƒÂ£o de banco de dados Room `MIGRATION_10_11` (`PlatformDatabase` v11) com auto-migraÃƒÂ§ÃƒÂ£o de saldos existentes e registro no `AppModule`.
  - ExportaÃƒÂ§ÃƒÂ£o e restauraÃƒÂ§ÃƒÂ£o segura de histÃƒÂ³rico de aportes via Storage Access Framework em `BackupDataDto` e `BackupRepositoryImpl`.
- **Card Visual Aprimorado (`NatureDistributionCard`)**:
  - RÃƒÂ³tulo e texto contextualizados: diagnÃƒÂ³stico semafÃƒÂ³rico inteligente com tÃƒÂ­tulo em destaque e descriÃƒÂ§ÃƒÂ£o orientadora.
  - InclusÃƒÂ£o visual dedicada da perna **"PoupanÃƒÂ§a (Metas)"** com indicador esmeralda (`#10B981`), valor em centavos formatado e barra de progresso.
  - RodapÃƒÂ© elegante com exibiÃƒÂ§ÃƒÂ£o do **Total OrÃƒÂ§ado (Despesas + Aportes)**.
- **SuÃƒÂ­te de Testes Automatizados**:
  - Novos testes unitÃƒÂ¡rios em `GetFinancialDashboardUseCaseTest` cobrindo cenÃƒÂ¡rio com 0% em Deseja/PoupanÃƒÂ§a (assegurando que nÃƒÂ£o sai "Excelente"), distribuiÃƒÂ§ÃƒÂ£o 50-30-20 equilibrada real com aportes de metas e cenÃƒÂ¡rios sem poupanÃƒÂ§a.
  - Nova suÃƒÂ­te de testes de domÃƒÂ­nio `BudgetRigidityCalculatorTest` validando todas as fronteiras e estados da matriz de classificaÃƒÂ§ÃƒÂ£o.
- **GovernanÃƒÂ§a e Arquitetura**:
  - CriaÃƒÂ§ÃƒÂ£o da [ADR 021: Rigidez OrÃƒÂ§amentÃƒÂ¡ria 50-30-20 Real com Metas (PoupanÃƒÂ§a) e ClassificaÃƒÂ§ÃƒÂ£o Multifaixas](.ai/DECISIONS/021-rigidez-orcamentaria-50-30-20-com-metas.md).
  - SincronizaÃƒÂ§ÃƒÂ£o do diÃƒÂ¡logo de novidades `ReleaseNotesDialog` em `SettingsScreen.kt`.

---

## [1.4.5] - 2026-10-07

### ÃƒÂ°Ã…Â¸Ã¢â‚¬â„¢Ã‚Â³ Baixa de Pagamento Retroativa & Data Real de Pagamento (`actualPaymentDate`)

- **Suporte a Data Real de Pagamento no Modelo e Banco**:
  - AdiÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o do campo opcional `actualPaymentDate: Long?` no modelo de domÃƒÆ’Ã‚Â­nio `BillInstallment` e na entidade Room `BillInstallmentEntity`.
  - MigraÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o de banco de dados Room `MIGRATION_9_10` (`PlatformDatabase` v10) aplicando `ALTER TABLE bill_installments ADD COLUMN actualPaymentDate INTEGER DEFAULT NULL`.
  - MigraÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o registrada e injetada no `AppModule`.
- **DiÃƒÆ’Ã‚Â¡logo Elegante de Baixa de Pagamento (`ConfirmPaymentDialog`)**:
  - Novo diÃƒÆ’Ã‚Â¡logo nativo Material 3 permitindo ao usuÃƒÆ’Ã‚Â¡rio informar a data real do pagamento na baixa da conta.
  - Atalhos instantÃƒÆ’Ã‚Â¢neos de 1 toque: "Hoje" e "No Vencimento".
  - Seletor de data (`DatePickerDialog`) permitindo conciliaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Âµes e baixas retroativas ou programadas com total flexibilidade.
  - Integrado no fluxo de quitaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o em `BillsScreen`, `HomeScreen`, `RecurringInstallmentsScreen`, `ContactDetailScreen` e `EditInstallmentBottomSheet`.
- **CÃƒÆ’Ã‚Â¡lculo Preciso da Taxa de Pagamentos Pontuais (`GetFinancialDashboardUseCase`)**:
  - O cÃƒÆ’Ã‚Â¡lculo da mÃƒÆ’Ã‚Â©trica `onTimePaymentRate` agora compara a data real de pagamento (`actualPaymentDate`), com fallback para `paidAt` e `dueDate`, contra o vencimento da parcela.
  - Baixas retroativas de contas pagas no prazo nÃƒÆ’Ã‚Â£o sÃƒÆ’Ã‚Â£o mais classificadas erroneamente como atrasadas.
- **SuÃƒÆ’Ã‚Â­te de Testes UnitÃƒÆ’Ã‚Â¡rios**:
  - Testes unitÃƒÆ’Ã‚Â¡rios atualizados e adicionados em `ToggleInstallmentPaymentUseCaseTest`, `GetFinancialDashboardUseCaseTest`, `BillsViewModelTest` e `HomeViewModelTest`, cobrindo quitaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o retroativa no prazo, quitaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o retroativa em atraso, fallback e desmarcaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o de pagamento.
- **GovernanÃƒÆ’Ã‚Â§a e Arquitetura**:
  - CriaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o da [ADR 020: Data Real de Pagamento, Baixa Retroativa e MigraÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o Room v10](.ai/DECISIONS/020-data-real-pagamento-e-baixa-retroativa.md).

---

## [1.4.4] - 2026-09-29

### ÃƒÆ’Ã‚Â¢Ãƒâ€¦Ã¢â‚¬Å“Ãƒâ€šÃ‚ÂÃƒÆ’Ã‚Â¯Ãƒâ€šÃ‚Â¸Ãƒâ€šÃ‚Â GestÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o e EdiÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o de Itens de Despesa (`ExpenseItemsScreen` & `NewExpenseScreen`)

- **EdiÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o Completa de Itens de Despesa**:
  - ImplementaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o do fluxo de ediÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o reativo para itens de despesa existentes via `AddEditExpenseItemBottomSheet`, permitindo renomear o item ou alterar sua categoria vinculada com heranÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§a instantÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢nea da nova natureza financeira.
- **HarmonizaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o com o PadrÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o Universal de Detalhes (`ExpenseItemDetailBottomSheet`)**:
  - O toque direto em qualquer card de item agora abre um `ModalBottomSheet` dedicado com avatar na cor da categoria, nome do item, identificadores, chip de natureza e menu contextual de 3 pontos (`MoreVert`).
  - Acesso direto ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â  aÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o "Editar Item" via botÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o de largura total na base do sheet e via menu de 3 pontos.
  - AÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o de exclusÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o mantida de forma segura com diÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡logo de confirmaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o `AlertDialog`.
- **Atalhos e Acesso RÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡pido em Nova Despesa (`NewExpenseScreen`)**:
  - Adicionada opÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o direta "Gerenciar Itens (Criar / Editar)" no menu dropdown de seleÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o de itens e botÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o de ediÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o rÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡pida de 1 toque no chip de natureza herdada, permitindo ajustar itens sem perder o contexto do lanÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§amento.
- **SuÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â­te de Testes Automatizados**:
  - Nova suÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â­te de testes unitÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡rios `ExpenseItemsViewModelTest.kt` validando carga inicial, filtro de pesquisa, filtro por categoria, criaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o e salvamento de itens editados (preservaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o de ID) e exclusÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o.

---

## [1.4.3] - 2026-09-29

### ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â°ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ Pacote de ExcelÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âªncia Operacional & UX (Visual, AÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âµes, FunÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âµes e Sistema)

- **1. Visual: TransiÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âµes de Tela CinemÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡ticas e Fluidas (`NavGraph.kt`)**:
  - ImplementaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o de transiÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âµes animadas nativas no `NavHost` (`slideIntoContainer` horizontal + `fadeIn`/`fadeOut` interpolados com `tween(260ms)`).
  - ExperiÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âªncia visual contÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â­nua sem saltos abruptos de layout ao transitar entre Dashboard, Registros, CartÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âµes, Recorrentes e EstatÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â­sticas.

- **2. AÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âµes (UX): Mecanismo de Desfazer (`Undo`) para Baixa de Contas e Faturas**:
  - IntegraÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o de `SnackbarHost` e canal de efeitos `HomeUiEffect.ShowUndoSnackbar` na `HomeScreen`.
  - Ao quitar uma conta avulsa ou fatura com 1 toque, ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â© exibido um Snackbar com aÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o **"Desfazer"**, permitindo reversÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o imediata de cliques acidentais.
  - Adicionado `reopenInvoice` em `FinancialRepositoryImpl.kt` com transaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o atÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â´mica (`database.withTransaction`), restaurando o status da fatura para `ABERTA` e suas parcelas vinculadas para `PENDING`.

- **3. FunÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âµes: DuplicaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o Inteligente de Despesas (`duplicateBillId`)**:
  - Nova opÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o **"Duplicar Despesa"** no menu de contexto (`DropdownMenu`) do BottomSheet de detalhes da parcela (`BillsScreen.kt`).
  - NavegaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o para `NewExpenseScreen` carregando a despesa original via `SavedStateHandle` no `NewExpenseViewModel`, clonando valor, categoria, item, contato, conta bancÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡ria, forma de pagamento e tipo, acelerando cadastros repetitivos.

- **4. Sistema: Lembretes e NotificaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âµes Locais de Vencimento Offline**:
  - CriaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o do canal e gerenciador `DueReminderManager.kt` (`com.platform.app.core.notification`) de alta prioridade.
  - `DueReminderReceiver.kt`: BroadcastReceiver com `@AndroidEntryPoint` e injeÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o Hilt para checagem diÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡ria programada via `AlarmManager`.
  - VerificaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o inteligente na `MainActivity`: Notifica o usuÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡rio de forma offline e discreta caso existam contas ou faturas com vencimento no prÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â³prio dia.
  - PermissÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o `POST_NOTIFICATIONS` declarada no `AndroidManifest.xml`.

- **5. Qualidade e Testes UnitÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡rios**:
  - Novos testes automatizados: `HomeViewModelTest.kt` (aÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âµes de pagamento, undo de contas e faturas) e `NewExpenseViewModelTest.kt` (prÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©-carregamento por duplicaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o).
  - 100% dos testes unitÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡rios validados e aprovados com sucesso (`./gradlew testDebugUnitTest`).

---

## [1.4.2] - 2026-09-29

### ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â°ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã¢â‚¬Å“ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â  InteligÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âªncia Financeira 360ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â° & RefatoraÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o da Tela de EstatÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â­sticas
- **VisÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o Temporal Integrada em 3 Eixos (Passado, Presente e Futuro)**:
  - NavegaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o fluida via `PlatformSegmentedTabs` (`Passado`, `Presente`, `Futuro`) com suporte nativo ao **Modo Privacidade** (`PlatformPrivacyToggle`).
- **1. Eixo Passado (HistÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â³rico & TendÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âªncias ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â Onde estivemos)**:
  - **MÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©dia HistÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â³rica Mensal**: Baseline de custo de vida para guiar novos planejamentos.
  - **EvoluÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o dos ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¡ltimos 6 Meses**: Barras comparativas mÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âªs a mÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âªs de valor devido vs valor liquidado, com taxa de quitaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o individual e atalho de salto direto para o mÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âªs.
  - **Comparativo com o MÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âªs Anterior**: VariaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o em R$ e % com badges semafÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â³ricos de alta/baixa.
  - **Picos e Vales HistÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â³ricos**: IdentificaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o do mÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âªs mais pesado vs mÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âªs mais econÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â´mico.
  - **Ranking de Top DestinatÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡rios / Fornecedores**: Contatos que mais absorveram recursos.
- **2. Eixo Presente (Raio-X do MÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âªs Selecionado ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â Onde estamos)**:
  - **Hero Card de ExecuÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o OrÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§amentÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡ria**: Total devido, valor pago, pendente e atrasado com termÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â´metro semafÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â³rico `PlatformProgressBar`.
  - **Rigidez OrÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§amentÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡ria (Regra 50/30/20 & Natureza dos Gastos)**: ClassificaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o em `ObrigatÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â³rio`, `NecessÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡rio`, `Deseja` e `Nenhum` com card de diagnÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â³stico estratÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©gico inteligente (*Alerta de OrÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§amento Engessado* caso ObrigatÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â³rio > 55%).
  - **Meio de LiquidaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o & CrÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©dito**: Barra bifurcada e percentuais de exposiÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o entre CartÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o de CrÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©dito vs DÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©bito/Pix/Dinheiro.
  - **Top Categorias & Contas BancÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡rias**: DistribuiÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o visual dos gastos e concentraÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o institucional.
- **3. Eixo Futuro (Previsibilidade & Tomada de DecisÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â Para onde vamos)**:
  - **Curva de DesoneraÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o (PrÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â³ximos 6 Meses)**: EvoluÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o decrescente dos pagamentos com destaque para o mÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âªs de maior pico e o mÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âªs de maior folga financeira.
  - **DesoneraÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o & TÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©rmino de Parcelas**: Cronograma de compras parceladas ativas que chegam ao fim, com cÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡lculo de alÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â­vio mensal gerado (*"+R$ X/mÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âªs livre"*).
  - **Cockpit de Tomada de DecisÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o (Smart Advisor)**: Consultoria contextual em tempo real para responder *"Posso assumir uma nova compra parcelada agora?"*.
- **4. DomÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â­nio & Testes Automatizados**:
  - Modelos enriquecidos em `FinancialDashboardMetrics.kt` e agregaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o performÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡tica em `GetFinancialDashboardUseCase.kt`.
  - 100% da suÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â­te unitÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡ria aprovada (`./gradlew testDebugUnitTest`).

---

## [1.4.1] - 2026-09-29

### ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â°ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â‚¬Å¾Ã‚Â¢ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â½ Grande Update das 4 Telas Centrais (Obsidian & Porcelain Minimalist)
- **1. Tela Inicial (`HomeScreen`)**:
  - **Executive Hero Card**: AdiÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o de barra de progresso horizontal (`PlatformProgressBar`) com cantos arredondados indicando o percentual de quitaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o do mÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âªs (ex: *"68% quitado"*) e mÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©tricas de JÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡ Quitado e Total Geral.
  - **Modo Privacidade (`PlatformPrivacyToggle`)**: Alternador com ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â­cone de olho para mascarar valores confidenciais (`R$ ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢`) em locais pÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âºblicos.
  - **Seletor de MÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âªs RÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡pido em Grade (`MonthPickerBottomSheet`)**: Grade visual de meses e seletor de ano direto no tÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â­tulo do mÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âªs, eliminando cliques repetitivos em setas.
  - **Empty State Triunfante (`MonthVictoryCard`)**: Card comemorativo esmeralda quando todas as contas do mÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âªs estiverem 100% quitadas.
  - **Micro-interaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âµes HÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡pticas**: VibraÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o suave ao efetuar a baixa de contas na lista.
- **2. Tela de Registros (`BillsScreen`)**:
  - **Segmented Tabs no Topo (`PlatformSegmentedTabs`)**: DivisÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o em 3 abas especializadas (`A Pagar`, `Pagas`, `Todas` com contadores numÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©ricos).
  - **Banner de Totais Filtrados**: Resumo instantÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢neo do valor total e quantidade de registros visÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â­veis.
  - **AÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âµes em Lote (`PlatformBatchActionBar`)**: Modo de seleÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o mÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âºltipla (toque longo ou botÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o no TopBar) para liquidar ou excluir vÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡rias contas com 1 confirmaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o.
- **3. Tela de Recorrentes & Parcelados (`RecurringInstallmentsScreen`)**:
  - **SeparaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o por Abas**: DivisÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o nÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â­tida entre `Compras Parceladas` (amortizaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o com data final) e `Assinaturas & Custos Fixos` (compromisso mensal contÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â­nuo).
  - **Card de AmortizaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o Estilo Financiamento**: Progresso visual com `PlatformProgressBar`, projeÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o de data de quitaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o final (*"TÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©rmino em MÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âªs/Ano"*) e saldo devedor restante.
  - **Cronograma de Parcelas**: Acompanhamento detalhado de cada parcela com baixa direta.
  - **ProjeÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o Anual de Assinaturas**: ExibiÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o do custo anual consolidado (*"R$ Y/ano"*).
- **4. Tela de CartÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âµes de CrÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©dito (`CreditCardsScreen`)**:
  - **Design Virtual Apple Card / Revolut (`PlatformCreditCardView`)**: ProporÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o bancÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡ria exata (`1.586f`), chip EMV vetorial metÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡lico, gradiente acetinado e termÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â´metro de limite inteligente semafÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â³rico integrado.
  - **Ciclo de 3 Faturas**: AlternÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ncia de abas entre `Aberta`, `Fechadas`, `Pagas` e `Todas`.
  - **Destaque do Melhor Dia de Compra**: OrientaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o visual do dia de corte para compras com atÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â© 40 dias de prazo.
  - **Atalho de Nova Compra**: BotÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o direto no extrato para lanÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ar despesa prÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©-selecionando o cartÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o.
- **5. Novos Componentes ReutilizÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡veis**:
  - `PlatformProgressBar.kt`, `PlatformPrivacyToggle.kt`, `PlatformSegmentedTabs.kt`, `PlatformBatchActionBar.kt`, `PlatformCreditCardView.kt`.
- **6. ValidaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o e Qualidade**:
  - 100% da suÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â­te `./gradlew testDebugUnitTest` aprovada e build limpo em 4 segundos.

---

## [1.4.0] - 2026-09-29

### ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â°ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ RefatoraÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o End-to-End: Wallet 100% Pessoal & GestÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o de Contas e Faturas
- **Contexto 100% Pessoal Unificado**:
  - EliminaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o definitiva de qualquer perfil corporativo/pessoal (`profileType`, `PESSOAL`/`EMPRESA`), operando com foco exclusivo em finanÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§as pessoais.
- **Foco em Contas a Pagar & Previsibilidade**:
  - Cadastro ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡gil e intuitivo exigindo apenas 3 dados obrigatÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â³rios: DescriÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o, Valor (em centavos `Long`) e Vencimento.
  - Seletores de Categoria e Item opcionais.
- **Natureza Financeira (ExpenseNature) & Itens de Despesa**:
  - ImplementaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o dos 4 pilares: `OBRIGATORIO` (custos inegociÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡veis), `NECESSARIO` (manutenÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o essencial), `DESEJA` (estilo de vida e conforto) e `NENHUM` (transitÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â³rios/nÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o classificados).
  - DepreciaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o de subcategorias e criaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o da tabela `expense_items` vinculada diretamente a `categories`, com heranÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§a estrita da natureza da categoria mÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£e.
  - Nova tela `ExpenseItemsScreen` em ConfiguraÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âµes para listar, pesquisar e cadastrar itens vinculados com prÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©-visualizaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o de natureza.
- **Efeito Cascata no Dashboard em Tempo Real**:
  - Total Previsto no MÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âªs calculado dinamicamente via Flow somando contas avulsas pendentes e faturas de cartÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o abertas/fechadas do perÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â­odo.
  - Agrupamento semafÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â³rico de urgÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âªncia: ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â°ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚ÂÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â´ Atrasadas, ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â°ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡ Vence Hoje, ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âª PrÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â³ximos 7 Dias e ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â°ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ Pagas no MÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âªs (colapsÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡vel).
  - Baixa com 1 Toque ("Pagar"): atualiza status para `PAGO` no banco e deduz instantaneamente do total mensal sem recarregar a tela.
- **CartÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âµes de CrÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©dito & Extrato de Faturas**:
  - CÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡lculo robusto de limite disponÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â­vel (`CreditCardCalculator`), fechamento de faturas baseado no dia de corte (`closingDay`) e status da fatura (`ABERTA`, `FECHADA`, `PAGA`).
  - Extrato detalhado de faturas em `CreditCardsScreen` com botÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o de liquidaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o em 1 toque.
- **PersistÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âªncia Room & MigraÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o**:
  - AtualizaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o do `PlatformDatabase` para versÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o `7` com `MIGRATION_6_7`.
  - InclusÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o da entidade `TransactionEntity` e DAO `TransactionDao` para rastreamento de transaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âµes.
  - Tipo alias `AppDatabase = PlatformDatabase` para interoperabilidade.
- **Design System Material 3 (ADR-018)**:
  - AplicaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o estrita de tokens de cores para Dark Mode e Light Mode.
  - ProibiÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o de cores literais nos componentes; uso estrito de `MaterialTheme.colorScheme`.
  - Tipografia em Title Case (sem ALL CAPS em textos de tela).
  - ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âcones funcionais do Material Icons substituindo formas abstratas.
- **LanÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§amento Focado em Itens (Item-Centric Expenses)**:
  - O Item de Despesa (`selectedItemId`) assumiu o protagonismo absoluto no formulÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡rio: primeiro campo de seleÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o com preview de Natureza Financeira e Categoria.
  - A descriÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o passa a ser campo de "ObservaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âµes Adicionais (Opcional)", herdando o nome do item selecionado como tÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â­tulo padrÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o caso nÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o preenchida.
  - ReorganizaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o do formulÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡rio em 5 blocos harmÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â´nicos e simÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©tricos com cards e divisÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âµes semÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢nticas.
- **Design System Premium Minimalista & Simetria CirÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âºrgica**:
  - Paletas Dark e Light purificadas: Dark Obsidian (`#0A0D14` / `#141923`) e Light Porcelain (`#F8FAFC` / `#FFFFFF`).
  - AnulaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o estrita de elevaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o tonal com `surfaceTint = Color.Transparent` em ambos os temas, eliminando 100% qualquer vazamento roxo/lavanda default do Material 3.
  - ErradicaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o completa de cores literais hardcoded (`Color.White`, `Color.Black`, hexadecimais soltos) em todas as telas, adotando exclusivamente tokens de `Theme.kt`, `Color.kt` e `MaterialTheme.colorScheme`.
  - Anti-gigantismo e harmonia visual: ajuste da tipografia do impacto financeiro na HomeScreen de `headlineLarge` para `titleLarge` semibold/bold.
  - Alinhamentos milimÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©tricos com cantos uniformes de 16.dp para cards, 10.dp para botÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âµes/inputs e bordas finas de 1.dp com `outlineVariant`.
  - **PadronizaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o Global dos FABs (100% Circular)**: UnificaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o dos FloatingActionButtons de todas as 9 telas financeiras (incluindo `RecurringInstallmentsScreen`) com `shape = CircleShape`, container `primary` e conteÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âºdo `onPrimary`.
  - **UnificaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o Absoluta de Fluxos de Despesa**: RemoÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o do modal legado `AddBillBottomSheet` em `BillsScreen`. Tanto a tela Inicial quanto a tela de Registros navegam para a mesma `NewExpenseScreen` com o formulÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡rio em 5 blocos harmÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â´nicos focado em itens.
  - **Componentes Centrais ReutilizÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡veis (`PlatformEmptyState` e `PlatformAvatar`)**:
    - `PlatformEmptyState`: EliminaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o definitiva de emojis informais (`ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â°ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â‚¬Å¾Ã‚Â¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â³`, `ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â°ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã¢â‚¬Å“ÃƒÆ’Ã¢â‚¬Â¹ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â `) em telas vazias, substituÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â­dos por containers vetoriais com fundo translÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âºcido a 12% e tipografia proporcional `titleLarge` semibold.
    - `PlatformAvatar`: EliminaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o de cÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â³digo duplicado de cÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡lculo de iniciais e renderizaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o visual homogÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âªnea em `ContactsScreen` e `ContactDetailScreen`.
  - **HigienizaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o Visual Estrita (Zero AÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âµes Inline NÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o Seguras)**:
    - `ExpenseItemsScreen`: RemoÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o de botÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âµes de exclusÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o inline em cards, substituÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â­dos por navegaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o sutil com chevron e `AlertDialog` de confirmaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o seguro.
    - `BudgetsScreen`: MigraÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o de `BudgetCard` para `PlatformCard` com raio de 16.dp e contorno suave.
    - `CreditCardsScreen`: Ajuste de contraste do botÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o "Liquidar Fatura" (`onPrimary` garantindo legibilidade perfeita no Tema Escuro e Claro).
    - `SettingsScreen`: DistinÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o de ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â­cone para Itens de Despesa com `Icons.Default.ShoppingBag`.
    - `AppDrawer`: CorreÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o do divisor de seÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âµes com verificaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o insensÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â­vel a maiÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âºsculas/minÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âºsculas (`ignoreCase = true`).
  - **EliminaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o de CÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â³digo Zumbi & DepreciaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âµes**:
    - RemoÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o do arquivo zumbi `DashboardScreen.kt` (-1.073 linhas de cÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â³digo morto).
    - MigraÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o de todos os ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â­cones deprecados para `Icons.AutoMirrored.Filled` (`ReceiptLong` e `TrendingUp`), zerando warnings de compilaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o.
- **Cobertura de Testes Automatizados**:
  - `NewExpenseValidationTest`: 6 testes automatizados validando campos obrigatÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â³rios (Item e Contato), heranÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§a de natureza, cÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡lculo de parcelas e preenchimento de tÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â­tulo a partir do item.
  - `CreditCardManagementTest`: ValidaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o de fechamento, limite e integridade de faturas.
  - `BillCalculationTest`, `ExpenseItemNatureTest`, `InvoiceClosingTest`: Todos aprovados com 100% de sucesso.

---

## [1.3.1] - 2026-09-28

### ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â°ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â½ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¨ Melhorias Visuais & Ergonomia
- **Ajuste de ProporÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âµes dos Cards em ConfiguraÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âµes**:
  - AmpliaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o da ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡rea de toque e respiro interno dos blocos de acesso direto a **Contas**, **Formas de Pagamento** e **Categorias** (padding vertical aumentado para 14dp e horizontal para 16dp).
  - ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âcones com badges ampliados de 32dp para 44dp e cantos arredondados de 12dp para maior destaque visual e clareza de toque.
  - InclusÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o de subtÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â­tulos descritivos elegantes abaixo de cada tÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â­tulo para reforÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ar o propÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â³sito de cada ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡rea.
  - Tipografia elevada para titleMedium semibold e chevron de navegaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o mais visÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â­vel.
  - EspaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§amento vertical entre blocos unificado em 12dp.

---

## [1.3.0] - 2026-09-28

### ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â°ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ Novas Funcionalidades
- **Backup e RestauraÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o de Dados Offline**:
  - ExportaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o completa e versionada do banco de dados em formato JSON compacto com valores inteiros em centavos (`Long`).
  - RestauraÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o atÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â´mica em lote utilizando transaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âµes seguras do Room (`database.withTransaction`), garantindo rollback total em caso de falha.
  - IntegraÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o com o *Storage Access Framework (SAF)* nativo do Android (`CreateDocument` e `OpenDocument`) para salvar e restaurar do Google Drive, Documentos, Downloads ou cartÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o SD.
  - Compartilhamento rÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡pido via *ShareSheet* (`FileProvider`) direto para WhatsApp, Telegram ou e-mail.
  - Indicador em tempo real nas ConfiguraÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âµes com a data e hora exata do ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âºltimo backup efetuado.
  - DiÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡logo de advertÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âªncia e confirmaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o antes de restauraÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âµes para impedir substituiÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âµes acidentais de dados.

### ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â°ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â½ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¨ HarmonizaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o Visual & PadrÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o de Detalhes (4 Etapas)
- **AdoÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o Universal do PadrÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o de Detalhes com BottomSheet**:
  - Cards 100% limpos e minimalistas em todo o sistema: remoÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o de botÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âµes inline de lixeira, lÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡pis e acordeÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âµes soltos.
  - Toque no card abre `ModalBottomSheet` dedicado com mÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©tricas contextuais, visualizaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o de vÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â­nculos (conta, categoria, contato, mÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©todo) e aÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âµes rÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡pidas.
  - Menu de 3 pontos (`MoreVert`) no topo do BottomSheet para ediÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o e exclusÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o segura.
  - DiÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡logos de confirmaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o obrigatÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â³rios (`AlertDialog`) antes de qualquer exclusÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o definitiva.
  - Seletores de campos com opÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âµes prÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©-definidas migrados para menus dropdown nativos (`ExposedDropdownMenuBox`).
  - Seletor de cores aprimorado com indicador visual circular branco na cor ativa.
- **Telas Harmonizadas**:
  - [ManagementScreen.kt]: Formas de Pagamento e Categorias desacopladas com BottomSheet e seletor com indicador.
  - [BillsScreen.kt]: Cards de parcelas sanitizados, tipografia reduzida de 28sp para 16sp bold, chevron sutil e BottomSheet detalhado de liquidaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o.
  - [GoalsScreen.kt]: Cards de metas limpos, BottomSheet com progresso, aporte rÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡pido e menu de aÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âµes.
  - [BudgetsScreen.kt]: Cards de orÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§amento limpos, BottomSheet com comparaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o teto vs realizado e ajuste de limite.
  - [RecurringInstallmentsScreen.kt]: Cards de contratos enxutos, BottomSheet com detalhes do contrato e listagem completa de parcelas interativas.
  - [ContactDetailScreen.kt]: ExclusÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o segura de contato no menu de 3 pontos com diÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡logo de confirmaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o.

---

## [1.2.0] - 2026-09-28

### ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â°ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ Melhorias & EvoluÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âµes
- **GestÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o Financeira Desacoplada nas ConfiguraÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âµes**: Telas dedicadas e independentes para Contas BancÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡rias, Formas de Pagamento e Categorias, sem acoplamento de abas concorrentes.
- **Menu Lateral Minimalista**: ReduÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o de ruÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â­do visual, eliminaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o de subtÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â­tulos descritivos em todos os itens do drawer, proporÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âµes enxutas e alinhamento com Material 3.
- **Dashboard Financeiro Macro**: Cards inteligentes com visÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o geral de saldo, compromissos do mÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âªs vigente e projeÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o futura de gastos.
- **Recorrentes & Parcelados**: GestÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o centralizada de assinaturas fixas e compras parceladas em andamento com cÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡lculo automÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡tico de amortizaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âµes.
- **Metas & OrÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§amentos**: Controle de reservas financeiras com barra de progresso visual e definiÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o de tetos de gastos mensais por categoria.
- **Cadastro Inteligente de Contatos**: IntegraÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o de busca automÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡tica de CEP via ViaCEP, novos campos estruturados de endereÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§o e ediÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o com 1 toque.
- **AutomaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o de Versionamento**: SincronizaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o automÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡tica entre o arquivo `VERSION`, `app/build.gradle.kts` e scripts de bump para evitar estagnaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o de versÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o.

---

## [1.0.0] - 2026-09-26

### ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â°ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã¢â‚¬Â¦Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ Melhorias
- FundaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o da arquitetura base do aplicativo Android em Clean Architecture (Presentation, Domain, Data, Core).
- ConfiguraÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o do Android Gradle Plugin com Kotlin DSL (`build.gradle.kts` e Version Catalog `libs.versions.toml`).
- IntegraÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o da stack moderna de UI com Jetpack Compose BOM, Material 3 e suporte a Tema Claro e Escuro.
- InjeÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o de dependÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âªncias desacoplada preparada com Hilt.
- ConfiguraÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o de persistÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âªncia local com Room Database e comunicaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o remota com Retrofit/OkHttp.
- ImplementaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o de fluxos reativos com Kotlin Coroutines e StateFlow.
- Estrutura completa de governanÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§a de IA (`AGENT_RULES.md`, `.ai/` e `.agents/` com skills e regras).
- AutomaÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£o de versionamento mÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â³vel sincronizando `versionCode`, `versionName` e `CHANGELOG.md`.

---





