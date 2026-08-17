package com.wakepulse.app.system

import android.content.Context
import android.os.PowerManager
import com.wakepulse.app.alarm.AlarmGateway
import com.wakepulse.app.domain.SystemStatus

class SystemStatusProvider(
    context: Context,
    private val alarmGateway: AlarmGateway,
) {
    private val packageName = context.packageName
    private val powerManager = context.getSystemService(PowerManager::class.java)
    private val sleepGuard = SleepGuard(context)

    fun snapshot(): SystemStatus = SystemStatus(
        exactAlarmsAllowed = alarmGateway.canScheduleExactAlarms(),
        ignoringBatteryOptimizations = powerManager.isIgnoringBatteryOptimizations(packageName),
        isDeviceIdle = powerManager.isDeviceIdleMode,
        isInteractive = powerManager.isInteractive,
        dndPolicyAccessGranted = sleepGuard.hasPolicyAccess(),
        isDoNotDisturbActive = sleepGuard.isDoNotDisturbActive(),
    )
}
