package com.platform.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.platform.app.data.local.entity.BillEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {
    @Query("SELECT * FROM bills WHERE deletedAt IS NULL ORDER BY createdAt DESC")
    fun getAll(): Flow<List<BillEntity>>

    @Query("SELECT * FROM bills WHERE id = :id AND deletedAt IS NULL LIMIT 1")
    suspend fun getById(id: String): BillEntity?

    @Query("SELECT * FROM bills WHERE contactId = :contactId AND deletedAt IS NULL ORDER BY createdAt DESC")
    fun getBillsByContact(contactId: String): Flow<List<BillEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bill: BillEntity)

    @Query("UPDATE bills SET deletedAt = :deletedAt, updatedAt = :updatedAt WHERE id = :id AND deletedAt IS NULL")
    suspend fun deleteById(id: String, deletedAt: Long = System.currentTimeMillis(), updatedAt: Long = System.currentTimeMillis())

    @Query("""
        UPDATE bills 
        SET title = :title,
            description = :description,
            totalAmountCents = :totalAmountCents,
            categoryId = :categoryId,
            itemId = :itemId,
            contactId = :contactId,
            financialAccountId = :financialAccountId,
            paymentMethodId = :paymentMethodId,
            updatedAt = :updatedAt
        WHERE id = :id AND deletedAt IS NULL
    """)
    suspend fun updateBillDetails(
        id: String,
        title: String,
        description: String,
        totalAmountCents: Long,
        categoryId: String?,
        itemId: String?,
        contactId: String?,
        financialAccountId: String?,
        paymentMethodId: String?,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE bills SET isPaused = :isPaused, updatedAt = :updatedAt WHERE id = :id AND deletedAt IS NULL")
    suspend fun updatePausedStatus(id: String, isPaused: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE bills SET isPaused = :isPaused, pausedUntil = :pausedUntil, updatedAt = :updatedAt WHERE id = :id AND deletedAt IS NULL")
    suspend fun updatePauseState(id: String, isPaused: Boolean, pausedUntil: Long?, updatedAt: Long = System.currentTimeMillis())

    @Query("""
        UPDATE bills 
        SET recurrenceEndType = 'UNTIL_DATE',
            recurrenceEndDate = :recurrenceEndDate,
            endedAt = :endedAt,
            totalInstallments = :totalInstallments,
            updatedAt = :updatedAt
        WHERE id = :id AND deletedAt IS NULL
    """)
    suspend fun stopRecurringBillWithDate(id: String, recurrenceEndDate: Long?, endedAt: Long?, totalInstallments: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("""
        UPDATE bills 
        SET recurrenceEndType = 'FOREVER',
            recurrenceEndDate = NULL,
            endedAt = NULL,
            isPaused = 0,
            pausedUntil = NULL,
            updatedAt = :updatedAt
        WHERE id = :id AND deletedAt IS NULL
    """)
    suspend fun reactivateRecurringBill(id: String, updatedAt: Long = System.currentTimeMillis())

    @Query("""
        UPDATE bills 
        SET deletedAt = :deletedAt,
            updatedAt = :deletedAt,
            recurrenceEndType = 'UNTIL_DATE',
            endedAt = :deletedAt
        WHERE id = :id AND deletedAt IS NULL
    """)
    suspend fun markBillEndedAndDeleted(id: String, deletedAt: Long)

    @Query("UPDATE bills SET totalAmountCents = :totalAmountCents, updatedAt = :updatedAt WHERE id = :id AND deletedAt IS NULL")
    suspend fun updateBillTotalAmount(id: String, totalAmountCents: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE bills SET totalAmountCents = :totalAmountCents, recurrenceEndDate = :recurrenceEndDate, totalInstallments = :totalInstallments, updatedAt = :updatedAt WHERE id = :id AND deletedAt IS NULL")
    suspend fun updateBillAmountAndEndDate(id: String, totalAmountCents: Long, recurrenceEndDate: Long?, totalInstallments: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE bills SET recurrenceEndDate = :recurrenceEndDate, totalInstallments = :totalInstallments, updatedAt = :updatedAt WHERE id = :id AND deletedAt IS NULL")
    suspend fun updateBillEndDateAndTotalInstallments(id: String, recurrenceEndDate: Long?, totalInstallments: Int, updatedAt: Long = System.currentTimeMillis())

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(bills: List<BillEntity>)

    @Query("SELECT * FROM bills WHERE deletedAt IS NULL")
    suspend fun getAllList(): List<BillEntity>

    @Query("DELETE FROM bills WHERE deletedAt IS NOT NULL AND deletedAt < :olderThanMillis")
    suspend fun purgeDeleted(olderThanMillis: Long)

    @Query("DELETE FROM bills")
    suspend fun deleteAll()

    @Query("""
        SELECT COALESCE(SUM(totalAmountCents), 0)
        FROM bills
        WHERE type = 'RECURRING'
          AND isPaused = 0
          AND endedAt IS NULL
          AND deletedAt IS NULL
    """)
    fun getActiveMonthlyRecurringCost(): Flow<Long>
}
