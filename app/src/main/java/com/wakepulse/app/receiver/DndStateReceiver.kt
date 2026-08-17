package com.wakepulse.app.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.os.SystemClock
import com.wakepulse.app.WakePulseApplication
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong

class DndStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (
            intent.action != NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED &&
            intent.action != NotificationManager.ACTION_NOTIFICATION_POLICY_ACCESS_GRANTED_CHANGED
        ) return

        val application = context.applicationContext as WakePulseApplication
        val active = application.sleepGuard.isDoNotDisturbActive()
        Log.i(TAG, "Não Perturbe alterado; ativo=$active")

        if (!active) {
            val now = SystemClock.elapsedRealtime()
            val previous = lastResumeRequestAt.getAndSet(now)
            if (now - previous < RESUME_DEBOUNCE_MILLIS) {
                Log.d(TAG, "Broadcast duplicado de saída do Não Perturbe ignorado")
                return
            }
            val pendingResult = goAsync()
            ReceiverScope.io.launch {
                try {
                    application.scheduler.scheduleNextIfEnabled()
                    Log.i(TAG, "Pulsos retomados após fim do Modo Sono/Não Perturbe")
                } catch (error: Exception) {
                    Log.e(TAG, "Falha ao retomar pulsos após Não Perturbe", error)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    private companion object {
        const val TAG = "WakePulse"
        const val RESUME_DEBOUNCE_MILLIS = 2_000L
        val lastResumeRequestAt = AtomicLong(0L)
    }
}
