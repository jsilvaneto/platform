package com.platform.app.data.local.converter

import androidx.room.TypeConverter
import java.time.LocalDate

class LocalDateEpochDayConverter {
    @TypeConverter
    fun fromEpochDay(value: Int?): LocalDate? {
        return value?.let { LocalDate.ofEpochDay(it.toLong()) }
    }

    @TypeConverter
    fun toEpochDay(date: LocalDate?): Int? {
        return date?.toEpochDay()?.toInt()
    }
}
