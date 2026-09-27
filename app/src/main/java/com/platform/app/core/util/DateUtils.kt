package com.platform.app.core.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    private val ptBrLocale = Locale("pt", "BR")
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", ptBrLocale)
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", ptBrLocale)

    fun formatDate(epochMillis: Long): String {
        return dateFormat.format(Date(epochMillis))
    }

    fun formatMonthYear(epochMillis: Long): String {
        val formatted = monthYearFormat.format(Date(epochMillis))
        return formatted.replaceFirstChar { if (it.isLowerCase()) it.titlecase(ptBrLocale) else it.toString() }
    }

    fun getStartOfMonth(epochMillis: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = epochMillis
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun getEndOfMonth(epochMillis: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = epochMillis
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return cal.timeInMillis
    }

    fun addMonths(epochMillis: Long, months: Int): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = epochMillis
            add(Calendar.MONTH, months)
        }
        return cal.timeInMillis
    }
}
