# Changelog - Platform (Android App)

Todas as alterações notáveis neste projeto serão documentadas neste arquivo.
O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/) e este projeto segue [Versionamento Semântico](https://semver.org/lang/pt-BR/).

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
- **Cobertura de Testes Automatizados**:
  - `BillCalculationTest`: Agregação de Total Previsto, dedução após pagamento e prevenção de dupla contagem de compras de cartão.
  - `ExpenseItemNatureTest`: Herança e preservação dos 4 pilares de natureza entre categoria e item.
  - `InvoiceClosingTest`: Fechamento de faturas, datas de vencimento e cálculo de limite disponível.

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
