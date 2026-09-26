package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisElectricBlue
import kotlin.math.sin

@Composable
fun VoiceWaveVisualizer(
    isListening: Boolean,
    isSpeaking: Boolean,
    rmsLevel: Float,
    modifier: Modifier = Modifier
) {
    val active = isListening || isSpeaking

    // Only compute animation when active to keep CPU idle at zero when standing by
    val phase = if (active) {
        val transition = rememberInfiniteTransition(label = "wave_anim")
        val animatedPhase by transition.animateFloat(
            initialValue = 0f,
            targetValue = (2 * Math.PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "phase"
        )
        animatedPhase
    } else {
        0f
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
    ) {
        val barCount = 18
        val barWidth = size.width / (barCount * 1.8f)
        val spacing = (size.width - (barCount * barWidth)) / (barCount + 1)
        val midY = size.height / 2f
        val baseRms = if (active) (rmsLevel.coerceIn(0.1f, 1.0f)) else 0.05f

        for (i in 0 until barCount) {
            val progress = i / barCount.toFloat()
            val envelope = sin(progress * Math.PI).toFloat()

            val wave = if (active) {
                (sin(phase + (i * 0.45f)) * 0.5f + 0.5f)
            } else {
                0.2f
            }

            val heightMultiplier = if (active) {
                (baseRms * 0.7f + wave * 0.3f) * envelope
            } else {
                0.12f * envelope
            }

            val barHeight = (size.height * heightMultiplier * 0.85f).coerceAtLeast(3f)
            val x = spacing + i * (barWidth + spacing)
            val y = midY - (barHeight / 2f)

            val color = if (active) {
                if (i % 2 == 0) JarvisCyan else JarvisElectricBlue
            } else {
                JarvisCyan.copy(alpha = 0.20f)
            }

            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}
