package com.wakepulse.app.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.BatterySaver
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MonitorHeart
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wakepulse.app.R
import com.wakepulse.app.domain.PulseInterval
import com.wakepulse.app.system.SystemSettingsNavigator
import com.wakepulse.app.ui.WakePulseViewModel
import com.wakepulse.app.ui.formatTime

@Composable
fun HomeScreen(
    viewModel: WakePulseViewModel,
    onOpenDiagnostics: () -> Unit,
) {
    val state by viewModel.pulseState.collectAsStateWithLifecycle()
    val systemStatus by viewModel.systemStatus.collectAsStateWithLifecycle()
    val busy by viewModel.operationInProgress.collectAsStateWithLifecycle()
    val notice by viewModel.notice.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(notice) {
        notice?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearNotice()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { scaffoldPadding ->
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = scaffoldPadding.calculateTopPadding() + 22.dp,
                bottom = scaffoldPadding.calculateBottomPadding() + 32.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.MonitorHeart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(34.dp),
                        )
                        Spacer(Modifier.size(10.dp))
                        Text(
                            text = "WakePulse",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Text(
                        text = stringResource(R.string.app_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth().animateContentSize(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = if (state.enabled) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        },
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.doze_protection),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = when {
                                        state.enabled && state.pauseDuringDnd && systemStatus.isDoNotDisturbActive ->
                                            stringResource(R.string.paused_for_sleep)
                                        state.enabled -> stringResource(R.string.enabled)
                                        else -> stringResource(R.string.disabled)
                                    },
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            if (busy) {
                                CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 3.dp)
                                Spacer(Modifier.size(14.dp))
                            }
                            Switch(
                                checked = state.enabled,
                                onCheckedChange = viewModel::setEnabled,
                                enabled = !busy,
                                modifier = Modifier.scale(1.12f),
                            )
                        }
                        Text(
                            text = when {
                                state.enabled && state.pauseDuringDnd && systemStatus.isDoNotDisturbActive ->
                                    stringResource(R.string.sleep_pause_message)
                                state.enabled ->
                                    stringResource(R.string.enabled_message)
                                else ->
                                    stringResource(R.string.disabled_message)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            item {
                SectionTitle(stringResource(R.string.status))
                Spacer(Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(13.dp),
                    ) {
                        StatusLine(
                            stringResource(R.string.last_pulse),
                            formatTime(state.lastPulseAtMillis),
                        )
                        StatusLine(
                            stringResource(R.string.next_pulse),
                            when {
                                !state.enabled -> stringResource(R.string.disabled)
                                state.pauseDuringDnd && systemStatus.isDoNotDisturbActive ->
                                    stringResource(
                                        R.string.paused_check_time,
                                        formatTime(
                                            state.nextPulseAtMillis,
                                            stringResource(R.string.waiting),
                                        ),
                                    )
                                else -> stringResource(
                                    R.string.approximate_time,
                                    formatTime(
                                        state.nextPulseAtMillis,
                                        stringResource(R.string.waiting),
                                    ),
                                )
                            },
                        )
                        StatusLine(
                            stringResource(R.string.interval),
                            pluralStringResource(
                                R.plurals.minutes_value,
                                state.intervalMinutes,
                                state.intervalMinutes,
                            ),
                        )
                        StatusLine(stringResource(R.string.total_pulses), state.pulseCount.toString())
                        StatusLine(
                            stringResource(R.string.exact_alarms),
                            if (systemStatus.exactAlarmsAllowed) {
                                stringResource(R.string.allowed)
                            } else {
                                stringResource(R.string.permission_required)
                            },
                            systemStatus.exactAlarmsAllowed,
                        )
                        StatusLine(
                            stringResource(R.string.battery_optimization),
                            if (systemStatus.ignoringBatteryOptimizations) {
                                stringResource(R.string.ignored)
                            } else {
                                stringResource(R.string.active)
                            },
                            systemStatus.ignoringBatteryOptimizations,
                        )
                        StatusLine(
                            stringResource(R.string.sleep_mode_dnd),
                            if (systemStatus.isDoNotDisturbActive) {
                                stringResource(R.string.active)
                            } else {
                                stringResource(R.string.inactive)
                            },
                            !systemStatus.isDoNotDisturbActive,
                        )
                    }
                }
            }

            item {
                SectionTitle(
                    stringResource(R.string.sleep_pause),
                    stringResource(R.string.sleep_pause_subtitle),
                )
                Spacer(Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(13.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Rounded.Bedtime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(Modifier.size(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    stringResource(R.string.pause_on_dnd),
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    if (systemStatus.isDoNotDisturbActive) {
                                        stringResource(R.string.pause_active_now)
                                    } else {
                                        stringResource(R.string.waiting_for_sleep)
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Switch(
                                checked = state.pauseDuringDnd,
                                onCheckedChange = viewModel::setPauseDuringDnd,
                                enabled = !busy,
                            )
                        }
                        StatusLine(
                            stringResource(R.string.dnd_access),
                            if (systemStatus.dndPolicyAccessGranted) {
                                stringResource(R.string.granted)
                            } else {
                                stringResource(R.string.permission_required)
                            },
                            systemStatus.dndPolicyAccessGranted,
                        )
                        Text(
                            stringResource(R.string.sleep_pause_details),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (state.pauseDuringDnd && !systemStatus.dndPolicyAccessGranted) {
                            Button(
                                onClick = { SystemSettingsNavigator.openDoNotDisturbAccessSettings(context) },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(stringResource(R.string.allow_dnd_access))
                            }
                        }
                    }
                }
            }

            item {
                SectionTitle(
                    stringResource(R.string.pulse_interval),
                    stringResource(R.string.shorter_intervals_battery),
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                ) {
                    PulseInterval.entries.forEach { interval ->
                        FilterChip(
                            selected = state.intervalMinutes == interval.minutes,
                            onClick = { viewModel.setInterval(interval.minutes) },
                            enabled = !busy,
                            label = {
                                Text(
                                    when (interval) {
                                        PulseInterval.RECOMMENDED ->
                                            stringResource(R.string.recommended_interval)
                                        PulseInterval.EXPERIMENTAL ->
                                            stringResource(R.string.experimental_interval)
                                        else -> interval.label
                                    },
                                )
                            },
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Rounded.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        stringResource(R.string.allow_while_idle_warning),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            item {
                SectionTitle(stringResource(R.string.permissions_configuration))
                Spacer(Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(13.dp),
                    ) {
                        StatusLine(
                            stringResource(R.string.exact_alarm),
                            if (systemStatus.exactAlarmsAllowed) {
                                stringResource(R.string.exact_alarms_allowed)
                            } else {
                                stringResource(R.string.permission_required)
                            },
                            systemStatus.exactAlarmsAllowed,
                        )
                        StatusLine("WakeLock", stringResource(R.string.configured), true)
                        StatusLine("Boot receiver", stringResource(R.string.configured), true)
                        if (!systemStatus.exactAlarmsAllowed) {
                            Button(
                                onClick = { SystemSettingsNavigator.openExactAlarmSettings(context) },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Icon(Icons.Rounded.Alarm, contentDescription = null)
                                Spacer(Modifier.size(8.dp))
                                Text(stringResource(R.string.allow_exact_alarms))
                            }
                        }
                    }
                }
            }

            item {
                SectionTitle(stringResource(R.string.battery_optimization))
                Spacer(Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(13.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.BatterySaver, contentDescription = null)
                            Spacer(Modifier.size(10.dp))
                            Text(
                                if (systemStatus.ignoringBatteryOptimizations) {
                                    stringResource(R.string.outside_optimization_message)
                                } else {
                                    stringResource(R.string.optimization_active_message)
                                },
                                fontWeight = FontWeight.Medium,
                            )
                        }
                        Text(
                            stringResource(R.string.battery_optimization_details),
                            style = MaterialTheme.typography.bodySmall,
                        )
                        if (!systemStatus.ignoringBatteryOptimizations) {
                            OutlinedButton(
                                onClick = { SystemSettingsNavigator.openBatteryOptimizationSettings(context) },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(stringResource(R.string.open_battery_settings))
                            }
                        }
                    }
                }
            }

            item {
                SectionTitle(stringResource(R.string.samsung_one_ui))
                Spacer(Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            stringResource(R.string.samsung_deep_sleep_warning),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        OutlinedButton(
                            onClick = { SystemSettingsNavigator.openAppDetails(context) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(stringResource(R.string.open_wakepulse_details))
                        }
                    }
                }
            }

            item {
                HorizontalDivider()
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onOpenDiagnostics,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                ) {
                    Icon(Icons.Rounded.Bolt, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text(
                        stringResource(R.string.open_diagnostics),
                        modifier = Modifier.weight(1f),
                    )
                    Icon(Icons.Rounded.ChevronRight, contentDescription = null)
                }
                Spacer(Modifier.height(10.dp))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.privacy_footer),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray,
                    )
                }
            }
        }
    }
}
