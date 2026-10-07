# Changelog - Platform (Android App)

Todas as alteraÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Âµes notÃƒÆ’Ã‚Â¡veis neste projeto serÃƒÆ’Ã‚Â£o documentadas neste arquivo.
O formato ÃƒÆ’Ã‚Â© baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/) e este projeto segue [Versionamento SemÃƒÆ’Ã‚Â¢ntico](https://semver.org/lang/pt-BR/).

## [1.5.0] - 2026-10-07

### 🎯 Rigidez Orçamentária Real 50-30-20 & Integração de Metas (Poupança)

- **Integração Holística de Metas Financeiras como Poupança (`GetFinancialDashboardUseCase`)**:
  - Injeção de `GoalRepository` no caso de uso do dashboard financeiro.
  - Apuração reativa dos aportes mensais em metas através do novo método `goalRepository.getMonthlyContribution(startOfMonth, endOfMonth)`.
  - Base de cálculo orçamentária unificada: $\text{Total Orçado} = \text{Total Contas do Mês} + \text{Aportes em Metas}$.
  - Distribuição percentual das 4 naturezas (`OBRIGATORIO`, `NECESSARIO`, `DESEJA`, `NENHUM`) e da Poupança agora somam exatamente 100% da capacidade orçamentária.
- **Motor de Diagnóstico Multifaixas de Rigidez (`BudgetRigidityCalculator`)**:
  - Eliminação definitiva do falso diagnóstico de "Excelente" quando Deseja e Poupança estiverem zerados (`SOBRECARREGADO`: 100% comprometido com despesas básicas, sem margem para estilo de vida ou reservas).
  - 8 estados e faixas estritas de classificação: `VAZIO`, `SOBRECARREGADO`, `ENGESSADO`, `ESTILO_DE_VIDA_ELEVADO`, `SEM_POUPANCA`, `NAO_CLASSIFICADO`, `EQUILIBRADO` e `EXCELENTE`.
  - Critério rigoroso para selo "Excelente": Essenciais $\le 60\%$ (Obrigatório $\le 50\%$), Desejos entre $10\%$ e $35\%$, Poupança $\ge 15\%$ e Não Classificados $\le 10\%$.
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

### ðŸ’³ Baixa de Pagamento Retroativa & Data Real de Pagamento (`actualPaymentDate`)

- **Suporte a Data Real de Pagamento no Modelo e Banco**:
  - AdiÃ§Ã£o do campo opcional `actualPaymentDate: Long?` no modelo de domÃ­nio `BillInstallment` e na entidade Room `BillInstallmentEntity`.
  - MigraÃ§Ã£o de banco de dados Room `MIGRATION_9_10` (`PlatformDatabase` v10) aplicando `ALTER TABLE bill_installments ADD COLUMN actualPaymentDate INTEGER DEFAULT NULL`.
  - MigraÃ§Ã£o registrada e injetada no `AppModule`.
- **DiÃ¡logo Elegante de Baixa de Pagamento (`ConfirmPaymentDialog`)**:
  - Novo diÃ¡logo nativo Material 3 permitindo ao usuÃ¡rio informar a data real do pagamento na baixa da conta.
  - Atalhos instantÃ¢neos de 1 toque: "Hoje" e "No Vencimento".
  - Seletor de data (`DatePickerDialog`) permitindo conciliaÃ§Ãµes e baixas retroativas ou programadas com total flexibilidade.
  - Integrado no fluxo de quitaÃ§Ã£o em `BillsScreen`, `HomeScreen`, `RecurringInstallmentsScreen`, `ContactDetailScreen` e `EditInstallmentBottomSheet`.
- **CÃ¡lculo Preciso da Taxa de Pagamentos Pontuais (`GetFinancialDashboardUseCase`)**:
  - O cÃ¡lculo da mÃ©trica `onTimePaymentRate` agora compara a data real de pagamento (`actualPaymentDate`), com fallback para `paidAt` e `dueDate`, contra o vencimento da parcela.
  - Baixas retroativas de contas pagas no prazo nÃ£o sÃ£o mais classificadas erroneamente como atrasadas.
- **SuÃ­te de Testes UnitÃ¡rios**:
  - Testes unitÃ¡rios atualizados e adicionados em `ToggleInstallmentPaymentUseCaseTest`, `GetFinancialDashboardUseCaseTest`, `BillsViewModelTest` e `HomeViewModelTest`, cobrindo quitaÃ§Ã£o retroativa no prazo, quitaÃ§Ã£o retroativa em atraso, fallback e desmarcaÃ§Ã£o de pagamento.
- **GovernanÃ§a e Arquitetura**:
  - CriaÃ§Ã£o da [ADR 020: Data Real de Pagamento, Baixa Retroativa e MigraÃ§Ã£o Room v10](.ai/DECISIONS/020-data-real-pagamento-e-baixa-retroativa.md).

---

## [1.4.4] - 2026-09-29

### Ã¢Å“ÂÃ¯Â¸Â GestÃƒÂ£o e EdiÃƒÂ§ÃƒÂ£o de Itens de Despesa (`ExpenseItemsScreen` & `NewExpenseScreen`)

- **EdiÃƒÂ§ÃƒÂ£o Completa de Itens de Despesa**:
  - ImplementaÃƒÂ§ÃƒÂ£o do fluxo de ediÃƒÂ§ÃƒÂ£o reativo para itens de despesa existentes via `AddEditExpenseItemBottomSheet`, permitindo renomear o item ou alterar sua categoria vinculada com heranÃƒÂ§a instantÃƒÂ¢nea da nova natureza financeira.
- **HarmonizaÃƒÂ§ÃƒÂ£o com o PadrÃƒÂ£o Universal de Detalhes (`ExpenseItemDetailBottomSheet`)**:
  - O toque direto em qualquer card de item agora abre um `ModalBottomSheet` dedicado com avatar na cor da categoria, nome do item, identificadores, chip de natureza e menu contextual de 3 pontos (`MoreVert`).
  - Acesso direto ÃƒÂ  aÃƒÂ§ÃƒÂ£o "Editar Item" via botÃƒÂ£o de largura total na base do sheet e via menu de 3 pontos.
  - AÃƒÂ§ÃƒÂ£o de exclusÃƒÂ£o mantida de forma segura com diÃƒÂ¡logo de confirmaÃƒÂ§ÃƒÂ£o `AlertDialog`.
- **Atalhos e Acesso RÃƒÂ¡pido em Nova Despesa (`NewExpenseScreen`)**:
  - Adicionada opÃƒÂ§ÃƒÂ£o direta "Gerenciar Itens (Criar / Editar)" no menu dropdown de seleÃƒÂ§ÃƒÂ£o de itens e botÃƒÂ£o de ediÃƒÂ§ÃƒÂ£o rÃƒÂ¡pida de 1 toque no chip de natureza herdada, permitindo ajustar itens sem perder o contexto do lanÃƒÂ§amento.
- **SuÃƒÂ­te de Testes Automatizados**:
  - Nova suÃƒÂ­te de testes unitÃƒÂ¡rios `ExpenseItemsViewModelTest.kt` validando carga inicial, filtro de pesquisa, filtro por categoria, criaÃƒÂ§ÃƒÂ£o e salvamento de itens editados (preservaÃƒÂ§ÃƒÂ£o de ID) e exclusÃƒÂ£o.

---

## [1.4.3] - 2026-09-29

### ÃƒÂ°Ã…Â¸Ã…Â¡Ã¢â€šÂ¬ Pacote de ExcelÃƒÆ’Ã‚Âªncia Operacional & UX (Visual, AÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Âµes, FunÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Âµes e Sistema)

- **1. Visual: TransiÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Âµes de Tela CinemÃƒÆ’Ã‚Â¡ticas e Fluidas (`NavGraph.kt`)**:
  - ImplementaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o de transiÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Âµes animadas nativas no `NavHost` (`slideIntoContainer` horizontal + `fadeIn`/`fadeOut` interpolados com `tween(260ms)`).
  - ExperiÃƒÆ’Ã‚Âªncia visual contÃƒÆ’Ã‚Â­nua sem saltos abruptos de layout ao transitar entre Dashboard, Registros, CartÃƒÆ’Ã‚Âµes, Recorrentes e EstatÃƒÆ’Ã‚Â­sticas.

- **2. AÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Âµes (UX): Mecanismo de Desfazer (`Undo`) para Baixa de Contas e Faturas**:
  - IntegraÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o de `SnackbarHost` e canal de efeitos `HomeUiEffect.ShowUndoSnackbar` na `HomeScreen`.
  - Ao quitar uma conta avulsa ou fatura com 1 toque, ÃƒÆ’Ã‚Â© exibido um Snackbar com aÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o **"Desfazer"**, permitindo reversÃƒÆ’Ã‚Â£o imediata de cliques acidentais.
  - Adicionado `reopenInvoice` em `FinancialRepositoryImpl.kt` com transaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o atÃƒÆ’Ã‚Â´mica (`database.withTransaction`), restaurando o status da fatura para `ABERTA` e suas parcelas vinculadas para `PENDING`.

- **3. FunÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Âµes: DuplicaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o Inteligente de Despesas (`duplicateBillId`)**:
  - Nova opÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o **"Duplicar Despesa"** no menu de contexto (`DropdownMenu`) do BottomSheet de detalhes da parcela (`BillsScreen.kt`).
  - NavegaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o para `NewExpenseScreen` carregando a despesa original via `SavedStateHandle` no `NewExpenseViewModel`, clonando valor, categoria, item, contato, conta bancÃƒÆ’Ã‚Â¡ria, forma de pagamento e tipo, acelerando cadastros repetitivos.

- **4. Sistema: Lembretes e NotificaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Âµes Locais de Vencimento Offline**:
  - CriaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o do canal e gerenciador `DueReminderManager.kt` (`com.platform.app.core.notification`) de alta prioridade.
  - `DueReminderReceiver.kt`: BroadcastReceiver com `@AndroidEntryPoint` e injeÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o Hilt para checagem diÃƒÆ’Ã‚Â¡ria programada via `AlarmManager`.
  - VerificaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o inteligente na `MainActivity`: Notifica o usuÃƒÆ’Ã‚Â¡rio de forma offline e discreta caso existam contas ou faturas com vencimento no prÃƒÆ’Ã‚Â³prio dia.
  - PermissÃƒÆ’Ã‚Â£o `POST_NOTIFICATIONS` declarada no `AndroidManifest.xml`.

- **5. Qualidade e Testes UnitÃƒÆ’Ã‚Â¡rios**:
  - Novos testes automatizados: `HomeViewModelTest.kt` (aÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Âµes de pagamento, undo de contas e faturas) e `NewExpenseViewModelTest.kt` (prÃƒÆ’Ã‚Â©-carregamento por duplicaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o).
  - 100% dos testes unitÃƒÆ’Ã‚Â¡rios validados e aprovados com sucesso (`./gradlew testDebugUnitTest`).

---

## [1.4.2] - 2026-09-29

### ÃƒÂ°Ã…Â¸Ã¢â‚¬Å“Ã…Â  InteligÃƒÆ’Ã‚Âªncia Financeira 360Ãƒâ€šÃ‚Â° & RefatoraÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o da Tela de EstatÃƒÆ’Ã‚Â­sticas
- **VisÃƒÆ’Ã‚Â£o Temporal Integrada em 3 Eixos (Passado, Presente e Futuro)**:
  - NavegaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o fluida via `PlatformSegmentedTabs` (`Passado`, `Presente`, `Futuro`) com suporte nativo ao **Modo Privacidade** (`PlatformPrivacyToggle`).
- **1. Eixo Passado (HistÃƒÆ’Ã‚Â³rico & TendÃƒÆ’Ã‚Âªncias ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Onde estivemos)**:
  - **MÃƒÆ’Ã‚Â©dia HistÃƒÆ’Ã‚Â³rica Mensal**: Baseline de custo de vida para guiar novos planejamentos.
  - **EvoluÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o dos ÃƒÆ’Ã…Â¡ltimos 6 Meses**: Barras comparativas mÃƒÆ’Ã‚Âªs a mÃƒÆ’Ã‚Âªs de valor devido vs valor liquidado, com taxa de quitaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o individual e atalho de salto direto para o mÃƒÆ’Ã‚Âªs.
  - **Comparativo com o MÃƒÆ’Ã‚Âªs Anterior**: VariaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o em R$ e % com badges semafÃƒÆ’Ã‚Â³ricos de alta/baixa.
  - **Picos e Vales HistÃƒÆ’Ã‚Â³ricos**: IdentificaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o do mÃƒÆ’Ã‚Âªs mais pesado vs mÃƒÆ’Ã‚Âªs mais econÃƒÆ’Ã‚Â´mico.
  - **Ranking de Top DestinatÃƒÆ’Ã‚Â¡rios / Fornecedores**: Contatos que mais absorveram recursos.
- **2. Eixo Presente (Raio-X do MÃƒÆ’Ã‚Âªs Selecionado ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Onde estamos)**:
  - **Hero Card de ExecuÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o OrÃƒÆ’Ã‚Â§amentÃƒÆ’Ã‚Â¡ria**: Total devido, valor pago, pendente e atrasado com termÃƒÆ’Ã‚Â´metro semafÃƒÆ’Ã‚Â³rico `PlatformProgressBar`.
  - **Rigidez OrÃƒÆ’Ã‚Â§amentÃƒÆ’Ã‚Â¡ria (Regra 50/30/20 & Natureza dos Gastos)**: ClassificaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o em `ObrigatÃƒÆ’Ã‚Â³rio`, `NecessÃƒÆ’Ã‚Â¡rio`, `Deseja` e `Nenhum` com card de diagnÃƒÆ’Ã‚Â³stico estratÃƒÆ’Ã‚Â©gico inteligente (*Alerta de OrÃƒÆ’Ã‚Â§amento Engessado* caso ObrigatÃƒÆ’Ã‚Â³rio > 55%).
  - **Meio de LiquidaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o & CrÃƒÆ’Ã‚Â©dito**: Barra bifurcada e percentuais de exposiÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o entre CartÃƒÆ’Ã‚Â£o de CrÃƒÆ’Ã‚Â©dito vs DÃƒÆ’Ã‚Â©bito/Pix/Dinheiro.
  - **Top Categorias & Contas BancÃƒÆ’Ã‚Â¡rias**: DistribuiÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o visual dos gastos e concentraÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o institucional.
- **3. Eixo Futuro (Previsibilidade & Tomada de DecisÃƒÆ’Ã‚Â£o ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Para onde vamos)**:
  - **Curva de DesoneraÃƒÂ§ÃƒÂ£o (PrÃƒÂ³ximos 6 Meses)**: EvoluÃƒÂ§ÃƒÂ£o decrescente dos pagamentos com destaque para o mÃƒÂªs de maior pico e o mÃƒÂªs de maior folga financeira.
  - **DesoneraÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o & TÃƒÆ’Ã‚Â©rmino de Parcelas**: Cronograma de compras parceladas ativas que chegam ao fim, com cÃƒÆ’Ã‚Â¡lculo de alÃƒÆ’Ã‚Â­vio mensal gerado (*"+R$ X/mÃƒÆ’Ã‚Âªs livre"*).
  - **Cockpit de Tomada de DecisÃƒÆ’Ã‚Â£o (Smart Advisor)**: Consultoria contextual em tempo real para responder *"Posso assumir uma nova compra parcelada agora?"*.
- **4. DomÃƒÆ’Ã‚Â­nio & Testes Automatizados**:
  - Modelos enriquecidos em `FinancialDashboardMetrics.kt` e agregaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o performÃƒÆ’Ã‚Â¡tica em `GetFinancialDashboardUseCase.kt`.
  - 100% da suÃƒÆ’Ã‚Â­te unitÃƒÆ’Ã‚Â¡ria aprovada (`./gradlew testDebugUnitTest`).

---

## [1.4.1] - 2026-09-29

### ÃƒÂ°Ã…Â¸Ã¢â‚¬â„¢Ã…Â½ Grande Update das 4 Telas Centrais (Obsidian & Porcelain Minimalist)
- **1. Tela Inicial (`HomeScreen`)**:
  - **Executive Hero Card**: AdiÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o de barra de progresso horizontal (`PlatformProgressBar`) com cantos arredondados indicando o percentual de quitaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o do mÃƒÆ’Ã‚Âªs (ex: *"68% quitado"*) e mÃƒÆ’Ã‚Â©tricas de JÃƒÆ’Ã‚Â¡ Quitado e Total Geral.
  - **Modo Privacidade (`PlatformPrivacyToggle`)**: Alternador com ÃƒÆ’Ã‚Â­cone de olho para mascarar valores confidenciais (`R$ ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¢`) em locais pÃƒÆ’Ã‚Âºblicos.
  - **Seletor de MÃƒÆ’Ã‚Âªs RÃƒÆ’Ã‚Â¡pido em Grade (`MonthPickerBottomSheet`)**: Grade visual de meses e seletor de ano direto no tÃƒÆ’Ã‚Â­tulo do mÃƒÆ’Ã‚Âªs, eliminando cliques repetitivos em setas.
  - **Empty State Triunfante (`MonthVictoryCard`)**: Card comemorativo esmeralda quando todas as contas do mÃƒÆ’Ã‚Âªs estiverem 100% quitadas.
  - **Micro-interaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Âµes HÃƒÆ’Ã‚Â¡pticas**: VibraÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o suave ao efetuar a baixa de contas na lista.
- **2. Tela de Registros (`BillsScreen`)**:
  - **Segmented Tabs no Topo (`PlatformSegmentedTabs`)**: DivisÃƒÆ’Ã‚Â£o em 3 abas especializadas (`A Pagar`, `Pagas`, `Todas` com contadores numÃƒÆ’Ã‚Â©ricos).
  - **Banner de Totais Filtrados**: Resumo instantÃƒÆ’Ã‚Â¢neo do valor total e quantidade de registros visÃƒÆ’Ã‚Â­veis.
  - **AÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Âµes em Lote (`PlatformBatchActionBar`)**: Modo de seleÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o mÃƒÆ’Ã‚Âºltipla (toque longo ou botÃƒÆ’Ã‚Â£o no TopBar) para liquidar ou excluir vÃƒÆ’Ã‚Â¡rias contas com 1 confirmaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o.
- **3. Tela de Recorrentes & Parcelados (`RecurringInstallmentsScreen`)**:
  - **SeparaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o por Abas**: DivisÃƒÆ’Ã‚Â£o nÃƒÆ’Ã‚Â­tida entre `Compras Parceladas` (amortizaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o com data final) e `Assinaturas & Custos Fixos` (compromisso mensal contÃƒÆ’Ã‚Â­nuo).
  - **Card de AmortizaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o Estilo Financiamento**: Progresso visual com `PlatformProgressBar`, projeÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o de data de quitaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o final (*"TÃƒÆ’Ã‚Â©rmino em MÃƒÆ’Ã‚Âªs/Ano"*) e saldo devedor restante.
  - **Cronograma de Parcelas**: Acompanhamento detalhado de cada parcela com baixa direta.
  - **ProjeÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o Anual de Assinaturas**: ExibiÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o do custo anual consolidado (*"R$ Y/ano"*).
- **4. Tela de CartÃƒÆ’Ã‚Âµes de CrÃƒÆ’Ã‚Â©dito (`CreditCardsScreen`)**:
  - **Design Virtual Apple Card / Revolut (`PlatformCreditCardView`)**: ProporÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o bancÃƒÆ’Ã‚Â¡ria exata (`1.586f`), chip EMV vetorial metÃƒÆ’Ã‚Â¡lico, gradiente acetinado e termÃƒÆ’Ã‚Â´metro de limite inteligente semafÃƒÆ’Ã‚Â³rico integrado.
  - **Ciclo de 3 Faturas**: AlternÃƒÆ’Ã‚Â¢ncia de abas entre `Aberta`, `Fechadas`, `Pagas` e `Todas`.
  - **Destaque do Melhor Dia de Compra**: OrientaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o visual do dia de corte para compras com atÃƒÆ’Ã‚Â© 40 dias de prazo.
  - **Atalho de Nova Compra**: BotÃƒÆ’Ã‚Â£o direto no extrato para lanÃƒÆ’Ã‚Â§ar despesa prÃƒÆ’Ã‚Â©-selecionando o cartÃƒÆ’Ã‚Â£o.
- **5. Novos Componentes ReutilizÃƒÆ’Ã‚Â¡veis**:
  - `PlatformProgressBar.kt`, `PlatformPrivacyToggle.kt`, `PlatformSegmentedTabs.kt`, `PlatformBatchActionBar.kt`, `PlatformCreditCardView.kt`.
- **6. ValidaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o e Qualidade**:
  - 100% da suÃƒÆ’Ã‚Â­te `./gradlew testDebugUnitTest` aprovada e build limpo em 4 segundos.

---

## [1.4.0] - 2026-09-29

### ÃƒÂ°Ã…Â¸Ã…Â¡Ã¢â€šÂ¬ RefatoraÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o End-to-End: Wallet 100% Pessoal & GestÃƒÆ’Ã‚Â£o de Contas e Faturas
- **Contexto 100% Pessoal Unificado**:
  - EliminaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o definitiva de qualquer perfil corporativo/pessoal (`profileType`, `PESSOAL`/`EMPRESA`), operando com foco exclusivo em finanÃƒÆ’Ã‚Â§as pessoais.
- **Foco em Contas a Pagar & Previsibilidade**:
  - Cadastro ÃƒÆ’Ã‚Â¡gil e intuitivo exigindo apenas 3 dados obrigatÃƒÆ’Ã‚Â³rios: DescriÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o, Valor (em centavos `Long`) e Vencimento.
  - Seletores de Categoria e Item opcionais.
- **Natureza Financeira (ExpenseNature) & Itens de Despesa**:
  - ImplementaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o dos 4 pilares: `OBRIGATORIO` (custos inegociÃƒÆ’Ã‚Â¡veis), `NECESSARIO` (manutenÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o essencial), `DESEJA` (estilo de vida e conforto) e `NENHUM` (transitÃƒÆ’Ã‚Â³rios/nÃƒÆ’Ã‚Â£o classificados).
  - DepreciaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o de subcategorias e criaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o da tabela `expense_items` vinculada diretamente a `categories`, com heranÃƒÆ’Ã‚Â§a estrita da natureza da categoria mÃƒÆ’Ã‚Â£e.
  - Nova tela `ExpenseItemsScreen` em ConfiguraÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Âµes para listar, pesquisar e cadastrar itens vinculados com prÃƒÆ’Ã‚Â©-visualizaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o de natureza.
- **Efeito Cascata no Dashboard em Tempo Real**:
  - Total Previsto no MÃƒÆ’Ã‚Âªs calculado dinamicamente via Flow somando contas avulsas pendentes e faturas de cartÃƒÆ’Ã‚Â£o abertas/fechadas do perÃƒÆ’Ã‚Â­odo.
  - Agrupamento semafÃƒÆ’Ã‚Â³rico de urgÃƒÆ’Ã‚Âªncia: ÃƒÂ°Ã…Â¸Ã¢â‚¬ÂÃ‚Â´ Atrasadas, ÃƒÂ°Ã…Â¸Ã…Â¸Ã‚Â¡ Vence Hoje, ÃƒÂ¢Ã…Â¡Ã‚Âª PrÃƒÆ’Ã‚Â³ximos 7 Dias e ÃƒÂ°Ã…Â¸Ã…Â¸Ã‚Â¢ Pagas no MÃƒÆ’Ã‚Âªs (colapsÃƒÆ’Ã‚Â¡vel).
  - Baixa com 1 Toque ("Pagar"): atualiza status para `PAGO` no banco e deduz instantaneamente do total mensal sem recarregar a tela.
- **CartÃƒÆ’Ã‚Âµes de CrÃƒÆ’Ã‚Â©dito & Extrato de Faturas**:
  - CÃƒÆ’Ã‚Â¡lculo robusto de limite disponÃƒÆ’Ã‚Â­vel (`CreditCardCalculator`), fechamento de faturas baseado no dia de corte (`closingDay`) e status da fatura (`ABERTA`, `FECHADA`, `PAGA`).
  - Extrato detalhado de faturas em `CreditCardsScreen` com botÃƒÆ’Ã‚Â£o de liquidaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o em 1 toque.
- **PersistÃƒÆ’Ã‚Âªncia Room & MigraÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o**:
  - AtualizaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o do `PlatformDatabase` para versÃƒÆ’Ã‚Â£o `7` com `MIGRATION_6_7`.
  - InclusÃƒÆ’Ã‚Â£o da entidade `TransactionEntity` e DAO `TransactionDao` para rastreamento de transaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Âµes.
  - Tipo alias `AppDatabase = PlatformDatabase` para interoperabilidade.
- **Design System Material 3 (ADR-018)**:
  - AplicaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o estrita de tokens de cores para Dark Mode e Light Mode.
  - ProibiÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o de cores literais nos componentes; uso estrito de `MaterialTheme.colorScheme`.
  - Tipografia em Title Case (sem ALL CAPS em textos de tela).
  - ÃƒÆ’Ã‚Âcones funcionais do Material Icons substituindo formas abstratas.
- **LanÃƒÆ’Ã‚Â§amento Focado em Itens (Item-Centric Expenses)**:
  - O Item de Despesa (`selectedItemId`) assumiu o protagonismo absoluto no formulÃƒÆ’Ã‚Â¡rio: primeiro campo de seleÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o com preview de Natureza Financeira e Categoria.
  - A descriÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o passa a ser campo de "ObservaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Âµes Adicionais (Opcional)", herdando o nome do item selecionado como tÃƒÆ’Ã‚Â­tulo padrÃƒÆ’Ã‚Â£o caso nÃƒÆ’Ã‚Â£o preenchida.
  - ReorganizaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o do formulÃƒÆ’Ã‚Â¡rio em 5 blocos harmÃƒÆ’Ã‚Â´nicos e simÃƒÆ’Ã‚Â©tricos com cards e divisÃƒÆ’Ã‚Âµes semÃƒÆ’Ã‚Â¢nticas.
- **Design System Premium Minimalista & Simetria CirÃƒÆ’Ã‚Âºrgica**:
  - Paletas Dark e Light purificadas: Dark Obsidian (`#0A0D14` / `#141923`) e Light Porcelain (`#F8FAFC` / `#FFFFFF`).
  - AnulaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o estrita de elevaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o tonal com `surfaceTint = Color.Transparent` em ambos os temas, eliminando 100% qualquer vazamento roxo/lavanda default do Material 3.
  - ErradicaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o completa de cores literais hardcoded (`Color.White`, `Color.Black`, hexadecimais soltos) em todas as telas, adotando exclusivamente tokens de `Theme.kt`, `Color.kt` e `MaterialTheme.colorScheme`.
  - Anti-gigantismo e harmonia visual: ajuste da tipografia do impacto financeiro na HomeScreen de `headlineLarge` para `titleLarge` semibold/bold.
  - Alinhamentos milimÃƒÆ’Ã‚Â©tricos com cantos uniformes de 16.dp para cards, 10.dp para botÃƒÆ’Ã‚Âµes/inputs e bordas finas de 1.dp com `outlineVariant`.
  - **PadronizaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o Global dos FABs (100% Circular)**: UnificaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o dos FloatingActionButtons de todas as 9 telas financeiras (incluindo `RecurringInstallmentsScreen`) com `shape = CircleShape`, container `primary` e conteÃƒÆ’Ã‚Âºdo `onPrimary`.
  - **UnificaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o Absoluta de Fluxos de Despesa**: RemoÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o do modal legado `AddBillBottomSheet` em `BillsScreen`. Tanto a tela Inicial quanto a tela de Registros navegam para a mesma `NewExpenseScreen` com o formulÃƒÆ’Ã‚Â¡rio em 5 blocos harmÃƒÆ’Ã‚Â´nicos focado em itens.
  - **Componentes Centrais ReutilizÃƒÆ’Ã‚Â¡veis (`PlatformEmptyState` e `PlatformAvatar`)**:
    - `PlatformEmptyState`: EliminaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o definitiva de emojis informais (`ÃƒÂ°Ã…Â¸Ã¢â‚¬â„¢Ã‚Â³`, `ÃƒÂ°Ã…Â¸Ã¢â‚¬Å“Ã‹â€ `) em telas vazias, substituÃƒÆ’Ã‚Â­dos por containers vetoriais com fundo translÃƒÆ’Ã‚Âºcido a 12% e tipografia proporcional `titleLarge` semibold.
    - `PlatformAvatar`: EliminaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o de cÃƒÆ’Ã‚Â³digo duplicado de cÃƒÆ’Ã‚Â¡lculo de iniciais e renderizaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o visual homogÃƒÆ’Ã‚Âªnea em `ContactsScreen` e `ContactDetailScreen`.
  - **HigienizaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o Visual Estrita (Zero AÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Âµes Inline NÃƒÆ’Ã‚Â£o Seguras)**:
    - `ExpenseItemsScreen`: RemoÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o de botÃƒÆ’Ã‚Âµes de exclusÃƒÆ’Ã‚Â£o inline em cards, substituÃƒÆ’Ã‚Â­dos por navegaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o sutil com chevron e `AlertDialog` de confirmaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o seguro.
    - `BudgetsScreen`: MigraÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o de `BudgetCard` para `PlatformCard` com raio de 16.dp e contorno suave.
    - `CreditCardsScreen`: Ajuste de contraste do botÃƒÆ’Ã‚Â£o "Liquidar Fatura" (`onPrimary` garantindo legibilidade perfeita no Tema Escuro e Claro).
    - `SettingsScreen`: DistinÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o de ÃƒÆ’Ã‚Â­cone para Itens de Despesa com `Icons.Default.ShoppingBag`.
    - `AppDrawer`: CorreÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o do divisor de seÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Âµes com verificaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o insensÃƒÆ’Ã‚Â­vel a maiÃƒÆ’Ã‚Âºsculas/minÃƒÆ’Ã‚Âºsculas (`ignoreCase = true`).
  - **EliminaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o de CÃƒÆ’Ã‚Â³digo Zumbi & DepreciaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Âµes**:
    - RemoÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o do arquivo zumbi `DashboardScreen.kt` (-1.073 linhas de cÃƒÆ’Ã‚Â³digo morto).
    - MigraÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o de todos os ÃƒÆ’Ã‚Â­cones deprecados para `Icons.AutoMirrored.Filled` (`ReceiptLong` e `TrendingUp`), zerando warnings de compilaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o.
- **Cobertura de Testes Automatizados**:
  - `NewExpenseValidationTest`: 6 testes automatizados validando campos obrigatÃƒÆ’Ã‚Â³rios (Item e Contato), heranÃƒÆ’Ã‚Â§a de natureza, cÃƒÆ’Ã‚Â¡lculo de parcelas e preenchimento de tÃƒÆ’Ã‚Â­tulo a partir do item.
  - `CreditCardManagementTest`: ValidaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o de fechamento, limite e integridade de faturas.
  - `BillCalculationTest`, `ExpenseItemNatureTest`, `InvoiceClosingTest`: Todos aprovados com 100% de sucesso.

---

## [1.3.1] - 2026-09-28

### ÃƒÂ°Ã…Â¸Ã…Â½Ã‚Â¨ Melhorias Visuais & Ergonomia
- **Ajuste de ProporÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Âµes dos Cards em ConfiguraÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Âµes**:
  - AmpliaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o da ÃƒÆ’Ã‚Â¡rea de toque e respiro interno dos blocos de acesso direto a **Contas**, **Formas de Pagamento** e **Categorias** (padding vertical aumentado para 14dp e horizontal para 16dp).
  - ÃƒÆ’Ã‚Âcones com badges ampliados de 32dp para 44dp e cantos arredondados de 12dp para maior destaque visual e clareza de toque.
  - InclusÃƒÆ’Ã‚Â£o de subtÃƒÆ’Ã‚Â­tulos descritivos elegantes abaixo de cada tÃƒÆ’Ã‚Â­tulo para reforÃƒÆ’Ã‚Â§ar o propÃƒÆ’Ã‚Â³sito de cada ÃƒÆ’Ã‚Â¡rea.
  - Tipografia elevada para titleMedium semibold e chevron de navegaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o mais visÃƒÆ’Ã‚Â­vel.
  - EspaÃƒÆ’Ã‚Â§amento vertical entre blocos unificado em 12dp.

---

## [1.3.0] - 2026-09-28

### ÃƒÂ°Ã…Â¸Ã…Â¡Ã¢â€šÂ¬ Novas Funcionalidades
- **Backup e RestauraÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o de Dados Offline**:
  - ExportaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o completa e versionada do banco de dados em formato JSON compacto com valores inteiros em centavos (`Long`).
  - RestauraÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o atÃƒÆ’Ã‚Â´mica em lote utilizando transaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Âµes seguras do Room (`database.withTransaction`), garantindo rollback total em caso de falha.
  - IntegraÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o com o *Storage Access Framework (SAF)* nativo do Android (`CreateDocument` e `OpenDocument`) para salvar e restaurar do Google Drive, Documentos, Downloads ou cartÃƒÆ’Ã‚Â£o SD.
  - Compartilhamento rÃƒÆ’Ã‚Â¡pido via *ShareSheet* (`FileProvider`) direto para WhatsApp, Telegram ou e-mail.
  - Indicador em tempo real nas ConfiguraÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Âµes com a data e hora exata do ÃƒÆ’Ã‚Âºltimo backup efetuado.
  - DiÃƒÆ’Ã‚Â¡logo de advertÃƒÆ’Ã‚Âªncia e confirmaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o antes de restauraÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Âµes para impedir substituiÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Âµes acidentais de dados.

### ÃƒÂ°Ã…Â¸Ã…Â½Ã‚Â¨ HarmonizaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o Visual & PadrÃƒÆ’Ã‚Â£o de Detalhes (4 Etapas)
- **AdoÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o Universal do PadrÃƒÆ’Ã‚Â£o de Detalhes com BottomSheet**:
  - Cards 100% limpos e minimalistas em todo o sistema: remoÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o de botÃƒÆ’Ã‚Âµes inline de lixeira, lÃƒÆ’Ã‚Â¡pis e acordeÃƒÆ’Ã‚Âµes soltos.
  - Toque no card abre `ModalBottomSheet` dedicado com mÃƒÆ’Ã‚Â©tricas contextuais, visualizaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o de vÃƒÆ’Ã‚Â­nculos (conta, categoria, contato, mÃƒÆ’Ã‚Â©todo) e aÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Âµes rÃƒÆ’Ã‚Â¡pidas.
  - Menu de 3 pontos (`MoreVert`) no topo do BottomSheet para ediÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o e exclusÃƒÆ’Ã‚Â£o segura.
  - DiÃƒÆ’Ã‚Â¡logos de confirmaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o obrigatÃƒÆ’Ã‚Â³rios (`AlertDialog`) antes de qualquer exclusÃƒÆ’Ã‚Â£o definitiva.
  - Seletores de campos com opÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Âµes prÃƒÆ’Ã‚Â©-definidas migrados para menus dropdown nativos (`ExposedDropdownMenuBox`).
  - Seletor de cores aprimorado com indicador visual circular branco na cor ativa.
- **Telas Harmonizadas**:
  - [ManagementScreen.kt]: Formas de Pagamento e Categorias desacopladas com BottomSheet e seletor com indicador.
  - [BillsScreen.kt]: Cards de parcelas sanitizados, tipografia reduzida de 28sp para 16sp bold, chevron sutil e BottomSheet detalhado de liquidaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o.
  - [GoalsScreen.kt]: Cards de metas limpos, BottomSheet com progresso, aporte rÃƒÆ’Ã‚Â¡pido e menu de aÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Âµes.
  - [BudgetsScreen.kt]: Cards de orÃƒÆ’Ã‚Â§amento limpos, BottomSheet com comparaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o teto vs realizado e ajuste de limite.
  - [RecurringInstallmentsScreen.kt]: Cards de contratos enxutos, BottomSheet com detalhes do contrato e listagem completa de parcelas interativas.
  - [ContactDetailScreen.kt]: ExclusÃƒÆ’Ã‚Â£o segura de contato no menu de 3 pontos com diÃƒÆ’Ã‚Â¡logo de confirmaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o.

---

## [1.2.0] - 2026-09-28

### ÃƒÂ°Ã…Â¸Ã…Â¡Ã¢â€šÂ¬ Melhorias & EvoluÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Âµes
- **GestÃƒÆ’Ã‚Â£o Financeira Desacoplada nas ConfiguraÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Âµes**: Telas dedicadas e independentes para Contas BancÃƒÆ’Ã‚Â¡rias, Formas de Pagamento e Categorias, sem acoplamento de abas concorrentes.
- **Menu Lateral Minimalista**: ReduÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o de ruÃƒÆ’Ã‚Â­do visual, eliminaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o de subtÃƒÆ’Ã‚Â­tulos descritivos em todos os itens do drawer, proporÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Âµes enxutas e alinhamento com Material 3.
- **Dashboard Financeiro Macro**: Cards inteligentes com visÃƒÆ’Ã‚Â£o geral de saldo, compromissos do mÃƒÆ’Ã‚Âªs vigente e projeÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o futura de gastos.
- **Recorrentes & Parcelados**: GestÃƒÆ’Ã‚Â£o centralizada de assinaturas fixas e compras parceladas em andamento com cÃƒÆ’Ã‚Â¡lculo automÃƒÆ’Ã‚Â¡tico de amortizaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Âµes.
- **Metas & OrÃƒÆ’Ã‚Â§amentos**: Controle de reservas financeiras com barra de progresso visual e definiÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o de tetos de gastos mensais por categoria.
- **Cadastro Inteligente de Contatos**: IntegraÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o de busca automÃƒÆ’Ã‚Â¡tica de CEP via ViaCEP, novos campos estruturados de endereÃƒÆ’Ã‚Â§o e ediÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o com 1 toque.
- **AutomaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o de Versionamento**: SincronizaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o automÃƒÆ’Ã‚Â¡tica entre o arquivo `VERSION`, `app/build.gradle.kts` e scripts de bump para evitar estagnaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o de versÃƒÆ’Ã‚Â£o.

---

## [1.0.0] - 2026-09-26

### ÃƒÂ°Ã…Â¸Ã…Â¡Ã¢â€šÂ¬ Melhorias
- FundaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o da arquitetura base do aplicativo Android em Clean Architecture (Presentation, Domain, Data, Core).
- ConfiguraÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o do Android Gradle Plugin com Kotlin DSL (`build.gradle.kts` e Version Catalog `libs.versions.toml`).
- IntegraÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o da stack moderna de UI com Jetpack Compose BOM, Material 3 e suporte a Tema Claro e Escuro.
- InjeÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o de dependÃƒÆ’Ã‚Âªncias desacoplada preparada com Hilt.
- ConfiguraÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o de persistÃƒÆ’Ã‚Âªncia local com Room Database e comunicaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o remota com Retrofit/OkHttp.
- ImplementaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o de fluxos reativos com Kotlin Coroutines e StateFlow.
- Estrutura completa de governanÃƒÆ’Ã‚Â§a de IA (`AGENT_RULES.md`, `.ai/` e `.agents/` com skills e regras).
- AutomaÃƒÆ’Ã‚Â§ÃƒÆ’Ã‚Â£o de versionamento mÃƒÆ’Ã‚Â³vel sincronizando `versionCode`, `versionName` e `CHANGELOG.md`.

---



