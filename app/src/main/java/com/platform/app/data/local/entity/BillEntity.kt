package com.platform.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.platform.app.domain.model.Bill
import com.platform.app.domain.model.BillType
import com.platform.app.domain.model.RecurrenceEndType
import com.platform.app.domain.model.RecurrenceFrequency

@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val type: String, // SINGLE, INSTALLMENT, RECURRING
    val totalAmountCents: Long,
    val categoryId: String?,
    val itemId: String? = null,
    val invoiceId: String? = null,
    val contactId: String? = null,
    val financialAccountId: String? = null,
    val paymentMethodId: String? = null,
    val totalInstallments: Int,
    val recurrenceFrequency: String? = null,
    val recurrenceEndType: String? = null,
    val recurrenceEndDate: Long? = null,
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
            itemId = itemId,
            invoiceId = invoiceId,
            contactId = contactId,
            financialAccountId = financialAccountId,
            paymentMethodId = paymentMethodId,
            totalInstallments = totalInstallments,
            recurrenceFrequency = recurrenceFrequency?.let {
                try { RecurrenceFrequency.valueOf(it) } catch (e: Exception) { null }
            },
            recurrenceEndType = recurrenceEndType?.let {
                try { RecurrenceEndType.valueOf(it) } catch (e: Exception) { null }
            },
            recurrenceEndDate = recurrenceEndDate,
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
                itemId = bill.itemId,
                invoiceId = bill.invoiceId,
                contactId = bill.contactId,
                financialAccountId = bill.financialAccountId,
                paymentMethodId = bill.paymentMethodId,
                totalInstallments = bill.totalInstallments,
                recurrenceFrequency = bill.recurrenceFrequency?.name,
                recurrenceEndType = bill.recurrenceEndType?.name,
                recurrenceEndDate = bill.recurrenceEndDate,
                createdAt = bill.createdAt
            )
        }
    }
}
