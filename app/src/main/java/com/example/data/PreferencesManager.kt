package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.CleanRecord
import com.example.model.CleanType
import org.json.JSONArray
import org.json.JSONObject

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "auto_cache_cleaner_prefs"
        private const val KEY_SCHEDULE_ENABLED = "key_schedule_enabled"
        private const val KEY_SCHEDULE_HOUR = "key_schedule_hour"
        private const val KEY_SCHEDULE_MINUTE = "key_schedule_minute"
        private const val KEY_LAST_CLEAN_TIME = "key_last_clean_time"
        private const val KEY_LAST_CLEANED_BYTES = "key_last_cleaned_bytes"
        private const val KEY_TOTAL_CLEANED_BYTES = "key_total_cleaned_bytes"
        private const val KEY_NOTIFY_ENABLED = "key_notify_enabled"
        private const val KEY_DEEP_CLEAN_ENABLED = "key_deep_clean_enabled"
        private const val KEY_LANGUAGE = "key_language"
        private const val KEY_HISTORY = "key_history"
    }

    var isScheduleEnabled: Boolean
        get() = prefs.getBoolean(KEY_SCHEDULE_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_SCHEDULE_ENABLED, value).apply()

    var scheduleHour: Int
        get() = prefs.getInt(KEY_SCHEDULE_HOUR, 3) // Default 03:00 AM (night time when phone is idle)
        set(value) = prefs.edit().putInt(KEY_SCHEDULE_HOUR, value).apply()

    var scheduleMinute: Int
        get() = prefs.getInt(KEY_SCHEDULE_MINUTE, 0)
        set(value) = prefs.edit().putInt(KEY_SCHEDULE_MINUTE, value).apply()

    var lastCleanTime: Long
        get() = prefs.getLong(KEY_LAST_CLEAN_TIME, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_CLEAN_TIME, value).apply()

    var lastCleanedBytes: Long
        get() = prefs.getLong(KEY_LAST_CLEANED_BYTES, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_CLEANED_BYTES, value).apply()

    var totalCleanedBytes: Long
        get() = prefs.getLong(KEY_TOTAL_CLEANED_BYTES, 0L)
        set(value) = prefs.edit().putLong(KEY_TOTAL_CLEANED_BYTES, value).apply()

    var isNotificationEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFY_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFY_ENABLED, value).apply()

    var isDeepCleanEnabled: Boolean
        get() = prefs.getBoolean(KEY_DEEP_CLEAN_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_DEEP_CLEAN_ENABLED, value).apply()

    var language: String
        get() = prefs.getString(KEY_LANGUAGE, "bn") ?: "bn"
        set(value) = prefs.edit().putString(KEY_LANGUAGE, value).apply()

    fun addCleanRecord(record: CleanRecord) {
        val list = getCleanHistory().toMutableList()
        list.add(0, record)
        val trimmed = if (list.size > 30) list.take(30) else list

        val array = JSONArray()
        for (item in trimmed) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("timestamp", item.timestamp)
                put("freedBytes", item.freedBytes)
                put("cleanType", item.cleanType.name)
                put("itemsCleaned", item.itemsCleaned)
                put("note", item.note)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_HISTORY, array.toString()).apply()

        // Also update totals
        lastCleanTime = record.timestamp
        lastCleanedBytes = record.freedBytes
        totalCleanedBytes += record.freedBytes
    }

    fun getCleanHistory(): List<CleanRecord> {
        val jsonStr = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        val list = mutableListOf<CleanRecord>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val type = try {
                    CleanType.valueOf(obj.optString("cleanType", CleanType.MANUAL.name))
                } catch (e: Exception) {
                    CleanType.MANUAL
                }
                list.add(
                    CleanRecord(
                        id = obj.optString("id", System.currentTimeMillis().toString()),
                        timestamp = obj.optLong("timestamp", 0L),
                        freedBytes = obj.optLong("freedBytes", 0L),
                        cleanType = type,
                        itemsCleaned = obj.optInt("itemsCleaned", 0),
                        note = obj.optString("note", "")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun clearHistory() {
        prefs.edit().remove(KEY_HISTORY).apply()
    }
}
