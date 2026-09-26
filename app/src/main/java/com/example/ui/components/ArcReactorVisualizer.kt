package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.JarvisSystemMode
import com.example.ui.theme.JarvisAlertRed
import com.example.ui.theme.JarvisCoreGlow
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisElectricBlue
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisTeal
import com.example.ui.theme.TextMuted

@Composable
fun ArcReactorVisualizer(
    systemMode: JarvisSystemMode,
    isListening: Boolean,
    isSpeaking: Boolean,
    rmsLevel: Float,
    modifier: Modifier = Modifier
) {
    // Single unified rotation to keep emulator CPU usage ultra-low
    val infiniteTransition = rememberInfiniteTransition(label = "arc_reactor_optimized")

    val rotationDeg by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "core_rotation"
    )

    val stateColor = when (systemMode) {
        JarvisSystemMode.STANDBY -> JarvisCyan
        JarvisSystemMode.LISTENING -> JarvisTeal
        JarvisSystemMode.PROCESSING -> JarvisGold
        JarvisSystemMode.SPEAKING -> JarvisElectricBlue
        JarvisSystemMode.ERROR -> JarvisAlertRed
    }

    val dynamicScale = when {
        isListening -> 1.0f + (rmsLevel * 0.15f)
        isSpeaking -> 1.02f + (rmsLevel * 0.12f)
        systemMode == JarvisSystemMode.PROCESSING -> 1.05f
        else -> 1.0f
    }

    Column(
        modifier = modifier.testTag("arc_reactor_container"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("arc_reactor_canvas")
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val baseRadius = (size.minDimension / 2f) * 0.85f * dynamicScale

                // 1. Soft Ambient Halo (Lightweight flat circles, zero GPU gradient overhead)
                drawCircle(
                    color = stateColor.copy(alpha = 0.10f),
                    center = center,
                    radius = baseRadius * 1.10f
                )
                drawCircle(
                    color = stateColor.copy(alpha = 0.18f),
                    center = center,
                    radius = baseRadius * 0.95f
                )

                // 2. Outer Segmented Ring (Rotates smoothly via rotate())
                rotate(degrees = rotationDeg, pivot = center) {
                    val segments = 12
                    val sweep = 360f / segments
                    for (i in 0 until segments) {
                        drawArc(
                            color = if (i % 2 == 0) stateColor else stateColor.copy(alpha = 0.45f),
                            startAngle = i * sweep + 4f,
                            sweepAngle = sweep - 8f,
                            useCenter = false,
                            topLeft = Offset(center.x - baseRadius * 0.82f, center.y - baseRadius * 0.82f),
                            size = Size(baseRadius * 1.64f, baseRadius * 1.64f),
                            style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                        )
                    }
                }

                // 3. Counter-rotating inner ring
                rotate(degrees = -rotationDeg * 1.5f, pivot = center) {
                    val innerSegments = 6
                    val sweep = 360f / innerSegments
                    for (i in 0 until innerSegments) {
                        drawArc(
                            color = stateColor,
                            startAngle = i * sweep + 6f,
                            sweepAngle = sweep - 12f,
                            useCenter = false,
                            topLeft = Offset(center.x - baseRadius * 0.55f, center.y - baseRadius * 0.55f),
                            size = Size(baseRadius * 1.10f, baseRadius * 1.10f),
                            style = Stroke(width = 4.5f, cap = StrokeCap.Round)
                        )
                    }
                }

                // 4. Inner Core Circle
                drawCircle(
                    color = stateColor.copy(alpha = 0.25f),
                    center = center,
                    radius = baseRadius * 0.38f
                )
                drawCircle(
                    color = Color.White,
                    center = center,
                    radius = baseRadius * 0.18f
                )
                drawCircle(
                    color = stateColor,
                    center = center,
                    radius = baseRadius * 0.28f,
                    style = Stroke(width = 2.5f)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Futuristic System Mode Badge
        Text(
            text = "[ ${systemMode.label} ]",
            color = stateColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp,
            modifier = Modifier.testTag("reactor_status_label")
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = systemMode.subtitle,
            color = TextMuted,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
