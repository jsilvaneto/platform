# Changelog - Platform (Android App)

Todas as alterações notáveis neste projeto serão documentadas neste arquivo.
O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/) e este projeto segue [Versionamento Semântico](https://semver.org/lang/pt-BR/).

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
