package com.wakepulse.app.alarm

import android.util.Log
import com.wakepulse.app.data.PulsePreferences
import com.wakepulse.app.domain.Clock
import com.wakepulse.app.domain.PulseInterval
import com.wakepulse.app.domain.PulseTiming
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

enum class SchedulePrecision {
    EXACT,
    INEXACT_FALLBACK,
}

interface PulseScheduleService {
    suspend fun schedule(intervalMinutes: Int): SchedulePrecision
    suspend fun scheduleNextIfEnabled(): SchedulePrecision?
    suspend fun cancel()
}

class PulseScheduler(
    private val preferences: PulsePreferences,
    private val alarmGateway: AlarmGateway,
    private val clock: Clock,
) : PulseScheduleService {
    private val schedulingMutex = Mutex()
    private var lastScheduleAtElapsed = 0L
    private var lastScheduledInterval = 0
    private var lastExactCapability = false
    private var lastPrecision: SchedulePrecision? = null

    override suspend fun schedule(intervalMinutes: Int): SchedulePrecision = schedulingMutex.withLock {
        val safeMinutes = PulseInterval.sanitize(intervalMinutes)
        val nowElapsed = clock.elapsedRealtimeMillis()
        val exactAllowed = alarmGateway.canScheduleExactAlarms()
        lastPrecision?.let { previousPrecision ->
            if (
                safeMinutes == lastScheduledInterval &&
                exactAllowed == lastExactCapability &&
                nowElapsed - lastScheduleAtElapsed < SCHEDULE_DEBOUNCE_MILLIS
            ) {
                Log.d(TAG, "Reagendamento duplicado coalescido")
                return@withLock previousPrecision
            }
        }
        val triggerElapsed = PulseTiming.nextElapsedRealtime(
            nowElapsed,
            safeMinutes,
        )
        val approximateWallTime = PulseTiming.approximateNextWallTime(
            clock.wallTimeMillis(),
            safeMinutes,
        )

        alarmGateway.cancel()
        Log.d(TAG, "Alarme anterior cancelado antes do reagendamento")

        val precision = if (exactAllowed) {
            try {
                alarmGateway.scheduleExact(triggerElapsed)
                SchedulePrecision.EXACT
            } catch (error: SecurityException) {
                Log.w(TAG, "Acesso a alarmes exatos mudou; usando fallback", error)
                alarmGateway.scheduleInexactAllowWhileIdle(triggerElapsed)
                SchedulePrecision.INEXACT_FALLBACK
            }
        } else {
            alarmGateway.scheduleInexactAllowWhileIdle(triggerElapsed)
            SchedulePrecision.INEXACT_FALLBACK
        }

        preferences.setNextPulse(approximateWallTime)
        lastScheduleAtElapsed = nowElapsed
        lastScheduledInterval = safeMinutes
        lastExactCapability = exactAllowed
        lastPrecision = precision
        Log.i(
            TAG,
            "Pulso agendado em $safeMinutes min; precisão=$precision, elapsed=$triggerElapsed",
        )
        precision
    }

    override suspend fun scheduleNextIfEnabled(): SchedulePrecision? {
        val state = preferences.state.first()
        return if (state.enabled) schedule(state.intervalMinutes) else null
    }

    override suspend fun cancel() {
        schedulingMutex.withLock {
            alarmGateway.cancel()
            preferences.setNextPulse(0L)
            lastPrecision = null
            Log.i(TAG, "Agendamento de pulso cancelado")
        }
    }

    private companion object {
        const val TAG = "WakePulse"
        const val SCHEDULE_DEBOUNCE_MILLIS = 1_500L
    }
}
