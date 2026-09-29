package com.platform.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = ExpenseItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = CreditCardInvoiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["invoiceId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("categoryId"),
        Index("itemId"),
        Index("invoiceId"),
        Index("dueDate")
    ]
)
data class TransactionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val description: String,
    val amountCents: Long,
    val dueDate: Long,
    val paymentDate: Long? = null,
    val status: String = "PENDENTE", // PENDENTE, PAGO
    val categoryId: String? = null,
    val itemId: String? = null,
    val invoiceId: String? = null,
    val notes: String? = null,
    val isRecurring: Boolean = false,
    val syncStatus: String = "PENDENTE"
)
