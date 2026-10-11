package com.platform.app.data.repository

import androidx.room.withTransaction
import com.platform.app.data.local.PlatformDatabase
import com.platform.app.data.local.dao.BillDao
import com.platform.app.data.local.dao.BillInstallmentDao
import com.platform.app.data.local.entity.BillEntity
import com.platform.app.data.local.entity.BillInstallmentEntity
import com.platform.app.domain.model.Bill
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.MonthSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Operações do agregado de Contas (Bills) e Parcelas (Installments).
 */
@Singleton
class BillDataSource @Inject constructor(
    private val database: PlatformDatabase,
    private val billDao: BillDao,
    private val installmentDao: BillInstallmentDao
) {

    fun getBills(): Flow<List<Bill>> {
        return billDao.getAll().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun getBillById(billId: String): Bill? {
        return billDao.getById(billId)?.toDomain()
    }

    fun getBillsByContact(contactId: String): Flow<List<Bill>> {
        return billDao.getBillsByContact(contactId).map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getInstallmentsByContact(contactId: String): Flow<List<BillInstallment>> {
        return installmentDao.getInstallmentsByContact(contactId).map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getPlannedInstallmentsByContact(contactId: String): Flow<List<BillInstallment>> {
        return installmentDao.getPlannedInstallmentsByContact(contactId).map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getInstallmentsForPeriod(startMillis: Long, endMillis: Long): Flow<List<BillInstallment>> {
        return installmentDao.getInstallmentsForPeriod(startMillis, endMillis).map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getAllInstallments(): Flow<List<BillInstallment>> {
        return installmentDao.getAllWithDetails().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun getInstallmentsByBillId(billId: String): List<BillInstallment> {
        return installmentDao.getInstallmentsWithDetailsByBillId(billId).map { it.toDomain() }
    }

    suspend fun getPendingInstallmentsInRange(startMillis: Long, endMillis: Long): List<BillInstallment> {
        return installmentDao.getPendingInstallmentsInRange(startMillis, endMillis).map { it.toDomain() }
    }

    suspend fun getOverduePendingInstallments(beforeMillis: Long): List<BillInstallment> {
        return installmentDao.getOverduePendingInstallments(beforeMillis).map { it.toDomain() }
    }

    suspend fun postponeInstallmentDueDate(installmentId: String, days: Int = 1) {
        val entity = installmentDao.getEntityById(installmentId) ?: return
        val newDueDate = entity.dueDate + (days.toLong() * 24L * 60L * 60L * 1000L)
        installmentDao.updateInstallmentAmountAndDate(installmentId, entity.amountCents, newDueDate)
    }

    suspend fun saveBillWithInstallments(bill: Bill, installments: List<BillInstallment>) {
        database.withTransaction {
            billDao.insert(BillEntity.fromDomain(bill))
            installmentDao.insertAll(installments.map { BillInstallmentEntity.fromDomain(it) })
        }
    }

    suspend fun addInstallments(bill: Bill, installments: List<BillInstallment>) {
        if (installments.isEmpty()) return
        database.withTransaction {
            installmentDao.insertAllIgnore(installments.map { BillInstallmentEntity.fromDomain(it) })
            val allForBill = installmentDao.getInstallmentsByBillId(bill.id)
            val maxDueDate = allForBill.maxOfOrNull { it.dueDate }
            billDao.updateBillEndDateAndTotalInstallments(
                id = bill.id,
                recurrenceEndDate = maxDueDate,
                totalInstallments = allForBill.size
            )
        }
    }

    suspend fun toggleInstallmentPayment(
        installmentId: String,
        isPaid: Boolean,
        paidTimestamp: Long?,
        actualPaymentDate: Long?
    ) {
        val status = if (isPaid) "PAID" else "PENDING"
        val effectivePaidAt = if (isPaid) (paidTimestamp ?: System.currentTimeMillis()) else null
        val effectiveActualDate = if (isPaid) (actualPaymentDate ?: effectivePaidAt) else null
        installmentDao.updatePayment(installmentId, effectivePaidAt, effectiveActualDate, status)
    }

    suspend fun updateInstallmentsActualPaymentDateBatch(
        installmentIds: List<String>,
        actualPaymentDate: Long
    ) {
        if (installmentIds.isEmpty()) return
        installmentDao.updateActualPaymentDateBatch(installmentIds, actualPaymentDate)
    }

    suspend fun updateInstallment(
        installmentId: String,
        newAmountCents: Long,
        newDueDate: Long
    ) {
        database.withTransaction {
            installmentDao.updateInstallmentAmountAndDate(installmentId, newAmountCents, newDueDate)
            val entity = installmentDao.getEntityById(installmentId)
            if (entity != null) {
                val bill = billDao.getById(entity.billId)
                if (bill != null && bill.totalInstallments == 1) {
                    billDao.updateBillDetails(
                        id = bill.id,
                        title = bill.title,
                        description = bill.description,
                        totalAmountCents = newAmountCents,
                        categoryId = bill.categoryId,
                        itemId = bill.itemId,
                        contactId = bill.contactId,
                        financialAccountId = bill.financialAccountId,
                        paymentMethodId = bill.paymentMethodId
                    )
                }
            }
        }
    }

    suspend fun updateBillAndInstallment(
        installmentId: String,
        billId: String,
        title: String,
        description: String,
        amountCents: Long,
        dueDate: Long,
        categoryId: String?,
        itemId: String?,
        contactId: String?,
        financialAccountId: String?,
        paymentMethodId: String?
    ) {
        database.withTransaction {
            billDao.updateBillDetails(
                id = billId,
                title = title.trim(),
                description = description.trim(),
                totalAmountCents = amountCents,
                categoryId = categoryId,
                itemId = itemId,
                contactId = contactId,
                financialAccountId = financialAccountId,
                paymentMethodId = paymentMethodId
            )
            installmentDao.updateInstallmentDetails(
                id = installmentId,
                amountCents = amountCents,
                dueDate = dueDate,
                itemId = itemId,
                contactId = contactId,
                financialAccountId = financialAccountId,
                paymentMethodId = paymentMethodId
            )
        }
    }

    suspend fun updateFutureInstallmentsAmount(
        billId: String,
        fromDueDate: Long,
        newAmountCents: Long
    ) {
        database.withTransaction {
            installmentDao.updateAmountForPendingFromDueDate(billId, fromDueDate, newAmountCents)
            billDao.updateBillTotalAmount(billId, newAmountCents)
        }
    }

    suspend fun deleteSingleInstallment(installmentId: String) {
        database.withTransaction {
            val entity = installmentDao.getEntityById(installmentId)
            if (entity != null) {
                installmentDao.deleteById(installmentId)
                val remaining = installmentDao.getInstallmentsByBillId(entity.billId)
                if (remaining.isEmpty()) {
                    billDao.deleteById(entity.billId)
                } else {
                    val maxDueDate = remaining.maxOfOrNull { it.dueDate }
                    billDao.updateBillEndDateAndTotalInstallments(
                        id = entity.billId,
                        recurrenceEndDate = maxDueDate,
                        totalInstallments = remaining.size
                    )
                }
            }
        }
    }

    suspend fun deleteFutureInstallments(billId: String, fromDueDate: Long) {
        database.withTransaction {
            installmentDao.deletePendingFromDueDate(billId, fromDueDate)
            val remaining = installmentDao.getInstallmentsByBillId(billId)
            if (remaining.isEmpty()) {
                billDao.deleteById(billId)
            } else {
                val maxDueDate = remaining.maxOfOrNull { it.dueDate }
                billDao.updateBillEndDateAndTotalInstallments(
                    id = billId,
                    recurrenceEndDate = maxDueDate,
                    totalInstallments = remaining.size
                )
            }
        }
    }

    suspend fun pauseRecurringBill(billId: String, pausedUntil: Long?) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfToday = cal.timeInMillis

        database.withTransaction {
            billDao.updatePauseState(billId, isPaused = true, pausedUntil = pausedUntil)
            // Apenas ocorrências a partir de hoje viram SKIPPED no período.
            // Atrasadas anteriores (dueDate < startOfToday) continuam PENDING e visíveis.
            installmentDao.markPendingAsSkippedInRange(
                billId = billId,
                startDate = startOfToday,
                endDate = pausedUntil
            )
        }
    }

    suspend fun resumeRecurringBill(billId: String) {
        database.withTransaction {
            billDao.updatePauseState(billId, isPaused = false, pausedUntil = null)
        }
    }

    suspend fun pauseRecurringBill(billId: String, isPaused: Boolean) {
        if (isPaused) {
            pauseRecurringBill(billId, pausedUntil = null)
        } else {
            resumeRecurringBill(billId)
        }
    }

    suspend fun stopRecurringBill(billId: String, fromDueDate: Long? = null) {
        database.withTransaction {
            val remaining = installmentDao.getInstallmentsByBillId(billId)
            val now = System.currentTimeMillis()
            val cutoff = fromDueDate ?: remaining
                .filter { it.dueDate >= now && it.paidAt == null }
                .minOfOrNull { it.dueDate }
                ?: now

            // Remove APENAS futuras pendentes depois da data de corte
            installmentDao.deletePendingAfterDueDate(billId, cutoff)

            val updatedRemaining = installmentDao.getInstallmentsByBillId(billId)
            val maxDueDate = updatedRemaining.maxOfOrNull { it.dueDate } ?: cutoff

            billDao.stopRecurringBillWithDate(
                id = billId,
                recurrenceEndDate = maxDueDate,
                endedAt = now,
                totalInstallments = updatedRemaining.size
            )
        }
    }

    suspend fun reactivateRecurringBill(billId: String) {
        database.withTransaction {
            billDao.reactivateRecurringBill(billId)
        }
    }

    suspend fun deleteRecurringBill(billId: String, keepPaidHistory: Boolean) {
        database.withTransaction {
            if (keepPaidHistory) {
                installmentDao.deleteAllPending(billId)
                billDao.markBillEndedAndDeleted(billId, System.currentTimeMillis())
            } else {
                installmentDao.deleteByBillId(billId)
                billDao.deleteById(billId)
            }
        }
    }

    suspend fun deleteBill(billId: String) {
        billDao.deleteById(billId)
    }

    fun getMonthSummary(startMillis: Long, endMillis: Long, nowMillis: Long): Flow<MonthSummary> {
        return installmentDao.getMonthSummary(startMillis, endMillis, nowMillis).map { it.toDomain() }
    }

    fun getOpenAmountByContact(): Flow<Map<String, Long>> {
        return installmentDao.getOpenAmountByContact().map { list ->
            list.associate { it.contactId to it.totalOpenCents }
        }
    }
}
