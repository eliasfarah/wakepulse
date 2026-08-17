package com.wakepulse.app.domain

import com.wakepulse.app.FakeClock
import com.wakepulse.app.FakePulsePreferences
import com.wakepulse.app.FakeScheduleService
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PulseControllerTest {
    @Test
    fun `activation persists state and schedules selected interval`() = runTest {
        val preferences = FakePulsePreferences(PulseState(intervalMinutes = 15, pulseCount = 8))
        val scheduler = FakeScheduleService()
        val clock = FakeClock()
        val controller = PulseController(preferences, scheduler, clock)

        controller.setEnabled(true)

        assertTrue(preferences.state.value.enabled)
        assertEquals(clock.wall, preferences.state.value.activatedAtMillis)
        assertEquals(0L, preferences.state.value.pulseCount)
        assertEquals(listOf(15), scheduler.scheduledIntervals)
    }

    @Test
    fun `deactivation persists state and cancels alarm`() = runTest {
        val preferences = FakePulsePreferences(PulseState(enabled = true, nextPulseAtMillis = 123L))
        val scheduler = FakeScheduleService()
        val controller = PulseController(preferences, scheduler, FakeClock())

        controller.setEnabled(false)

        assertFalse(preferences.state.value.enabled)
        assertEquals(1, scheduler.cancelCount)
        assertEquals(0L, preferences.state.value.nextPulseAtMillis)
    }

    @Test
    fun `interval change is persisted and active mechanism is rescheduled`() = runTest {
        val preferences = FakePulsePreferences(PulseState(enabled = true))
        val scheduler = FakeScheduleService()
        val controller = PulseController(preferences, scheduler, FakeClock())

        controller.setInterval(30)

        assertEquals(30, preferences.state.value.intervalMinutes)
        assertEquals(listOf(30), scheduler.scheduledIntervals)
    }
}
