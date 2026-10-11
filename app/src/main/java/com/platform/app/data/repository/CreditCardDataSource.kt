package com.platform.app.data.repository

import com.platform.app.core.util.DateUtils
import com.platform.app.data.local.dao.BillInstallmentDao
import com.platform.app.data.local.dao.CreditCardDao
import com.platform.app.data.local.dao.SyncDao
import com.platform.app.data.local.entity.CreditCardEntity
import com.platform.app.data.local.entity.CreditCardInvoiceEntity
import com.platform.app.data.local.entity.SyncOutboxEntity
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
    private val installmentDao: BillInstallmentDao,
    private val syncDao: SyncDao? = null
) {

    fun getCreditCards(): Flow<List<CreditCard>> {
        return creditCardDao.getAllCards().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun saveCreditCard(card: CreditCard) {
        creditCardDao.upsertCard(CreditCardEntity.fromDomain(card))
        syncDao?.enqueue(
            SyncOutboxEntity(
                entityType = "CREDIT_CARD",
                entityId = card.id,
                operation = "UPSERT",
                payload = """{"id":"${card.id}","name":"${card.name}"}"""
            )
        )
    }

    suspend fun deleteCreditCard(cardId: String) {
        creditCardDao.deleteCardById(cardId)
        syncDao?.enqueue(
            SyncOutboxEntity(
                entityType = "CREDIT_CARD",
                entityId = cardId,
                operation = "DELETE",
                payload = """{"id":"$cardId"}"""
            )
        )
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
        val startDate = DateUtils.millisToLocalDate(startMillis)
        val endDate = DateUtils.millisToLocalDate(endMillis)
        return combine(creditCardDao.getInvoicesForDueDateRange(startDate, endDate), installmentDao.getInvoiceTotals()) { invoices, totals ->
            val totalsMap = totals.associate { it.invoiceId to it.totalAmountCents }
            invoices.map { invEntity ->
                invEntity.toDomain(totalAmountCents = totalsMap[invEntity.id] ?: 0L)
            }
        }
    }

    suspend fun getPendingInvoicesInRange(startMillis: Long, endMillis: Long): List<CreditCardInvoice> {
        val startDate = DateUtils.millisToLocalDate(startMillis)
        val endDate = DateUtils.millisToLocalDate(endMillis)
        val invoices = creditCardDao.getPendingInvoicesInRange(startDate, endDate)
        val totals = installmentDao.getInvoiceTotalsList().associate { it.invoiceId to it.totalAmountCents }
        return invoices.map { it.toDomain(totalAmountCents = totals[it.id] ?: 0L) }
    }

    suspend fun getOverdueInvoices(beforeMillis: Long): List<CreditCardInvoice> {
        val beforeDate = DateUtils.millisToLocalDate(beforeMillis)
        val invoices = creditCardDao.getOverdueInvoices(beforeDate)
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

        val today = java.time.LocalDate.now()
        val card = creditCardDao.getCardById(cardId) ?: return CreditCardInvoice(
            creditCardId = cardId,
            referenceMonth = referenceMonth,
            closingDate = today,
            dueDate = today
        )

        val parts = referenceMonth.split("-")
        val year = parts.getOrNull(0)?.toIntOrNull() ?: today.year
        val month = (parts.getOrNull(1)?.toIntOrNull() ?: today.monthValue).coerceIn(1, 12)

        val maxClosingDay = java.time.YearMonth.of(year, month).lengthOfMonth()
        val closingDate = java.time.LocalDate.of(year, month, card.closingDay.coerceIn(1, maxClosingDay))

        var dueYear = year
        var dueMonth = month
        if (card.dueDay <= card.closingDay) {
            val nextMonth = java.time.YearMonth.of(year, month).plusMonths(1)
            dueYear = nextMonth.year
            dueMonth = nextMonth.monthValue
        }
        val maxDueDay = java.time.YearMonth.of(dueYear, dueMonth).lengthOfMonth()
        val dueDate = java.time.LocalDate.of(dueYear, dueMonth, card.dueDay.coerceIn(1, maxDueDay))

        val newInvoice = CreditCardInvoice(
            creditCardId = cardId,
            referenceMonth = referenceMonth,
            closingDate = closingDate,
            dueDate = dueDate,
            status = InvoiceStatus.ABERTA
        )
        creditCardDao.insertInvoice(CreditCardInvoiceEntity.fromDomain(newInvoice))
        syncDao?.enqueue(
            SyncOutboxEntity(
                entityType = "CREDIT_CARD_INVOICE",
                entityId = newInvoice.id,
                operation = "INSERT",
                payload = """{"id":"${newInvoice.id}","referenceMonth":"$referenceMonth"}"""
            )
        )
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
        syncDao?.enqueue(
            SyncOutboxEntity(
                entityType = "CREDIT_CARD_INVOICE",
                entityId = invoiceId,
                operation = "UPDATE",
                payload = """{"id":"$invoiceId","status":"PAGA","paidAt":$now}"""
            )
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
        syncDao?.enqueue(
            SyncOutboxEntity(
                entityType = "CREDIT_CARD_INVOICE",
                entityId = invoiceId,
                operation = "UPDATE",
                payload = """{"id":"$invoiceId","status":"ABERTA"}"""
            )
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
