package com.orion.dndgame.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orion.dndgame.eds.core.EmotionalState
import com.orion.dndgame.eds.enhanced.EnhancedEmotionalDialogueSystem
import kotlinx.coroutines.delay
import kotlin.math.*

/**
 * Real-time visual feedback component for NPC emotional states
 */
@Composable
fun RealTimeEmotionalFeedback(
    currentEmotion: EmotionalState,
    emotionalIntensity: Double,
    isTransitioning: Boolean,
    previousEmotion: EmotionalState? = null,
    modifier: Modifier = Modifier
) {
    var animationTrigger by remember { mutableStateOf(0) }
    
    // Trigger animation when emotion changes
    LaunchedEffect(currentEmotion) {
        animationTrigger++
    }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Color.Black.copy(alpha = 0.1f),
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Emotional State Visualization
        EmotionalVisualization(
            emotion = currentEmotion,
            intensity = emotionalIntensity,
            isTransitioning = isTransitioning,
            animationTrigger = animationTrigger
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Emotion Label with Transition
        AnimatedContent(
            targetState = currentEmotion,
            transitionSpec = {
                slideInVertically { it } + fadeIn() with
                slideOutVertically { -it } + fadeOut()
            }
        ) { emotion ->
            Text(
                text = emotion.displayName,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = getEmotionColor(emotion)
                ),
                textAlign = TextAlign.Center
            )
        }
        
        // Intensity Indicator
        IntensityIndicator(
            intensity = emotionalIntensity,
            emotion = currentEmotion
        )
        
        // Transition Indicator
        if (isTransitioning && previousEmotion != null) {
            Spacer(modifier = Modifier.height(8.dp))
            TransitionIndicator(
                fromEmotion = previousEmotion,
                toEmotion = currentEmotion
            )
        }
    }
}

@Composable
private fun EmotionalVisualization(
    emotion: EmotionalState,
    intensity: Double,
    isTransitioning: Boolean,
    animationTrigger: Int
) {
    val animationSpec = remember {
        infiniteRepeatable<Float>(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    }
    
    val pulseAnimation by animateFloatAsState(
        targetValue = if (isTransitioning) 1.2f else 1f,
        animationSpec = tween(500, easing = FastOutSlowInEasing)
    )
    
    val infiniteTransition = rememberInfiniteTransition()
    val rotationAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = animationSpec
    )
    
    Box(
        modifier = Modifier
            .size(120.dp)
            .scale(pulseAnimation),
        contentAlignment = Alignment.Center
    ) {
        // Background aura
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            drawEmotionalAura(emotion, intensity, rotationAnimation)
        }
        
        // Core emotional indicator
        Canvas(
            modifier = Modifier.size(80.dp)
        ) {
            drawEmotionalCore(emotion, intensity)
        }
        
        // Intensity ripples
        if (intensity > 0.7) {
            repeat(3) { index ->
                val delay = index * 200f
                val rippleAnimation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, delayMillis = delay.toInt()),
                        repeatMode = RepeatMode.Restart
                    )
                )
                
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    drawRippleEffect(emotion, rippleAnimation)
                }
            }
        }
    }
}

private fun DrawScope.drawEmotionalAura(
    emotion: EmotionalState,
    intensity: Double,
    rotation: Float
) {
    val colors = getEmotionGradient(emotion)
    val brush = Brush.radialGradient(
        colors = colors,
        radius = size.minDimension / 2f
    )
    
    val alpha = (intensity * 0.6f).toFloat()
    
    // Rotating aura
    rotate(rotation) {
        for (i in 0..8) {
            val angle = (i * 45f)
            val distance = size.minDimension / 4f
            val x = center.x + cos(Math.toRadians(angle.toDouble())).toFloat() * distance
            val y = center.y + sin(Math.toRadians(angle.toDouble())).toFloat() * distance
            
            drawCircle(
                brush = brush,
                radius = 20f,
                center = Offset(x, y),
                alpha = alpha
            )
        }
    }
}

