package com.platform.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.platform.app.domain.model.PaymentMethod

@Entity(tableName = "payment_methods")
data class PaymentMethodEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val iconName: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null
) {
    fun toDomain(): PaymentMethod {
        return PaymentMethod(
            id = id,
            name = name,
            iconName = iconName,
            createdAt = createdAt,
            updatedAt = updatedAt,
            deletedAt = deletedAt
        )
    }

    companion object {
        fun fromDomain(method: PaymentMethod): PaymentMethodEntity {
            return PaymentMethodEntity(
                id = method.id,
                name = method.name,
                iconName = method.iconName,
                createdAt = method.createdAt,
                updatedAt = method.updatedAt,
                deletedAt = method.deletedAt
            )
        }
    }
}
