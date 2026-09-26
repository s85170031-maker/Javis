package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisDeepDark
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisSurfaceDark
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MicrophonePermissionRationaleDialog(
    onGrantClicked: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "🎙️ AUDIO SENSORY PERMISSION",
                color = JarvisCyan,
                fontFamily = FontFamily.Monospace,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        },
        text = {
            Column {
                Text(
                    text = "J.A.R.V.I.S. requires microphone access to process your vocal directives and enable \"Hey Jarvis\" active listening.",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = JarvisSurfaceElevated),
                    border = BorderStroke(1.dp, JarvisCardBorder),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Your audio is processed strictly for voice commands and speech recognition. No recordings are stored or distributed.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onGrantClicked,
                colors = ButtonDefaults.buttonColors(
                    containerColor = JarvisCyan,
                    contentColor = JarvisDeepDark
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("grant_mic_permission_button")
            ) {
                Text("AUTHORIZE MICROPHONE", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("CANCEL", color = TextMuted, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            }
        },
        containerColor = JarvisSurfaceDark,
        tonalElevation = 6.dp,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun ApiKeyInfoDialog(
    isApiKeyConfigured: Boolean,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isApiKeyConfigured) "⚡ GEMINI CLOUD LINK ACTIVE" else "⚡ GEMINI COGNITIVE CORE",
                color = if (isApiKeyConfigured) JarvisCyan else JarvisGold,
                fontFamily = FontFamily.Monospace,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                if (isApiKeyConfigured) {
                    Text(
                        text = "J.A.R.V.I.S. is connected to Google Gemini 3.5 Flash! You have unlimited multi-turn intelligence, complex reasoning, and real-time knowledge retrieval.",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                } else {
                    Text(
                        text = "J.A.R.V.I.S. is currently operating on the Stark Local Core. Voice commands, TTS vocal synthesis, device diagnostics, and Stark protocols are fully operational!",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "To unlock full generative AI intelligence:",
                        color = JarvisGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "1. Open the Secrets panel in AI Studio.\n2. Add the variable: GEMINI_API_KEY\n3. Paste your Google AI Studio API key.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = JarvisCyan,
                    contentColor = JarvisDeepDark
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().testTag("close_api_key_dialog")
            ) {
                Text("ACKNOWLEDGE", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = JarvisSurfaceDark,
        tonalElevation = 6.dp,
        shape = RoundedCornerShape(16.dp)
    )
}
