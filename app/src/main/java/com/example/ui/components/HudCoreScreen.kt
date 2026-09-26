package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.JarvisSystemMode
import com.example.model.JarvisUiState
import com.example.ui.theme.JarvisAlertRed
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisDeepDark
import com.example.ui.theme.JarvisElectricBlue
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisSuccessGreen
import com.example.ui.theme.JarvisSurfaceDark
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisTeal
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HudCoreScreen(
    uiState: JarvisUiState,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onToggleHeyJarvis: () -> Unit,
    onToggleMute: () -> Unit,
    onReplayAudio: () -> Unit,
    onStopSpeaking: () -> Unit,
    onCommandSelected: (String) -> Unit,
    onApiKeyBadgeClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisDeepDark)
            .verticalScroll(scrollState)
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Top Tactical Header ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "STARK IND. // JARVIS OS",
                    color = JarvisCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "DEFENSE PROTOCOL ACTIVE",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Cloud / Local Status Chip
                Surface(
                    onClick = onApiKeyBadgeClicked,
                    shape = RoundedCornerShape(12.dp),
                    color = if (uiState.isApiKeyPresent) JarvisSurfaceElevated else JarvisSurfaceElevated,
                    border = BorderStroke(1.dp, if (uiState.isApiKeyPresent) JarvisSuccessGreen.copy(alpha = 0.6f) else JarvisGold.copy(alpha = 0.6f)),
                    modifier = Modifier.testTag("ai_status_chip")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (uiState.isApiKeyPresent) Icons.Default.Cloud else Icons.Default.CloudOff,
                            contentDescription = "AI Mode",
                            tint = if (uiState.isApiKeyPresent) JarvisSuccessGreen else JarvisGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (uiState.isApiKeyPresent) "GEMINI LINKED" else "LOCAL CORE",
                            color = if (uiState.isApiKeyPresent) JarvisSuccessGreen else JarvisGold,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Mute / Unmute Button
                IconButton(
                    onClick = onToggleMute,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(JarvisSurfaceElevated)
                        .border(1.dp, JarvisCardBorder, CircleShape)
                        .testTag("mute_toggle_button")
                ) {
                    Icon(
                        imageVector = if (uiState.isMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = if (uiState.isMuted) "Unmute JARVIS" else "Mute JARVIS",
                        tint = if (uiState.isMuted) JarvisAlertRed else JarvisCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- Central Holographic Arc Reactor ---
        ArcReactorVisualizer(
            systemMode = uiState.systemMode,
            isListening = uiState.isListening,
            isSpeaking = uiState.isSpeaking,
            rmsLevel = uiState.rmsLevel,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        // --- Audio Spectrum Equalizer Wave ---
        VoiceWaveVisualizer(
            isListening = uiState.isListening,
            isSpeaking = uiState.isSpeaking,
            rmsLevel = uiState.rmsLevel,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
        )

        // --- Live Voice Transcription Bubble (if listening or active speech) ---
        AnimatedVisibility(
            visible = uiState.liveTranscript.isNotBlank() || uiState.isListening,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .testTag("live_transcript_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = JarvisSurfaceDark),
                border = BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "VOICE INPUT STREAM",
                            color = JarvisCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (uiState.isListening) JarvisAlertRed else JarvisCyan)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (uiState.liveTranscript.isNotBlank()) "\"${uiState.liveTranscript}\"" else "Listening for your voice...",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Default,
                        fontStyle = if (uiState.liveTranscript.isBlank()) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal
                    )
                }
            }
        }

        // --- Latest JARVIS Response Card ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .testTag("latest_response_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = JarvisSurfaceDark),
            border = BorderStroke(1.dp, JarvisCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(JarvisCyan)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "J.A.R.V.I.S. VOCAL RESPONSE",
                            color = JarvisCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Row {
                        if (uiState.isSpeaking) {
                            IconButton(
                                onClick = onStopSpeaking,
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("stop_speech_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "Stop Speech",
                                    tint = JarvisAlertRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else {
                            IconButton(
                                onClick = onReplayAudio,
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("replay_speech_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Replay Speech",
                                    tint = JarvisTeal,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = uiState.lastResponse,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontFamily = FontFamily.Default
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- Primary Voice Action Button ---
        Box(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .size(80.dp),
            contentAlignment = Alignment.Center
        ) {
            // Active listening halo ring
            if (uiState.isListening) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(JarvisCyan.copy(alpha = 0.20f))
                )
            }

            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = if (uiState.isListening)
                                listOf(JarvisCyan, JarvisTeal, JarvisSurfaceElevated)
                            else
                                listOf(JarvisSurfaceElevated, JarvisSurfaceDark)
                        )
                    )
                    .border(
                        2.dp,
                        if (uiState.isListening) JarvisCyan else JarvisCardBorder,
                        CircleShape
                    )
                    .clickable {
                        if (uiState.isListening) onStopListening() else onStartListening()
                    }
                    .testTag("voice_action_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (uiState.isListening) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = if (uiState.isListening) "Stop Listening" else "Speak to JARVIS",
                    tint = if (uiState.isListening) JarvisDeepDark else JarvisCyan,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Text(
            text = if (uiState.isListening) "Tap to finish directive" else "Tap to speak to J.A.R.V.I.S.",
            color = if (uiState.isListening) JarvisCyan else TextSecondary,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- "Hey Jarvis" Continuous Listening Mode Card ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .testTag("hey_jarvis_mode_card"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = JarvisSurfaceDark),
            border = BorderStroke(1.dp, if (uiState.continuousHeyJarvis) JarvisCyan.copy(alpha = 0.6f) else JarvisCardBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🗣️ \"Hey Jarvis\" Listening Mode",
                        color = if (uiState.continuousHeyJarvis) JarvisCyan else TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (uiState.continuousHeyJarvis) "Microphone stays active for continuous directives" else "Enable hands-free continuous listening",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                Switch(
                    checked = uiState.continuousHeyJarvis,
                    onCheckedChange = { onToggleHeyJarvis() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = JarvisDeepDark,
                        checkedTrackColor = JarvisCyan,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = JarvisSurfaceElevated
                    ),
                    modifier = Modifier.testTag("hey_jarvis_switch")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- Quick Directives Chips ---
        Text(
            text = "QUICK TACTICAL DIRECTIVES",
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            textAlign = TextAlign.Start
        )

        QuickCommandChips(
            onCommandSelected = onCommandSelected,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
