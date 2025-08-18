package com.orion.dndgame.eds.dialogue

import com.orion.dndgame.data.models.NPC
import com.orion.dndgame.data.models.PlayerChoice
import com.orion.dndgame.eds.core.EmotionalState
import com.orion.dndgame.network.OrionWebSocketClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Adapter that integrates the Android D&D app with the ProjectChimera consciousness system
 * running on the Orion orchestrator. Provides consciousness-driven dialogue generation.
 */
@Singleton
class ProjectChimeraDialogueAdapter @Inject constructor(
    private val orionClient: OrionWebSocketClient
) {

    private var isConnected: Boolean = false
    private val consciousnessCache = mutableMapOf<String, ConsciousnessState>()

    /**
     * Generate a consciousness-driven response using ProjectChimera integration
     */
    suspend fun generateConsciousResponse(
        npc: NPC,
        context: DialogueContext,
        playerInput: PlayerChoice
    ): ConsciousnessResponse {
        
        return try {
            if (orionClient.isConnected.first()) {
                generateChimeraResponse(npc, context, playerInput)
            } else {
                generateFallbackResponse(npc, context, playerInput)
            }
        } catch (e: Exception) {
            // Fallback to local generation if Chimera is unavailable
            generateFallbackResponse(npc, context, playerInput)
        }
    }

    /**
     * Generate response using ProjectChimera consciousness system via Orion
     */
    private suspend fun generateChimeraResponse(
        npc: NPC,
        context: DialogueContext,
        playerInput: PlayerChoice
    ): ConsciousnessResponse {
        
        val chimeraRequest = ChimeraDialogueRequest(
            npcId = npc.id,
            npcPersonality = npc.personality,
            emotionalProfile = npc.emotionalProfile,
            playerAction = PlayerActionStimuli(
                type = playerInput.type.name,
                content = playerInput.text,
                emotionalIntensity = calculateEmotionalIntensity(playerInput),
                novelty = calculateNovelty(npc, playerInput),
                complexity = calculateComplexity(context, playerInput)
            ),
            contextualData = ContextualData(
                questState = context.quest?.status?.name ?: "NONE",
                relationshipLevel = npc.emotionalProfile.memory.getRelationshipWith("player"),
                recentInteractions = npc.emotionalProfile.memory.recentEvents.takeLast(3),
                environmentalFactors = context.environmentalFactors
            )
        )

        // Send request to Orion/ProjectChimera
        val response = withTimeout(5000) {
            orionClient.sendChimeraDialogueRequest(chimeraRequest)
        }

        return ConsciousnessResponse(
            responseText = response.synthesizedResponse.content,
            awarenessLevel = response.consciousnessUpdate.awarenessLevel,
            cognitiveLoad = response.consciousnessUpdate.cognitiveLoad,
            metacognitionLevel = response.consciousnessUpdate.metacognitionLevel,
            authenticity = response.synthesizedResponse.authenticity,
            emergentTraits = response.emergentBehavior.traits.map { it.name },
            consciousnessCommentary = response.consciousnessCommentary
        )
    }

    /**
     * Fallback response when ProjectChimera is not available
     */
    private suspend fun generateFallbackResponse(
        npc: NPC,
        context: DialogueContext,
        playerInput: PlayerChoice
    ): ConsciousnessResponse {
        
        // Simulate processing delay
        delay(Random.nextLong(100, 300))
        
        val awareness = calculateFallbackAwareness(npc, context)
        val cognitiveLoad = calculateFallbackCognitiveLoad(context)
        
        return ConsciousnessResponse(
            responseText = generateSimulatedResponse(npc, context, playerInput),
            awarenessLevel = awareness,
            cognitiveLoad = cognitiveLoad,
            metacognitionLevel = awareness * 0.7f,
            authenticity = 0.6f + Random.nextFloat() * 0.3f,
            emergentTraits = generateSimulatedTraits(npc),
            consciousnessCommentary = generateSimulatedCommentary(awareness, cognitiveLoad)
        )
    }

    /**
     * Calculate emotional intensity of player input
     */
    private fun calculateEmotionalIntensity(playerInput: PlayerChoice): Float {
        return when (playerInput.type) {
            PlayerChoiceType.AGGRESSIVE, PlayerChoiceType.HOSTILE -> 0.8f + Random.nextFloat() * 0.2f
            PlayerChoiceType.COMPASSIONATE, PlayerChoiceType.HEROIC -> 0.6f + Random.nextFloat() * 0.3f
            PlayerChoiceType.DIPLOMATIC, PlayerChoiceType.FRIENDLY -> 0.4f + Random.nextFloat() * 0.3f
            PlayerChoiceType.NEUTRAL -> 0.2f + Random.nextFloat() * 0.2f
            else -> 0.3f + Random.nextFloat() * 0.4f
        }
    }

    /**
     * Calculate novelty of the interaction for this NPC
     */
    private fun calculateNovelty(npc: NPC, playerInput: PlayerChoice): Float {
        val recentChoices = npc.emotionalProfile.memory.recentEvents
            .map { it.source }
            .count { it == playerInput.type.name }
            
        return (1.0f - (recentChoices.toFloat() / 10.0f)).coerceAtLeast(0.1f)
    }

    /**
     * Calculate complexity of the current dialogue situation
     */
    private fun calculateComplexity(context: DialogueContext, playerInput: PlayerChoice): Float {
        var complexity = 0.3f
        
        if (context.quest != null) complexity += 0.2f
        if (context.hasMultipleNPCs) complexity += 0.3f
        if (playerInput.type == PlayerChoiceType.DIPLOMATIC) complexity += 0.2f
        if (context.environmentalFactors.isNotEmpty()) complexity += 0.1f
        
        return complexity.coerceAtMost(1.0f)
    }

    private fun calculateFallbackAwareness(npc: NPC, context: DialogueContext): Float {
        val baseAwareness = when (npc.emotionalProfile.primaryState) {
            EmotionalState.HOPEFUL, EmotionalState.JOYFUL -> 0.7f
            EmotionalState.FEARFUL, EmotionalState.RESIGNED -> 0.4f
            EmotionalState.WRATHFUL, EmotionalState.BITTER -> 0.6f
            EmotionalState.LOYAL -> 0.8f
            EmotionalState.BETRAYED -> 0.5f
        }
        
        return baseAwareness + Random.nextFloat() * 0.2f - 0.1f
    }

    private fun calculateFallbackCognitiveLoad(context: DialogueContext): Float {
        var load = 0.3f
        
        if (context.quest != null) load += 0.2f
        if (context.hasMultipleNPCs) load += 0.3f
        if (context.environmentalFactors.isNotEmpty()) load += 0.1f
        
        return (load + Random.nextFloat() * 0.2f).coerceAtMost(1.0f)
    }

    private fun generateSimulatedResponse(
        npc: NPC,
        context: DialogueContext,
        playerInput: PlayerChoice
    ): String {
        val emotion = npc.emotionalProfile.primaryState
        val responses = when (emotion) {
            EmotionalState.HOPEFUL -> listOf(
                "I believe things will work out for the best.",
                "There's always reason to hope, friend.",
                "I have a good feeling about this."
            )
            EmotionalState.FEARFUL -> listOf(
                "I... I'm not sure about this.",
                "What if something goes wrong?",
                "Please, be careful out there."
            )
            EmotionalState.JOYFUL -> listOf(
                "What a wonderful day this is!",
                "I'm so glad you're here!",
                "Life has been treating me well lately."
            )
            EmotionalState.BITTER -> listOf(
                "Life has a way of disappointing you.",
                "I've learned not to expect much from people.",
                "Things rarely turn out as promised."
            )
            EmotionalState.WRATHFUL -> listOf(
                "I'm tired of being pushed around!",
                "Someone will pay for what happened!",
                "My patience has run out!"
            )
            EmotionalState.LOYAL -> listOf(
                "You can count on me, always.",
                "I stand by my word.",
                "Together we can overcome anything."
            )
            EmotionalState.BETRAYED -> listOf(
                "How can I trust anyone anymore?",
                "I thought they cared, but I was wrong.",
                "Everyone leaves in the end."
            )
            EmotionalState.RESIGNED -> listOf(
                "I suppose it doesn't matter anymore.",
                "What will be, will be.",
                "I've accepted my fate."
            )
        }
        
        return responses.random()
    }

    private fun generateSimulatedTraits(npc: NPC): List<String> {
        val baseTraits = when (npc.emotionalProfile.primaryState) {
            EmotionalState.HOPEFUL -> listOf("optimistic_outlook", "future_focused")
            EmotionalState.FEARFUL -> listOf("heightened_caution", "anxiety_response")
            EmotionalState.JOYFUL -> listOf("infectious_enthusiasm", "positive_energy")
            EmotionalState.BITTER -> listOf("cynical_worldview", "emotional_walls")
            EmotionalState.WRATHFUL -> listOf("aggressive_tendencies", "justice_seeking")
            EmotionalState.LOYAL -> listOf("unwavering_dedication", "protective_instincts")
            EmotionalState.BETRAYED -> listOf("trust_issues", "emotional_vulnerability")
            EmotionalState.RESIGNED -> listOf("passive_acceptance", "emotional_numbness")
        }
        
        return baseTraits.take(Random.nextInt(1, 3))
    }

    private fun generateSimulatedCommentary(awareness: Float, cognitiveLoad: Float): String {
        return when {
            awareness > 0.8f -> "I'm acutely aware of the nuances in this conversation."
            awareness > 0.6f -> "I'm following the conversation well."
            awareness > 0.4f -> "I'm somewhat aware, though some details may escape me."
            else -> "My awareness is limited to immediate concerns."
        }
    }
}

