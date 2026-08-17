package com.wakepulse.app.receiver

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.wakepulse.app.WakePulseApplication
import kotlinx.coroutines.launch

class ExactAlarmPermissionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED) return

        Log.i(TAG, "Estado de alarmes exatos alterado; verificando reagendamento")
        val pendingResult = goAsync()
        val application = context.applicationContext as WakePulseApplication
        ReceiverScope.io.launch {
            try {
                if (application.alarmGateway.canScheduleExactAlarms()) {
                    application.restoreAfterBoot.restore()
                }
            } catch (error: Exception) {
                Log.e(TAG, "Erro ao reagendar após concessão de alarmes exatos", error)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private companion object {
        const val TAG = "WakePulse"
    }
}
