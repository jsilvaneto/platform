# ADR 034: Nova Arquitetura de Informação, Bottom Navigation e Overhaul do Design System

## Status
Aprovado

## Contexto
O aplicativo Platform acumulou, ao longo de sucessivas iterações, uma dispersão considerável de padrões visuais e estruturas de navegação:
1. **Inconsistência de Tokens e Formas**: Mais de 12 raios de curvatura literais dispersos (10dp em 86 locais, 8dp em 47, 12dp em 37, 4dp em 26...), mais de 120 usos de `Surface` e `Card` crus sem seguir tokens semânticos, e múltiplos estilos concorrentes de top bars e seletores.
2. **Poluição Visual e Conflito Semântico de Cores**: Cores com sentidos concorrentes (laranja significando "Necessário" e "Vence hoje"; vermelho significando "Obrigatório" e "Atrasado"). Chips de natureza repetidos exaustivamente em todas as linhas de listas.
3. **Redundância de Informação e Telas**: Três telas concorrentes exibindo as mesmas parcelas (Início com 3 abas, Registros e Pagamentos Planejados); Visão Mensal afirmando "está tudo pago" de cinco maneiras distintas; KPIs sofrendo cortes em telas menores.
4. **Jargão Corporativo**: Uso de termos excessivamente corporativos em aplicativo de finanças pessoais ("Radar de Desembolso", "Curva de Desoneração", "Cockpit de Tomada de Decisão", "Rigidez Orçamentária").
5. **Ergonomia e Fricção em Cadastros**: Formulário de Nova Despesa longo com 5 blocos, contato obrigatório sem necessidade, campos secundários de pagamento sempre expostos e ausência de lançamento rápido por atalho/FAB.

## Decisão

### 1. Sistema Semântico de Formas (`PlatformShapes`) e Superfícies (`PlatformSurface`)
- Criado `PlatformShapes` com escala estrita de cantos arredondados:
  - `small` (8.dp): microelementos, chips, badges e botões compactos.
  - `medium` (12.dp): cards secundários, text fields, linhas de listas e botões de ação.
  - `large` (16.dp): hero cards, cards principais e diálogos.
  - `pill` (CircleShape): pílulas de status e abas segmentadas.
  - `bottomSheet`: RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp).
- Criado `PlatformSurface` com variantes semânticas `Flat`, `Tonal` e `Outlined`.
- Estabelecida a regra visual: **no máximo 1 nível de borda por tela**; blocos e cartões internos utilizam superfície tonal sutil (`surfaceVariant`).
- Criado e integrado ao pipeline Gradle o guard rail `:app:checkRawShapes`, proibindo a reintrodução de `RoundedCornerShape(n.dp)` cru na camada de apresentação.

### 2. Papéis Semânticos de Cor e Chips
- **Papel Exclusivo de Status**: Cores vivas semafóricas (verde para pago, neutro para pendente, âmbar para vence hoje/atenção, vermelho para atrasado) são reservadas estritamente para o estado da obrigação financeira.
- **Natureza do Gasto**: Deixou de ser chip colorido proeminente; passa a ser representado como ponto discreto ou suprimido nas listas quando idêntico à categoria-mãe.
- Unificação de status chips em torno do componente padronizado `PlatformStatusChip`.

### 3. Humanização de Glossário e Tom de Voz
- Centralização de strings literais em `AppStrings` eliminando jargões:
  - *Radar de Desembolso & Liquidez* → **Próximos pagamentos**
  - *Pilares Estruturais das Finanças* → **Resumo das contas**
  - *Passivo em Parcelamentos* → **Parcelamentos em aberto**
  - *Curva de Desoneração* → **Pagamentos nos próximos meses**
  - *Cockpit de Tomada de Decisão* → **Sugestões**
  - *Rigidez Orçamentária* → **Equilíbrio do orçamento**
  - *Meio de Liquidação & Crédito* → **Como você paga**
  - *Concentração por Contas Bancárias* → **Gastos por conta**
  - *Assinatura Contínua* → **Conta fixa**
  - *Registros* → **Contas**
  - *Itens de Despesa* → **Itens**

