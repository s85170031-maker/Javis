package com.example.model

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build

data class DeviceTelemetry(
    val batteryPercent: Int = 100,
    val isCharging: Boolean = false,
    val availableMemoryMb: Long = 0,
    val totalMemoryMb: Long = 0,
    val networkStatus: String = "ONLINE",
    val deviceModel: String = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}",
    val osVersion: String = "Android ${Build.VERSION.RELEASE}",
    val arcReactorPower: String = "OPTIMAL",
    val protocolState: String = "DEFENSE STANDBY"
) {
    companion object {
        fun capture(context: Context): DeviceTelemetry {
            // Battery Telemetry
            var battery = 100
            var charging = false
            try {
                val batteryStatus = context.registerReceiver(
                    null,
                    IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                )
                if (batteryStatus != null) {
                    val level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    if (level != -1 && scale != -1) {
                        battery = (level * 100 / scale.toFloat()).toInt()
                    }
                    val status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL
                }
            } catch (_: Exception) {}

            // Memory Telemetry
            var availMem = 0L
            var totalMem = 0L
            try {
                val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
                val memoryInfo = ActivityManager.MemoryInfo()
                actManager?.getMemoryInfo(memoryInfo)
                availMem = memoryInfo.availMem / (1024 * 1024)
                totalMem = memoryInfo.totalMem / (1024 * 1024)
            } catch (_: Exception) {}

            // Network Telemetry
            var net = "OFFLINE"
            try {
                val connManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                val activeNetwork = connManager?.activeNetwork
                val caps = connManager?.getNetworkCapabilities(activeNetwork)
                if (caps != null) {
                    net = when {
                        caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WIFI (LINKED)"
                        caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "CELLULAR 5G"
                        caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ETHERNET"
                        else -> "ONLINE"
                    }
                }
            } catch (_: Exception) {}

            return DeviceTelemetry(
                batteryPercent = battery,
                isCharging = charging,
                availableMemoryMb = availMem,
                totalMemoryMb = totalMem,
                networkStatus = net
            )
        }
    }
}
