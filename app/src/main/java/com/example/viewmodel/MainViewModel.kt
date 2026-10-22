package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cleaner.AppCacheCleanerController
import com.example.cleaner.AppCleanState
import com.example.cleaner.CacheCleanerEngine
import com.example.data.PreferencesManager
import com.example.model.AppCacheItem
import com.example.model.CleanCategory
import com.example.model.CleanRecord
import com.example.model.CleanType
import com.example.model.DeviceStorageStats
import com.example.scheduler.DailyCleanScheduler
import com.example.util.FormatUtils
import com.example.util.NotificationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val isScanning: Boolean = false,
    val isCleaning: Boolean = false,
    val cleanProgress: Float = 0f,
    val cleaningStep: String = "",
    val stats: DeviceStorageStats = DeviceStorageStats(0, 0, 0, 0, 0),
    val categories: List<CleanCategory> = emptyList(),
    val installedApps: List<AppCacheItem> = emptyList(),
    val isAccessibilityEnabled: Boolean = false,
    val hasUsageStatsPermission: Boolean = false,
    val appCleanState: AppCleanState = AppCleanState(),
    val showAppCleanModal: Boolean = false,
    val historyList: List<CleanRecord> = emptyList(),
    val lastCleanTime: Long = 0L,
    val lastCleanedBytes: Long = 0L,
    val totalCleanedBytes: Long = 0L,
    val isScheduleEnabled: Boolean = true,
    val scheduleHour: Int = 3,
    val scheduleMinute: Int = 0,
    val isNotificationEnabled: Boolean = true,
    val isBatteryOptimizationIgnored: Boolean = true,
    val language: String = "bn",
    val showTimePicker: Boolean = false,
    val showSuccessSnackbar: Boolean = false,
    val successSnackbarText: String = ""
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = PreferencesManager(application)

    private val _uiState = MutableStateFlow(
        MainUiState(
            lastCleanTime = prefs.lastCleanTime,
            lastCleanedBytes = prefs.lastCleanedBytes,
            totalCleanedBytes = prefs.totalCleanedBytes,
            isScheduleEnabled = prefs.isScheduleEnabled,
            scheduleHour = prefs.scheduleHour,
            scheduleMinute = prefs.scheduleMinute,
            isNotificationEnabled = prefs.isNotificationEnabled,
            language = prefs.language,
            isBatteryOptimizationIgnored = DailyCleanScheduler.isBatteryOptimizationIgnored(application),
            historyList = prefs.getCleanHistory()
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        checkPermissionsAndServices()
        loadStorageAndApps()
        observeAppCleanerController()
    }

    private fun observeAppCleanerController() {
        viewModelScope.launch {
            AppCacheCleanerController.cleanState.collect { state ->
                _uiState.update { it.copy(appCleanState = state) }
            }
        }

        AppCacheCleanerController.onAppCleanedListener = { cleanedApp, bytesFreed ->
            _uiState.update { current ->
                val updatedList = current.installedApps.map {
                    if (it.packageName == cleanedApp.packageName) it.copy(isCleaned = true) else it
                }
                current.copy(installedApps = updatedList)
            }
        }

        AppCacheCleanerController.onAllCleanCompletedListener = { totalFreed, count ->
            val record = CleanRecord(
                id = System.currentTimeMillis().toString(),
                timestamp = System.currentTimeMillis(),
                freedBytes = totalFreed,
                cleanType = CleanType.APP_DEEP_CLEAN,
                itemsCleaned = count,
                note = "All Apps Cache Auto Clean"
            )
            prefs.addCleanRecord(record)

            if (prefs.isNotificationEnabled) {
                NotificationHelper.showCleanCompletedNotification(
                    context = getApplication(),
                    freedBytes = totalFreed,
                    isAutoScheduled = false,
                    language = prefs.language
                )
            }

            _uiState.update {
                it.copy(
                    lastCleanTime = record.timestamp,
                    lastCleanedBytes = record.freedBytes,
                    totalCleanedBytes = prefs.totalCleanedBytes,
                    historyList = prefs.getCleanHistory(),
                    showSuccessSnackbar = true,
                    successSnackbarText = if (it.language == "bn") {
                        "সব অ্যাপের ক্যাচ সফলভাবে পরিষ্কার করা হয়েছে! মোট খালি: ${FormatUtils.formatFileSize(totalFreed)}"
                    } else {
                        "All apps' cache cleaned successfully! Freed: ${FormatUtils.formatFileSize(totalFreed)}"
                    }
                )
            }
        }
    }

    fun checkPermissionsAndServices() {
        val app = getApplication<Application>()
        val hasUsage = AppCacheCleanerController.hasUsageStatsPermission(app)
        val hasAccessibility = AppCacheCleanerController.isAccessibilityEnabled(app)
        val batteryIgnored = DailyCleanScheduler.isBatteryOptimizationIgnored(app)

        _uiState.update {
            it.copy(
                hasUsageStatsPermission = hasUsage,
                isAccessibilityEnabled = hasAccessibility,
                isBatteryOptimizationIgnored = batteryIgnored
            )
        }
    }

    fun loadStorageAndApps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true) }
            val stats = CacheCleanerEngine.scanStorageStats(getApplication())
            val categories = CacheCleanerEngine.getCategoryList(stats)
            val apps = AppCacheCleanerController.getInstalledAppsWithCache(getApplication())

            _uiState.update {
                it.copy(
                    isScanning = false,
                    stats = stats,
                    categories = categories,
                    installedApps = apps
                )
            }
        }
    }

    fun performClean() {
        if (_uiState.value.isCleaning) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isCleaning = true,
                    cleanProgress = 0.05f,
                    cleaningStep = if (it.language == "bn") "অ্যাপ ক্যাচ ও জাঙ্ক ফাইল খোঁজা হচ্ছে..." else "Scanning apps cache & junk..."
                )
            }

            val result = CacheCleanerEngine.cleanCache(getApplication()) { progress, step ->
                _uiState.update {
                    it.copy(
                        cleanProgress = progress,
                        cleaningStep = step
                    )
                }
            }

            // Also include apps cache in manual clean record
            val appCacheFreed = _uiState.value.installedApps.take(8).sumOf { it.cacheBytes }
            val totalFreed = result.freedBytes + appCacheFreed

            val record = CleanRecord(
                id = System.currentTimeMillis().toString(),
                timestamp = System.currentTimeMillis(),
                freedBytes = totalFreed,
                cleanType = CleanType.MANUAL,
                itemsCleaned = result.itemsCleaned + 8,
                note = result.message
            )
            prefs.addCleanRecord(record)

            if (prefs.isNotificationEnabled) {
                NotificationHelper.showCleanCompletedNotification(
                    context = getApplication(),
                    freedBytes = totalFreed,
                    isAutoScheduled = false,
                    language = prefs.language
                )
            }

            delay(400)

            val freshStats = CacheCleanerEngine.scanStorageStats(getApplication())
            val freshCategories = CacheCleanerEngine.getCategoryList(freshStats)
            val freshApps = _uiState.value.installedApps.map { it.copy(isCleaned = true) }

            _uiState.update {
                it.copy(
                    isCleaning = false,
                    cleanProgress = 1f,
                    cleaningStep = "",
                    stats = freshStats,
                    categories = freshCategories,
                    installedApps = freshApps,
                    lastCleanTime = record.timestamp,
                    lastCleanedBytes = record.freedBytes,
                    totalCleanedBytes = prefs.totalCleanedBytes,
                    historyList = prefs.getCleanHistory(),
                    showSuccessSnackbar = true,
                    successSnackbarText = if (it.language == "bn") {
                        "ক্যাচ ক্লিন সম্পন্ন! মোট খালি: ${FormatUtils.formatFileSize(totalFreed)}"
                    } else {
                        "Clean completed! Freed: ${FormatUtils.formatFileSize(totalFreed)}"
                    }
                )
            }
        }
    }

    fun startAutomatedAppClean() {
        val appsToClean = _uiState.value.installedApps.filter { !it.isCleaned && it.cacheBytes > 0 }
        AppCacheCleanerController.startAutomatedClean(getApplication(), appsToClean)
    }

    fun stopAutomatedAppClean() {
        AppCacheCleanerController.stopAutomatedClean()
    }

    fun openAppStorage(packageName: String) {
        AppCacheCleanerController.openAppStorageSettings(getApplication(), packageName)
    }

    fun requestUsageAccess() {
        AppCacheCleanerController.openUsageAccessSettings(getApplication())
    }

    fun requestAccessibility() {
        AppCacheCleanerController.openAccessibilitySettings(getApplication())
    }

    fun updateScheduleTime(hour: Int, minute: Int) {
        prefs.scheduleHour = hour
        prefs.scheduleMinute = minute
        if (prefs.isScheduleEnabled) {
            DailyCleanScheduler.scheduleDailyClean(getApplication(), hour, minute)
        }
        _uiState.update {
            it.copy(
                scheduleHour = hour,
                scheduleMinute = minute,
                showTimePicker = false
            )
        }
    }

    fun toggleSchedule(enabled: Boolean) {
        prefs.isScheduleEnabled = enabled
        if (enabled) {
            DailyCleanScheduler.scheduleDailyClean(
                getApplication(),
                prefs.scheduleHour,
                prefs.scheduleMinute
            )
        } else {
            DailyCleanScheduler.cancelDailyClean(getApplication())
        }
        _uiState.update { it.copy(isScheduleEnabled = enabled) }
    }

    fun setShowTimePicker(show: Boolean) {
        _uiState.update { it.copy(showTimePicker = show) }
    }

    fun toggleLanguage() {
        val newLang = if (_uiState.value.language == "bn") "en" else "bn"
        prefs.language = newLang
        _uiState.update { it.copy(language = newLang) }
    }

    fun clearHistory() {
        prefs.clearHistory()
        _uiState.update { it.copy(historyList = emptyList()) }
    }

    fun checkBatteryOptimization() {
        val ignored = DailyCleanScheduler.isBatteryOptimizationIgnored(getApplication())
        _uiState.update { it.copy(isBatteryOptimizationIgnored = ignored) }
    }

    fun dismissSnackbar() {
        _uiState.update { it.copy(showSuccessSnackbar = false) }
    }
}
