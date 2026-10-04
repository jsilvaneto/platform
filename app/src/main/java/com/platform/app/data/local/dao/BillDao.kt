package com.platform.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.platform.app.data.local.entity.BillEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {
    @Query("SELECT * FROM bills ORDER BY createdAt DESC")
    fun getAll(): Flow<List<BillEntity>>

    @Query("SELECT * FROM bills WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): BillEntity?

    @Query("SELECT * FROM bills WHERE contactId = :contactId ORDER BY createdAt DESC")
    fun getBillsByContact(contactId: String): Flow<List<BillEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bill: BillEntity)

    @Query("DELETE FROM bills WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("""
        UPDATE bills 
        SET title = :title,
            description = :description,
            totalAmountCents = :totalAmountCents,
            categoryId = :categoryId,
            itemId = :itemId,
            contactId = :contactId,
            financialAccountId = :financialAccountId,
            paymentMethodId = :paymentMethodId
        WHERE id = :id
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
        paymentMethodId: String?
    )

    @Query("UPDATE bills SET isPaused = :isPaused WHERE id = :id")
    suspend fun updatePausedStatus(id: String, isPaused: Boolean)

    @Query("UPDATE bills SET totalAmountCents = :totalAmountCents WHERE id = :id")
    suspend fun updateBillTotalAmount(id: String, totalAmountCents: Long)

    @Query("UPDATE bills SET totalAmountCents = :totalAmountCents, recurrenceEndDate = :recurrenceEndDate, totalInstallments = :totalInstallments WHERE id = :id")
    suspend fun updateBillAmountAndEndDate(id: String, totalAmountCents: Long, recurrenceEndDate: Long?, totalInstallments: Int)

    @Query("UPDATE bills SET recurrenceEndDate = :recurrenceEndDate, totalInstallments = :totalInstallments WHERE id = :id")
    suspend fun updateBillEndDateAndTotalInstallments(id: String, recurrenceEndDate: Long?, totalInstallments: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(bills: List<BillEntity>)

    @Query("SELECT * FROM bills")
    suspend fun getAllList(): List<BillEntity>

    @Query("DELETE FROM bills")
    suspend fun deleteAll()
}
