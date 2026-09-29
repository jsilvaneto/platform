# ADR 019: Escopo 100% Pessoal, Itens de Despesa com Natureza e Gestão de Cartões de Crédito

## Status
Aprovado

## Contexto
O produto evoluiu para focar exclusivamente na gestão de **Contas a Pagar Pessoais**. Para simplificar a experiência e aumentar a previsibilidade financeira sem elevar a fricção:
1. Eliminamos o suporte a múltiplos perfis (Pessoal/Empresarial).
2. Depreciamos a estrutura de subcategorias em prol de **Itens de Despesa** vinculados a **Categorias com Natureza Financeira** (Obrigatório, Necessário, Deseja, Nenhum).
3. Adicionamos suporte completo a **Cartões de Crédito** e **Faturas Mensais**.

## Decisões

### 1. Escopo Exclusivamente Pessoal
- Remoção total do campo `profileType` de todas as entidades do banco e formulários.
- Remoção do seletor PESSOAL/EMPRESA na `TopAppBar` e no `NavigationDrawer`.

### 2. Categorias com Natureza e Itens de Despesa
- Cada Categoria possui uma natureza financeira pré-definida:
  - 🔴 `OBRIGATORIO`: Moradia, Contas de Consumo, Financiamentos.
  - 🟡 `NECESSARIO`: Alimentação básica, Saúde, Combustível essencial.
  - 🔵 `DESEJA`: Estilo de vida, Lazer, Restaurantes, Streaming.
  - ⚪ `NENHUM`: Despesas transitórias ou não classificadas.
- A tabela `subcategories` foi removida. Em seu lugar, a tabela `expense_items` armazena itens específicos (ex: "Supermercado", "Internet", "Combustível") vinculados diretamente a uma Categoria.

### 3. Cartões de Crédito e Faturas
- Tabelas `credit_cards` e `credit_card_invoices`.
- Cada lançamento feito no cartão é vinculado à fatura do mês correspondente (`referenceMonth`: "YYYY-MM").
- A quitação de uma fatura gera o registro de liquidação em lote com status `PAGA`.

### 4. Design System Executivo (Material 3)
- **Dark Mode**: Fundo `0xFF0A0D14` (Deep Night), Superfícies `0xFF141923`, Bordas `0xFF222B3D` (1.dp).
- **Light Mode**: Fundo `0xFFF4F6F9`, Superfícies `0xFFFFFFFF`, Bordas `0xFFE2E8F0` (1.dp).
- **Sinalização Semafórica**: Atrasadas (`0xFFEF4444`), Vence Hoje (`0xFFF59E0B`), Pagas (`0xFF43A047`).
