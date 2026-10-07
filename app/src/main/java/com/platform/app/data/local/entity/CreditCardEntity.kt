package com.platform.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.platform.app.domain.model.CreditCard
import java.util.UUID

@Entity(tableName = "credit_cards")
data class CreditCardEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val totalLimitCents: Long,
    val closingDay: Int,
    val dueDay: Int,
    val colorHex: String = "#3B82F6"
) {
    fun toDomain(): CreditCard {
        return CreditCard(
            id = id,
            name = name,
            totalLimitCents = totalLimitCents,
            closingDay = closingDay,
            dueDay = dueDay,
            colorHex = colorHex
        )
    }

    companion object {
        fun fromDomain(card: CreditCard): CreditCardEntity {
            return CreditCardEntity(
                id = card.id,
                name = card.name,
                totalLimitCents = card.totalLimitCents,
                closingDay = card.closingDay,
                dueDay = card.dueDay,
                colorHex = card.colorHex
            )
        }
    }
}
