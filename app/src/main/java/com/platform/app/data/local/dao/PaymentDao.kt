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

    @Query("SELECT * FROM payments WHERE id = :id LIMIT 1")
    suspend fun getPaymentById(id: String): PaymentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<PaymentEntity>)

    @Update
    suspend fun updatePayment(payment: PaymentEntity)

    @Query("UPDATE payments SET deletedAt = :deletedAt, updatedAt = :deletedAt WHERE id = :id")
    suspend fun softDeletePayment(id: String, deletedAt: Long = System.currentTimeMillis())

    @Delete
    suspend fun deletePayment(payment: PaymentEntity)

    @Query("SELECT SUM(amountCents + interestCents + fineCents - discountCents) FROM payments WHERE billInstallmentId = :installmentId AND deletedAt IS NULL")
    suspend fun getTotalPaidForInstallmentCents(installmentId: String): Long?
}
