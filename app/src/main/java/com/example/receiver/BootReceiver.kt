package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.PreferencesManager
import com.example.scheduler.DailyCleanScheduler

class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        Log.d(TAG, "Boot or Time changed event: $action")

        val validActions = listOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED
        )

        if (validActions.contains(action)) {
            val prefs = PreferencesManager(context)
            if (prefs.isScheduleEnabled) {
                Log.d(TAG, "Re-registering daily clean alarm at ${prefs.scheduleHour}:${prefs.scheduleMinute}")
                DailyCleanScheduler.scheduleDailyClean(
                    context,
                    prefs.scheduleHour,
                    prefs.scheduleMinute
                )
            }
        }
    }
}
