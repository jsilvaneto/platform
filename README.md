# Platform — Aplicativo Android (Contas a Pagar 100% Offline-First)

Aplicativo Android nativo de produtividade e gestão pessoal construído com **Kotlin**, **Jetpack Compose (Material 3)**, **Clean Architecture**, arquitetura **100% Offline-First**, padrão **MVI (Model-View-Intent)**, injeção de dependências com **Hilt**, persistência local com **Room**, preferências com **AndroidX DataStore** e fluxos reativos com **Coroutines & StateFlow**.

O aplicativo é especializado no controle rigoroso de **Contas a Pagar, Compras Parceladas, Despesas Recorrentes e Orçamentos**, permitindo vincular lançamentos a **Contas Financeiras de Referência** (bancos, carteiras e cartões).

---

## 🏛️ Visão Geral da Arquitetura

O projeto adota separação estrita de responsabilidades:
- **100% Offline-First & Integridade de Centavos**:
  - Todo dado gerado é persistido exclusivamente no banco local **Room (SQLite)** e preferências **DataStore**.
  - Valores monetários são calculados e armazenados como inteiros em centavos (`amountCents: Long`), impedindo imprecisões de ponto flutuante.
  - Compras parceladas utilizam divisão matemática de centavos exatos com distribuição do resto na primeira parcela.
  - Gravações compostas operam sob transações atômicas (`database.withTransaction`).
- **Padrão MVI com Efeitos Seguros**:
  - `UiState`: Estado imutável da tela exposto via `StateFlow`.
  - `UiAction`: Intenções explícitas do usuário enviadas ao ViewModel.
  - `UiEffect`: Efeitos colaterais transitórios (Snackbars, navegação) enviados através de um `Channel` bufferizado, eliminando repetições indesejadas em recomposição ou rotação de tela.
- **Telas & Navegação (Material 3 AppDrawer)**:
  - 📊 **Início (Dashboard)**: KPIs do mês (Total a Pagar, Pago, Pendente, Vencido), seletor de mês, alertas de vencimentos dos próximos 7 dias com quitação rápida e distribuição por categoria.
  - 💳 **Registros (Contas a Pagar)**: Lista de vencimentos com busca inline animada na TopAppBar, filtros de tipo (`Avulsas`, `Parceladas`, `Recorrentes`), filtros de status (`A Pagar`, `Pagas`, `Vencidas`), quitação rápida e cadastro de contas.
  - 🔄 **Recorrentes e Parcelados**: Acompanhamento de progresso de compras parceladas e valor consolidado de compromissos mensais.
  - 📈 **Estatísticas**: Histórico de pagamentos e pontualidade.
  - 🎯 **Orçamentos & Metas**: Tetos mensais de gastos por categoria e objetivos de reserva.
  - 👥 **Contatos**: Favorecidos e beneficiários a quem os pagamentos são devidos.
  - 🏦 **Contas**: Gestão direta de Contas Financeiras de Referência (bancos, carteiras, cartões).
  - 💳 **Formas de Pagamento**: Cadastro de métodos e vínculos padrão.
  - 🏷️ **Categorias & Subcategorias**: Classificação hierárquica e tags coloridas.
  - 💾 **Backup & Restauração Offline**: Exportação e restauração atômica de banco de dados em formato JSON com SAF nativo e compartilhamento direto (ShareSheet).
  - ⚙️ **Configurações**: Notas de versão dinâmicas, gerenciamento de dados e preferências.
- **Domain Layer**: Modelos de domínio puros, contratos de repositório e Casos de Uso (`UseCases`) desacoplados de qualquer framework Android (100% Kotlin puro).
- **Dependency Injection**: Injeção desacoplada de escopo Singleton e ViewModel via **Dagger Hilt**.
- **Infraestrutura de Governança**: Governança em [.ai/](file:///c:/Users/jsilvaneto/drive/projects/platform/.ai), diretrizes em [AGENT_RULES.md](file:///c:/Users/jsilvaneto/drive/projects/platform/AGENT_RULES.md), regras invioláveis em [.agents/rules/](file:///c:/Users/jsilvaneto/drive/projects/platform/.agents/rules) e catálogo de 11 skills em [.agents/skills/](file:///c:/Users/jsilvaneto/drive/projects/platform/.agents/skills).

---

## 📁 Estrutura de Pastas

```text
platform/
├── .agents/                 # Regras mandatórias e skills especializadas para Agentes de IA
│   ├── rules/               # architecture.md, coding_standards.md, test_data_cleanup.md, governance_and_versioning.md
│   └── skills/              # ui-elegance-and-proportions, offline-backup-and-export, financial-domain-guard, etc.
├── .ai/                     # Documentação canônica viva de arquitetura e contexto
│   ├── DECISIONS/           # Architecture Decision Records (ADRs 001 a 031)
│   ├── ARCHITECTURE.md      # Referência técnica canônica e diagramas
│   ├── CONTEXT.md           # Visão de produto e regras de domínio móvel 100% offline
│   ├── STATUS.md            # Roadmap de releases e checklist de fases (Versão atual: 1.21.0)
│   └── ANDROID_GUIDE.md     # Manual prático de desenvolvimento
├── app/                     # Módulo principal do aplicativo Android
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/platform/app/
│   │   │   │   ├── core/            # CurrencyUtils, DateUtils, MVI, Preferences, Security
│   │   │   │   ├── data/            # Room (DAOs, Entities, Database, BackupRepository), Repositories
│   │   │   │   ├── domain/          # Modelos, UseCases, Contratos de Repositório
│   │   │   │   ├── di/              # Módulos de injeção Hilt (AppModule, RepositoryModule)
│   │   │   │   └── presentation/    # Dashboard, Bills, Recurring, Budgets, Goals, Contacts, Management, Settings
│   │   │   ├── res/                 # Strings, cores, temas nativos, regras de backup
│   │   │   └── AndroidManifest.xml  # Manifesto do app (FileProvider configurado para exportação)
│   │   └── test/                    # Testes unitários com JUnit, MockK e Turbine
│   ├── build.gradle.kts             # Dependências e build config do módulo app
│   └── proguard-rules.pro           # Regras de ofuscação e otimização R8/ProGuard
├── gradle/
│   ├── libs.versions.toml           # Version Catalog com versões centralizadas de dependências
│   └── wrapper/                     # Gradle wrapper
├── scripts/
│   ├── bump-version.ps1             # Automação de versionamento para Windows PowerShell (versionCode e versionName)
│   └── bump-version.sh              # Automação de versionamento para Bash/Linux
├── AGENT_RULES.md                   # Diretrizes operacionais para agentes de IA
├── CHANGELOG.md                     # Registro histórico de alterações por versão (v1.3.0)
├── VERSION                          # Versão SemVer atual (1.3.0)
├── build.gradle.kts                 # Script de build raiz
├── settings.gradle.kts              # Configuração de repositórios e módulos
└── gradle.properties                # Configurações de JVM e AndroidX
```

---

## 🚀 Como Executar o Projeto no Android Studio

1. Abra o **Android Studio**.
2. Abra a pasta do projeto `platform`.
3. Selecione um emulador Android ou conecte um celular físico via USB (Android 8.0+ / API 26+).
4. Clique em **Run** (`Shift + F10`).
5. O app iniciará no **Dashboard Financeiro**, permitindo navegar pelo menu lateral com a nova estrutura ergonômica direta (**Início**, **Registros**, **Recorrentes & Parcelados**, **Estatísticas**, **Orçamentos**, **Metas**, **Contatos**, **Contas**, **Formas de Pagamento**, **Categorias** e **Configurações**).
