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
import com.platform.app.domain.model.MonthSummary
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
    @ColumnInfo(name = "payment_method_name") val paymentMethodName: String?,
    @ColumnInfo(name = "bill_created_at") val billCreatedAt: Long? = null,
    @ColumnInfo(name = "bill_credit_card_id") val billCreditCardId: String? = null
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
            billType = parsedType,
            billCreatedAt = billCreatedAt,
            billCreditCardId = billCreditCardId
        )
    }
}

data class InvoiceTotal(
    @ColumnInfo(name = "invoiceId") val invoiceId: String,
    @ColumnInfo(name = "totalAmountCents") val totalAmountCents: Long
)

data class MonthSummaryRaw(
    @ColumnInfo(name = "totalCents") val totalCents: Long,
    @ColumnInfo(name = "paidCents") val paidCents: Long,
    @ColumnInfo(name = "pendingCents") val pendingCents: Long,
    @ColumnInfo(name = "overdueCents") val overdueCents: Long,
    @ColumnInfo(name = "paidCount") val paidCount: Int,
    @ColumnInfo(name = "pendingCount") val pendingCount: Int,
    @ColumnInfo(name = "overdueCount") val overdueCount: Int
) {
    fun toDomain(): MonthSummary = MonthSummary(
        totalCents = totalCents,
        paidCents = paidCents,
        pendingCents = pendingCents,
        overdueCents = overdueCents,
        paidCount = paidCount,
        pendingCount = pendingCount,
        overdueCount = overdueCount
    )
}

data class ContactOpenBalanceRaw(
    @ColumnInfo(name = "contactId") val contactId: String,
    @ColumnInfo(name = "totalOpenCents") val totalOpenCents: Long
)

data class AttentionCountsRaw(
    @ColumnInfo(name = "overdueCount") val overdueCount: Int,
    @ColumnInfo(name = "dueTodayCount") val dueTodayCount: Int
)

data class OpenInstallmentsRaw(
    @ColumnInfo(name = "totalAmountCents") val totalAmountCents: Long,
    @ColumnInfo(name = "installmentCount") val installmentCount: Int,
    @ColumnInfo(name = "lastDueDate") val lastDueDate: Long?
)

