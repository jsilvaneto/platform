# Changelog - Platform (Android App)

Todas as alteraÃ§Ãµes notÃ¡veis neste projeto serÃ£o documentadas neste arquivo.
O formato Ã© baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/) e este projeto segue [Versionamento SemÃ¢ntico](https://semver.org/lang/pt-BR/).

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

