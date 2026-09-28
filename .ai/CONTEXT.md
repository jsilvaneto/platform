# Contexto de Negócio e Domínio (.ai/CONTEXT.md)

Este documento registra a visão do produto, princípios fundamentais e invariantes de domínio do aplicativo móvel **Platform**.

---

## 1. Visão Geral do Produto
O **Platform** é um aplicativo Android nativo focado exclusivamente na gestão pessoal e controle financeiro de **Contas a Pagar, Compras Parceladas, Despesas Recorrentes e Orçamentos de Gastos**, operando prioritariamente de forma **100% Offline-First**.

---

## 2. Invariantes de Domínio e Arquitetura Móvel
1. **Foco Exclusivo em Contas a Pagar & Despesas**:
   - O aplicativo NÃO processa receitas, salários nem entradas de dinheiro.
   - O objetivo é dar ao usuário controle estrito de suas dívidas, faturas, assinaturas e compromissos futuros.
2. **Contas Bancárias como Referência (`FinancialAccount`)**:
   - As contas cadastradas (ex: Carteira, Nubank, Itaú, Cartão XP) atuam unicamente como **contas de referência** para vincular onde a despesa/parcela foi ou será debitada. Não há controle de saldo contábil ou conciliação bancária de ativos.
3. **Operação 100% Offline (Single Source of Truth Local)**:
   - Todo dado gerado ou manipulado pelo usuário é persistido exclusivamente no banco local **Room (SQLite)** e nas preferências locais **DataStore**. O aplicativo opera com 100% de funcionalidade sem conexão com a internet.
4. **Integridade Financeira em Centavos (`amountCents: Long`)**:
   - Todo valor monetário é um inteiro positivo em centavos, eliminando perdas ou distorções causadas por aritmética de ponto flutuante (`Double`/`Float`).
5. **Padrão MVI com Efeitos Seguros**:
   - Estados de tela são imutáveis (`UiState`), intenções de usuário são enviadas via `UiAction` e efeitos colaterais de disparo único (como Snackbars e navegação) trafegam via canal dedicado (`UiEffect`) para evitar repetições indesejadas em recomposição ou rotação de tela.
6. **Isolamento de Negócio (Clean Architecture)**:
   - A camada `domain` é 100% Kotlin puro, livre de dependências do Android Framework (`Context`, `View`, `Composable`) ou persistência (`Room`).
7. **Elegância Visual e Anti-Gigantismo**:
   - Suporte nativo a temas Claro e Escuro (*Material 3 Dark Theme*), busca inline expansível na barra superior e componentes compactos e proporcionais.

---

## 3. Entidades do Módulo Financeiro
- **Conta (`Bill`)**: Contrato mestre da despesa (título, descrição, tipo, total em centavos, categoria vinculada, conta financeira de referência).
- **Tipos de Conta (`BillType`)**:
  - `SINGLE`: Conta avulsa/pontual com vencimento único.
  - `INSTALLMENT`: Compra parcelada em N vezes com distribuição matemática precisa de centavos.
  - `RECURRING`: Conta mensal contínua ou assinatura (aluguel, internet, streaming).
- **Parcela / Vencimento (`BillInstallment`)**: Ocorrência individual com data de vencimento, status (`PENDING`, `PAID`, `OVERDUE`) e data de liquidação (`paidAt`).
- **Categoria (`Category`) e Subcategoria (`Subcategory`)**: Categorização visual com cor identificadora e auto-seeding inicial.
- **Conta Financeira de Referência (`FinancialAccount`)**: Banco ou carteira de referência associada ao lançamento.
- **Forma de Pagamento (`PaymentMethod`)**: Meio de pagamento (Pix, Cartão de Crédito, Boleto, Dinheiro, etc.).
- **Contato / Favorecido (`Contact`)**: Beneficiário a quem o pagamento é devido.
- **Orçamento (`Budget`)**: Teto mensal estipulado por categoria para contenção de gastos.
- **Meta Financeira (`Goal`)**: Alvos de reserva financeira com barra de progresso.
