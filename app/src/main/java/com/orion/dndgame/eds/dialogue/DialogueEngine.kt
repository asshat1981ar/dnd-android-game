package com.orion.dndgame.eds.dialogue

import com.orion.dndgame.eds.core.EmotionalProfile
import com.orion.dndgame.eds.core.EmotionalState
import com.orion.dndgame.data.models.NPC
import com.orion.dndgame.data.models.Quest
import com.orion.dndgame.data.models.PlayerChoice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Advanced Dialogue Engine that generates contextual, emotional responses
 * Integrates with ProjectChimera consciousness system for truly dynamic NPCs
 */
@Singleton
class DialogueEngine @Inject constructor(
    private val contextAnalyzer: DialogueContextAnalyzer,
    private val responseGenerator: EmotionalResponseGenerator,
    private val memoryManager: DialogueMemoryManager,
    private val chimeraIntegration: ProjectChimeraDialogueAdapter
) {

    /**
     * Generate a dialogue response based on NPC emotional state, quest context, and player choice
     */
    suspend fun generateDialogueResponse(
        npc: NPC,
        playerChoice: PlayerChoice,
        questContext: Quest?,
        conversationHistory: List<DialogueEntry>
    ): DialogueResponse {

        // Analyze the current dialogue context
        val context = contextAnalyzer.analyzeContext(
            npc = npc,
            playerChoice = playerChoice,
            questContext = questContext,
            history = conversationHistory
        )

        // Generate consciousness-driven response through ProjectChimera
        val consciousnessResponse = chimeraIntegration.generateConsciousResponse(
            npc = npc,
            context = context,
            playerInput = playerChoice
        )

        // Generate emotional response based on current state
        val emotionalResponse = responseGenerator.generateResponse(
            emotionalProfile = npc.emotionalProfile,
            context = context,
            consciousnessInput = consciousnessResponse
        )

        // Update NPC memory with this interaction
        val updatedNPC = memoryManager.updateNPCMemory(
            npc = npc,
            playerChoice = playerChoice,
            context = context
        )

        // Create the final dialogue response
        return DialogueResponse(
            npcId = npc.id,
            responseText = emotionalResponse.text,
            emotionalTone = emotionalResponse.tone,
            availableChoices = generatePlayerChoices(context, emotionalResponse),
            npcStateChange = NPCStateChange(
                updatedEmotionalProfile = updatedNPC.emotionalProfile,
                relationshipChange = calculateRelationshipChange(playerChoice, context),
                questImpact = calculateQuestImpact(playerChoice, questContext)
            ),
            metadata = DialogueMetadata(
                consciousnessLevel = consciousnessResponse.awarenessLevel,
                emotionalIntensity = emotionalResponse.intensity,
                authenticityScore = consciousnessResponse.authenticity,
                generationTimestamp = System.currentTimeMillis()
            )
        )
    }

    /**
     * Generate contextually appropriate player choices
     */
    private fun generatePlayerChoices(
        context: DialogueContext,
        emotionalResponse: EmotionalResponse
    ): List<PlayerChoice> {
        val choices = mutableListOf<PlayerChoice>()

        // Always include basic choices
        choices.add(
            PlayerChoice(
                id = "continue",
                text = "Continue conversation",
                type = PlayerChoiceType.NEUTRAL,
                emotionalImpact = emptyMap()
            )
        )

        // Add emotional responses based on NPC state
        when (emotionalResponse.tone.primaryEmotion) {
            EmotionalState.FEARFUL -> {
                choices.add(
                    PlayerChoice(
                        id = "reassure",
                        text = "Don't worry, I'm here to help",
                        type = PlayerChoiceType.COMPASSIONATE,
                        emotionalImpact = mapOf(
                            EmotionalState.HOPEFUL.name to 0.4f,
                            EmotionalState.FEARFUL.name to -0.3f
                        )
                    )
                )
                choices.add(
                    PlayerChoice(
                        id = "intimidate",
                        text = "You should be afraid",
                        type = PlayerChoiceType.AGGRESSIVE,
                        emotionalImpact = mapOf(
                            EmotionalState.FEARFUL.name to 0.6f,
                            EmotionalState.WRATHFUL.name to 0.3f
                        )
                    )
                )
            }
            
            EmotionalState.HOSTILE -> {
                choices.add(
                    PlayerChoice(
                        id = "diplomatic",
                        text = "Let's talk this through peacefully",
                        type = PlayerChoiceType.DIPLOMATIC,
                        emotionalImpact = mapOf(
                            EmotionalState.WRATHFUL.name to -0.2f,
                            EmotionalState.HOPEFUL.name to 0.3f
                        )
                    )
                )
                choices.add(
                    PlayerChoice(
                        id = "aggressive",
                        text = "I don't have time for this!",
                        type = PlayerChoiceType.AGGRESSIVE,
                        emotionalImpact = mapOf(
                            EmotionalState.WRATHFUL.name to 0.5f,
                            EmotionalState.BETRAYED.name to 0.2f
                        )
                    )
                )
            }
            
            EmotionalState.JOYFUL -> {
                choices.add(
                    PlayerChoice(
                        id = "share_joy",
                        text = "I'm glad to see you so happy!",
                        type = PlayerChoiceType.FRIENDLY,
                        emotionalImpact = mapOf(
                            EmotionalState.JOYFUL.name to 0.3f,
                            EmotionalState.LOYAL.name to 0.2f
                        )
                    )
                )
            }
        }

        // Add quest-specific choices if applicable
        if (context.quest != null) {
            choices.addAll(generateQuestChoices(context.quest, emotionalResponse))
        }

        return choices.take(4) // Limit to 4 choices for UI
    }

    private fun generateQuestChoices(quest: Quest, response: EmotionalResponse): List<PlayerChoice> {
        val choices = mutableListOf<PlayerChoice>()
        
        when (quest.type) {
            QuestType.DELIVERY -> {
                choices.add(
                    PlayerChoice(
                        id = "offer_item",
                        text = "I have something for you",
                        type = PlayerChoiceType.QUEST_ACTION,
                        emotionalImpact = mapOf(EmotionalState.HOPEFUL.name to 0.5f)
                    )
                )
            }
            QuestType.RESCUE -> {
                choices.add(
                    PlayerChoice(
                        id = "offer_help",
                        text = "I can help rescue them",
                        type = PlayerChoiceType.HEROIC,
                        emotionalImpact = mapOf(
                            EmotionalState.HOPEFUL.name to 0.6f,
                            EmotionalState.LOYAL.name to 0.3f
                        )
                    )
                )
            }
            QuestType.INVESTIGATION -> {
                choices.add(
                    PlayerChoice(
                        id = "ask_questions",
                        text = "Tell me what you know",
                        type = PlayerChoiceType.INVESTIGATIVE,
                        emotionalImpact = mapOf(EmotionalState.FEARFUL.name to 0.1f)
                    )
                )
            }
        }
        
        return choices
    }

    private fun calculateRelationshipChange(
        playerChoice: PlayerChoice,
        context: DialogueContext
    ): Float {
        var change = 0.0f
        
        // Base change from choice type
        change += when (playerChoice.type) {
            PlayerChoiceType.COMPASSIONATE -> 0.2f
            PlayerChoiceType.FRIENDLY -> 0.15f
            PlayerChoiceType.DIPLOMATIC -> 0.1f
            PlayerChoiceType.NEUTRAL -> 0.0f
            PlayerChoiceType.AGGRESSIVE -> -0.2f
            PlayerChoiceType.HOSTILE -> -0.3f
            PlayerChoiceType.HEROIC -> 0.25f
            PlayerChoiceType.INVESTIGATIVE -> 0.05f
            PlayerChoiceType.QUEST_ACTION -> 0.1f
        }
        
        // Modify based on NPC's current emotional state
        val npcState = context.npc.emotionalProfile.primaryState
        change *= when (npcState) {
            EmotionalState.LOYAL -> 1.2f // Loyal NPCs respond more strongly to positive actions
            EmotionalState.BITTER -> 0.7f // Bitter NPCs are harder to win over
            EmotionalState.BETRAYED -> 0.5f // Betrayed NPCs are very resistant to positive change
            EmotionalState.FEARFUL -> 1.1f // Fearful NPCs appreciate kindness more
            else -> 1.0f
        }
        
        return change.coerceIn(-1.0f, 1.0f)
    }

    private fun calculateQuestImpact(
        playerChoice: PlayerChoice,
        quest: Quest?
    ): QuestImpact? {
        if (quest == null) return null
        
        return when (playerChoice.type) {
            PlayerChoiceType.QUEST_ACTION -> QuestImpact(
                questId = quest.id,
                progressChange = 0.2f,
                statusChange = null
            )
            PlayerChoiceType.HEROIC -> QuestImpact(
                questId = quest.id,
                progressChange = 0.15f,
                statusChange = null
            )
            PlayerChoiceType.AGGRESSIVE -> QuestImpact(
                questId = quest.id,
                progressChange = -0.1f,
                statusChange = null
            )
            else -> null
        }
    }
}

