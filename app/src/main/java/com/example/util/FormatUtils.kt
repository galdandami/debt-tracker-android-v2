package com.example.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

object FormatUtils {

    fun formatCurrency(amount: Double, currencySymbol: String = "₽"): String {
        val formatter = NumberFormat.getInstance(Locale("ru", "RU")).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 0
        }
        val formatted = formatter.format(amount)
        return "$formatted $currencySymbol"
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("d MMMM yyyy", Locale("ru"))
        return sdf.format(Date(timestamp))
    }

    fun formatDateShort(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
        return sdf.format(Date(timestamp))
    }

    /**
     * Calculates deadline status text and color hint category.
     * Returns Triple(StatusText, DaysRemainingInt, IsOverdue)
     */
    fun getDeadlineInfo(dueDateTimestamp: Long?): DeadlineInfo? {
        if (dueDateTimestamp == null) return null

        val nowMillis = System.currentTimeMillis()
        val diffMs = dueDateTimestamp - nowMillis
        val diffDays = (diffMs / (1000 * 60 * 60 * 24)).toInt()

        return when {
            diffMs < 0 -> {
                val overdueDays = abs((diffMs / (1000 * 60 * 60 * 24)).toInt())
                val text = if (overdueDays == 0) "Дедлайн сегодня!" else "Просрочено на $overdueDays ${getDaysWord(overdueDays)}"
                DeadlineInfo(text = text, days = -overdueDays, isOverdue = true, isWarning = true)
            }
            diffDays == 0 -> {
                DeadlineInfo(text = "Дедлайн сегодня", days = 0, isOverdue = false, isWarning = true)
            }
            diffDays <= 3 -> {
                DeadlineInfo(text = "Осталось $diffDays ${getDaysWord(diffDays)}", days = diffDays, isOverdue = false, isWarning = true)
            }
            else -> {
                DeadlineInfo(text = "Осталось $diffDays ${getDaysWord(diffDays)}", days = diffDays, isOverdue = false, isWarning = false)
            }
        }
    }

    private fun getDaysWord(days: Int): String {
        val absDays = abs(days) % 100
        val lastDigit = absDays % 10
        return when {
            absDays in 11..19 -> "дней"
            lastDigit == 1 -> "день"
            lastDigit in 2..4 -> "дня"
            else -> "дней"
        }
    }
}

data class DeadlineInfo(
    val text: String,
    val days: Int,
    val isOverdue: Boolean,
    val isWarning: Boolean
)
