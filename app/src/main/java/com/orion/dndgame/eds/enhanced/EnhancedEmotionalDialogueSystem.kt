package com.orion.dndgame.eds.enhanced

import com.orion.dndgame.eds.core.EmotionalState
import com.orion.dndgame.eds.core.NPCMemory
import com.orion.dndgame.eds.core.PlayerChoice
import com.orion.dndgame.eds.core.QuestState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Enhanced Emotional Dialogue System with improved transitions and user experience
 */
@Singleton
class EnhancedEmotionalDialogueSystem @Inject constructor() {
    
    private val _emotionalTransitions = MutableStateFlow(EmotionalTransitionMatrix())
    val emotionalTransitions: StateFlow<EmotionalTransitionMatrix> = _emotionalTransitions.asStateFlow()
    
    private val _recentInteractions = MutableStateFlow<List<InteractionContext>>(emptyList())
    val recentInteractions: StateFlow<List<InteractionContext>> = _recentInteractions.asStateFlow()
    
    /**
     * Enhanced emotional state transition matrix with probabilities and context
     */
    data class EmotionalTransitionMatrix(
        val transitions: Map<EmotionalState, Map<String, TransitionProbability>> = createDefaultTransitions()
    ) {
        companion object {
            fun createDefaultTransitions(): Map<EmotionalState, Map<String, TransitionProbability>> {
                return mapOf(
                    EmotionalState.NEUTRAL to mapOf(
                        "encouragement" to TransitionProbability(EmotionalState.HOPEFUL, 0.8),
                        "betrayal" to TransitionProbability(EmotionalState.BITTER, 0.7),
                        "threat" to TransitionProbability(EmotionalState.FEARFUL, 0.6),
                        "kindness" to TransitionProbability(EmotionalState.HOPEFUL, 0.5),
                        "dismissal" to TransitionProbability(EmotionalState.BITTER, 0.4)
                    ),
                    EmotionalState.HOPEFUL to mapOf(
                        "success" to TransitionProbability(EmotionalState.LOYAL, 0.9),
                        "major_success" to TransitionProbability(EmotionalState.JOYFUL, 0.8),
                        "betrayal" to TransitionProbability(EmotionalState.BETRAYED, 0.9),
                        "failure" to TransitionProbability(EmotionalState.BITTER, 0.6),
                        "continued_support" to TransitionProbability(EmotionalState.HOPEFUL, 0.8)
                    ),
                    EmotionalState.BITTER to mapOf(
                        "redemption" to TransitionProbability(EmotionalState.HOPEFUL, 0.4),
                        "continued_betrayal" to TransitionProbability(EmotionalState.WRATHFUL, 0.7),
                        "genuine_apology" to TransitionProbability(EmotionalState.NEUTRAL, 0.5),
                        "time_passage" to TransitionProbability(EmotionalState.RESIGNED, 0.3)
                    ),
                    EmotionalState.WRATHFUL to mapOf(
                        "justice" to TransitionProbability(EmotionalState.NEUTRAL, 0.6),
                        "revenge" to TransitionProbability(EmotionalState.BITTER, 0.5),
                        "time_passage" to TransitionProbability(EmotionalState.RESIGNED, 0.4),
                        "escalation" to TransitionProbability(EmotionalState.WRATHFUL, 0.9)
                    ),
                    EmotionalState.FEARFUL to mapOf(
                        "protection" to TransitionProbability(EmotionalState.HOPEFUL, 0.6),
                        "safety" to TransitionProbability(EmotionalState.NEUTRAL, 0.7),
                        "repeated_threats" to TransitionProbability(EmotionalState.RESIGNED, 0.8),
                        "abandonment" to TransitionProbability(EmotionalState.BITTER, 0.5)
                    ),
                    EmotionalState.RESIGNED to mapOf(
                        "unexpected_kindness" to TransitionProbability(EmotionalState.HOPEFUL, 0.3),
                        "major_positive_event" to TransitionProbability(EmotionalState.NEUTRAL, 0.5),
                        "continued_neglect" to TransitionProbability(EmotionalState.RESIGNED, 0.9)
                    ),
                    EmotionalState.JOYFUL to mapOf(
                        "betrayal" to TransitionProbability(EmotionalState.BETRAYED, 0.9),
                        "continued_success" to TransitionProbability(EmotionalState.LOYAL, 0.7),
                        "minor_setback" to TransitionProbability(EmotionalState.HOPEFUL, 0.6),
                        "celebration" to TransitionProbability(EmotionalState.JOYFUL, 0.8)
                    ),
                    EmotionalState.BETRAYED to mapOf(
                        "justice" to TransitionProbability(EmotionalState.BITTER, 0.6),
                        "revenge" to TransitionProbability(EmotionalState.WRATHFUL, 0.8),
                        "time_healing" to TransitionProbability(EmotionalState.RESIGNED, 0.4),
                        "genuine_reconciliation" to TransitionProbability(EmotionalState.NEUTRAL, 0.2)
                    ),
                    EmotionalState.LOYAL to mapOf(
                        "betrayal" to TransitionProbability(EmotionalState.BETRAYED, 1.0),
                        "continued_partnership" to TransitionProbability(EmotionalState.LOYAL, 0.9),
                        "great_achievement" to TransitionProbability(EmotionalState.JOYFUL, 0.7),
                        "minor_disappointment" to TransitionProbability(EmotionalState.HOPEFUL, 0.3)
                    )
                )
            }
        }
    }
    
