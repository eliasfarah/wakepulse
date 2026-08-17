package com.wakepulse.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wakepulse.app.R
import com.wakepulse.app.ui.WakePulseViewModel
import com.wakepulse.app.ui.formatDateTime
import com.wakepulse.app.ui.formatElapsed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(
    viewModel: WakePulseViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.pulseState.collectAsStateWithLifecycle()
    val systemStatus by viewModel.systemStatus.collectAsStateWithLifecycle()
    val now by viewModel.currentTimeMillis.collectAsStateWithLifecycle()
    val busy by viewModel.operationInProgress.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.diagnostics)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::refreshSystemStatus) {
                        Icon(
                            Icons.Rounded.Refresh,
                            contentDescription = stringResource(R.string.refresh),
                        )
                    }
                },
            )
        },
    ) { scaffoldPadding ->
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = scaffoldPadding.calculateTopPadding() + 10.dp,
                bottom = scaffoldPadding.calculateBottomPadding() + 32.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.real_time_status),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(R.string.local_readings_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        StatusLine(
                            stringResource(R.string.screen),
                            if (systemStatus.isInteractive) {
                                stringResource(R.string.interactive)
                            } else {
                                stringResource(R.string.screen_off)
                            },
                            systemStatus.isInteractive,
                        )
                        StatusLine(
                            stringResource(R.string.idle_mode),
                            if (systemStatus.isDeviceIdle) {
                                stringResource(R.string.active)
                            } else {
                                stringResource(R.string.inactive)
                            },
                            !systemStatus.isDeviceIdle,
                        )
                        StatusLine("isDeviceIdleMode()", systemStatus.isDeviceIdle.toString())
                        StatusLine("isInteractive()", systemStatus.isInteractive.toString())
                        HorizontalDivider()
                        StatusLine(
                            stringResource(R.string.time_since_last_pulse),
                            formatElapsed(state.lastPulseAtMillis, now),
                        )
                        StatusLine(stringResource(R.string.pulse_counter), state.pulseCount.toString())
                        StatusLine(
                            stringResource(R.string.next_schedule),
                            formatDateTime(state.nextPulseAtMillis),
                        )
                        StatusLine(
                            "canScheduleExactAlarms()",
                            systemStatus.exactAlarmsAllowed.toString(),
                            systemStatus.exactAlarmsAllowed,
                        )
                        StatusLine(
                            stringResource(R.string.outside_battery_optimization),
                            systemStatus.ignoringBatteryOptimizations.toString(),
                            systemStatus.ignoringBatteryOptimizations,
                        )
                        StatusLine(
                            stringResource(R.string.dnd_active),
                            systemStatus.isDoNotDisturbActive.toString(),
                            !systemStatus.isDoNotDisturbActive,
                        )
                        StatusLine(
                            stringResource(R.string.dnd_policy_access),
                            systemStatus.dndPolicyAccessGranted.toString(),
                            systemStatus.dndPolicyAccessGranted,
                        )
                        StatusLine(
                            stringResource(R.string.pause_during_dnd),
                            state.pauseDuringDnd.toString(),
                            state.pauseDuringDnd,
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = viewModel::executePulseNow,
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(17.dp),
                ) {
                    if (busy) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Icon(Icons.Rounded.Bolt, contentDescription = null)
                    }
                    Spacer(Modifier.size(9.dp))
                    Text(
                        if (busy) stringResource(R.string.running)
                        else stringResource(R.string.run_pulse_now),
                    )
                }
                Spacer(Modifier.height(7.dp))
                Text(
                    text = stringResource(R.string.manual_pulse_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(14.dp))
                OutlinedButton(
                    onClick = viewModel::scheduleAlarmSelfTest,
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(15.dp),
                ) {
                    Text(stringResource(R.string.test_alarm_manager))
                }
                Spacer(Modifier.height(7.dp))
                Text(
                    text = stringResource(R.string.alarm_self_test_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.size(9.dp))
                    SectionTitle(
                        stringResource(R.string.history),
                        pluralStringResource(
                            R.plurals.history_summary,
                            state.history.size,
                            state.history.size,
                        ),
                    )
                }
            }

            if (state.history.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                    ) {
                        Text(
                            stringResource(R.string.no_pulses_yet),
                            modifier = Modifier.padding(20.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                items(state.history.size, key = { state.history[it] }) { index ->
                    val timestamp = state.history[index]
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("#${state.pulseCount - index}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(formatDateTime(timestamp), fontWeight = FontWeight.Medium)
                    }
                    if (index < state.history.lastIndex) HorizontalDivider()
                }
            }
        }
    }
}
