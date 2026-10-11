package com.platform.app.data.repository

import com.platform.app.core.dispatcher.DispatcherProvider
import com.platform.app.data.local.dao.PaymentDao
import com.platform.app.data.local.dao.SyncDao
import com.platform.app.data.local.entity.PaymentEntity
import com.platform.app.data.local.entity.SyncOutboxEntity
import com.platform.app.domain.model.Payment
import com.platform.app.domain.repository.PaymentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepositoryImpl @Inject constructor(
    private val paymentDao: PaymentDao,
    private val syncDao: SyncDao,
    private val dispatcherProvider: DispatcherProvider
) : PaymentRepository {

    override fun getAllPayments(): Flow<List<Payment>> {
        return paymentDao.getAllPayments()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(dispatcherProvider.io)
    }

    override fun getPaymentsForInstallment(installmentId: String): Flow<List<Payment>> {
        return paymentDao.getPaymentsForInstallment(installmentId)
            .map { list -> list.map { it.toDomain() } }
            .flowOn(dispatcherProvider.io)
    }

    override suspend fun getPaymentsForInstallmentSync(installmentId: String): List<Payment> {
        return withContext(dispatcherProvider.io) {
            paymentDao.getPaymentsForInstallmentSync(installmentId).map { it.toDomain() }
        }
    }

    override suspend fun getPaymentById(id: String): Payment? {
        return withContext(dispatcherProvider.io) {
            paymentDao.getPaymentById(id)?.toDomain()
        }
    }

    override suspend fun recordPayment(payment: Payment) {
        withContext(dispatcherProvider.io) {
            val entity = PaymentEntity.fromDomain(payment)
            paymentDao.insertPayment(entity)

            // Registrar operacao na outbox para sincronizacao offline-first
            val payload = buildPaymentPayload(payment)
            syncDao.enqueue(
                SyncOutboxEntity(
                    entityType = "PAYMENT",
                    entityId = payment.id,
                    operation = "INSERT",
                    payload = payload,
                    createdAt = payment.createdAt
                )
            )
        }
    }

    override suspend fun softDeletePayment(id: String) {
        withContext(dispatcherProvider.io) {
            val now = System.currentTimeMillis()
            paymentDao.softDeletePayment(id, now)

            syncDao.enqueue(
                SyncOutboxEntity(
                    entityType = "PAYMENT",
                    entityId = id,
                    operation = "DELETE",
                    payload = """{"id":"$id","deletedAt":$now}""",
                    createdAt = now
                )
            )
        }
    }

    override suspend fun getTotalPaidForInstallmentCents(installmentId: String): Long {
        return withContext(dispatcherProvider.io) {
            paymentDao.getTotalPaidForInstallmentCents(installmentId) ?: 0L
        }
    }

    private fun buildPaymentPayload(p: Payment): String {
        return """{"id":"${p.id}","billInstallmentId":"${p.billInstallmentId}","amountCents":${p.amountCents},"paymentDate":${p.paymentDate},"interestCents":${p.interestCents},"fineCents":${p.fineCents},"discountCents":${p.discountCents},"notes":${p.notes?.let { "\"$it\"" } ?: "null"}}"""
    }
}