/**
 * Data classes for dialogue system
 */

data class DialogueResponse(
    val npcId: String,
    val responseText: String,
    val emotionalTone: EmotionalTone,
    val availableChoices: List<PlayerChoice>,
    val npcStateChange: NPCStateChange,
    val metadata: DialogueMetadata
)

data class EmotionalTone(
    val primaryEmotion: EmotionalState,
    val intensity: Float,
    val secondaryEmotions: List<Pair<EmotionalState, Float>> = emptyList()
)

data class NPCStateChange(
    val updatedEmotionalProfile: EmotionalProfile,
    val relationshipChange: Float,
    val questImpact: QuestImpact?
)

data class DialogueMetadata(
    val consciousnessLevel: Float,
    val emotionalIntensity: Float,
    val authenticityScore: Float,
    val generationTimestamp: Long
)

data class QuestImpact(
    val questId: String,
    val progressChange: Float,
    val statusChange: QuestStatus?
)

data class DialogueEntry(
    val speaker: Speaker,
    val text: String,
    val timestamp: Long,
    val emotionalState: EmotionalState? = null
)

enum class Speaker {
    PLAYER, NPC, NARRATOR
}

enum class QuestType {
    DELIVERY, RESCUE, INVESTIGATION, COMBAT, EXPLORATION, DIPLOMACY
}

enum class QuestStatus {
    ACTIVE, COMPLETED, FAILED, PAUSED
}

enum class PlayerChoiceType {
    NEUTRAL, FRIENDLY, AGGRESSIVE, DIPLOMATIC, COMPASSIONATE, 
    HOSTILE, HEROIC, INVESTIGATIVE, QUEST_ACTION
}

/**
 * Emotional response from the generation system
 */
data class EmotionalResponse(
    val text: String,
    val tone: EmotionalTone,
    val intensity: Float,
    val authenticity: Float
)