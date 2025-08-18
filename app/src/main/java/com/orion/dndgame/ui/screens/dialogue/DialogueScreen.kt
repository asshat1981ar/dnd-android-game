package com.orion.dndgame.ui.screens.dialogue

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.orion.dndgame.data.models.GameState
import com.orion.dndgame.data.models.PlayerChoice
import com.orion.dndgame.eds.core.EmotionalState
import com.orion.dndgame.eds.dialogue.DialogueEntry
import com.orion.dndgame.eds.dialogue.Speaker
import com.orion.dndgame.ui.components.EmotionalStateIndicator
import com.orion.dndgame.ui.components.LoadingPulse
import com.orion.dndgame.ui.events.GameEvent
import com.orion.dndgame.ui.viewmodel.DialogueViewModel

/**
 * Advanced dialogue screen with emotional intelligence and consciousness integration
 * Features real-time NPC emotional states and dynamic choice generation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogueScreen(
    gameState: GameState,
    onNavigateBack: () -> Unit,
    onGameEvent: (GameEvent) -> Unit,
    viewModel: DialogueViewModel = hiltViewModel()
) {
    val dialogueState by viewModel.dialogueState.collectAsStateWithLifecycle()
    val isGeneratingResponse by viewModel.isGeneratingResponse.collectAsStateWithLifecycle()
    val currentNPC by viewModel.currentNPC.collectAsStateWithLifecycle()
    val availableChoices by viewModel.availableChoices.collectAsStateWithLifecycle()
    val conversationHistory by viewModel.conversationHistory.collectAsStateWithLifecycle()
    
    val scrollState = rememberLazyListState()
    
    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(conversationHistory.size) {
        if (conversationHistory.isNotEmpty()) {
            scrollState.animateScrollToItem(conversationHistory.size - 1)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentNPC?.name ?: "Dialogue",
                            fontWeight = FontWeight.Bold
                        )
                        if (currentNPC != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            EmotionalStateIndicator(
                                emotionalState = currentNPC.emotionalProfile.primaryState,
                                intensity = currentNPC.emotionalProfile.intensity,
                                size = 20.dp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (currentNPC != null) {
                        IconButton(
                            onClick = {
                                onGameEvent(GameEvent.ShowConsciousnessDetails(currentNPC.id))
                            }
                        ) {
                            Icon(
                                Icons.Default.Psychology,
                                contentDescription = "Consciousness Details",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Conversation history
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = scrollState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(conversationHistory) { entry ->
                    DialogueMessage(
                        entry = entry,
                        isPlayer = entry.speaker == Speaker.PLAYER,
                        emotionalState = entry.emotionalState
                    )
                }
                
                if (isGeneratingResponse) {
                    item {
                        NPCThinkingIndicator(
                            npcName = currentNPC?.name ?: "NPC",
                            consciousnessLevel = currentNPC?.consciousnessLevel ?: 0.5f
                        )
                    }
                }
            }
            
            // Player choice selection
            AnimatedVisibility(
                visible = availableChoices.isNotEmpty() && !isGeneratingResponse,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                PlayerChoicePanel(
                    choices = availableChoices,
                    onChoiceSelected = { choice ->
                        viewModel.selectChoice(choice)
                        onGameEvent(GameEvent.DialogueChoiceMade(choice))
                    }
                )
            }
        }
    }
}

@Composable
private fun DialogueMessage(
    entry: DialogueEntry,
    isPlayer: Boolean,
    emotionalState: EmotionalState?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isPlayer) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .fillMaxWidth(if (isPlayer) 0.85f else 1f),
            colors = CardDefaults.cardColors(
                containerColor = if (isPlayer) {
                    MaterialTheme.colorScheme.primary
                } else {
                    emotionalState?.getColorTheme()?.primaryColor?.let { Color(it) }
                        ?: MaterialTheme.colorScheme.surfaceVariant
                }
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isPlayer) 16.dp else 4.dp,
                bottomEnd = if (isPlayer) 4.dp else 16.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                if (!isPlayer && emotionalState != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        EmotionalStateIndicator(
                            emotionalState = emotionalState,
                            intensity = 0.7f,
                            size = 16.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = emotionalState.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                Text(
                    text = entry.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isPlayer) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
private fun NPCThinkingIndicator(
    npcName: String,
    consciousnessLevel: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LoadingPulse(
                size = 24.dp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "$npcName is thinking...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Consciousness level: ${(consciousnessLevel * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun PlayerChoicePanel(
    choices: List<PlayerChoice>,
    onChoiceSelected: (PlayerChoice) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Choose your response:",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            choices.forEach { choice ->
                PlayerChoiceButton(
                    choice = choice,
                    onClick = { onChoiceSelected(choice) }
                )
            }
        }
    }
}

@Composable
private fun PlayerChoiceButton(
    choice: PlayerChoice,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = when (choice.type) {
                com.orion.dndgame.eds.dialogue.PlayerChoiceType.AGGRESSIVE -> 
                    MaterialTheme.colorScheme.errorContainer
                com.orion.dndgame.eds.dialogue.PlayerChoiceType.COMPASSIONATE -> 
                    Color(0xFF4CAF50).copy(alpha = 0.1f)
                com.orion.dndgame.eds.dialogue.PlayerChoiceType.DIPLOMATIC -> 
                    MaterialTheme.colorScheme.primaryContainer
                com.orion.dndgame.eds.dialogue.PlayerChoiceType.HEROIC -> 
                    Color(0xFFFFD700).copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = choice.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                
                Badge(
                    containerColor = when (choice.type) {
                        com.orion.dndgame.eds.dialogue.PlayerChoiceType.AGGRESSIVE -> 
                            MaterialTheme.colorScheme.error
                        com.orion.dndgame.eds.dialogue.PlayerChoiceType.COMPASSIONATE -> 
                            Color(0xFF4CAF50)
                        com.orion.dndgame.eds.dialogue.PlayerChoiceType.DIPLOMATIC -> 
                            MaterialTheme.colorScheme.primary
                        com.orion.dndgame.eds.dialogue.PlayerChoiceType.HEROIC -> 
                            Color(0xFFFFD700)
                        else -> MaterialTheme.colorScheme.secondary
                    }
                ) {
                    Text(
                        text = choice.type.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onError
                    )
                }
            }
            
            // Show emotional impact preview
            if (choice.emotionalImpact.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    choice.emotionalImpact.forEach { (emotionName, impact) ->
                        if (impact != 0f) {
                            EmotionalImpactChip(
                                emotionName = emotionName,
                                impact = impact
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmotionalImpactChip(
    emotionName: String,
    impact: Float
) {
    val color = if (impact > 0) Color(0xFF4CAF50) else Color(0xFFF44336)
    val sign = if (impact > 0) "+" else ""
    
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.clip(RoundedCornerShape(12.dp))
    ) {
        Text(
            text = "$emotionName $sign${(impact * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}