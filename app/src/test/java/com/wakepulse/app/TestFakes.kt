package com.wakepulse.app

import com.wakepulse.app.alarm.PulseScheduleService
import com.wakepulse.app.alarm.SchedulePrecision
import com.wakepulse.app.data.PulsePreferences
import com.wakepulse.app.domain.Clock
import com.wakepulse.app.domain.PulseInterval
import com.wakepulse.app.domain.PulseState
import kotlinx.coroutines.flow.MutableStateFlow

class FakeClock(
    var wall: Long = 1_700_000_000_000L,
    var elapsed: Long = 42_000L,
) : Clock {
    override fun wallTimeMillis(): Long = wall
    override fun elapsedRealtimeMillis(): Long = elapsed
}

class FakePulsePreferences(initial: PulseState = PulseState()) : PulsePreferences {
    override val state = MutableStateFlow(initial)

    override suspend fun setEnabled(enabled: Boolean, nowMillis: Long) {
        val previous = state.value
        state.value = previous.copy(
            enabled = enabled,
            activatedAtMillis = if (enabled && !previous.enabled) nowMillis else previous.activatedAtMillis,
            pulseCount = if (enabled && !previous.enabled) 0L else previous.pulseCount,
            nextPulseAtMillis = if (enabled) previous.nextPulseAtMillis else 0L,
        )
    }

    override suspend fun setInterval(minutes: Int) {
        state.value = state.value.copy(intervalMinutes = PulseInterval.sanitize(minutes))
    }

    override suspend fun recordPulse(timestampMillis: Long) {
        state.value = state.value.copy(
            lastPulseAtMillis = timestampMillis,
            pulseCount = state.value.pulseCount + 1,
            history = (listOf(timestampMillis) + state.value.history).distinct().take(50),
        )
    }

    override suspend fun setNextPulse(timestampMillis: Long) {
        state.value = state.value.copy(nextPulseAtMillis = timestampMillis)
    }

    override suspend fun setPauseDuringDnd(enabled: Boolean) {
        state.value = state.value.copy(pauseDuringDnd = enabled)
    }
}

class FakeScheduleService : PulseScheduleService {
    val scheduledIntervals = mutableListOf<Int>()
    var cancelCount = 0

    override suspend fun schedule(intervalMinutes: Int): SchedulePrecision {
        scheduledIntervals += intervalMinutes
        return SchedulePrecision.EXACT
    }

    override suspend fun scheduleNextIfEnabled(): SchedulePrecision? = null

    override suspend fun cancel() {
        cancelCount++
    }
}
