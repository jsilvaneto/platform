package com.platform.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.platform.app.domain.model.CreditCardInvoice
import com.platform.app.domain.model.InvoiceStatus
import java.util.UUID

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
    val closingDate: Long,
    val dueDate: Long,
    val status: String = "ABERTA", // "ABERTA", "FECHADA", "PAGA"
    val syncStatus: String = "PENDENTE"
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
            syncStatus = syncStatus
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
                syncStatus = invoice.syncStatus
            )
        }
    }
}