    data class TransitionProbability(
        val targetState: EmotionalState,
        val probability: Double,
        val contextModifiers: Map<String, Double> = emptyMap()
    )
    
    data class InteractionContext(
        val npcId: String,
        val playerAction: String,
        val playerChoice: PlayerChoice,
        val questState: QuestState,
        val timestamp: Long,
        val emotionalImpact: Double,
        val memorySignificance: Double
    )
    
    data class EnhancedDialogueResponse(
        val content: String,
        val emotionalTone: EmotionalState,
        val emotionalIntensity: Double,
        val memoryImprint: MemoryImprint,
        val futureInfluence: FutureInfluence,
        val visualCues: VisualCues,
        val audioHints: AudioHints
    )
    
    data class MemoryImprint(
        val significance: Double,
        val emotionalWeight: Double,
        val decayRate: Double,
        val associations: List<String>
    )
    
    data class FutureInfluence(
        val trustImpact: Double,
        val relationshipShift: Double,
        val questConsequences: List<String>,
        val unlocksPotential: List<String>
    )
    
    data class VisualCues(
        val facialExpression: String,
        val bodyLanguage: String,
        val eyeContact: String,
        val gestureIntensity: Double
    )
    
    data class AudioHints(
        val toneOfVoice: String,
        val speechPace: String,
        val volume: String,
        val emotionalUndertone: String
    )
    
    /**
     * Generate enhanced dialogue with improved emotional transitions
     */
    suspend fun generateEnhancedDialogue(
        npcId: String,
        currentEmotion: EmotionalState,
        playerAction: String,
        playerChoice: PlayerChoice,
        questState: QuestState,
        npcMemory: NPCMemory
    ): EnhancedDialogueResponse {
        
        // Calculate emotional transition
        val newEmotion = calculateEmotionalTransition(
            currentEmotion, 
            playerAction, 
            npcMemory,
            questState
        )
        
        // Generate contextual dialogue
        val baseDialogue = generateContextualDialogue(
            newEmotion,
            playerChoice,
            questState,
            npcMemory
        )
        
        // Add emotional layering
        val enhancedContent = addEmotionalLayering(
            baseDialogue,
            currentEmotion,
            newEmotion,
            npcMemory
        )
        
        // Calculate memory imprint
        val memoryImprint = calculateMemoryImprint(
            playerAction,
            playerChoice,
            newEmotion,
            npcMemory
        )
        
        // Determine future influence
        val futureInfluence = calculateFutureInfluence(
            currentEmotion,
            newEmotion,
            playerChoice,
            npcMemory
        )
        
        // Generate visual and audio cues
        val visualCues = generateVisualCues(newEmotion, memoryImprint.emotionalWeight)
        val audioHints = generateAudioHints(newEmotion, futureInfluence.trustImpact)
        
        // Record interaction
        recordInteraction(
            npcId,
            playerAction,
            playerChoice,
            questState,
            memoryImprint.significance
        )
        
        return EnhancedDialogueResponse(
            content = enhancedContent,
            emotionalTone = newEmotion,
            emotionalIntensity = calculateEmotionalIntensity(currentEmotion, newEmotion),
            memoryImprint = memoryImprint,
            futureInfluence = futureInfluence,
            visualCues = visualCues,
            audioHints = audioHints
        )
    }
    
