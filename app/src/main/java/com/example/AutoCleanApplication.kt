package com.example

import android.app.Application
import android.util.Log
import com.example.data.PreferencesManager
import com.example.scheduler.DailyCleanScheduler
import com.example.util.NotificationHelper

class AutoCleanApplication : Application() {

    companion object {
        const val TAG = "AutoCleanApp"
        lateinit var instance: AutoCleanApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        try {
            // Create notification channels
            NotificationHelper.createNotificationChannels(this)

            // Ensure daily schedule is active if enabled in preferences
            val prefs = PreferencesManager(this)
            if (prefs.isScheduleEnabled) {
                DailyCleanScheduler.scheduleDailyClean(
                    context = this,
                    hour = prefs.scheduleHour,
                    minute = prefs.scheduleMinute
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed initializing auto clean application", e)
        }
    }
}
