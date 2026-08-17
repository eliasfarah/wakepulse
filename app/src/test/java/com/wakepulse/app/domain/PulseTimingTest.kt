package com.wakepulse.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class PulseTimingTest {
    @Test
    fun `next pulse uses elapsed realtime plus configured interval`() {
        assertEquals(582_000L, PulseTiming.nextElapsedRealtime(42_000L, 9))
    }

    @Test
    fun `unsupported interval falls back to nine minutes`() {
        assertEquals(540_000L, PulseTiming.intervalMillis(2))
    }

    @Test
    fun `approximate wall time preserves interval`() {
        assertEquals(1_540_000L, PulseTiming.approximateNextWallTime(1_000_000L, 9))
    }
}
