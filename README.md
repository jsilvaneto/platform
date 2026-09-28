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
  - ⚙️ **Cadastros Base**: Gestão de Contas Financeiras de Referência, Formas de Pagamento e Categorias/Subcategorias.
- **Domain Layer**: Modelos de domínio puros, contratos de repositório e Casos de Uso (`UseCases`) desacoplados de qualquer framework Android (100% Kotlin puro).
- **Dependency Injection**: Injeção desacoplada de escopo Singleton e ViewModel via **Dagger Hilt**.
- **Infraestrutura de IA**: Governança em [.ai/](file:///home/jsilvaneto/projetos/platform/.ai), manual mestre em [AGENT_RULES.md](file:///home/jsilvaneto/projetos/platform/AGENT_RULES.md) e catálogo de 10 skills em [.agents/skills/](file:///home/jsilvaneto/projetos/platform/.agents/skills).

---

## 📁 Estrutura de Pastas

```text
platform/
├── .agents/                 # Skills e regras executáveis para Agentes de IA
│   ├── rules/               # architecture.md, coding_standards.md, test_data_cleanup.md
│   └── skills/              # financial-domain-guard, room-database-and-migrations, etc.
├── .ai/                     # Documentação canônica de arquitetura e contexto
│   ├── DECISIONS/           # Architecture Decision Records
│   ├── ARCHITECTURE.md      # Referência técnica canônica e diagramas
│   ├── CONTEXT.md           # Visão de produto e regras de domínio móvel 100% offline
│   ├── STATUS.md            # Roadmap de releases e checklist de fases
│   └── ANDROID_GUIDE.md     # Manual prático de desenvolvimento
├── app/                     # Módulo principal do aplicativo Android
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/platform/app/
│   │   │   │   ├── core/            # CurrencyUtils, DateUtils, MVI, Preferences, Security
│   │   │   │   ├── data/            # Room (DAOs, Entities, Database), Repositories
│   │   │   │   ├── domain/          # Modelos, UseCases, Contratos de Repositório
│   │   │   │   ├── di/              # Módulos de injeção Hilt (AppModule, RepositoryModule)
│   │   │   │   └── presentation/    # Dashboard, Bills, Recurring, Budgets, Goals, Contacts, Management
│   │   │   ├── res/                 # Strings, cores, temas nativos, regras de backup
│   │   │   └── AndroidManifest.xml  # Manifesto do app
│   │   └── test/                    # Testes unitários com JUnit, MockK e Turbine
│   ├── build.gradle.kts             # Dependências e build config do módulo app
│   └── proguard-rules.pro           # Regras de ofuscação e otimização R8/ProGuard
├── gradle/
│   ├── libs.versions.toml           # Version Catalog com versões centralizadas de dependências
│   └── wrapper/                     # Gradle wrapper
├── scripts/
│   └── bump-version.sh              # Automação de versionamento móvel (versionCode e versionName)
├── AGENT_RULES.md                   # Diretrizes operacionais para agentes de IA
├── CHANGELOG.md                     # Registro histórico de alterações por versão
├── VERSION                          # Versão SemVer atual (ex: 1.0.0)
├── build.gradle.kts                 # Script de build raiz
├── settings.gradle.kts              # Configuração de repositórios e módulos
└── gradle.properties                # Configurações de JVM e AndroidX
```

---

## 🚀 Como Executar o Projeto no Android Studio

1. Abra o **Android Studio**.
2. Abra a pasta `/home/jsilvaneto/projetos/platform`.
3. Selecione um emulador ou conecte um celular físico via USB.
4. Clique em **Run** (`Shift + F10`).
5. O app iniciará no **Dashboard Financeiro**, permitindo navegar pelo menu lateral (**Início**, **Registros**, **Recorrentes & Parcelados**, **Estatísticas**, **Orçamentos**, **Metas**, **Contatos**, **Cadastros Base** e **Configurações**).
