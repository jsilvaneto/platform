---
name: financial-domain-guard
description: >-
  Use this skill to enforce personal finance domain rules for bills, payables, installment division,
  recurring commitments, reference financial accounts, and exact cents integer representation (amountCents: Long).
---

# Skill: Financial Domain Guard (Regras Financeiras & Contas a Pagar)

Esta skill estabelece as regras e invariantes inegociáveis de finanças pessoais para o desenvolvimento no aplicativo **Platform**, especializado no controle rigoroso de **Contas a Pagar, Parcelamentos, Recorrências e Orçamentos**.

---

## 1. Escopo Estrito do Domínio: Contas a Pagar & Despesas
O aplicativo é focado exclusivamente no planejamento e liquidação de **Contas a Pagar**:
- **Sem Tratamento de Receitas**: O sistema NÃO trata salários, rendas ou entradas financeiras.
- **Contas Bancárias como Referência**: A entidade `FinancialAccount` (ex: Nubank, Itaú, Dinheiro/Carteira) atua estritamente como **conta de referência e vínculo de pagamento**. Ela **não gerencia saldo contábil**, não processa transferências entre contas e serve para indicar de onde ou por onde a conta/parcela foi ou será debitada.
- **Favorecidos / Beneficiários**: Contatos (`Contact`) representam as pessoas, empresas ou fornecedores a quem os pagamentos são devidos.

---

## 2. Valores Monetários em Centavos Inteiros (`amountCents: Long`)
- **Proibição de Float / Double**: NUNCA utilize tipos de ponto flutuante para armazenar ou calcular valores monetários.
- **Tipo Canônico**: Todo valor monetário é um inteiro positivo em centavos representado por `Long` (`amountCents: Long`).
  - R$ 1,00 = `100L`
  - R$ 150,50 = `15050L`
  - R$ 1.250,00 = `125000L`
- **Exibição Formatada**: Utilize exclusivamente `CurrencyUtils.formatCentsToCurrency(amountCents)`. Proibido concatenar strings com `"R$ " + valor`.

---

## 3. Tipologia de Contas e Regras de Parcelamento (`BillType`)
As contas cadastradas (`Bill`) se dividem estritamente em:

1. **Avulsa (`SINGLE`)**:
   - Gera exatamente 1 parcela com vencimento único e valor integral.
2. **Parcelada (`INSTALLMENT`)**:
   - Compras divididas em $N$ vezes (mínimo 2 parcelas).
   - `totalAmountCents`: Valor total da compra.
   - **Divisão Exata de Centavos (Sem dízimas ou perdas)**:
     ```kotlin
     val totalInstallments = bill.totalInstallments.coerceAtLeast(1)
     val baseAmount = bill.totalAmountCents / totalInstallments
     val remainder = bill.totalAmountCents % totalInstallments
     // O resto da divisão por inteiros é somado obrigatoriamente na primeira parcela
     val firstInstallment = baseAmount + remainder
     val otherInstallments = baseAmount
     ```
     *Invariante*: A soma de todas as parcelas DEVE ser identicamente igual a `bill.totalAmountCents`.
3. **Recorrente (`RECURRING`)**:
   - Assinaturas e compromissos contínuos (aluguel, condomínio, streaming, internet).
   - `totalAmountCents`: Valor mensal fixo da assinatura.
   - **Natureza Contínua**: NÃO é um carnê de amortização com término fixo. Não possui "saldo restante de quitação" no ano, mas sim o valor do ciclo mensal e a projeção dos próximos vencimentos.

---

## 4. Estados de Parcelas e Vencimento (`BillStatus`)
- `PENDING`: Parcela em aberto com data de vencimento futura ou no dia de hoje (`dueDate >= now && !isPaid`).
- `OVERDUE`: Parcela não liquidada com data de vencimento vencida (`dueDate < now && !isPaid`).
- `PAID`: Parcela liquidada, com timestamp `paidAt` preenchido.
- *Propriedade auxiliar*: `val isPaid: Boolean get() = paidAt != null || status == BillStatus.PAID`.

---

## 5. Orçamentos e Tetos de Gastos por Categoria
- A entidade `Budget` define o teto mensal máximo estipulado pelo usuário para cada categoria de despesa.
- O cálculo do consumo (`spentCents`) soma todas as parcelas com vencimento no mês selecionado associadas àquela categoria.
- Alertas de estouro visual (`isExceeded = spent > limitAmountCents`) devem ser sinalizados com a cor de erro do tema.

---

## 6. Integridade Referencial e Proteção de Histórico
- **Exclusão Segura**: Ao tentar excluir uma Categoria, Conta Financeira ou Contato, verifique se existem contas ou parcelas ativas vinculadas.
- **Prevenção de Dados Órfãos**: Nunca realize exclusões cegas no banco que deixem IDs de categoria ou conta apontando para registros inexistentes.
