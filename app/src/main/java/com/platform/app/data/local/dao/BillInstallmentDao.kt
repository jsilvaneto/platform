package com.platform.app.data.local.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.platform.app.data.local.entity.BillInstallmentEntity
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.BillType
import kotlinx.coroutines.flow.Flow

data class InstallmentWithDetails(
    @Embedded val installment: BillInstallmentEntity,
    @ColumnInfo(name = "bill_title") val billTitle: String,
    @ColumnInfo(name = "bill_type") val billType: String,
    @ColumnInfo(name = "category_id") val categoryId: String?,
    @ColumnInfo(name = "category_name") val categoryName: String?,
    @ColumnInfo(name = "category_color_hex") val categoryColorHex: String?,
    @ColumnInfo(name = "subcategory_name") val subcategoryName: String?,
    @ColumnInfo(name = "contact_name") val contactName: String?,
    @ColumnInfo(name = "financial_account_name") val financialAccountName: String?,
    @ColumnInfo(name = "payment_method_name") val paymentMethodName: String?
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
            subcategoryName = subcategoryName,
            contactName = contactName,
            financialAccountName = financialAccountName,
            paymentMethodName = paymentMethodName,
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
            c.colorHex AS category_color_hex,
            sub.name AS subcategory_name,
            cont.name AS contact_name,
            fa.name AS financial_account_name,
            pm.name AS payment_method_name
        FROM bill_installments i
        INNER JOIN bills b ON i.billId = b.id
        LEFT JOIN categories c ON b.categoryId = c.id
        LEFT JOIN subcategories sub ON (i.subcategoryId = sub.id OR b.subcategoryId = sub.id)
        LEFT JOIN contacts cont ON (i.contactId = cont.id OR b.contactId = cont.id)
        LEFT JOIN financial_accounts fa ON (i.financialAccountId = fa.id OR b.financialAccountId = fa.id)
        LEFT JOIN payment_methods pm ON (i.paymentMethodId = pm.id OR b.paymentMethodId = pm.id)
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
            c.colorHex AS category_color_hex,
            sub.name AS subcategory_name,
            cont.name AS contact_name,
            fa.name AS financial_account_name,
            pm.name AS payment_method_name
        FROM bill_installments i
        INNER JOIN bills b ON i.billId = b.id
        LEFT JOIN categories c ON b.categoryId = c.id
        LEFT JOIN subcategories sub ON (i.subcategoryId = sub.id OR b.subcategoryId = sub.id)
        LEFT JOIN contacts cont ON (i.contactId = cont.id OR b.contactId = cont.id)
        LEFT JOIN financial_accounts fa ON (i.financialAccountId = fa.id OR b.financialAccountId = fa.id)
        LEFT JOIN payment_methods pm ON (i.paymentMethodId = pm.id OR b.paymentMethodId = pm.id)
        ORDER BY i.dueDate ASC
        """
    )
    fun getAllWithDetails(): Flow<List<InstallmentWithDetails>>

    @Query(
        """
        SELECT 
            i.*,
            b.title AS bill_title,
            b.type AS bill_type,
            b.categoryId AS category_id,
            c.name AS category_name,
            c.colorHex AS category_color_hex,
            sub.name AS subcategory_name,
            cont.name AS contact_name,
            fa.name AS financial_account_name,
            pm.name AS payment_method_name
        FROM bill_installments i
        INNER JOIN bills b ON i.billId = b.id
        LEFT JOIN categories c ON b.categoryId = c.id
        LEFT JOIN subcategories sub ON (i.subcategoryId = sub.id OR b.subcategoryId = sub.id)
        LEFT JOIN contacts cont ON (i.contactId = cont.id OR b.contactId = cont.id)
        LEFT JOIN financial_accounts fa ON (i.financialAccountId = fa.id OR b.financialAccountId = fa.id)
        LEFT JOIN payment_methods pm ON (i.paymentMethodId = pm.id OR b.paymentMethodId = pm.id)
        WHERE (i.contactId = :contactId OR b.contactId = :contactId)
        ORDER BY i.dueDate ASC
        """
    )
    fun getInstallmentsByContact(contactId: String): Flow<List<InstallmentWithDetails>>

    @Query(
        """
        SELECT 
            i.*,
            b.title AS bill_title,
            b.type AS bill_type,
            b.categoryId AS category_id,
            c.name AS category_name,
            c.colorHex AS category_color_hex,
            sub.name AS subcategory_name,
            cont.name AS contact_name,
            fa.name AS financial_account_name,
            pm.name AS payment_method_name
        FROM bill_installments i
        INNER JOIN bills b ON i.billId = b.id
        LEFT JOIN categories c ON b.categoryId = c.id
        LEFT JOIN subcategories sub ON (i.subcategoryId = sub.id OR b.subcategoryId = sub.id)
        LEFT JOIN contacts cont ON (i.contactId = cont.id OR b.contactId = cont.id)
        LEFT JOIN financial_accounts fa ON (i.financialAccountId = fa.id OR b.financialAccountId = fa.id)
        LEFT JOIN payment_methods pm ON (i.paymentMethodId = pm.id OR b.paymentMethodId = pm.id)
        WHERE (i.contactId = :contactId OR b.contactId = :contactId) AND i.paidAt IS NULL
        ORDER BY i.dueDate ASC
        """
    )
    fun getPlannedInstallmentsByContact(contactId: String): Flow<List<InstallmentWithDetails>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(installments: List<BillInstallmentEntity>)

    @Query("UPDATE bill_installments SET paidAt = :paidAt, status = :status WHERE id = :id")
    suspend fun updatePayment(id: String, paidAt: Long?, status: String)

    @Query("DELETE FROM bill_installments WHERE billId = :billId")
    suspend fun deleteByBillId(billId: String)
}
