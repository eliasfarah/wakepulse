package com.wakepulse.app.domain

import com.wakepulse.app.FakePulsePreferences
import com.wakepulse.app.FakeScheduleService
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RestoreAfterBootTest {
    @Test
    fun `boot restores schedule only when enabled`() = runTest {
        val scheduler = FakeScheduleService()
        val enabled = RestoreAfterBoot(
            FakePulsePreferences(PulseState(enabled = true, intervalMinutes = 9)),
            scheduler,
        )

        assertTrue(enabled.restore())
        assertEquals(listOf(9), scheduler.scheduledIntervals)

        val disabled = RestoreAfterBoot(FakePulsePreferences(PulseState(enabled = false)), scheduler)
        assertFalse(disabled.restore())
        assertEquals(listOf(9), scheduler.scheduledIntervals)
    }
}
