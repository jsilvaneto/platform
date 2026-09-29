# ADR 018: Design System e Diretrizes de UI/UX Mobile (Material 3)

## Status
Aprovado

## Contexto
O aplicativo Android anterior apresentava inconsistências visuais e excesso de densidade cognitiva ao tentar reproduzir painéis contábeis completos em telas reduzidas. O objetivo central do produto neste estágio é a gestão de **Contas a Pagar com Alta Previsibilidade e Baixa Fricção**.

## Decisões

### 1. Filosofia de Design e UX
- **Menor atrito possível**: Cadastros rápidos exigem apenas 3 dados obrigatórios (Descrição, Valor e Vencimento).
- **Ações em 1 toque**: Baixa de contas e marcação de status sem telas intermediárias desnecessárias.
- **Hierarquia Semafórica Funcional**: O uso de cores vibrantes fica restrito a alertas de vencimento:
  - 🔴 **Crítico/Atrasado**: Vermelho Coral.
  - 🟡 **Vence Hoje**: Âmbar Dourado.
  - 🟢 **Liquidado/Pago**: Verde Esmeralda suave.
  - ⚪ **Futuro/Neutro**: Tons de cinza e azul-marinho.

### 2. Padrão Tipográfico e Capitalização
- **Title Case (Apenas a 1ª letra maiúscula)**: Nomes de Contas, Categorias, Meios de Pagamento e Títulos de Cards (ex: `Internet Fibra`, `Cartão Nubank`).
- **Sentence Case (Apenas a 1ª letra da frase maiúscula)**: Descrições de lançamentos, notas e mensagens (ex: `Compra de insumos do escritório`).
- **ALL CAPS**: Exclusivo para constantes do sistema (`PESSOAL`, `EMPRESA`, `PENDENTE`, `PAGO`) e chips minúsculos de status.

### 3. Paleta de Cores e Superfícies
- **Dark Mode (Padrão OLED/Deep Navy)**:
  - Fundo principal: `#0F1117`
  - Cards e Superfícies: `#1A1F2B`
  - Bordas de Contraste: `#283042` (1.dp)
  - Texto Primário: `#F1F5F9` | Texto Secundário: `#94A3B8`
- **Light Mode (Clean Slate)**:
  - Fundo principal: `#F8FAFC`
  - Cards e Superfícies: `#FFFFFF`
  - Bordas de Contraste: `#E2E8F0` (1.dp)
  - Texto Primário: `#0F172A` | Texto Secundário: `#64748B`

### 4. Regras de Componentes Compose
- Todos os cards devem utilizar `RoundedCornerShape(16.dp)` e borda sutil de `1.dp`.
- Espaçamento padrão de tela: `horizontal = 16.dp`, espaçamento entre itens: `12.dp`.
- Floating Action Button (FAB) sempre arredondado (`CircleShape`) com elevação controlada.
- Valores monetários sempre manipulados como `Long` (centavos) e formatados com destaque tipográfico em negrito (`FontWeight.Bold`).
