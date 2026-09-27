# ADR 003: Arquitetura 100% Offline-First e Padrão MVI com Efeitos Únicos

## Status
Aprovado

## Contexto
O aplicativo **Platform** tem como premissa mandatória operar de forma **100% offline**. Todo o armazenamento de dados, buscas, filtros, marcações e persistência de preferências de usuário devem ocorrer localmente no dispositivo móvel. A sincronização com servidores externos é um recurso planejado apenas para etapas futuras e não deve, sob hipótese alguma, travar ou bloquear a experiência do usuário.
Além disso, no Jetpack Compose, eventos transitórios de interface (como exibição de `Snackbar`, `Dialog` ou navegação) sofrem com re-execução indevida em recomposições ou rotações de tela quando colocados diretamente no estado (`UiState`).

## Decisão
1. **Fonte Única da Verdade Local (Room Database)**:
   - Todo o fluxo de dados opera exclusivamente sobre o banco SQLite local via Room, com queries reativas via Kotlin `Flow`.
2. **Preferências Desacopladas (AndroidX DataStore)**:
   - Preferências do usuário, temas e timestamps de backup local são persistidos de forma assíncrona no DataStore.
3. **Monitoramento Passivo de Conectividade (`NetworkMonitor`)**:
   - Um monitor reativo em Flow observa a conexão do aparelho apenas para feedback informativo ou futura sincronização, sem interferir no funcionamento local.
4. **Padrão MVI com Efeitos Colaterais Seguros (`UiEffect`)**:
   - `UiState`: Representa o estado visual da tela (imutável, exposto via `StateFlow`).
   - `UiAction`: Intenções explícitas do usuário disparadas para o ViewModel via método `onAction(action)`.
   - `UiEffect`: Eventos voláteis de disparo único enviados através de um `Channel<UiEffect>(Channel.BUFFERED)` e consumidos pela UI via `receiveAsFlow()` dentro de um `LaunchedEffect`.
5. **Sincronização Remota como Extensão Futura**:
   - A camada de rede (Retrofit/OkHttp) permanece desacoplada e isolada, pronta para ser ativada no futuro sem afetar o comportamento offline já consolidado.

## Consequências
- O aplicativo inicia instantaneamente e funciona plenamente em modo avião ou sem conectividade.
- Zero dependência de latência, disponibilidade ou timeouts de servidores externos.
- Código previsível, desacoplado, facilmente testável com MockK e Turbine e imune a bugs de recomposição no Compose.
