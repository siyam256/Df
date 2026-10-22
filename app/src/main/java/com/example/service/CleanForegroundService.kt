package com.example.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ServiceCompat
import com.example.cleaner.CacheCleanerEngine
import com.example.data.PreferencesManager
import com.example.model.CleanRecord
import com.example.model.CleanType
import com.example.scheduler.DailyCleanScheduler
import com.example.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class CleanForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    companion object {
        private const val TAG = "CleanFgService"
        const val ACTION_START_CLEAN = "com.aistudio.autocachecleaner.START_CLEAN"
        const val EXTRA_IS_AUTO = "extra_is_auto"

        fun start(context: Context, isAuto: Boolean) {
            val intent = Intent(context, CleanForegroundService::class.java).apply {
                action = ACTION_START_CLEAN
                putExtra(EXTRA_IS_AUTO, isAuto)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val prefs = PreferencesManager(this)
        val isAuto = intent?.getBooleanExtra(EXTRA_IS_AUTO, true) ?: true

        // Display brief ongoing notification
        val notification = NotificationHelper.buildForegroundNotification(this, prefs.language)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceCompat.startForeground(
                    this,
                    NotificationHelper.NOTIFICATION_ID_FOREGROUND,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                )
            } else {
                startForeground(NotificationHelper.NOTIFICATION_ID_FOREGROUND, notification)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Foreground start failed: ${e.message}")
        }

        serviceScope.launch {
            try {
                Log.d(TAG, "Starting background cache cleaning routine (isAuto=$isAuto)...")
                val result = CacheCleanerEngine.cleanCache(applicationContext)

                // Save record in history & preferences
                val record = CleanRecord(
                    id = System.currentTimeMillis().toString(),
                    timestamp = System.currentTimeMillis(),
                    freedBytes = result.freedBytes,
                    cleanType = if (isAuto) CleanType.AUTO_SCHEDULED else CleanType.MANUAL,
                    itemsCleaned = result.itemsCleaned,
                    note = result.message
                )
                prefs.addCleanRecord(record)

                // Show notification if user enabled notifications
                if (prefs.isNotificationEnabled) {
                    NotificationHelper.showCleanCompletedNotification(
                        context = applicationContext,
                        freedBytes = result.freedBytes,
                        isAutoScheduled = isAuto,
                        language = prefs.language
                    )
                }

                // If this was an auto scheduled clean, ensure next day's alarm is locked in
                if (isAuto && prefs.isScheduleEnabled) {
                    DailyCleanScheduler.scheduleDailyClean(
                        context = applicationContext,
                        hour = prefs.scheduleHour,
                        minute = prefs.scheduleMinute
                    )
                }

                Log.d(TAG, "Clean complete: freed ${result.freedBytes} bytes")
            } catch (e: Exception) {
                Log.e(TAG, "Error during clean execution", e)
            } finally {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
