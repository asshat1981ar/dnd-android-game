package com.orion.dndgame.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun LoadingPulse(
    size: Dp = 24.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading_pulse")
    
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_phase"
    )
    
    Canvas(
        modifier = modifier.size(size)
    ) {
        drawLoadingPulse(phase, color)
    }
}

private fun DrawScope.drawLoadingPulse(phase: Float, color: Color) {
    val centerX = size.width / 2
    val centerY = size.height / 2
    val maxRadius = size.minDimension / 2
    
    for (i in 0..2) {
        val pulsePhase = (phase + i * 0.3f) % 1f
        val radius = maxRadius * sin(pulsePhase * Math.PI).toFloat()
        val alpha = (1f - pulsePhase) * 0.7f
        
        drawCircle(
            color = color.copy(alpha = alpha),
            radius = radius,
            center = androidx.compose.ui.geometry.Offset(centerX, centerY)
        )
    }
}