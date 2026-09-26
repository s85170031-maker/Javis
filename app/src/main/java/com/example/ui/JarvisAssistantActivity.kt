package com.example.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.data.local.JarvisDatabase
import com.example.data.local.JarvisMessageEntity
import com.example.data.remote.GeminiService
import com.example.model.JarvisSystemMode
import com.example.ui.components.ArcReactorVisualizer
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisDeepDark
import com.example.ui.theme.JarvisElectricBlue
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisSurfaceDark
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.util.JarvisAppLauncher
import com.example.voice.JarvisVoiceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class JarvisAssistantActivity : ComponentActivity() {

    private var voiceManager: JarvisVoiceManager? = null
    private val geminiService = GeminiService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyApplicationTheme {
                AssistantOverlay(
                    onDismiss = { finish() },
                    onInitVoice = { onSpeech, onPartial, onListening, onSpeaking, onRms ->
                        voiceManager = JarvisVoiceManager(
                            context = this@JarvisAssistantActivity,
                            onSpeechResult = onSpeech,
                            onPartialResult = onPartial,
                            onListeningStateChanged = onListening,
                            onSpeakingStateChanged = onSpeaking,
                            onRmsChangedCallback = onRms
                        )
                    },
                    onStartListening = { voiceManager?.startListening() },
                    onStopListening = { voiceManager?.stopListening() },
                    onSpeak = { text -> voiceManager?.speak(text) },
                    onStopSpeaking = { voiceManager?.stopSpeaking() },
                    executeDirective = { query, onComplete ->
                        lifecycleScope.launch {
                            val response = processDirective(query)
                            onComplete(response)
                        }
                    }
                )
            }
        }
    }

    private suspend fun processDirective(query: String): String = withContext(Dispatchers.IO) {
        val appResult = JarvisAppLauncher.handleAppOrWebDirective(this@JarvisAssistantActivity, query)
        if (appResult != null) {
            return@withContext appResult.jarvisResponse
        }

        // Try Gemini if available
        val result = geminiService.generateJarvisResponse(query, emptyList())
        val answer = result.getOrElse {
            "I have acknowledged your directive regarding \"$query\", Sir. System telemetry is active."
        }

        // Save to database
        try {
            val db = JarvisDatabase.getDatabase(this@JarvisAssistantActivity)
            db.jarvisDao().insertMessage(JarvisMessageEntity(sender = "USER", content = query, isVoice = true))
            db.jarvisDao().insertMessage(JarvisMessageEntity(sender = "JARVIS", content = answer, isVoice = true))
        } catch (_: Exception) {}

        answer
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceManager?.destroy()
    }
}

@Composable
fun AssistantOverlay(
    onDismiss: () -> Unit,
    onInitVoice: (
        onSpeech: (String) -> Unit,
        onPartial: (String) -> Unit,
        onListening: (Boolean) -> Unit,
        onSpeaking: (Boolean) -> Unit,
        onRms: (Float) -> Unit
    ) -> Unit,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onSpeak: (String) -> Unit,
    onStopSpeaking: () -> Unit,
    executeDirective: (String, (String) -> Unit) -> Unit
) {
    var recognizedText by remember { mutableStateOf("") }
    var assistantResponse by remember { mutableStateOf("At your service, Sir. Listening...") }
    var isListening by remember { mutableStateOf(false) }
    var isSpeaking by remember { mutableStateOf(false) }
    var rmsLevel by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        onInitVoice(
            { speech ->
                recognizedText = speech
                assistantResponse = "Analyzing directive..."
                executeDirective(speech) { response ->
                    assistantResponse = response
                    onSpeak(response)
                }
            },
            { partial ->
                recognizedText = partial
            },
            { listening -> isListening = listening },
            { speaking -> isSpeaking = speaking },
            { rms -> rmsLevel = rms }
        )
        delay(300)
        onStartListening()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* absorb clicks */ }
                    .testTag("assistant_overlay_card"),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = JarvisDeepDark,
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Bar
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
                                text = "J.A.R.V.I.S. OVERLAY PROTOCOL",
                                color = JarvisCyan,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close overlay",
                                tint = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Arc Reactor Mini Visualizer
                    ArcReactorVisualizer(
                        systemMode = when {
                            isSpeaking -> JarvisSystemMode.SPEAKING
                            isListening -> JarvisSystemMode.LISTENING
                            recognizedText.isNotEmpty() -> JarvisSystemMode.PROCESSING
                            else -> JarvisSystemMode.STANDBY
                        },
                        isListening = isListening,
                        isSpeaking = isSpeaking,
                        rmsLevel = rmsLevel,
                        modifier = Modifier.size(150.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Recognized User Speech
                    if (recognizedText.isNotEmpty()) {
                        Text(
                            text = "\"$recognizedText\"",
                            color = JarvisGold,
                            fontSize = 15.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // JARVIS Response
                    Text(
                        text = assistantResponse,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Bottom Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                if (isListening) onStopListening() else onStartListening()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isListening) JarvisElectricBlue else JarvisCyan
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("overlay_mic_toggle")
                        ) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = null,
                                tint = Color.Black
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isListening) "LISTENING..." else "TAP TO SPEAK",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceElevated),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = "DISMISS",
                                color = TextMuted,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
