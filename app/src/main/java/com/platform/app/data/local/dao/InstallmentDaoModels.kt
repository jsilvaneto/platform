package com.platform.app.data.local.dao

import androidx.room.ColumnInfo
import androidx.room.Embedded
import com.platform.app.data.local.entity.BillInstallmentEntity
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.BillType
import com.platform.app.domain.model.ExpenseNature
import com.platform.app.domain.model.MonthSummary
import java.time.LocalDate

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
    @ColumnInfo(name = "lastDueDate") val lastDueDate: LocalDate?
)

data class FreeingUpCommitmentRaw(
    @ColumnInfo(name = "billId") val billId: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "endDate") val endDate: LocalDate?,
    @ColumnInfo(name = "monthlyAmountCents") val monthlyAmountCents: Long
)
