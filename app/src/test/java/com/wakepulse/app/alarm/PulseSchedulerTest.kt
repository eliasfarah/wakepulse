package com.wakepulse.app.alarm

import com.wakepulse.app.FakeClock
import com.wakepulse.app.FakePulsePreferences
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PulseSchedulerTest {
    @Test
    fun `schedule cancels previous alarm and uses exact alarm when allowed`() = runTest {
        val gateway = RecordingAlarmGateway(exactAllowed = true)
        val preferences = FakePulsePreferences()
        val clock = FakeClock()
        val scheduler = PulseScheduler(preferences, gateway, clock)

        val precision = scheduler.schedule(9)

        assertEquals(SchedulePrecision.EXACT, precision)
        assertEquals(1, gateway.cancelCount)
        assertEquals(582_000L, gateway.exactTrigger)
        assertEquals(clock.wall + 540_000L, preferences.state.value.nextPulseAtMillis)
    }

    @Test
    fun `schedule uses allow while idle fallback without exact permission`() = runTest {
        val gateway = RecordingAlarmGateway(exactAllowed = false)
        val scheduler = PulseScheduler(FakePulsePreferences(), gateway, FakeClock())

        val precision = scheduler.schedule(5)

        assertEquals(SchedulePrecision.INEXACT_FALLBACK, precision)
        assertEquals(342_000L, gateway.inexactTrigger)
        assertTrue(gateway.exactTrigger == null)
    }

    @Test
    fun `duplicate schedule requests are coalesced but cancel allows a new schedule`() = runTest {
        val gateway = RecordingAlarmGateway(exactAllowed = true)
        val preferences = FakePulsePreferences()
        val scheduler = PulseScheduler(preferences, gateway, FakeClock())

        scheduler.schedule(9)
        scheduler.schedule(9)
        assertEquals(1, gateway.cancelCount)

        scheduler.cancel()
        scheduler.schedule(9)
        assertEquals(3, gateway.cancelCount)
    }

    private class RecordingAlarmGateway(
        private val exactAllowed: Boolean,
    ) : AlarmGateway {
        var cancelCount = 0
        var exactTrigger: Long? = null
        var inexactTrigger: Long? = null

        override fun canScheduleExactAlarms() = exactAllowed
        override fun cancel() { cancelCount++ }
        override fun scheduleExact(triggerAtElapsedRealtime: Long) {
            exactTrigger = triggerAtElapsedRealtime
        }
        override fun scheduleInexactAllowWhileIdle(triggerAtElapsedRealtime: Long) {
            inexactTrigger = triggerAtElapsedRealtime
        }
    }
}
