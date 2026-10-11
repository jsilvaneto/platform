package com.platform.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.platform.app.domain.model.CreditCardInvoice
import com.platform.app.domain.model.InvoiceStatus
import java.util.UUID

import java.time.LocalDate

@Entity(
    tableName = "credit_card_invoices",
    foreignKeys = [
        ForeignKey(
            entity = CreditCardEntity::class,
            parentColumns = ["id"],
            childColumns = ["creditCardId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("creditCardId")]
)
data class CreditCardInvoiceEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val creditCardId: String,
    val referenceMonth: String, // "YYYY-MM"
    val closingDate: LocalDate,
    val dueDate: LocalDate,
    val status: String = "ABERTA", // "ABERTA", "FECHADA", "PAGA"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null
) {
    fun toDomain(totalAmountCents: Long = 0L): CreditCardInvoice {
        return CreditCardInvoice(
            id = id,
            creditCardId = creditCardId,
            referenceMonth = referenceMonth,
            closingDate = closingDate,
            dueDate = dueDate,
            status = try { InvoiceStatus.valueOf(status) } catch (e: Exception) { InvoiceStatus.ABERTA },
            totalAmountCents = totalAmountCents,
            createdAt = createdAt,
            updatedAt = updatedAt,
            deletedAt = deletedAt
        )
    }

    companion object {
        fun fromDomain(invoice: CreditCardInvoice): CreditCardInvoiceEntity {
            return CreditCardInvoiceEntity(
                id = invoice.id,
                creditCardId = invoice.creditCardId,
                referenceMonth = invoice.referenceMonth,
                closingDate = invoice.closingDate,
                dueDate = invoice.dueDate,
                status = invoice.status.name,
                createdAt = invoice.createdAt,
                updatedAt = invoice.updatedAt,
                deletedAt = invoice.deletedAt
            )
        }
    }
}
