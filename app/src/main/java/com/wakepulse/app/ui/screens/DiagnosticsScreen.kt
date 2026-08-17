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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
                title = { Text("Diagnóstico") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::refreshSystemStatus) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Atualizar")
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
                    text = "Estado em tempo real",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Leituras locais das APIs do Android. Nenhum dado sai do aparelho.",
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
                            "Tela",
                            if (systemStatus.isInteractive) "Interativa" else "Desligada / não interativa",
                            systemStatus.isInteractive,
                        )
                        StatusLine(
                            "Modo idle (Doze)",
                            if (systemStatus.isDeviceIdle) "Ativo" else "Inativo",
                            !systemStatus.isDeviceIdle,
                        )
                        StatusLine("isDeviceIdleMode()", systemStatus.isDeviceIdle.toString())
                        StatusLine("isInteractive()", systemStatus.isInteractive.toString())
                        HorizontalDivider()
                        StatusLine("Tempo desde o último pulso", formatElapsed(state.lastPulseAtMillis, now))
                        StatusLine("Contador de pulsos", state.pulseCount.toString())
                        StatusLine("Próximo agendamento", formatDateTime(state.nextPulseAtMillis))
                        StatusLine(
                            "canScheduleExactAlarms()",
                            systemStatus.exactAlarmsAllowed.toString(),
                            systemStatus.exactAlarmsAllowed,
                        )
                        StatusLine(
                            "Fora da otimização de bateria",
                            systemStatus.ignoringBatteryOptimizations.toString(),
                            systemStatus.ignoringBatteryOptimizations,
                        )
                        StatusLine(
                            "Não Perturbe ativo",
                            systemStatus.isDoNotDisturbActive.toString(),
                            !systemStatus.isDoNotDisturbActive,
                        )
                        StatusLine(
                            "Acesso à política DND",
                            systemStatus.dndPolicyAccessGranted.toString(),
                            systemStatus.dndPolicyAccessGranted,
                        )
                        StatusLine(
                            "Pausa durante DND",
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
                    Text(if (busy) "Executando…" else "Executar pulso agora")
                }
                Spacer(Modifier.height(7.dp))
                Text(
                    text = "O teste mantém um WakeLock parcial por cerca de 5 segundos e registra o resultado, sem alterar o próximo alarme.",
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
                    Text("Testar AlarmManager em ~60 segundos")
                }
                Spacer(Modifier.height(7.dp))
                Text(
                    text = "Este autoteste agenda um PendingIntent separado. Desligue a tela; quando ele disparar, o contador e o histórico serão atualizados e o log mostrará “AUTOTESTE disparado”.",
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
                    SectionTitle("Histórico", "Últimos ${state.history.size} de até 50 pulsos")
                }
            }

            if (state.history.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                    ) {
                        Text(
                            "Nenhum pulso registrado ainda.",
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
