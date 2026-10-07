package com.platform.app.data.local.converter

import androidx.room.TypeConverter
import com.platform.app.domain.model.FinancialAccountType

class FinancialAccountTypeConverter {
    @TypeConverter
    fun fromType(type: FinancialAccountType?): String {
        return type?.name ?: FinancialAccountType.CORRENTE.name
    }

    @TypeConverter
    fun toType(value: String?): FinancialAccountType {
        return FinancialAccountType.fromString(value)
    }
}