/**
 * Data classes for ProjectChimera integration
 */

data class ChimeraDialogueRequest(
    val npcId: String,
    val npcPersonality: Map<String, Float>,
    val emotionalProfile: com.orion.dndgame.eds.core.EmotionalProfile,
    val playerAction: PlayerActionStimuli,
    val contextualData: ContextualData
)

data class PlayerActionStimuli(
    val type: String,
    val content: String,
    val emotionalIntensity: Float,
    val novelty: Float,
    val complexity: Float
)

data class ContextualData(
    val questState: String,
    val relationshipLevel: Float,
    val recentInteractions: List<com.orion.dndgame.eds.core.EmotionalEvent>,
    val environmentalFactors: List<String>
)

data class ConsciousnessResponse(
    val responseText: String,
    val awarenessLevel: Float,
    val cognitiveLoad: Float,
    val metacognitionLevel: Float,
    val authenticity: Float,
    val emergentTraits: List<String>,
    val consciousnessCommentary: String
)

data class ConsciousnessState(
    val awarenessLevel: Float,
    val cognitiveLoad: Float,
    val metacognitionLevel: Float,
    val lastUpdated: Long
)

data class DialogueContext(
    val npc: NPC,
    val quest: com.orion.dndgame.data.models.Quest?,
    val hasMultipleNPCs: Boolean = false,
    val environmentalFactors: List<String> = emptyList(),
    val timeOfDay: String = "day",
    val location: String = "unknown"
)