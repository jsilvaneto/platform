package com.platform.app.data.repository

import androidx.room.withTransaction
import com.platform.app.core.util.DateUtils
import com.platform.app.data.local.PlatformDatabase
import com.platform.app.data.local.dao.BillDao
import com.platform.app.data.local.dao.BillInstallmentDao
import com.platform.app.data.local.dao.SyncDao
import com.platform.app.data.local.entity.BillEntity
import com.platform.app.data.local.entity.BillInstallmentEntity
import com.platform.app.data.local.entity.SyncOutboxEntity
import com.platform.app.domain.model.AttentionCounts
import com.platform.app.domain.model.Bill
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.FreeingUpCommitment
import com.platform.app.domain.model.MonthSummary
import com.platform.app.domain.model.OpenInstallmentsSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.platform.app.data.local.dao.PaymentDao
import com.platform.app.data.local.entity.PaymentEntity
import java.time.LocalDate
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Operações do agregado de Contas (Bills) e Parcelas (Installments).
 */
@Singleton
class BillDataSource @Inject constructor(
    private val database: PlatformDatabase,
    private val billDao: BillDao,
    private val installmentDao: BillInstallmentDao,
    private val syncDao: SyncDao? = null,
    private val paymentDao: PaymentDao? = null,
    private val paymentDataSource: InstallmentPaymentDataSource? = null,
    private val reportingDataSource: BillReportingDataSource? = null
) {
    private val effectivePaymentDataSource by lazy {
        paymentDataSource ?: paymentDao?.let {
            InstallmentPaymentDataSource(database, installmentDao, it, syncDao)
        }
    }
    private val effectiveReportingDataSource by lazy {
        reportingDataSource ?: BillReportingDataSource(billDao, installmentDao)
    }

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
        val startDate = DateUtils.millisToLocalDate(startMillis)
        val endDate = DateUtils.millisToLocalDate(endMillis)
        return installmentDao.getInstallmentsForPeriod(startDate, endDate).map { list ->
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
        val startDate = DateUtils.millisToLocalDate(startMillis)
        val endDate = DateUtils.millisToLocalDate(endMillis)
        return installmentDao.getPendingInstallmentsInRange(startDate, endDate).map { it.toDomain() }
    }

    suspend fun getOverduePendingInstallments(beforeMillis: Long): List<BillInstallment> {
        val beforeDate = DateUtils.millisToLocalDate(beforeMillis)
        return installmentDao.getOverduePendingInstallments(beforeDate).map { it.toDomain() }
    }

    suspend fun postponeInstallmentDueDate(installmentId: String, days: Int = 1) {
        val entity = installmentDao.getEntityById(installmentId) ?: return
        val newDueDate = entity.dueDate.plusDays(days.toLong())
        installmentDao.updateInstallmentAmountAndDate(installmentId, entity.amountCents, newDueDate)
        syncDao?.enqueue(
            SyncOutboxEntity(
                entityType = "BILL_INSTALLMENT",
                entityId = installmentId,
                operation = "UPDATE",
                payload = """{"id":"$installmentId","dueDate":"$newDueDate"}"""
            )
        )
    }

    suspend fun saveBillWithInstallments(bill: Bill, installments: List<BillInstallment>) {
        database.withTransaction {
            billDao.insert(BillEntity.fromDomain(bill))
            installmentDao.insertAll(installments.map { BillInstallmentEntity.fromDomain(it) })

            syncDao?.enqueue(
                SyncOutboxEntity(
                    entityType = "BILL",
                    entityId = bill.id,
                    operation = "INSERT",
                    payload = """{"id":"${bill.id}","title":"${bill.title}"}"""
                )
            )
            syncDao?.enqueueAll(
                installments.map { inst ->
                    SyncOutboxEntity(
                        entityType = "BILL_INSTALLMENT",
                        entityId = inst.id,
                        operation = "INSERT",
                        payload = """{"id":"${inst.id}","billId":"${inst.billId}","amountCents":${inst.amountCents}}"""
                    )
                }
            )

            effectivePaymentDataSource?.recordPrePaidInstallments(installments)
        }
    }

    suspend fun addInstallments(bill: Bill, installments: List<BillInstallment>) {
        if (installments.isEmpty()) return
        database.withTransaction {
            installmentDao.insertAllIgnore(installments.map { BillInstallmentEntity.fromDomain(it) })
            val allForBill = installmentDao.getInstallmentsByBillId(bill.id)
            val maxDueDate = allForBill.maxOfOrNull { DateUtils.localDateToMillis(it.dueDate) }
            billDao.updateBillEndDateAndTotalInstallments(
                id = bill.id,
                recurrenceEndDate = maxDueDate,
                totalInstallments = allForBill.size
            )

            syncDao?.enqueue(
                SyncOutboxEntity(
                    entityType = "BILL",
                    entityId = bill.id,
                    operation = "UPDATE",
                    payload = """{"id":"${bill.id}","totalInstallments":${allForBill.size}}"""
                )
            )
            syncDao?.enqueueAll(
                installments.map { inst ->
                    SyncOutboxEntity(
                        entityType = "BILL_INSTALLMENT",
                        entityId = inst.id,
                        operation = "INSERT",
                        payload = """{"id":"${inst.id}","billId":"${inst.billId}"}"""
                    )
                }
            )
        }
    }

    suspend fun toggleInstallmentPayment(
        installmentId: String,
        isPaid: Boolean,
        paidTimestamp: Long?,
        actualPaymentDate: Long?
    ) {
        val pds = effectivePaymentDataSource
        if (pds != null) {
            pds.toggleInstallmentPayment(installmentId, isPaid, paidTimestamp, actualPaymentDate)
        } else {
            val status = if (isPaid) "PAID" else "PENDING"
            val effectivePaidAt = if (isPaid) (paidTimestamp ?: System.currentTimeMillis()) else null
            val effectiveActualDate = if (isPaid) (actualPaymentDate ?: effectivePaidAt) else null
            installmentDao.updatePayment(installmentId, effectivePaidAt, effectiveActualDate, status)
            syncDao?.enqueue(
                SyncOutboxEntity(
                    entityType = "BILL_INSTALLMENT",
                    entityId = installmentId,
                    operation = "UPDATE",
                    payload = """{"id":"$installmentId","status":"$status","paidAt":$effectivePaidAt,"actualPaymentDate":$effectiveActualDate}"""
                )
            )
        }
    }

    suspend fun recordInstallmentPayment(
        installmentId: String,
        amountCents: Long,
        paymentDate: Long,
        paymentMethodId: String? = null,
        financialAccountId: String? = null,
        interestCents: Long = 0L,
        fineCents: Long = 0L,
        discountCents: Long = 0L,
        notes: String? = null
    ) {
        effectivePaymentDataSource?.recordInstallmentPayment(
            installmentId = installmentId,
            amountCents = amountCents,
            paymentDate = paymentDate,
            paymentMethodId = paymentMethodId,
            financialAccountId = financialAccountId,
            interestCents = interestCents,
            fineCents = fineCents,
            discountCents = discountCents,
            notes = notes
        )
    }

    suspend fun updateInstallmentsActualPaymentDateBatch(
        installmentIds: List<String>,
        actualPaymentDate: Long
    ) {
        if (installmentIds.isEmpty()) return
        installmentDao.updateActualPaymentDateBatch(installmentIds, actualPaymentDate)

        syncDao?.enqueueAll(
            installmentIds.map { id ->
                SyncOutboxEntity(
                    entityType = "BILL_INSTALLMENT",
                    entityId = id,
                    operation = "UPDATE",
                    payload = """{"id":"$id","actualPaymentDate":$actualPaymentDate}"""
                )
            }
        )
    }

    suspend fun updateInstallment(
        installmentId: String,
        newAmountCents: Long,
        newDueDate: Long
    ) {
        val newDate = DateUtils.millisToLocalDate(newDueDate)
        database.withTransaction {
            installmentDao.updateInstallmentAmountAndDate(installmentId, newAmountCents, newDate)
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
                    syncDao?.enqueue(
                        SyncOutboxEntity(
                            entityType = "BILL",
                            entityId = bill.id,
                            operation = "UPDATE",
                            payload = """{"id":"${bill.id}","totalAmountCents":$newAmountCents}"""
                        )
                    )
                }
            }
            syncDao?.enqueue(
                SyncOutboxEntity(
                    entityType = "BILL_INSTALLMENT",
                    entityId = installmentId,
                    operation = "UPDATE",
                    payload = """{"id":"$installmentId","amountCents":$newAmountCents,"dueDate":"$newDate"}"""
                )
            )
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
        val dueDateLocal = DateUtils.millisToLocalDate(dueDate)
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
                dueDate = dueDateLocal,
                itemId = itemId,
                contactId = contactId,
                financialAccountId = financialAccountId,
                paymentMethodId = paymentMethodId
            )

            syncDao?.enqueue(
                SyncOutboxEntity(
                    entityType = "BILL",
                    entityId = billId,
                    operation = "UPDATE",
                    payload = """{"id":"$billId","title":"$title","totalAmountCents":$amountCents}"""
                )
            )
            syncDao?.enqueue(
                SyncOutboxEntity(
                    entityType = "BILL_INSTALLMENT",
                    entityId = installmentId,
                    operation = "UPDATE",
                    payload = """{"id":"$installmentId","amountCents":$amountCents,"dueDate":"$dueDateLocal"}"""
                )
            )
        }
    }

    suspend fun updateFutureInstallmentsAmount(
        billId: String,
        fromDueDate: Long,
        newAmountCents: Long
    ) {
        val fromDueDateLocal = DateUtils.millisToLocalDate(fromDueDate)
        database.withTransaction {
            installmentDao.updateAmountForPendingFromDueDate(billId, fromDueDateLocal, newAmountCents)
            billDao.updateBillTotalAmount(billId, newAmountCents)

            syncDao?.enqueue(
                SyncOutboxEntity(
                    entityType = "BILL",
                    entityId = billId,
                    operation = "UPDATE",
                    payload = """{"id":"$billId","totalAmountCents":$newAmountCents}"""
                )
            )
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
                    syncDao?.enqueue(
                        SyncOutboxEntity(
                            entityType = "BILL",
                            entityId = entity.billId,
                            operation = "DELETE",
                            payload = """{"id":"${entity.billId}"}"""
                        )
                    )
                } else {
                    val maxDueDate = remaining.maxOfOrNull { DateUtils.localDateToMillis(it.dueDate) }
                    billDao.updateBillEndDateAndTotalInstallments(
                        id = entity.billId,
                        recurrenceEndDate = maxDueDate,
                        totalInstallments = remaining.size
                    )
                }

                syncDao?.enqueue(
                    SyncOutboxEntity(
                        entityType = "BILL_INSTALLMENT",
                        entityId = installmentId,
                        operation = "DELETE",
                        payload = """{"id":"$installmentId"}"""
                    )
                )
            }
        }
    }

    suspend fun deleteFutureInstallments(billId: String, fromDueDate: Long) {
        val fromDueDateLocal = DateUtils.millisToLocalDate(fromDueDate)
        database.withTransaction {
            installmentDao.deletePendingFromDueDate(billId, fromDueDateLocal)
            val remaining = installmentDao.getInstallmentsByBillId(billId)
            if (remaining.isEmpty()) {
                billDao.deleteById(billId)
                syncDao?.enqueue(
                    SyncOutboxEntity(
                        entityType = "BILL",
                        entityId = billId,
                        operation = "DELETE",
                        payload = """{"id":"$billId"}"""
                    )
                )
            } else {
                val maxDueDate = remaining.maxOfOrNull { DateUtils.localDateToMillis(it.dueDate) }
                billDao.updateBillEndDateAndTotalInstallments(
                    id = billId,
                    recurrenceEndDate = maxDueDate,
                    totalInstallments = remaining.size
                )
                syncDao?.enqueue(
                    SyncOutboxEntity(
                        entityType = "BILL",
                        entityId = billId,
                        operation = "UPDATE",
                        payload = """{"id":"$billId","totalInstallments":${remaining.size}}"""
                    )
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
        val startOfTodayDate = DateUtils.millisToLocalDate(startOfToday)
        val pausedUntilDate = pausedUntil?.let { DateUtils.millisToLocalDate(it) }

        database.withTransaction {
            billDao.updatePauseState(billId, isPaused = true, pausedUntil = pausedUntil)
            installmentDao.markPendingAsSkippedInRange(
                billId = billId,
                startDate = startOfTodayDate,
                endDate = pausedUntilDate
            )
            syncDao?.enqueue(
                SyncOutboxEntity(
                    entityType = "BILL",
                    entityId = billId,
                    operation = "UPDATE",
                    payload = """{"id":"$billId","isPaused":true,"pausedUntil":$pausedUntil}"""
                )
            )
        }
    }

    suspend fun resumeRecurringBill(billId: String) {
        database.withTransaction {
            billDao.updatePauseState(billId, isPaused = false, pausedUntil = null)
            syncDao?.enqueue(
                SyncOutboxEntity(
                    entityType = "BILL",
                    entityId = billId,
                    operation = "UPDATE",
                    payload = """{"id":"$billId","isPaused":false,"pausedUntil":null}"""
                )
            )
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
            val today = LocalDate.now()
            val cutoff = fromDueDate?.let { DateUtils.millisToLocalDate(it) } ?: remaining
                .filter { !it.dueDate.isBefore(today) && it.paidAt == null }
                .minOfOrNull { it.dueDate }
                ?: today

            val now = System.currentTimeMillis()
            // Remove APENAS futuras pendentes depois da data de corte via soft delete
            installmentDao.deletePendingAfterDueDate(billId, cutoff, deletedAt = now, updatedAt = now)

            val updatedRemaining = installmentDao.getInstallmentsByBillId(billId)
            val maxDueDate = updatedRemaining.maxOfOrNull { DateUtils.localDateToMillis(it.dueDate) } 
                ?: DateUtils.localDateToMillis(cutoff)

            billDao.stopRecurringBillWithDate(
                id = billId,
                recurrenceEndDate = maxDueDate,
                endedAt = now,
                totalInstallments = updatedRemaining.size
            )

            syncDao?.enqueue(
                SyncOutboxEntity(
                    entityType = "BILL",
                    entityId = billId,
                    operation = "UPDATE",
                    payload = """{"id":"$billId","endedAt":$now,"totalInstallments":${updatedRemaining.size}}"""
                )
            )
        }
    }

    suspend fun reactivateRecurringBill(billId: String) {
        database.withTransaction {
            billDao.reactivateRecurringBill(billId)
            syncDao?.enqueue(
                SyncOutboxEntity(
                    entityType = "BILL",
                    entityId = billId,
                    operation = "UPDATE",
                    payload = """{"id":"$billId","endedAt":null,"isPaused":false}"""
                )
            )
        }
    }

    suspend fun deleteRecurringBill(billId: String, keepPaidHistory: Boolean) {
        database.withTransaction {
            val now = System.currentTimeMillis()
            if (keepPaidHistory) {
                // Soft-delete de todas as parcelas pendentes; histórico pago permanece visível nos relatórios
                installmentDao.deleteAllPending(billId, deletedAt = now, updatedAt = now)
                billDao.markBillEndedAndDeleted(billId, now)
            } else {
                // Soft-delete total da conta e de todas as suas parcelas
                installmentDao.deleteByBillId(billId, deletedAt = now, updatedAt = now)
                billDao.deleteById(billId, deletedAt = now, updatedAt = now)
            }

            syncDao?.enqueue(
                SyncOutboxEntity(
                    entityType = "BILL",
                    entityId = billId,
                    operation = "DELETE",
                    payload = """{"id":"$billId","deletedAt":$now}"""
                )
            )
        }
    }

    suspend fun deleteBill(billId: String) {
        val now = System.currentTimeMillis()
        database.withTransaction {
            installmentDao.deleteByBillId(billId, deletedAt = now, updatedAt = now)
            billDao.deleteById(billId, deletedAt = now, updatedAt = now)
            syncDao?.enqueue(
                SyncOutboxEntity(
                    entityType = "BILL",
                    entityId = billId,
                    operation = "DELETE",
                    payload = """{"id":"$billId","deletedAt":$now}"""
                )
            )
        }
    }

    fun getMonthSummary(startMillis: Long, endMillis: Long, nowMillis: Long): Flow<MonthSummary> =
        effectiveReportingDataSource.getMonthSummary(startMillis, endMillis, nowMillis)

    fun getOpenAmountByContact(): Flow<Map<String, Long>> =
        effectiveReportingDataSource.getOpenAmountByContact()

    fun getAttentionCounts(startOfDayMillis: Long, endOfDayMillis: Long): Flow<AttentionCounts> =
        effectiveReportingDataSource.getAttentionCounts(startOfDayMillis, endOfDayMillis)

    fun getOpenInstallmentsOverview(): Flow<OpenInstallmentsSummary> =
        effectiveReportingDataSource.getOpenInstallmentsOverview()

    fun getActiveMonthlyRecurringCost(): Flow<Long> =
        effectiveReportingDataSource.getActiveMonthlyRecurringCost()

    fun getFreeingUpCommitments(): Flow<List<FreeingUpCommitment>> =
        effectiveReportingDataSource.getFreeingUpCommitments()
}