    private fun calculateEmotionalTransition(
        currentEmotion: EmotionalState,
        playerAction: String,
        npcMemory: NPCMemory,
        questState: QuestState
    ): EmotionalState {
        val transitions = _emotionalTransitions.value.transitions[currentEmotion] ?: return currentEmotion
        
        // Find the most appropriate trigger
        val trigger = mapPlayerActionToTrigger(playerAction, npcMemory, questState)
        val transition = transitions[trigger]
        
        if (transition != null) {
            // Apply memory and context modifiers
            val modifiedProbability = applyContextModifiers(
                transition.probability,
                npcMemory,
                questState
            )
            
            // Use probability to determine if transition occurs
            if (Random.nextDouble() < modifiedProbability) {
                return transition.targetState
            }
        }
        
        // If no transition occurs, potentially apply minor emotional drift
        return applyEmotionalDrift(currentEmotion, npcMemory)
    }
    
    private fun mapPlayerActionToTrigger(
        playerAction: String,
        npcMemory: NPCMemory,
        questState: QuestState
    ): String {
        return when {
            playerAction.contains("betray", ignoreCase = true) -> "betrayal"
            playerAction.contains("help", ignoreCase = true) -> "encouragement" 
            playerAction.contains("threaten", ignoreCase = true) -> "threat"
            playerAction.contains("apologize", ignoreCase = true) -> "genuine_apology"
            playerAction.contains("succeed", ignoreCase = true) -> when {
                questState == QuestState.COMPLETION -> "major_success"
                else -> "success"
            }
            playerAction.contains("fail", ignoreCase = true) -> "failure"
            npcMemory.trustLevel > 0.8 -> "continued_support"
            npcMemory.trustLevel < 0.3 -> "continued_betrayal"
            else -> "neutral_interaction"
        }
    }
    
    private fun applyContextModifiers(
        baseProbability: Double,
        npcMemory: NPCMemory,
        questState: QuestState
    ): Double {
        var modifiedProbability = baseProbability
        
        // Trust level affects emotional transitions
        when {
            npcMemory.trustLevel > 0.7 -> modifiedProbability *= 1.2 // More positive transitions
            npcMemory.trustLevel < 0.3 -> modifiedProbability *= 0.8 // More resistant to positive
        }
        
        // Quest state affects emotional volatility
        when (questState) {
            QuestState.INTRO -> modifiedProbability *= 0.8 // Less volatile at start
            QuestState.IN_PROGRESS -> modifiedProbability *= 1.0 // Normal
            QuestState.COMPLETION -> modifiedProbability *= 1.3 // More emotional at completion
            QuestState.FAILURE -> modifiedProbability *= 1.4 // High emotional impact on failure
        }
        
        return modifiedProbability.coerceIn(0.0, 1.0)
    }
    
    private fun applyEmotionalDrift(
        currentEmotion: EmotionalState,
        npcMemory: NPCMemory
    ): EmotionalState {
        // NPCs gradually drift toward neutral based on trust and time
        val driftProbability = (1.0 - npcMemory.trustLevel) * 0.1
        
        if (Random.nextDouble() < driftProbability) {
            return when (currentEmotion) {
                EmotionalState.BITTER -> if (Random.nextDouble() < 0.3) EmotionalState.RESIGNED else currentEmotion
                EmotionalState.WRATHFUL -> if (Random.nextDouble() < 0.2) EmotionalState.BITTER else currentEmotion
                EmotionalState.FEARFUL -> if (Random.nextDouble() < 0.4) EmotionalState.NEUTRAL else currentEmotion
                else -> currentEmotion
            }
        }
        
        return currentEmotion
    }
    
    private fun generateContextualDialogue(
        emotion: EmotionalState,
        playerChoice: PlayerChoice,
        questState: QuestState,
        npcMemory: NPCMemory
    ): String {
        val baseTemplates = getDialogueTemplates(emotion, questState)
        val choiceModifiers = getChoiceModifiers(playerChoice, npcMemory)
        
        return enhanceDialogueWithContext(
            baseTemplates.random(),
            choiceModifiers,
            npcMemory
        )
    }
    
