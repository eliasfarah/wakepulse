package com.wakepulse.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.wakepulse.app.WakePulseApplication
import com.wakepulse.app.domain.PulseSource
import kotlinx.coroutines.launch

class PulseReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_PULSE && intent.action != ACTION_DIAGNOSTIC_PULSE) return

        val diagnostic = intent.action == ACTION_DIAGNOSTIC_PULSE
        Log.i(TAG, if (diagnostic) "Alarme de AUTOTESTE disparado" else "Alarme de pulso disparado")
        val pendingResult = goAsync()
        val application = context.applicationContext as WakePulseApplication
        ReceiverScope.io.launch {
            try {
                application.pulseExecutor.execute(PulseSource.ALARM)
            } catch (error: Exception) {
                Log.e(TAG, "Falha não tratada no receiver de pulso", error)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_PULSE = "com.wakepulse.app.action.PULSE"
        const val ACTION_DIAGNOSTIC_PULSE = "com.wakepulse.app.action.DIAGNOSTIC_PULSE"
        private const val TAG = "WakePulse"
    }
}
