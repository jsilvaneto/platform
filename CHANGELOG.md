# Changelog - Platform (Android App)

Todas as alterações notáveis neste projeto serão documentadas neste arquivo.
O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/) e este projeto segue [Versionamento Semântico](https://semver.org/lang/pt-BR/).

## [1.25.0] - 2026-10-10

### 🔒 Segurança Robusta, Notificações Resilientes, Política de Backup E2E e Orçamento Preciso (Prompts 37-42)

- **Notificações Confiáveis de Vencimento com WorkManager (Prompt 37)**:
  - Substituição de `AlarmManager` inexato por `WorkManager` periódico de 24h (`ExpenseNotificationWorker`), persistente a reinicializações e inicializado em `PlatformApplication.onCreate()`.
  - Permissão contextual em runtime para `POST_NOTIFICATIONS` no Android 13+ ao salvar despesas no `NewExpenseScreen` e `QuickExpenseBottomSheet`.
  - Banner explicativo de notificações desativadas em `SettingsScreen` (`NotificationDisabledBanner`) com atalho direto para as configurações do sistema operacional.
  - Ações rápidas na própria notificação: "Paguei", "Adiar 1 dia" e "Pagar Fatura" processadas pelo `ExpenseNotificationActionReceiver`.
  - Proteção de privacidade na tela de bloqueio via `NotificationCompat.VISIBILITY_PRIVATE` com notificação pública resumida sem valores monetários explícitos.
  - Ícone monocromático nativo (`ic_stat_notification.xml`) em conformidade com o Android 13+.
  - Queries SQL com filtro por intervalo de datas (`getPendingInstallmentsInRange`, `getOverduePendingInstallments`, `getPendingInvoicesInRange`, `getOverdueInvoices`), eliminando carregamento total do banco em memória.
- **Bloqueio Biométrico Robusto e Proteção Anti-Snooping em Memória (Prompt 38)**:
  - Estado de desbloqueio migrado do `rememberSaveable` para o `AppLockState` `@Singleton` em memória RAM, prevenindo restauração indevida após recriação da Activity ou rotação de tela.
  - Re-bloqueio automático gerenciado por `ProcessLifecycleOwner` (`onStop`/`onStart`) baseado em timeout configurável (Imediato, 30s, 1min, 5min).
  - Inicialização com estado neutro enquanto o DataStore carrega assincronamente, eliminando o "flash" de dados sensíveis na abertura do app.
  - Proteção dinâmica com `FLAG_SECURE` ativada por preferência do usuário para ocultar telas em aplicativos recentes e bloquear prints de tela.
- **Política de Backup do Android e Checkpoint WAL SQLite (Prompt 39)**:
  - Sincronização estrita de `data_extraction_rules.xml` e `backup_rules.xml` adicionando `disableIfNoEncryptionCapabilities="true"` no cloud backup (garantindo apenas backup em nuvem com criptografia de ponta a ponta E2E no Google Drive).
  - Exclusão explícita de `platform_db-wal`, `platform_db-shm` e `device_keystore.xml` dos backups.
  - Execução automática de `PRAGMA wal_checkpoint(TRUNCATE)` em segundo plano (`onStop`) e antes de exportações JSON em `BackupRepositoryImpl.exportBackupJson()`, garantindo consistência relacional e integridade de arquivo.
  - Documentação formal no **ADR 035**.
- **Endurecimento do Release e HTTPS Estrito (Prompt 40)**:
  - `HttpLoggingInterceptor` condicionado a `BuildConfig.DEBUG` (`Level.BODY` apenas em debug, `Level.NONE` em release para não vazar CEP e endereço no logcat).
  - Ativação de `isMinifyEnabled = true` e `isShrinkResources = true` em `buildTypes.release` com regras detalhadas de Proguard/R8 para Room, Retrofit, Gson DTOs, WorkManager e Hilt.
  - Criação de `network_security_config.xml` com `cleartextTrafficPermitted="false"`, bloqueando qualquer tráfego HTTP desprotegido.
  - `MainActivity` com `android:exported="false"` protegida contra inicializações arbitrárias de terceiros.
- **Cálculo de Orçamento via UseCase e Navegação Mensal (Prompt 41)**:
  - Extração da lógica de cálculo para `GetBudgetProgressUseCase`, eliminando duplicidade de contagem entre Teto Geral e tetos de categoria.
  - Alocação temporal de despesas no cartão pela data de compra (`createdAt`) e não pelo vencimento futuro da fatura (`dueDate`).
  - Exclusão estrita de despesas pausadas (`isPaused`) e segregação visual e analítica entre valores pagos (`paidCents`) e pendentes (`pendingCents`).
  - Navegador temporal mensal integrado (`MonthNavigationHeader`) na tela de Orçamentos permitindo consulta de meses anteriores e futuros.
- **Rede de Segurança de UI com Jetpack Compose (Prompt 42)**:
  - Criação de testes instrumentados Compose (`createComposeRule`) em `ComposeUiFlowsTest.kt`, cobrindo overlay de bloqueio biométrico, cartões de despesa e diálogos de backup.

## [1.24.0] - 2026-10-10

### 🛡️ Higiene Arquitetural, Decomposição do Repositório, Guard Rails e Ciclo de Vida de Faturas

- **Exclusão de Status PAUSED da Materialização e dos Totais de Faturas (Prompt 26)**:
  - `BillInstallmentDao.getUnattachedRecurringInstallmentsForCard` agora filtra `i.status != 'PAUSED'`, impedindo que assinaturas temporariamente suspensas sejam vinculadas a novas faturas.
  - Queries de totais de fatura (`getInvoiceTotals`, `getInvoiceTotalsList` e `getInvoiceTotal`) agora excluem parcelas com status `PAUSED` (`WHERE invoiceId IS NOT NULL AND status != 'PAUSED'`), refletindo o valor exato a ser pago.
- **Materialização Preventiva na Quitação da Fatura (`payInvoice`)**:
  - `payInvoice` agora identifica a competência e o cartão da fatura antes da quitação e executa a materialização de ocorrências recorrentes do ciclo, garantindo que compras não fiquem órfãs ou atrasadas fora da fatura paga.
- **Separação CQS: Consulta Pura vs Comando de Materialização**:
  - `getOrCreateInvoiceForMonth` foi desacoplada de efeitos colaterais, atuando como busca/criação determinística pura.
  - Criado o comando explícito `materializeRecurringForInvoice(cardId, invoiceId, referenceMonth)`, invocado atomicamente onde necessário.
  - Otimizado `CreateBillUseCase` com cache por competência (`invoiceCache`), realizando apenas uma consulta por mês distinto.
- **Decomposição Modular de `FinancialRepositoryImpl` em Agregados Coesos (Prompt 27)**:
  - `BillDataSource` (251 linhas): Gerenciamento de contas (`Bill`) e parcelas (`BillInstallment`).
  - `CreditCardDataSource` (189 linhas): Gestão de cartões, faturas e materialização recorrente.
  - `CatalogDataSource` (220 linhas): Gestão de categorias, itens de despesa, contatos, contas bancárias, formas de pagamento e seeds iniciais.
  - `FinancialRepositoryImpl` mantido como fachada limpa e transparente (212 linhas), em estrita conformidade com o limite de 600 linhas.
- **Extensão do Guard Rail de Build `checkFileSize` para Data e Domain**:
  - Task Gradle `:app:checkFileSize` expandida para validar não apenas `presentation/`, mas também `data/` e `domain/`.
  - 100% dos arquivos do projeto respeitam o limite de 600 linhas, mantendo a baseline zerada.
- **Registro Formal de Decisão Técnica (ADR 033)**:
  - Documentada em ADR a decisão arquitetural dos novos guard rails e o comportamento de âncora para despesas de cartão legadas pré-1.20.

### 🎨 Overhaul do Design System e Nova Arquitetura de Informação (Prompts 28-36)

- **Tokens de Forma, Superfícies e Guard Rail de Formas (Prompt 28)**:
  - Criação de `PlatformShapes` (small 8dp, medium 12dp, large 16dp, pill e bottomSheet) e `PlatformSurface` (Flat, Tonal, Outlined).
  - Regra visual do design system: no máximo 1 nível de borda por tela; blocos internos utilizam superfície tonal sutil.
  - Criação do guard rail Gradle `:app:checkRawShapes`, prevenindo usos de `RoundedCornerShape(n.dp)` crus na interface.
- **Papéis Semânticos de Cor e Chips (Prompt 29)**:
  - Papel exclusivo para status: verde para pago, neutro para pendente, âmbar para vence hoje/atenção e vermelho para atrasado.
  - Natureza do gasto convertida em ponto discreto de 8dp, suprimida da linha quando idêntica à da categoria-mãe.
  - Unificação de chips de status em torno do `PlatformStatusChip`.
- **Nova Navegação por Barra Inferior (Prompt 30)**:
  - Substituição definitiva do `AppDrawer` por barra de navegação inferior com 5 destinos: *Hoje*, *Contas*, *Cartões*, *Análises* e *Mais*.
  - Unificação de *Registros* e *Pagamentos Planejados* em uma única tela *Contas*, com abas (*Todas | Recorrentes | Parceladas*) e filtros em bottom sheet.
  - Criação do hub *Mais* para Contatos, Cadastros de apoio, Configurações e Backup.
- **Tela "Hoje" Racionalizada (Prompt 31)**:
  - Hero único com valor restante do mês, barra de progresso e linha descritiva de status.
  - Seção "Precisa de atenção" sempre prioritária (contas atrasadas, vencendo hoje e em até 7 dias) com estado positivo compacto ("Tudo em dia").
  - "Próximos pagamentos" com limite de 5 itens e atalho "Ver todas" para Contas; contas pagas colapsadas por padrão.
  - Toggle ágil entre *Lista* e *Calendário* no topo com auto-seleção inteligente do próximo dia com pendências.
  - KPIs dispostos em grid 2x2 harmonioso sem corte horizontal.
- **Nova Despesa & Lançamento Rápido com FAB (Prompt 32)**:
  - Valor no topo em tipografia grande com foco imediato no teclado numérico.
  - Vencimento posicionado com chips de atalho (*Hoje, Amanhã, Dia 5, Outro*).
  - Contato/Fornecedor opcional; tipo de compromisso em abas segmentadas; meio de pagamento recolhido por padrão.
  - FAB contextual na tela inicial abrindo o `QuickExpenseBottomSheet` para lançamento ágil (Valor, Item, toggle Já Paga), com opção de "Mais detalhes" para o formulário completo.
  - Suporte a Desfazer lançamento (`UndoSaveBill`) via Snackbar com reversão imediata.
- **Humanização do Glossário e Tom de Voz (Prompt 33)**:
  - Substituição sistemática de jargões corporativos por termos simples e humanos via `AppStrings` ("Radar de Desembolso" → "Próximos pagamentos", "Rigidez Orçamentária" → "Equilíbrio do orçamento", "Cockpit" → "Sugestões", etc.).
- **Estatísticas Enxutas com 1 Insight + 4 Cards (Prompt 34)**:
  - Cada aba de estatísticas (Passado, Presente, Futuro) exibe 1 frase de insight + até 4 cards principais; itens secundários organizados sob botão expansível "Ver mais análises".
- **Contatos e Itens Simplificados (Prompt 35)**:
  - Linha da lista de contatos simplificada com subtítulo único legível (`tipo · total em aberto`), reservando detalhes cadastrais para o perfil.
  - Itens de despesa com ocultação de chip de natureza redundante quando igual à da categoria.
- **Configurações Agrupadas (Prompt 36)**:
  - Agrupamento em 4 blocos semânticos (Cadastros, Aparência, Backup e Sobre) e banner de status do último backup.
- **Registro em ADR 034**:
  - Documentação completa da nova arquitetura de informação e padrões visuais no ADR 034.

---

## [1.23.0] - 2026-10-09

### 🔐 Fortalecimento Criptográfico de Backups (AES-256-GCM v2, AAD, OWASP 600k e Indicador de Força)

- **PBKDF2-HMAC-SHA256 Atualizado para 600.000 Iterações (v2)**:
  - Adotada a recomendação atual da OWASP para KDF PBKDF2 com 600.000 iterações em novos backups (`EncryptedBackupDto.CURRENT_VERSION = 2`).
  - Suporte total à leitura e restauração de arquivos v1 legados utilizando o valor registrado no campo `iterations` (ex: 65.536).
- **Autenticação de Cabeçalho via AAD (`cipher.updateAAD`)**:
  - O cabeçalho estrutural (`format`, `version`, `iterations`) passa a ser autenticado criptograficamente como Dados Adicionais Associados (AAD) via `cipher.updateAAD` em envelopes v2.
  - Qualquer adulteração em `format`, `version` ou `iterations` resulta em rejeição imediata com falha de autenticação GCM (`AEADBadTagException` / `SecurityException`).
- **Proteção Contra Ataques DoS no Restore (Limites de Iterações)**:
  - Validação estrita limitando o número de iterações entre `10_000` e `2_000_000`. Arquivos forjados com valores abusivos são rejeitados antes da derivação da chave.
- **Segurança e Ergonomia na UI (Mínimo de 8 Caracteres & Indicador de Força)**:
  - Atualizada a exigência mínima de senha para 8 caracteres, mitigando riscos de força bruta offline associados a PINs curtos.
  - Ajustados todos os diálogos de exportação e restauração (`CreateBackupPasswordDialog`, `RestorePasswordDialog`), removendo menções a "PIN".
  - Implementado componente `PasswordStrengthIndicator` com feedback visual de 4 níveis de força da senha em conformidade estrita com o Material 3 e tokens de tema.
- **Suíte de Testes Automatizados de Segurança**:
  - Testes unitários cobrindo rejeição de senha incorreta, detecção de ciphertext adulterado, detecção de cabeçalho adulterado (versão, iterações e formato), validação de limites de iterações e compatibilidade de restauração com arquivos v1 legados sem AAD.

---

## [1.22.0] - 2026-10-08

### 🛡️ Exportação de Esquemas Room, Correção de Cascata em FKs (14→15) e Testes Instrumentados com MigrationTestHelper

- **Ativação e Versionamento de Esquemas Room (`exportSchema = true`)**:
  - Habilitado `exportSchema = true` na anotação `@Database` de `PlatformDatabase`.
  - Configurado argumento KSP `room.schemaLocation = "$projectDir/schemas"` e inclusão nos `sourceSets.assets` em `app/build.gradle.kts`.
  - Versionados os arquivos de esquema JSON (9.json a 17.json) em `app/schemas/com.platform.app.data.local.PlatformDatabase/`.
- **Correção da Migração 14→15 (`PlatformDatabase.MIGRATION_14_15`)**:
  - Eliminados os comandos ineficazes `PRAGMA foreign_keys = OFF` e `PRAGMA foreign_keys = ON`, que não possuem efeito dentro de transações de migração do SQLite gerenciadas pelo Room.
  - Adicionado `PRAGMA defer_foreign_keys = ON` para diferir validações de integridade até o commit da transação.
  - Invertida e ordenada a recriação das tabelas com chave estrangeira: tabelas-filhas (`credit_card_invoices_new`, `expense_items_new`) são preparadas e populadas com dados antes de qualquer alteração nas tabelas-pai (`credit_cards`, `categories`).
  - Drop ordenado (filha antes da pai) e renomeação subsequente, eliminando o risco de `DROP TABLE credit_cards` disparar `ON DELETE CASCADE` acidental e apagar as faturas de cartão de crédito.
- **Testes Instrumentados com `MigrationTestHelper` (`PlatformDatabaseMigrationAndroidTest`)**:
  - Adicionada dependência `androidx.room:room-testing` em `androidTestImplementation` e `testImplementation`.
  - Criado teste instrumentado cobrindo o encadeamento 9→10 até 14→15 (e 9→17 ponta a ponta) sobre banco de dados previamente populado com dados relacionais complexos (cartões, faturas, despesas parceladas vinculadas, contas e orçamentos).
  - Verificação e garantia de invariância nas contagens de linhas antes e depois da migração (especialmente `credit_cards`, `credit_card_invoices` e `bill_installments`).
  - Execução e asserção de `PRAGMA foreign_key_check` retornando estritamente zero violações, atestando integridade referencial absoluta.

---

## [1.21.0] - 2026-10-08

### 🔒 Concorrência Thread-Safe com Mutex, Ponto Único de Disparo e Índice Único de Parcelas

- **Use Case Singleton com Proteção Atômica de Mutex (`ExtendRecurringBillsUseCase`)**:
  - Anotação `@Singleton` aplicada ao `ExtendRecurringBillsUseCase`.
  - Todo o ciclo de extensão (`ler -> decidir -> calcular -> inserir`) foi envolvido em `Mutex.withLock`, impedindo condições de corrida onde chamadas simultâneas leem o mesmo estado e geram parcelas em duplicidade.
- **Centralização em Ponto Único de Disparo (`PlatformApplication`)**:
  - A execução de `ExtendRecurringBillsUseCase` foi centralizada exclusivamente no ciclo de vida de inicialização da aplicação (`PlatformApplication.onCreate()`) dentro do `applicationScope`.
  - Remoção completa das 5 chamadas concorrentes dispersas: `MainActivity`, `DueReminderReceiver`, `BillsViewModel`, `RecurringInstallmentsViewModel` e `DashboardViewModel`.
- **Evolução de Schema e Migração Room v17 (`PlatformDatabase.MIGRATION_16_17`)**:
  - Limpeza de dados legados no SQLite: remoção de duplicatas históricas mantendo a ocorrência mais antiga por `(billId, installmentNumber)` e preservando estritamente as já pagas (`status = 'PAID'` ou `paidAt IS NOT NULL`).
  - Criação de `UNIQUE INDEX index_bill_installments_billId_installmentNumber ON bill_installments(billId, installmentNumber)`.
  - Atualização de `BillInstallmentDao`: `insertAll` e novo `insertAllIgnore` configurados com `OnConflictStrategy.IGNORE`, protegendo o banco contra sobreposições concorrentes.
  - Recálculo de `totalInstallments` em `bills` após saneamento de duplicatas.
- **Suíte de Testes Automatizados**:
  - `ExtendRecurringBillsUseCaseTest`: novo teste com chamadas assíncronas concorrentes (`async(Dispatchers.Default)`), comprovando que invocações simultâneas produzem exatamente o conjunto correto de parcelas sem nenhuma duplicata de `installmentNumber`.
  - `PlatformDatabaseMigrationTest`: validação de schema da migração 16 -> 17, conferindo a deleção de duplicatas, recálculo de totais e criação do índice único.
  - 100% dos testes unitários verdes em todo o projeto.

---

## [1.20.0] - 2026-10-08

### 💳 Materialização Sob Demanda de Faturas e Ancoragem Estável de Recorrências no Cartão

- **Materialização Sob Demanda de Faturas para Assinaturas no Cartão (`FinancialRepositoryImpl`)**:
  - Implementada vinculação sob demanda em `getOrCreateInvoiceForMonth`: ao abrir ou consultar qualquer fatura mensal de um cartão, o repositório busca ocorrências `RECURRING` pendentes (`invoiceId IS NULL`) cujo ciclo pertença à competência calculada por `CreditCardCalculator.determineInvoiceReferenceMonth` e as anexa atomicamente à fatura (`invoiceId = invoice.id`, `dueDate = invoice.dueDate`).
  - Nova operação `materializeRecurringCardInvoices(referenceTimeMillis)` no `FinancialRepository`: varre os cartões ativos e materializa preventivamente as faturas e seus vínculos recorrentes para todas as competências até o mês corrente.
  - Integração nos pontos-chave do ciclo de vida: rotina diária de lembretes (`DueReminderReceiver`), expansão de horizontes recorrentes (`ExtendRecurringBillsUseCase`) e seleção/abertura de faturas na interface (`CreditCardsViewModel`).
- **Desacoplamento e Ancoragem Estável de Ocorrências (`CalculateInstallmentsUseCase`)**:
  - `generateNextRecurringInstallments` agora define explicitamente `invoiceId = null` para parcelas futuras de cartão, eliminando o defeito onde ocorrências estendidas (mês 13+) herdavam a 1ª fatura (`bill.invoiceId`), muitas vezes já liquidada.
  - Ancoragem temporal fixada em `bill.recurrenceAnchorDate` (data do ciclo original da compra) em vez de `firstInstallment.dueDate` (que no cartão assume a data de vencimento da fatura). Evita desvio cumulativo do dia de fechamento/competência em horizontes estendidos.
  - Propagação de `recurrenceAnchorDate` e `creditCardId` em `CreateBillUseCase` e `NewExpenseViewModel`.
- **Evolução do Schema e Migração Room v16 (`PlatformDatabase.MIGRATION_15_16`)**:
  - Adicionadas colunas `recurrenceAnchorDate INTEGER DEFAULT NULL` e `creditCardId TEXT DEFAULT NULL` na tabela `bills`.
  - Script de migração com backfill inteligente: deriva `creditCardId` a partir dos vínculos existentes em `credit_card_invoices` e reconstitui `recurrenceAnchorDate` com base na primeira parcela de cada despesa recorrente.
  - Queries otimizadas em `BillInstallmentDao`: `getUnattachedRecurringInstallmentsForCard` e `attachInstallmentsToInvoice`.
- **Nova Suíte de Testes Automatizados**:
  - `RecurringCardSubscription14MonthsTest`: teste completo simulando assinatura contínua por 14 meses no cartão (Nubank: corte 20, vencimento 27, compra dia 10). Valida exatamente 1 ocorrência por fatura mensal, nenhuma ocorrência estendida apontando para a 1ª fatura e dia de vencimento perfeitamente estável.
  - `FinancialRepositoryInvoiceTest`: novos testes unitários para o anexo sob demanda de parcelas e materialização preventiva.
  - `CalculateInstallmentsUseCaseTest`: validação de `invoiceId = null` e preservação estrita de `recurrenceAnchorDate`.
  - `PlatformDatabaseMigrationTest`: validação da integridade estrutural e de dados da migração 15 -> 16.

---

## [1.19.0] - 2026-10-07

### 🎨 Eliminação de Cores Literais Residuais e Guard Rail Automatizado no Build

- **Tokenização da Paleta de Gráficos (`PlatformColorPalette`)**:
  - Centralização dos tokens semânticos de visualização de dados `chartSeries1` a `chartSeries7` e da lista imutável `chartPalette` em `PlatformColorPalette.kt`.
  - Atualização de `PaymentMethodDistributionCard.kt` (`StatisticsScreen`) para consumir exclusivamente `PlatformColorPalette.chartPalette`.
- **Exceções Arquiteturais Centralizadas e Documentadas (`DesignExceptionColors.kt`)**:
  - `ThemePreviewColors`: Isolação das 18 cores nominais das miniaturas de temas (Classic, Modern V2, Emerald, Obsidian). Documentado formalmente por que essas miniaturas precisam ser estáticas (para representar com precisão a identidade do tema de destino independentemente do modo ativo no app).
  - `CardSkinColors`: Agrupamento das 15 cores e gradações do acabamento físico do cartão (`PlatformCreditCardView`: gradiente dark, chip EMV metálico ouro/bronze, tipografia de alto contraste e ações rápidas).
- **Correção de Cores Literais Soltas em Telas e Componentes**:
  - `BillInstallmentItemCard.kt`: Substituído `Color.White` no ícone de checkmark de pagamento por `MaterialTheme.colorScheme.surface`.
  - `EditInstallmentBottomSheet.kt`: Substituído `Color(0xFF64748B)` no fallback de cor por `MaterialTheme.colorScheme.onSurfaceVariant`.
  - `NatureDistributionCard.kt`: Substituído `Color(0xFF10B981)` por token semântico `SuccessGreen`.
  - `ContactsScreen.kt`: Substituídas cores literais de tipos de contatos por `WarningAmber` e `MaterialTheme.colorScheme.tertiary`.
  - `ExpenseItemsScreen.kt`: Substituído `Color.Gray` de fallback de categorias por `BrandPrimary`.
  - `PlatformColorPicker.kt`: Substituídas cores de borda e seleção por tokens dinâmicos (`MaterialTheme.colorScheme.onSurface` e `scrim`).
- **Guard Rail Automatizado contra Novas Cores Literais (`checkLiteralColors`)**:
  - Nova task Gradle `:app:checkLiteralColors` vinculada ao ciclo de compilação (`preBuild`). Qualquer tentativa de adicionar `Color(0x...)`, `Color.White`, `Color.Black` ou cores estáticas fora de `presentation/theme` interrompe imediatamente o build com erro descritivo.
  - Scripts standalone multiplataforma adicionados: `scripts/check-literal-colors.ps1` (PowerShell) e `scripts/check-literal-colors.sh` (Bash).
- **Validação Completa**:
  - Execução de `:app:checkLiteralColors` e suíte completa de testes unitários (`BUILD SUCCESSFUL`). Zero violações detectadas em todo o pacote de apresentação.

---

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

### 💳 Regra Única de Limite de Cartão de Crédito e Faturas Sob Demanda

- **Criação do `GetCreditCardSummariesUseCase`**:
  - Centralizado o cálculo do limite consumido e limite disponível de cartões de crédito em uma única fonte da verdade.
  - Implementada a **Regra Canônica de Limite**:
    - **`INSTALLMENT` (Parcelados)**: Consome o saldo devedor restante de todas as parcelas não pagas (atuais e futuras contratadas).
    - **`RECURRING` (Assinaturas) e `SINGLE` (À vista)**: Consomem exclusivamente o que pertence à fatura aberta ou fechada atual não paga, eliminando o erro de projeção que consumia antecipadamente o limite de até 12 meses futuros de assinaturas.
- **Eliminação de Cálculos Duplicados nas ViewModels**:
  - `CreditCardsViewModel` e `NewExpenseViewModel` agora consomem diretamente o Flow reativo de `GetCreditCardSummariesUseCase`.
  - Corrigido vazamento na `NewExpenseViewModel`, que somava parcelas de todos os cartões indistintamente sem isolamento por `cardId`.
- **Faturas Recorrentes Sob Demanda**:
  - `CreateBillUseCase` atualizado para não mais pré-criar 11 faturas vazias antecipadas no SQLite para compras do tipo `RECURRING`. Apenas a 1ª ocorrência do ciclo inicial é atrelada de imediato à fatura atual, preservando as projeções futuras de contas a pagar e gerando faturas sob demanda à medida que os ciclos chegam.
- **Suíte de Testes Automatizados**:
  - Criado `GetCreditCardSummariesUseCaseTest` cobrindo detalhadamente o cenário de um cartão com 1 compra parcelada em 10x, 1 assinatura mensal e 1 compra à vista, além da liberação proporcional de limite após pagamento de fatura.
  - Atualizados `CreateBillUseCaseTest` e `NewExpenseViewModelTest`.
  - 100% dos testes unitários (150 testes) aprovados via Gradle.

---

## [1.15.0] - 2026-10-07

### 🏛️ Unificação da Geração de Parcelas no `CreateBillUseCase` e Paridade entre Cartão e Contas Avulsas

- **Extensão do `CreateBillUseCase` com Suporte a Cartão de Crédito**:
  - `CreateBillUseCase` agora recebe opcionalmente a entidade `CreditCard?`, `isFirstInstallmentPaid: Boolean` e `actualPaymentDate: Long?`.
  - Reutiliza integralmente `CalculateInstallmentsUseCase` para o cálculo canônico de contagem de parcelas, divisão exata de centavos com resto na 1ª parcela e datas-base de ocorrência.
  - Quando a despesa for no cartão (`creditCard != null`), mapeia cada ocorrência para sua respectiva fatura usando `CreditCardCalculator.determineInvoiceReferenceMonth()` e `repository.getOrCreateInvoiceForMonth()`, vinculando `invoiceId` e ajustando o `dueDate` da parcela para o vencimento da fatura.
  - Atualiza atomicamente a `bill` vinculando o `invoiceId` inicial e persistindo conta e parcelas em transação Room.
- **Eliminação de Código Duplicado Inline na `NewExpenseViewModel`**:
  - Removido bloco redundante de ~150 linhas em `NewExpenseViewModel.saveExpense()` que recalculava parcelas, datas, resto de centavos e faturas inline.
  - Resolvida a divergência de contagem de parcelas (`coerceAtLeast(2)` vs `coerceAtLeast(1)`), restabelecendo o alinhamento estrito com as fronteiras de Clean Architecture.
- **Testes Automatizados de Paridade**:
  - Criado `CreateBillUseCaseTest` validando a paridade absoluta entre os fluxos com e sem cartão:
    - Mesma divisão exata de centavos e mesmo valor individual por parcela.
    - Alocação consistente do resto de centavos na 1ª parcela.
    - Mesmas datas-base de ocorrência utilizadas para geração das faturas correspondentes.
    - Paridade para parcelamentos (`INSTALLMENT`), despesas à vista (`SINGLE`) e recorrências (`RECURRING`).
    - Tratamento uniforme de status e timestamp de quitação quando a despesa já é criada como paga.
  - Atualizado `NewExpenseViewModelTest` injetando `CreateBillUseCase`.
  - 100% dos testes unitários validados via Gradle.

---

## [1.14.0] - 2026-10-07

### 💳 Remoção do Seed Fictício de Cartões e Centralização de Seeds no Primeiro Uso

- **Remoção de Dados Fictícios de Cartões de Crédito**:
  - Excluído o seed automático que criava "Cartão Principal" e "Cartão Secundário" quando a tabela estava vazia.
  - Implementado empty state refinado em `CreditCardsScreen` com botão de Call-To-Action (CTA): `"Cadastrar primeiro cartão"`.
  - O app agora respeita quando o usuário exclui todos os cartões, nunca mais recriando dados fictícios sem autorização.
- **Centralização da Inicialização no Primeiro Uso (Startup)**:
  - Criado o caso de uso `SeedInitialDataUseCase` e centralizado o bootstrap de entidades de referência (Categorias, Itens, Contatos, Contas Financeiras e Formas de Pagamento) em `PlatformApplication.onCreate()`.
  - Controle de primeiro uso governado via flag persistente `seeds_applied` no `PreferencesManager` (Jetpack DataStore) em vez de checagens repetitivas de "tabela vazia".
  - Se o usuário excluir conscientemente todos os registros de qualquer entidade (categorias, contatos, etc.), eles não são recriados ao abrir o app.
- **Desacoplamento e Limpeza nos ViewModels**:
  - Removidas chamadas de seed dos blocos `init` de 7 ViewModels (`HomeViewModel`, `BillsViewModel`, `NewExpenseViewModel`, `ManagementViewModel`, `ExpenseItemsViewModel`, `CreditCardsViewModel` e `DashboardViewModel`).
  - Redução drástica de overhead de I/O de banco e checagens concorrentes desnecessárias na navegação entre telas.
- **Testes Automatizados & Governança**:
  - Criado `FinancialRepositoryImplSeedTest` validando a garantia de resíduo zero: mesmo com todas as tabelas vazias, uma vez que a flag `seeds_applied` está ativa, nenhuma entidade é recriada.
  - Atualizado `ExpenseItemsViewModelTest` com foco no carregamento de dados.
  - Registrado [ADR 029](.ai/DECISIONS/029-remocao-seed-cartoes-e-centralizacao-startup-seeds.md).

---

## [1.13.0] - 2026-10-07

### 🎯 DAO Dedicado para Goal Contributions e Recálculo Derivado de Aportes

- **DAO Próprio (`GoalContributionDao`)**:
  - Criada a interface dedicada `GoalContributionDao` com consultas especializadas para inserção, soma por meta (`sumByGoal`), soma por período (`sumForPeriod`), listagem por meta e período (`getByGoalForPeriod`), e deleção vinculada.
  - Registrado em `PlatformDatabase` e provido como Singleton no container Hilt em `AppModule.kt`.
- **Registro Datado & Recálculo do Cache de Saldo**:
  - `GoalRepository.addContribution`: insere atomicamente a contribuição datada no banco de dados e recalcula o campo cache `currentAmountCents` da meta a partir da soma real de todas as suas contribuições (`sumByGoal`), eliminando riscos de inconsistência contábil.
  - `saveGoal`: metas criadas com valor inicial geram automaticamente o primeiro registro histórico de contribuição com a data de criação da meta.
- **Consulta Granular por Meta e Período**:
  - Adicionado o método `getContributionsForPeriod(goalId, startDate, endDate)` em `GoalRepository` e `GoalRepositoryImpl`, viabilizando apuração por meta em intervalos de datas e histórico de aportes.
- **Testes Automatizados**:
  - Criado `GoalRepositoryImplTest` com cobertura completa de múltiplos aportes em meses distintos, filtros temporais e recálculo do cache.
- **Decisão Arquitetural Documentada**:
  - Registrada [ADR 028](.ai/DECISIONS/028-dao-dedicado-goal-contributions-e-recalculo-aportes.md).

---

## [1.12.0] - 2026-10-07

### 🧹 Remoção de `syncStatus` Residual, Resíduo Zero e Migração Room v15

- **Expurgo de Resíduo Morto e Preparação para Fase 11**:
  - Removido o campo legado `syncStatus: String = "PENDENTE"` de todos os modelos de domínio (`Category`, `ExpenseItem`, `CreditCard`, `CreditCardInvoice`) e entidades Room (`CategoryEntity`, `ExpenseItemEntity`, `CreditCardEntity`, `CreditCardInvoiceEntity`).
  - O campo consistia em uma tentativa histórica e fragmentada de sincronização sem qualquer consumo na lógica do aplicativo, que gerava assimetrias em relação às entidades principais (`Bill`, `BillInstallment`, `Contact`, `FinancialAccount`, `Goal`, `Budget`).
  - Adotada formalmente a decisão arquitetural de conceber a sincronização bidirecional na nuvem (Fase 11) do zero, com fila de mutações transacional (Outbox), controle de exclusão (Tombstones) e detecção de conflitos, em vez de flags estáticas pontuais.
- **Migração Física no SQLite (`PlatformDatabase` v15)**:
  - Incrementada a versão do banco de dados Room para `version = 15`.
  - Implementada e registrada a migração atômica `MIGRATION_14_15` com recriação segura de tabelas (`categories`, `expense_items`, `credit_cards`, `credit_card_invoices`) sob `PRAGMA foreign_keys = OFF / ON`, garantindo compatibilidade universal com qualquer versão do SQLite nativo.
- **Higiene e Compactação de Backups**:
  - Arquivos de backup JSON locais protegidos por senha agora são gerados sem chaves `syncStatus` redundantes.
- **Testes Automatizados & Governança**:
  - Atualizado `PlatformDatabaseMigrationTest` validando a execução estrutural da migração `14 -> 15`.
  - Registrada a decisão técnica no [ADR 027](.ai/DECISIONS/027-remocao-sync-status-residuo-zero-preparacao-fase-11.md).

---

## [1.11.1] - 2026-10-07

### 🧹 Limpeza de Typealias Obsoleto (`AppDatabase`)

- **Remoção de Código Morto Residual**:
  - Excluído o arquivo `AppDatabase.kt` contendo `typealias AppDatabase = PlatformDatabase`.
  - O alias de compatibilidade foi introduzido durante o rename inicial da aplicação e não possuía nenhum consumidor em nenhuma camada do projeto.
  - Eliminação definitiva de ambiguidades conceituais em favor do canônico `PlatformDatabase`.

---

## [1.11.0] - 2026-10-07

### 🧹 Remoção da Tabela Legada `transactions`, Resíduo Zero e Migração Room v14

- **Princípio do Resíduo Zero & Limpeza de Código Legado**:
  - Removidos completamente os artefatos `TransactionEntity.kt` e `TransactionDao.kt`, eliminando classes residuais anteriores à consolidação do modelo unificado de Contas e Parcelas (`bills` / `bill_installments`).
  - Removido o provider de injeção de dependência `@Provides @Singleton fun provideTransactionDao(...)` em `AppModule.kt`.
  - Removido o campo `transactions` e sua serialização/deserialização em `BackupDataDto.kt` e `BackupRepositoryImpl.kt`, eliminando nós vazios nos arquivos de backup JSON exportados.
- **Migração Física no SQLite (`PlatformDatabase` v14)**:
  - Incrementada a versão do banco de dados Room para `version = 14`.
  - Criada e registrada a migração `MIGRATION_13_14` executando `DROP TABLE IF EXISTS transactions`, limpando tabelas, índices e chaves estrangeiras obsoletas do SQLite dos dispositivos dos usuários.
- **Testes e Garantia de Qualidade**:
  - Criado `PlatformDatabaseMigrationTest` validando a execução do comando `DROP TABLE IF EXISTS transactions` na migração `13 -> 14`.
  - Atualizado `BackupRepositoryImplTest` para operação sem dependências de transações legadas.
  - 100% dos testes unitários da aplicação aprovados.
- **Decisão Arquitetural Documentada**:
  - Registrada [ADR 026](.ai/DECISIONS/026-remocao-tabela-transactions-e-migracao-room-v14.md) documentando a política de Resíduo Zero e a migração.

---

## [1.10.0] - 2026-10-07

### 🌐 Centralização de Strings de Interface (UI) e Fonte Única da Verdade (`AppStrings`)

- **Fonte Única da Verdade (`AppStrings.kt`)**:
  - Criado o objeto estruturado `AppStrings` em `com.platform.app.presentation.common` agrupando constantes textuais de interface de usuário sem overhead de contexto Android.
  - Sub-namespaces organizados: `Status`, `Nature`, `AccountType`, `ContactType`, `BillType`, `Actions`, `Home`, `Dialogs`.
  - Sincronização espelhada em `res/values/strings.xml` para paridade com recursos nativos do framework Android e suporte nativo a internacionalização futura.
- **Refatoração 1:1 Sem Alterações de Texto**:
  - Substituição de termos literais repetidos na camada `presentation/` preservando rigorosamente 1:1 todos os caracteres originais:
    - **Status**: "Pendente", "Pago", "Paga", "Já Paga", "A Pagar", "Vencida", "Vencido", "Atrasado", "Liquidado", "Tudo quitado", "100% quitado", "Aberta", "Fechada", "Pausada".
    - **Natureza do Gasto**: "Obrigatório", "Necessário", "Deseja", "Nenhum", "Poupança", "Natureza do Gasto", rótulos e descrições conceituais.
    - **Tipos de Conta**: "Conta Corrente", "Carteira / Dinheiro", "Poupança", "Investimento".
    - **Tipos de Contato**: "Pessoa Física", "Fornecedor", "Órgão Público", "Pessoa", "Empresa", "Público", "Salvar Contato", "Atualizar Contato", "Excluir Contato".
    - **Ações Comuns**: "Salvar", "Salvar Alterações", "Atualizar", "Cancelar", "Excluir", "Confirmar", "Voltar", "Filtrar", "Todos".
    - **Dashboard**: "Restante a Pagar no Mês", "Total vencido:".
- **Telas e Componentes Atualizados**:
  - `HomeScreen`: status badges ("Tudo quitado", "100% quitado", "Pago"), "Total vencido:", "Restante a Pagar no Mês".
  - `BillsScreen`: status de pagamento ("Pago"), ações de exclusão em lote ("Excluir", "Cancelar").
  - `NewExpenseScreen`: status chips ("Pendente", "Já Paga"), botões de diálogo rápido ("Salvar", "Cancelar").
  - `EditInstallmentBottomSheet`: títulos e ações de confirmação/exclusão ("Salvar Alterações", "Excluir Registro", "Excluir", "Cancelar").
  - `ConfirmPaymentDialog`: ações de confirmação ("Confirmar", "Cancelar").
  - `ContactsScreen` & `ContactDetailScreen`: chips de tipo ("Pessoa", "Empresa", "Público"), badges, filtros agregados ("Todos", "Pessoa Física", "Fornecedor", "Órgão Público"), botões de ação e exclusão.
  - `ManagementScreen`: abas e cards de gestão com status ("Pendente", "Liquidado", "Pago"), "Natureza do Gasto:", diálogos de contas, métodos e categorias ("Salvar", "Atualizar", "Cancelar", "Excluir").
  - `StatisticsScreen`: breakdown de status do mês ("Pago", "Pendente", "Atrasado").
  - `CreditCardsScreen`: badges de fatura ("Aberta", "Fechada", "Paga"), diálogo de exclusão de cartão ("Cancelar", "Excluir").
  - `ExpenseItemsScreen`: chips e descrições de natureza, botões do modal ("Salvar", "Salvar Alterações", "Cancelar", "Excluir").
  - `GoalsScreen` & `BudgetsScreen`: botões de salvamento, edição, cancelamento e exclusão.
  - `RecurringInstallmentsScreen` & `AdjustInstallmentDialog`: rótulos de status ("Pago"), diálogos de cancelamento/exclusão/pausa ("Excluir", "Cancelar", "Voltar").
  - `SettingsScreen`: diálogos de backup criptografado e restauração ("Cancelar").
- **Testes Automatizados**:
  - Criado `AppStringsTest` garantindo que todos os namespaces e constantes de interface possuam os valores contratuais esperados e previnam regressões acidentais.
  - Suíte completa de 125+ testes unitários executada com 100% de sucesso (`BUILD SUCCESSFUL`).
- **Segurança para Refatorações Futuras**:
  - Quaisquer futuras renomeações conceituais (ex: "Pendente" -> "A Pagar", "Vencida" -> "Em Atraso") agora são realizadas de forma pontual e atômica em um único arquivo, com zero risco de divergência entre telas.

---

## [1.9.0] - 2026-10-07

### 🔒 Criptografia AES-256-GCM para Backups com Proteção por Senha/PIN

- **Criptografia Simétrica Forte (AEAD AES-256-GCM)**:
  - Implementada criptografia simétrica autenticada (`AES/GCM/NoPadding`) de 256 bits via `BackupCryptoHelper`, eliminando o risco de vazamento de dados em texto plano em backups locais e compartilhamentos via ShareSheet (WhatsApp, E-mail, etc.).
  - Derivação de chaves via `PBKDF2WithHmacSHA256` com 65.536 iterações, sal criptográfico aleatório de 16 bytes e IV aleatório de 12 bytes gerados via `SecureRandom`.
  - Descarte defensivo imediato da senha em memória RAM (`clearPassword()` e preenchimento com zeros).
- **Envelope Criptográfico Seguro (`EncryptedBackupDto`)**:
  - Dados exportados em envelope versionado contendo parâmetros do KDF, vetor de inicialização e o criptograma em Base64, protegendo 100% dos dados financeiros contra inspeção não autorizada.
- **Contratos e Casos de Uso com Senha**:
  - `BackupRepository`: atualizado para exigir senha em `exportBackupJson(password)` e `restoreBackupFromJson(backupJson, password)`.
  - `ExportBackupUseCase` e `RestoreBackupUseCase`: validação mandatória de preenchimento e integridade.
- **Interface e Experiência do Usuário (UI/UX)**:
  - `SettingsScreen`: diálogos dedicados `CreateBackupPasswordDialog` (criação e confirmação de senha de no mínimo 4 caracteres com alertas visuais) e `RestorePasswordDialog` (solicitação de senha para descriptografia e restauração).
  - Tratamento de erro detalhado informando explicitamente quando a senha/PIN fornecido estiver incorreto.
- **Testes Automatizados**:
  - `BackupCryptoHelperTest`: validação de ciclo completo de criptografia/descriptografia, confidencialidade do ciphertext contra vazamento de tokens, integridade contra adulteração e rejeição de senhas incorretas.
  - `BackupRepositoryImplTest`: validação do ciclo exportação -> restauração com senha correta e rejeição atômica com senha incorreta.
  - `SettingsViewModelTest`: testes atualizados com a nova API protegida por senha.
- **Governança & Arquitetura**:
  - Registrada a [ADR 025: Criptografia Simétrica AES-256-GCM para Backups com Proteção por Senha/PIN](.ai/DECISIONS/025-criptografia-aes-gcm-backups-protegidos-por-senha.md).

---

## [1.8.0] - 2026-10-07

### 💳 Tipificação de Contas Financeiras (`FinancialAccountType`) & Migração Room v13

- **Enum de Domínio Puro (`FinancialAccountType`)**:
  - Criado o enum `FinancialAccountType` (`CORRENTE`, `CARTEIRA`, `POUPANCA`, `INVESTIMENTO`) com rótulos amigáveis (`displayName`) e resolução de aliases legados (`fromString`).
  - Substituído o campo `accountType: String` livre em `FinancialAccount` pelo enum tipado.
- **Evolução de Persistência no Room (`PlatformDatabase` v13)**:
  - Criado `FinancialAccountTypeConverter` para serialização e deserialização no banco SQLite.
  - Implementada a migração `MIGRATION_12_13`, normalizando strings legadas (`CHECKING`, `CASH`, `SAVINGS`, `Conta Corrente`, etc.) para os nomes canônicos do enum.
  - Registrada a migração no provider do `AppModule`.
- **Mapeamento em Entidades e Casos de Uso**:
  - `FinancialAccountEntity` atualizado com o enum e mapeamento bidirecional `toDomain()` / `fromDomain()`.
  - Métrica de dashboard `AccountSpend` atualizada com o campo `accountType: FinancialAccountType`, mapeando rótulo e tipo via `GetFinancialDashboardUseCase`.
- **Interface e Experiência do Usuário (UI/UX)**:
  - `ManagementScreen`: badges e listas de detalhe exibem `account.accountType.displayName`. Diálogo de criação e edição (`AddEditAccountDialog`) utiliza dropdown baseado em `FinancialAccountType.entries` com ícones temáticos contextuais.
  - `StatisticsScreen`: card `AccountsDistributionCard` exibe ícones representativos (`AccountBalance`, `Payments`, `Savings`, `TrendingUp`) e subtítulo informativo com o tipo da conta.
- **Testes Automatizados**:
  - Novos testes unitários em `FinancialAccountTypeTest` cobrindo conversão de aliases, display names, fallbacks, `FinancialAccountTypeConverter` e `FinancialAccountEntity`.
  - Atualização dos testes em `ManagementViewModelTest`.
- **Governança & Arquitetura**:
  - Registrada a [ADR 024: Enum FinancialAccountType e Migração Room v13](.ai/DECISIONS/024-enum-financial-account-type-e-migracao-room.md).

---

## [1.7.0] - 2026-10-07

### 👥 Tipificação de Contatos (`ContactType`) & Migração Room v12

- **Enum de Domínio Puro (`ContactType`)**:
  - Introduzido o enum `ContactType` (`PESSOA_FISICA`, `FORNECEDOR`, `ORGAO_PUBLICO`) com método utilitário resiliente `fromString()`.
  - Atualizado o modelo `Contact` para conter `val type: ContactType = ContactType.FORNECEDOR`.
- **Evolução de Persistência no Room (`PlatformDatabase` v12)**:
  - Adicionada a coluna `type TEXT NOT NULL DEFAULT 'FORNECEDOR'` na tabela `contacts` via migração `MIGRATION_11_12`.
  - Registrada a migração no `AppModule` preservando a integridade dos dados existentes.
- **Filtros e Agrupamento na Tela de Contatos (`ContactsScreen`)**:
  - Adicionada barra de chips de filtro superior (`ContactTypeFilterRow`) com contadores dinâmicos para "Todos", "Pessoa Física", "Fornecedor" e "Órgão Público".
  - Agrupamento visual automático por seções temáticas (`ContactSectionHeader`) quando a visualização estiver em "Todos".
  - Filtro exclusivo e direto ao selecionar um tipo específico.
- **Indicadores Visuais de Tipo de Contato (`ContactTypeBadge` & `PlatformAvatar`)**:
  - Chip temático com ícone dedicado (`Person`, `Business`, `AccountBalance`) e paleta semântica em cada card de contato.
  - Avatar colorido dinamicamente com a identidade visual do tipo.
  - Integração do badge no cabeçalho da tela de detalhes (`ContactDetailScreen`).
- **Seletor de Tipo no Formulário (`AddContactBottomSheet`)**:
  - Seletor ergonômico em cartões para escolha rápida do tipo de contato durante criação ou edição.
- **Testes Automatizados**:
  - Novos testes unitários em `ContactsViewModelTest` cobrindo filtragem por cada tipo, restauração e busca textual combinada com filtro.
- **Governança & Arquitetura**:
  - Criada a [ADR 023: Tipificação de Contatos (ContactType) e Migração Room v12](.ai/DECISIONS/023-tipificacao-contatos-e-migracao-room.md).

---

## [1.6.1] - 2026-10-07

### 🎨 Semântica e Hierarquia Visual na Visão Mensal (`HomeScreen`)

- **Rótulo Semântico "Restante a Pagar no Mês"**:
  - Renomeado de "Total Previsto no Mês" para "Restante a Pagar no Mês" no card executivo `ForecastImpactCard`, eliminando a ambiguidade com o total orçado e deixando claro que o montante destacado reflete apenas o saldo ainda em aberto.
- **Hierarquia Visual Reforçada para Quitação Completa**:
  - Quando todas as contas do mês estiverem pagas (`R$ 0,00` restante), a UI destaca o badge de sucesso **"Tudo quitado"** com ícone de confirmação no topo do card, **antes** da exibição do valor.
  - O valor `R$ 0,00` adota a tonalidade esmeralda de sucesso (`SuccessGreen`), reforçando visualmente a conquista de quitação em vez de sugerir ausência de dados ou valor zerado neutro.

---

## [1.6.0] - 2026-10-07

### 🔄 Extensão Contínua de Recorrências FOREVER (Janela Deslizante Automática)

- **Geração Dinâmica de Próximas Ocorrências (`CalculateInstallmentsUseCase`)**:
  - Novo método `generateNextRecurringInstallments(bill, existingInstallments, countToAdd)` para gerar parcelas subsequentes sem descontinuidade.
  - Cálculo de passos ancorado na primeira parcela para evitar desvios cumulativos de dia de vencimento (ex: preservação estrita do dia 31 após meses menores como fevereiro).
  - Continuidade estrita da numeração das parcelas (`installmentNumber = lastNumber + 1..lastNumber + count`) e atualização do total projetado.
- **Caso de Uso de Extensão de Janela (`ExtendRecurringBillsUseCase`)**:
  - Novo caso de uso de domínio puro (`domain/usecase/ExtendRecurringBillsUseCase`), sem dependências do framework Android.
  - Avaliação de limiares por frequência:
    - `MONTHLY`: estende em +12 parcelas quando restarem <= 3 ocorrências futuras ou horizonte <= 3 meses.
    - `DAILY`: estende em +30 parcelas quando restarem <= 7 ocorrências.
    - `WEEKLY`: estende em +26 parcelas quando restarem <= 4 ocorrências.
    - `YEARLY`: estende em +5 parcelas quando restar <= 1 ocorrência.
  - Laço auto-recuperativo com limite de segurança (`maxBatches = 5`), garantindo projeção adequada mesmo após longos períodos sem abertura do app.
  - Idempotência total e garantia de que contas `BY_OCCURRENCES` e `UNTIL_DATE` permaneçam estritamente delimitadas.
- **Suporte Transacional no Repositório & DAO (`FinancialRepositoryImpl` & `BillInstallmentDao`)**:
  - Novo método `addInstallments(bill, newInstallments)` com inserção transacional no Room e atualização do `recurrenceEndDate` e `totalInstallments` na tabela `bills`.
  - Nova consulta detalhada `getInstallmentsWithDetailsByBillId(billId)` em `BillInstallmentDao`.
- **Garantia de Execução Oportuna e Periódica**:
  - Invocação automática ao inicializar e atualizar `DashboardViewModel`, `BillsViewModel` (Registros) e `RecurringInstallmentsViewModel` (Assinaturas).
  - Execução matinal em segundo plano no `DueReminderReceiver` (integrado com o `AlarmManager` existente) e no desbloqueio do app em `MainActivity`.
- **Testes Automatizados**:
  - Nova suíte de testes unitários `ExtendRecurringBillsUseCaseTest` validando extensão pontual, idempotência com janelas cheias, isolamento de contas não-FOREVER e recuperação temporal.
  - Novos testes em `CalculateInstallmentsUseCaseTest` cobrindo `generateNextRecurringInstallments` e resiliência a exclusão de parcelas antigas.
  - Atualização dos testes de apresentação em `BillsViewModelTest` e `RecurringInstallmentsViewModelTest`.
- **Governança & Arquitetura**:
  - Criação da [ADR 022: Extensão Contínua de Recorrências FOREVER via Janela Deslizante Automática](.ai/DECISIONS/022-extensao-continua-recorrencias-forever.md).

---

## [1.5.0] - 2026-10-07

### 🎯 Rigidez Orçamentária Real 50-30-20 & Integração de Metas (Poupança)

- **Integração Holística de Metas Financeiras como Poupança (`GetFinancialDashboardUseCase`)**:
  - Injeção de `GoalRepository` no caso de uso do dashboard financeiro.
  - Apuração reativa dos aportes mensais em metas através do novo método `goalRepository.getMonthlyContribution(startOfMonth, endOfMonth)`.
  - Base de cálculo orçamentária unificada: Total Orçado = Total Contas do Mês + Aportes em Metas.
  - Distribuição percentual das 4 naturezas (`OBRIGATORIO`, `NECESSARIO`, `DESEJA`, `NENHUM`) e da Poupança agora somam exatamente 100% da capacidade orçamentária.
- **Motor de Diagnóstico Multifaixas de Rigidez (`BudgetRigidityCalculator`)**:
  - Eliminação definitiva do falso diagnóstico de "Excelente" quando Deseja e Poupança estiverem zerados (`SOBRECARREGADO`: 100% comprometido com despesas básicas, sem margem para estilo de vida ou reservas).
  - 8 estados e faixas estritas de classificação: `VAZIO`, `SOBRECARREGADO`, `ENGESSADO`, `ESTILO_DE_VIDA_ELEVADO`, `SEM_POUPANCA`, `NAO_CLASSIFICADO`, `EQUILIBRADO` e `EXCELENTE`.
  - Critério rigoroso para selo "Excelente": Essenciais <= 60% (Obrigatório <= 50%), Desejos entre 10% e 35%, Poupança >= 15% e Não Classificados <= 10%.
- **Histórico de Aportes no Room & Migração v11 (`PlatformDatabase`)**:
  - Criação da tabela e entidade `goal_contributions` (`id`, `goalId`, `amountCents`, `date`) com chave estrangeira e exclusão em cascata.
  - Migração de banco de dados Room `MIGRATION_10_11` (`PlatformDatabase` v11) com auto-migração de saldos existentes e registro no `AppModule`.
  - Exportação e restauração segura de histórico de aportes via Storage Access Framework em `BackupDataDto` e `BackupRepositoryImpl`.
- **Card Visual Aprimorado (`NatureDistributionCard`)**:
  - Rótulo e texto contextualizados: diagnóstico semafórico inteligente com título em destaque e descrição orientadora.
  - Inclusão visual dedicada da perna **"Poupança (Metas)"** com indicador esmeralda (`#10B981`), valor em centavos formatado e barra de progresso.
  - Rodapé elegante com exibição do **Total Orçado (Despesas + Aportes)**.
- **Suíte de Testes Automatizados**:
  - Novos testes unitários em `GetFinancialDashboardUseCaseTest` cobrindo cenário com 0% em Deseja/Poupança (assegurando que não sai "Excelente"), distribuição 50-30-20 equilibrada real com aportes de metas e cenários sem poupança.
  - Nova suíte de testes de domínio `BudgetRigidityCalculatorTest` validando todas as fronteiras e estados da matriz de classificação.
- **Governança e Arquitetura**:
  - Criação da [ADR 021: Rigidez Orçamentária 50-30-20 Real com Metas (Poupança) e Classificação Multifaixas](.ai/DECISIONS/021-rigidez-orcamentaria-50-30-20-com-metas.md).
  - Sincronização do diálogo de novidades `ReleaseNotesDialog` em `SettingsScreen.kt`.

---

## [1.4.5] - 2026-10-07

### 💳 Baixa de Pagamento Retroativa & Data Real de Pagamento (`actualPaymentDate`)

- **Suporte a Data Real de Pagamento no Modelo e Banco**:
  - Adição do campo opcional `actualPaymentDate: Long?` no modelo de domínio `BillInstallment` e na entidade Room `BillInstallmentEntity`.
  - Migração de banco de dados Room `MIGRATION_9_10` (`PlatformDatabase` v10) aplicando `ALTER TABLE bill_installments ADD COLUMN actualPaymentDate INTEGER DEFAULT NULL`.
  - Migração registrada e injetada no `AppModule`.
- **Diálogo Elegante de Baixa de Pagamento (`ConfirmPaymentDialog`)**:
  - Novo diálogo nativo Material 3 permitindo ao usuário informar a data real do pagamento na baixa da conta.
  - Atalhos instantâneos de 1 toque: "Hoje" e "No Vencimento".
  - Seletor de data (`DatePickerDialog`) permitindo conciliações e baixas retroativas ou programadas com total flexibilidade.
  - Integrado no fluxo de quitação em `BillsScreen`, `HomeScreen`, `RecurringInstallmentsScreen`, `ContactDetailScreen` e `EditInstallmentBottomSheet`.
- **Cálculo Preciso da Taxa de Pagamentos Pontuais (`GetFinancialDashboardUseCase`)**:
  - O cálculo da métrica `onTimePaymentRate` agora compara a data real de pagamento (`actualPaymentDate`), com fallback para `paidAt` e `dueDate`, contra o vencimento da parcela.
  - Baixas retroativas de contas pagas no prazo não são mais classificadas erroneamente como atrasadas.
- **Suíte de Testes Unitários**:
  - Testes unitários atualizados e adicionados em `ToggleInstallmentPaymentUseCaseTest`, `GetFinancialDashboardUseCaseTest`, `BillsViewModelTest` e `HomeViewModelTest`, cobrindo quitação retroativa no prazo, quitação retroativa em atraso, fallback e desmarcação de pagamento.
- **Governança e Arquitetura**:
  - Criação da [ADR 020: Data Real de Pagamento, Baixa Retroativa e Migração Room v10](.ai/DECISIONS/020-data-real-pagamento-e-baixa-retroativa.md).

---

## [1.4.4] - 2026-09-29

### ✏️ Gestão e Edição de Itens de Despesa (`ExpenseItemsScreen` & `NewExpenseScreen`)

- **Edição Completa de Itens de Despesa**:
  - Implementação do fluxo de edição reativo para itens de despesa existentes via `AddEditExpenseItemBottomSheet`, permitindo renomear o item ou alterar sua categoria vinculada com herança instantânea da nova natureza financeira.
- **Harmonização com o Padrão Universal de Detalhes (`ExpenseItemDetailBottomSheet`)**:
  - O toque direto em qualquer card de item agora abre um `ModalBottomSheet` dedicado com avatar na cor da categoria, nome do item, identificadores, chip de natureza e menu contextual de 3 pontos (`MoreVert`).
  - Acesso direto à ação "Editar Item" via botão de largura total na base do sheet e via menu de 3 pontos.
  - Ação de exclusão mantida de forma segura com diálogo de confirmação `AlertDialog`.
- **Atalhos e Acesso Rápido em Nova Despesa (`NewExpenseScreen`)**:
  - Adicionada opção direta "Gerenciar Itens (Criar / Editar)" no menu dropdown de seleção de itens e botão de edição rápida de 1 toque no chip de natureza herdada, permitindo ajustar itens sem perder o contexto do lançamento.
- **Suíte de Testes Automatizados**:
  - Nova suíte de testes unitários `ExpenseItemsViewModelTest.kt` validando carga inicial, filtro de pesquisa, filtro por categoria, criação e salvamento de itens editados (preservação de ID) e exclusão.

---

## [1.4.3] - 2026-09-29

### 🚀 Pacote de Excelência Operacional & UX (Visual, Ações, Funções e Sistema)

- **1. Visual: Transições de Tela Cinemáticas e Fluidas (`NavGraph.kt`)**:
  - Implementação de transições animadas nativas no `NavHost` (`slideIntoContainer` horizontal + `fadeIn`/`fadeOut` interpolados com `tween(260ms)`).
  - Experiência visual contínua sem saltos abruptos de layout ao transitar entre Dashboard, Registros, Cartões, Recorrentes e Estatísticas.

- **2. Ações (UX): Mecanismo de Desfazer (`Undo`) para Baixa de Contas e Faturas**:
  - Integração de `SnackbarHost` e canal de efeitos `HomeUiEffect.ShowUndoSnackbar` na `HomeScreen`.
  - Ao quitar uma conta avulsa ou fatura com 1 toque, é exibido um Snackbar com ação **"Desfazer"**, permitindo reversão imediata de cliques acidentais.
  - Adicionado `reopenInvoice` em `FinancialRepositoryImpl.kt` com transação atômica (`database.withTransaction`), restaurando o status da fatura para `ABERTA` e suas parcelas vinculadas para `PENDING`.

- **3. Funções: Duplicação Inteligente de Despesas (`duplicateBillId`)**:
  - Nova opção **"Duplicar Despesa"** no menu de contexto (`DropdownMenu`) do BottomSheet de detalhes da parcela (`BillsScreen.kt`).
  - Navegação para `NewExpenseScreen` carregando a despesa original via `SavedStateHandle` no `NewExpenseViewModel`, clonando valor, categoria, item, contato, conta bancária, forma de pagamento e tipo, acelerando cadastros repetitivos.

- **4. Sistema: Lembretes e Notificações Locais de Vencimento Offline**:
  - Criação do canal e gerenciador `DueReminderManager.kt` (`com.platform.app.core.notification`) de alta prioridade.
  - `DueReminderReceiver.kt`: BroadcastReceiver com `@AndroidEntryPoint` e injeção Hilt para checagem diária programada via `AlarmManager`.
  - Verificação inteligente na `MainActivity`: Notifica o usuário de forma offline e discreta caso existam contas ou faturas com vencimento no próprio dia.
  - Permissão `POST_NOTIFICATIONS` declarada no `AndroidManifest.xml`.

- **5. Qualidade e Testes Unitários**:
  - Novos testes automatizados: `HomeViewModelTest.kt` (ações de pagamento, undo de contas e faturas) e `NewExpenseViewModelTest.kt` (pré-carregamento por duplicação).
  - 100% dos testes unitários validados e aprovados com sucesso (`./gradlew testDebugUnitTest`).

---

## [1.4.2] - 2026-09-29

### 📊 Inteligência Financeira 360° & Refatoração da Tela de Estatísticas
- **Visão Temporal Integrada em 3 Eixos (Passado, Presente e Futuro)**:
  - Navegação fluida via `PlatformSegmentedTabs` (`Passado`, `Presente`, `Futuro`) com suporte nativo ao **Modo Privacidade** (`PlatformPrivacyToggle`).
- **1. Eixo Passado (Histórico & Tendências — Onde estivemos)**:
  - **Média Histórica Mensal**: Baseline de custo de vida para guiar novos planejamentos.
  - **Evolução dos Últimos 6 Meses**: Barras comparativas mês a mês de valor devido vs valor liquidado, com taxa de quitação individual e atalho de salto direto para o mês.
  - **Comparativo com o Mês Anterior**: Variação em R$ e % com badges semafóricos de alta/baixa.
  - **Picos e Vales Históricos**: Identificação do mês mais pesado vs mês mais econômico.
  - **Ranking de Top Destinatários / Fornecedores**: Contatos que mais absorveram recursos.
- **2. Eixo Presente (Raio-X do Mês Selecionado — Onde estamos)**:
  - **Hero Card de Execução Orçamentária**: Total devido, valor pago, pendente e atrasado com termômetro semafórico `PlatformProgressBar`.
  - **Rigidez Orçamentária (Regra 50/30/20 & Natureza dos Gastos)**: Classificação em `Obrigatório`, `Necessário`, `Deseja` e `Nenhum` com card de diagnóstico estratégico inteligente (*Alerta de Orçamento Engessado* caso Obrigatório > 55%).
  - **Meio de Liquidação & Crédito**: Barra bifurcada e percentuais de exposição entre Cartão de Crédito vs Débito/Pix/Dinheiro.
  - **Top Categorias & Contas Bancárias**: Distribuição visual dos gastos e concentração institucional.
- **3. Eixo Futuro (Previsibilidade & Tomada de Decisão — Para onde vamos)**:
  - **Curva de Desoneração (Próximos 6 Meses)**: Evolução decrescente dos pagamentos com destaque para o mês de maior pico e o mês de maior folga financeira.
  - **Desoneração & Término de Parcelas**: Cronograma de compras parceladas ativas que chegam ao fim, com cálculo de alívio mensal gerado (*"+R$ X/mês livre"*).
  - **Cockpit de Tomada de Decisão (Smart Advisor)**: Consultoria contextual em tempo real para responder *"Posso assumir uma nova compra parcelada agora?"*.
- **4. Domínio & Testes Automatizados**:
  - Modelos enriquecidos em `FinancialDashboardMetrics.kt` e agregação performática em `GetFinancialDashboardUseCase.kt`.
  - 100% da suíte unitária aprovada (`./gradlew testDebugUnitTest`).

---

## [1.4.1] - 2026-09-29

### 💎 Grande Update das 4 Telas Centrais (Obsidian & Porcelain Minimalist)
- **1. Tela Inicial (`HomeScreen`)**:
  - **Executive Hero Card**: Adição de barra de progresso horizontal (`PlatformProgressBar`) com cantos arredondados indicando o percentual de quitação do mês (ex: *"68% quitado"*) e métricas de Já Quitado e Total Geral.
  - **Modo Privacidade (`PlatformPrivacyToggle`)**: Alternador com ícone de olho para mascarar valores confidenciais (`R$ ••••••`) em locais públicos.
  - **Seletor de Mês Rápido em Grade (`MonthPickerBottomSheet`)**: Grade visual de meses e seletor de ano direto no título do mês, eliminando cliques repetitivos em setas.
  - **Empty State Triunfante (`MonthVictoryCard`)**: Card comemorativo esmeralda quando todas as contas do mês estiverem 100% quitadas.
  - **Micro-interações Hápticas**: Vibração suave ao efetuar a baixa de contas na lista.
- **2. Tela de Registros (`BillsScreen`)**:
  - **Segmented Tabs no Topo (`PlatformSegmentedTabs`)**: Divisão em 3 abas especializadas (`A Pagar`, `Pagas`, `Todas` com contadores numéricos).
  - **Banner de Totais Filtrados**: Resumo instantâneo do valor total e quantidade de registros visíveis.
  - **Ações em Lote (`PlatformBatchActionBar`)**: Modo de seleção múltipla (toque longo ou botão no TopBar) para liquidar ou excluir várias contas com 1 confirmação.
- **3. Tela de Recorrentes & Parcelados (`RecurringInstallmentsScreen`)**:
  - **Separação por Abas**: Divisão nítida entre `Compras Parceladas` (amortização com data final) e `Assinaturas & Custos Fixos` (compromisso mensal contínuo).
  - **Card de Amortização Estilo Financiamento**: Progresso visual com `PlatformProgressBar`, projeção de data de quitação final (*"Término em Mês/Ano"*) e saldo devedor restante.
  - **Cronograma de Parcelas**: Acompanhamento detalhado de cada parcela com baixa direta.
  - **Projeção Anual de Assinaturas**: Exibição do custo anual consolidado (*"R$ Y/ano"*).
- **4. Tela de Cartões de Crédito (`CreditCardsScreen`)**:
  - **Design Virtual Apple Card / Revolut (`PlatformCreditCardView`)**: Proporção bancária exata (`1.586f`), chip EMV vetorial metálico, gradiente acetinado e termômetro de limite inteligente semafórico integrado.
  - **Ciclo de 3 Faturas**: Alternância de abas entre `Aberta`, `Fechadas`, `Pagas` e `Todas`.
  - **Destaque do Melhor Dia de Compra**: Orientação visual do dia de corte para compras com até 40 dias de prazo.
  - **Atalho de Nova Compra**: Botão direto no extrato para lançar despesa pré-selecionando o cartão.
- **5. Novos Componentes Reutilizáveis**:
  - `PlatformProgressBar.kt`, `PlatformPrivacyToggle.kt`, `PlatformSegmentedTabs.kt`, `PlatformBatchActionBar.kt`, `PlatformCreditCardView.kt`.
- **6. Validação e Qualidade**:
  - 100% da suíte `./gradlew testDebugUnitTest` aprovada e build limpo em 4 segundos.

---

## [1.4.0] - 2026-09-29

### 🚀 Refatoração End-to-End: Wallet 100% Pessoal & Gestão de Contas e Faturas
- **Contexto 100% Pessoal Unificado**:
  - Eliminação definitiva de qualquer perfil corporativo/pessoal (`profileType`, `PESSOAL`/`EMPRESA`), operando com foco exclusivo em finanças pessoais.
- **Foco em Contas a Pagar & Previsibilidade**:
  - Cadastro ágil e intuitivo exigindo apenas 3 dados obrigatórios: Descrição, Valor (em centavos `Long`) e Vencimento.
  - Seletores de Categoria e Item opcionais.
- **Natureza Financeira (ExpenseNature) & Itens de Despesa**:
  - Implementação dos 4 pilares: `OBRIGATORIO` (custos inegociáveis), `NECESSARIO` (manutenção essencial), `DESEJA` (estilo de vida e conforto) e `NENHUM` (transitórios/não classificados).
  - Depreciação de subcategorias e criação da tabela `expense_items` vinculada diretamente a `categories`, com herança estrita da natureza da categoria mãe.
  - Nova tela `ExpenseItemsScreen` em Configurações para listar, pesquisar e cadastrar itens vinculados com pré-visualização de natureza.
- **Efeito Cascata no Dashboard em Tempo Real**:
  - Total Previsto no Mês calculado dinamicamente via Flow somando contas avulsas pendentes e faturas de cartão abertas/fechadas do período.
  - Agrupamento semafórico de urgência: 🔴 Atrasadas, 🟡 Vence Hoje, ⚪ Próximos 7 Dias e 🟢 Pagas no Mês (colapsável).
  - Baixa com 1 Toque ("Pagar"): atualiza status para `PAGO` no banco e deduz instantaneamente do total mensal sem recarregar a tela.
- **Cartões de Crédito & Extrato de Faturas**:
  - Cálculo robusto de limite disponível (`CreditCardCalculator`), fechamento de faturas baseado no dia de corte (`closingDay`) e status da fatura (`ABERTA`, `FECHADA`, `PAGA`).
  - Extrato detalhado de faturas em `CreditCardsScreen` com botão de liquidação em 1 toque.
- **Persistência Room & Migração**:
  - Atualização do `PlatformDatabase` para versão `7` com `MIGRATION_6_7`.
  - Inclusão da entidade `TransactionEntity` e DAO `TransactionDao` para rastreamento de transações.
  - Tipo alias `AppDatabase = PlatformDatabase` para interoperabilidade.
- **Design System Material 3 (ADR-018)**:
  - Aplicação estrita de tokens de cores para Dark Mode e Light Mode.
  - Proibição de cores literais nos componentes; uso estrito de `MaterialTheme.colorScheme`.
  - Tipografia em Title Case (sem ALL CAPS em textos de tela).
  - Ícones funcionais do Material Icons substituindo formas abstratas.
- **Lançamento Focado em Itens (Item-Centric Expenses)**:
  - O Item de Despesa (`selectedItemId`) assumiu o protagonismo absoluto no formulário: primeiro campo de seleção com preview de Natureza Financeira e Categoria.
  - A descrição passa a ser campo de "Observações Adicionais (Opcional)", herdando o nome do item selecionado como título padrão caso não preenchida.
  - Reorganização do formulário em 5 blocos harmônicos e simétricos com cards e divisões semânticas.
- **Design System Premium Minimalista & Simetria Cirúrgica**:
  - Paletas Dark e Light purificadas: Dark Obsidian (`#0A0D14` / `#141923`) e Light Porcelain (`#F8FAFC` / `#FFFFFF`).
  - Anulação estrita de elevação tonal com `surfaceTint = Color.Transparent` em ambos os temas, eliminando 100% qualquer vazamento roxo/lavanda default do Material 3.
  - Erradicação completa de cores literais hardcoded (`Color.White`, `Color.Black`, hexadecimais soltos) em todas as telas, adotando exclusivamente tokens de `Theme.kt`, `Color.kt` e `MaterialTheme.colorScheme`.
  - Anti-gigantismo e harmonia visual: ajuste da tipografia do impacto financeiro na HomeScreen de `headlineLarge` para `titleLarge` semibold/bold.
  - Alinhamentos milimétricos com cantos uniformes de 16.dp para cards, 10.dp para botões/inputs e bordas finas de 1.dp com `outlineVariant`.
  - **Padronização Global dos FABs (100% Circular)**: Unificação dos FloatingActionButtons de todas as 9 telas financeiras (incluindo `RecurringInstallmentsScreen`) com `shape = CircleShape`, container `primary` e conteúdo `onPrimary`.
  - **Unificação Absoluta de Fluxos de Despesa**: Remoção do modal legado `AddBillBottomSheet` em `BillsScreen`. Tanto a tela Inicial quanto a tela de Registros navegam para a mesma `NewExpenseScreen` com o formulário em 5 blocos harmônicos focado em itens.
  - **Componentes Centrais Reutilizáveis (`PlatformEmptyState` e `PlatformAvatar`)**:
    - `PlatformEmptyState`: Eliminação definitiva de emojis informais (`💳`, `📈`) em telas vazias, substituídos por containers vetoriais com fundo translúcido a 12% e tipografia proporcional `titleLarge` semibold.
    - `PlatformAvatar`: Eliminação de código duplicado de cálculo de iniciais e renderização visual homogênea em `ContactsScreen` e `ContactDetailScreen`.
  - **Higienização Visual Estrita (Zero Ações Inline Não Seguras)**:
    - `ExpenseItemsScreen`: Remoção de botões de exclusão inline em cards, substituídos por navegação sutil com chevron e `AlertDialog` de confirmação seguro.
    - `BudgetsScreen`: Migração de `BudgetCard` para `PlatformCard` com raio de 16.dp e contorno suave.
    - `CreditCardsScreen`: Ajuste de contraste do botão "Liquidar Fatura" (`onPrimary` garantindo legibilidade perfeita no Tema Escuro e Claro).
    - `SettingsScreen`: Distinção de ícone para Itens de Despesa com `Icons.Default.ShoppingBag`.
    - `AppDrawer`: Correção do divisor de seções com verificação insensível a maiúsculas/minúsculas (`ignoreCase = true`).
  - **Eliminação de Código Zumbi & Depreciações**:
    - Remoção do arquivo zumbi `DashboardScreen.kt` (-1.073 linhas de código morto).
    - Migração de todos os ícones deprecados para `Icons.AutoMirrored.Filled` (`ReceiptLong` e `TrendingUp`), zerando warnings de compilação.
- **Cobertura de Testes Automatizados**:
  - `NewExpenseValidationTest`: 6 testes automatizados validando campos obrigatórios (Item e Contato), herança de natureza, cálculo de parcelas e preenchimento de título a partir do item.
  - `CreditCardManagementTest`: Validação de fechamento, limite e integridade de faturas.
  - `BillCalculationTest`, `ExpenseItemNatureTest`, `InvoiceClosingTest`: Todos aprovados com 100% de sucesso.

---

## [1.3.1] - 2026-09-28

### 🎨 Melhorias Visuais & Ergonomia
- **Ajuste de Proporções dos Cards em Configurações**:
  - Ampliação da área de toque e respiro interno dos blocos de acesso direto a **Contas**, **Formas de Pagamento** e **Categorias** (padding vertical aumentado para 14dp e horizontal para 16dp).
  - Ícones com badges ampliados de 32dp para 44dp e cantos arredondados de 12dp para maior destaque visual e clareza de toque.
  - Inclusão de subtítulos descritivos elegantes abaixo de cada título para reforçar o propósito de cada área.
  - Tipografia elevada para titleMedium semibold e chevron de navegação mais visível.
  - Espaçamento vertical entre blocos unificado em 12dp.

---

## [1.3.0] - 2026-09-28

### 🚀 Novas Funcionalidades
- **Backup e Restauração de Dados Offline**:
  - Exportação completa e versionada do banco de dados em formato JSON compacto com valores inteiros em centavos (`Long`).
  - Restauração atômica em lote utilizando transações seguras do Room (`database.withTransaction`), garantindo rollback total em caso de falha.
  - Integração com o *Storage Access Framework (SAF)* nativo do Android (`CreateDocument` e `OpenDocument`) para salvar e restaurar do Google Drive, Documentos, Downloads ou cartão SD.
  - Compartilhamento rápido via *ShareSheet* (`FileProvider`) direto para WhatsApp, Telegram ou e-mail.
  - Indicador em tempo real nas Configurações com a data e hora exata do último backup efetuado.
  - Diálogo de advertência e confirmação antes de restaurações para impedir substituições acidentais de dados.

### 🎨 Harmonização Visual & Padrão de Detalhes (4 Etapas)
- **Adoção Universal do Padrão de Detalhes com BottomSheet**:
  - Cards 100% limpos e minimalistas em todo o sistema: remoção de botões inline de lixeira, lápis e acordeões soltos.
  - Toque no card abre `ModalBottomSheet` dedicado com métricas contextuais, visualização de vínculos (conta, categoria, contato, método) e ações rápidas.
  - Menu de 3 pontos (`MoreVert`) no topo do BottomSheet para edição e exclusão segura.
  - Diálogos de confirmação obrigatórios (`AlertDialog`) antes de qualquer exclusão definitiva.
  - Seletores de campos com opções pré-definidas migrados para menus dropdown nativos (`ExposedDropdownMenuBox`).
  - Seletor de cores aprimorado com indicador visual circular branco na cor ativa.
- **Telas Harmonizadas**:
  - [ManagementScreen.kt]: Formas de Pagamento e Categorias desacopladas com BottomSheet e seletor com indicador.
  - [BillsScreen.kt]: Cards de parcelas sanitizados, tipografia reduzida de 28sp para 16sp bold, chevron sutil e BottomSheet detalhado de liquidação.
  - [GoalsScreen.kt]: Cards de metas limpos, BottomSheet com progresso, aporte rápido e menu de ações.
  - [BudgetsScreen.kt]: Cards de orçamento limpos, BottomSheet com comparação teto vs realizado e ajuste de limite.
  - [RecurringInstallmentsScreen.kt]: Cards de contratos enxutos, BottomSheet com detalhes do contrato e listagem completa de parcelas interativas.
  - [ContactDetailScreen.kt]: Exclusão segura de contato no menu de 3 pontos com diálogo de confirmação.

---

## [1.2.0] - 2026-09-28

### 🚀 Melhorias & Evoluções
- **Gestão Financeira Desacoplada nas Configurações**: Telas dedicadas e independentes para Contas Bancárias, Formas de Pagamento e Categorias, sem acoplamento de abas concorrentes.
- **Menu Lateral Minimalista**: Redução de ruído visual, eliminação de subtítulos descritivos em todos os itens do drawer, proporções enxutas e alinhamento com Material 3.
- **Dashboard Financeiro Macro**: Cards inteligentes com visão geral de saldo, compromissos do mês vigente e projeção futura de gastos.
- **Recorrentes & Parcelados**: Gestão centralizada de assinaturas fixas e compras parceladas em andamento com cálculo automático de amortizações.
- **Metas & Orçamentos**: Controle de reservas financeiras com barra de progresso visual e definição de tetos de gastos mensais por categoria.
- **Cadastro Inteligente de Contatos**: Integração de busca automática de CEP via ViaCEP, novos campos estruturados de endereço e edição com 1 toque.
- **Automação de Versionamento**: Sincronização automática entre o arquivo `VERSION`, `app/build.gradle.kts` e scripts de bump para evitar estagnação de versão.

---

## [1.0.0] - 2026-09-26

### 🚀 Melhorias
- Fundação da arquitetura base do aplicativo Android em Clean Architecture (Presentation, Domain, Data, Core).
- Configuração do Android Gradle Plugin com Kotlin DSL (`build.gradle.kts` e Version Catalog `libs.versions.toml`).
- Integração da stack moderna de UI com Jetpack Compose BOM, Material 3 e suporte a Tema Claro e Escuro.
- Injeção de dependências desacoplada preparada com Hilt.
- Configuração de persistência local com Room Database e comunicação remota com Retrofit/OkHttp.
- Implementação de fluxos reativos com Kotlin Coroutines e StateFlow.
- Estrutura completa de governança de IA (`AGENT_RULES.md`, `.ai/` e `.agents/` com skills e regras).
- Automação de versionamento móvel sincronizando `versionCode`, `versionName` e `CHANGELOG.md`.

---









