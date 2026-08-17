package com.wakepulse.app.data

import com.wakepulse.app.domain.PulseState
import kotlinx.coroutines.flow.Flow

interface PulsePreferences {
    val state: Flow<PulseState>

    suspend fun setEnabled(enabled: Boolean, nowMillis: Long)
    suspend fun setInterval(minutes: Int)
    suspend fun recordPulse(timestampMillis: Long)
    suspend fun setNextPulse(timestampMillis: Long)
    suspend fun setPauseDuringDnd(enabled: Boolean)
}
