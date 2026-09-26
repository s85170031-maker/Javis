package com.example.ui.components

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import com.example.service.JarvisBackgroundService
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DeviceTelemetry
import com.example.ui.theme.JarvisAlertRed
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisDeepDark
import com.example.ui.theme.JarvisElectricBlue
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisSuccessGreen
import com.example.ui.theme.JarvisSurfaceDark
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TelemetryScreen(
    telemetry: DeviceTelemetry,
    isApiKeyPresent: Boolean,
    speechPitch: Float,
    speechRate: Float,
    onPitchChange: (Float) -> Unit,
    onRateChange: (Float) -> Unit,
    onTestVoice: () -> Unit,
    onRefreshTelemetry: () -> Unit,
    onTriggerProtocol: (String) -> Unit,
    onApiKeyBadgeClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisDeepDark)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "STARK HARDWARE TELEMETRY",
                    color = JarvisCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "DIAGNOSTIC SUBSYSTEMS",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            IconButton(
                onClick = onRefreshTelemetry,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(JarvisSurfaceElevated)
                    .testTag("refresh_telemetry_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh Telemetry",
                    tint = JarvisCyan,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // --- Core Diagnostics Grid ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Power / Battery Card
            TelemetryMetricCard(
                icon = Icons.Default.BatteryChargingFull,
                title = "ARC CORE POWER",
                value = "${telemetry.batteryPercent}%",
                subtitle = if (telemetry.isCharging) "CHARGING // NOMINAL" else "INTERNAL CELL",
                progress = telemetry.batteryPercent / 100f,
                accentColor = if (telemetry.batteryPercent > 20) JarvisCyan else JarvisAlertRed,
                modifier = Modifier.weight(1f)
            )

            // Memory / RAM Card
            val memPercent = if (telemetry.totalMemoryMb > 0)
                ((telemetry.totalMemoryMb - telemetry.availableMemoryMb).toFloat() / telemetry.totalMemoryMb.toFloat())
            else 0.5f

            TelemetryMetricCard(
                icon = Icons.Default.Memory,
                title = "NEURAL MEMORY",
                value = "${telemetry.availableMemoryMb} MB",
                subtitle = "FREE OF ${telemetry.totalMemoryMb} MB",
                progress = memPercent,
                accentColor = JarvisElectricBlue,
                modifier = Modifier.weight(1f)
            )
        }

        // System Network & Hardware Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = JarvisSurfaceDark),
            border = BorderStroke(1.dp, JarvisCardBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "NETWORK & COMPUTATIONAL PLATFORM",
                    color = JarvisCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(10.dp))

                TelemetryRow(label = "UPLINK STATUS", value = telemetry.networkStatus, icon = Icons.Default.Wifi)
                Spacer(modifier = Modifier.height(6.dp))
                TelemetryRow(label = "DEVICE CHASSIS", value = telemetry.deviceModel, icon = Icons.Default.Info)
                Spacer(modifier = Modifier.height(6.dp))
                TelemetryRow(label = "OPERATING SYSTEM", value = telemetry.osVersion, icon = Icons.Default.Security)
            }
        }

        // AI Core Intelligence Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = JarvisSurfaceDark),
            border = BorderStroke(1.dp, if (isApiKeyPresent) JarvisSuccessGreen.copy(alpha = 0.5f) else JarvisGold.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "COGNITIVE CORE LINK",
                        color = if (isApiKeyPresent) JarvisSuccessGreen else JarvisGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Icon(
                        imageVector = if (isApiKeyPresent) Icons.Default.CheckCircle else Icons.Default.Info,
                        contentDescription = "Core Status",
                        tint = if (isApiKeyPresent) JarvisSuccessGreen else JarvisGold,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isApiKeyPresent)
                        "Linked to Google Gemini 3.5 Flash server-side neural model. Unlimited intelligence with natural speech dialogue."
                    else
                        "Running on Stark Local Protocol Core. For unlimited generative intelligence, configure your GEMINI_API_KEY in the Secrets panel in AI Studio.",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onApiKeyBadgeClicked,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JarvisSurfaceElevated,
                        contentColor = JarvisCyan
                    ),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, JarvisCardBorder),
                    modifier = Modifier.fillMaxWidth().testTag("view_ai_secrets_button")
                ) {
                    Text(
                        text = if (isApiKeyPresent) "GEMINI CONFIGURATION ACTIVE" else "HOW TO CONFIGURE GEMINI KEY",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Voice Synthesizer Tuning Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = JarvisSurfaceDark),
            border = BorderStroke(1.dp, JarvisCardBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Voice Tuning",
                        tint = JarvisCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "VOCAL SYNTHESIZER CALIBRATION",
                        color = JarvisCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Pitch Slider
                Text(
                    text = "Vocal Pitch: ${(speechPitch * 100).toInt()}% (Stark British Tone)",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
                Slider(
                    value = speechPitch,
                    onValueChange = onPitchChange,
                    valueRange = 0.7f..1.3f,
                    colors = SliderDefaults.colors(
                        thumbColor = JarvisCyan,
                        activeTrackColor = JarvisCyan,
                        inactiveTrackColor = JarvisSurfaceElevated
                    ),
                    modifier = Modifier.testTag("pitch_slider")
                )

                // Speed Slider
                Text(
                    text = "Cadence Speed: ${(speechRate * 100).toInt()}% (Crisp Delivery)",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
                Slider(
                    value = speechRate,
                    onValueChange = onRateChange,
                    valueRange = 0.8f..1.4f,
                    colors = SliderDefaults.colors(
                        thumbColor = JarvisCyan,
                        activeTrackColor = JarvisCyan,
                        inactiveTrackColor = JarvisSurfaceElevated
                    ),
                    modifier = Modifier.testTag("speed_slider")
                )

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = onTestVoice,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JarvisCyan,
                        contentColor = JarvisDeepDark
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("test_voice_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = "Test Voice",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TEST J.A.R.V.I.S. VOICE SYNTHESIS",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // System Digital Assistant & Background Service Card
        val context = LocalContext.current
        var isBgServiceRunning by remember { mutableStateOf(JarvisBackgroundService.isRunning) }

        Card(
            modifier = Modifier.fillMaxWidth().testTag("system_assistant_card"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = JarvisSurfaceDark),
            border = BorderStroke(1.dp, JarvisCardBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "System Assistant",
                        tint = JarvisCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "SYSTEM ASSISTANT & BACKGROUND PROTOCOL",
                        color = JarvisCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Operate JARVIS over YouTube, Chrome, and all apps:",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "• Option 1: Engage the Background Protocol below to keep a persistent voice trigger in your notification shade.\n" +
                            "• Option 2: Set JARVIS as your phone's Default Digital Assistant. Then hold the Power Button or swipe up from the bottom corner to speak to JARVIS without leaving YouTube.",
                    color = TextMuted,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Toggle Background Service
                Button(
                    onClick = {
                        if (isBgServiceRunning) {
                            JarvisBackgroundService.stopService(context)
                            isBgServiceRunning = false
                        } else {
                            JarvisBackgroundService.startService(context)
                            isBgServiceRunning = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isBgServiceRunning) JarvisElectricBlue else JarvisCyan,
                        contentColor = JarvisDeepDark
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("toggle_background_service_button")
                ) {
                    Text(
                        text = if (isBgServiceRunning) "🛑 DISENGAGE BACKGROUND PROTOCOL" else "🛡️ ENGAGE BACKGROUND PROTOCOL",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Open Android Default Assistant Settings
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Settings.ACTION_VOICE_INPUT_SETTINGS).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            try {
                                val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                val intent = Intent(Settings.ACTION_SETTINGS).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JarvisSurfaceElevated,
                        contentColor = JarvisGold
                    ),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, JarvisCardBorder),
                    modifier = Modifier.fillMaxWidth().testTag("open_assistant_settings_button")
                ) {
                    Text(
                        text = "⚙️ CONFIGURE AS DEFAULT ASSISTANT",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Protocol Directives Triggers
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = JarvisSurfaceDark),
            border = BorderStroke(1.dp, JarvisCardBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "STARK PROTOCOL OVERRIDES",
                    color = JarvisCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onTriggerProtocol("House Party Protocol") },
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceElevated),
                        border = BorderStroke(1.dp, JarvisCardBorder),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("HOUSE PARTY", color = JarvisGold, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }

                    Button(
                        onClick = { onTriggerProtocol("Mark 85 armor check") },
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceElevated),
                        border = BorderStroke(1.dp, JarvisCardBorder),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("MARK 85 ARMOR", color = JarvisCyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

@Composable
private fun TelemetryMetricCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    subtitle: String,
    progress: Float,
    accentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = JarvisSurfaceDark),
        border = BorderStroke(1.dp, JarvisCardBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                color = TextMuted,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = accentColor,
                trackColor = JarvisSurfaceElevated
            )
        }
    }
}

@Composable
private fun TelemetryRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = TextMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
        Text(
            text = value,
            color = TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace
        )
    }
}
