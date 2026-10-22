package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.PreferencesManager
import com.example.model.CleanRecord
import com.example.model.CleanType
import com.example.util.FormatUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Auto Cache Cleaner", appName)
    }

    @Test
    fun `test preferences manager schedule and history`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = PreferencesManager(context)

        prefs.scheduleHour = 4
        prefs.scheduleMinute = 30
        prefs.isScheduleEnabled = true

        assertEquals(4, prefs.scheduleHour)
        assertEquals(30, prefs.scheduleMinute)
        assertTrue(prefs.isScheduleEnabled)

        val record = CleanRecord(
            id = "test_1",
            timestamp = System.currentTimeMillis(),
            freedBytes = 150L * 1024 * 1024,
            cleanType = CleanType.AUTO_SCHEDULED,
            itemsCleaned = 12,
            note = "Test Clean"
        )
        prefs.addCleanRecord(record)

        val history = prefs.getCleanHistory()
        assertTrue(history.isNotEmpty())
        assertEquals(CleanType.AUTO_SCHEDULED, history.first().cleanType)
    }

    @Test
    fun `test format file size utils`() {
        val formattedMB = FormatUtils.formatFileSize(150L * 1024 * 1024)
        assertEquals("150.0 MB", formattedMB)

        val formattedKB = FormatUtils.formatFileSize(512L * 1024)
        assertEquals("512 KB", formattedKB)
    }

    @Test
    fun `test app cache item creation and clean status`() {
        val item = com.example.model.AppCacheItem(
            packageName = "com.google.android.youtube",
            appName = "YouTube",
            cacheBytes = 450L * 1024 * 1024,
            isSystemApp = false,
            isCleaned = false
        )
        assertEquals("YouTube", item.appName)
        assertEquals(450L * 1024 * 1024, item.cacheBytes)
        val cleaned = item.copy(isCleaned = true)
        assertTrue(cleaned.isCleaned)
    }
}
