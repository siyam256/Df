package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object FormatUtils {

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 MB"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0

        return when {
            gb >= 1.0 -> String.format(Locale.US, "%.2f GB", gb)
            mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
            kb >= 1.0 -> String.format(Locale.US, "%.0f KB", kb)
            else -> "$bytes B"
        }
    }

    fun formatTime(hour: Int, minute: Int): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(cal.time)
    }

    fun formatDateTime(timestamp: Long): String {
        if (timestamp <= 0) return "—"
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun getNextCleanTimeString(hour: Int, minute: Int, language: String): String {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val isTomorrow = target.timeInMillis <= now.timeInMillis
        val timeFormatted = formatTime(hour, minute)

        return if (language == "bn") {
            if (isTomorrow) "আগামীকাল $timeFormatted" else "আজ $timeFormatted"
        } else {
            if (isTomorrow) "Tomorrow at $timeFormatted" else "Today at $timeFormatted"
        }
    }
}
