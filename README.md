# Platform — Aplicativo Android (Android Studio)

Aplicativo Android nativo construído com as melhores práticas de engenharia de software móvel moderna, utilizando **Kotlin**, **Jetpack Compose (Material 3)**, **Clean Architecture**, injeção de dependências com **Hilt**, persistência local com **Room**, consumo de API com **Retrofit/OkHttp** e fluxos assíncronos reativos com **Coroutines & StateFlow**.

---

## 🏛️ Visão Geral da Arquitetura

O projeto adota separação estrita de responsabilidades:
- **Presentation Layer**: Telas e componentes puramente declarativos em Jetpack Compose, gerenciamento de estado previsível via `StateFlow` e ViewModels integrados com Hilt (`@HiltViewModel`). Suporte completo a **Modo Claro** e **Modo Escuro** (*Dark Mode*) com Material 3 e Dynamic Colors.
- **Domain Layer**: Modelos de domínio puros, contratos de repositório e Casos de Uso (`UseCases`) desacoplados de qualquer framework Android.
- **Data Layer**: Cache local offline-first via **Room Database**, cliente HTTP **Retrofit + OkHttp** com interceptor de logging e sincronização bidirecional.
- **Dependency Injection**: Injeção desacoplada de escopo Singleton e ViewModel via **Dagger Hilt**.
- **Infraestrutura de IA**: Governança em [.ai/](file:///home/jsilvaneto/projetos/platform/.ai), manual mestre em [AGENT_RULES.md](file:///home/jsilvaneto/projetos/platform/AGENT_RULES.md) e catálogo de skills em [.agents/skills/](file:///home/jsilvaneto/projetos/platform/.agents/skills).

---

## 📁 Estrutura de Pastas

```text
platform/
├── .agents/                 # Skills e regras executáveis para Agentes de IA
│   ├── rules/               # architecture.md, coding_standards.md, test_data_cleanup.md
│   └── skills/              # android-compose-design-system, add-new-screen-or-feature, etc.
├── .ai/                     # Documentação de arquitetura, contexto e status do projeto
│   ├── DECISIONS/           # Architecture Decision Records (ADRs)
│   ├── ARCHITECTURE.md      # Referência técnica canônica e diagramas
│   ├── CONTEXT.md           # Visão de produto e regras de domínio móvel
│   ├── STATUS.md            # Roadmap de releases e checklist de fases
│   └── ANDROID_GUIDE.md     # Manual prático de desenvolvimento
├── app/                     # Módulo principal do aplicativo Android
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/platform/app/
│   │   │   │   ├── core/            # Dispatchers, wrappers de resultado (Resource), utils
│   │   │   │   ├── data/            # Room (DAO, Entity), Retrofit (Service, DTO), Repositories
│   │   │   │   ├── domain/          # Modelos puros, UseCases e interfaces de Repository
│   │   │   │   ├── di/              # Módulos de injeção Hilt (AppModule, RepositoryModule)
│   │   │   │   └── presentation/    # Telas Compose, ViewModels, UiState, Tema Material 3
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

1. Abra o **Android Studio** (versão Iguana, Jellyfish ou superior recomendada).
2. Selecione **Open** e navegue até a pasta `/home/jsilvaneto/projetos/platform`.
3. Aguarde o Android Studio realizar a sincronização automática do Gradle (*Sync Project with Gradle Files*).
4. Selecione um emulador Android (API 26 ou superior) ou conecte um dispositivo físico via USB com Depuração USB ativada.
5. Clique no botão **Run** (`Shift + F10`) para compilar e iniciar o aplicativo.

---

## 🧪 Testes Automatizados

O projeto inclui suite de testes unitários isolados com MockK e Turbine:
```bash
./gradlew test
```

Para verificar regras de código e linting:
```bash
./gradlew lint
```

---

## 🏷️ Versionamento Semântico para Mobile

O projeto conta com automação para atualizar tanto a versão SemVer (`versionName`) quanto o código incremental do Google Play (`versionCode`):

```bash
# Atualizar versão patch (ex: 1.0.0 -> 1.0.1)
./scripts/bump-version.sh patch

# Atualizar versão minor (ex: 1.0.0 -> 1.1.0)
./scripts/bump-version.sh minor

# Atualizar versão major (ex: 1.0.0 -> 2.0.0)
./scripts/bump-version.sh major
```
