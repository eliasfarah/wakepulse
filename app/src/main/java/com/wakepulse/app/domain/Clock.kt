package com.wakepulse.app.domain

import android.os.SystemClock

interface Clock {
    fun wallTimeMillis(): Long
    fun elapsedRealtimeMillis(): Long
}

object AndroidClock : Clock {
    override fun wallTimeMillis(): Long = System.currentTimeMillis()
    override fun elapsedRealtimeMillis(): Long = SystemClock.elapsedRealtime()
}

object PulseTiming {
    fun intervalMillis(minutes: Int): Long =
        PulseInterval.sanitize(minutes) * 60_000L

    fun nextElapsedRealtime(nowElapsedRealtime: Long, intervalMinutes: Int): Long =
        nowElapsedRealtime + intervalMillis(intervalMinutes)

    fun approximateNextWallTime(nowWallTime: Long, intervalMinutes: Int): Long =
        nowWallTime + intervalMillis(intervalMinutes)
}
