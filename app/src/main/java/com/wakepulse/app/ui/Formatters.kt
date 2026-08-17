package com.wakepulse.app.ui

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.max

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")

fun formatTime(timestampMillis: Long, empty: String = "Ainda não ocorreu"): String =
    if (timestampMillis <= 0L) empty
    else Instant.ofEpochMilli(timestampMillis).atZone(ZoneId.systemDefault()).format(timeFormatter)

fun formatDateTime(timestampMillis: Long, empty: String = "Não disponível"): String =
    if (timestampMillis <= 0L) empty
    else Instant.ofEpochMilli(timestampMillis).atZone(ZoneId.systemDefault()).format(dateTimeFormatter)

fun formatElapsed(sinceMillis: Long, nowMillis: Long): String {
    if (sinceMillis <= 0L) return "Sem pulsos registrados"
    val totalSeconds = max(0L, (nowMillis - sinceMillis) / 1_000L)
    val hours = totalSeconds / 3_600
    val minutes = (totalSeconds % 3_600) / 60
    val seconds = totalSeconds % 60
    return when {
        hours > 0 -> "${hours}h ${minutes}min ${seconds}s"
        minutes > 0 -> "${minutes}min ${seconds}s"
        else -> "${seconds}s"
    }
}