    private fun getDialogueTemplates(
        emotion: EmotionalState,
        questState: QuestState
    ): List<String> {
        return when (emotion) {
            EmotionalState.HOPEFUL -> when (questState) {
                QuestState.INTRO -> listOf(
                    "Perhaps together we can accomplish something great.",
                    "I sense potential in this endeavor.",
                    "There's a spark of possibility here."
                )
                QuestState.IN_PROGRESS -> listOf(
                    "We're making real progress - I can feel it!",
                    "Each step brings us closer to our goal.",
                    "Hope grows stronger with every success."
                )
                QuestState.COMPLETION -> listOf(
                    "We did it! I knew we could succeed together.",
                    "This victory proves what hope can achieve.",
                    "Our perseverance has finally paid off."
                )
                QuestState.FAILURE -> listOf(
                    "Even in defeat, I believe we can try again.",
                    "This setback won't extinguish my hope.",
                    "Perhaps another path will open for us."
                )
            }
            EmotionalState.BITTER -> when (questState) {
                QuestState.INTRO -> listOf(
                    "Another grand promise? I've heard them all before.",
                    "Hope is a luxury I can no longer afford.",
                    "Your words ring hollow to these old ears."
                )
                QuestState.IN_PROGRESS -> listOf(
                    "Progress? Or are we just prolonging the inevitable?",
                    "I've seen too many ventures crumble to trust easily.",
                    "Each step forward feels like two steps back."
                )
                QuestState.COMPLETION -> listOf(
                    "Success... it tastes strange after so much disappointment.",
                    "Perhaps I was wrong to doubt, though it pains me to admit.",
                    "One victory doesn't erase a lifetime of betrayals."
                )
                QuestState.FAILURE -> listOf(
                    "Of course. Another disappointment in an endless chain.",
                    "I should have known better than to expect different.",
                    "Failure follows me like a faithful shadow."
                )
            }
            EmotionalState.WRATHFUL -> when (questState) {
                QuestState.INTRO -> listOf(
                    "Speak quickly before my patience burns away!",
                    "Another's request means little to my fury!",
                    "What would you have me do in my rage?"
                )
                QuestState.IN_PROGRESS -> listOf(
                    "Good! Let our enemies feel the heat of our wrath!",
                    "Progress fueled by righteous anger burns brightest!",
                    "Each victory is another coal on the fire of justice!"
                )
                QuestState.COMPLETION -> listOf(
                    "Justice is served! My fury finds satisfaction at last!",
                    "The guilty have faced their reckoning!",
                    "Wrath channeled toward purpose burns truest!"
                )
                QuestState.FAILURE -> listOf(
                    "Failure only feeds the flames of my anger!",
                    "Incompetence kindles fury beyond measure!",
                    "This setback demands twice the vengeance!"
                )
            }
            // Add other emotional states...
            else -> listOf("I acknowledge your words.")
        }
    }
    
    private fun getChoiceModifiers(
        playerChoice: PlayerChoice,
        npcMemory: NPCMemory
    ): Map<String, String> {
        return when (playerChoice) {
            PlayerChoice.HONORABLE -> mapOf(
                "tone" to "respectful",
                "address" to if (npcMemory.trustLevel > 0.6) "my friend" else "honored one",
                "manner" to "with dignity"
            )
            PlayerChoice.RUTHLESS -> mapOf(
                "tone" to "calculating",
                "address" to "you",
                "manner" to "with cold efficiency"
            )
            PlayerChoice.GREEDY -> mapOf(
                "tone" to "transactional",
                "address" to "business partner",
                "manner" to "with clear expectations"
            )
            PlayerChoice.COMPASSIONATE -> mapOf(
                "tone" to "warm",
                "address" to if (npcMemory.trustLevel > 0.7) "dear friend" else "kind soul",
                "manner" to "with gentle understanding"
            )
            PlayerChoice.NEUTRAL -> mapOf(
                "tone" to "neutral",
                "address" to "traveler",
                "manner" to "straightforwardly"
            )
        }
    }
    
    private fun enhanceDialogueWithContext(
        baseTemplate: String,
        choiceModifiers: Map<String, String>,
        npcMemory: NPCMemory
    ): String {
        var enhanced = baseTemplate
        
        // Add memory references for high-trust NPCs
        if (npcMemory.trustLevel > 0.8 && npcMemory.significantMemories.isNotEmpty()) {
            val memoryReference = npcMemory.significantMemories.last()
            enhanced = "Remembering ${memoryReference.description}, $enhanced"
        }
        
        // Add emotional qualifiers based on trust
        when {
            npcMemory.trustLevel > 0.8 -> enhanced = enhanced.replace(".", " - and I trust you completely.")
            npcMemory.trustLevel < 0.3 -> enhanced = enhanced.replace(".", " - though I question your motives.")
        }
        
        return enhanced
    }
    
