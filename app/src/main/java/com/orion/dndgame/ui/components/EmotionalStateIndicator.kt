package com.orion.dndgame.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.orion.dndgame.eds.core.EmotionalState
import kotlin.math.*

/**
 * Visual indicator for NPC emotional states with animated effects
 * Shows primary emotion with pulsing intensity and color coding
 */
@Composable
fun EmotionalStateIndicator(
    emotionalState: EmotionalState,
    intensity: Float,
    size: Dp = 32.dp,
    showPulse: Boolean = true,
    modifier: Modifier = Modifier
) {
    val colorTheme = emotionalState.getColorTheme()
    val primaryColor = Color(colorTheme.primaryColor)
    val secondaryColor = Color(colorTheme.secondaryColor)
    
    // Pulsing animation based on emotional intensity
    val infiniteTransition = rememberInfiniteTransition(label = "emotion_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = intensity.coerceIn(0.5f, 1.0f),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (1000 + (1000 * (1 - intensity))).toInt(),
                easing = EaseInOutSine
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            drawEmotionalOrb(
                emotionalState = emotionalState,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                intensity = if (showPulse) pulseAlpha else intensity,
                size = size.toPx()
            )
        }
    }
}

/**
 * Multi-layer emotional visualization for complex emotional states
 */
@Composable
fun ComplexEmotionalIndicator(
    primaryEmotion: EmotionalState,
    secondaryEmotions: List<Pair<EmotionalState, Float>>,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val center = Offset(size.toPx() / 2, size.toPx() / 2)
            val radius = size.toPx() / 2
            
            // Draw primary emotion as the core
            val primaryTheme = primaryEmotion.getColorTheme()
            drawCircle(
                color = Color(primaryTheme.primaryColor),
                radius = radius * 0.6f,
                center = center
            )
            
            // Draw secondary emotions as rings
            secondaryEmotions.forEachIndexed { index, (emotion, strength) ->
                val ringTheme = emotion.getColorTheme()
                val ringRadius = radius * (0.7f + index * 0.15f)
                val strokeWidth = radius * 0.1f * strength
                
                drawCircle(
                    color = Color(ringTheme.primaryColor).copy(alpha = strength * 0.6f),
                    radius = ringRadius,
                    center = center,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth)
                )
            }
        }
    }
}

/**
 * Emotional state transition animation
 */
@Composable
fun EmotionalTransitionIndicator(
    fromState: EmotionalState,
    toState: EmotionalState,
    progress: Float,
    size: Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    val fromTheme = fromState.getColorTheme()
    val toTheme = toState.getColorTheme()
    
    // Interpolate colors
    val currentColor = lerp(
        Color(fromTheme.primaryColor),
        Color(toTheme.primaryColor),
        progress
    )
    
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        currentColor.copy(alpha = 0.8f),
                        currentColor.copy(alpha = 0.3f),
                        Color.Transparent
                    )
                )
            )
    )
}

/**
 * Emotional intensity bar
 */
@Composable
fun EmotionalIntensityBar(
    intensity: Float,
    emotionalState: EmotionalState,
    modifier: Modifier = Modifier
) {
    val colorTheme = emotionalState.getColorTheme()
    val barColor = Color(colorTheme.primaryColor)
    
    Box(
        modifier = modifier
            .height(8.dp)
            .fillMaxWidth()
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(intensity.coerceIn(0f, 1f))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            barColor.copy(alpha = 0.6f),
                            barColor
                        )
                    )
                )
        )
    }
}

/**
 * Draw an orb representing emotional state
 */
private fun DrawScope.drawEmotionalOrb(
    emotionalState: EmotionalState,
    primaryColor: Color,
    secondaryColor: Color,
    intensity: Float,
    size: Float
) {
    val center = Offset(size / 2, size / 2)
    val radius = size / 2
    
    // Create gradient based on emotional state
    val gradient = when (emotionalState) {
        EmotionalState.WRATHFUL, EmotionalState.BITTER -> {
            Brush.radialGradient(
                colors = listOf(
                    primaryColor.copy(alpha = intensity),
                    primaryColor.copy(alpha = intensity * 0.3f),
                    Color.Transparent
                ),
                center = center,
                radius = radius
            )
        }
        EmotionalState.JOYFUL, EmotionalState.HOPEFUL -> {
            Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.8f),
                    primaryColor.copy(alpha = intensity),
                    secondaryColor.copy(alpha = intensity * 0.5f)
                ),
                center = center,
                radius = radius
            )
        }
        EmotionalState.FEARFUL, EmotionalState.RESIGNED -> {
            Brush.radialGradient(
                colors = listOf(
                    primaryColor.copy(alpha = intensity * 0.6f),
                    primaryColor.copy(alpha = intensity * 0.8f),
                    primaryColor.copy(alpha = intensity * 0.2f)
                ),
                center = center,
                radius = radius
            )
        }
        else -> {
            Brush.radialGradient(
                colors = listOf(
                    primaryColor.copy(alpha = intensity),
                    secondaryColor.copy(alpha = intensity * 0.7f),
                    Color.Transparent
                ),
                center = center,
                radius = radius
            )
        }
    }
    
    // Draw the main orb
    drawCircle(
        brush = gradient,
        radius = radius * 0.8f,
        center = center
    )
    
    // Add special effects based on emotional state
    when (emotionalState) {
        EmotionalState.WRATHFUL -> {
            // Jagged energy effect
            for (i in 0..7) {
                val angle = (i * 45f) * (PI / 180f).toFloat()
                val startRadius = radius * 0.6f
                val endRadius = radius * (0.9f + sin(angle * 4) * 0.1f)
                
                drawLine(
                    color = primaryColor.copy(alpha = intensity * 0.7f),
                    start = center + Offset(
                        cos(angle) * startRadius,
                        sin(angle) * startRadius
                    ),
                    end = center + Offset(
                        cos(angle) * endRadius,
                        sin(angle) * endRadius
                    ),
                    strokeWidth = 2f
                )
            }
        }
        EmotionalState.JOYFUL -> {
            // Sparkle effect
            for (i in 0..11) {
                val angle = (i * 30f) * (PI / 180f).toFloat()
                val sparkleRadius = radius * (0.3f + i % 3 * 0.1f)
                val sparkleCenter = center + Offset(
                    cos(angle) * sparkleRadius,
                    sin(angle) * sparkleRadius
                )
                
                drawCircle(
                    color = Color.White.copy(alpha = intensity * 0.8f),
                    radius = 2f,
                    center = sparkleCenter
                )
            }
        }
        EmotionalState.FEARFUL -> {
            // Trembling effect
            val trembleOffset = sin(System.currentTimeMillis() * 0.01f) * 2f
            drawCircle(
                color = primaryColor.copy(alpha = intensity * 0.3f),
                radius = radius * 0.9f,
                center = center + Offset(trembleOffset, trembleOffset)
            )
        }
        else -> {
            // Default subtle glow
            drawCircle(
                color = primaryColor.copy(alpha = intensity * 0.2f),
                radius = radius,
                center = center
            )
        }
    }
}

/**
 * Helper function to interpolate between colors
 */
private fun lerp(start: Color, stop: Color, fraction: Float): Color {
    return Color(
        red = lerp(start.red, stop.red, fraction),
        green = lerp(start.green, stop.green, fraction),
        blue = lerp(start.blue, stop.blue, fraction),
        alpha = lerp(start.alpha, stop.alpha, fraction)
    )
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + fraction * (stop - start)
}