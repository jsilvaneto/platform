package com.platform.app.domain.usecase

import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.BillType
import com.platform.app.domain.model.CreditCard
import com.platform.app.domain.model.CreditCardCalculator
import com.platform.app.domain.model.CreditCardInvoice
import com.platform.app.domain.model.CreditCardWithInvoiceSummary
import com.platform.app.domain.model.InvoiceStatus
import com.platform.app.domain.repository.FinancialRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Caso de uso central para cálculo do limite consumido e resumos dos cartões de crédito.
 *
 * Regra Única de Limite:
 * 1. [BillType.INSTALLMENT]: Consome o saldo devedor restante de todas as parcelas não pagas.
 * 2. [BillType.RECURRING] e [BillType.SINGLE]: Consomem apenas o que está na fatura aberta ou fechada atual não paga,
 *    sem projetar ocorrências de ciclos futuros no limite do cartão.
 */
class GetCreditCardSummariesUseCase @Inject constructor(
    private val repository: FinancialRepository
) {

    operator fun invoke(): Flow<List<CreditCardWithInvoiceSummary>> {
        return invoke(System.currentTimeMillis())
    }

    operator fun invoke(currentTimestamp: Long): Flow<List<CreditCardWithInvoiceSummary>> {
        return combine(
            repository.getCreditCards(),
            repository.getAllCreditCardInvoices(),
            repository.getAllInstallments()
        ) { cards, invoices, installments ->
            calculateSummaries(cards, invoices, installments, currentTimestamp)
        }
    }

    fun calculateSummaries(
        cards: List<CreditCard>,
        allInvoices: List<CreditCardInvoice>,
        allInstallments: List<BillInstallment>,
        currentTimestamp: Long = System.currentTimeMillis()
    ): List<CreditCardWithInvoiceSummary> {
        return cards.map { card ->
            val cardInvoices = allInvoices.filter { it.creditCardId == card.id }
            val currentRefMonth = CreditCardCalculator.determineInvoiceReferenceMonth(currentTimestamp, card.closingDay)

            // Fatura atual: preferencialmente a do mês de referência atual, ou a primeira não paga
            val candidateInvoice = cardInvoices.find { it.referenceMonth == currentRefMonth }
                ?: cardInvoices.firstOrNull { it.status != InvoiceStatus.PAGA }
                ?: cardInvoices.firstOrNull()

            val currentInvoiceWithTotal = candidateInvoice?.let { inv ->
                val invTotal = allInstallments
                    .filter { it.invoiceId == inv.id }
                    .sumOf { it.amountCents }
                inv.copy(totalAmountCents = invTotal)
            }

            // Faturas ativas pendentes (abertas ou fechadas até a competência atual)
            val activeCurrentInvoiceIds = cardInvoices
                .filter { it.status != InvoiceStatus.PAGA && it.referenceMonth <= currentRefMonth }
                .map { it.id }
                .toSet()

            val targetActiveInvoiceIds = if (activeCurrentInvoiceIds.isNotEmpty()) {
                activeCurrentInvoiceIds
            } else {
                setOfNotNull(candidateInvoice?.takeIf { it.status != InvoiceStatus.PAGA }?.id)
            }

            val cardInvoiceIds = cardInvoices.map { it.id }.toSet()
            val cardInstallments = allInstallments.filter { it.invoiceId in cardInvoiceIds && !it.isPaid }

            // 1. INSTALLMENT: consome saldo de todas as parcelas não pagas (atuais e futuras)
            val installmentUsed = cardInstallments
                .filter { it.type == BillType.INSTALLMENT }
                .sumOf { it.amountCents }

            // 2. SINGLE e RECURRING: consomem apenas o que pertence à fatura atual pendente
            val singleAndRecurringUsed = cardInstallments
                .filter { (it.type == BillType.SINGLE || it.type == BillType.RECURRING) && it.invoiceId in targetActiveInvoiceIds }
                .sumOf { it.amountCents }

            val totalUsed = installmentUsed + singleAndRecurringUsed

            CreditCardWithInvoiceSummary(
                card = card,
                currentInvoice = currentInvoiceWithTotal,
                usedLimitCents = totalUsed
            )
        }
    }
}
