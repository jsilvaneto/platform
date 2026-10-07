# ADR 029: Remoção do Seed Fictício de Cartões e Centralização do Primeiro Uso via PreferencesManager

## Status
Aprovado

## Contexto
Anteriormente, o método `FinancialRepositoryImpl.seedInitialCreditCardsIfEmpty()` criava automaticamente dois cartões de crédito fictícios ("Cartão Principal" com limite de R$ 10.000, fechamento 25 e vencimento 5, e "Cartão Secundário" com limite de R$ 5.000, fechamento 15 e vencimento 25) sempre que `countCards() == 0`.
Essa abordagem trazia graves problemas:
1. **Dados Fantasma**: Apresentava dados inventados como se fossem dados reais do usuário.
2. **Impossibilidade de Exclusão Definitiva**: Se o usuário excluísse conscientemente todos os seus cartões, eles eram recriados na abertura seguinte do app ou da tela de cartões.
3. **Dispersão e Redundância de Seeds**: O seeding de dados padrão (categorias, itens de despesa, contatos, contas e formas de pagamento) era executado nos blocos `init` de múltiplos ViewModels (`NewExpenseViewModel`, `BillsViewModel`, `CreditCardsViewModel`, `ExpenseItemsViewModel`, `ManagementViewModel`, `HomeViewModel` e `DashboardViewModel`), disparando múltiplas queries e verificações concorrentes em cada navegação.

## Decisão

### 1. Eliminação Completa do Seed de Cartões de Crédito
- O método `seedInitialCreditCardsIfEmpty()` foi removido do repositório e de todas as telas.
- Em [CreditCardsScreen.kt](file:///c:/Users/jsilvaneto/drive/projects/platform/app/src/main/java/com/platform/app/presentation/creditcards/CreditCardsScreen.kt), quando o usuário não possui cartões cadastrados (`cardsWithSummary.isEmpty()`), é exibido um empty state refinado e informativo com CTA explícito: `"Cadastrar primeiro cartão"`.

### 2. Centralização da Carga de Primeiro Uso (Startup / SeedInitialDataUseCase)
- Criado o UseCase [SeedInitialDataUseCase.kt](file:///c:/Users/jsilvaneto/drive/projects/platform/app/src/main/java/com/platform/app/domain/usecase/SeedInitialDataUseCase.kt) e a operação `seedInitialData()` em [FinancialRepository.kt](file:///c:/Users/jsilvaneto/drive/projects/platform/app/src/main/java/com/platform/app/domain/repository/FinancialRepository.kt).
- O controle de primeiro uso foi desacoplado de checagens do tipo "tabela vazia" e passou a ser governado pela flag booleana `seeds_applied` persistida no [PreferencesManager.kt](file:///c:/Users/jsilvaneto/drive/projects/platform/app/src/main/java/com/platform/app/core/preferences/PreferencesManager.kt) via Jetpack DataStore:
  - Se `seeds_applied == true`, nenhuma inserção é executada, respeitando plenamente caso o usuário opte por apagar todos os registros de qualquer entidade.
  - Se `seeds_applied == false`, mas o banco já contiver dados (usuário existente migrando de versão), a flag é marcada como `true` sem reinserir dados.
  - Se `seeds_applied == false` e o banco estiver vazio (instalação limpa), as tabelas de referência inicial (categorias, itens, contatos, contas financeiras e métodos de pagamento) são semeadas em uma única transação Room, e a flag é salva como `true`.
- A inicialização é disparada uma única vez no ciclo de vida da aplicação em [PlatformApplication.kt](file:///c:/Users/jsilvaneto/drive/projects/platform/app/src/main/java/com/platform/app/PlatformApplication.kt) através do `applicationScope`.

### 3. Remoção de Lógica de Seed dos ViewModels
- Removidas todas as chamadas de seeding dos blocos `init` dos ViewModels (`HomeViewModel`, `BillsViewModel`, `NewExpenseViewModel`, `ManagementViewModel`, `ExpenseItemsViewModel`, `CreditCardsViewModel`, `DashboardViewModel`).
- ViewModels agora focam exclusivamente em carregar e orquestrar estado da UI a partir do banco de dados já inicializado.

## Consequências

### Positivas
- **Respeito aos Dados do Usuário**: Se o usuário deletar todos os cartões, categorias, contatos ou contas, nada é recriado indevidamente.
- **Performance e Concorrência**: Eliminação de acessos redundantes e chamadas concorrentes de seed ao abrir telas.
- **Experiência Limpa (Zero Resíduo Inventado)**: Nenhum cartão fictício é exibido ou criado sem o consentimento e ação explícita do usuário.
- **Arquitetura Limpa**: Inicialização concentrada no Startup do app e ViewModels desacoplados de regras de bootstrap do banco.
