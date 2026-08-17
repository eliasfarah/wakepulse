package com.wakepulse.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.wakepulse.app.R
import java.text.DateFormat
import java.util.Date
import kotlin.math.max

@Composable
fun formatTime(timestampMillis: Long, empty: String? = null): String {
    val fallback = empty ?: stringResource(R.string.not_yet_occurred)
    if (timestampMillis <= 0L) return fallback
    val locale = LocalConfiguration.current.locales[0]
    return DateFormat.getTimeInstance(DateFormat.MEDIUM, locale).format(Date(timestampMillis))
}

@Composable
fun formatDateTime(timestampMillis: Long, empty: String? = null): String {
    val fallback = empty ?: stringResource(R.string.not_available)
    if (timestampMillis <= 0L) return fallback
    val locale = LocalConfiguration.current.locales[0]
    return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, locale)
        .format(Date(timestampMillis))
}

@Composable
fun formatElapsed(sinceMillis: Long, nowMillis: Long): String {
    if (sinceMillis <= 0L) return stringResource(R.string.no_pulses_recorded)
    val totalSeconds = max(0L, (nowMillis - sinceMillis) / 1_000L)
    val hours = totalSeconds / 3_600
    val minutes = (totalSeconds % 3_600) / 60
    val seconds = totalSeconds % 60
    return when {
        hours > 0 -> stringResource(R.string.elapsed_hours, hours, minutes, seconds)
        minutes > 0 -> stringResource(R.string.elapsed_minutes, minutes, seconds)
        else -> stringResource(R.string.elapsed_seconds, seconds)
    }
}
