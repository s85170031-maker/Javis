package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
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
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.TextPrimary

@Composable
fun QuickCommandChips(
    onCommandSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val commands = listOf(
        "Open Chrome",
        "Open YouTube",
        "Search YouTube for AC/DC",
        "Search Google for news",
        "Open Camera",
        "Open Phone",
        "Open Settings",
        "Open WhatsApp",
        "Open Maps",
        "Open Calculator",
        "System Status",
        "Who are you?",
        "What can you do?",
        "Atmospheric scan",
        "Tell me a joke",
        "Mark 85 armor check",
        "Current time",
        "House Party Protocol",
        "Device battery level"
    )

    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("quick_command_chips"),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        commands.forEach { cmd ->
            Surface(
                onClick = { onCommandSelected(cmd) },
                shape = RoundedCornerShape(16.dp),
                color = JarvisSurfaceElevated,
                border = BorderStroke(1.dp, JarvisCardBorder),
                modifier = Modifier.testTag("chip_${cmd.lowercase().replace(" ", "_")}")
            ) {
                Text(
                    text = cmd,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}
