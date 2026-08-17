package com.wakepulse.app.domain

data class PulseState(
    val enabled: Boolean = false,
    val intervalMinutes: Int = PulseInterval.RECOMMENDED_MINUTES,
    val lastPulseAtMillis: Long = 0L,
    val nextPulseAtMillis: Long = 0L,
    val pulseCount: Long = 0L,
    val activatedAtMillis: Long = 0L,
    val history: List<Long> = emptyList(),
    val pauseDuringDnd: Boolean = true,
)

enum class PulseInterval(val minutes: Int, val label: String) {
    EXPERIMENTAL(5, "5 min"),
    RECOMMENDED(9, "9 min"),
    BALANCED(15, "15 min"),
    ECONOMY(30, "30 min");

    companion object {
        const val RECOMMENDED_MINUTES = 9
        val supportedMinutes = entries.map { it.minutes }.toSet()

        fun sanitize(minutes: Int): Int =
            if (minutes in supportedMinutes) minutes else RECOMMENDED_MINUTES
    }
}

enum class PulseSource {
    ALARM,
    MANUAL,
}

data class SystemStatus(
    val exactAlarmsAllowed: Boolean,
    val ignoringBatteryOptimizations: Boolean,
    val isDeviceIdle: Boolean,
    val isInteractive: Boolean,
    val dndPolicyAccessGranted: Boolean,
    val isDoNotDisturbActive: Boolean,
)
