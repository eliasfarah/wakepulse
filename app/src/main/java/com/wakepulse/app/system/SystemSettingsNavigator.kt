package com.wakepulse.app.system

import android.app.AlarmManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.net.toUri

object SystemSettingsNavigator {
    fun openDoNotDisturbAccessSettings(context: Context) {
        startSafely(context, Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
    }

    fun openAppDetails(context: Context) {
        startSafely(
            context,
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                "package:${context.packageName}".toUri(),
            ),
        )
    }

    fun openExactAlarmSettings(context: Context) {
        val packageUri = "package:${context.packageName}".toUri()
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, packageUri)
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri)
        }
        startSafely(context, intent)
    }

    fun openBatteryOptimizationSettings(context: Context) {
        val packageUri = "package:${context.packageName}".toUri()
        val requestIntent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, packageUri)
        try {
            context.startActivity(requestIntent)
        } catch (error: ActivityNotFoundException) {
            Log.w(TAG, "Tela específica de bateria indisponível; abrindo lista", error)
            startSafely(context, Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
        }
    }

    private fun startSafely(context: Context, intent: Intent) {
        try {
            context.startActivity(intent)
        } catch (error: ActivityNotFoundException) {
            Log.e(TAG, "Tela de configurações indisponível", error)
            context.startActivity(
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    "package:${context.packageName}".toUri(),
                ),
            )
        }
    }

    private const val TAG = "WakePulse"
}