### 4. Nova Arquitetura de Navegação (Bottom Navigation Bar)
- Eliminação definitiva do `AppDrawer` com menu lateral oculto.
- Implementação de barra de navegação inferior (`NavigationBar`) com 5 destinos primários:
  1. **Hoje**: Cockpit diário do mês vigente com visão imediata de compromissos.
  2. **Contas**: Fusão unificada de Registros e Pagamentos Planejados, com abas *Todas | Recorrentes | Parceladas* e filtros em bottom sheet.
  3. **Cartões**: Faturas em aberto, limites e liquidação de ciclo.
  4. **Análises**: Inteligência financeira (passado, presente e futuro), incorporando Orçamentos e Metas como seções integradas de planejamento.
  5. **Mais**: Hub de acesso para Contatos, Cadastros (Contas, Formas de Pagamento, Categorias, Itens), Configurações e Backup.

### 5. Tela "Hoje" Racionalizada
- **Hero Consolidado**: Valor restante no mês, barra de progresso e linha descritiva única ("faltam X contas, a próxima vence dia Y" ou confirmação de tudo pago).
- **Precisa de Atenção**: Seção prioritária no topo (contas atrasadas, vencendo hoje e nos próximos 7 dias). Quando zerada, exibe banner compacto e positivo ("Tudo em dia").
- **Próximos Pagamentos**: Lista enxuta de até 5 compromissos com atalho rápido "Ver todas" direcionando para a tela de Contas. Contas pagas ficam recolhidas por padrão.
- **Toggle Limpo Lista | Calendário**: Substituição das 3 abas pesadas anteriores por um seletor rápido com autoseleção inteligente do próximo dia com vencimento pendente.
- **KPIs em Grid 2x2**: Total do Mês, Já Pago, Vence Hoje e Próximos 7 Dias dispostos sem cortes laterais de viewport.

### 6. Nova Despesa & Lançamento Rápido
- **Hierarquia Invertida no Formulário Completo**:
  - Valor em destaque no topo (tipografia headline com teclado numérico direto).
  - Item de despesa categorizado como segundo elemento obrigatório.
  - Vencimento com chips de atalho rápido (*Hoje, Amanhã, Dia 5, Outro*).
  - Contato/Fornecedor transformado em campo opcional.
  - Tipo de compromisso em abas segmentadas (parcelamento e recorrência visíveis somente se acionados).
  - Meio de pagamento recolhido por padrão com resumo sutil; quando selecionado cartão, exibe apenas cartão e fatura prevista.
  - Botão CTA fixo no rodapé (`bottomBar`) com mensagem explicativa quando desabilitado.
- **Lançamento Rápido via FAB (`QuickExpenseBottomSheet`)**:
  - FAB na tela inicial abre bottom sheet compacto para lançamento em poucos toques (Valor, Item, toggle "Já paga" e confirmação), com link de "Mais detalhes" para o formulário completo.
  - Suporte a Desfazer lançamento (`UndoSaveBill`) via Snackbar com reversão imediata no repositório.

### 7. Estatísticas Enxutas, Contatos e Configurações
- **Estatísticas (Análises)**: Cada aba (Passado, Presente, Futuro) exibe 1 frase de insight contextual + no máximo 4 cards centrais estruturados (título curto, valor principal e comparação). Cards secundários são organizados sob o botão expansível "Ver mais análises".
- **Contatos**: Linha da lista padronizada com subtítulo único legível (`tipo · total em aberto`), preservando telefones, e-mails e endereços completos para a visualização detalhada.
- **Configurações**: Agrupamento em 4 blocos semânticos (Cadastros, Aparência, Backup, Sobre) com banner de status do último backup no topo da seção de armazenamento.

## Consequências

### Positivas
- **Fricção Reduzida ao Mínimo**: Lançar despesas ou consultar o saldo pendente do mês agora exige fração dos toques e rolagem anteriormente necessários.
- **Clareza Visual Absoluta**: Eliminação de poluição de cores, cortes de layout e termos obscuros; o aplicativo comunica informações com elegância e consistência.
- **Arquitetura Protegida por 4 Guard Rails**:
  - `:app:checkFileSize`: 100% dos arquivos em presentation/, data/ e domain/ respeitam o teto de 600 linhas.
  - `:app:checkLiteralColors`: Zero cores literais em arquivos de UI.
  - `:app:checkHardcodedStrings`: Zero novas strings hardcoded sem centralização em `AppStrings` ou `strings.xml`.
  - `:app:checkRawShapes`: Zero RoundedCornerShape crus fora do design system.
- **Conformidade de Testes**: 177 testes unitários passando com sucesso sem regressões funcionais.
