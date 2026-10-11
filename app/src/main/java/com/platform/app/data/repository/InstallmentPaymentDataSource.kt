package com.platform.app.data.repository

import androidx.room.withTransaction
import com.platform.app.data.local.PlatformDatabase
import com.platform.app.data.local.dao.BillInstallmentDao
import com.platform.app.data.local.dao.PaymentDao
import com.platform.app.data.local.dao.SyncDao
import com.platform.app.data.local.entity.BillInstallmentEntity
import com.platform.app.data.local.entity.PaymentEntity
import com.platform.app.data.local.entity.SyncOutboxEntity
import com.platform.app.domain.model.BillInstallment
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Agregado especializado para baixas, pagamentos parciais, estornos e integridade da tabela `payments`.
 */
@Singleton
class InstallmentPaymentDataSource @Inject constructor(
    private val database: PlatformDatabase,
    private val installmentDao: BillInstallmentDao,
    private val paymentDao: PaymentDao,
    private val syncDao: SyncDao? = null
) {

    suspend fun toggleInstallmentPayment(
        installmentId: String,
        isPaid: Boolean,
        paidTimestamp: Long?,
        actualPaymentDate: Long?
    ) {
        val now = System.currentTimeMillis()
        val effectivePaidAt = if (isPaid) (paidTimestamp ?: now) else null
        val effectiveActualDate = if (isPaid) (actualPaymentDate ?: effectivePaidAt ?: now) else null

        database.withTransaction {
            if (isPaid) {
                val inst = installmentDao.getEntityById(installmentId)
                if (inst != null) {
                    val activePayments = paymentDao.getPaymentsForInstallmentSync(installmentId)
                    val totalPaid = activePayments.sumOf { it.amountCents + it.discountCents }
                    val remaining = (inst.amountCents - totalPaid).coerceAtLeast(0L)
                    val paymentAmount = if (remaining > 0L) remaining else inst.amountCents

                    val paymentId = UUID.randomUUID().toString()
                    val payment = PaymentEntity(
                        id = paymentId,
                        billInstallmentId = installmentId,
                        amountCents = paymentAmount,
                        paymentDate = effectiveActualDate ?: now,
                        paymentMethodId = inst.paymentMethodId,
                        financialAccountId = inst.financialAccountId,
                        interestCents = 0L,
                        fineCents = 0L,
                        discountCents = 0L,
                        notes = null,
                        createdAt = effectivePaidAt ?: now,
                        updatedAt = now,
                        deletedAt = null
                    )
                    paymentDao.insertPayment(payment)
                    syncDao?.enqueue(
                        SyncOutboxEntity(
                            entityType = "PAYMENT",
                            entityId = paymentId,
                            operation = "INSERT",
                            payload = """{"id":"$paymentId","billInstallmentId":"$installmentId","amountCents":$paymentAmount,"paymentDate":${effectiveActualDate ?: now}}"""
                        )
                    )
                }
                installmentDao.updatePayment(installmentId, effectivePaidAt, effectiveActualDate, "PAID", now)
                syncDao?.enqueue(
                    SyncOutboxEntity(
                        entityType = "BILL_INSTALLMENT",
                        entityId = installmentId,
                        operation = "UPDATE",
                        payload = """{"id":"$installmentId","status":"PAID","paidAt":$effectivePaidAt,"actualPaymentDate":$effectiveActualDate}"""
                    )
                )
            } else {
                paymentDao.softDeletePaymentsForInstallment(installmentId, deletedAt = now, updatedAt = now)
                syncDao?.enqueue(
                    SyncOutboxEntity(
                        entityType = "PAYMENT",
                        entityId = installmentId,
                        operation = "DELETE_ALL_FOR_INSTALLMENT",
                        payload = """{"billInstallmentId":"$installmentId","deletedAt":$now}"""
                    )
                )
                installmentDao.updatePayment(installmentId, null, null, "PENDING", now)
                syncDao?.enqueue(
                    SyncOutboxEntity(
                        entityType = "BILL_INSTALLMENT",
                        entityId = installmentId,
                        operation = "UPDATE",
                        payload = """{"id":"$installmentId","status":"PENDING","paidAt":null,"actualPaymentDate":null}"""
                    )
                )
            }
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
        val now = System.currentTimeMillis()
        database.withTransaction {
            val inst = installmentDao.getEntityById(installmentId) ?: return@withTransaction
            val paymentId = UUID.randomUUID().toString()
            val payment = PaymentEntity(
                id = paymentId,
                billInstallmentId = installmentId,
                amountCents = amountCents,
                paymentDate = paymentDate,
                paymentMethodId = paymentMethodId ?: inst.paymentMethodId,
                financialAccountId = financialAccountId ?: inst.financialAccountId,
                interestCents = interestCents,
                fineCents = fineCents,
                discountCents = discountCents,
                notes = notes,
                createdAt = now,
                updatedAt = now,
                deletedAt = null
            )
            paymentDao.insertPayment(payment)
            syncDao?.enqueue(
                SyncOutboxEntity(
                    entityType = "PAYMENT",
                    entityId = paymentId,
                    operation = "INSERT",
                    payload = """{"id":"$paymentId","billInstallmentId":"$installmentId","amountCents":$amountCents,"paymentDate":$paymentDate}"""
                )
            )

            val allPayments = paymentDao.getPaymentsForInstallmentSync(installmentId)
            val totalAmortized = allPayments.sumOf { it.amountCents + it.discountCents }
            val latestPayment = allPayments.maxByOrNull { it.paymentDate }

            val (newStatus, newPaidAt, newActualPaymentDate) = when {
                totalAmortized >= inst.amountCents -> {
                    Triple("PAID", latestPayment?.createdAt ?: now, latestPayment?.paymentDate ?: paymentDate)
                }
                totalAmortized > 0L -> {
                    Triple("PARTIALLY_PAID", null, latestPayment?.paymentDate ?: paymentDate)
                }
                else -> {
                    Triple("PENDING", null, null)
                }
            }

            installmentDao.updatePayment(
                id = installmentId,
                paidAt = newPaidAt,
                actualPaymentDate = newActualPaymentDate,
                status = newStatus,
                updatedAt = now
            )
            syncDao?.enqueue(
                SyncOutboxEntity(
                    entityType = "BILL_INSTALLMENT",
                    entityId = installmentId,
                    operation = "UPDATE",
                    payload = """{"id":"$installmentId","status":"$newStatus","paidAt":$newPaidAt,"actualPaymentDate":$newActualPaymentDate}"""
                )
            )
        }
    }

    suspend fun recordPrePaidInstallments(installments: List<BillInstallment>) {
        val paidInstallments = installments.filter { it.isPaid }
        if (paidInstallments.isEmpty()) return

        val now = System.currentTimeMillis()
        val payments = paidInstallments.map { inst ->
            val pId = UUID.randomUUID().toString()
            val pDate = inst.actualPaymentDate ?: inst.paidAt ?: now
            PaymentEntity(
                id = pId,
                billInstallmentId = inst.id,
                amountCents = inst.amountCents,
                paymentDate = pDate,
                paymentMethodId = inst.paymentMethodId,
                financialAccountId = inst.financialAccountId,
                interestCents = 0L,
                fineCents = 0L,
                discountCents = 0L,
                notes = "Baixa na criação da despesa",
                createdAt = inst.paidAt ?: now,
                updatedAt = now,
                deletedAt = null
            )
        }
        paymentDao.insertPayments(payments)
        syncDao?.enqueueAll(
            payments.map { p ->
                SyncOutboxEntity(
                    entityType = "PAYMENT",
                    entityId = p.id,
                    operation = "INSERT",
                    payload = """{"id":"${p.id}","billInstallmentId":"${p.billInstallmentId}","amountCents":${p.amountCents},"paymentDate":${p.paymentDate}}"""
                )
            }
        )
    }

    suspend fun payInvoiceInstallments(
        invoiceId: String,
        invoiceInsts: List<BillInstallmentEntity>,
        effectiveActualDate: Long
    ) {
        if (invoiceInsts.isEmpty()) return
        val now = System.currentTimeMillis()
        val payments = invoiceInsts.map { inst ->
            val paymentId = UUID.randomUUID().toString()
            PaymentEntity(
                id = paymentId,
                billInstallmentId = inst.id,
                amountCents = inst.amountCents,
                paymentDate = effectiveActualDate,
                paymentMethodId = inst.paymentMethodId,
                financialAccountId = inst.financialAccountId,
                interestCents = 0L,
                fineCents = 0L,
                discountCents = 0L,
                notes = "Pagamento da fatura ($invoiceId)",
                createdAt = now,
                updatedAt = now,
                deletedAt = null
            )
        }
        paymentDao.insertPayments(payments)
        syncDao?.enqueueAll(
            payments.map { p ->
                SyncOutboxEntity(
                    entityType = "PAYMENT",
                    entityId = p.id,
                    operation = "INSERT",
                    payload = """{"id":"${p.id}","billInstallmentId":"${p.billInstallmentId}","amountCents":${p.amountCents},"paymentDate":${p.paymentDate}}"""
                )
            }
        )
    }

    suspend fun revertInvoicePayments(invoiceId: String) {
        val now = System.currentTimeMillis()
        paymentDao.softDeletePaymentsForInvoice(invoiceId, deletedAt = now, updatedAt = now)
        syncDao?.enqueue(
            SyncOutboxEntity(
                entityType = "PAYMENT",
                entityId = invoiceId,
                operation = "DELETE_ALL_FOR_INVOICE",
                payload = """{"invoiceId":"$invoiceId","deletedAt":$now}"""
            )
        )
    }
}
