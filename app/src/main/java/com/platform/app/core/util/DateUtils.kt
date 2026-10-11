package com.platform.app.core.util

import com.platform.app.domain.model.RecurrenceFrequency
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

    fun formatDate(date: java.time.LocalDate): String {
        return date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy", ptBrLocale))
    }

    fun formatDayMonth(epochMillis: Long): String {
        val fmt = SimpleDateFormat("dd/MM", ptBrLocale)
        return fmt.format(Date(epochMillis))
    }

    fun formatDayMonth(date: java.time.LocalDate): String {
        return date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM", ptBrLocale))
    }

    fun formatMonthYear(epochMillis: Long): String {
        val formatted = monthYearFormat.format(Date(epochMillis))
        return formatted.replaceFirstChar { if (it.isLowerCase()) it.titlecase(ptBrLocale) else it.toString() }
    }

    fun formatMonthYear(date: java.time.LocalDate): String {
        val formatter = java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy", ptBrLocale)
        val formatted = date.format(formatter)
        return formatted.replaceFirstChar { if (it.isLowerCase()) it.titlecase(ptBrLocale) else it.toString() }
    }

    fun formatShortMonthYear(epochMillis: Long): String {
        val shortFmt = SimpleDateFormat("MMM/yy", ptBrLocale)
        val formatted = shortFmt.format(Date(epochMillis))
        return formatted.replaceFirstChar { if (it.isLowerCase()) it.titlecase(ptBrLocale) else it.toString() }
    }

    fun formatShortMonthYear(date: java.time.LocalDate): String {
        val formatter = java.time.format.DateTimeFormatter.ofPattern("MMM/yy", ptBrLocale)
        val formatted = date.format(formatter)
        return formatted.replaceFirstChar { if (it.isLowerCase()) it.titlecase(ptBrLocale) else it.toString() }
    }

    fun millisToLocalDate(epochMillis: Long): java.time.LocalDate {
        return java.time.Instant.ofEpochMilli(epochMillis)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
    }

    fun localDateToMillis(date: java.time.LocalDate): Long {
        return date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
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

    fun addMonths(date: java.time.LocalDate, months: Int): java.time.LocalDate {
        return date.plusMonths(months.toLong())
    }

    fun getYear(epochMillis: Long): Int {
        val cal = Calendar.getInstance().apply { timeInMillis = epochMillis }
        return cal.get(Calendar.YEAR)
    }

    fun getMonth(epochMillis: Long): Int {
        val cal = Calendar.getInstance().apply { timeInMillis = epochMillis }
        return cal.get(Calendar.MONTH)
    }

    fun getDayOfMonth(epochMillis: Long): Int {
        val cal = Calendar.getInstance().apply { timeInMillis = epochMillis }
        return cal.get(Calendar.DAY_OF_MONTH)
    }

    fun createMonthMillis(year: Int, month: Int): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun addDays(epochMillis: Long, days: Int): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = epochMillis
            add(Calendar.DAY_OF_YEAR, days)
        }
        return cal.timeInMillis
    }

    fun addWeeks(epochMillis: Long, weeks: Int): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = epochMillis
            add(Calendar.WEEK_OF_YEAR, weeks)
        }
        return cal.timeInMillis
    }

    fun addYears(epochMillis: Long, years: Int): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = epochMillis
            add(Calendar.YEAR, years)
        }
        return cal.timeInMillis
    }

    fun addRecurrenceStep(epochMillis: Long, frequency: RecurrenceFrequency, steps: Int): Long {
        if (steps == 0) return epochMillis
        val cal = Calendar.getInstance().apply {
            timeInMillis = epochMillis
            when (frequency) {
                RecurrenceFrequency.DAILY -> add(Calendar.DAY_OF_YEAR, steps)
                RecurrenceFrequency.WEEKLY -> add(Calendar.WEEK_OF_YEAR, steps)
                RecurrenceFrequency.MONTHLY -> add(Calendar.MONTH, steps)
                RecurrenceFrequency.YEARLY -> add(Calendar.YEAR, steps)
            }
        }
        return cal.timeInMillis
    }

    fun addRecurrenceStep(date: java.time.LocalDate, frequency: RecurrenceFrequency, steps: Int): java.time.LocalDate {
        if (steps == 0) return date
        return when (frequency) {
            RecurrenceFrequency.DAILY -> date.plusDays(steps.toLong())
            RecurrenceFrequency.WEEKLY -> date.plusWeeks(steps.toLong())
            RecurrenceFrequency.MONTHLY -> date.plusMonths(steps.toLong())
            RecurrenceFrequency.YEARLY -> date.plusYears(steps.toLong())
        }
    }

    fun getEndOfDay(epochMillis: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = epochMillis
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return cal.timeInMillis
    }

    fun getStartOfDay(epochMillis: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = epochMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun isToday(epochMillis: Long): Boolean {
        val today = Calendar.getInstance()
        val target = Calendar.getInstance().apply { timeInMillis = epochMillis }
        return today.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
    }

    fun isToday(date: java.time.LocalDate): Boolean {
        return date == java.time.LocalDate.now()
    }
}
