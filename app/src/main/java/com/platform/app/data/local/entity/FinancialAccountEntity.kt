package com.platform.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.platform.app.domain.model.FinancialAccount

@Entity(tableName = "financial_accounts")
data class FinancialAccountEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val accountType: String,
    val colorHex: String
) {
    fun toDomain(): FinancialAccount {
        return FinancialAccount(
            id = id,
            name = name,
            accountType = accountType,
            colorHex = colorHex
        )
    }

    companion object {
        fun fromDomain(account: FinancialAccount): FinancialAccountEntity {
            return FinancialAccountEntity(
                id = account.id,
                name = account.name,
                accountType = account.accountType,
                colorHex = account.colorHex
            )
        }
    }
}
