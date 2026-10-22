package com.example.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.util.Log
import com.example.receiver.DailyCleanReceiver
import java.util.Calendar

object DailyCleanScheduler {

    private const val TAG = "DailyCleanScheduler"
    const val REQUEST_CODE = 4401

    fun scheduleDailyClean(context: Context, hour: Int, minute: Int): Boolean {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager not available")
            return false
        }

        val intent = Intent(context, DailyCleanReceiver::class.java).apply {
            action = DailyCleanReceiver.ACTION_DAILY_CLEAN
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val targetTime = calendar.timeInMillis
        Log.d(TAG, "Scheduling next daily clean for: ${calendar.time} ($targetTime)")

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Allows alarm to wake up phone even in Doze mode while idle / screen is off!
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    targetTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    targetTime,
                    pendingIntent
                )
            }
            return true
        } catch (e: SecurityException) {
            Log.w(TAG, "Exact alarm permission check required on Android 12+, falling back to setWindow/setAndAllowWhileIdle", e)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        targetTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, targetTime, pendingIntent)
                }
                return true
            } catch (ex: Exception) {
                Log.e(TAG, "Failed setting alarm fallback", ex)
                return false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed setting daily clean alarm", e)
            return false
        }
    }

    fun cancelDailyClean(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, DailyCleanReceiver::class.java).apply {
            action = DailyCleanReceiver.ACTION_DAILY_CLEAN
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Cancelled scheduled daily clean alarm")
        }
    }

    fun isBatteryOptimizationIgnored(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            return pm?.isIgnoringBatteryOptimizations(context.packageName) ?: true
        }
        return true
    }
}
