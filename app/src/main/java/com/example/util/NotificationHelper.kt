package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity

object NotificationHelper {

    const val CHANNEL_ID = "auto_clean_notifications"
    const val CHANNEL_NAME = "Daily Auto Clean"
    const val FOREGROUND_CHANNEL_ID = "auto_clean_service_channel"
    const val FOREGROUND_CHANNEL_NAME = "Cache Cleaning Status"

    const val NOTIFICATION_ID_COMPLETED = 1001
    const val NOTIFICATION_ID_FOREGROUND = 1002

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val cleanChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications when daily auto clean finishes"
                enableLights(true)
                lightColor = 0xFF10B981.toInt()
            }

            val fgChannel = NotificationChannel(
                FOREGROUND_CHANNEL_ID,
                FOREGROUND_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Active background cleaning progress"
            }

            manager.createNotificationChannel(cleanChannel)
            manager.createNotificationChannel(fgChannel)
        }
    }

    fun showCleanCompletedNotification(
        context: Context,
        freedBytes: Long,
        isAutoScheduled: Boolean,
        language: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val freedText = FormatUtils.formatFileSize(freedBytes)
        val title = if (language == "bn") {
            if (isAutoScheduled) "অটো ক্যাচ ক্লিন সফল হয়েছে" else "ক্যাচ ক্লিন সম্পন্ন!"
        } else {
            if (isAutoScheduled) "Auto Cache Clean Complete" else "Cache Cleaned Successfully!"
        }

        val content = if (language == "bn") {
            "আপনার ফোন থেকে $freedText ক্যাচ ও ট্র্যাশ সফলভাবে খালি করা হয়েছে।"
        } else {
            "Freed $freedText of cached files & junk to keep your device smooth."
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(0xFF059669.toInt())
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID_COMPLETED, notification)
    }

    fun buildForegroundNotification(context: Context, language: String): android.app.Notification {
        val title = if (language == "bn") "ব্যাকগ্রাউন্ড ক্যাচ ক্লিনিং চলছে..." else "Cleaning cache in background..."
        val content = if (language == "bn") "মেমোরি ও অপ্রয়োজনীয় ডাটা সরানো হচ্ছে" else "Optimizing storage & clearing junk"

        return NotificationCompat.Builder(context, FOREGROUND_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setColor(0xFF059669.toInt())
            .build()
    }
}
