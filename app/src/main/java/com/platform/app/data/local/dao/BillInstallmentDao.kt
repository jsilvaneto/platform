package com.platform.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.platform.app.data.local.entity.BillInstallmentEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

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
        WHERE i.deletedAt IS NULL AND (b.deletedAt IS NULL OR i.paidAt IS NOT NULL)
          AND i.dueDate BETWEEN :startDate AND :endDate
        ORDER BY i.dueDate ASC
        """
    )
    fun getInstallmentsForPeriod(startDate: LocalDate, endDate: LocalDate): Flow<List<InstallmentWithDetails>>

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
        WHERE i.deletedAt IS NULL AND b.deletedAt IS NULL
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
          AND i.deletedAt IS NULL AND (b.deletedAt IS NULL OR i.paidAt IS NOT NULL)
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
        WHERE (i.contactId = :contactId OR b.contactId = :contactId) 
          AND i.paidAt IS NULL
          AND i.deletedAt IS NULL AND b.deletedAt IS NULL
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
          AND i.deletedAt IS NULL AND b.deletedAt IS NULL
        ORDER BY i.dueDate ASC
        """
    )
    fun getInstallmentsForInvoice(invoiceId: String): Flow<List<InstallmentWithDetails>>

    @Query("SELECT * FROM bill_installments WHERE invoiceId = :invoiceId AND deletedAt IS NULL")
    suspend fun getInstallmentsListForInvoice(invoiceId: String): List<BillInstallmentEntity>

    @Query(
        """
        SELECT invoiceId, COALESCE(SUM(amountCents), 0) AS totalAmountCents
        FROM bill_installments
        WHERE invoiceId IS NOT NULL
          AND status != 'PAUSED'
          AND deletedAt IS NULL
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
          AND deletedAt IS NULL
        GROUP BY invoiceId
        """
    )
    suspend fun getInvoiceTotalsList(): List<InvoiceTotal>

    @Query("SELECT COALESCE(SUM(amountCents), 0) FROM bill_installments WHERE invoiceId = :invoiceId AND status != 'PAUSED' AND deletedAt IS NULL")
    suspend fun getInvoiceTotal(invoiceId: String): Long

    @Query(
        """
        SELECT COUNT(i.id) FROM bill_installments i
        INNER JOIN credit_card_invoices inv ON i.invoiceId = inv.id
        WHERE inv.creditCardId = :cardId
          AND i.deletedAt IS NULL AND inv.deletedAt IS NULL
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
          AND i.deletedAt IS NULL AND b.deletedAt IS NULL
        """
    )
    suspend fun getUnattachedRecurringInstallmentsForCard(cardId: String): List<BillInstallmentEntity>

    @Query("UPDATE bill_installments SET invoiceId = :invoiceId, dueDate = :invoiceDueDate, updatedAt = :updatedAt WHERE id IN (:installmentIds) AND deletedAt IS NULL")
    suspend fun attachInstallmentsToInvoice(
        installmentIds: List<String>,
        invoiceId: String,
        invoiceDueDate: LocalDate,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(installments: List<BillInstallmentEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIgnore(installments: List<BillInstallmentEntity>)

    @Query("UPDATE bill_installments SET paidAt = :paidAt, actualPaymentDate = :actualPaymentDate, status = :status, updatedAt = :updatedAt WHERE id = :id AND deletedAt IS NULL")
    suspend fun updatePayment(
        id: String,
        paidAt: Long?,
        actualPaymentDate: Long?,
        status: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE bill_installments SET actualPaymentDate = :actualPaymentDate, updatedAt = :updatedAt WHERE id = :id AND deletedAt IS NULL")
    suspend fun updateActualPaymentDate(id: String, actualPaymentDate: Long?, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE bill_installments SET actualPaymentDate = :actualPaymentDate, updatedAt = :updatedAt WHERE id IN (:ids) AND deletedAt IS NULL")
    suspend fun updateActualPaymentDateBatch(ids: List<String>, actualPaymentDate: Long?, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE bill_installments SET paidAt = :paidAt, actualPaymentDate = :actualPaymentDate, status = :status, updatedAt = :updatedAt WHERE invoiceId = :invoiceId AND deletedAt IS NULL")
    suspend fun updatePaymentByInvoiceId(
        invoiceId: String,
        paidAt: Long?,
        actualPaymentDate: Long?,
        status: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE bill_installments SET amountCents = :newAmountCents, dueDate = :newDueDate, updatedAt = :updatedAt WHERE id = :id AND deletedAt IS NULL")
    suspend fun updateInstallmentAmountAndDate(
        id: String,
        newAmountCents: Long,
        newDueDate: LocalDate,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE bill_installments 
        SET amountCents = :amountCents, 
            dueDate = :dueDate, 
            itemId = :itemId, 
            contactId = :contactId, 
            financialAccountId = :financialAccountId, 
            paymentMethodId = :paymentMethodId,
            updatedAt = :updatedAt
        WHERE id = :id AND deletedAt IS NULL
    """)
    suspend fun updateInstallmentDetails(
        id: String,
        amountCents: Long,
        dueDate: LocalDate,
        itemId: String?,
        contactId: String?,
        financialAccountId: String?,
        paymentMethodId: String?,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("SELECT * FROM bill_installments WHERE id = :id AND deletedAt IS NULL LIMIT 1")
    suspend fun getEntityById(id: String): BillInstallmentEntity?

    @Query("UPDATE bill_installments SET deletedAt = :deletedAt, updatedAt = :updatedAt WHERE id = :id AND deletedAt IS NULL")
    suspend fun deleteById(id: String, deletedAt: Long = System.currentTimeMillis(), updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE bill_installments SET deletedAt = :deletedAt, updatedAt = :updatedAt WHERE billId = :billId AND paidAt IS NULL AND dueDate >= :fromDueDate AND deletedAt IS NULL")
    suspend fun deletePendingFromDueDate(
        billId: String,
        fromDueDate: LocalDate,
        deletedAt: Long = System.currentTimeMillis(),
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE bill_installments SET amountCents = :newAmountCents, updatedAt = :updatedAt WHERE billId = :billId AND paidAt IS NULL AND dueDate >= :fromDueDate AND deletedAt IS NULL")
    suspend fun updateAmountForPendingFromDueDate(
        billId: String,
        fromDueDate: LocalDate,
        newAmountCents: Long,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE bill_installments SET status = :status, updatedAt = :updatedAt WHERE billId = :billId AND paidAt IS NULL AND deletedAt IS NULL")
    suspend fun updateStatusForPending(billId: String, status: String, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM bill_installments WHERE billId = :billId AND deletedAt IS NULL ORDER BY dueDate ASC")
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
        WHERE i.billId = :billId AND i.deletedAt IS NULL AND b.deletedAt IS NULL
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
          AND i.deletedAt IS NULL
          AND b.deletedAt IS NULL
        ORDER BY i.dueDate ASC
        """
    )
    suspend fun getPendingInstallmentsInRange(startDate: LocalDate, endDate: LocalDate): List<InstallmentWithDetails>

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
          AND i.deletedAt IS NULL
          AND b.deletedAt IS NULL
        ORDER BY i.dueDate ASC
        """
    )
    suspend fun getOverduePendingInstallments(beforeDate: LocalDate): List<InstallmentWithDetails>

    @Query("""
        UPDATE bill_installments 
        SET status = 'SKIPPED', updatedAt = :updatedAt
        WHERE billId = :billId 
          AND paidAt IS NULL 
          AND dueDate >= :startDate 
          AND (:endDate IS NULL OR dueDate <= :endDate)
          AND deletedAt IS NULL
    """)
    suspend fun markPendingAsSkippedInRange(
        billId: String,
        startDate: LocalDate,
        endDate: LocalDate?,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE bill_installments SET deletedAt = :deletedAt, updatedAt = :updatedAt WHERE billId = :billId AND paidAt IS NULL AND dueDate > :cutoffDueDate AND deletedAt IS NULL")
    suspend fun deletePendingAfterDueDate(
        billId: String,
        cutoffDueDate: LocalDate,
        deletedAt: Long = System.currentTimeMillis(),
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE bill_installments SET deletedAt = :deletedAt, updatedAt = :updatedAt WHERE billId = :billId AND paidAt IS NULL AND deletedAt IS NULL")
    suspend fun deleteAllPending(
        billId: String,
        deletedAt: Long = System.currentTimeMillis(),
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE bill_installments SET deletedAt = :deletedAt, updatedAt = :updatedAt WHERE billId = :billId AND deletedAt IS NULL")
    suspend fun deleteByBillId(
        billId: String,
        deletedAt: Long = System.currentTimeMillis(),
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("SELECT * FROM bill_installments WHERE deletedAt IS NULL")
    suspend fun getAllInstallmentsList(): List<BillInstallmentEntity>

    @Query("DELETE FROM bill_installments")
    suspend fun deleteAll()

    @Query("DELETE FROM bill_installments WHERE deletedAt IS NOT NULL AND deletedAt < :olderThanMillis")
    suspend fun purgeDeleted(olderThanMillis: Long)

    @Query(
        """
        SELECT 
            COALESCE(SUM(amountCents), 0) AS totalCents,
            COALESCE(SUM(CASE WHEN paidAt IS NOT NULL THEN amountCents ELSE 0 END), 0) AS paidCents,
            COALESCE(SUM(CASE WHEN paidAt IS NULL AND dueDate >= :todayDate THEN amountCents ELSE 0 END), 0) AS pendingCents,
            COALESCE(SUM(CASE WHEN paidAt IS NULL AND dueDate < :todayDate THEN amountCents ELSE 0 END), 0) AS overdueCents,
            COUNT(CASE WHEN paidAt IS NOT NULL THEN 1 END) AS paidCount,
            COUNT(CASE WHEN paidAt IS NULL AND dueDate >= :todayDate THEN 1 END) AS pendingCount,
            COUNT(CASE WHEN paidAt IS NULL AND dueDate < :todayDate THEN 1 END) AS overdueCount
        FROM bill_installments
        WHERE dueDate BETWEEN :startDate AND :endDate
          AND status != 'PAUSED'
          AND status != 'SKIPPED'
          AND deletedAt IS NULL
        """
    )
    fun getMonthSummary(startDate: LocalDate, endDate: LocalDate, todayDate: LocalDate): Flow<MonthSummaryRaw>

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
          AND i.deletedAt IS NULL
          AND b.deletedAt IS NULL
          AND COALESCE(i.contactId, b.contactId) IS NOT NULL
        GROUP BY COALESCE(i.contactId, b.contactId)
        """
    )
    fun getOpenAmountByContact(): Flow<List<ContactOpenBalanceRaw>>

    @Query("""
        SELECT 
            COUNT(CASE WHEN paidAt IS NULL AND dueDate < :todayDate THEN 1 END) AS overdueCount,
            COUNT(CASE WHEN paidAt IS NULL AND dueDate = :todayDate THEN 1 END) AS dueTodayCount
        FROM bill_installments
        WHERE status != 'PAUSED' AND status != 'SKIPPED' AND deletedAt IS NULL
    """)
    fun getAttentionCounts(todayDate: LocalDate): Flow<AttentionCountsRaw>

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
