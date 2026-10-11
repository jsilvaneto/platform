package com.platform.app.domain.repository

import com.platform.app.domain.model.Payment
import kotlinx.coroutines.flow.Flow

interface PaymentRepository {
    fun getAllPayments(): Flow<List<Payment>>
    fun getPaymentsForInstallment(installmentId: String): Flow<List<Payment>>
    suspend fun getPaymentsForInstallmentSync(installmentId: String): List<Payment>
    suspend fun getPaymentById(id: String): Payment?
    suspend fun recordPayment(payment: Payment)
    suspend fun softDeletePayment(id: String)
    suspend fun getTotalPaidForInstallmentCents(installmentId: String): Long
}