private fun DrawScope.drawEmotionalCore(
    emotion: EmotionalState,
    intensity: Double
) {
    val color = getEmotionColor(emotion)
    val radius = (size.minDimension / 2f) * intensity.toFloat()
    
    // Core circle
    drawCircle(
        color = color,
        radius = radius,
        center = center,
        alpha = 0.8f
    )
    
    // Inner highlight
    drawCircle(
        color = Color.White,
        radius = radius * 0.3f,
        center = center,
        alpha = 0.4f
    )
    
    // Emotional pattern
    when (emotion) {
        EmotionalState.WRATHFUL -> drawWrathfulPattern(color, intensity)
        EmotionalState.JOYFUL -> drawJoyfulPattern(color, intensity)
        EmotionalState.FEARFUL -> drawFearfulPattern(color, intensity)
        EmotionalState.LOYAL -> drawLoyalPattern(color, intensity)
        else -> {}
    }
}

private fun DrawScope.drawWrathfulPattern(color: Color, intensity: Double) {
    val spikes = 8
    val innerRadius = size.minDimension / 4f
    val outerRadius = innerRadius * 1.5f * intensity.toFloat()
    
    for (i in 0 until spikes) {
        val angle = (i * (360f / spikes))
        val startX = center.x + cos(Math.toRadians(angle.toDouble())).toFloat() * innerRadius
        val startY = center.y + sin(Math.toRadians(angle.toDouble())).toFloat() * innerRadius
        val endX = center.x + cos(Math.toRadians(angle.toDouble())).toFloat() * outerRadius
        val endY = center.y + sin(Math.toRadians(angle.toDouble())).toFloat() * outerRadius
        
        drawLine(
            color = color,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = 3.dp.toPx(),
            alpha = 0.7f
        )
    }
}

private fun DrawScope.drawJoyfulPattern(color: Color, intensity: Double) {
    val sparkles = 12
    val radius = size.minDimension / 3f
    
    for (i in 0 until sparkles) {
        val angle = (i * (360f / sparkles))
        val distance = radius * (0.7f + Math.random().toFloat() * 0.3f)
        val x = center.x + cos(Math.toRadians(angle.toDouble())).toFloat() * distance
        val y = center.y + sin(Math.toRadians(angle.toDouble())).toFloat() * distance
        
        drawCircle(
            color = Color.Yellow.copy(alpha = 0.8f),
            radius = 3.dp.toPx() * intensity.toFloat(),
            center = Offset(x, y)
        )
    }
}

private fun DrawScope.drawFearfulPattern(color: Color, intensity: Double) {
    val waves = 6
    val baseRadius = size.minDimension / 4f
    
    for (i in 0 until waves) {
        val waveRadius = baseRadius + (i * 5.dp.toPx())
        drawCircle(
            color = color,
            radius = waveRadius,
            center = center,
            alpha = (0.3f / (i + 1)) * intensity.toFloat(),
            style = Stroke(width = 1.dp.toPx())
        )
    }
}

private fun DrawScope.drawLoyalPattern(color: Color, intensity: Double) {
    // Draw a shield-like pattern
    val shieldPoints = listOf(
        Offset(center.x, center.y - 20.dp.toPx()),
        Offset(center.x + 15.dp.toPx(), center.y - 10.dp.toPx()),
        Offset(center.x + 15.dp.toPx(), center.y + 10.dp.toPx()),
        Offset(center.x, center.y + 20.dp.toPx()),
        Offset(center.x - 15.dp.toPx(), center.y + 10.dp.toPx()),
        Offset(center.x - 15.dp.toPx(), center.y - 10.dp.toPx())
    )
    
    val path = Path().apply {
        moveTo(shieldPoints[0].x, shieldPoints[0].y)
        shieldPoints.forEach { point ->
            lineTo(point.x, point.y)
        }
        close()
    }
    
    drawPath(
        path = path,
        color = color,
        alpha = 0.6f * intensity.toFloat(),
        style = Stroke(width = 2.dp.toPx())
    )
}

