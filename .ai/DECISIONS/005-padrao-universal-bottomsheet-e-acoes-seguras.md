# ADR 005: Padrão Universal de Detalhes via BottomSheet e Ações Seguras

## Data
2026-09-28

## Status
Aprovado e Implementado (v1.3.0)

## Contexto
O aplicativo apresentava cards de listagem com botões inline soltos (ícones de lixeira, lápis de edição, sanfonas expansíveis), o que gerava ruído visual, aumentava a área de clique acidental e comprometia as proporções elegantes estabelecidas no menu lateral minimalista.

## Decisão
1. **Cards Limpos (Sem Botões Inline)**:
   - Todo card de entidade (Contas, Métodos de Pagamento, Categorias, Contas a Pagar, Metas, Orçamentos, Recorrentes) possui superfície limpa, sem botões de exclusão ou edição diretamente na lista.
2. **Abertura de Detalhes via ModalBottomSheet**:
   - O toque direto no card abre um `ModalBottomSheet` dedicado.
   - O BottomSheet expõe:
     - Header com ícone, título e valor/status.
     - Bloco contextual com todos os vínculos (conta bancária debitada, categoria, contato favorecido, forma de pagamento).
     - No topo superior direito: menu de 3 pontos (`MoreVert`) com as ações de "Editar" e "Excluir".
3. **Exclusão Segura Obrigatória**:
   - A opção de excluir NUNCA apaga diretamente. Dispara sempre um `AlertDialog` de confirmação com aviso explícito e botão de cancelamento.
4. **Seletores Nativos e Indicadores Visuais**:
   - Campos de opções fixas utilizam `ExposedDropdownMenuBox` nativo em vez de digitação livre.
   - Paletas de cores possuem indicador circular branco para marcar a cor selecionada.

## Consequências
- Visual limpo, profissional e consistente em todas as 6 telas do sistema.
- Eliminação total de exclusões acidentais por toque inadvertido em lixeiras inline.
- Experiência de navegação moderna e harmônica.
