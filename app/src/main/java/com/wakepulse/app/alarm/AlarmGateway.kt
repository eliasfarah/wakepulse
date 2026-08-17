package com.wakepulse.app.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.wakepulse.app.receiver.PulseReceiver

interface AlarmGateway {
    fun canScheduleExactAlarms(): Boolean
    fun cancel()
    fun scheduleExact(triggerAtElapsedRealtime: Long)
    fun scheduleInexactAllowWhileIdle(triggerAtElapsedRealtime: Long)
}

class AndroidAlarmGateway(context: Context) : AlarmGateway {
    private val appContext = context.applicationContext
    private val alarmManager = appContext.getSystemService(AlarmManager::class.java)

    override fun canScheduleExactAlarms(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

    override fun cancel() {
        alarmManager.cancel(pulsePendingIntent())
    }

    override fun scheduleExact(triggerAtElapsedRealtime: Long) {
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            triggerAtElapsedRealtime,
            pulsePendingIntent(),
        )
    }

    override fun scheduleInexactAllowWhileIdle(triggerAtElapsedRealtime: Long) {
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            triggerAtElapsedRealtime,
            pulsePendingIntent(),
        )
    }

    private fun pulsePendingIntent(): PendingIntent {
        val intent = Intent(appContext, PulseReceiver::class.java).apply {
            action = PulseReceiver.ACTION_PULSE
            `package` = appContext.packageName
        }
        return PendingIntent.getBroadcast(
            appContext,
            PULSE_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private companion object {
        const val PULSE_REQUEST_CODE = 909
    }
}
