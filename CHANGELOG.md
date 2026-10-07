# Changelog - Platform (Android App)

Todas as alteraÃƒÂ§ÃƒÂµes notÃƒÂ¡veis neste projeto serÃƒÂ£o documentadas neste arquivo.
O formato ÃƒÂ© baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/) e este projeto segue [Versionamento SemÃƒÂ¢ntico](https://semver.org/lang/pt-BR/).

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

### âœï¸ GestÃ£o e EdiÃ§Ã£o de Itens de Despesa (`ExpenseItemsScreen` & `NewExpenseScreen`)

- **EdiÃ§Ã£o Completa de Itens de Despesa**:
  - ImplementaÃ§Ã£o do fluxo de ediÃ§Ã£o reativo para itens de despesa existentes via `AddEditExpenseItemBottomSheet`, permitindo renomear o item ou alterar sua categoria vinculada com heranÃ§a instantÃ¢nea da nova natureza financeira.
- **HarmonizaÃ§Ã£o com o PadrÃ£o Universal de Detalhes (`ExpenseItemDetailBottomSheet`)**:
  - O toque direto em qualquer card de item agora abre um `ModalBottomSheet` dedicado com avatar na cor da categoria, nome do item, identificadores, chip de natureza e menu contextual de 3 pontos (`MoreVert`).
  - Acesso direto Ã  aÃ§Ã£o "Editar Item" via botÃ£o de largura total na base do sheet e via menu de 3 pontos.
  - AÃ§Ã£o de exclusÃ£o mantida de forma segura com diÃ¡logo de confirmaÃ§Ã£o `AlertDialog`.
- **Atalhos e Acesso RÃ¡pido em Nova Despesa (`NewExpenseScreen`)**:
  - Adicionada opÃ§Ã£o direta "Gerenciar Itens (Criar / Editar)" no menu dropdown de seleÃ§Ã£o de itens e botÃ£o de ediÃ§Ã£o rÃ¡pida de 1 toque no chip de natureza herdada, permitindo ajustar itens sem perder o contexto do lanÃ§amento.
- **SuÃ­te de Testes Automatizados**:
  - Nova suÃ­te de testes unitÃ¡rios `ExpenseItemsViewModelTest.kt` validando carga inicial, filtro de pesquisa, filtro por categoria, criaÃ§Ã£o e salvamento de itens editados (preservaÃ§Ã£o de ID) e exclusÃ£o.

---

## [1.4.3] - 2026-09-29

### Ã°Å¸Å¡â‚¬ Pacote de ExcelÃƒÂªncia Operacional & UX (Visual, AÃƒÂ§ÃƒÂµes, FunÃƒÂ§ÃƒÂµes e Sistema)

- **1. Visual: TransiÃƒÂ§ÃƒÂµes de Tela CinemÃƒÂ¡ticas e Fluidas (`NavGraph.kt`)**:
  - ImplementaÃƒÂ§ÃƒÂ£o de transiÃƒÂ§ÃƒÂµes animadas nativas no `NavHost` (`slideIntoContainer` horizontal + `fadeIn`/`fadeOut` interpolados com `tween(260ms)`).
  - ExperiÃƒÂªncia visual contÃƒÂ­nua sem saltos abruptos de layout ao transitar entre Dashboard, Registros, CartÃƒÂµes, Recorrentes e EstatÃƒÂ­sticas.

- **2. AÃƒÂ§ÃƒÂµes (UX): Mecanismo de Desfazer (`Undo`) para Baixa de Contas e Faturas**:
  - IntegraÃƒÂ§ÃƒÂ£o de `SnackbarHost` e canal de efeitos `HomeUiEffect.ShowUndoSnackbar` na `HomeScreen`.
  - Ao quitar uma conta avulsa ou fatura com 1 toque, ÃƒÂ© exibido um Snackbar com aÃƒÂ§ÃƒÂ£o **"Desfazer"**, permitindo reversÃƒÂ£o imediata de cliques acidentais.
  - Adicionado `reopenInvoice` em `FinancialRepositoryImpl.kt` com transaÃƒÂ§ÃƒÂ£o atÃƒÂ´mica (`database.withTransaction`), restaurando o status da fatura para `ABERTA` e suas parcelas vinculadas para `PENDING`.

- **3. FunÃƒÂ§ÃƒÂµes: DuplicaÃƒÂ§ÃƒÂ£o Inteligente de Despesas (`duplicateBillId`)**:
  - Nova opÃƒÂ§ÃƒÂ£o **"Duplicar Despesa"** no menu de contexto (`DropdownMenu`) do BottomSheet de detalhes da parcela (`BillsScreen.kt`).
  - NavegaÃƒÂ§ÃƒÂ£o para `NewExpenseScreen` carregando a despesa original via `SavedStateHandle` no `NewExpenseViewModel`, clonando valor, categoria, item, contato, conta bancÃƒÂ¡ria, forma de pagamento e tipo, acelerando cadastros repetitivos.

- **4. Sistema: Lembretes e NotificaÃƒÂ§ÃƒÂµes Locais de Vencimento Offline**:
  - CriaÃƒÂ§ÃƒÂ£o do canal e gerenciador `DueReminderManager.kt` (`com.platform.app.core.notification`) de alta prioridade.
  - `DueReminderReceiver.kt`: BroadcastReceiver com `@AndroidEntryPoint` e injeÃƒÂ§ÃƒÂ£o Hilt para checagem diÃƒÂ¡ria programada via `AlarmManager`.
  - VerificaÃƒÂ§ÃƒÂ£o inteligente na `MainActivity`: Notifica o usuÃƒÂ¡rio de forma offline e discreta caso existam contas ou faturas com vencimento no prÃƒÂ³prio dia.
  - PermissÃƒÂ£o `POST_NOTIFICATIONS` declarada no `AndroidManifest.xml`.

- **5. Qualidade e Testes UnitÃƒÂ¡rios**:
  - Novos testes automatizados: `HomeViewModelTest.kt` (aÃƒÂ§ÃƒÂµes de pagamento, undo de contas e faturas) e `NewExpenseViewModelTest.kt` (prÃƒÂ©-carregamento por duplicaÃƒÂ§ÃƒÂ£o).
  - 100% dos testes unitÃƒÂ¡rios validados e aprovados com sucesso (`./gradlew testDebugUnitTest`).

---

## [1.4.2] - 2026-09-29

### Ã°Å¸â€œÅ  InteligÃƒÂªncia Financeira 360Ã‚Â° & RefatoraÃƒÂ§ÃƒÂ£o da Tela de EstatÃƒÂ­sticas
- **VisÃƒÂ£o Temporal Integrada em 3 Eixos (Passado, Presente e Futuro)**:
  - NavegaÃƒÂ§ÃƒÂ£o fluida via `PlatformSegmentedTabs` (`Passado`, `Presente`, `Futuro`) com suporte nativo ao **Modo Privacidade** (`PlatformPrivacyToggle`).
- **1. Eixo Passado (HistÃƒÂ³rico & TendÃƒÂªncias Ã¢â‚¬â€ Onde estivemos)**:
  - **MÃƒÂ©dia HistÃƒÂ³rica Mensal**: Baseline de custo de vida para guiar novos planejamentos.
  - **EvoluÃƒÂ§ÃƒÂ£o dos ÃƒÅ¡ltimos 6 Meses**: Barras comparativas mÃƒÂªs a mÃƒÂªs de valor devido vs valor liquidado, com taxa de quitaÃƒÂ§ÃƒÂ£o individual e atalho de salto direto para o mÃƒÂªs.
  - **Comparativo com o MÃƒÂªs Anterior**: VariaÃƒÂ§ÃƒÂ£o em R$ e % com badges semafÃƒÂ³ricos de alta/baixa.
  - **Picos e Vales HistÃƒÂ³ricos**: IdentificaÃƒÂ§ÃƒÂ£o do mÃƒÂªs mais pesado vs mÃƒÂªs mais econÃƒÂ´mico.
  - **Ranking de Top DestinatÃƒÂ¡rios / Fornecedores**: Contatos que mais absorveram recursos.
- **2. Eixo Presente (Raio-X do MÃƒÂªs Selecionado Ã¢â‚¬â€ Onde estamos)**:
  - **Hero Card de ExecuÃƒÂ§ÃƒÂ£o OrÃƒÂ§amentÃƒÂ¡ria**: Total devido, valor pago, pendente e atrasado com termÃƒÂ´metro semafÃƒÂ³rico `PlatformProgressBar`.
  - **Rigidez OrÃƒÂ§amentÃƒÂ¡ria (Regra 50/30/20 & Natureza dos Gastos)**: ClassificaÃƒÂ§ÃƒÂ£o em `ObrigatÃƒÂ³rio`, `NecessÃƒÂ¡rio`, `Deseja` e `Nenhum` com card de diagnÃƒÂ³stico estratÃƒÂ©gico inteligente (*Alerta de OrÃƒÂ§amento Engessado* caso ObrigatÃƒÂ³rio > 55%).
  - **Meio de LiquidaÃƒÂ§ÃƒÂ£o & CrÃƒÂ©dito**: Barra bifurcada e percentuais de exposiÃƒÂ§ÃƒÂ£o entre CartÃƒÂ£o de CrÃƒÂ©dito vs DÃƒÂ©bito/Pix/Dinheiro.
  - **Top Categorias & Contas BancÃƒÂ¡rias**: DistribuiÃƒÂ§ÃƒÂ£o visual dos gastos e concentraÃƒÂ§ÃƒÂ£o institucional.
- **3. Eixo Futuro (Previsibilidade & Tomada de DecisÃƒÂ£o Ã¢â‚¬â€ Para onde vamos)**:
  - **Curva de DesoneraÃ§Ã£o (PrÃ³ximos 6 Meses)**: EvoluÃ§Ã£o decrescente dos pagamentos com destaque para o mÃªs de maior pico e o mÃªs de maior folga financeira.
  - **DesoneraÃƒÂ§ÃƒÂ£o & TÃƒÂ©rmino de Parcelas**: Cronograma de compras parceladas ativas que chegam ao fim, com cÃƒÂ¡lculo de alÃƒÂ­vio mensal gerado (*"+R$ X/mÃƒÂªs livre"*).
  - **Cockpit de Tomada de DecisÃƒÂ£o (Smart Advisor)**: Consultoria contextual em tempo real para responder *"Posso assumir uma nova compra parcelada agora?"*.
- **4. DomÃƒÂ­nio & Testes Automatizados**:
  - Modelos enriquecidos em `FinancialDashboardMetrics.kt` e agregaÃƒÂ§ÃƒÂ£o performÃƒÂ¡tica em `GetFinancialDashboardUseCase.kt`.
  - 100% da suÃƒÂ­te unitÃƒÂ¡ria aprovada (`./gradlew testDebugUnitTest`).

---

## [1.4.1] - 2026-09-29

### Ã°Å¸â€™Å½ Grande Update das 4 Telas Centrais (Obsidian & Porcelain Minimalist)
- **1. Tela Inicial (`HomeScreen`)**:
  - **Executive Hero Card**: AdiÃƒÂ§ÃƒÂ£o de barra de progresso horizontal (`PlatformProgressBar`) com cantos arredondados indicando o percentual de quitaÃƒÂ§ÃƒÂ£o do mÃƒÂªs (ex: *"68% quitado"*) e mÃƒÂ©tricas de JÃƒÂ¡ Quitado e Total Geral.
  - **Modo Privacidade (`PlatformPrivacyToggle`)**: Alternador com ÃƒÂ­cone de olho para mascarar valores confidenciais (`R$ Ã¢â‚¬Â¢Ã¢â‚¬Â¢Ã¢â‚¬Â¢Ã¢â‚¬Â¢Ã¢â‚¬Â¢Ã¢â‚¬Â¢`) em locais pÃƒÂºblicos.
  - **Seletor de MÃƒÂªs RÃƒÂ¡pido em Grade (`MonthPickerBottomSheet`)**: Grade visual de meses e seletor de ano direto no tÃƒÂ­tulo do mÃƒÂªs, eliminando cliques repetitivos em setas.
  - **Empty State Triunfante (`MonthVictoryCard`)**: Card comemorativo esmeralda quando todas as contas do mÃƒÂªs estiverem 100% quitadas.
  - **Micro-interaÃƒÂ§ÃƒÂµes HÃƒÂ¡pticas**: VibraÃƒÂ§ÃƒÂ£o suave ao efetuar a baixa de contas na lista.
- **2. Tela de Registros (`BillsScreen`)**:
  - **Segmented Tabs no Topo (`PlatformSegmentedTabs`)**: DivisÃƒÂ£o em 3 abas especializadas (`A Pagar`, `Pagas`, `Todas` com contadores numÃƒÂ©ricos).
  - **Banner de Totais Filtrados**: Resumo instantÃƒÂ¢neo do valor total e quantidade de registros visÃƒÂ­veis.
  - **AÃƒÂ§ÃƒÂµes em Lote (`PlatformBatchActionBar`)**: Modo de seleÃƒÂ§ÃƒÂ£o mÃƒÂºltipla (toque longo ou botÃƒÂ£o no TopBar) para liquidar ou excluir vÃƒÂ¡rias contas com 1 confirmaÃƒÂ§ÃƒÂ£o.
- **3. Tela de Recorrentes & Parcelados (`RecurringInstallmentsScreen`)**:
  - **SeparaÃƒÂ§ÃƒÂ£o por Abas**: DivisÃƒÂ£o nÃƒÂ­tida entre `Compras Parceladas` (amortizaÃƒÂ§ÃƒÂ£o com data final) e `Assinaturas & Custos Fixos` (compromisso mensal contÃƒÂ­nuo).
  - **Card de AmortizaÃƒÂ§ÃƒÂ£o Estilo Financiamento**: Progresso visual com `PlatformProgressBar`, projeÃƒÂ§ÃƒÂ£o de data de quitaÃƒÂ§ÃƒÂ£o final (*"TÃƒÂ©rmino em MÃƒÂªs/Ano"*) e saldo devedor restante.
  - **Cronograma de Parcelas**: Acompanhamento detalhado de cada parcela com baixa direta.
  - **ProjeÃƒÂ§ÃƒÂ£o Anual de Assinaturas**: ExibiÃƒÂ§ÃƒÂ£o do custo anual consolidado (*"R$ Y/ano"*).
- **4. Tela de CartÃƒÂµes de CrÃƒÂ©dito (`CreditCardsScreen`)**:
  - **Design Virtual Apple Card / Revolut (`PlatformCreditCardView`)**: ProporÃƒÂ§ÃƒÂ£o bancÃƒÂ¡ria exata (`1.586f`), chip EMV vetorial metÃƒÂ¡lico, gradiente acetinado e termÃƒÂ´metro de limite inteligente semafÃƒÂ³rico integrado.
  - **Ciclo de 3 Faturas**: AlternÃƒÂ¢ncia de abas entre `Aberta`, `Fechadas`, `Pagas` e `Todas`.
  - **Destaque do Melhor Dia de Compra**: OrientaÃƒÂ§ÃƒÂ£o visual do dia de corte para compras com atÃƒÂ© 40 dias de prazo.
  - **Atalho de Nova Compra**: BotÃƒÂ£o direto no extrato para lanÃƒÂ§ar despesa prÃƒÂ©-selecionando o cartÃƒÂ£o.
- **5. Novos Componentes ReutilizÃƒÂ¡veis**:
  - `PlatformProgressBar.kt`, `PlatformPrivacyToggle.kt`, `PlatformSegmentedTabs.kt`, `PlatformBatchActionBar.kt`, `PlatformCreditCardView.kt`.
- **6. ValidaÃƒÂ§ÃƒÂ£o e Qualidade**:
  - 100% da suÃƒÂ­te `./gradlew testDebugUnitTest` aprovada e build limpo em 4 segundos.

---

## [1.4.0] - 2026-09-29

### Ã°Å¸Å¡â‚¬ RefatoraÃƒÂ§ÃƒÂ£o End-to-End: Wallet 100% Pessoal & GestÃƒÂ£o de Contas e Faturas
- **Contexto 100% Pessoal Unificado**:
  - EliminaÃƒÂ§ÃƒÂ£o definitiva de qualquer perfil corporativo/pessoal (`profileType`, `PESSOAL`/`EMPRESA`), operando com foco exclusivo em finanÃƒÂ§as pessoais.
- **Foco em Contas a Pagar & Previsibilidade**:
  - Cadastro ÃƒÂ¡gil e intuitivo exigindo apenas 3 dados obrigatÃƒÂ³rios: DescriÃƒÂ§ÃƒÂ£o, Valor (em centavos `Long`) e Vencimento.
  - Seletores de Categoria e Item opcionais.
- **Natureza Financeira (ExpenseNature) & Itens de Despesa**:
  - ImplementaÃƒÂ§ÃƒÂ£o dos 4 pilares: `OBRIGATORIO` (custos inegociÃƒÂ¡veis), `NECESSARIO` (manutenÃƒÂ§ÃƒÂ£o essencial), `DESEJA` (estilo de vida e conforto) e `NENHUM` (transitÃƒÂ³rios/nÃƒÂ£o classificados).
  - DepreciaÃƒÂ§ÃƒÂ£o de subcategorias e criaÃƒÂ§ÃƒÂ£o da tabela `expense_items` vinculada diretamente a `categories`, com heranÃƒÂ§a estrita da natureza da categoria mÃƒÂ£e.
  - Nova tela `ExpenseItemsScreen` em ConfiguraÃƒÂ§ÃƒÂµes para listar, pesquisar e cadastrar itens vinculados com prÃƒÂ©-visualizaÃƒÂ§ÃƒÂ£o de natureza.
- **Efeito Cascata no Dashboard em Tempo Real**:
  - Total Previsto no MÃƒÂªs calculado dinamicamente via Flow somando contas avulsas pendentes e faturas de cartÃƒÂ£o abertas/fechadas do perÃƒÂ­odo.
  - Agrupamento semafÃƒÂ³rico de urgÃƒÂªncia: Ã°Å¸â€Â´ Atrasadas, Ã°Å¸Å¸Â¡ Vence Hoje, Ã¢Å¡Âª PrÃƒÂ³ximos 7 Dias e Ã°Å¸Å¸Â¢ Pagas no MÃƒÂªs (colapsÃƒÂ¡vel).
  - Baixa com 1 Toque ("Pagar"): atualiza status para `PAGO` no banco e deduz instantaneamente do total mensal sem recarregar a tela.
- **CartÃƒÂµes de CrÃƒÂ©dito & Extrato de Faturas**:
  - CÃƒÂ¡lculo robusto de limite disponÃƒÂ­vel (`CreditCardCalculator`), fechamento de faturas baseado no dia de corte (`closingDay`) e status da fatura (`ABERTA`, `FECHADA`, `PAGA`).
  - Extrato detalhado de faturas em `CreditCardsScreen` com botÃƒÂ£o de liquidaÃƒÂ§ÃƒÂ£o em 1 toque.
- **PersistÃƒÂªncia Room & MigraÃƒÂ§ÃƒÂ£o**:
  - AtualizaÃƒÂ§ÃƒÂ£o do `PlatformDatabase` para versÃƒÂ£o `7` com `MIGRATION_6_7`.
  - InclusÃƒÂ£o da entidade `TransactionEntity` e DAO `TransactionDao` para rastreamento de transaÃƒÂ§ÃƒÂµes.
  - Tipo alias `AppDatabase = PlatformDatabase` para interoperabilidade.
- **Design System Material 3 (ADR-018)**:
  - AplicaÃƒÂ§ÃƒÂ£o estrita de tokens de cores para Dark Mode e Light Mode.
  - ProibiÃƒÂ§ÃƒÂ£o de cores literais nos componentes; uso estrito de `MaterialTheme.colorScheme`.
  - Tipografia em Title Case (sem ALL CAPS em textos de tela).
  - ÃƒÂcones funcionais do Material Icons substituindo formas abstratas.
- **LanÃƒÂ§amento Focado em Itens (Item-Centric Expenses)**:
  - O Item de Despesa (`selectedItemId`) assumiu o protagonismo absoluto no formulÃƒÂ¡rio: primeiro campo de seleÃƒÂ§ÃƒÂ£o com preview de Natureza Financeira e Categoria.
  - A descriÃƒÂ§ÃƒÂ£o passa a ser campo de "ObservaÃƒÂ§ÃƒÂµes Adicionais (Opcional)", herdando o nome do item selecionado como tÃƒÂ­tulo padrÃƒÂ£o caso nÃƒÂ£o preenchida.
  - ReorganizaÃƒÂ§ÃƒÂ£o do formulÃƒÂ¡rio em 5 blocos harmÃƒÂ´nicos e simÃƒÂ©tricos com cards e divisÃƒÂµes semÃƒÂ¢nticas.
- **Design System Premium Minimalista & Simetria CirÃƒÂºrgica**:
  - Paletas Dark e Light purificadas: Dark Obsidian (`#0A0D14` / `#141923`) e Light Porcelain (`#F8FAFC` / `#FFFFFF`).
  - AnulaÃƒÂ§ÃƒÂ£o estrita de elevaÃƒÂ§ÃƒÂ£o tonal com `surfaceTint = Color.Transparent` em ambos os temas, eliminando 100% qualquer vazamento roxo/lavanda default do Material 3.
  - ErradicaÃƒÂ§ÃƒÂ£o completa de cores literais hardcoded (`Color.White`, `Color.Black`, hexadecimais soltos) em todas as telas, adotando exclusivamente tokens de `Theme.kt`, `Color.kt` e `MaterialTheme.colorScheme`.
  - Anti-gigantismo e harmonia visual: ajuste da tipografia do impacto financeiro na HomeScreen de `headlineLarge` para `titleLarge` semibold/bold.
  - Alinhamentos milimÃƒÂ©tricos com cantos uniformes de 16.dp para cards, 10.dp para botÃƒÂµes/inputs e bordas finas de 1.dp com `outlineVariant`.
  - **PadronizaÃƒÂ§ÃƒÂ£o Global dos FABs (100% Circular)**: UnificaÃƒÂ§ÃƒÂ£o dos FloatingActionButtons de todas as 9 telas financeiras (incluindo `RecurringInstallmentsScreen`) com `shape = CircleShape`, container `primary` e conteÃƒÂºdo `onPrimary`.
  - **UnificaÃƒÂ§ÃƒÂ£o Absoluta de Fluxos de Despesa**: RemoÃƒÂ§ÃƒÂ£o do modal legado `AddBillBottomSheet` em `BillsScreen`. Tanto a tela Inicial quanto a tela de Registros navegam para a mesma `NewExpenseScreen` com o formulÃƒÂ¡rio em 5 blocos harmÃƒÂ´nicos focado em itens.
  - **Componentes Centrais ReutilizÃƒÂ¡veis (`PlatformEmptyState` e `PlatformAvatar`)**:
    - `PlatformEmptyState`: EliminaÃƒÂ§ÃƒÂ£o definitiva de emojis informais (`Ã°Å¸â€™Â³`, `Ã°Å¸â€œË†`) em telas vazias, substituÃƒÂ­dos por containers vetoriais com fundo translÃƒÂºcido a 12% e tipografia proporcional `titleLarge` semibold.
    - `PlatformAvatar`: EliminaÃƒÂ§ÃƒÂ£o de cÃƒÂ³digo duplicado de cÃƒÂ¡lculo de iniciais e renderizaÃƒÂ§ÃƒÂ£o visual homogÃƒÂªnea em `ContactsScreen` e `ContactDetailScreen`.
  - **HigienizaÃƒÂ§ÃƒÂ£o Visual Estrita (Zero AÃƒÂ§ÃƒÂµes Inline NÃƒÂ£o Seguras)**:
    - `ExpenseItemsScreen`: RemoÃƒÂ§ÃƒÂ£o de botÃƒÂµes de exclusÃƒÂ£o inline em cards, substituÃƒÂ­dos por navegaÃƒÂ§ÃƒÂ£o sutil com chevron e `AlertDialog` de confirmaÃƒÂ§ÃƒÂ£o seguro.
    - `BudgetsScreen`: MigraÃƒÂ§ÃƒÂ£o de `BudgetCard` para `PlatformCard` com raio de 16.dp e contorno suave.
    - `CreditCardsScreen`: Ajuste de contraste do botÃƒÂ£o "Liquidar Fatura" (`onPrimary` garantindo legibilidade perfeita no Tema Escuro e Claro).
    - `SettingsScreen`: DistinÃƒÂ§ÃƒÂ£o de ÃƒÂ­cone para Itens de Despesa com `Icons.Default.ShoppingBag`.
    - `AppDrawer`: CorreÃƒÂ§ÃƒÂ£o do divisor de seÃƒÂ§ÃƒÂµes com verificaÃƒÂ§ÃƒÂ£o insensÃƒÂ­vel a maiÃƒÂºsculas/minÃƒÂºsculas (`ignoreCase = true`).
  - **EliminaÃƒÂ§ÃƒÂ£o de CÃƒÂ³digo Zumbi & DepreciaÃƒÂ§ÃƒÂµes**:
    - RemoÃƒÂ§ÃƒÂ£o do arquivo zumbi `DashboardScreen.kt` (-1.073 linhas de cÃƒÂ³digo morto).
    - MigraÃƒÂ§ÃƒÂ£o de todos os ÃƒÂ­cones deprecados para `Icons.AutoMirrored.Filled` (`ReceiptLong` e `TrendingUp`), zerando warnings de compilaÃƒÂ§ÃƒÂ£o.
- **Cobertura de Testes Automatizados**:
  - `NewExpenseValidationTest`: 6 testes automatizados validando campos obrigatÃƒÂ³rios (Item e Contato), heranÃƒÂ§a de natureza, cÃƒÂ¡lculo de parcelas e preenchimento de tÃƒÂ­tulo a partir do item.
  - `CreditCardManagementTest`: ValidaÃƒÂ§ÃƒÂ£o de fechamento, limite e integridade de faturas.
  - `BillCalculationTest`, `ExpenseItemNatureTest`, `InvoiceClosingTest`: Todos aprovados com 100% de sucesso.

---

## [1.3.1] - 2026-09-28

### Ã°Å¸Å½Â¨ Melhorias Visuais & Ergonomia
- **Ajuste de ProporÃƒÂ§ÃƒÂµes dos Cards em ConfiguraÃƒÂ§ÃƒÂµes**:
  - AmpliaÃƒÂ§ÃƒÂ£o da ÃƒÂ¡rea de toque e respiro interno dos blocos de acesso direto a **Contas**, **Formas de Pagamento** e **Categorias** (padding vertical aumentado para 14dp e horizontal para 16dp).
  - ÃƒÂcones com badges ampliados de 32dp para 44dp e cantos arredondados de 12dp para maior destaque visual e clareza de toque.
  - InclusÃƒÂ£o de subtÃƒÂ­tulos descritivos elegantes abaixo de cada tÃƒÂ­tulo para reforÃƒÂ§ar o propÃƒÂ³sito de cada ÃƒÂ¡rea.
  - Tipografia elevada para titleMedium semibold e chevron de navegaÃƒÂ§ÃƒÂ£o mais visÃƒÂ­vel.
  - EspaÃƒÂ§amento vertical entre blocos unificado em 12dp.

---

## [1.3.0] - 2026-09-28

### Ã°Å¸Å¡â‚¬ Novas Funcionalidades
- **Backup e RestauraÃƒÂ§ÃƒÂ£o de Dados Offline**:
  - ExportaÃƒÂ§ÃƒÂ£o completa e versionada do banco de dados em formato JSON compacto com valores inteiros em centavos (`Long`).
  - RestauraÃƒÂ§ÃƒÂ£o atÃƒÂ´mica em lote utilizando transaÃƒÂ§ÃƒÂµes seguras do Room (`database.withTransaction`), garantindo rollback total em caso de falha.
  - IntegraÃƒÂ§ÃƒÂ£o com o *Storage Access Framework (SAF)* nativo do Android (`CreateDocument` e `OpenDocument`) para salvar e restaurar do Google Drive, Documentos, Downloads ou cartÃƒÂ£o SD.
  - Compartilhamento rÃƒÂ¡pido via *ShareSheet* (`FileProvider`) direto para WhatsApp, Telegram ou e-mail.
  - Indicador em tempo real nas ConfiguraÃƒÂ§ÃƒÂµes com a data e hora exata do ÃƒÂºltimo backup efetuado.
  - DiÃƒÂ¡logo de advertÃƒÂªncia e confirmaÃƒÂ§ÃƒÂ£o antes de restauraÃƒÂ§ÃƒÂµes para impedir substituiÃƒÂ§ÃƒÂµes acidentais de dados.

### Ã°Å¸Å½Â¨ HarmonizaÃƒÂ§ÃƒÂ£o Visual & PadrÃƒÂ£o de Detalhes (4 Etapas)
- **AdoÃƒÂ§ÃƒÂ£o Universal do PadrÃƒÂ£o de Detalhes com BottomSheet**:
  - Cards 100% limpos e minimalistas em todo o sistema: remoÃƒÂ§ÃƒÂ£o de botÃƒÂµes inline de lixeira, lÃƒÂ¡pis e acordeÃƒÂµes soltos.
  - Toque no card abre `ModalBottomSheet` dedicado com mÃƒÂ©tricas contextuais, visualizaÃƒÂ§ÃƒÂ£o de vÃƒÂ­nculos (conta, categoria, contato, mÃƒÂ©todo) e aÃƒÂ§ÃƒÂµes rÃƒÂ¡pidas.
  - Menu de 3 pontos (`MoreVert`) no topo do BottomSheet para ediÃƒÂ§ÃƒÂ£o e exclusÃƒÂ£o segura.
  - DiÃƒÂ¡logos de confirmaÃƒÂ§ÃƒÂ£o obrigatÃƒÂ³rios (`AlertDialog`) antes de qualquer exclusÃƒÂ£o definitiva.
  - Seletores de campos com opÃƒÂ§ÃƒÂµes prÃƒÂ©-definidas migrados para menus dropdown nativos (`ExposedDropdownMenuBox`).
  - Seletor de cores aprimorado com indicador visual circular branco na cor ativa.
- **Telas Harmonizadas**:
  - [ManagementScreen.kt]: Formas de Pagamento e Categorias desacopladas com BottomSheet e seletor com indicador.
  - [BillsScreen.kt]: Cards de parcelas sanitizados, tipografia reduzida de 28sp para 16sp bold, chevron sutil e BottomSheet detalhado de liquidaÃƒÂ§ÃƒÂ£o.
  - [GoalsScreen.kt]: Cards de metas limpos, BottomSheet com progresso, aporte rÃƒÂ¡pido e menu de aÃƒÂ§ÃƒÂµes.
  - [BudgetsScreen.kt]: Cards de orÃƒÂ§amento limpos, BottomSheet com comparaÃƒÂ§ÃƒÂ£o teto vs realizado e ajuste de limite.
  - [RecurringInstallmentsScreen.kt]: Cards de contratos enxutos, BottomSheet com detalhes do contrato e listagem completa de parcelas interativas.
  - [ContactDetailScreen.kt]: ExclusÃƒÂ£o segura de contato no menu de 3 pontos com diÃƒÂ¡logo de confirmaÃƒÂ§ÃƒÂ£o.

---

## [1.2.0] - 2026-09-28

### Ã°Å¸Å¡â‚¬ Melhorias & EvoluÃƒÂ§ÃƒÂµes
- **GestÃƒÂ£o Financeira Desacoplada nas ConfiguraÃƒÂ§ÃƒÂµes**: Telas dedicadas e independentes para Contas BancÃƒÂ¡rias, Formas de Pagamento e Categorias, sem acoplamento de abas concorrentes.
- **Menu Lateral Minimalista**: ReduÃƒÂ§ÃƒÂ£o de ruÃƒÂ­do visual, eliminaÃƒÂ§ÃƒÂ£o de subtÃƒÂ­tulos descritivos em todos os itens do drawer, proporÃƒÂ§ÃƒÂµes enxutas e alinhamento com Material 3.
- **Dashboard Financeiro Macro**: Cards inteligentes com visÃƒÂ£o geral de saldo, compromissos do mÃƒÂªs vigente e projeÃƒÂ§ÃƒÂ£o futura de gastos.
- **Recorrentes & Parcelados**: GestÃƒÂ£o centralizada de assinaturas fixas e compras parceladas em andamento com cÃƒÂ¡lculo automÃƒÂ¡tico de amortizaÃƒÂ§ÃƒÂµes.
- **Metas & OrÃƒÂ§amentos**: Controle de reservas financeiras com barra de progresso visual e definiÃƒÂ§ÃƒÂ£o de tetos de gastos mensais por categoria.
- **Cadastro Inteligente de Contatos**: IntegraÃƒÂ§ÃƒÂ£o de busca automÃƒÂ¡tica de CEP via ViaCEP, novos campos estruturados de endereÃƒÂ§o e ediÃƒÂ§ÃƒÂ£o com 1 toque.
- **AutomaÃƒÂ§ÃƒÂ£o de Versionamento**: SincronizaÃƒÂ§ÃƒÂ£o automÃƒÂ¡tica entre o arquivo `VERSION`, `app/build.gradle.kts` e scripts de bump para evitar estagnaÃƒÂ§ÃƒÂ£o de versÃƒÂ£o.

---

## [1.0.0] - 2026-09-26

### Ã°Å¸Å¡â‚¬ Melhorias
- FundaÃƒÂ§ÃƒÂ£o da arquitetura base do aplicativo Android em Clean Architecture (Presentation, Domain, Data, Core).
- ConfiguraÃƒÂ§ÃƒÂ£o do Android Gradle Plugin com Kotlin DSL (`build.gradle.kts` e Version Catalog `libs.versions.toml`).
- IntegraÃƒÂ§ÃƒÂ£o da stack moderna de UI com Jetpack Compose BOM, Material 3 e suporte a Tema Claro e Escuro.
- InjeÃƒÂ§ÃƒÂ£o de dependÃƒÂªncias desacoplada preparada com Hilt.
- ConfiguraÃƒÂ§ÃƒÂ£o de persistÃƒÂªncia local com Room Database e comunicaÃƒÂ§ÃƒÂ£o remota com Retrofit/OkHttp.
- ImplementaÃƒÂ§ÃƒÂ£o de fluxos reativos com Kotlin Coroutines e StateFlow.
- Estrutura completa de governanÃƒÂ§a de IA (`AGENT_RULES.md`, `.ai/` e `.agents/` com skills e regras).
- AutomaÃƒÂ§ÃƒÂ£o de versionamento mÃƒÂ³vel sincronizando `versionCode`, `versionName` e `CHANGELOG.md`.

---


