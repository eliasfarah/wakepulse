package com.wakepulse.app.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wakepulse.app.R
import com.wakepulse.app.WakePulseApplication
import com.wakepulse.app.domain.PulseSource
import com.wakepulse.app.domain.PulseState
import com.wakepulse.app.domain.SystemStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class WakePulseViewModel(application: Application) : AndroidViewModel(application) {
    private val container = application as WakePulseApplication

    val pulseState: StateFlow<PulseState> = container.preferences.state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PulseState(),
    )

    private val _systemStatus = MutableStateFlow(container.systemStatusProvider.snapshot())
    val systemStatus: StateFlow<SystemStatus> = _systemStatus.asStateFlow()

    private val _currentTimeMillis = MutableStateFlow(System.currentTimeMillis())
    val currentTimeMillis: StateFlow<Long> = _currentTimeMillis.asStateFlow()

    private val _operationInProgress = MutableStateFlow(false)
    val operationInProgress: StateFlow<Boolean> = _operationInProgress.asStateFlow()

    private val _notice = MutableStateFlow<String?>(null)
    val notice: StateFlow<String?> = _notice.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val restored = container.restoreAfterBoot.restore()
                if (restored) Log.i(TAG, "Agendamento reconciliado ao abrir o app")
            } catch (error: Exception) {
                Log.e(TAG, "Falha ao reconciliar agendamento na abertura", error)
                _notice.value = application.getString(R.string.notice_schedule_unconfirmed)
            }
        }
        viewModelScope.launch {
            while (isActive) {
                _currentTimeMillis.value = System.currentTimeMillis()
                delay(1_000)
            }
        }
        viewModelScope.launch {
            while (isActive) {
                refreshSystemStatus()
                delay(10_000)
            }
        }
    }

    fun setEnabled(enabled: Boolean) = runOperation {
        container.controller.setEnabled(enabled)
        refreshSystemStatus()
    }

    fun setInterval(minutes: Int) = runOperation {
        container.controller.setInterval(minutes)
    }

    fun setPauseDuringDnd(enabled: Boolean) = runOperation {
        container.controller.setPauseDuringDnd(enabled)
        refreshSystemStatus()
    }

    fun executePulseNow() = runOperation {
        val executed = container.pulseExecutor.execute(PulseSource.MANUAL)
        _notice.value = applicationString(
            if (executed) R.string.notice_manual_pulse_complete else R.string.notice_pulse_in_progress,
        )
    }

    fun scheduleAlarmSelfTest() = runOperation {
        val exact = container.systemStatusProvider.snapshot().exactAlarmsAllowed
        val scheduledAsExact = container.diagnosticAlarmScheduler.scheduleOneMinuteTest(exact)
        _notice.value = if (scheduledAsExact) {
            applicationString(R.string.notice_exact_self_test)
        } else {
            applicationString(R.string.notice_inexact_self_test)
        }
    }

    fun refreshSystemStatus() {
        _systemStatus.value = container.systemStatusProvider.snapshot()
    }

    fun clearNotice() {
        _notice.value = null
    }

    private fun runOperation(block: suspend () -> Unit) {
        if (_operationInProgress.value) return
        viewModelScope.launch {
            _operationInProgress.value = true
            try {
                block()
            } catch (error: Exception) {
                Log.e(TAG, "Operação da interface falhou", error)
                val detail = error.localizedMessage ?: applicationString(R.string.unknown_error)
                _notice.value = applicationString(R.string.notice_operation_failed, detail)
            } finally {
                _operationInProgress.value = false
            }
        }
    }

    private fun applicationString(id: Int, vararg arguments: Any): String =
        getApplication<Application>().getString(id, *arguments)

    private companion object {
        const val TAG = "WakePulse"
    }
}
