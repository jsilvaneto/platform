package com.platform.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.BillStatus
import com.platform.app.domain.model.BillType
import com.platform.app.domain.model.ExpenseNature

@Entity(
    tableName = "bill_installments",
    foreignKeys = [
        ForeignKey(
            entity = BillEntity::class,
            parentColumns = ["id"],
            childColumns = ["billId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("billId"),
        Index("dueDate"),
        Index("contactId"),
        Index("financialAccountId"),
        Index("itemId"),
        Index("invoiceId"),
        Index(value = ["billId", "installmentNumber"], unique = true)
    ]
)
data class BillInstallmentEntity(
    @PrimaryKey
    val id: String,
    val billId: String,
    val installmentNumber: Int,
    val totalInstallments: Int,
    val amountCents: Long,
    val dueDate: Long,
    val paidAt: Long?,
    val status: String,
    val itemId: String? = null,
    val invoiceId: String? = null,
    val contactId: String? = null,
    val financialAccountId: String? = null,
    val paymentMethodId: String? = null,
    val actualPaymentDate: Long? = null
) {
    fun toDomain(
        billTitle: String,
        categoryId: String?,
        categoryName: String,
        categoryColorHex: String,
        categoryIconName: String = "category",
        nature: ExpenseNature = ExpenseNature.NECESSARIO,
        itemName: String?,
        contactName: String?,
        financialAccountName: String?,
        paymentMethodName: String?,
        billType: BillType,
        billCreatedAt: Long? = null,
        billCreditCardId: String? = null
    ): BillInstallment {
        return BillInstallment(
            id = id,
            billId = billId,
            billTitle = billTitle,
            categoryId = categoryId,
            categoryName = categoryName,
            categoryColorHex = categoryColorHex,
            categoryIconName = categoryIconName,
            nature = nature,
            itemId = itemId,
            itemName = itemName,
            invoiceId = invoiceId,
            contactId = contactId,
            contactName = contactName,
            financialAccountId = financialAccountId,
            financialAccountName = financialAccountName,
            paymentMethodId = paymentMethodId,
            paymentMethodName = paymentMethodName,
            installmentNumber = installmentNumber,
            totalInstallments = totalInstallments,
            amountCents = amountCents,
            dueDate = dueDate,
            paidAt = paidAt,
            actualPaymentDate = actualPaymentDate,
            status = try {
                BillStatus.valueOf(status)
            } catch (e: Exception) {
                BillStatus.PENDING
            },
            type = billType,
            createdAt = billCreatedAt ?: dueDate,
            creditCardId = billCreditCardId
        )
    }

    companion object {
        fun fromDomain(installment: BillInstallment): BillInstallmentEntity {
            return BillInstallmentEntity(
                id = installment.id,
                billId = installment.billId,
                installmentNumber = installment.installmentNumber,
                totalInstallments = installment.totalInstallments,
                amountCents = installment.amountCents,
                dueDate = installment.dueDate,
                paidAt = installment.paidAt,
                status = installment.status.name,
                itemId = installment.itemId,
                invoiceId = installment.invoiceId,
                contactId = installment.contactId,
                financialAccountId = installment.financialAccountId,
                paymentMethodId = installment.paymentMethodId,
                actualPaymentDate = installment.actualPaymentDate
            )
        }
    }
}
