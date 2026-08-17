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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
                        text = "Reduza notificações atrasadas durante o repouso do Android",
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
                                    text = "Proteção contra Doze",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = when {
                                        state.enabled && state.pauseDuringDnd && systemStatus.isDoNotDisturbActive -> "Pausado para dormir"
                                        state.enabled -> "Ativado"
                                        else -> "Desativado"
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
                                    "Não Perturbe/Modo Sono está ativo. Os WakeLocks estão pausados."
                                state.enabled ->
                                    "WakePulse está mantendo pulsos periódicos durante o repouso."
                                else ->
                                    "O Android pode entrar normalmente em Doze profundo."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            item {
                SectionTitle("Status")
                Spacer(Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(13.dp),
                    ) {
                        StatusLine("Último pulso", formatTime(state.lastPulseAtMillis))
                        StatusLine(
                            "Próximo pulso",
                            when {
                                !state.enabled -> "Desativado"
                                state.pauseDuringDnd && systemStatus.isDoNotDisturbActive ->
                                    "Pausado · checagem ~${formatTime(state.nextPulseAtMillis, "aguardando")}"
                                else -> "~${formatTime(state.nextPulseAtMillis, "aguardando")}"
                            },
                        )
                        StatusLine("Intervalo", "${state.intervalMinutes} minutos")
                        StatusLine("Total de pulsos", state.pulseCount.toString())
                        StatusLine(
                            "Alarmes exatos",
                            if (systemStatus.exactAlarmsAllowed) "Permitidos" else "Permissão necessária",
                            systemStatus.exactAlarmsAllowed,
                        )
                        StatusLine(
                            "Otimização de bateria",
                            if (systemStatus.ignoringBatteryOptimizations) "Ignorada" else "Ativa",
                            systemStatus.ignoringBatteryOptimizations,
                        )
                        StatusLine(
                            "Modo Sono / Não Perturbe",
                            if (systemStatus.isDoNotDisturbActive) "Ativo" else "Inativo",
                            !systemStatus.isDoNotDisturbActive,
                        )
                    }
                }
            }

            item {
                SectionTitle(
                    "Pausa durante o sono",
                    "Usa o Não Perturbe do Android, inclusive quando ativado pelo Modo Sono da Samsung.",
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
                                Text("Pausar no Não Perturbe", fontWeight = FontWeight.SemiBold)
                                Text(
                                    if (systemStatus.isDoNotDisturbActive) "Pausa ativa agora" else "Aguardando o horário de sono",
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
                            "Acesso ao Não Perturbe",
                            if (systemStatus.dndPolicyAccessGranted) "Concedido" else "Permissão necessária",
                            systemStatus.dndPolicyAccessGranted,
                        )
                        Text(
                            "Durante a pausa, o alarme faz apenas uma checagem curta no intervalo configurado e não adquire WakeLock nem registra pulso. Ao sair do Não Perturbe, o agendamento normal é retomado.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (state.pauseDuringDnd && !systemStatus.dndPolicyAccessGranted) {
                            Button(
                                onClick = { SystemSettingsNavigator.openDoNotDisturbAccessSettings(context) },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("Permitir acesso ao Não Perturbe")
                            }
                        }
                    }
                }
            }

            item {
                SectionTitle(
                    "Intervalo do pulso",
                    "Intervalos menores podem consumir mais bateria.",
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
                                        PulseInterval.RECOMMENDED -> "9 min · recomendado"
                                        PulseInterval.EXPERIMENTAL -> "5 min · experimental"
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
                        "O Android pode limitar alarmes allowWhileIdle durante o Doze, especialmente em 5 minutos. O horário exibido é aproximado.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            item {
                SectionTitle("Permissões e configuração")
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
                            "Exact Alarm",
                            if (systemStatus.exactAlarmsAllowed) "Alarmes exatos permitidos" else "Permissão necessária",
                            systemStatus.exactAlarmsAllowed,
                        )
                        StatusLine("WakeLock", "Configurado", true)
                        StatusLine("Boot receiver", "Configurado", true)
                        if (!systemStatus.exactAlarmsAllowed) {
                            Button(
                                onClick = { SystemSettingsNavigator.openExactAlarmSettings(context) },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Icon(Icons.Rounded.Alarm, contentDescription = null)
                                Spacer(Modifier.size(8.dp))
                                Text("Permitir alarmes exatos")
                            }
                        }
                    }
                }
            }

            item {
                SectionTitle("Otimização de bateria")
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
                                    "WakePulse está fora da otimização de bateria."
                                } else {
                                    "A otimização de bateria ainda está ativa."
                                },
                                fontWeight = FontWeight.Medium,
                            )
                        }
                        Text(
                            "Remover o WakePulse da otimização pode aumentar a confiabilidade, mas também o consumo. A decisão sempre é confirmada pelo usuário nas configurações do Android.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        if (!systemStatus.ignoringBatteryOptimizations) {
                            OutlinedButton(
                                onClick = { SystemSettingsNavigator.openBatteryOptimizationSettings(context) },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("Abrir configurações de bateria")
                            }
                        }
                    }
                }
            }

            item {
                SectionTitle("Samsung / One UI")
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
                            "No One UI, confirme também que o WakePulse não foi colocado em Apps em suspensão profunda. A Samsung pode manter uma camada adicional além da otimização padrão do Android.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        OutlinedButton(
                            onClick = { SystemSettingsNavigator.openAppDetails(context) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Abrir detalhes do WakePulse")
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
                    Text("Abrir diagnóstico", modifier = Modifier.weight(1f))
                    Icon(Icons.Rounded.ChevronRight, contentDescription = null)
                }
                Spacer(Modifier.height(10.dp))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        "Offline · sem analytics · sem leitura de notificações",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray,
                    )
                }
            }
        }
    }
}
