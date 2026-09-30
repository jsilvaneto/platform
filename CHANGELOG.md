# Changelog - Platform (Android App)

Todas as alteraÃ§Ãµes notÃ¡veis neste projeto serÃ£o documentadas neste arquivo.
O formato Ã© baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/) e este projeto segue [Versionamento SemÃ¢ntico](https://semver.org/lang/pt-BR/).

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

### ðŸš€ Pacote de ExcelÃªncia Operacional & UX (Visual, AÃ§Ãµes, FunÃ§Ãµes e Sistema)

- **1. Visual: TransiÃ§Ãµes de Tela CinemÃ¡ticas e Fluidas (`NavGraph.kt`)**:
  - ImplementaÃ§Ã£o de transiÃ§Ãµes animadas nativas no `NavHost` (`slideIntoContainer` horizontal + `fadeIn`/`fadeOut` interpolados com `tween(260ms)`).
  - ExperiÃªncia visual contÃ­nua sem saltos abruptos de layout ao transitar entre Dashboard, Registros, CartÃµes, Recorrentes e EstatÃ­sticas.

- **2. AÃ§Ãµes (UX): Mecanismo de Desfazer (`Undo`) para Baixa de Contas e Faturas**:
  - IntegraÃ§Ã£o de `SnackbarHost` e canal de efeitos `HomeUiEffect.ShowUndoSnackbar` na `HomeScreen`.
  - Ao quitar uma conta avulsa ou fatura com 1 toque, Ã© exibido um Snackbar com aÃ§Ã£o **"Desfazer"**, permitindo reversÃ£o imediata de cliques acidentais.
  - Adicionado `reopenInvoice` em `FinancialRepositoryImpl.kt` com transaÃ§Ã£o atÃ´mica (`database.withTransaction`), restaurando o status da fatura para `ABERTA` e suas parcelas vinculadas para `PENDING`.

- **3. FunÃ§Ãµes: DuplicaÃ§Ã£o Inteligente de Despesas (`duplicateBillId`)**:
  - Nova opÃ§Ã£o **"Duplicar Despesa"** no menu de contexto (`DropdownMenu`) do BottomSheet de detalhes da parcela (`BillsScreen.kt`).
  - NavegaÃ§Ã£o para `NewExpenseScreen` carregando a despesa original via `SavedStateHandle` no `NewExpenseViewModel`, clonando valor, categoria, item, contato, conta bancÃ¡ria, forma de pagamento e tipo, acelerando cadastros repetitivos.

- **4. Sistema: Lembretes e NotificaÃ§Ãµes Locais de Vencimento Offline**:
  - CriaÃ§Ã£o do canal e gerenciador `DueReminderManager.kt` (`com.platform.app.core.notification`) de alta prioridade.
  - `DueReminderReceiver.kt`: BroadcastReceiver com `@AndroidEntryPoint` e injeÃ§Ã£o Hilt para checagem diÃ¡ria programada via `AlarmManager`.
  - VerificaÃ§Ã£o inteligente na `MainActivity`: Notifica o usuÃ¡rio de forma offline e discreta caso existam contas ou faturas com vencimento no prÃ³prio dia.
  - PermissÃ£o `POST_NOTIFICATIONS` declarada no `AndroidManifest.xml`.

- **5. Qualidade e Testes UnitÃ¡rios**:
  - Novos testes automatizados: `HomeViewModelTest.kt` (aÃ§Ãµes de pagamento, undo de contas e faturas) e `NewExpenseViewModelTest.kt` (prÃ©-carregamento por duplicaÃ§Ã£o).
  - 100% dos testes unitÃ¡rios validados e aprovados com sucesso (`./gradlew testDebugUnitTest`).

---

## [1.4.2] - 2026-09-29

### ðŸ“Š InteligÃªncia Financeira 360Â° & RefatoraÃ§Ã£o da Tela de EstatÃ­sticas
- **VisÃ£o Temporal Integrada em 3 Eixos (Passado, Presente e Futuro)**:
  - NavegaÃ§Ã£o fluida via `PlatformSegmentedTabs` (`Passado`, `Presente`, `Futuro`) com suporte nativo ao **Modo Privacidade** (`PlatformPrivacyToggle`).
- **1. Eixo Passado (HistÃ³rico & TendÃªncias â€” Onde estivemos)**:
  - **MÃ©dia HistÃ³rica Mensal**: Baseline de custo de vida para guiar novos planejamentos.
  - **EvoluÃ§Ã£o dos Ãšltimos 6 Meses**: Barras comparativas mÃªs a mÃªs de valor devido vs valor liquidado, com taxa de quitaÃ§Ã£o individual e atalho de salto direto para o mÃªs.
  - **Comparativo com o MÃªs Anterior**: VariaÃ§Ã£o em R$ e % com badges semafÃ³ricos de alta/baixa.
  - **Picos e Vales HistÃ³ricos**: IdentificaÃ§Ã£o do mÃªs mais pesado vs mÃªs mais econÃ´mico.
  - **Ranking de Top DestinatÃ¡rios / Fornecedores**: Contatos que mais absorveram recursos.
- **2. Eixo Presente (Raio-X do MÃªs Selecionado â€” Onde estamos)**:
  - **Hero Card de ExecuÃ§Ã£o OrÃ§amentÃ¡ria**: Total devido, valor pago, pendente e atrasado com termÃ´metro semafÃ³rico `PlatformProgressBar`.
  - **Rigidez OrÃ§amentÃ¡ria (Regra 50/30/20 & Natureza dos Gastos)**: ClassificaÃ§Ã£o em `ObrigatÃ³rio`, `NecessÃ¡rio`, `Deseja` e `Nenhum` com card de diagnÃ³stico estratÃ©gico inteligente (*Alerta de OrÃ§amento Engessado* caso ObrigatÃ³rio > 55%).
  - **Meio de LiquidaÃ§Ã£o & CrÃ©dito**: Barra bifurcada e percentuais de exposiÃ§Ã£o entre CartÃ£o de CrÃ©dito vs DÃ©bito/Pix/Dinheiro.
  - **Top Categorias & Contas BancÃ¡rias**: DistribuiÃ§Ã£o visual dos gastos e concentraÃ§Ã£o institucional.
- **3. Eixo Futuro (Previsibilidade & Tomada de DecisÃ£o â€” Para onde vamos)**:
  - **Curva de DesoneraÃ§Ã£o (PrÃ³ximos 6 Meses)**: EvoluÃ§Ã£o decrescente dos desembolsos com destaque para o mÃªs de maior pico e o mÃªs de maior folga financeira.
  - **DesoneraÃ§Ã£o & TÃ©rmino de Parcelas**: Cronograma de compras parceladas ativas que chegam ao fim, com cÃ¡lculo de alÃ­vio mensal gerado (*"+R$ X/mÃªs livre"*).
  - **Cockpit de Tomada de DecisÃ£o (Smart Advisor)**: Consultoria contextual em tempo real para responder *"Posso assumir uma nova compra parcelada agora?"*.
- **4. DomÃ­nio & Testes Automatizados**:
  - Modelos enriquecidos em `FinancialDashboardMetrics.kt` e agregaÃ§Ã£o performÃ¡tica em `GetFinancialDashboardUseCase.kt`.
  - 100% da suÃ­te unitÃ¡ria aprovada (`./gradlew testDebugUnitTest`).

---

## [1.4.1] - 2026-09-29

### ðŸ’Ž Grande Update das 4 Telas Centrais (Obsidian & Porcelain Minimalist)
- **1. Tela Inicial (`HomeScreen`)**:
  - **Executive Hero Card**: AdiÃ§Ã£o de barra de progresso horizontal (`PlatformProgressBar`) com cantos arredondados indicando o percentual de quitaÃ§Ã£o do mÃªs (ex: *"68% quitado"*) e mÃ©tricas de JÃ¡ Quitado e Total Geral.
  - **Modo Privacidade (`PlatformPrivacyToggle`)**: Alternador com Ã­cone de olho para mascarar valores confidenciais (`R$ â€¢â€¢â€¢â€¢â€¢â€¢`) em locais pÃºblicos.
  - **Seletor de MÃªs RÃ¡pido em Grade (`MonthPickerBottomSheet`)**: Grade visual de meses e seletor de ano direto no tÃ­tulo do mÃªs, eliminando cliques repetitivos em setas.
  - **Empty State Triunfante (`MonthVictoryCard`)**: Card comemorativo esmeralda quando todas as contas do mÃªs estiverem 100% quitadas.
  - **Micro-interaÃ§Ãµes HÃ¡pticas**: VibraÃ§Ã£o suave ao efetuar a baixa de contas na lista.
- **2. Tela de Registros (`BillsScreen`)**:
  - **Segmented Tabs no Topo (`PlatformSegmentedTabs`)**: DivisÃ£o em 3 abas especializadas (`A Pagar`, `Pagas`, `Todas` com contadores numÃ©ricos).
  - **Banner de Totais Filtrados**: Resumo instantÃ¢neo do valor total e quantidade de registros visÃ­veis.
  - **AÃ§Ãµes em Lote (`PlatformBatchActionBar`)**: Modo de seleÃ§Ã£o mÃºltipla (toque longo ou botÃ£o no TopBar) para liquidar ou excluir vÃ¡rias contas com 1 confirmaÃ§Ã£o.
- **3. Tela de Recorrentes & Parcelados (`RecurringInstallmentsScreen`)**:
  - **SeparaÃ§Ã£o por Abas**: DivisÃ£o nÃ­tida entre `Compras Parceladas` (amortizaÃ§Ã£o com data final) e `Assinaturas & Custos Fixos` (compromisso mensal contÃ­nuo).
  - **Card de AmortizaÃ§Ã£o Estilo Financiamento**: Progresso visual com `PlatformProgressBar`, projeÃ§Ã£o de data de quitaÃ§Ã£o final (*"TÃ©rmino em MÃªs/Ano"*) e saldo devedor restante.
  - **Cronograma de Parcelas**: Acompanhamento detalhado de cada parcela com baixa direta.
  - **ProjeÃ§Ã£o Anual de Assinaturas**: ExibiÃ§Ã£o do custo anual consolidado (*"R$ Y/ano"*).
- **4. Tela de CartÃµes de CrÃ©dito (`CreditCardsScreen`)**:
  - **Design Virtual Apple Card / Revolut (`PlatformCreditCardView`)**: ProporÃ§Ã£o bancÃ¡ria exata (`1.586f`), chip EMV vetorial metÃ¡lico, gradiente acetinado e termÃ´metro de limite inteligente semafÃ³rico integrado.
  - **Ciclo de 3 Faturas**: AlternÃ¢ncia de abas entre `Aberta`, `Fechadas`, `Pagas` e `Todas`.
  - **Destaque do Melhor Dia de Compra**: OrientaÃ§Ã£o visual do dia de corte para compras com atÃ© 40 dias de prazo.
  - **Atalho de Nova Compra**: BotÃ£o direto no extrato para lanÃ§ar despesa prÃ©-selecionando o cartÃ£o.
- **5. Novos Componentes ReutilizÃ¡veis**:
  - `PlatformProgressBar.kt`, `PlatformPrivacyToggle.kt`, `PlatformSegmentedTabs.kt`, `PlatformBatchActionBar.kt`, `PlatformCreditCardView.kt`.
- **6. ValidaÃ§Ã£o e Qualidade**:
  - 100% da suÃ­te `./gradlew testDebugUnitTest` aprovada e build limpo em 4 segundos.

---

## [1.4.0] - 2026-09-29

### ðŸš€ RefatoraÃ§Ã£o End-to-End: Wallet 100% Pessoal & GestÃ£o de Contas e Faturas
- **Contexto 100% Pessoal Unificado**:
  - EliminaÃ§Ã£o definitiva de qualquer perfil corporativo/pessoal (`profileType`, `PESSOAL`/`EMPRESA`), operando com foco exclusivo em finanÃ§as pessoais.
- **Foco em Contas a Pagar & Previsibilidade**:
  - Cadastro Ã¡gil e intuitivo exigindo apenas 3 dados obrigatÃ³rios: DescriÃ§Ã£o, Valor (em centavos `Long`) e Vencimento.
  - Seletores de Categoria e Item opcionais.
- **Natureza Financeira (ExpenseNature) & Itens de Despesa**:
  - ImplementaÃ§Ã£o dos 4 pilares: `OBRIGATORIO` (custos inegociÃ¡veis), `NECESSARIO` (manutenÃ§Ã£o essencial), `DESEJA` (estilo de vida e conforto) e `NENHUM` (transitÃ³rios/nÃ£o classificados).
  - DepreciaÃ§Ã£o de subcategorias e criaÃ§Ã£o da tabela `expense_items` vinculada diretamente a `categories`, com heranÃ§a estrita da natureza da categoria mÃ£e.
  - Nova tela `ExpenseItemsScreen` em ConfiguraÃ§Ãµes para listar, pesquisar e cadastrar itens vinculados com prÃ©-visualizaÃ§Ã£o de natureza.
- **Efeito Cascata no Dashboard em Tempo Real**:
  - Total Previsto no MÃªs calculado dinamicamente via Flow somando contas avulsas pendentes e faturas de cartÃ£o abertas/fechadas do perÃ­odo.
  - Agrupamento semafÃ³rico de urgÃªncia: ðŸ”´ Atrasadas, ðŸŸ¡ Vence Hoje, âšª PrÃ³ximos 7 Dias e ðŸŸ¢ Pagas no MÃªs (colapsÃ¡vel).
  - Baixa com 1 Toque ("Pagar"): atualiza status para `PAGO` no banco e deduz instantaneamente do total mensal sem recarregar a tela.
- **CartÃµes de CrÃ©dito & Extrato de Faturas**:
  - CÃ¡lculo robusto de limite disponÃ­vel (`CreditCardCalculator`), fechamento de faturas baseado no dia de corte (`closingDay`) e status da fatura (`ABERTA`, `FECHADA`, `PAGA`).
  - Extrato detalhado de faturas em `CreditCardsScreen` com botÃ£o de liquidaÃ§Ã£o em 1 toque.
- **PersistÃªncia Room & MigraÃ§Ã£o**:
  - AtualizaÃ§Ã£o do `PlatformDatabase` para versÃ£o `7` com `MIGRATION_6_7`.
  - InclusÃ£o da entidade `TransactionEntity` e DAO `TransactionDao` para rastreamento de transaÃ§Ãµes.
  - Tipo alias `AppDatabase = PlatformDatabase` para interoperabilidade.
- **Design System Material 3 (ADR-018)**:
  - AplicaÃ§Ã£o estrita de tokens de cores para Dark Mode e Light Mode.
  - ProibiÃ§Ã£o de cores literais nos componentes; uso estrito de `MaterialTheme.colorScheme`.
  - Tipografia em Title Case (sem ALL CAPS em textos de tela).
  - Ãcones funcionais do Material Icons substituindo formas abstratas.
- **LanÃ§amento Focado em Itens (Item-Centric Expenses)**:
  - O Item de Despesa (`selectedItemId`) assumiu o protagonismo absoluto no formulÃ¡rio: primeiro campo de seleÃ§Ã£o com preview de Natureza Financeira e Categoria.
  - A descriÃ§Ã£o passa a ser campo de "ObservaÃ§Ãµes Adicionais (Opcional)", herdando o nome do item selecionado como tÃ­tulo padrÃ£o caso nÃ£o preenchida.
  - ReorganizaÃ§Ã£o do formulÃ¡rio em 5 blocos harmÃ´nicos e simÃ©tricos com cards e divisÃµes semÃ¢nticas.
- **Design System Premium Minimalista & Simetria CirÃºrgica**:
  - Paletas Dark e Light purificadas: Dark Obsidian (`#0A0D14` / `#141923`) e Light Porcelain (`#F8FAFC` / `#FFFFFF`).
  - AnulaÃ§Ã£o estrita de elevaÃ§Ã£o tonal com `surfaceTint = Color.Transparent` em ambos os temas, eliminando 100% qualquer vazamento roxo/lavanda default do Material 3.
  - ErradicaÃ§Ã£o completa de cores literais hardcoded (`Color.White`, `Color.Black`, hexadecimais soltos) em todas as telas, adotando exclusivamente tokens de `Theme.kt`, `Color.kt` e `MaterialTheme.colorScheme`.
  - Anti-gigantismo e harmonia visual: ajuste da tipografia do impacto financeiro na HomeScreen de `headlineLarge` para `titleLarge` semibold/bold.
  - Alinhamentos milimÃ©tricos com cantos uniformes de 16.dp para cards, 10.dp para botÃµes/inputs e bordas finas de 1.dp com `outlineVariant`.
  - **PadronizaÃ§Ã£o Global dos FABs (100% Circular)**: UnificaÃ§Ã£o dos FloatingActionButtons de todas as 9 telas financeiras (incluindo `RecurringInstallmentsScreen`) com `shape = CircleShape`, container `primary` e conteÃºdo `onPrimary`.
  - **UnificaÃ§Ã£o Absoluta de Fluxos de Despesa**: RemoÃ§Ã£o do modal legado `AddBillBottomSheet` em `BillsScreen`. Tanto a tela Inicial quanto a tela de Registros navegam para a mesma `NewExpenseScreen` com o formulÃ¡rio em 5 blocos harmÃ´nicos focado em itens.
  - **Componentes Centrais ReutilizÃ¡veis (`PlatformEmptyState` e `PlatformAvatar`)**:
    - `PlatformEmptyState`: EliminaÃ§Ã£o definitiva de emojis informais (`ðŸ’³`, `ðŸ“ˆ`) em telas vazias, substituÃ­dos por containers vetoriais com fundo translÃºcido a 12% e tipografia proporcional `titleLarge` semibold.
    - `PlatformAvatar`: EliminaÃ§Ã£o de cÃ³digo duplicado de cÃ¡lculo de iniciais e renderizaÃ§Ã£o visual homogÃªnea em `ContactsScreen` e `ContactDetailScreen`.
  - **HigienizaÃ§Ã£o Visual Estrita (Zero AÃ§Ãµes Inline NÃ£o Seguras)**:
    - `ExpenseItemsScreen`: RemoÃ§Ã£o de botÃµes de exclusÃ£o inline em cards, substituÃ­dos por navegaÃ§Ã£o sutil com chevron e `AlertDialog` de confirmaÃ§Ã£o seguro.
    - `BudgetsScreen`: MigraÃ§Ã£o de `BudgetCard` para `PlatformCard` com raio de 16.dp e contorno suave.
    - `CreditCardsScreen`: Ajuste de contraste do botÃ£o "Liquidar Fatura" (`onPrimary` garantindo legibilidade perfeita no Tema Escuro e Claro).
    - `SettingsScreen`: DistinÃ§Ã£o de Ã­cone para Itens de Despesa com `Icons.Default.ShoppingBag`.
    - `AppDrawer`: CorreÃ§Ã£o do divisor de seÃ§Ãµes com verificaÃ§Ã£o insensÃ­vel a maiÃºsculas/minÃºsculas (`ignoreCase = true`).
  - **EliminaÃ§Ã£o de CÃ³digo Zumbi & DepreciaÃ§Ãµes**:
    - RemoÃ§Ã£o do arquivo zumbi `DashboardScreen.kt` (-1.073 linhas de cÃ³digo morto).
    - MigraÃ§Ã£o de todos os Ã­cones deprecados para `Icons.AutoMirrored.Filled` (`ReceiptLong` e `TrendingUp`), zerando warnings de compilaÃ§Ã£o.
- **Cobertura de Testes Automatizados**:
  - `NewExpenseValidationTest`: 6 testes automatizados validando campos obrigatÃ³rios (Item e Contato), heranÃ§a de natureza, cÃ¡lculo de parcelas e preenchimento de tÃ­tulo a partir do item.
  - `CreditCardManagementTest`: ValidaÃ§Ã£o de fechamento, limite e integridade de faturas.
  - `BillCalculationTest`, `ExpenseItemNatureTest`, `InvoiceClosingTest`: Todos aprovados com 100% de sucesso.

---

## [1.3.1] - 2026-09-28

### ðŸŽ¨ Melhorias Visuais & Ergonomia
- **Ajuste de ProporÃ§Ãµes dos Cards em ConfiguraÃ§Ãµes**:
  - AmpliaÃ§Ã£o da Ã¡rea de toque e respiro interno dos blocos de acesso direto a **Contas**, **Formas de Pagamento** e **Categorias** (padding vertical aumentado para 14dp e horizontal para 16dp).
  - Ãcones com badges ampliados de 32dp para 44dp e cantos arredondados de 12dp para maior destaque visual e clareza de toque.
  - InclusÃ£o de subtÃ­tulos descritivos elegantes abaixo de cada tÃ­tulo para reforÃ§ar o propÃ³sito de cada Ã¡rea.
  - Tipografia elevada para titleMedium semibold e chevron de navegaÃ§Ã£o mais visÃ­vel.
  - EspaÃ§amento vertical entre blocos unificado em 12dp.

---

## [1.3.0] - 2026-09-28

### ðŸš€ Novas Funcionalidades
- **Backup e RestauraÃ§Ã£o de Dados Offline**:
  - ExportaÃ§Ã£o completa e versionada do banco de dados em formato JSON compacto com valores inteiros em centavos (`Long`).
  - RestauraÃ§Ã£o atÃ´mica em lote utilizando transaÃ§Ãµes seguras do Room (`database.withTransaction`), garantindo rollback total em caso de falha.
  - IntegraÃ§Ã£o com o *Storage Access Framework (SAF)* nativo do Android (`CreateDocument` e `OpenDocument`) para salvar e restaurar do Google Drive, Documentos, Downloads ou cartÃ£o SD.
  - Compartilhamento rÃ¡pido via *ShareSheet* (`FileProvider`) direto para WhatsApp, Telegram ou e-mail.
  - Indicador em tempo real nas ConfiguraÃ§Ãµes com a data e hora exata do Ãºltimo backup efetuado.
  - DiÃ¡logo de advertÃªncia e confirmaÃ§Ã£o antes de restauraÃ§Ãµes para impedir substituiÃ§Ãµes acidentais de dados.

### ðŸŽ¨ HarmonizaÃ§Ã£o Visual & PadrÃ£o de Detalhes (4 Etapas)
- **AdoÃ§Ã£o Universal do PadrÃ£o de Detalhes com BottomSheet**:
  - Cards 100% limpos e minimalistas em todo o sistema: remoÃ§Ã£o de botÃµes inline de lixeira, lÃ¡pis e acordeÃµes soltos.
  - Toque no card abre `ModalBottomSheet` dedicado com mÃ©tricas contextuais, visualizaÃ§Ã£o de vÃ­nculos (conta, categoria, contato, mÃ©todo) e aÃ§Ãµes rÃ¡pidas.
  - Menu de 3 pontos (`MoreVert`) no topo do BottomSheet para ediÃ§Ã£o e exclusÃ£o segura.
  - DiÃ¡logos de confirmaÃ§Ã£o obrigatÃ³rios (`AlertDialog`) antes de qualquer exclusÃ£o definitiva.
  - Seletores de campos com opÃ§Ãµes prÃ©-definidas migrados para menus dropdown nativos (`ExposedDropdownMenuBox`).
  - Seletor de cores aprimorado com indicador visual circular branco na cor ativa.
- **Telas Harmonizadas**:
  - [ManagementScreen.kt]: Formas de Pagamento e Categorias desacopladas com BottomSheet e seletor com indicador.
  - [BillsScreen.kt]: Cards de parcelas sanitizados, tipografia reduzida de 28sp para 16sp bold, chevron sutil e BottomSheet detalhado de liquidaÃ§Ã£o.
  - [GoalsScreen.kt]: Cards de metas limpos, BottomSheet com progresso, aporte rÃ¡pido e menu de aÃ§Ãµes.
  - [BudgetsScreen.kt]: Cards de orÃ§amento limpos, BottomSheet com comparaÃ§Ã£o teto vs realizado e ajuste de limite.
  - [RecurringInstallmentsScreen.kt]: Cards de contratos enxutos, BottomSheet com detalhes do contrato e listagem completa de parcelas interativas.
  - [ContactDetailScreen.kt]: ExclusÃ£o segura de contato no menu de 3 pontos com diÃ¡logo de confirmaÃ§Ã£o.

---

## [1.2.0] - 2026-09-28

### ðŸš€ Melhorias & EvoluÃ§Ãµes
- **GestÃ£o Financeira Desacoplada nas ConfiguraÃ§Ãµes**: Telas dedicadas e independentes para Contas BancÃ¡rias, Formas de Pagamento e Categorias, sem acoplamento de abas concorrentes.
- **Menu Lateral Minimalista**: ReduÃ§Ã£o de ruÃ­do visual, eliminaÃ§Ã£o de subtÃ­tulos descritivos em todos os itens do drawer, proporÃ§Ãµes enxutas e alinhamento com Material 3.
- **Dashboard Financeiro Macro**: Cards inteligentes com visÃ£o geral de saldo, compromissos do mÃªs vigente e projeÃ§Ã£o futura de gastos.
- **Recorrentes & Parcelados**: GestÃ£o centralizada de assinaturas fixas e compras parceladas em andamento com cÃ¡lculo automÃ¡tico de amortizaÃ§Ãµes.
- **Metas & OrÃ§amentos**: Controle de reservas financeiras com barra de progresso visual e definiÃ§Ã£o de tetos de gastos mensais por categoria.
- **Cadastro Inteligente de Contatos**: IntegraÃ§Ã£o de busca automÃ¡tica de CEP via ViaCEP, novos campos estruturados de endereÃ§o e ediÃ§Ã£o com 1 toque.
- **AutomaÃ§Ã£o de Versionamento**: SincronizaÃ§Ã£o automÃ¡tica entre o arquivo `VERSION`, `app/build.gradle.kts` e scripts de bump para evitar estagnaÃ§Ã£o de versÃ£o.

---

## [1.0.0] - 2026-09-26

### ðŸš€ Melhorias
- FundaÃ§Ã£o da arquitetura base do aplicativo Android em Clean Architecture (Presentation, Domain, Data, Core).
- ConfiguraÃ§Ã£o do Android Gradle Plugin com Kotlin DSL (`build.gradle.kts` e Version Catalog `libs.versions.toml`).
- IntegraÃ§Ã£o da stack moderna de UI com Jetpack Compose BOM, Material 3 e suporte a Tema Claro e Escuro.
- InjeÃ§Ã£o de dependÃªncias desacoplada preparada com Hilt.
- ConfiguraÃ§Ã£o de persistÃªncia local com Room Database e comunicaÃ§Ã£o remota com Retrofit/OkHttp.
- ImplementaÃ§Ã£o de fluxos reativos com Kotlin Coroutines e StateFlow.
- Estrutura completa de governanÃ§a de IA (`AGENT_RULES.md`, `.ai/` e `.agents/` com skills e regras).
- AutomaÃ§Ã£o de versionamento mÃ³vel sincronizando `versionCode`, `versionName` e `CHANGELOG.md`.

---

