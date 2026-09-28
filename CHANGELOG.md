# Changelog - Platform (Android App)

Todas as alterações notáveis neste projeto serão documentadas neste arquivo.
O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/) e este projeto segue [Versionamento Semântico](https://semver.org/lang/pt-BR/).

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
