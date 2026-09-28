package com.platform.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.platform.app.domain.model.BillInstallment
import com.platform.app.domain.model.BillStatus
import com.platform.app.domain.model.BillType

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
        Index("financialAccountId")
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
    val subcategoryId: String? = null,
    val contactId: String? = null,
    val financialAccountId: String? = null,
    val paymentMethodId: String? = null
) {
    fun toDomain(
        billTitle: String,
        categoryId: String?,
        categoryName: String,
        categoryColorHex: String,
        subcategoryName: String?,
        contactName: String?,
        financialAccountName: String?,
        paymentMethodName: String?,
        billType: BillType
    ): BillInstallment {
        return BillInstallment(
            id = id,
            billId = billId,
            billTitle = billTitle,
            categoryId = categoryId,
            categoryName = categoryName,
            categoryColorHex = categoryColorHex,
            subcategoryId = subcategoryId,
            subcategoryName = subcategoryName,
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
            status = try {
                BillStatus.valueOf(status)
            } catch (e: Exception) {
                BillStatus.PENDING
            },
            type = billType
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
                subcategoryId = installment.subcategoryId,
                contactId = installment.contactId,
                financialAccountId = installment.financialAccountId,
                paymentMethodId = installment.paymentMethodId
            )
        }
    }
}
