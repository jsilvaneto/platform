---
name: ui-elegance-and-proportions
description: >-
  Use this skill to enforce visual elegance, compact and harmonious proportions, refined typography,
  anti-gigantism rules, streamlined Material 3 components, and clean interaction patterns across all Android UI screens.
---

# Skill: Elegância Visual, Harmonia e Proporções em Compose (ui-elegance-and-proportions)

Esta skill estabelece diretrizes rigorosas de design de interface, refinamento estético, contenção de escala (anti-gigantismo) e elegância visual para todos os componentes e telas do aplicativo **Platform**.

---

## 1. Princípios de Proporção e Escala (Anti-Gigantismo)

### 1.1 Hierarquia de Tipografia Harmoniosa
- **Títulos de Tela**: Utilize `MaterialTheme.typography.titleLarge` ou `titleMedium` com `FontWeight.Bold`. NUNCA utilize `displayLarge`, `headlineLarge` ou `headlineMedium` para títulos de topo ou cabeçalhos comuns.
- **Valores Monetários e Métricas**: Destaque com moderação usando `titleLarge` ou `headlineSmall`. NUNCA crie números gigantescos que desequilibrem o card.
- **Rótulos e Legendas**: Utilize `labelSmall` ou `bodySmall` com transparência suave (`onSurface.copy(alpha = 0.6f)`).
- **Sem Duplicação de Títulos**: Se uma tela já possui `TopAppBar` com o título ("Contatos", "Início", "Metas"), NUNCA repita um `Text(text = "...", headlineMedium)` no corpo da tela.

### 1.2 Dimensões de Componentes e Avatares
- **Avatares de Iniciais / Ícones de Categoria**: Tamanho padrão entre `38.dp` e `44.dp`. Evite avatares de 54.dp+ em listas ou cards secundários.
- **Ícones de Ação e Suporte**: Entre `16.dp` e `20.dp` em linhas de detalhe; no máximo `24.dp` em barras de ferramentas.
- **Espaçamento e Gaps**:
  - Entre itens de lista: `Arrangement.spacedBy(8.dp)` a `Arrangement.spacedBy(10.dp)`.
  - Margens horizontais de tela: `16.dp`.
  - Espaçamentos verticais internos (`Spacer`): preferencialmente `6.dp`, `8.dp`, `12.dp` ou `16.dp`. Evite `Spacer(height = 32.dp+)` solto no meio de telas ativas.

### 1.3 Cards e Containers Enxutos
- **Padding Interno**: `12.dp` a `14.dp` para cards de lista; `16.dp` para cards de destaque ("Hero Card").
- **Cantos Arredondados**: `RoundedCornerShape(12.dp)` a `RoundedCornerShape(16.dp)` para cards normais.
- **Densidade de Informação**: Prefira agrupar informações em linhas compactas com ícones sutis em vez de criar múltiplos blocos verticais separados.

---

## 2. Padrões de Elegância e Limpeza Visual

### 2.1 Ações de Barra Superior (TopAppBar)
- **Busca Inline Elegante**: Em vez de caixas de busca volumosas ocupando o corpo da tela, use uma lupa (`Icons.Default.Search`) no canto superior direito. Ao clicar, ela expande horizontalmente uma linha de pesquisa compacta com animação suave (`AnimatedVisibility`), botão de limpar e fechar.
- **Menu de 3 Pontos para Ações Secundárias**: Agrupe edições, exclusões e opções adicionais em um botão de overflow de 3 pontos (`Icons.Default.MoreVert`) com `DropdownMenu` elegante, em vez de poluir a barra ou as linhas com múltiplos botões de ação soltos.
- **Listas Limpas sem Lápis Redundantes**: Cards de lista devem ter toque direto (`clickable`) que navega para o detalhe ou abre o fluxo correspondente. Evite colocar botões de lápis e lixeira repetidos em cada linha se a ação principal já for intuitiva.

### 2.2 Cores Suaves e Contraste Premium
- **Containers e Superfícies**: Prefira superfícies translúcidas com `MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)` ou `surface` com elevação sutil em vez de fundos opacos e pesados.
- **Divisores e Bordas**: Use `outlineVariant.copy(alpha = 0.25f)` para contornos suaves que delimitam sem chamar atenção indesejada.
- **Indicadores de Status**: Badges compactas com cantos arredondados (`RoundedCornerShape(6.dp)` ou `CircleShape`), padding reduzido (`horizontal = 8.dp, vertical = 2.dp`) e texto `labelSmall` em negrito.

### 2.3 Formulários e Campos de Texto
- **Altura Compacta**: Campos com `singleLine = true`, altura comedida e cantos de `10.dp` a `12.dp`.
- **Labels e Placeholders Discretos**: Textos de ajuda curtos em `bodySmall` que não poluam o fluxo visual do usuário.

---

## 3. Checklist de Validação Visual
Antes de concluir qualquer tela ou componente em Compose:
1. [ ] A tela possui proporções equilibradas e não parece "pesada" ou "gigante"?
2. [ ] Não há títulos duplicados entre o TopAppBar e o corpo da tela?
3. [ ] A busca é discreta e integrada na barra superior sempre que possível?
4. [ ] O overflow menu (`MoreVert` com `DropdownMenu`) é usado para ações secundárias?
5. [ ] Todos os espaçamentos respeitam o grid de 4.dp/8.dp/12.dp/16.dp?
6. [ ] As cores respeitam o tema claro/escuro via `MaterialTheme.colorScheme`?