    private fun addEmotionalLayering(
        baseContent: String,
        previousEmotion: EmotionalState,
        newEmotion: EmotionalState,
        npcMemory: NPCMemory
    ): String {
        if (previousEmotion == newEmotion) {
            return baseContent // No emotional transition
        }
        
        // Add transition phrases for emotional shifts
        val transitionPhrase = when (previousEmotion to newEmotion) {
            EmotionalState.NEUTRAL to EmotionalState.HOPEFUL -> "A glimmer of hope stirs within me... "
            EmotionalState.HOPEFUL to EmotionalState.BITTER -> "My hope withers as I realize... "
            EmotionalState.BITTER to EmotionalState.WRATHFUL -> "Bitterness ignites into fury... "
            EmotionalState.FEARFUL to EmotionalState.HOPEFUL -> "Fear gives way to possibility... "
            EmotionalState.WRATHFUL to EmotionalState.RESIGNED -> "Anger cools to acceptance... "
            EmotionalState.LOYAL to EmotionalState.BETRAYED -> "My loyalty crumbles to ash... "
            else -> ""
        }
        
        return transitionPhrase + baseContent
    }
    
    private fun calculateMemoryImprint(
        playerAction: String,
        playerChoice: PlayerChoice,
        emotion: EmotionalState,
        npcMemory: NPCMemory
    ): MemoryImprint {
        val significance = when {
            emotion == EmotionalState.BETRAYED -> 1.0
            emotion == EmotionalState.JOYFUL -> 0.9
            emotion == EmotionalState.WRATHFUL -> 0.8
            playerChoice == PlayerChoice.RUTHLESS -> 0.7
            else -> 0.5
        }
        
        val emotionalWeight = when (emotion) {
            EmotionalState.BETRAYED, EmotionalState.JOYFUL -> 1.0
            EmotionalState.WRATHFUL, EmotionalState.LOYAL -> 0.8
            EmotionalState.HOPEFUL, EmotionalState.BITTER -> 0.6
            else -> 0.4
        }
        
        return MemoryImprint(
            significance = significance,
            emotionalWeight = emotionalWeight,
            decayRate = if (emotionalWeight > 0.8) 0.05 else 0.1,
            associations = listOf(playerAction, playerChoice.name.lowercase())
        )
    }
    
    private fun calculateFutureInfluence(
        previousEmotion: EmotionalState,
        newEmotion: EmotionalState,
        playerChoice: PlayerChoice,
        npcMemory: NPCMemory
    ): FutureInfluence {
        val trustImpact = when (newEmotion) {
            EmotionalState.LOYAL -> 0.3
            EmotionalState.HOPEFUL -> 0.1
            EmotionalState.BETRAYED -> -0.5
            EmotionalState.BITTER -> -0.2
            EmotionalState.WRATHFUL -> -0.3
            else -> 0.0
        }
        
        val relationshipShift = when (previousEmotion to newEmotion) {
            EmotionalState.NEUTRAL to EmotionalState.HOPEFUL -> 0.2
            EmotionalState.HOPEFUL to EmotionalState.LOYAL -> 0.3
            EmotionalState.LOYAL to EmotionalState.BETRAYED -> -0.8
            else -> 0.0
        }
        
        return FutureInfluence(
            trustImpact = trustImpact,
            relationshipShift = relationshipShift,
            questConsequences = calculateQuestConsequences(newEmotion),
            unlocksPotential = calculateUnlockedPotential(newEmotion, npcMemory)
        )
    }
    
    private fun calculateQuestConsequences(emotion: EmotionalState): List<String> {
        return when (emotion) {
            EmotionalState.LOYAL -> listOf("enhanced_rewards", "secret_information", "future_alliance")
            EmotionalState.BETRAYED -> listOf("reduced_rewards", "hostile_faction", "quest_complications")
            EmotionalState.JOYFUL -> listOf("celebration_event", "bonus_experience", "positive_reputation")
            EmotionalState.WRATHFUL -> listOf("aggressive_options", "combat_advantage", "intimidation_bonus")
            else -> emptyList()
        }
    }
    
