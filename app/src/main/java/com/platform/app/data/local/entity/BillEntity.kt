package com.platform.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.platform.app.domain.model.Bill
import com.platform.app.domain.model.BillType

@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val type: String, // SINGLE, INSTALLMENT, RECURRING
    val totalAmountCents: Long,
    val categoryId: String?,
    val subcategoryId: String? = null,
    val contactId: String? = null,
    val financialAccountId: String? = null,
    val paymentMethodId: String? = null,
    val totalInstallments: Int,
    val createdAt: Long
) {
    fun toDomain(): Bill {
        return Bill(
            id = id,
            title = title,
            description = description,
            type = try {
                BillType.valueOf(type)
            } catch (e: Exception) {
                BillType.SINGLE
            },
            totalAmountCents = totalAmountCents,
            categoryId = categoryId,
            subcategoryId = subcategoryId,
            contactId = contactId,
            financialAccountId = financialAccountId,
            paymentMethodId = paymentMethodId,
            totalInstallments = totalInstallments,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromDomain(bill: Bill): BillEntity {
            return BillEntity(
                id = bill.id,
                title = bill.title,
                description = bill.description,
                type = bill.type.name,
                totalAmountCents = bill.totalAmountCents,
                categoryId = bill.categoryId,
                subcategoryId = bill.subcategoryId,
                contactId = bill.contactId,
                financialAccountId = bill.financialAccountId,
                paymentMethodId = bill.paymentMethodId,
                totalInstallments = bill.totalInstallments,
                createdAt = bill.createdAt
            )
        }
    }
}
