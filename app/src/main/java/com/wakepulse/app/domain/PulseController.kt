package com.wakepulse.app.domain

import android.util.Log
import com.wakepulse.app.alarm.PulseScheduleService
import com.wakepulse.app.alarm.SchedulePrecision
import com.wakepulse.app.data.PulsePreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PulseController(
    private val preferences: PulsePreferences,
    private val scheduler: PulseScheduleService,
    private val clock: Clock,
) {
    private val stateChangeMutex = Mutex()

    suspend fun setEnabled(enabled: Boolean): SchedulePrecision? = stateChangeMutex.withLock {
        if (enabled) {
            preferences.setEnabled(true, clock.wallTimeMillis())
            val interval = preferences.state.first().intervalMinutes
            Log.i(TAG, "WakePulse ativado com intervalo de $interval min")
            scheduler.schedule(interval)
        } else {
            preferences.setEnabled(false, clock.wallTimeMillis())
            scheduler.cancel()
            Log.i(TAG, "WakePulse desativado")
            null
        }
    }

    suspend fun setInterval(minutes: Int): SchedulePrecision? = stateChangeMutex.withLock {
        val safeMinutes = PulseInterval.sanitize(minutes)
        preferences.setInterval(safeMinutes)
        val state = preferences.state.first()
        Log.i(TAG, "Intervalo alterado para $safeMinutes min")
        if (state.enabled) scheduler.schedule(safeMinutes) else null
    }

    suspend fun setPauseDuringDnd(enabled: Boolean) {
        preferences.setPauseDuringDnd(enabled)
        Log.i(TAG, "Pausa durante Não Perturbe: $enabled")
    }

    private companion object {
        const val TAG = "WakePulse"
    }
}

class RestoreAfterBoot(
    private val preferences: PulsePreferences,
    private val scheduler: PulseScheduleService,
) {
    suspend fun restore(): Boolean {
        val state = preferences.state.first()
        if (!state.enabled) return false
        scheduler.schedule(state.intervalMinutes)
        return true
    }
}
