package com.platform.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.platform.app.data.local.converter.FinancialAccountTypeConverter
import com.platform.app.domain.model.FinancialAccount
import com.platform.app.domain.model.FinancialAccountType

@Entity(tableName = "financial_accounts")
@TypeConverters(FinancialAccountTypeConverter::class)
data class FinancialAccountEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val accountType: FinancialAccountType = FinancialAccountType.CORRENTE,
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