private fun DrawScope.drawRippleEffect(
    emotion: EmotionalState,
    animation: Float
) {
    val color = getEmotionColor(emotion)
    val maxRadius = size.minDimension / 2f
    val currentRadius = maxRadius * animation
    val alpha = 1f - animation
    
    drawCircle(
        color = color,
        radius = currentRadius,
        center = center,
        alpha = alpha * 0.3f,
        style = Stroke(width = 2.dp.toPx())
    )
}

@Composable
private fun IntensityIndicator(
    intensity: Double,
    emotion: EmotionalState
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Intensity:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Intensity bars
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            repeat(5) { index ->
                val barHeight = 4.dp + (index * 2.dp)
                val isActive = (index + 1) <= (intensity * 5).toInt()
                
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .height(barHeight)
                        .background(
                            color = if (isActive) getEmotionColor(emotion) 
                                   else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(1.dp)
                        )
                )
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = "${(intensity * 100).toInt()}%",
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium
            ),
            color = getEmotionColor(emotion)
        )
    }
}

@Composable
private fun TransitionIndicator(
    fromEmotion: EmotionalState,
    toEmotion: EmotionalState
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // From emotion
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            getEmotionColor(fromEmotion).copy(alpha = 0.6f),
                            CircleShape
                        )
                )
                Text(
                    text = fromEmotion.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            // Arrow with animation
            Text(
                text = "→",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            // To emotion
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            getEmotionColor(toEmotion),
                            CircleShape
                        )
                )
                Text(
                    text = toEmotion.displayName,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = getEmotionColor(toEmotion)
                )
            }
        }
    }
}

@Composable
fun TypingIndicator(
    isVisible: Boolean,
    npcName: String
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically()
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Typing animation dots
                repeat(3) { index ->
                    val animationDelay = index * 200
                    val infiniteTransition = rememberInfiniteTransition()
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, delayMillis = animationDelay),
                            repeatMode = RepeatMode.Reverse
                        )
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = alpha),
                                CircleShape
                            )
                    )
                    
                    if (index < 2) {
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = "$npcName is thinking...",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

// Helper functions
private fun getEmotionColor(emotion: EmotionalState): Color {
    return when (emotion) {
        EmotionalState.HOPEFUL -> Color(0xFF4CAF50)      // Green
        EmotionalState.BITTER -> Color(0xFF8D6E63)       // Brown
        EmotionalState.WRATHFUL -> Color(0xFFF44336)     // Red
        EmotionalState.FEARFUL -> Color(0xFF9C27B0)      // Purple
        EmotionalState.RESIGNED -> Color(0xFF607D8B)     // Blue Grey
        EmotionalState.JOYFUL -> Color(0xFFFFEB3B)       // Yellow
        EmotionalState.BETRAYED -> Color(0xFF673AB7)     // Deep Purple
        EmotionalState.LOYAL -> Color(0xFF2196F3)        // Blue
        EmotionalState.NEUTRAL -> Color(0xFF9E9E9E)      // Grey
    }
}

private fun getEmotionGradient(emotion: EmotionalState): List<Color> {
    val baseColor = getEmotionColor(emotion)
    return listOf(
        baseColor.copy(alpha = 0.8f),
        baseColor.copy(alpha = 0.4f),
        baseColor.copy(alpha = 0.1f),
        Color.Transparent
    )
}

val EmotionalState.displayName: String
    get() = when (this) {
        EmotionalState.HOPEFUL -> "Hopeful"
        EmotionalState.BITTER -> "Bitter"
        EmotionalState.WRATHFUL -> "Wrathful"
        EmotionalState.FEARFUL -> "Fearful"
        EmotionalState.RESIGNED -> "Resigned"
        EmotionalState.JOYFUL -> "Joyful"
        EmotionalState.BETRAYED -> "Betrayed"
        EmotionalState.LOYAL -> "Loyal"
        EmotionalState.NEUTRAL -> "Neutral"
    }