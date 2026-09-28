# Changelog - Platform (Android App)

Todas as alterações notáveis neste projeto serão documentadas neste arquivo.
O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/) e este projeto segue [Versionamento Semântico](https://semver.org/lang/pt-BR/).

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
