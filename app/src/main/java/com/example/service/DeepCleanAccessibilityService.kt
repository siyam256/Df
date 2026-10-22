package com.example.service

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.cleaner.AppCacheCleanerController

class DeepCleanAccessibilityService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private var lastClickedTime = 0L

    companion object {
        private const val TAG = "DeepCleanAccess"
        var isServiceRunning = false
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isServiceRunning = true
        Log.d(TAG, "DeepCleanAccessibilityService connected and ready")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val rootNode = rootInActiveWindow ?: return

        val now = System.currentTimeMillis()
        if (now - lastClickedTime < 350) return // Debounce clicks

        try {
            // Step 1: Look for "Clear cache" button on Storage screen
            val clearCacheTerms = listOf(
                "Clear cache",
                "Clear Cache",
                "ক্যাশে মুছুন",
                "ক্লিয়ার ক্যাশে",
                "মুছুন ক্যাশে",
                "Borrar caché",
                "Vider le cache"
            )

            var clearCacheNode: AccessibilityNodeInfo? = null
            for (term in clearCacheTerms) {
                val matches = rootNode.findAccessibilityNodeInfosByText(term)
                for (node in matches) {
                    if (isActionable(node)) {
                        clearCacheNode = node
                        break
                    }
                }
                if (clearCacheNode != null) break
            }

            // Also check standard Android Settings button IDs
            if (clearCacheNode == null) {
                val buttonMatches = rootNode.findAccessibilityNodeInfosByViewId("com.android.settings:id/button2")
                for (node in buttonMatches) {
                    val text = node.text?.toString() ?: ""
                    if (clearCacheTerms.any { text.contains(it, ignoreCase = true) }) {
                        clearCacheNode = node
                        break
                    }
                }
            }

            if (clearCacheNode != null) {
                if (clearCacheNode.isEnabled) {
                    clearCacheNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    lastClickedTime = System.currentTimeMillis()
                    Log.d(TAG, "Clicked 'Clear Cache' button!")

                    AppCacheCleanerController.onClearCacheTriggered(applicationContext)

                    // Go back after clicking
                    handler.postDelayed({
                        performGlobalAction(GLOBAL_ACTION_BACK)
                    }, 400)
                } else {
                    // Cache already 0/cleared
                    Log.d(TAG, "'Clear Cache' button already disabled (cache is 0)")
                    AppCacheCleanerController.onClearCacheTriggered(applicationContext)
                    handler.postDelayed({
                        performGlobalAction(GLOBAL_ACTION_BACK)
                    }, 300)
                }
                return
            }

            // Step 2: If on App Info screen, look for "Storage & cache" row
            val storageTerms = listOf(
                "Storage & cache",
                "Storage and cache",
                "Storage",
                "স্টোরেজ ও ক্যাশ",
                "স্টোরেজ",
                "Almacenamiento",
                "Stockage"
            )

            for (term in storageTerms) {
                val matches = rootNode.findAccessibilityNodeInfosByText(term)
                for (node in matches) {
                    val clickableNode = findClickableParent(node)
                    if (clickableNode != null && clickableNode.isEnabled) {
                        clickableNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        lastClickedTime = System.currentTimeMillis()
                        Log.d(TAG, "Navigating into Storage details...")
                        return
                    }
                }
            }

        } catch (e: Exception) {
            Log.w(TAG, "Accessibility node inspection error", e)
        }
    }

    private fun isActionable(node: AccessibilityNodeInfo): Boolean {
        return node.isClickable || node.parent?.isClickable == true
    }

    private fun findClickableParent(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        var current = node
        while (current != null) {
            if (current.isClickable) return current
            current = current.parent
        }
        return null
    }

    override fun onInterrupt() {
        Log.d(TAG, "DeepCleanAccessibilityService interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
    }
}
