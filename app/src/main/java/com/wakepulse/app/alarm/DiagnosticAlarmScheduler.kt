package com.wakepulse.app.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import com.wakepulse.app.receiver.PulseReceiver

/**
 * Agenda um alarme separado, iniciado pelo usuário, para comprovar o caminho
 * AlarmManager -> BroadcastReceiver -> WakeLock sem substituir o PendingIntent principal.
 */
class DiagnosticAlarmScheduler(context: Context) {
    private val appContext = context.applicationContext
    private val alarmManager = appContext.getSystemService(AlarmManager::class.java)

    fun scheduleOneMinuteTest(exactAllowed: Boolean): Boolean {
        val pendingIntent = diagnosticPendingIntent()
        val trigger = SystemClock.elapsedRealtime() + TEST_DELAY_MILLIS
        alarmManager.cancel(pendingIntent)
        return try {
            if (exactAllowed) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    trigger,
                    pendingIntent,
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    trigger,
                    pendingIntent,
                )
            }
            Log.i(
                TAG,
                "AUTOTESTE agendado para ~60s; exact=$exactAllowed, elapsed=$trigger",
            )
            exactAllowed
        } catch (error: SecurityException) {
            Log.w(TAG, "Permissão exact mudou durante o autoteste; aplicando fallback", error)
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                trigger,
                pendingIntent,
            )
            false
        }
    }

    private fun diagnosticPendingIntent(): PendingIntent {
        val intent = Intent(appContext, PulseReceiver::class.java).apply {
            action = PulseReceiver.ACTION_DIAGNOSTIC_PULSE
            `package` = appContext.packageName
        }
        return PendingIntent.getBroadcast(
            appContext,
            DIAGNOSTIC_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private companion object {
        const val TAG = "WakePulse"
        const val TEST_DELAY_MILLIS = 60_000L
        const val DIAGNOSTIC_REQUEST_CODE = 910
    }
}