data class FreeingUpCommitmentRaw(
    @ColumnInfo(name = "billId") val billId: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "endDate") val endDate: Long,
    @ColumnInfo(name = "monthlyAmountCents") val monthlyAmountCents: Long
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
            pm.name AS payment_method_name,
            b.createdAt AS bill_created_at,
            b.creditCardId AS bill_credit_card_id
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
            pm.name AS payment_method_name,
            b.createdAt AS bill_created_at,
            b.creditCardId AS bill_credit_card_id
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
            pm.name AS payment_method_name,
            b.createdAt AS bill_created_at,
            b.creditCardId AS bill_credit_card_id
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
            pm.name AS payment_method_name,
            b.createdAt AS bill_created_at,
            b.creditCardId AS bill_credit_card_id
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
            pm.name AS payment_method_name,
            b.createdAt AS bill_created_at,
            b.creditCardId AS bill_credit_card_id
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
          AND status != 'PAUSED'
        GROUP BY invoiceId
        """
    )
    fun getInvoiceTotals(): Flow<List<InvoiceTotal>>

    @Query(
        """
        SELECT invoiceId, COALESCE(SUM(amountCents), 0) AS totalAmountCents
        FROM bill_installments
        WHERE invoiceId IS NOT NULL
          AND status != 'PAUSED'
        GROUP BY invoiceId
        """
    )
    suspend fun getInvoiceTotalsList(): List<InvoiceTotal>

    @Query("SELECT COALESCE(SUM(amountCents), 0) FROM bill_installments WHERE invoiceId = :invoiceId AND status != 'PAUSED'")
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
          AND i.status != 'PAUSED'
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
            pm.name AS payment_method_name,
            b.createdAt AS bill_created_at,
            b.creditCardId AS bill_credit_card_id
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
            pm.name AS payment_method_name,
            b.createdAt AS bill_created_at,
            b.creditCardId AS bill_credit_card_id
        FROM bill_installments i
        INNER JOIN bills b ON i.billId = b.id
        LEFT JOIN categories c ON b.categoryId = c.id
        LEFT JOIN expense_items ei ON (i.itemId = ei.id OR b.itemId = ei.id)
        LEFT JOIN contacts cont ON (i.contactId = cont.id OR b.contactId = cont.id)
        LEFT JOIN financial_accounts fa ON (i.financialAccountId = fa.id OR b.financialAccountId = fa.id)
        LEFT JOIN payment_methods pm ON (i.paymentMethodId = pm.id OR b.paymentMethodId = pm.id)
        WHERE i.invoiceId IS NULL
          AND i.paidAt IS NULL
          AND i.status != 'PAUSED'
          AND i.status != 'SKIPPED'
          AND i.dueDate BETWEEN :startDate AND :endDate
        ORDER BY i.dueDate ASC
        """
    )
    suspend fun getPendingInstallmentsInRange(startDate: Long, endDate: Long): List<InstallmentWithDetails>

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
            pm.name AS payment_method_name,
            b.createdAt AS bill_created_at,
            b.creditCardId AS bill_credit_card_id
        FROM bill_installments i
        INNER JOIN bills b ON i.billId = b.id
        LEFT JOIN categories c ON b.categoryId = c.id
        LEFT JOIN expense_items ei ON (i.itemId = ei.id OR b.itemId = ei.id)
        LEFT JOIN contacts cont ON (i.contactId = cont.id OR b.contactId = cont.id)
        LEFT JOIN financial_accounts fa ON (i.financialAccountId = fa.id OR b.financialAccountId = fa.id)
        LEFT JOIN payment_methods pm ON (i.paymentMethodId = pm.id OR b.paymentMethodId = pm.id)
        WHERE i.invoiceId IS NULL
          AND i.paidAt IS NULL
          AND i.status != 'PAUSED'
          AND i.status != 'SKIPPED'
          AND i.dueDate < :beforeDate
        ORDER BY i.dueDate ASC
        """
    )
    suspend fun getOverduePendingInstallments(beforeDate: Long): List<InstallmentWithDetails>

    @Query("""
        UPDATE bill_installments 
        SET status = 'SKIPPED' 
        WHERE billId = :billId 
          AND paidAt IS NULL 
          AND dueDate >= :startDate 
          AND (:endDate IS NULL OR dueDate <= :endDate)
    """)
    suspend fun markPendingAsSkippedInRange(billId: String, startDate: Long, endDate: Long?)

    @Query("DELETE FROM bill_installments WHERE billId = :billId AND paidAt IS NULL AND dueDate > :cutoffDueDate")
    suspend fun deletePendingAfterDueDate(billId: String, cutoffDueDate: Long)

    @Query("DELETE FROM bill_installments WHERE billId = :billId AND paidAt IS NULL")
    suspend fun deleteAllPending(billId: String)

    @Query("DELETE FROM bill_installments WHERE billId = :billId")
    suspend fun deleteByBillId(billId: String)

    @Query("SELECT * FROM bill_installments")
    suspend fun getAllInstallmentsList(): List<BillInstallmentEntity>

    @Query("DELETE FROM bill_installments")
    suspend fun deleteAll()

    @Query(
        """
        SELECT 
            COALESCE(SUM(amountCents), 0) AS totalCents,
            COALESCE(SUM(CASE WHEN paidAt IS NOT NULL THEN amountCents ELSE 0 END), 0) AS paidCents,
            COALESCE(SUM(CASE WHEN paidAt IS NULL AND dueDate >= :nowMillis THEN amountCents ELSE 0 END), 0) AS pendingCents,
            COALESCE(SUM(CASE WHEN paidAt IS NULL AND dueDate < :nowMillis THEN amountCents ELSE 0 END), 0) AS overdueCents,
            COUNT(CASE WHEN paidAt IS NOT NULL THEN 1 END) AS paidCount,
            COUNT(CASE WHEN paidAt IS NULL AND dueDate >= :nowMillis THEN 1 END) AS pendingCount,
            COUNT(CASE WHEN paidAt IS NULL AND dueDate < :nowMillis THEN 1 END) AS overdueCount
        FROM bill_installments
        WHERE dueDate BETWEEN :startMillis AND :endMillis
          AND status != 'PAUSED'
          AND status != 'SKIPPED'
        """
    )
    fun getMonthSummary(startMillis: Long, endMillis: Long, nowMillis: Long): Flow<MonthSummaryRaw>

    @Query(
        """
        SELECT 
            COALESCE(i.contactId, b.contactId) AS contactId,
            COALESCE(SUM(i.amountCents), 0) AS totalOpenCents
        FROM bill_installments i
        INNER JOIN bills b ON i.billId = b.id
        WHERE i.paidAt IS NULL 
          AND i.status != 'PAUSED'
          AND i.status != 'SKIPPED'
          AND COALESCE(i.contactId, b.contactId) IS NOT NULL
        GROUP BY COALESCE(i.contactId, b.contactId)
        """
    )
    fun getOpenAmountByContact(): Flow<List<ContactOpenBalanceRaw>>

    @Query("""
        SELECT 
            COUNT(CASE WHEN paidAt IS NULL AND dueDate < :startOfDayMillis THEN 1 END) AS overdueCount,
            COUNT(CASE WHEN paidAt IS NULL AND dueDate >= :startOfDayMillis AND dueDate <= :endOfDayMillis THEN 1 END) AS dueTodayCount
        FROM bill_installments
        WHERE status != 'PAUSED' AND status != 'SKIPPED' AND deletedAt IS NULL
    """)
    fun getAttentionCounts(startOfDayMillis: Long, endOfDayMillis: Long): Flow<AttentionCountsRaw>

    @Query("""
        SELECT 
            COALESCE(SUM(i.amountCents), 0) AS totalAmountCents,
            COUNT(i.id) AS installmentCount,
            MAX(i.dueDate) AS lastDueDate
        FROM bill_installments i
        INNER JOIN bills b ON i.billId = b.id
        WHERE b.type = 'INSTALLMENT' AND i.paidAt IS NULL AND i.status != 'PAUSED' AND i.status != 'SKIPPED' AND b.deletedAt IS NULL AND i.deletedAt IS NULL
    """)
    fun getOpenInstallmentsOverview(): Flow<OpenInstallmentsRaw>

    @Query("""
        SELECT 
            b.id AS billId,
            b.title AS title,
            MAX(i.dueDate) AS endDate,
            CAST(ROUND(AVG(i.amountCents)) AS INTEGER) AS monthlyAmountCents
        FROM bills b
        INNER JOIN bill_installments i ON b.id = i.billId
        WHERE b.type = 'INSTALLMENT' AND b.deletedAt IS NULL AND i.deletedAt IS NULL AND i.status != 'PAUSED' AND i.status != 'SKIPPED'
        GROUP BY b.id, b.title
        HAVING COUNT(CASE WHEN i.paidAt IS NULL THEN 1 END) > 0
        ORDER BY endDate ASC
        LIMIT 5
    """)
    fun getFreeingUpInstallments(): Flow<List<FreeingUpCommitmentRaw>>
}
