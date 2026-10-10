package com.platform.app.data.repository

import com.platform.app.data.local.dao.BillInstallmentDao
import com.platform.app.data.local.dao.CreditCardDao
import com.platform.app.data.local.entity.CreditCardEntity
import com.platform.app.data.local.entity.CreditCardInvoiceEntity
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.CardDependencies
import com.platform.app.domain.model.CreditCard
import com.platform.app.domain.model.CreditCardCalculator
import com.platform.app.domain.model.CreditCardInvoice
import com.platform.app.domain.model.InvoiceStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Operações do agregado de Cartões de Crédito e Faturas.
 */
@Singleton
class CreditCardDataSource @Inject constructor(
    private val creditCardDao: CreditCardDao,
    private val installmentDao: BillInstallmentDao
) {

    fun getCreditCards(): Flow<List<CreditCard>> {
        return creditCardDao.getAllCards().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun saveCreditCard(card: CreditCard) {
        creditCardDao.upsertCard(CreditCardEntity.fromDomain(card))
    }

    suspend fun deleteCreditCard(cardId: String) {
        creditCardDao.deleteCardById(cardId)
    }

    fun getCreditCardInvoices(cardId: String): Flow<List<CreditCardInvoice>> {
        return combine(creditCardDao.getInvoicesForCard(cardId), installmentDao.getInvoiceTotals()) { invoices, totals ->
            val totalsMap = totals.associate { it.invoiceId to it.totalAmountCents }
            invoices.map { invEntity ->
                invEntity.toDomain(totalAmountCents = totalsMap[invEntity.id] ?: 0L)
            }
        }
    }

    fun getAllCreditCardInvoices(): Flow<List<CreditCardInvoice>> {
        return combine(creditCardDao.getAllInvoices(), installmentDao.getInvoiceTotals()) { invoices, totals ->
            val totalsMap = totals.associate { it.invoiceId to it.totalAmountCents }
            invoices.map { invEntity ->
                invEntity.toDomain(totalAmountCents = totalsMap[invEntity.id] ?: 0L)
            }
        }
    }

    fun getInvoicesForPeriod(startMillis: Long, endMillis: Long): Flow<List<CreditCardInvoice>> {
        return combine(creditCardDao.getInvoicesForDueDateRange(startMillis, endMillis), installmentDao.getInvoiceTotals()) { invoices, totals ->
            val totalsMap = totals.associate { it.invoiceId to it.totalAmountCents }
            invoices.map { invEntity ->
                invEntity.toDomain(totalAmountCents = totalsMap[invEntity.id] ?: 0L)
            }
        }
    }

    suspend fun getPendingInvoicesInRange(startMillis: Long, endMillis: Long): List<CreditCardInvoice> {
        val invoices = creditCardDao.getPendingInvoicesInRange(startMillis, endMillis)
        val totals = installmentDao.getInvoiceTotalsList().associate { it.invoiceId to it.totalAmountCents }
        return invoices.map { it.toDomain(totalAmountCents = totals[it.id] ?: 0L) }
    }

    suspend fun getOverdueInvoices(beforeMillis: Long): List<CreditCardInvoice> {
        val invoices = creditCardDao.getOverdueInvoices(beforeMillis)
        val totals = installmentDao.getInvoiceTotalsList().associate { it.invoiceId to it.totalAmountCents }
        return invoices.map { it.toDomain(totalAmountCents = totals[it.id] ?: 0L) }
    }

    fun getInstallmentsForInvoice(invoiceId: String): Flow<List<BillInstallment>> {
        return installmentDao.getInstallmentsForInvoice(invoiceId).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun getOrCreateInvoiceForMonth(cardId: String, referenceMonth: String): CreditCardInvoice {
        val existing = creditCardDao.getInvoiceByMonth(cardId, referenceMonth)
        if (existing != null) {
            return existing.toDomain()
        }

        val card = creditCardDao.getCardById(cardId) ?: return CreditCardInvoice(
            creditCardId = cardId,
            referenceMonth = referenceMonth,
            closingDate = System.currentTimeMillis(),
            dueDate = System.currentTimeMillis()
        )

        val cal = Calendar.getInstance()
        val parts = referenceMonth.split("-")
        val year = parts.getOrNull(0)?.toIntOrNull() ?: cal.get(Calendar.YEAR)
        val month = (parts.getOrNull(1)?.toIntOrNull() ?: (cal.get(Calendar.MONTH) + 1)) - 1

        cal.set(year, month, card.closingDay, 23, 59, 59)
        val closingDate = cal.timeInMillis

        cal.set(year, month, card.dueDay, 23, 59, 59)
        if (card.dueDay <= card.closingDay) {
            cal.add(Calendar.MONTH, 1)
        }
        val dueDate = cal.timeInMillis

        val newInvoice = CreditCardInvoice(
            creditCardId = cardId,
            referenceMonth = referenceMonth,
            closingDate = closingDate,
            dueDate = dueDate,
            status = InvoiceStatus.ABERTA
        )
        creditCardDao.insertInvoice(CreditCardInvoiceEntity.fromDomain(newInvoice))
        return newInvoice
    }

    suspend fun materializeRecurringForInvoice(cardId: String, invoiceId: String, referenceMonth: String): Int {
        val card = creditCardDao.getCardById(cardId) ?: return 0
        val invoice = creditCardDao.getInvoiceById(invoiceId) ?: return 0
        val unattached = installmentDao.getUnattachedRecurringInstallmentsForCard(cardId)
        val matchingIds = unattached.filter { inst ->
            CreditCardCalculator.determineInvoiceReferenceMonth(inst.dueDate, card.closingDay) == referenceMonth
        }.map { it.id }

        if (matchingIds.isNotEmpty()) {
            installmentDao.attachInstallmentsToInvoice(
                installmentIds = matchingIds,
                invoiceId = invoice.id,
                invoiceDueDate = invoice.dueDate
            )
        }
        return matchingIds.size
    }

    suspend fun materializeRecurringCardInvoices(referenceTimeMillis: Long): Int {
        val cards = creditCardDao.getAllCardsList()
        var totalAttached = 0

        for (card in cards) {
            val currentRefMonth = CreditCardCalculator.determineInvoiceReferenceMonth(
                purchaseTimestamp = referenceTimeMillis,
                closingDay = card.closingDay
            )

            val unattached = installmentDao.getUnattachedRecurringInstallmentsForCard(card.id)
            if (unattached.isEmpty()) continue

            val byRefMonth = unattached.groupBy {
                CreditCardCalculator.determineInvoiceReferenceMonth(it.dueDate, card.closingDay)
            }

            for ((refMonth, _) in byRefMonth) {
                if (refMonth <= currentRefMonth) {
                    val invoice = getOrCreateInvoiceForMonth(card.id, refMonth)
                    val attached = materializeRecurringForInvoice(card.id, invoice.id, refMonth)
                    totalAttached += attached
                }
            }
        }

        return totalAttached
    }

    suspend fun payInvoice(invoiceId: String, actualPaymentDate: Long?) {
        val now = System.currentTimeMillis()
        val effectiveActualDate = actualPaymentDate ?: now
        val invoice = creditCardDao.getInvoiceById(invoiceId)
        if (invoice != null) {
            materializeRecurringForInvoice(invoice.creditCardId, invoice.id, invoice.referenceMonth)
        }
        creditCardDao.updateInvoiceStatus(invoiceId, InvoiceStatus.PAGA.name)
        installmentDao.updatePaymentByInvoiceId(
            invoiceId = invoiceId,
            paidAt = now,
            actualPaymentDate = effectiveActualDate,
            status = "PAID"
        )
    }

    suspend fun reopenInvoice(invoiceId: String) {
        creditCardDao.updateInvoiceStatus(invoiceId, InvoiceStatus.ABERTA.name)
        installmentDao.updatePaymentByInvoiceId(
            invoiceId = invoiceId,
            paidAt = null,
            actualPaymentDate = null,
            status = "PENDING"
        )
    }

    suspend fun getCardDependencies(cardId: String): CardDependencies {
        val invCount = creditCardDao.countInvoicesForCard(cardId)
        val instCount = installmentDao.countInstallmentsForCard(cardId)
        return CardDependencies(
            invoiceCount = invCount,
            installmentCount = instCount,
            hasActiveDependencies = invCount > 0 || instCount > 0
        )
    }
}
