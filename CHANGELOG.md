# Changelog - Platform (Android App)

Todas as alteraÃ§Ãµes notÃ¡veis neste projeto serÃ£o documentadas neste arquivo.
O formato Ã© baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/) e este projeto segue [Versionamento SemÃ¢ntico](https://semver.org/lang/pt-BR/).

## [1.17.0] - 2026-10-07

### ⚡ Otimização SQL de Pagamento e Totais de Faturas (`BillInstallmentDao` & `FinancialRepositoryImpl`)

- **Queries SQL de Alta Performance no Room (`BillInstallmentDao`)**:
  - Implementação da query atômica de liquidação: `UPDATE bill_installments SET paidAt = :paidAt, actualPaymentDate = :actualPaymentDate, status = :status WHERE invoiceId = :invoiceId` (`updatePaymentByInvoiceId`).
  - Implementação da query SQL de agregação direta: `SELECT invoiceId, COALESCE(SUM(amountCents), 0) AS totalAmountCents FROM bill_installments WHERE invoiceId IS NOT NULL GROUP BY invoiceId` (`getInvoiceTotals`).
  - Eliminação definitiva da sobrecarga de carregar toda a tabela de parcelas em memória (`getAllInstallmentsList()` e `getAllInstallments()`) para filtragens lineares em Kotlin nos fluxos de quitação e totais de faturas.
- **Suporte a Data Real de Pagamento na Baixa de Fatura (`actualPaymentDate`)**:
  - `FinancialRepository.payInvoice` agora aceita `actualPaymentDate: Long? = null` (padrão = hoje via `System.currentTimeMillis()`), garantindo conformidade com a ADR 020 e prevenindo distorções no indicador de pontualidade (`onTimePaymentRate`).
  - Ações de pagamento em `CreditCardsUiAction.PayInvoice` e `HomeUiAction.PayInvoice` estendidas com `actualPaymentDate` opcional.
- **Atomicidade e Transações Mantidas**:
  - `payInvoice` e `reopenInvoice` continuam executando sob `database.withTransaction`, garantindo consistência relacional entre a tabela de faturas (`credit_card_invoices`) e as parcelas (`bill_installments`).
- **Suíte de Testes Automatizados**:
  - Criado `FinancialRepositoryInvoiceTest` cobrindo detalhadamente:
    - Quitação de fatura com data real customizada marcando exclusivamente as parcelas da própria fatura alvo.
    - Quitação de fatura sem data explicitada aplicando fallback seguro para a data corrente (`paidAt == actualPaymentDate`).
    - Reabertura de fatura (`reopenInvoice`) limpando timestamps e redefinindo status para `PENDING` apenas nas parcelas da fatura em questão.
    - Agregação reativa de totais de faturas via `getInvoiceTotals()` sem carregar relacionamentos desnecessários em memória.
  - 100% dos testes unitários (154 testes) aprovados via Gradle.

---

## [1.16.0] - 2026-10-07

### ðŸ’³ Regra Ãšnica de Limite de CartÃ£o de CrÃ©dito e Faturas Sob Demanda

- **CriaÃ§Ã£o do `GetCreditCardSummariesUseCase`**:
  - Centralizado o cÃ¡lculo do limite consumido e limite disponÃ­vel de cartÃµes de crÃ©dito em uma Ãºnica fonte da verdade.
  - Implementada a **Regra CanÃ´nica de Limite**:
    - **`INSTALLMENT` (Parcelados)**: Consome o saldo devedor restante de todas as parcelas nÃ£o pagas (atuais e futuras contratadas).
    - **`RECURRING` (Assinaturas) e `SINGLE` (Ã€ vista)**: Consomem exclusivamente o que pertence Ã  fatura aberta ou fechada atual nÃ£o paga, eliminando o erro de projeÃ§Ã£o que consumia antecipadamente o limite de atÃ© 12 meses futuros de assinaturas.
- **EliminaÃ§Ã£o de CÃ¡lculos Duplicados nas ViewModels**:
  - `CreditCardsViewModel` e `NewExpenseViewModel` agora consomem diretamente o Flow reativo de `GetCreditCardSummariesUseCase`.
  - Corrigido vazamento na `NewExpenseViewModel`, que somava parcelas de todos os cartÃµes indistintamente sem isolamento por `cardId`.
- **Faturas Recorrentes Sob Demanda**:
  - `CreateBillUseCase` atualizado para nÃ£o mais prÃ©-criar 11 faturas vazias antecipadas no SQLite para compras do tipo `RECURRING`. Apenas a 1Âª ocorrÃªncia do ciclo inicial Ã© atrelada de imediato Ã  fatura atual, preservando as projeÃ§Ãµes futuras de contas a pagar e gerando faturas sob demanda Ã  medida que os ciclos chegam.
- **SuÃ­te de Testes Automatizados**:
  - Criado `GetCreditCardSummariesUseCaseTest` cobrindo detalhadamente o cenÃ¡rio de um cartÃ£o com 1 compra parcelada em 10x, 1 assinatura mensal e 1 compra Ã  vista, alÃ©m da liberaÃ§Ã£o proporcional de limite apÃ³s pagamento de fatura.
  - Atualizados `CreateBillUseCaseTest` e `NewExpenseViewModelTest`.
  - 100% dos testes unitÃ¡rios (150 testes) aprovados via Gradle.

---

## [1.15.0] - 2026-10-07

### ðŸ›ï¸ UnificaÃ§Ã£o da GeraÃ§Ã£o de Parcelas no `CreateBillUseCase` e Paridade entre CartÃ£o e Contas Avulsas

- **ExtensÃ£o do `CreateBillUseCase` com Suporte a CartÃ£o de CrÃ©dito**:
  - `CreateBillUseCase` agora recebe opcionalmente a entidade `CreditCard?`, `isFirstInstallmentPaid: Boolean` e `actualPaymentDate: Long?`.
  - Reutiliza integralmente `CalculateInstallmentsUseCase` para o cÃ¡lculo canÃ´nico de contagem de parcelas, divisÃ£o exata de centavos com resto na 1Âª parcela e datas-base de ocorrÃªncia.
  - Quando a despesa for no cartÃ£o (`creditCard != null`), mapeia cada ocorrÃªncia para sua respectiva fatura usando `CreditCardCalculator.determineInvoiceReferenceMonth()` e `repository.getOrCreateInvoiceForMonth()`, vinculando `invoiceId` e ajustando o `dueDate` da parcela para o vencimento da fatura.
  - Atualiza atomicamente a `bill` vinculando o `invoiceId` inicial e persistindo conta e parcelas em transaÃ§Ã£o Room.
- **EliminaÃ§Ã£o de CÃ³digo Duplicado Inline na `NewExpenseViewModel`**:
  - Removido bloco redundante de ~150 linhas em `NewExpenseViewModel.saveExpense()` que recalculava parcelas, datas, resto de centavos e faturas inline.
  - Resolvida a divergÃªncia de contagem de parcelas (`coerceAtLeast(2)` vs `coerceAtLeast(1)`), restabelecendo o alinhamento estrito com as fronteiras de Clean Architecture.
- **Testes Automatizados de Paridade**:
  - Criado `CreateBillUseCaseTest` validando a paridade absoluta entre os fluxos com e sem cartÃ£o:
    - Mesma divisÃ£o exata de centavos e mesmo valor individual por parcela.
    - AlocaÃ§Ã£o consistente do resto de centavos na 1Âª parcela.
    - Mesmas datas-base de ocorrÃªncia utilizadas para geraÃ§Ã£o das faturas correspondentes.
    - Paridade para parcelamentos (`INSTALLMENT`), despesas Ã  vista (`SINGLE`) e recorrÃªncias (`RECURRING`).
    - Tratamento uniforme de status e timestamp de quitaÃ§Ã£o quando a despesa jÃ¡ Ã© criada como paga.
  - Atualizado `NewExpenseViewModelTest` injetando `CreateBillUseCase`.
  - 100% dos testes unitÃ¡rios validados via Gradle.

---

## [1.14.0] - 2026-10-07

### ðŸ’³ RemoÃ§Ã£o do Seed FictÃ­cio de CartÃµes e CentralizaÃ§Ã£o de Seeds no Primeiro Uso

- **RemoÃ§Ã£o de Dados FictÃ­cios de CartÃµes de CrÃ©dito**:
  - ExcluÃ­do o seed automÃ¡tico que criava "CartÃ£o Principal" e "CartÃ£o SecundÃ¡rio" quando a tabela estava vazia.
  - Implementado empty state refinado em `CreditCardsScreen` com botÃ£o de Call-To-Action (CTA): `"Cadastrar primeiro cartÃ£o"`.
  - O app agora respeita quando o usuÃ¡rio exclui todos os cartÃµes, nunca mais recriando dados fictÃ­cios sem autorizaÃ§Ã£o.
- **CentralizaÃ§Ã£o da InicializaÃ§Ã£o no Primeiro Uso (Startup)**:
  - Criado o caso de uso `SeedInitialDataUseCase` e centralizado o bootstrap de entidades de referÃªncia (Categorias, Itens, Contatos, Contas Financeiras e Formas de Pagamento) em `PlatformApplication.onCreate()`.
  - Controle de primeiro uso governado via flag persistente `seeds_applied` no `PreferencesManager` (Jetpack DataStore) em vez de checagens repetitivas de "tabela vazia".
  - Se o usuÃ¡rio excluir conscientemente todos os registros de qualquer entidade (categorias, contatos, etc.), eles nÃ£o sÃ£o recriados ao abrir o app.
- **Desacoplamento e Limpeza nos ViewModels**:
  - Removidas chamadas de seed dos blocos `init` de 7 ViewModels (`HomeViewModel`, `BillsViewModel`, `NewExpenseViewModel`, `ManagementViewModel`, `ExpenseItemsViewModel`, `CreditCardsViewModel` e `DashboardViewModel`).
  - ReduÃ§Ã£o drÃ¡stica de overhead de I/O de banco e checagens concorrentes desnecessÃ¡rias na navegaÃ§Ã£o entre telas.
- **Testes Automatizados & GovernanÃ§a**:
  - Criado `FinancialRepositoryImplSeedTest` validando a garantia de resÃ­duo zero: mesmo com todas as tabelas vazias, uma vez que a flag `seeds_applied` estÃ¡ ativa, nenhuma entidade Ã© recriada.
  - Atualizado `ExpenseItemsViewModelTest` com foco no carregamento de dados.
  - Registrado [ADR 029](.ai/DECISIONS/029-remocao-seed-cartoes-e-centralizacao-startup-seeds.md).

---

## [1.13.0] - 2026-10-07

### ðŸŽ¯ DAO Dedicado para Goal Contributions e RecÃ¡lculo Derivado de Aportes

- **DAO PrÃ³prio (`GoalContributionDao`)**:
  - Criada a interface dedicada `GoalContributionDao` com consultas especializadas para inserÃ§Ã£o, soma por meta (`sumByGoal`), soma por perÃ­odo (`sumForPeriod`), listagem por meta e perÃ­odo (`getByGoalForPeriod`), e deleÃ§Ã£o vinculada.
  - Registrado em `PlatformDatabase` e provido como Singleton no container Hilt em `AppModule.kt`.
- **Registro Datado & RecÃ¡lculo do Cache de Saldo**:
  - `GoalRepository.addContribution`: insere atomicamente a contribuiÃ§Ã£o datada no banco de dados e recalcula o campo cache `currentAmountCents` da meta a partir da soma real de todas as suas contribuiÃ§Ãµes (`sumByGoal`), eliminando riscos de inconsistÃªncia contÃ¡bil.
  - `saveGoal`: metas criadas com valor inicial geram automaticamente o primeiro registro histÃ³rico de contribuiÃ§Ã£o com a data de criaÃ§Ã£o da meta.
- **Consulta Granular por Meta e PerÃ­odo**:
  - Adicionado o mÃ©todo `getContributionsForPeriod(goalId, startDate, endDate)` em `GoalRepository` e `GoalRepositoryImpl`, viabilizando apuraÃ§Ã£o por meta em intervalos de datas e histÃ³rico de aportes.
- **Testes Automatizados**:
  - Criado `GoalRepositoryImplTest` com cobertura completa de mÃºltiplos aportes em meses distintos, filtros temporais e recÃ¡lculo do cache.
- **DecisÃ£o Arquitetural Documentada**:
  - Registrada [ADR 028](.ai/DECISIONS/028-dao-dedicado-goal-contributions-e-recalculo-aportes.md).

---

## [1.12.0] - 2026-10-07

### ðŸ§¹ RemoÃ§Ã£o de `syncStatus` Residual, ResÃ­duo Zero e MigraÃ§Ã£o Room v15

- **Expurgo de ResÃ­duo Morto e PreparaÃ§Ã£o para Fase 11**:
  - Removido o campo legado `syncStatus: String = "PENDENTE"` de todos os modelos de domÃ­nio (`Category`, `ExpenseItem`, `CreditCard`, `CreditCardInvoice`) e entidades Room (`CategoryEntity`, `ExpenseItemEntity`, `CreditCardEntity`, `CreditCardInvoiceEntity`).
  - O campo consistia em uma tentativa histÃ³rica e fragmentada de sincronizaÃ§Ã£o sem qualquer consumo na lÃ³gica do aplicativo, que gerava assimetrias em relaÃ§Ã£o Ã s entidades principais (`Bill`, `BillInstallment`, `Contact`, `FinancialAccount`, `Goal`, `Budget`).
  - Adotada formalmente a decisÃ£o arquitetural de conceber a sincronizaÃ§Ã£o bidirecional na nuvem (Fase 11) do zero, com fila de mutaÃ§Ãµes transacional (Outbox), controle de exclusÃ£o (Tombstones) e detecÃ§Ã£o de conflitos, em vez de flags estÃ¡ticas pontuais.
- **MigraÃ§Ã£o FÃ­sica no SQLite (`PlatformDatabase` v15)**:
  - Incrementada a versÃ£o do banco de dados Room para `version = 15`.
  - Implementada e registrada a migraÃ§Ã£o atÃ´mica `MIGRATION_14_15` com recriaÃ§Ã£o segura de tabelas (`categories`, `expense_items`, `credit_cards`, `credit_card_invoices`) sob `PRAGMA foreign_keys = OFF / ON`, garantindo compatibilidade universal com qualquer versÃ£o do SQLite nativo.
- **Higiene e CompactaÃ§Ã£o de Backups**:
  - Arquivos de backup JSON locais protegidos por senha agora sÃ£o gerados sem chaves `syncStatus` redundantes.
- **Testes Automatizados & GovernanÃ§a**:
  - Atualizado `PlatformDatabaseMigrationTest` validando a execuÃ§Ã£o estrutural da migraÃ§Ã£o `14 -> 15`.
  - Registrada a decisÃ£o tÃ©cnica no [ADR 027](.ai/DECISIONS/027-remocao-sync-status-residuo-zero-preparacao-fase-11.md).

---

## [1.11.1] - 2026-10-07

### ðŸ§¹ Limpeza de Typealias Obsoleto (`AppDatabase`)

- **RemoÃ§Ã£o de CÃ³digo Morto Residual**:
  - ExcluÃ­do o arquivo `AppDatabase.kt` contendo `typealias AppDatabase = PlatformDatabase`.
  - O alias de compatibilidade foi introduzido durante o rename inicial da aplicaÃ§Ã£o e nÃ£o possuÃ­a nenhum consumidor em nenhuma camada do projeto.
  - EliminaÃ§Ã£o definitiva de ambiguidades conceituais em favor do canÃ´nico `PlatformDatabase`.

---

## [1.11.0] - 2026-10-07

### ðŸ§¹ RemoÃ§Ã£o da Tabela Legada `transactions`, ResÃ­duo Zero e MigraÃ§Ã£o Room v14

- **PrincÃ­pio do ResÃ­duo Zero & Limpeza de CÃ³digo Legado**:
  - Removidos completamente os artefatos `TransactionEntity.kt` e `TransactionDao.kt`, eliminando classes residuais anteriores Ã  consolidaÃ§Ã£o do modelo unificado de Contas e Parcelas (`bills` / `bill_installments`).
  - Removido o provider de injeÃ§Ã£o de dependÃªncia `@Provides @Singleton fun provideTransactionDao(...)` em `AppModule.kt`.
  - Removido o campo `transactions` e sua serializaÃ§Ã£o/deserializaÃ§Ã£o em `BackupDataDto.kt` e `BackupRepositoryImpl.kt`, eliminando nÃ³s vazios nos arquivos de backup JSON exportados.
- **MigraÃ§Ã£o FÃ­sica no SQLite (`PlatformDatabase` v14)**:
  - Incrementada a versÃ£o do banco de dados Room para `version = 14`.
  - Criada e registrada a migraÃ§Ã£o `MIGRATION_13_14` executando `DROP TABLE IF EXISTS transactions`, limpando tabelas, Ã­ndices e chaves estrangeiras obsoletas do SQLite dos dispositivos dos usuÃ¡rios.
- **Testes e Garantia de Qualidade**:
  - Criado `PlatformDatabaseMigrationTest` validando a execuÃ§Ã£o do comando `DROP TABLE IF EXISTS transactions` na migraÃ§Ã£o `13 -> 14`.
  - Atualizado `BackupRepositoryImplTest` para operaÃ§Ã£o sem dependÃªncias de transaÃ§Ãµes legadas.
  - 100% dos testes unitÃ¡rios da aplicaÃ§Ã£o aprovados.
- **DecisÃ£o Arquitetural Documentada**:
  - Registrada [ADR 026](.ai/DECISIONS/026-remocao-tabela-transactions-e-migracao-room-v14.md) documentando a polÃ­tica de ResÃ­duo Zero e a migraÃ§Ã£o.

---

## [1.10.0] - 2026-10-07

### ðŸŒ CentralizaÃ§Ã£o de Strings de Interface (UI) e Fonte Ãšnica da Verdade (`AppStrings`)

- **Fonte Ãšnica da Verdade (`AppStrings.kt`)**:
  - Criado o objeto estruturado `AppStrings` em `com.platform.app.presentation.common` agrupando constantes textuais de interface de usuÃ¡rio sem overhead de contexto Android.
  - Sub-namespaces organizados: `Status`, `Nature`, `AccountType`, `ContactType`, `BillType`, `Actions`, `Home`, `Dialogs`.
  - SincronizaÃ§Ã£o espelhada em `res/values/strings.xml` para paridade com recursos nativos do framework Android e suporte nativo a internacionalizaÃ§Ã£o futura.
- **RefatoraÃ§Ã£o 1:1 Sem AlteraÃ§Ãµes de Texto**:
  - SubstituiÃ§Ã£o de termos literais repetidos na camada `presentation/` preservando rigorosamente 1:1 todos os caracteres originais:
    - **Status**: "Pendente", "Pago", "Paga", "JÃ¡ Paga", "A Pagar", "Vencida", "Vencido", "Atrasado", "Liquidado", "Tudo quitado", "100% quitado", "Aberta", "Fechada", "Pausada".
    - **Natureza do Gasto**: "ObrigatÃ³rio", "NecessÃ¡rio", "Deseja", "Nenhum", "PoupanÃ§a", "Natureza do Gasto", rÃ³tulos e descriÃ§Ãµes conceituais.
    - **Tipos de Conta**: "Conta Corrente", "Carteira / Dinheiro", "PoupanÃ§a", "Investimento".
    - **Tipos de Contato**: "Pessoa FÃ­sica", "Fornecedor", "Ã“rgÃ£o PÃºblico", "Pessoa", "Empresa", "PÃºblico", "Salvar Contato", "Atualizar Contato", "Excluir Contato".
    - **AÃ§Ãµes Comuns**: "Salvar", "Salvar AlteraÃ§Ãµes", "Atualizar", "Cancelar", "Excluir", "Confirmar", "Voltar", "Filtrar", "Todos".
    - **Dashboard**: "Restante a Pagar no MÃªs", "Total vencido:".
- **Telas e Componentes Atualizados**:
  - `HomeScreen`: status badges ("Tudo quitado", "100% quitado", "Pago"), "Total vencido:", "Restante a Pagar no MÃªs".
  - `BillsScreen`: status de pagamento ("Pago"), aÃ§Ãµes de exclusÃ£o em lote ("Excluir", "Cancelar").
  - `NewExpenseScreen`: status chips ("Pendente", "JÃ¡ Paga"), botÃµes de diÃ¡logo rÃ¡pido ("Salvar", "Cancelar").
  - `EditInstallmentBottomSheet`: tÃ­tulos e aÃ§Ãµes de confirmaÃ§Ã£o/exclusÃ£o ("Salvar AlteraÃ§Ãµes", "Excluir Registro", "Excluir", "Cancelar").
  - `ConfirmPaymentDialog`: aÃ§Ãµes de confirmaÃ§Ã£o ("Confirmar", "Cancelar").
  - `ContactsScreen` & `ContactDetailScreen`: chips de tipo ("Pessoa", "Empresa", "PÃºblico"), badges, filtros agregados ("Todos", "Pessoa FÃ­sica", "Fornecedor", "Ã“rgÃ£o PÃºblico"), botÃµes de aÃ§Ã£o e exclusÃ£o.
  - `ManagementScreen`: abas e cards de gestÃ£o com status ("Pendente", "Liquidado", "Pago"), "Natureza do Gasto:", diÃ¡logos de contas, mÃ©todos e categorias ("Salvar", "Atualizar", "Cancelar", "Excluir").
  - `StatisticsScreen`: breakdown de status do mÃªs ("Pago", "Pendente", "Atrasado").
  - `CreditCardsScreen`: badges de fatura ("Aberta", "Fechada", "Paga"), diÃ¡logo de exclusÃ£o de cartÃ£o ("Cancelar", "Excluir").
  - `ExpenseItemsScreen`: chips e descriÃ§Ãµes de natureza, botÃµes do modal ("Salvar", "Salvar AlteraÃ§Ãµes", "Cancelar", "Excluir").
  - `GoalsScreen` & `BudgetsScreen`: botÃµes de salvamento, ediÃ§Ã£o, cancelamento e exclusÃ£o.
  - `RecurringInstallmentsScreen` & `AdjustInstallmentDialog`: rÃ³tulos de status ("Pago"), diÃ¡logos de cancelamento/exclusÃ£o/pausa ("Excluir", "Cancelar", "Voltar").
  - `SettingsScreen`: diÃ¡logos de backup criptografado e restauraÃ§Ã£o ("Cancelar").
- **Testes Automatizados**:
  - Criado `AppStringsTest` garantindo que todos os namespaces e constantes de interface possuam os valores contratuais esperados e previnam regressÃµes acidentais.
  - SuÃ­te completa de 125+ testes unitÃ¡rios executada com 100% de sucesso (`BUILD SUCCESSFUL`).
- **SeguranÃ§a para RefatoraÃ§Ãµes Futuras**:
  - Quaisquer futuras renomeaÃ§Ãµes conceituais (ex: "Pendente" $\rightarrow$ "A Pagar", "Vencida" $\rightarrow$ "Em Atraso") agora sÃ£o realizadas de forma pontual e atÃ´mica em um Ãºnico arquivo, com zero risco de divergÃªncia entre telas.

---

## [1.9.0] - 2026-10-07

### ðŸ”’ Criptografia AES-256-GCM para Backups com ProteÃ§Ã£o por Senha/PIN

- **Criptografia SimÃ©trica Forte (AEAD AES-256-GCM)**:
  - Implementada criptografia simÃ©trica autenticada (`AES/GCM/NoPadding`) de 256 bits via `BackupCryptoHelper`, eliminando o risco de vazamento de dados em texto plano em backups locais e compartilhamentos via ShareSheet (WhatsApp, E-mail, etc.).
  - DerivaÃ§Ã£o de chaves via `PBKDF2WithHmacSHA256` com 65.536 iteraÃ§Ãµes, sal criptogrÃ¡fico aleatÃ³rio de 16 bytes e IV aleatÃ³rio de 12 bytes gerados via `SecureRandom`.
  - Descarte defensivo imediato da senha em memÃ³ria RAM (`clearPassword()` e preenchimento com zeros).
- **Envelope CriptogrÃ¡fico Seguro (`EncryptedBackupDto`)**:
  - Dados exportados em envelope versionado contendo parÃ¢metros do KDF, vetor de inicializaÃ§Ã£o e o criptograma em Base64, protegendo 100% dos dados financeiros contra inspeÃ§Ã£o nÃ£o autorizada.
- **Contratos e Casos de Uso com Senha**:
  - `BackupRepository`: atualizado para exigir senha em `exportBackupJson(password)` e `restoreBackupFromJson(backupJson, password)`.
  - `ExportBackupUseCase` e `RestoreBackupUseCase`: validaÃ§Ã£o mandatÃ³ria de preenchimento e integridade.
- **Interface e ExperiÃªncia do UsuÃ¡rio (UI/UX)**:
  - `SettingsScreen`: diÃ¡logos dedicados `CreateBackupPasswordDialog` (criaÃ§Ã£o e confirmaÃ§Ã£o de senha de no mÃ­nimo 4 caracteres com alertas visuais) e `RestorePasswordDialog` (solicitaÃ§Ã£o de senha para descriptografia e restauraÃ§Ã£o).
  - Tratamento de erro detalhado informando explicitamente quando a senha/PIN fornecido estiver incorreto.
- **Testes Automatizados**:
  - `BackupCryptoHelperTest`: validaÃ§Ã£o de ciclo completo de criptografia/descriptografia, confidencialidade do ciphertext contra vazamento de tokens, integridade contra adulteraÃ§Ã£o e rejeiÃ§Ã£o de senhas incorretas.
  - `BackupRepositoryImplTest`: validaÃ§Ã£o do ciclo exportaÃ§Ã£o $\rightarrow$ restauraÃ§Ã£o com senha correta e rejeiÃ§Ã£o atÃ´mica com senha incorreta.
  - `SettingsViewModelTest`: testes atualizados com a nova API protegida por senha.
- **GovernanÃ§a & Arquitetura**:
  - Registrada a [ADR 025: Criptografia SimÃ©trica AES-256-GCM para Backups com ProteÃ§Ã£o por Senha/PIN](.ai/DECISIONS/025-criptografia-aes-gcm-backups-protegidos-por-senha.md).

---

## [1.8.0] - 2026-10-07

### ðŸ’³ TipificaÃ§Ã£o de Contas Financeiras (`FinancialAccountType`) & MigraÃ§Ã£o Room v13

- **Enum de DomÃ­nio Puro (`FinancialAccountType`)**:
  - Criado o enum `FinancialAccountType` (`CORRENTE`, `CARTEIRA`, `POUPANCA`, `INVESTIMENTO`) com rÃ³tulos amigÃ¡veis (`displayName`) e resoluÃ§Ã£o de aliases legados (`fromString`).
  - SubstituÃ­do o campo `accountType: String` livre em `FinancialAccount` pelo enum tipado.
- **EvoluÃ§Ã£o de PersistÃªncia no Room (`PlatformDatabase` v13)**:
  - Criado `FinancialAccountTypeConverter` para serializaÃ§Ã£o e deserializaÃ§Ã£o no banco SQLite.
  - Implementada a migraÃ§Ã£o `MIGRATION_12_13`, normalizando strings legadas (`CHECKING`, `CASH`, `SAVINGS`, `Conta Corrente`, etc.) para os nomes canÃ´nicos do enum.
  - Registrada a migraÃ§Ã£o no provider do `AppModule`.
- **Mapeamento em Entidades e Casos de Uso**:
  - `FinancialAccountEntity` atualizado com o enum e mapeamento bidirecional `toDomain()` / `fromDomain()`.
  - MÃ©trica de dashboard `AccountSpend` atualizada com o campo `accountType: FinancialAccountType`, mapeando rÃ³tulo e tipo via `GetFinancialDashboardUseCase`.
- **Interface e ExperiÃªncia do UsuÃ¡rio (UI/UX)**:
  - `ManagementScreen`: badges e listas de detalhe exibem `account.accountType.displayName`. DiÃ¡logo de criaÃ§Ã£o e ediÃ§Ã£o (`AddEditAccountDialog`) utiliza dropdown baseado em `FinancialAccountType.entries` com Ã­cones temÃ¡ticos contextuais.
  - `StatisticsScreen`: card `AccountsDistributionCard` exibe Ã­cones representativos (`AccountBalance`, `Payments`, `Savings`, `TrendingUp`) e subtÃ­tulo informativo com o tipo da conta.
- **Testes Automatizados**:
  - Novos testes unitÃ¡rios em `FinancialAccountTypeTest` cobrindo conversÃ£o de aliases, display names, fallbacks, `FinancialAccountTypeConverter` e `FinancialAccountEntity`.
  - AtualizaÃ§Ã£o dos testes em `ManagementViewModelTest`.
- **GovernanÃ§a & Arquitetura**:
  - Registrada a [ADR 024: Enum FinancialAccountType e MigraÃ§Ã£o Room v13](.ai/DECISIONS/024-enum-financial-account-type-e-migracao-room.md).

---

## [1.7.0] - 2026-10-07

### ðŸ‘¥ TipificaÃ§Ã£o de Contatos (`ContactType`) & MigraÃ§Ã£o Room v12

- **Enum de DomÃ­nio Puro (`ContactType`)**:
  - Introduzido o enum `ContactType` (`PESSOA_FISICA`, `FORNECEDOR`, `ORGAO_PUBLICO`) com mÃ©todo utilitÃ¡rio resiliente `fromString()`.
  - Atualizado o modelo `Contact` para conter `val type: ContactType = ContactType.FORNECEDOR`.
- **EvoluÃ§Ã£o de PersistÃªncia no Room (`PlatformDatabase` v12)**:
  - Adicionada a coluna `type TEXT NOT NULL DEFAULT 'FORNECEDOR'` na tabela `contacts` via migraÃ§Ã£o `MIGRATION_11_12`.
  - Registrada a migraÃ§Ã£o no `AppModule` preservando a integridade dos dados existentes.
- **Filtros e Agrupamento na Tela de Contatos (`ContactsScreen`)**:
  - Adicionada barra de chips de filtro superior (`ContactTypeFilterRow`) com contadores dinÃ¢micos para "Todos", "Pessoa FÃ­sica", "Fornecedor" e "Ã“rgÃ£o PÃºblico".
  - Agrupamento visual automÃ¡tico por seÃ§Ãµes temÃ¡ticas (`ContactSectionHeader`) quando a visualizaÃ§Ã£o estiver em "Todos".
  - Filtro exclusivo e direto ao selecionar um tipo especÃ­fico.
- **Indicadores Visuais de Tipo de Contato (`ContactTypeBadge` & `PlatformAvatar`)**:
  - Chip temÃ¡tico com Ã­cone dedicado (`Person`, `Business`, `AccountBalance`) e paleta semÃ¢ntica em cada card de contato.
  - Avatar colorido dinamicamente com a identidade visual do tipo.
  - IntegraÃ§Ã£o do badge no cabeÃ§alho da tela de detalhes (`ContactDetailScreen`).
- **Seletor de Tipo no FormulÃ¡rio (`AddContactBottomSheet`)**:
  - Seletor ergonÃ´mico em cartÃµes para escolha rÃ¡pida do tipo de contato durante criaÃ§Ã£o ou ediÃ§Ã£o.
- **Testes Automatizados**:
  - Novos testes unitÃ¡rios em `ContactsViewModelTest` cobrindo filtragem por cada tipo, restauraÃ§Ã£o e busca textual combinada com filtro.
- **GovernanÃ§a & Arquitetura**:
  - Criada a [ADR 023: TipificaÃ§Ã£o de Contatos (ContactType) e MigraÃ§Ã£o Room v12](.ai/DECISIONS/023-tipificacao-contatos-e-migracao-room.md).

---

## [1.6.1] - 2026-10-07

### ðŸŽ¨ SemÃ¢ntica e Hierarquia Visual na VisÃ£o Mensal (`HomeScreen`)

- **RÃ³tulo SemÃ¢ntico "Restante a Pagar no MÃªs"**:
  - Renomeado de "Total Previsto no MÃªs" para "Restante a Pagar no MÃªs" no card executivo `ForecastImpactCard`, eliminando a ambiguidade com o total orÃ§ado e deixando claro que o montante destacado reflete apenas o saldo ainda em aberto.
- **Hierarquia Visual ReforÃ§ada para QuitaÃ§Ã£o Completa**:
  - Quando todas as contas do mÃªs estiverem pagas (`R$ 0,00` restante), a UI destaca o badge de sucesso **"Tudo quitado"** com Ã­cone de confirmaÃ§Ã£o no topo do card, **antes** da exibiÃ§Ã£o do valor.
  - O valor `R$ 0,00` adota a tonalidade esmeralda de sucesso (`SuccessGreen`), reforÃ§ando visualmente a conquista de quitaÃ§Ã£o em vez de sugerir ausÃªncia de dados ou valor zerado neutro.

---

## [1.6.0] - 2026-10-07

### ðŸ”„ ExtensÃ£o ContÃ­nua de RecorrÃªncias FOREVER (Janela Deslizante AutomÃ¡tica)

- **GeraÃ§Ã£o DinÃ¢mica de PrÃ³ximas OcorrÃªncias (`CalculateInstallmentsUseCase`)**:
  - Novo mÃ©todo `generateNextRecurringInstallments(bill, existingInstallments, countToAdd)` para gerar parcelas subsequentes sem descontinuidade.
  - CÃ¡lculo de passos ancorado na primeira parcela para evitar desvios cumulativos de dia de vencimento (ex: preservaÃ§Ã£o estrita do dia 31 apÃ³s meses menores como fevereiro).
  - Continuidade estrita da numeraÃ§Ã£o das parcelas (`installmentNumber = lastNumber + 1..lastNumber + count`) e atualizaÃ§Ã£o do total projetado.
- **Caso de Uso de ExtensÃ£o de Janela (`ExtendRecurringBillsUseCase`)**:
  - Novo caso de uso de domÃ­nio puro (`domain/usecase/ExtendRecurringBillsUseCase`), sem dependÃªncias do framework Android.
  - AvaliaÃ§Ã£o de limiares por frequÃªncia:
    - `MONTHLY`: estende em +12 parcelas quando restarem $\le 3$ ocorrÃªncias futuras ou horizonte $\le 3$ meses.
    - `DAILY`: estende em +30 parcelas quando restarem $\le 7$ ocorrÃªncias.
    - `WEEKLY`: estende em +26 parcelas quando restarem $\le 4$ ocorrÃªncias.
    - `YEARLY`: estende em +5 parcelas quando restar $\le 1$ ocorrÃªncia.
  - LaÃ§o auto-recuperativo com limite de seguranÃ§a (`maxBatches = 5`), garantindo projeÃ§Ã£o adequada mesmo apÃ³s longos perÃ­odos sem abertura do app.
  - IdempotÃªncia total e garantia de que contas `BY_OCCURRENCES` e `UNTIL_DATE` permaneÃ§am estritamente delimitadas.
- **Suporte Transacional no RepositÃ³rio & DAO (`FinancialRepositoryImpl` & `BillInstallmentDao`)**:
  - Novo mÃ©todo `addInstallments(bill, newInstallments)` com inserÃ§Ã£o transacional no Room e atualizaÃ§Ã£o do `recurrenceEndDate` e `totalInstallments` na tabela `bills`.
  - Nova consulta detalhada `getInstallmentsWithDetailsByBillId(billId)` em `BillInstallmentDao`.
- **Garantia de ExecuÃ§Ã£o Oportuna e PeriÃ³dica**:
  - InvocaÃ§Ã£o automÃ¡tica ao inicializar e atualizar `DashboardViewModel`, `BillsViewModel` (Registros) e `RecurringInstallmentsViewModel` (Assinaturas).
  - ExecuÃ§Ã£o matinal em segundo plano no `DueReminderReceiver` (integrado com o `AlarmManager` existente) e no desbloqueio do app em `MainActivity`.
- **Testes Automatizados**:
  - Nova suÃ­te de testes unitÃ¡rios `ExtendRecurringBillsUseCaseTest` validando extensÃ£o pontual, idempotÃªncia com janelas cheias, isolamento de contas nÃ£o-FOREVER e recuperaÃ§Ã£o temporal.
  - Novos testes em `CalculateInstallmentsUseCaseTest` cobrindo `generateNextRecurringInstallments` e resiliÃªncia a exclusÃ£o de parcelas antigas.
  - AtualizaÃ§Ã£o dos testes de apresentaÃ§Ã£o em `BillsViewModelTest` e `RecurringInstallmentsViewModelTest`.
- **GovernanÃ§a & Arquitetura**:
  - CriaÃ§Ã£o da [ADR 022: ExtensÃ£o ContÃ­nua de RecorrÃªncias FOREVER via Janela Deslizante AutomÃ¡tica](.ai/DECISIONS/022-extensao-continua-recorrencias-forever.md).

---

## [1.5.0] - 2026-10-07

### ðŸŽ¯ Rigidez OrÃ§amentÃ¡ria Real 50-30-20 & IntegraÃ§Ã£o de Metas (PoupanÃ§a)

- **IntegraÃ§Ã£o HolÃ­stica de Metas Financeiras como PoupanÃ§a (`GetFinancialDashboardUseCase`)**:
  - InjeÃ§Ã£o de `GoalRepository` no caso de uso do dashboard financeiro.
  - ApuraÃ§Ã£o reativa dos aportes mensais em metas atravÃ©s do novo mÃ©todo `goalRepository.getMonthlyContribution(startOfMonth, endOfMonth)`.
  - Base de cÃ¡lculo orÃ§amentÃ¡ria unificada: $\text{Total OrÃ§ado} = \text{Total Contas do MÃªs} + \text{Aportes em Metas}$.
  - DistribuiÃ§Ã£o percentual das 4 naturezas (`OBRIGATORIO`, `NECESSARIO`, `DESEJA`, `NENHUM`) e da PoupanÃ§a agora somam exatamente 100% da capacidade orÃ§amentÃ¡ria.
- **Motor de DiagnÃ³stico Multifaixas de Rigidez (`BudgetRigidityCalculator`)**:
  - EliminaÃ§Ã£o definitiva do falso diagnÃ³stico de "Excelente" quando Deseja e PoupanÃ§a estiverem zerados (`SOBRECARREGADO`: 100% comprometido com despesas bÃ¡sicas, sem margem para estilo de vida ou reservas).
  - 8 estados e faixas estritas de classificaÃ§Ã£o: `VAZIO`, `SOBRECARREGADO`, `ENGESSADO`, `ESTILO_DE_VIDA_ELEVADO`, `SEM_POUPANCA`, `NAO_CLASSIFICADO`, `EQUILIBRADO` e `EXCELENTE`.
  - CritÃ©rio rigoroso para selo "Excelente": Essenciais $\le 60\%$ (ObrigatÃ³rio $\le 50\%$), Desejos entre $10\%$ e $35\%$, PoupanÃ§a $\ge 15\%$ e NÃ£o Classificados $\le 10\%$.
- **HistÃ³rico de Aportes no Room & MigraÃ§Ã£o v11 (`PlatformDatabase`)**:
  - CriaÃ§Ã£o da tabela e entidade `goal_contributions` (`id`, `goalId`, `amountCents`, `date`) com chave estrangeira e exclusÃ£o em cascata.
  - MigraÃ§Ã£o de banco de dados Room `MIGRATION_10_11` (`PlatformDatabase` v11) com auto-migraÃ§Ã£o de saldos existentes e registro no `AppModule`.
  - ExportaÃ§Ã£o e restauraÃ§Ã£o segura de histÃ³rico de aportes via Storage Access Framework em `BackupDataDto` e `BackupRepositoryImpl`.
- **Card Visual Aprimorado (`NatureDistributionCard`)**:
  - RÃ³tulo e texto contextualizados: diagnÃ³stico semafÃ³rico inteligente com tÃ­tulo em destaque e descriÃ§Ã£o orientadora.
  - InclusÃ£o visual dedicada da perna **"PoupanÃ§a (Metas)"** com indicador esmeralda (`#10B981`), valor em centavos formatado e barra de progresso.
  - RodapÃ© elegante com exibiÃ§Ã£o do **Total OrÃ§ado (Despesas + Aportes)**.
- **SuÃ­te de Testes Automatizados**:
  - Novos testes unitÃ¡rios em `GetFinancialDashboardUseCaseTest` cobrindo cenÃ¡rio com 0% em Deseja/PoupanÃ§a (assegurando que nÃ£o sai "Excelente"), distribuiÃ§Ã£o 50-30-20 equilibrada real com aportes de metas e cenÃ¡rios sem poupanÃ§a.
  - Nova suÃ­te de testes de domÃ­nio `BudgetRigidityCalculatorTest` validando todas as fronteiras e estados da matriz de classificaÃ§Ã£o.
- **GovernanÃ§a e Arquitetura**:
  - CriaÃ§Ã£o da [ADR 021: Rigidez OrÃ§amentÃ¡ria 50-30-20 Real com Metas (PoupanÃ§a) e ClassificaÃ§Ã£o Multifaixas](.ai/DECISIONS/021-rigidez-orcamentaria-50-30-20-com-metas.md).
  - SincronizaÃ§Ã£o do diÃ¡logo de novidades `ReleaseNotesDialog` em `SettingsScreen.kt`.

---

## [1.4.5] - 2026-10-07

### Ã°Å¸â€™Â³ Baixa de Pagamento Retroativa & Data Real de Pagamento (`actualPaymentDate`)

- **Suporte a Data Real de Pagamento no Modelo e Banco**:
  - AdiÃƒÂ§ÃƒÂ£o do campo opcional `actualPaymentDate: Long?` no modelo de domÃƒÂ­nio `BillInstallment` e na entidade Room `BillInstallmentEntity`.
  - MigraÃƒÂ§ÃƒÂ£o de banco de dados Room `MIGRATION_9_10` (`PlatformDatabase` v10) aplicando `ALTER TABLE bill_installments ADD COLUMN actualPaymentDate INTEGER DEFAULT NULL`.
  - MigraÃƒÂ§ÃƒÂ£o registrada e injetada no `AppModule`.
- **DiÃƒÂ¡logo Elegante de Baixa de Pagamento (`ConfirmPaymentDialog`)**:
  - Novo diÃƒÂ¡logo nativo Material 3 permitindo ao usuÃƒÂ¡rio informar a data real do pagamento na baixa da conta.
  - Atalhos instantÃƒÂ¢neos de 1 toque: "Hoje" e "No Vencimento".
  - Seletor de data (`DatePickerDialog`) permitindo conciliaÃƒÂ§ÃƒÂµes e baixas retroativas ou programadas com total flexibilidade.
  - Integrado no fluxo de quitaÃƒÂ§ÃƒÂ£o em `BillsScreen`, `HomeScreen`, `RecurringInstallmentsScreen`, `ContactDetailScreen` e `EditInstallmentBottomSheet`.
- **CÃƒÂ¡lculo Preciso da Taxa de Pagamentos Pontuais (`GetFinancialDashboardUseCase`)**:
  - O cÃƒÂ¡lculo da mÃƒÂ©trica `onTimePaymentRate` agora compara a data real de pagamento (`actualPaymentDate`), com fallback para `paidAt` e `dueDate`, contra o vencimento da parcela.
  - Baixas retroativas de contas pagas no prazo nÃƒÂ£o sÃƒÂ£o mais classificadas erroneamente como atrasadas.
- **SuÃƒÂ­te de Testes UnitÃƒÂ¡rios**:
  - Testes unitÃƒÂ¡rios atualizados e adicionados em `ToggleInstallmentPaymentUseCaseTest`, `GetFinancialDashboardUseCaseTest`, `BillsViewModelTest` e `HomeViewModelTest`, cobrindo quitaÃƒÂ§ÃƒÂ£o retroativa no prazo, quitaÃƒÂ§ÃƒÂ£o retroativa em atraso, fallback e desmarcaÃƒÂ§ÃƒÂ£o de pagamento.
- **GovernanÃƒÂ§a e Arquitetura**:
  - CriaÃƒÂ§ÃƒÂ£o da [ADR 020: Data Real de Pagamento, Baixa Retroativa e MigraÃƒÂ§ÃƒÂ£o Room v10](.ai/DECISIONS/020-data-real-pagamento-e-baixa-retroativa.md).

---

## [1.4.4] - 2026-09-29

### ÃƒÂ¢Ã…â€œÃ‚ÂÃƒÂ¯Ã‚Â¸Ã‚Â GestÃƒÆ’Ã‚Â£o e EdiÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o de Itens de Despesa (`ExpenseItemsScreen` & `NewExpenseScreen`)

- **EdiÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o Completa de Itens de Despesa**:
  - ImplementaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o do fluxo de ediÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o reativo para itens de despesa existentes via `AddEditExpenseItemBottomSheet`, permitindo renomear o item ou alterar sua categoria vinculada com heranÃƒÆ’Ã‚Â§a instantÃƒÆ’Ã‚Â¢nea da nova natureza financeira.
- **HarmonizaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o com o PadrÃƒÆ’Ã‚Â£o Universal de Detalhes (`ExpenseItemDetailBottomSheet`)**:
  - O toque direto em qualquer card de item agora abre um `ModalBottomSheet` dedicado com avatar na cor da categoria, nome do item, identificadores, chip de natureza e menu contextual de 3 pontos (`MoreVert`).
  - Acesso direto ÃƒÆ’Ã‚Â  aÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o "Editar Item" via botÃƒÆ’Ã‚Â£o de largura total na base do sheet e via menu de 3 pontos.
  - AÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o de exclusÃƒÆ’Ã‚Â£o mantida de forma segura com diÃƒÆ’Ã‚Â¡logo de confirmaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o `AlertDialog`.
- **Atalhos e Acesso RÃƒÆ’Ã‚Â¡pido em Nova Despesa (`NewExpenseScreen`)**:
  - Adicionada opÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o direta "Gerenciar Itens (Criar / Editar)" no menu dropdown de seleÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o de itens e botÃƒÆ’Ã‚Â£o de ediÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o rÃƒÆ’Ã‚Â¡pida de 1 toque no chip de natureza herdada, permitindo ajustar itens sem perder o contexto do lanÃƒÆ’Ã‚Â§amento.
- **SuÃƒÆ’Ã‚Â­te de Testes Automatizados**:
  - Nova suÃƒÆ’Ã‚Â­te de testes unitÃƒÆ’Ã‚Â¡rios `ExpenseItemsViewModelTest.kt` validando carga inicial, filtro de pesquisa, filtro por categoria, criaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o e salvamento de itens editados (preservaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o de ID) e exclusÃƒÆ’Ã‚Â£o.

---

## [1.4.3] - 2026-09-29

### ÃƒÆ’Ã‚Â°Ãƒâ€¦Ã‚Â¸Ãƒâ€¦Ã‚Â¡ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ Pacote de ExcelÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âªncia Operacional & UX (Visual, AÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âµes, FunÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âµes e Sistema)

- **1. Visual: TransiÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âµes de Tela CinemÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ticas e Fluidas (`NavGraph.kt`)**:
  - ImplementaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o de transiÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âµes animadas nativas no `NavHost` (`slideIntoContainer` horizontal + `fadeIn`/`fadeOut` interpolados com `tween(260ms)`).
  - ExperiÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âªncia visual contÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â­nua sem saltos abruptos de layout ao transitar entre Dashboard, Registros, CartÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âµes, Recorrentes e EstatÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â­sticas.

- **2. AÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âµes (UX): Mecanismo de Desfazer (`Undo`) para Baixa de Contas e Faturas**:
  - IntegraÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o de `SnackbarHost` e canal de efeitos `HomeUiEffect.ShowUndoSnackbar` na `HomeScreen`.
  - Ao quitar uma conta avulsa ou fatura com 1 toque, ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â© exibido um Snackbar com aÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o **"Desfazer"**, permitindo reversÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o imediata de cliques acidentais.
  - Adicionado `reopenInvoice` em `FinancialRepositoryImpl.kt` com transaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o atÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â´mica (`database.withTransaction`), restaurando o status da fatura para `ABERTA` e suas parcelas vinculadas para `PENDING`.

- **3. FunÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âµes: DuplicaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o Inteligente de Despesas (`duplicateBillId`)**:
  - Nova opÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o **"Duplicar Despesa"** no menu de contexto (`DropdownMenu`) do BottomSheet de detalhes da parcela (`BillsScreen.kt`).
  - NavegaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o para `NewExpenseScreen` carregando a despesa original via `SavedStateHandle` no `NewExpenseViewModel`, clonando valor, categoria, item, contato, conta bancÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ria, forma de pagamento e tipo, acelerando cadastros repetitivos.

- **4. Sistema: Lembretes e NotificaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âµes Locais de Vencimento Offline**:
  - CriaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o do canal e gerenciador `DueReminderManager.kt` (`com.platform.app.core.notification`) de alta prioridade.
  - `DueReminderReceiver.kt`: BroadcastReceiver com `@AndroidEntryPoint` e injeÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o Hilt para checagem diÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ria programada via `AlarmManager`.
  - VerificaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o inteligente na `MainActivity`: Notifica o usuÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡rio de forma offline e discreta caso existam contas ou faturas com vencimento no prÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â³prio dia.
  - PermissÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o `POST_NOTIFICATIONS` declarada no `AndroidManifest.xml`.

- **5. Qualidade e Testes UnitÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡rios**:
  - Novos testes automatizados: `HomeViewModelTest.kt` (aÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âµes de pagamento, undo de contas e faturas) e `NewExpenseViewModelTest.kt` (prÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â©-carregamento por duplicaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o).
  - 100% dos testes unitÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡rios validados e aprovados com sucesso (`./gradlew testDebugUnitTest`).

---

## [1.4.2] - 2026-09-29

### ÃƒÆ’Ã‚Â°Ãƒâ€¦Ã‚Â¸ÃƒÂ¢Ã¢â€šÂ¬Ã…â€œÃƒâ€¦Ã‚Â  InteligÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âªncia Financeira 360ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â° & RefatoraÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o da Tela de EstatÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â­sticas
- **VisÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o Temporal Integrada em 3 Eixos (Passado, Presente e Futuro)**:
  - NavegaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o fluida via `PlatformSegmentedTabs` (`Passado`, `Presente`, `Futuro`) com suporte nativo ao **Modo Privacidade** (`PlatformPrivacyToggle`).
- **1. Eixo Passado (HistÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â³rico & TendÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âªncias ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â Onde estivemos)**:
  - **MÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â©dia HistÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â³rica Mensal**: Baseline de custo de vida para guiar novos planejamentos.
  - **EvoluÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o dos ÃƒÆ’Ã†â€™Ãƒâ€¦Ã‚Â¡ltimos 6 Meses**: Barras comparativas mÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âªs a mÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âªs de valor devido vs valor liquidado, com taxa de quitaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o individual e atalho de salto direto para o mÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âªs.
  - **Comparativo com o MÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âªs Anterior**: VariaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o em R$ e % com badges semafÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â³ricos de alta/baixa.
  - **Picos e Vales HistÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â³ricos**: IdentificaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o do mÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âªs mais pesado vs mÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âªs mais econÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â´mico.
  - **Ranking de Top DestinatÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡rios / Fornecedores**: Contatos que mais absorveram recursos.
- **2. Eixo Presente (Raio-X do MÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âªs Selecionado ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â Onde estamos)**:
  - **Hero Card de ExecuÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o OrÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§amentÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ria**: Total devido, valor pago, pendente e atrasado com termÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â´metro semafÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â³rico `PlatformProgressBar`.
  - **Rigidez OrÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§amentÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ria (Regra 50/30/20 & Natureza dos Gastos)**: ClassificaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o em `ObrigatÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â³rio`, `NecessÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡rio`, `Deseja` e `Nenhum` com card de diagnÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â³stico estratÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â©gico inteligente (*Alerta de OrÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§amento Engessado* caso ObrigatÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â³rio > 55%).
  - **Meio de LiquidaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o & CrÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â©dito**: Barra bifurcada e percentuais de exposiÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o entre CartÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o de CrÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â©dito vs DÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â©bito/Pix/Dinheiro.
  - **Top Categorias & Contas BancÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡rias**: DistribuiÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o visual dos gastos e concentraÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o institucional.
- **3. Eixo Futuro (Previsibilidade & Tomada de DecisÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â Para onde vamos)**:
  - **Curva de DesoneraÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o (PrÃƒÆ’Ã‚Â³ximos 6 Meses)**: EvoluÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o decrescente dos pagamentos com destaque para o mÃƒÆ’Ã‚Âªs de maior pico e o mÃƒÆ’Ã‚Âªs de maior folga financeira.
  - **DesoneraÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o & TÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â©rmino de Parcelas**: Cronograma de compras parceladas ativas que chegam ao fim, com cÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡lculo de alÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â­vio mensal gerado (*"+R$ X/mÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âªs livre"*).
  - **Cockpit de Tomada de DecisÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o (Smart Advisor)**: Consultoria contextual em tempo real para responder *"Posso assumir uma nova compra parcelada agora?"*.
- **4. DomÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â­nio & Testes Automatizados**:
  - Modelos enriquecidos em `FinancialDashboardMetrics.kt` e agregaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o performÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡tica em `GetFinancialDashboardUseCase.kt`.
  - 100% da suÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â­te unitÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ria aprovada (`./gradlew testDebugUnitTest`).

---

## [1.4.1] - 2026-09-29

### ÃƒÆ’Ã‚Â°Ãƒâ€¦Ã‚Â¸ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢Ãƒâ€¦Ã‚Â½ Grande Update das 4 Telas Centrais (Obsidian & Porcelain Minimalist)
- **1. Tela Inicial (`HomeScreen`)**:
  - **Executive Hero Card**: AdiÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o de barra de progresso horizontal (`PlatformProgressBar`) com cantos arredondados indicando o percentual de quitaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o do mÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âªs (ex: *"68% quitado"*) e mÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â©tricas de JÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ Quitado e Total Geral.
  - **Modo Privacidade (`PlatformPrivacyToggle`)**: Alternador com ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â­cone de olho para mascarar valores confidenciais (`R$ ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â¢`) em locais pÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âºblicos.
  - **Seletor de MÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âªs RÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡pido em Grade (`MonthPickerBottomSheet`)**: Grade visual de meses e seletor de ano direto no tÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â­tulo do mÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âªs, eliminando cliques repetitivos em setas.
  - **Empty State Triunfante (`MonthVictoryCard`)**: Card comemorativo esmeralda quando todas as contas do mÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âªs estiverem 100% quitadas.
  - **Micro-interaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âµes HÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡pticas**: VibraÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o suave ao efetuar a baixa de contas na lista.
- **2. Tela de Registros (`BillsScreen`)**:
  - **Segmented Tabs no Topo (`PlatformSegmentedTabs`)**: DivisÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o em 3 abas especializadas (`A Pagar`, `Pagas`, `Todas` com contadores numÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â©ricos).
  - **Banner de Totais Filtrados**: Resumo instantÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢neo do valor total e quantidade de registros visÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â­veis.
  - **AÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âµes em Lote (`PlatformBatchActionBar`)**: Modo de seleÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o mÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âºltipla (toque longo ou botÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o no TopBar) para liquidar ou excluir vÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡rias contas com 1 confirmaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o.
- **3. Tela de Recorrentes & Parcelados (`RecurringInstallmentsScreen`)**:
  - **SeparaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o por Abas**: DivisÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o nÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â­tida entre `Compras Parceladas` (amortizaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o com data final) e `Assinaturas & Custos Fixos` (compromisso mensal contÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â­nuo).
  - **Card de AmortizaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o Estilo Financiamento**: Progresso visual com `PlatformProgressBar`, projeÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o de data de quitaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o final (*"TÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â©rmino em MÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âªs/Ano"*) e saldo devedor restante.
  - **Cronograma de Parcelas**: Acompanhamento detalhado de cada parcela com baixa direta.
  - **ProjeÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o Anual de Assinaturas**: ExibiÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o do custo anual consolidado (*"R$ Y/ano"*).
- **4. Tela de CartÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âµes de CrÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â©dito (`CreditCardsScreen`)**:
  - **Design Virtual Apple Card / Revolut (`PlatformCreditCardView`)**: ProporÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o bancÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ria exata (`1.586f`), chip EMV vetorial metÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡lico, gradiente acetinado e termÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â´metro de limite inteligente semafÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â³rico integrado.
  - **Ciclo de 3 Faturas**: AlternÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ncia de abas entre `Aberta`, `Fechadas`, `Pagas` e `Todas`.
  - **Destaque do Melhor Dia de Compra**: OrientaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o visual do dia de corte para compras com atÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â© 40 dias de prazo.
  - **Atalho de Nova Compra**: BotÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o direto no extrato para lanÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ar despesa prÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â©-selecionando o cartÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o.
- **5. Novos Componentes ReutilizÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡veis**:
  - `PlatformProgressBar.kt`, `PlatformPrivacyToggle.kt`, `PlatformSegmentedTabs.kt`, `PlatformBatchActionBar.kt`, `PlatformCreditCardView.kt`.
- **6. ValidaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o e Qualidade**:
  - 100% da suÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â­te `./gradlew testDebugUnitTest` aprovada e build limpo em 4 segundos.

---

## [1.4.0] - 2026-09-29

### ÃƒÆ’Ã‚Â°Ãƒâ€¦Ã‚Â¸Ãƒâ€¦Ã‚Â¡ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ RefatoraÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o End-to-End: Wallet 100% Pessoal & GestÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o de Contas e Faturas
- **Contexto 100% Pessoal Unificado**:
  - EliminaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o definitiva de qualquer perfil corporativo/pessoal (`profileType`, `PESSOAL`/`EMPRESA`), operando com foco exclusivo em finanÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§as pessoais.
- **Foco em Contas a Pagar & Previsibilidade**:
  - Cadastro ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡gil e intuitivo exigindo apenas 3 dados obrigatÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â³rios: DescriÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o, Valor (em centavos `Long`) e Vencimento.
  - Seletores de Categoria e Item opcionais.
- **Natureza Financeira (ExpenseNature) & Itens de Despesa**:
  - ImplementaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o dos 4 pilares: `OBRIGATORIO` (custos inegociÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡veis), `NECESSARIO` (manutenÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o essencial), `DESEJA` (estilo de vida e conforto) e `NENHUM` (transitÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â³rios/nÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o classificados).
  - DepreciaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o de subcategorias e criaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o da tabela `expense_items` vinculada diretamente a `categories`, com heranÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§a estrita da natureza da categoria mÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£e.
  - Nova tela `ExpenseItemsScreen` em ConfiguraÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âµes para listar, pesquisar e cadastrar itens vinculados com prÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â©-visualizaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o de natureza.
- **Efeito Cascata no Dashboard em Tempo Real**:
  - Total Previsto no MÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âªs calculado dinamicamente via Flow somando contas avulsas pendentes e faturas de cartÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o abertas/fechadas do perÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â­odo.
  - Agrupamento semafÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â³rico de urgÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âªncia: ÃƒÆ’Ã‚Â°Ãƒâ€¦Ã‚Â¸ÃƒÂ¢Ã¢â€šÂ¬Ã‚ÂÃƒâ€šÃ‚Â´ Atrasadas, ÃƒÆ’Ã‚Â°Ãƒâ€¦Ã‚Â¸Ãƒâ€¦Ã‚Â¸Ãƒâ€šÃ‚Â¡ Vence Hoje, ÃƒÆ’Ã‚Â¢Ãƒâ€¦Ã‚Â¡Ãƒâ€šÃ‚Âª PrÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â³ximos 7 Dias e ÃƒÆ’Ã‚Â°Ãƒâ€¦Ã‚Â¸Ãƒâ€¦Ã‚Â¸Ãƒâ€šÃ‚Â¢ Pagas no MÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âªs (colapsÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡vel).
  - Baixa com 1 Toque ("Pagar"): atualiza status para `PAGO` no banco e deduz instantaneamente do total mensal sem recarregar a tela.
- **CartÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âµes de CrÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â©dito & Extrato de Faturas**:
  - CÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡lculo robusto de limite disponÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â­vel (`CreditCardCalculator`), fechamento de faturas baseado no dia de corte (`closingDay`) e status da fatura (`ABERTA`, `FECHADA`, `PAGA`).
  - Extrato detalhado de faturas em `CreditCardsScreen` com botÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o de liquidaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o em 1 toque.
- **PersistÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âªncia Room & MigraÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o**:
  - AtualizaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o do `PlatformDatabase` para versÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o `7` com `MIGRATION_6_7`.
  - InclusÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o da entidade `TransactionEntity` e DAO `TransactionDao` para rastreamento de transaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âµes.
  - Tipo alias `AppDatabase = PlatformDatabase` para interoperabilidade.
- **Design System Material 3 (ADR-018)**:
  - AplicaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o estrita de tokens de cores para Dark Mode e Light Mode.
  - ProibiÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o de cores literais nos componentes; uso estrito de `MaterialTheme.colorScheme`.
  - Tipografia em Title Case (sem ALL CAPS em textos de tela).
  - ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âcones funcionais do Material Icons substituindo formas abstratas.
- **LanÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§amento Focado em Itens (Item-Centric Expenses)**:
  - O Item de Despesa (`selectedItemId`) assumiu o protagonismo absoluto no formulÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡rio: primeiro campo de seleÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o com preview de Natureza Financeira e Categoria.
  - A descriÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o passa a ser campo de "ObservaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âµes Adicionais (Opcional)", herdando o nome do item selecionado como tÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â­tulo padrÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o caso nÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o preenchida.
  - ReorganizaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o do formulÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡rio em 5 blocos harmÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â´nicos e simÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â©tricos com cards e divisÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âµes semÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢nticas.
- **Design System Premium Minimalista & Simetria CirÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âºrgica**:
  - Paletas Dark e Light purificadas: Dark Obsidian (`#0A0D14` / `#141923`) e Light Porcelain (`#F8FAFC` / `#FFFFFF`).
  - AnulaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o estrita de elevaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o tonal com `surfaceTint = Color.Transparent` em ambos os temas, eliminando 100% qualquer vazamento roxo/lavanda default do Material 3.
  - ErradicaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o completa de cores literais hardcoded (`Color.White`, `Color.Black`, hexadecimais soltos) em todas as telas, adotando exclusivamente tokens de `Theme.kt`, `Color.kt` e `MaterialTheme.colorScheme`.
  - Anti-gigantismo e harmonia visual: ajuste da tipografia do impacto financeiro na HomeScreen de `headlineLarge` para `titleLarge` semibold/bold.
  - Alinhamentos milimÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â©tricos com cantos uniformes de 16.dp para cards, 10.dp para botÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âµes/inputs e bordas finas de 1.dp com `outlineVariant`.
  - **PadronizaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o Global dos FABs (100% Circular)**: UnificaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o dos FloatingActionButtons de todas as 9 telas financeiras (incluindo `RecurringInstallmentsScreen`) com `shape = CircleShape`, container `primary` e conteÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âºdo `onPrimary`.
  - **UnificaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o Absoluta de Fluxos de Despesa**: RemoÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o do modal legado `AddBillBottomSheet` em `BillsScreen`. Tanto a tela Inicial quanto a tela de Registros navegam para a mesma `NewExpenseScreen` com o formulÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡rio em 5 blocos harmÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â´nicos focado em itens.
  - **Componentes Centrais ReutilizÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡veis (`PlatformEmptyState` e `PlatformAvatar`)**:
    - `PlatformEmptyState`: EliminaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o definitiva de emojis informais (`ÃƒÆ’Ã‚Â°Ãƒâ€¦Ã‚Â¸ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢Ãƒâ€šÃ‚Â³`, `ÃƒÆ’Ã‚Â°Ãƒâ€¦Ã‚Â¸ÃƒÂ¢Ã¢â€šÂ¬Ã…â€œÃƒâ€¹Ã¢â‚¬Â `) em telas vazias, substituÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â­dos por containers vetoriais com fundo translÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âºcido a 12% e tipografia proporcional `titleLarge` semibold.
    - `PlatformAvatar`: EliminaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o de cÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â³digo duplicado de cÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡lculo de iniciais e renderizaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o visual homogÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âªnea em `ContactsScreen` e `ContactDetailScreen`.
  - **HigienizaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o Visual Estrita (Zero AÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âµes Inline NÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o Seguras)**:
    - `ExpenseItemsScreen`: RemoÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o de botÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âµes de exclusÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o inline em cards, substituÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â­dos por navegaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o sutil com chevron e `AlertDialog` de confirmaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o seguro.
    - `BudgetsScreen`: MigraÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o de `BudgetCard` para `PlatformCard` com raio de 16.dp e contorno suave.
    - `CreditCardsScreen`: Ajuste de contraste do botÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o "Liquidar Fatura" (`onPrimary` garantindo legibilidade perfeita no Tema Escuro e Claro).
    - `SettingsScreen`: DistinÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o de ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â­cone para Itens de Despesa com `Icons.Default.ShoppingBag`.
    - `AppDrawer`: CorreÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o do divisor de seÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âµes com verificaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o insensÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â­vel a maiÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âºsculas/minÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âºsculas (`ignoreCase = true`).
  - **EliminaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o de CÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â³digo Zumbi & DepreciaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âµes**:
    - RemoÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o do arquivo zumbi `DashboardScreen.kt` (-1.073 linhas de cÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â³digo morto).
    - MigraÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o de todos os ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â­cones deprecados para `Icons.AutoMirrored.Filled` (`ReceiptLong` e `TrendingUp`), zerando warnings de compilaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o.
- **Cobertura de Testes Automatizados**:
  - `NewExpenseValidationTest`: 6 testes automatizados validando campos obrigatÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â³rios (Item e Contato), heranÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§a de natureza, cÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡lculo de parcelas e preenchimento de tÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â­tulo a partir do item.
  - `CreditCardManagementTest`: ValidaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o de fechamento, limite e integridade de faturas.
  - `BillCalculationTest`, `ExpenseItemNatureTest`, `InvoiceClosingTest`: Todos aprovados com 100% de sucesso.

---

## [1.3.1] - 2026-09-28

### ÃƒÆ’Ã‚Â°Ãƒâ€¦Ã‚Â¸Ãƒâ€¦Ã‚Â½Ãƒâ€šÃ‚Â¨ Melhorias Visuais & Ergonomia
- **Ajuste de ProporÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âµes dos Cards em ConfiguraÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âµes**:
  - AmpliaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o da ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡rea de toque e respiro interno dos blocos de acesso direto a **Contas**, **Formas de Pagamento** e **Categorias** (padding vertical aumentado para 14dp e horizontal para 16dp).
  - ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âcones com badges ampliados de 32dp para 44dp e cantos arredondados de 12dp para maior destaque visual e clareza de toque.
  - InclusÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o de subtÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â­tulos descritivos elegantes abaixo de cada tÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â­tulo para reforÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ar o propÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â³sito de cada ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡rea.
  - Tipografia elevada para titleMedium semibold e chevron de navegaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o mais visÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â­vel.
  - EspaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§amento vertical entre blocos unificado em 12dp.

---

## [1.3.0] - 2026-09-28

### ÃƒÆ’Ã‚Â°Ãƒâ€¦Ã‚Â¸Ãƒâ€¦Ã‚Â¡ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ Novas Funcionalidades
- **Backup e RestauraÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o de Dados Offline**:
  - ExportaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o completa e versionada do banco de dados em formato JSON compacto com valores inteiros em centavos (`Long`).
  - RestauraÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o atÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â´mica em lote utilizando transaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âµes seguras do Room (`database.withTransaction`), garantindo rollback total em caso de falha.
  - IntegraÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o com o *Storage Access Framework (SAF)* nativo do Android (`CreateDocument` e `OpenDocument`) para salvar e restaurar do Google Drive, Documentos, Downloads ou cartÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o SD.
  - Compartilhamento rÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡pido via *ShareSheet* (`FileProvider`) direto para WhatsApp, Telegram ou e-mail.
  - Indicador em tempo real nas ConfiguraÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âµes com a data e hora exata do ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âºltimo backup efetuado.
  - DiÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡logo de advertÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âªncia e confirmaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o antes de restauraÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âµes para impedir substituiÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âµes acidentais de dados.

### ÃƒÆ’Ã‚Â°Ãƒâ€¦Ã‚Â¸Ãƒâ€¦Ã‚Â½Ãƒâ€šÃ‚Â¨ HarmonizaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o Visual & PadrÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o de Detalhes (4 Etapas)
- **AdoÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o Universal do PadrÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o de Detalhes com BottomSheet**:
  - Cards 100% limpos e minimalistas em todo o sistema: remoÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o de botÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âµes inline de lixeira, lÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡pis e acordeÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âµes soltos.
  - Toque no card abre `ModalBottomSheet` dedicado com mÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â©tricas contextuais, visualizaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o de vÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â­nculos (conta, categoria, contato, mÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â©todo) e aÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âµes rÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡pidas.
  - Menu de 3 pontos (`MoreVert`) no topo do BottomSheet para ediÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o e exclusÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o segura.
  - DiÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡logos de confirmaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o obrigatÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â³rios (`AlertDialog`) antes de qualquer exclusÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o definitiva.
  - Seletores de campos com opÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âµes prÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â©-definidas migrados para menus dropdown nativos (`ExposedDropdownMenuBox`).
  - Seletor de cores aprimorado com indicador visual circular branco na cor ativa.
- **Telas Harmonizadas**:
  - [ManagementScreen.kt]: Formas de Pagamento e Categorias desacopladas com BottomSheet e seletor com indicador.
  - [BillsScreen.kt]: Cards de parcelas sanitizados, tipografia reduzida de 28sp para 16sp bold, chevron sutil e BottomSheet detalhado de liquidaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o.
  - [GoalsScreen.kt]: Cards de metas limpos, BottomSheet com progresso, aporte rÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡pido e menu de aÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âµes.
  - [BudgetsScreen.kt]: Cards de orÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§amento limpos, BottomSheet com comparaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o teto vs realizado e ajuste de limite.
  - [RecurringInstallmentsScreen.kt]: Cards de contratos enxutos, BottomSheet com detalhes do contrato e listagem completa de parcelas interativas.
  - [ContactDetailScreen.kt]: ExclusÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o segura de contato no menu de 3 pontos com diÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡logo de confirmaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o.

---

## [1.2.0] - 2026-09-28

### ÃƒÆ’Ã‚Â°Ãƒâ€¦Ã‚Â¸Ãƒâ€¦Ã‚Â¡ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ Melhorias & EvoluÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âµes
- **GestÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o Financeira Desacoplada nas ConfiguraÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âµes**: Telas dedicadas e independentes para Contas BancÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡rias, Formas de Pagamento e Categorias, sem acoplamento de abas concorrentes.
- **Menu Lateral Minimalista**: ReduÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o de ruÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â­do visual, eliminaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o de subtÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â­tulos descritivos em todos os itens do drawer, proporÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âµes enxutas e alinhamento com Material 3.
- **Dashboard Financeiro Macro**: Cards inteligentes com visÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o geral de saldo, compromissos do mÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âªs vigente e projeÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o futura de gastos.
- **Recorrentes & Parcelados**: GestÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o centralizada de assinaturas fixas e compras parceladas em andamento com cÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡lculo automÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡tico de amortizaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âµes.
- **Metas & OrÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§amentos**: Controle de reservas financeiras com barra de progresso visual e definiÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o de tetos de gastos mensais por categoria.
- **Cadastro Inteligente de Contatos**: IntegraÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o de busca automÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡tica de CEP via ViaCEP, novos campos estruturados de endereÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§o e ediÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o com 1 toque.
- **AutomaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o de Versionamento**: SincronizaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o automÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡tica entre o arquivo `VERSION`, `app/build.gradle.kts` e scripts de bump para evitar estagnaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o de versÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o.

---

## [1.0.0] - 2026-09-26

### ÃƒÆ’Ã‚Â°Ãƒâ€¦Ã‚Â¸Ãƒâ€¦Ã‚Â¡ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ Melhorias
- FundaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o da arquitetura base do aplicativo Android em Clean Architecture (Presentation, Domain, Data, Core).
- ConfiguraÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o do Android Gradle Plugin com Kotlin DSL (`build.gradle.kts` e Version Catalog `libs.versions.toml`).
- IntegraÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o da stack moderna de UI com Jetpack Compose BOM, Material 3 e suporte a Tema Claro e Escuro.
- InjeÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o de dependÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âªncias desacoplada preparada com Hilt.
- ConfiguraÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o de persistÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Âªncia local com Room Database e comunicaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o remota com Retrofit/OkHttp.
- ImplementaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o de fluxos reativos com Kotlin Coroutines e StateFlow.
- Estrutura completa de governanÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§a de IA (`AGENT_RULES.md`, `.ai/` e `.agents/` com skills e regras).
- AutomaÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â§ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â£o de versionamento mÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â³vel sincronizando `versionCode`, `versionName` e `CHANGELOG.md`.

---