    private fun calculateUnlockedPotential(
        emotion: EmotionalState,
        npcMemory: NPCMemory
    ): List<String> {
        val potential = mutableListOf<String>()
        
        if (emotion == EmotionalState.LOYAL && npcMemory.trustLevel > 0.8) {
            potential.add("deep_personal_quest")
        }
        
        if (emotion == EmotionalState.WRATHFUL) {
            potential.add("revenge_questline")
        }
        
        if (emotion == EmotionalState.JOYFUL) {
            potential.add("celebration_activities")
        }
        
        return potential
    }
    
    private fun generateVisualCues(
        emotion: EmotionalState,
        emotionalWeight: Double
    ): VisualCues {
        val intensity = if (emotionalWeight > 0.7) "high" else "moderate"
        
        return when (emotion) {
            EmotionalState.HOPEFUL -> VisualCues(
                facialExpression = "gentle_smile",
                bodyLanguage = "open_posture",
                eyeContact = "direct_and_warm",
                gestureIntensity = 0.6
            )
            EmotionalState.BITTER -> VisualCues(
                facialExpression = "slight_frown",
                bodyLanguage = "crossed_arms",
                eyeContact = "indirect_glances",
                gestureIntensity = 0.3
            )
            EmotionalState.WRATHFUL -> VisualCues(
                facialExpression = "intense_scowl",
                bodyLanguage = "aggressive_stance",
                eyeContact = "piercing_stare",
                gestureIntensity = 0.9
            )
            EmotionalState.LOYAL -> VisualCues(
                facialExpression = "confident_smile",
                bodyLanguage = "attentive_posture",
                eyeContact = "steady_and_trusting",
                gestureIntensity = 0.7
            )
            else -> VisualCues(
                facialExpression = "neutral",
                bodyLanguage = "relaxed",
                eyeContact = "normal",
                gestureIntensity = 0.5
            )
        }
    }
    
    private fun generateAudioHints(
        emotion: EmotionalState,
        trustImpact: Double
    ): AudioHints {
        return when (emotion) {
            EmotionalState.HOPEFUL -> AudioHints(
                toneOfVoice = "optimistic",
                speechPace = "moderate",
                volume = "normal",
                emotionalUndertone = "warmth"
            )
            EmotionalState.BITTER -> AudioHints(
                toneOfVoice = "cynical",
                speechPace = "slow",
                volume = "quiet",
                emotionalUndertone = "disappointment"
            )
            EmotionalState.WRATHFUL -> AudioHints(
                toneOfVoice = "aggressive",
                speechPace = "fast",
                volume = "loud",
                emotionalUndertone = "anger"
            )
            EmotionalState.FEARFUL -> AudioHints(
                toneOfVoice = "trembling",
                speechPace = "quick",
                volume = "soft",
                emotionalUndertone = "anxiety"
            )
            else -> AudioHints(
                toneOfVoice = "neutral",
                speechPace = "normal",
                volume = "normal",
                emotionalUndertone = "calm"
            )
        }
    }
    
    private fun calculateEmotionalIntensity(
        previousEmotion: EmotionalState,
        newEmotion: EmotionalState
    ): Double {
        return when {
            previousEmotion != newEmotion -> 0.8 // High intensity during transitions
            newEmotion in listOf(EmotionalState.WRATHFUL, EmotionalState.JOYFUL, EmotionalState.BETRAYED) -> 0.9
            newEmotion in listOf(EmotionalState.HOPEFUL, EmotionalState.BITTER) -> 0.6
            else -> 0.4
        }
    }
    
    private fun recordInteraction(
        npcId: String,
        playerAction: String,
        playerChoice: PlayerChoice,
        questState: QuestState,
        significance: Double
    ) {
        val interaction = InteractionContext(
            npcId = npcId,
            playerAction = playerAction,
            playerChoice = playerChoice,
            questState = questState,
            timestamp = System.currentTimeMillis(),
            emotionalImpact = significance,
            memorySignificance = significance
        )
        
        val currentInteractions = _recentInteractions.value.toMutableList()
        currentInteractions.add(interaction)
        
        // Keep only recent interactions (last 50)
        if (currentInteractions.size > 50) {
            currentInteractions.removeAt(0)
        }
        
        _recentInteractions.value = currentInteractions
    }
}