package com.odbol.twittysnitch

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS
import android.view.accessibility.AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS

class A11yService : AccessibilityService() {

    private lateinit var highlighter: Highlighter
    private var packages = listOf("com.twitter.android")

    private var shouldTrack = false
    override fun onServiceConnected() {
        val info = serviceInfo
        info.packageNames = packages.toTypedArray()
        info.eventTypes = AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED or
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
        this.serviceInfo = info

//        setAccessibilityFocusAppearance(2, Color.GREEN)
//        highlighter.onConnected()
    }

    override fun onCreate() {
        super.onCreate()
        highlighter = Highlighter(this)
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
        event.windowId
        eventText = eventType.toString() + eventText + " CD: " + event.contentDescription + " "
        val source = event.source ?: return
//        source.performAction(ACTION_CLEAR_ACCESSIBILITY_FOCUS)
//        val parent = source.parent
        Log.d(TAG, "New source $eventText : event.recordCount: ${event.recordCount} " + event)
        highlighter.clearHighlights()
        printAllText(source, shouldToast)
//        parent?.recycle()
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

    private fun printAllText(source: AccessibilityNodeInfo?, shouldToast: Boolean, path: List<String> = listOf()) {
        if (source == null || !source.isVisibleToUser) {
            return
        }
//        if (!source.text.isNullOrBlank()) {
            var id = source.viewIdResourceName
            if (id != null) {
                id = id.split("/").toTypedArray()[1]
            }
            val eventData = "id: " + id + ", text:" + source.text + " cd: ${source.contentDescription}"
            val levelIndent = " ".repeat(path.size)
            Log.d(TAG, levelIndent + eventData)

            if (isTweet(path, source)) {
                highlight(source);
            }
//            BusProvider.UI_BUS.post(TextChangeEvent(eventData, shouldToast))
//        }
        for (i in 0 until source.childCount) {
            val child = source.getChild(i)
            if (child != null) {
                printAllText(child, shouldToast, path + id)
                child.recycle()
            }
        }
    }

    private val highlightRect = Rect()
    private fun highlight(source: AccessibilityNodeInfo) {
        source.getBoundsInScreen(highlightRect)
        Log.d(TAG, "highlight $highlightRect")
        source.performAction(ACTION_ACCESSIBILITY_FOCUS)
//        highlighter.addHighlight(highlightRect)
    }

    private fun isTweet(path: List<String>, source: AccessibilityNodeInfo): Boolean {
        return (path.containsAll(listOf("nested_coordinator_layout", "list", "row")) &&
                source.contentDescription?.lastIndexOf("retweets") != -1)
    }

    override fun onInterrupt() {}

    companion object {
        private const val TAG = "A11yService"
    }
}
