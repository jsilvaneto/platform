---
name: financial-domain-guard
description: >-
  Use this skill to enforce personal finance domain rules, exact cents integer representation
  (amountCents: Long), installment divisions, and offline-first data consistency.
---

# Skill: Financial Domain Guard (Regras Financeiras)

Esta skill estabelece as regras e invariantes financeiras inegociáveis para o desenvolvimento no aplicativo **Platform**.

## 1. Valores Monetários em Centavos Inteiros
- **Proibição de Float / Double**: NUNCA utilize tipos de ponto flutuante para armazenar ou realizar cálculos de dinheiro.
- **Tipo Canônico**: Todo valor monetário é um inteiro positivo em centavos representado pelo tipo `Long` (`amountCents: Long`).
  - R$ 1,00 = `100L`
  - R$ 150,50 = `15050L`
- Utilize sempre `CurrencyUtils.formatCentsToCurrency(amountCents)` para exibição em tela.

## 2. Tipos de Contas
- **Avulsa (`SINGLE`)**: Gera exatamente 1 parcela vinculada com valor total e vencimento único.
- **Parcelada (`INSTALLMENT`)**:
  - `totalInstallments`: Número de parcelas (mínimo 2).
  - **Divisão Exata de Centavos**:
    ```kotlin
    val base = totalAmountCents / totalInstallments
    val remainder = totalAmountCents % totalInstallments
    val firstInstallment = base + remainder
    val otherInstallments = base
    ```
    A soma das parcelas DEVE ser identicamente igual a `totalAmountCents`.
  - Os vencimentos das parcelas subsequentes são gerados somando 1 mês (`DateUtils.addMonths(dueDate, i)`).
- **Recorrente (`RECURRING`)**: Contas periódicas (aluguel, assinaturas) geram projeções contínuas de vencimentos mensais mantendo o valor fixo.

## 3. Estados de Parcelas
- `PENDING`: Parcela em aberto com data de vencimento futura ou no dia atual.
- `OVERDUE`: Parcela não paga com data de vencimento inferior ao dia atual (`dueDate < now`).
- `PAID`: Parcela liquidada, com `paidAt` preenchido.
