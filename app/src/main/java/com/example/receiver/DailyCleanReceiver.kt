package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import com.example.data.PreferencesManager
import com.example.scheduler.DailyCleanScheduler
import com.example.service.CleanForegroundService

class DailyCleanReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "DailyCleanReceiver"
        const val ACTION_DAILY_CLEAN = "com.aistudio.autocachecleaner.ACTION_DAILY_CLEAN"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        Log.d(TAG, "Received daily clean alarm broadcast at: ${System.currentTimeMillis()}")

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val wakeLock = powerManager?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "AutoCacheCleaner:DailyCleanWakeLock"
        )

        try {
            wakeLock?.acquire(60_000L) // 1 minute max safeguard
            val prefs = PreferencesManager(context)

            if (prefs.isScheduleEnabled) {
                CleanForegroundService.start(context, isAuto = true)
                // Schedule the next cycle for tomorrow at the same time
                DailyCleanScheduler.scheduleDailyClean(
                    context,
                    prefs.scheduleHour,
                    prefs.scheduleMinute
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling daily clean trigger", e)
        } finally {
            try {
                if (wakeLock?.isHeld == true) {
                    wakeLock.release()
                }
            } catch (e: Exception) {
                // Ignore wake lock release errors
            }
        }
    }
}
