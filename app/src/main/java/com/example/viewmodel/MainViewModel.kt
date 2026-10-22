package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cleaner.CacheCleanerEngine
import com.example.data.PreferencesManager
import com.example.model.CleanCategory
import com.example.model.CleanRecord
import com.example.model.CleanType
import com.example.model.DeviceStorageStats
import com.example.scheduler.DailyCleanScheduler
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
        loadStorageStats()
        checkBatteryOptimization()
    }

    fun loadStorageStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true) }
            val stats = CacheCleanerEngine.scanStorageStats(getApplication())
            val categories = CacheCleanerEngine.getCategoryList(stats)
            _uiState.update {
                it.copy(
                    isScanning = false,
                    stats = stats,
                    categories = categories
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
                    cleaningStep = if (it.language == "bn") "ক্লিন শুরু হচ্ছে..." else "Starting clean..."
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

            // Record clean
            val record = CleanRecord(
                id = System.currentTimeMillis().toString(),
                timestamp = System.currentTimeMillis(),
                freedBytes = result.freedBytes,
                cleanType = CleanType.MANUAL,
                itemsCleaned = result.itemsCleaned,
                note = result.message
            )
            prefs.addCleanRecord(record)

            if (prefs.isNotificationEnabled) {
                NotificationHelper.showCleanCompletedNotification(
                    context = getApplication(),
                    freedBytes = result.freedBytes,
                    isAutoScheduled = false,
                    language = prefs.language
                )
            }

            delay(400) // Brief pause to show completion animation

            // Refresh stats to reflect cleaned state
            val freshStats = CacheCleanerEngine.scanStorageStats(getApplication())
            val freshCategories = CacheCleanerEngine.getCategoryList(freshStats)

            _uiState.update {
                it.copy(
                    isCleaning = false,
                    cleanProgress = 1f,
                    cleaningStep = "",
                    stats = freshStats,
                    categories = freshCategories,
                    lastCleanTime = record.timestamp,
                    lastCleanedBytes = record.freedBytes,
                    totalCleanedBytes = prefs.totalCleanedBytes,
                    historyList = prefs.getCleanHistory(),
                    showSuccessSnackbar = true,
                    successSnackbarText = if (it.language == "bn") {
                        "সফলভাবে ক্লিন সম্পন্ন হয়েছে!"
                    } else {
                        "Clean completed successfully!"
                    }
                )
            }
        }
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

    fun toggleNotifications(enabled: Boolean) {
        prefs.isNotificationEnabled = enabled
        _uiState.update { it.copy(isNotificationEnabled = enabled) }
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
