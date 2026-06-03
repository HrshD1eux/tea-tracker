package com.example.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    // Standard format for indexing database records
    private const val DB_DATE_FORMAT = "yyyy-MM-dd"

    fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat(DB_DATE_FORMAT, Locale.getDefault())
        return sdf.format(Date())
    }

    fun getCurrentDayName(): String {
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format(Date())
    }

    fun getFormattedDate(dateString: String): String {
        return try {
            val parser = SimpleDateFormat(DB_DATE_FORMAT, Locale.getDefault())
            val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val date = parser.parse(dateString)
            if (date != null) formatter.format(date) else dateString
        } catch (e: Exception) {
            dateString
        }
    }

    fun getShortDate(dateString: String): String {
        return try {
            val parser = SimpleDateFormat(DB_DATE_FORMAT, Locale.getDefault())
            val formatter = SimpleDateFormat("dd MMM", Locale.getDefault())
            val date = parser.parse(dateString)
            if (date != null) formatter.format(date) else dateString
        } catch (e: Exception) {
            dateString
        }
    }

    fun getDayName(dateString: String): String {
        return try {
            val parser = SimpleDateFormat(DB_DATE_FORMAT, Locale.getDefault())
            val formatter = SimpleDateFormat("EEEE", Locale.getDefault())
            val date = parser.parse(dateString)
            if (date != null) formatter.format(date) else ""
        } catch (e: Exception) {
            ""
        }
    }

    fun getCurrentMonthYearString(): String {
        // e.g., "2026-05" - useful to filter this month's records
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return sdf.format(Date())
    }

    fun getMonthYearString(dateString: String): String {
        // Input: "yyyy-MM-dd" -> Output: "yyyy-MM" E.g., "2026-05"
        return if (dateString.length >= 7) {
            dateString.substring(0, 7)
        } else {
            dateString
        }
    }

    fun formatMonthYear(monthStr: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            val formatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            val date = parser.parse(monthStr)
            if (date != null) formatter.format(date) else monthStr
        } catch (e: Exception) {
            monthStr
        }
    }
}

