package com.wakepulse.app.pulse

import android.content.Context
import android.os.PowerManager
import android.util.Log
import com.wakepulse.app.alarm.PulseScheduleService
import com.wakepulse.app.data.PulsePreferences
import com.wakepulse.app.domain.Clock
import com.wakepulse.app.domain.PulseSource
import com.wakepulse.app.system.SleepGuard
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex

class PulseExecutor(
    context: Context,
    private val preferences: PulsePreferences,
    private val scheduler: PulseScheduleService,
    private val clock: Clock,
    private val sleepGuard: SleepGuard,
) {
    private val powerManager = context.applicationContext.getSystemService(PowerManager::class.java)
    private val wakeLockTag = "${context.packageName}:WakePulse"

    suspend fun execute(source: PulseSource): Boolean {
        if (!executionMutex.tryLock()) {
            Log.w(TAG, "Pulso $source ignorado: outra execução já está em andamento")
            return false
        }

        if (source == PulseSource.ALARM) {
            val state = preferences.state.first()
            if (state.pauseDuringDnd && sleepGuard.isDoNotDisturbActive()) {
                return try {
                    Log.i(TAG, "Pulso pausado: Não Perturbe/Modo Sono está ativo")
                    scheduler.scheduleNextIfEnabled()
                    true
                } catch (error: Exception) {
                    Log.e(TAG, "Falha ao manter verificação durante a pausa de sono", error)
                    false
                } finally {
                    executionMutex.unlock()
                }
            }
        }

        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            wakeLockTag,
        ).apply { setReferenceCounted(false) }

        return try {
            wakeLock.acquire(WAKE_LOCK_TIMEOUT_MILLIS)
            Log.d(TAG, "WakeLock parcial adquirido para pulso $source")

            val now = clock.wallTimeMillis()
            preferences.recordPulse(now)
            Log.i(TAG, "Pulso $source registrado em $now")

            if (source == PulseSource.ALARM) {
                scheduler.scheduleNextIfEnabled()
            }

            delay(WAKE_LOCK_HOLD_MILLIS)
            true
        } catch (error: Exception) {
            Log.e(TAG, "Erro durante pulso $source", error)
            false
        } finally {
            if (wakeLock.isHeld) {
                wakeLock.release()
                Log.d(TAG, "WakeLock liberado")
            }
            executionMutex.unlock()
        }
    }

    companion object {
        private const val TAG = "WakePulse"
        private const val WAKE_LOCK_HOLD_MILLIS = 5_000L
        private const val WAKE_LOCK_TIMEOUT_MILLIS = 8_000L
        private val executionMutex = Mutex()
    }
}
