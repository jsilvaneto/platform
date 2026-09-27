package com.platform.app.data.local.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.platform.app.data.local.entity.BillInstallmentEntity
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.BillStatus
import com.platform.app.domain.model.BillType
import kotlinx.coroutines.flow.Flow

data class InstallmentWithDetails(
    @Embedded val installment: BillInstallmentEntity,
    @ColumnInfo(name = "bill_title") val billTitle: String,
    @ColumnInfo(name = "bill_type") val billType: String,
    @ColumnInfo(name = "category_id") val categoryId: String?,
    @ColumnInfo(name = "category_name") val categoryName: String?,
    @ColumnInfo(name = "category_color_hex") val categoryColorHex: String?
) {
    fun toDomain(): BillInstallment {
        val parsedType = try {
            BillType.valueOf(billType)
        } catch (e: Exception) {
            BillType.SINGLE
        }

        return installment.toDomain(
            billTitle = billTitle,
            categoryId = categoryId,
            categoryName = categoryName ?: "Geral",
            categoryColorHex = categoryColorHex ?: "#64748B",
            billType = parsedType
        )
    }
}

@Dao
interface BillInstallmentDao {

    @Query(
        """
        SELECT 
            i.*,
            b.title AS bill_title,
            b.type AS bill_type,
            b.categoryId AS category_id,
            c.name AS category_name,
            c.colorHex AS category_color_hex
        FROM bill_installments i
        INNER JOIN bills b ON i.billId = b.id
        LEFT JOIN categories c ON b.categoryId = c.id
        WHERE i.dueDate BETWEEN :startMillis AND :endMillis
        ORDER BY i.dueDate ASC
        """
    )
    fun getInstallmentsForPeriod(startMillis: Long, endMillis: Long): Flow<List<InstallmentWithDetails>>

    @Query(
        """
        SELECT 
            i.*,
            b.title AS bill_title,
            b.type AS bill_type,
            b.categoryId AS category_id,
            c.name AS category_name,
            c.colorHex AS category_color_hex
        FROM bill_installments i
        INNER JOIN bills b ON i.billId = b.id
        LEFT JOIN categories c ON b.categoryId = c.id
        ORDER BY i.dueDate ASC
        """
    )
    fun getAllWithDetails(): Flow<List<InstallmentWithDetails>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(installments: List<BillInstallmentEntity>)

    @Query("UPDATE bill_installments SET paidAt = :paidAt, status = :status WHERE id = :id")
    suspend fun updatePayment(id: String, paidAt: Long?, status: String)

    @Query("DELETE FROM bill_installments WHERE billId = :billId")
    suspend fun deleteByBillId(billId: String)
}
