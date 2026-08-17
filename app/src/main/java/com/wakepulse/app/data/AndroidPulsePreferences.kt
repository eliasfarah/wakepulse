package com.wakepulse.app.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wakepulse.app.domain.PulseInterval
import com.wakepulse.app.domain.PulseState
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

val Context.pulseDataStore: DataStore<Preferences> by preferencesDataStore(name = "wakepulse")

class AndroidPulsePreferences(
    private val dataStore: DataStore<Preferences>,
) : PulsePreferences {
    override val state: Flow<PulseState> = dataStore.data
        .catch { error ->
            if (error is IOException) {
                Log.e(TAG, "Falha ao ler DataStore; usando estado padrão", error)
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw error
            }
        }
        .map { preferences -> preferences.toPulseState() }

    override suspend fun setEnabled(enabled: Boolean, nowMillis: Long) {
        dataStore.edit { preferences ->
            val wasEnabled = preferences[Keys.ENABLED] ?: false
            preferences[Keys.ENABLED] = enabled
            if (enabled && !wasEnabled) {
                preferences[Keys.ACTIVATED_AT] = nowMillis
                preferences[Keys.PULSE_COUNT] = 0L
            }
            if (!enabled) preferences[Keys.NEXT_PULSE_AT] = 0L
        }
    }

    override suspend fun setInterval(minutes: Int) {
        dataStore.edit { preferences ->
            preferences[Keys.INTERVAL_MINUTES] = PulseInterval.sanitize(minutes)
        }
    }

    override suspend fun recordPulse(timestampMillis: Long) {
        dataStore.edit { preferences ->
            preferences[Keys.LAST_PULSE_AT] = timestampMillis
            preferences[Keys.PULSE_COUNT] = (preferences[Keys.PULSE_COUNT] ?: 0L) + 1L
            val history = decodeHistory(preferences[Keys.HISTORY]).toMutableList()
            history.add(0, timestampMillis)
            preferences[Keys.HISTORY] = history.distinct().take(HISTORY_LIMIT).joinToString(",")
        }
    }

    override suspend fun setNextPulse(timestampMillis: Long) {
        dataStore.edit { preferences ->
            preferences[Keys.NEXT_PULSE_AT] = timestampMillis
        }
    }

    override suspend fun setPauseDuringDnd(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.PAUSE_DURING_DND] = enabled
        }
    }

    private fun Preferences.toPulseState(): PulseState = PulseState(
        enabled = this[Keys.ENABLED] ?: false,
        intervalMinutes = PulseInterval.sanitize(
            this[Keys.INTERVAL_MINUTES] ?: PulseInterval.RECOMMENDED_MINUTES,
        ),
        lastPulseAtMillis = this[Keys.LAST_PULSE_AT] ?: 0L,
        nextPulseAtMillis = this[Keys.NEXT_PULSE_AT] ?: 0L,
        pulseCount = this[Keys.PULSE_COUNT] ?: 0L,
        activatedAtMillis = this[Keys.ACTIVATED_AT] ?: 0L,
        history = decodeHistory(this[Keys.HISTORY]),
        pauseDuringDnd = this[Keys.PAUSE_DURING_DND] ?: true,
    )

    private fun decodeHistory(raw: String?): List<Long> = raw
        ?.split(',')
        ?.mapNotNull(String::toLongOrNull)
        ?.take(HISTORY_LIMIT)
        .orEmpty()

    private object Keys {
        val ENABLED = booleanPreferencesKey("enabled")
        val INTERVAL_MINUTES = intPreferencesKey("interval_minutes")
        val LAST_PULSE_AT = longPreferencesKey("last_pulse_at")
        val NEXT_PULSE_AT = longPreferencesKey("next_pulse_at")
        val PULSE_COUNT = longPreferencesKey("pulse_count")
        val ACTIVATED_AT = longPreferencesKey("activated_at")
        val HISTORY = stringPreferencesKey("pulse_history")
        val PAUSE_DURING_DND = booleanPreferencesKey("pause_during_dnd")
    }

    companion object {
        private const val TAG = "WakePulse"
        const val HISTORY_LIMIT = 50
    }
}
