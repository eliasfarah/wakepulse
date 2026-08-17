package com.wakepulse.app.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.core.app.ApplicationProvider
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidPulsePreferencesTest {
    @Test
    fun intervalAndHistoryArePersisted() = runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val file = File(context.cacheDir, "wakepulse-test-${System.nanoTime()}.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { file },
        )
        val preferences = AndroidPulsePreferences(dataStore)

        preferences.setInterval(15)
        repeat(55) { preferences.recordPulse(1_000L + it) }
        val state = preferences.state.first()

        assertEquals(15, state.intervalMinutes)
        assertEquals(50, state.history.size)
        assertEquals(1_054L, state.history.first())
        assertTrue(state.pulseCount == 55L)
    }
}
