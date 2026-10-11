# ADR 038: Papéis Canônicos das Listas, Remoção de Telas Órfãs e Ponto Único de Cadastros

- **Status**: Aceito
- **Data**: 2026-10-10
- **Contexto**: Prompt 43 (Limpeza, consolidação e remoção de telas e rotas duplicadas)

## Contexto e Problema

Com a evolução da v1.20 até a v1.25, diversas telas e rotas sofreram sobreposição funcional:
1. **Listas redundantes**: As telas `Hoje`, `Contas` e `Calendário` exibiam conjuntos idênticos de parcelas sem uma separação semântica clara do seu propósito de uso, causando confusão cognitiva no usuário.
2. **Tela órfã**: A tela legada `RecurringInstallmentsScreen` (Recorrentes e Parceladas) permanecia no código com mais de 540 linhas, sua ViewModel e rota dedicada, sem que houvesse mais qualquer ponto de entrada ou navegação para ela após a introdução das abas em `BillsScreen`.
3. **Rotas duplicadas**: Existiam rotas redundantes para o mesmo destino (e.g. `Screen.Home` e `Screen.Dashboard` apontando para a mesma tela `today`).
4. **Cadastros fragmentados**: Formulários e listagens de entidades base (Contas Financeiras, Formas de Pagamento, Categorias e Itens de Despesa) estavam espalhados em três pontos diferentes: menu "Mais", tela "Configurações" e tela "Cadastros" (`ManagementScreen`), além de possuírem rotas-wrapper legadas.
5. **Metas e Orçamentos duplicados**: Apareciam tanto no menu "Mais" quanto na aba de "Análises".

## Decisões Tomadas

1. **Papéis Canônicos das Três Listas**:
   - **Hoje (Agir)**: Foco imediato na execução diária e semanal. Apresenta o Hero Card de restante a pagar no mês ("Restante a Pagar no Mês"), seção "Precisa de atenção" (vencidas e a vencer), pagamento rápido com 1 toque e opção de desfazer (Snackbar). Fonte de dados calculada localmente para 100% offline.
   - **Contas (Gerir)**: Gestão operacional e tática das contas. Estruturada em abas (*Todas*, *Recorrentes*, *Parceladas*), suporte a seleção e quitação em lote, ações contextuais por item e filtros analíticos avançados.
   - **Calendário (Planejar)**: Visão temporal e prospectiva do fluxo de despesas ao longo do mês e do ano, permitindo prever gargalos de liquidez por dia de vencimento.

2. **Remoção de Código e Rotas Órfãs**:
   - Removido o pacote completo `com.platform.app.presentation.recurring` (`RecurringInstallmentsScreen`, `RecurringInstallmentsViewModel`, diálogos e componentes auxiliares) e seus respectivos testes unitários.
   - Removida a rota legada `Screen.Home` em prol de `Screen.Dashboard` ("today").
   - Removida a rota `Screen.RecurringInstallments`.

3. **Ponto Único Canônico de Cadastros (`Mais -> Cadastros`)**:
   - Centralização na `ManagementScreen` através de 4 abas nativas completas:
     1. **Contas**: Gestão de contas financeiras (`FinancialAccount`).
     2. **Formas**: Gestão de métodos de pagamento (`PaymentMethod`).
     3. **Categorias**: Gestão de categorias analíticas (`Category`).
     4. **Itens**: Gestão dos itens de despesa (`ExpenseItem`), integrada com `ExpenseItemsScreen(showTopBar = false)`.
   - Remoção dos wrappers intermediários `AccountsScreen`, `PaymentMethodsScreen` e `CategoriesScreen`, e eliminação das rotas individuais em `Screen.kt` e `NavGraph.kt`.
   - Remoção da seção duplicada de cadastros na `SettingsScreen`.
   - Redirecionamento da gestão de Metas e Orçamentos exclusivamente para `Análises -> Planejar`.

4. **Garantia por Testes**:
   - Implementado o teste `NavigationCompletenessTest`, assegurando que todas as rotas ativas são únicas, que não existem rotas órfãs ou legadas, e que as rotas antigas (`home`, `recurring_installments`, `accounts`, etc.) não retornem ao grafo.

## Consequências

- **Positivas**:
  - Eliminação de centenas de linhas de código morto e duplicado.
  - Arquitetura de informação limpa e intuitiva: cada tela possui um propósito único e inconfundível.
  - Navegação consistente sem rotas zumbis.
- **Negativas / Mitigações**:
  - Usuários acostumados a acessar contas pelo menu Configurações agora utilizam o hub unificado de Cadastros em "Mais".
