package com.platform.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.platform.app.domain.model.Payment

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = BillInstallmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["billInstallmentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("billInstallmentId"),
        Index("paymentDate"),
        Index("paymentMethodId"),
        Index("financialAccountId")
    ]
)
data class PaymentEntity(
    @PrimaryKey
    val id: String,
    val billInstallmentId: String,
    val amountCents: Long,
    val paymentDate: Long,
    val paymentMethodId: String? = null,
    val financialAccountId: String? = null,
    val interestCents: Long = 0L,
    val fineCents: Long = 0L,
    val discountCents: Long = 0L,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null
) {
    fun toDomain(): Payment {
        return Payment(
            id = id,
            billInstallmentId = billInstallmentId,
            amountCents = amountCents,
            paymentDate = paymentDate,
            paymentMethodId = paymentMethodId,
            financialAccountId = financialAccountId,
            interestCents = interestCents,
            fineCents = fineCents,
            discountCents = discountCents,
            notes = notes,
            createdAt = createdAt,
            updatedAt = updatedAt,
            deletedAt = deletedAt
        )
    }

    companion object {
        fun fromDomain(payment: Payment): PaymentEntity {
            return PaymentEntity(
                id = payment.id,
                billInstallmentId = payment.billInstallmentId,
                amountCents = payment.amountCents,
                paymentDate = payment.paymentDate,
                paymentMethodId = payment.paymentMethodId,
                financialAccountId = payment.financialAccountId,
                interestCents = payment.interestCents,
                fineCents = payment.fineCents,
                discountCents = payment.discountCents,
                notes = payment.notes,
                createdAt = payment.createdAt,
                updatedAt = payment.updatedAt,
                deletedAt = payment.deletedAt
            )
        }
    }
}
