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
import com.platform.app.domain.model.ExpenseNature
import kotlinx.coroutines.flow.Flow

data class InstallmentWithDetails(
    @Embedded val installment: BillInstallmentEntity,
    @ColumnInfo(name = "bill_title") val billTitle: String,
    @ColumnInfo(name = "bill_type") val billType: String,
    @ColumnInfo(name = "category_id") val categoryId: String?,
    @ColumnInfo(name = "category_name") val categoryName: String?,
    @ColumnInfo(name = "category_color_hex") val categoryColorHex: String?,
    @ColumnInfo(name = "category_nature") val categoryNature: String?,
    @ColumnInfo(name = "category_icon_name") val categoryIconName: String? = null,
    @ColumnInfo(name = "item_name") val itemName: String?,
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
        val parsedNature = try {
            ExpenseNature.valueOf(categoryNature ?: "NECESSARIO")
        } catch (e: Exception) {
            ExpenseNature.NECESSARIO
        }

        return installment.toDomain(
            billTitle = billTitle,
            categoryId = categoryId,
            categoryName = categoryName ?: "Geral",
            categoryColorHex = categoryColorHex ?: "#64748B",
            categoryIconName = categoryIconName ?: "category",
            nature = parsedNature,
            itemName = itemName,
            contactName = contactName,
            financialAccountName = financialAccountName,
            paymentMethodName = paymentMethodName,
            billType = parsedType
        )
    }
}

