# ADR 001: Escolha da Arquitetura Base (Clean Architecture + MVVM + Hilt)

## Status
Aprovado

## Contexto
O projeto **Platform** necessita de uma base arquitetural robusta, testável e manutenível tanto por engenheiros humanos quanto por **Agentes de IA**. O ecossistema Android evoluiu para o Modern Android Development (MAD), exigindo desacoplamento entre UI, regras de negócio e fontes de dados (banco local e API).

## Decisão
1. **Padrão Arquitetural**: Clean Architecture com MVVM (Model-View-ViewModel) e UDF (Unidirectional Data Flow).
2. **Inversão de Dependência**: O núcleo de negócio (`domain`) é 100% Kotlin puro, dependendo apenas de interfaces de repositório.
3. **Injeção de Dependências**: Dagger Hilt com KSP, assegurando validação em tempo de compilação e integração com ViewModels e ciclo de vida do Android.
4. **Persistência e Cache**: Room Database como fonte de verdade offline-first com queries reativas via `Flow`.

## Consequências
- Alta testabilidade com testes unitários puros sem necessidade de emuladores ou Robolectric para a camada de domínio.
- Facilidade de iteração para agentes de IA através de camadas estritamente delimitadas.
- Código limpo, padronizado e alinhado às recomendações oficiais da Google.
