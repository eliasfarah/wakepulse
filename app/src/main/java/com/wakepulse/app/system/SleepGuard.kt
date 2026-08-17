package com.wakepulse.app.system

import android.app.NotificationManager
import android.content.Context

class SleepGuard(context: Context) {
    private val notificationManager =
        context.applicationContext.getSystemService(NotificationManager::class.java)

    fun hasPolicyAccess(): Boolean = notificationManager.isNotificationPolicyAccessGranted

    fun isDoNotDisturbActive(): Boolean =
        notificationManager.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL &&
            notificationManager.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_UNKNOWN
}