data class InvoiceTotal(
    @ColumnInfo(name = "invoiceId") val invoiceId: String,
    @ColumnInfo(name = "totalAmountCents") val totalAmountCents: Long
)

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
            c.nature AS category_nature,
            c.iconName AS category_icon_name,
            ei.name AS item_name,
            cont.name AS contact_name,
            fa.name AS financial_account_name,
            pm.name AS payment_method_name
        FROM bill_installments i
        INNER JOIN bills b ON i.billId = b.id
        LEFT JOIN categories c ON b.categoryId = c.id
        LEFT JOIN expense_items ei ON (i.itemId = ei.id OR b.itemId = ei.id)
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
            c.nature AS category_nature,
            c.iconName AS category_icon_name,
            ei.name AS item_name,
            cont.name AS contact_name,
            fa.name AS financial_account_name,
            pm.name AS payment_method_name
        FROM bill_installments i
        INNER JOIN bills b ON i.billId = b.id
        LEFT JOIN categories c ON b.categoryId = c.id
        LEFT JOIN expense_items ei ON (i.itemId = ei.id OR b.itemId = ei.id)
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
            c.nature AS category_nature,
            c.iconName AS category_icon_name,
            ei.name AS item_name,
            cont.name AS contact_name,
            fa.name AS financial_account_name,
            pm.name AS payment_method_name
        FROM bill_installments i
        INNER JOIN bills b ON i.billId = b.id
        LEFT JOIN categories c ON b.categoryId = c.id
        LEFT JOIN expense_items ei ON (i.itemId = ei.id OR b.itemId = ei.id)
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
            c.nature AS category_nature,
            c.iconName AS category_icon_name,
            ei.name AS item_name,
            cont.name AS contact_name,
            fa.name AS financial_account_name,
            pm.name AS payment_method_name
        FROM bill_installments i
        INNER JOIN bills b ON i.billId = b.id
        LEFT JOIN categories c ON b.categoryId = c.id
        LEFT JOIN expense_items ei ON (i.itemId = ei.id OR b.itemId = ei.id)
        LEFT JOIN contacts cont ON (i.contactId = cont.id OR b.contactId = cont.id)
        LEFT JOIN financial_accounts fa ON (i.financialAccountId = fa.id OR b.financialAccountId = fa.id)
        LEFT JOIN payment_methods pm ON (i.paymentMethodId = pm.id OR b.paymentMethodId = pm.id)
        WHERE (i.contactId = :contactId OR b.contactId = :contactId) AND i.paidAt IS NULL
        ORDER BY i.dueDate ASC
        """
    )
    fun getPlannedInstallmentsByContact(contactId: String): Flow<List<InstallmentWithDetails>>

    @Query(
        """
        SELECT 
            i.*,
            b.title AS bill_title,
            b.type AS bill_type,
            b.categoryId AS category_id,
            c.name AS category_name,
            c.colorHex AS category_color_hex,
            c.nature AS category_nature,
            c.iconName AS category_icon_name,
            ei.name AS item_name,
            cont.name AS contact_name,
            fa.name AS financial_account_name,
            pm.name AS payment_method_name
        FROM bill_installments i
        INNER JOIN bills b ON i.billId = b.id
        LEFT JOIN categories c ON b.categoryId = c.id
        LEFT JOIN expense_items ei ON (i.itemId = ei.id OR b.itemId = ei.id)
        LEFT JOIN contacts cont ON (i.contactId = cont.id OR b.contactId = cont.id)
        LEFT JOIN financial_accounts fa ON (i.financialAccountId = fa.id OR b.financialAccountId = fa.id)
        LEFT JOIN payment_methods pm ON (i.paymentMethodId = pm.id OR b.paymentMethodId = pm.id)
        WHERE i.invoiceId = :invoiceId
        ORDER BY i.dueDate ASC
        """
    )
    fun getInstallmentsForInvoice(invoiceId: String): Flow<List<InstallmentWithDetails>>

    @Query("SELECT * FROM bill_installments WHERE invoiceId = :invoiceId")
    suspend fun getInstallmentsListForInvoice(invoiceId: String): List<BillInstallmentEntity>

    @Query(
        """
        SELECT invoiceId, COALESCE(SUM(amountCents), 0) AS totalAmountCents
        FROM bill_installments
        WHERE invoiceId IS NOT NULL
        GROUP BY invoiceId
        """
    )
    fun getInvoiceTotals(): Flow<List<InvoiceTotal>>

    @Query(
        """
        SELECT invoiceId, COALESCE(SUM(amountCents), 0) AS totalAmountCents
        FROM bill_installments
        WHERE invoiceId IS NOT NULL
        GROUP BY invoiceId
        """
    )
    suspend fun getInvoiceTotalsList(): List<InvoiceTotal>

    @Query("SELECT COALESCE(SUM(amountCents), 0) FROM bill_installments WHERE invoiceId = :invoiceId")
    suspend fun getInvoiceTotal(invoiceId: String): Long

    @Query(
        """
        SELECT COUNT(i.id) FROM bill_installments i
        INNER JOIN credit_card_invoices inv ON i.invoiceId = inv.id
        WHERE inv.creditCardId = :cardId
        """
    )
    suspend fun countInstallmentsForCard(cardId: String): Int

    @Query(
        """
        SELECT i.* FROM bill_installments i
        INNER JOIN bills b ON i.billId = b.id
        LEFT JOIN credit_card_invoices inv ON b.invoiceId = inv.id
        WHERE b.type = 'RECURRING'
          AND i.invoiceId IS NULL
          AND (b.creditCardId = :cardId OR inv.creditCardId = :cardId)
        """
    )
    suspend fun getUnattachedRecurringInstallmentsForCard(cardId: String): List<BillInstallmentEntity>

    @Query("UPDATE bill_installments SET invoiceId = :invoiceId, dueDate = :invoiceDueDate WHERE id IN (:installmentIds)")
    suspend fun attachInstallmentsToInvoice(installmentIds: List<String>, invoiceId: String, invoiceDueDate: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(installments: List<BillInstallmentEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIgnore(installments: List<BillInstallmentEntity>)

    @Query("UPDATE bill_installments SET paidAt = :paidAt, actualPaymentDate = :actualPaymentDate, status = :status WHERE id = :id")
    suspend fun updatePayment(id: String, paidAt: Long?, actualPaymentDate: Long?, status: String)

    @Query("UPDATE bill_installments SET actualPaymentDate = :actualPaymentDate WHERE id = :id")
    suspend fun updateActualPaymentDate(id: String, actualPaymentDate: Long?)

    @Query("UPDATE bill_installments SET actualPaymentDate = :actualPaymentDate WHERE id IN (:ids)")
    suspend fun updateActualPaymentDateBatch(ids: List<String>, actualPaymentDate: Long?)

    @Query("UPDATE bill_installments SET paidAt = :paidAt, actualPaymentDate = :actualPaymentDate, status = :status WHERE invoiceId = :invoiceId")
    suspend fun updatePaymentByInvoiceId(invoiceId: String, paidAt: Long?, actualPaymentDate: Long?, status: String)

    @Query("UPDATE bill_installments SET amountCents = :newAmountCents, dueDate = :newDueDate WHERE id = :id")
    suspend fun updateInstallmentAmountAndDate(id: String, newAmountCents: Long, newDueDate: Long)

    @Query("""
        UPDATE bill_installments 
        SET amountCents = :amountCents, 
            dueDate = :dueDate, 
            itemId = :itemId, 
            contactId = :contactId, 
            financialAccountId = :financialAccountId, 
            paymentMethodId = :paymentMethodId 
        WHERE id = :id
    """)
    suspend fun updateInstallmentDetails(
        id: String,
        amountCents: Long,
        dueDate: Long,
        itemId: String?,
        contactId: String?,
        financialAccountId: String?,
        paymentMethodId: String?
    )

    @Query("SELECT * FROM bill_installments WHERE id = :id LIMIT 1")
    suspend fun getEntityById(id: String): BillInstallmentEntity?

    @Query("DELETE FROM bill_installments WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM bill_installments WHERE billId = :billId AND paidAt IS NULL AND dueDate >= :fromDueDate")
    suspend fun deletePendingFromDueDate(billId: String, fromDueDate: Long)

    @Query("UPDATE bill_installments SET amountCents = :newAmountCents WHERE billId = :billId AND paidAt IS NULL AND dueDate >= :fromDueDate")
    suspend fun updateAmountForPendingFromDueDate(billId: String, fromDueDate: Long, newAmountCents: Long)

    @Query("UPDATE bill_installments SET status = :status WHERE billId = :billId AND paidAt IS NULL")
    suspend fun updateStatusForPending(billId: String, status: String)

    @Query("SELECT * FROM bill_installments WHERE billId = :billId ORDER BY dueDate ASC")
    suspend fun getInstallmentsByBillId(billId: String): List<BillInstallmentEntity>

    @Query(
        """
        SELECT 
            i.*,
            b.title AS bill_title,
            b.type AS bill_type,
            b.categoryId AS category_id,
            c.name AS category_name,
            c.colorHex AS category_color_hex,
            c.nature AS category_nature,
            c.iconName AS category_icon_name,
            ei.name AS item_name,
            cont.name AS contact_name,
            fa.name AS financial_account_name,
            pm.name AS payment_method_name
        FROM bill_installments i
        INNER JOIN bills b ON i.billId = b.id
        LEFT JOIN categories c ON b.categoryId = c.id
        LEFT JOIN expense_items ei ON (i.itemId = ei.id OR b.itemId = ei.id)
        LEFT JOIN contacts cont ON (i.contactId = cont.id OR b.contactId = cont.id)
        LEFT JOIN financial_accounts fa ON (i.financialAccountId = fa.id OR b.financialAccountId = fa.id)
        LEFT JOIN payment_methods pm ON (i.paymentMethodId = pm.id OR b.paymentMethodId = pm.id)
        WHERE i.billId = :billId
        ORDER BY i.dueDate ASC
        """
    )
    suspend fun getInstallmentsWithDetailsByBillId(billId: String): List<InstallmentWithDetails>

    @Query("DELETE FROM bill_installments WHERE billId = :billId")
    suspend fun deleteByBillId(billId: String)

    @Query("SELECT * FROM bill_installments")
    suspend fun getAllInstallmentsList(): List<BillInstallmentEntity>

    @Query("DELETE FROM bill_installments")
    suspend fun deleteAll()
}
