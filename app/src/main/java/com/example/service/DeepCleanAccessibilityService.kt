package com.example.service

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class DeepCleanAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "DeepCleanAccess"
        var isServiceRunning = false
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isServiceRunning = true
        Log.d(TAG, "DeepCleanAccessibilityService connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val rootNode = rootInActiveWindow ?: return

        try {
            // Check for "Clear cache" or "ক্যাশে মুছুন" button in Settings
            val clearCacheNodes = rootNode.findAccessibilityNodeInfosByText("Clear cache")
                .ifEmpty { rootNode.findAccessibilityNodeInfosByText("Clear Cache") }
                .ifEmpty { rootNode.findAccessibilityNodeInfosByText("ক্যাশে মুছুন") }

            for (node in clearCacheNodes) {
                if (node.isClickable && node.isEnabled) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    Log.d(TAG, "Automated Clear Cache button click executed")
                    break
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Accessibility node inspection error", e)
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
    }
}
