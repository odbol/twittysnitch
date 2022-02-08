package com.odbol.twittysnitch

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class A11yService : AccessibilityService() {

    private var packages = listOf("com.twitter.android")

    private var shouldTrack = false
    override fun onServiceConnected() {
        val info = serviceInfo
        info.packageNames = packages.toTypedArray()
        info.eventTypes = AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED or
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
        this.serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        var shouldToast = true
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            val componentName = ComponentName(
                event.packageName.toString(),
                event.className.toString()
            )
            shouldToast = false
//            val activityInfo = tryGetActivity(componentName)
//            val isActivity = activityInfo != null
//            if (isActivity) {
                shouldTrack = isActivityToBeTracked(componentName)
//            }
        }
        if (!shouldTrack) {
            return
        }
        val eventType = event.eventType
        var eventText: String? = null
        when (eventType) {
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                eventText = " Text Changed "
            }
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                eventText = " Window Changed "
            }
        }
        eventText = eventType.toString() + eventText + " CD: " + event.contentDescription + " "
        val source = event.source ?: return
        val parent = source.parent
        Log.d(TAG, "New source $eventText : event.recordCount: ${event.recordCount} " + event)
        printAllText(source, shouldToast)
        parent.recycle()
        source.recycle()
    }

    private fun isActivityToBeTracked(activityInfo: ComponentName): Boolean {
        return packages.contains(activityInfo.packageName)
    }

    private fun tryGetActivity(componentName: ComponentName): ActivityInfo? {
        return try {
            packageManager.getActivityInfo(componentName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    private fun printAllText(source: AccessibilityNodeInfo?, shouldToast: Boolean, level: Int = 0) {
        if (source == null) {
            return
        }
//        if (!source.text.isNullOrBlank()) {
            var id = source.viewIdResourceName
            if (id != null) {
                id = id.split("/").toTypedArray()[1]
            }
            val eventData = "id: " + id + ", text:" + source.text + " cd: ${source.contentDescription}"
            val levelIndent = " ".repeat(level)
            Log.d(TAG, levelIndent + eventData)
//            BusProvider.UI_BUS.post(TextChangeEvent(eventData, shouldToast))
//        }
        for (i in 0 until source.childCount) {
            val child = source.getChild(i)
            if (child != null) {
                printAllText(child, shouldToast, level + 1)
                child.recycle()
            }
        }
    }

    override fun onInterrupt() {}

    companion object {
        private const val TAG = "A11yService"
    }
}
