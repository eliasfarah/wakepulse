package com.wakepulse.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.wakepulse.app.WakePulseApplication
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED && intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return

        Log.i(TAG, "Evento de restauração recebido: ${intent.action}")
        val pendingResult = goAsync()
        val application = context.applicationContext as WakePulseApplication
        ReceiverScope.io.launch {
            try {
                val restored = application.restoreAfterBoot.restore()
                Log.i(TAG, if (restored) "Pulso restaurado após reboot" else "WakePulse inativo; nada a restaurar")
            } catch (error: Exception) {
                Log.e(TAG, "Erro ao restaurar após reboot", error)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private companion object {
        const val TAG = "WakePulse"
    }
}
