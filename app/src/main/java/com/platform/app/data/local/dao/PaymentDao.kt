package com.platform.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.platform.app.data.local.entity.PaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments WHERE deletedAt IS NULL ORDER BY paymentDate DESC")
    fun getAllPayments(): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE billInstallmentId = :installmentId AND deletedAt IS NULL ORDER BY paymentDate ASC")
    fun getPaymentsForInstallment(installmentId: String): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE billInstallmentId = :installmentId AND deletedAt IS NULL ORDER BY paymentDate ASC")
    suspend fun getPaymentsForInstallmentSync(installmentId: String): List<PaymentEntity>

    @Query("SELECT * FROM payments WHERE id = :id AND deletedAt IS NULL LIMIT 1")
    suspend fun getPaymentById(id: String): PaymentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<PaymentEntity>)

    @Update
    suspend fun updatePayment(payment: PaymentEntity)

    @Query("UPDATE payments SET deletedAt = :deletedAt, updatedAt = :updatedAt WHERE id = :id AND deletedAt IS NULL")
    suspend fun softDeletePayment(
        id: String,
        deletedAt: Long = System.currentTimeMillis(),
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE payments SET deletedAt = :deletedAt, updatedAt = :updatedAt WHERE billInstallmentId = :installmentId AND deletedAt IS NULL")
    suspend fun softDeletePaymentsForInstallment(
        installmentId: String,
        deletedAt: Long = System.currentTimeMillis(),
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE payments SET deletedAt = :deletedAt, updatedAt = :updatedAt WHERE billInstallmentId IN (SELECT id FROM bill_installments WHERE invoiceId = :invoiceId AND deletedAt IS NULL) AND deletedAt IS NULL")
    suspend fun softDeletePaymentsForInvoice(
        invoiceId: String,
        deletedAt: Long = System.currentTimeMillis(),
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM payments WHERE deletedAt IS NOT NULL AND deletedAt < :olderThanMillis")
    suspend fun purgeDeleted(olderThanMillis: Long)

    @Query("DELETE FROM payments")
    suspend fun deleteAll()

    @Query("SELECT SUM(amountCents + interestCents + fineCents - discountCents) FROM payments WHERE billInstallmentId = :installmentId AND deletedAt IS NULL")
    suspend fun getTotalPaidForInstallmentCents(installmentId: String): Long?

    @Query("SELECT SUM(amountCents + discountCents) FROM payments WHERE billInstallmentId = :installmentId AND deletedAt IS NULL")
    suspend fun getTotalAmortizedForInstallmentCents(installmentId: String): Long?
}
