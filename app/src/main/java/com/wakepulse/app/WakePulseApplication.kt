package com.wakepulse.app

import android.app.Application
import com.wakepulse.app.alarm.AndroidAlarmGateway
import com.wakepulse.app.alarm.DiagnosticAlarmScheduler
import com.wakepulse.app.alarm.PulseScheduler
import com.wakepulse.app.data.AndroidPulsePreferences
import com.wakepulse.app.data.pulseDataStore
import com.wakepulse.app.domain.AndroidClock
import com.wakepulse.app.domain.PulseController
import com.wakepulse.app.domain.RestoreAfterBoot
import com.wakepulse.app.pulse.PulseExecutor
import com.wakepulse.app.system.SystemStatusProvider
import com.wakepulse.app.system.SleepGuard
import com.wakepulse.app.receiver.DndStateReceiver
import android.app.NotificationManager
import android.content.IntentFilter
import android.os.Build

class WakePulseApplication : Application() {
    val preferences by lazy { AndroidPulsePreferences(pulseDataStore) }
    val alarmGateway by lazy { AndroidAlarmGateway(this) }
    val scheduler by lazy { PulseScheduler(preferences, alarmGateway, AndroidClock) }
    val controller by lazy { PulseController(preferences, scheduler, AndroidClock) }
    val sleepGuard by lazy { SleepGuard(this) }
    val pulseExecutor by lazy { PulseExecutor(this, preferences, scheduler, AndroidClock, sleepGuard) }
    val restoreAfterBoot by lazy { RestoreAfterBoot(preferences, scheduler) }
    val systemStatusProvider by lazy { SystemStatusProvider(this, alarmGateway) }
    val diagnosticAlarmScheduler by lazy { DiagnosticAlarmScheduler(this) }

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter().apply {
            addAction(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED)
            addAction(NotificationManager.ACTION_NOTIFICATION_POLICY_ACCESS_GRANTED_CHANGED)
        }
        val receiver = DndStateReceiver()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(receiver, filter)
        }
    }
}
