package com.orion.dndgame.quest

import com.orion.dndgame.data.models.Quest
import com.orion.dndgame.eds.core.EmotionalState
import com.orion.dndgame.eds.core.NPCMemory
import com.orion.dndgame.eds.core.PlayerChoice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Dynamic Quest Adaptation System that modifies quests based on NPC emotional states
 * and player choices to create emergent storytelling experiences
 */
@Singleton
class DynamicQuestAdaptationSystem @Inject constructor() {
    
    private val _activeAdaptations = MutableStateFlow<List<QuestAdaptation>>(emptyList())
    val activeAdaptations: StateFlow<List<QuestAdaptation>> = _activeAdaptations.asStateFlow()
    
    private val _questConsequences = MutableStateFlow<Map<String, List<Consequence>>>(emptyMap())
    val questConsequences: StateFlow<Map<String, List<Consequence>>> = _questConsequences.asStateFlow()
    
    data class QuestAdaptation(
        val questId: String,
        val adaptationType: AdaptationType,
        val trigger: AdaptationTrigger,
        val modifications: List<QuestModification>,
        val emotionalContext: EmotionalContext,
        val timestamp: Long,
        val priority: Int
    )
    
    enum class AdaptationType {
        NARRATIVE_BRANCH,
        OBJECTIVE_MODIFICATION,
        NPC_BEHAVIOR_CHANGE,
        REWARD_ADJUSTMENT,
        DIFFICULTY_SCALING,
        ENVIRONMENTAL_CHANGE,
        RELATIONSHIP_CONSEQUENCE
    }
    
    data class AdaptationTrigger(
        val npcId: String,
        val emotionalTransition: EmotionalTransition,
        val playerAction: String,
        val trustLevel: Double,
        val significantEvent: Boolean
    )
    
    data class EmotionalTransition(
        val fromState: EmotionalState,
        val toState: EmotionalState,
        val intensity: Double
    )
    
    data class QuestModification(
        val type: ModificationType,
        val target: String,
        val change: String,
        val impact: ImpactLevel
    )
    
    enum class ModificationType {
        ADD_OBJECTIVE,
        MODIFY_OBJECTIVE,
        REMOVE_OBJECTIVE,
        CHANGE_DIALOGUE,
        ALTER_REWARD,
        MODIFY_DIFFICULTY,
        ADD_COMPLICATION,
        CREATE_OPPORTUNITY
    }
    
    enum class ImpactLevel {
        MINOR,      // Cosmetic changes
        MODERATE,   // Meaningful but not quest-changing
        MAJOR,      // Significant quest alterations
        CRITICAL    // Quest-defining changes
    }
    
    data class EmotionalContext(
        val dominantEmotion: EmotionalState,
        val emotionalHistory: List<EmotionalState>,
        val relationshipTrend: RelationshipTrend,
        val memorySignificance: Double
    )
    
    enum class RelationshipTrend {
        IMPROVING,
        DECLINING,
        STABLE,
        VOLATILE
    }
    
    data class Consequence(
        val type: ConsequenceType,
        val description: String,
        val impact: ImpactLevel,
        val affectedNPCs: List<String>,
        val questChainEffects: List<String>
    )
    
    enum class ConsequenceType {
        RELATIONSHIP_CHANGE,
        REPUTATION_SHIFT,
        RESOURCE_GAIN,
        RESOURCE_LOSS,
        ACCESS_GRANTED,
        ACCESS_DENIED,
        STORY_BRANCH,
        CHARACTER_DEVELOPMENT
    }
    
    /**
     * Analyze NPC emotional state and adapt quest accordingly
     */
    suspend fun adaptQuestBasedOnEmotion(
        quest: Quest,
        npcId: String,
        emotionalTransition: EmotionalTransition,
        playerChoice: PlayerChoice,
        npcMemory: NPCMemory
    ): QuestAdaptation? {
        
        val trigger = AdaptationTrigger(
            npcId = npcId,
            emotionalTransition = emotionalTransition,
            playerAction = playerChoice.name,
            trustLevel = npcMemory.trustLevel,
            significantEvent = emotionalTransition.intensity > 0.7
        )
        
        val emotionalContext = EmotionalContext(
            dominantEmotion = emotionalTransition.toState,
            emotionalHistory = extractEmotionalHistory(npcMemory),
            relationshipTrend = calculateRelationshipTrend(npcMemory),
            memorySignificance = npcMemory.significantMemories.maxOfOrNull { it.significance } ?: 0.5
        )
        
        val adaptationType = determineAdaptationType(emotionalTransition, emotionalContext)
        val modifications = generateQuestModifications(quest, adaptationType, trigger, emotionalContext)
        
        if (modifications.isEmpty()) return null
        
        val adaptation = QuestAdaptation(
            questId = quest.id,
            adaptationType = adaptationType,
            trigger = trigger,
            modifications = modifications,
            emotionalContext = emotionalContext,
            timestamp = System.currentTimeMillis(),
            priority = calculateAdaptationPriority(emotionalTransition, emotionalContext)
        )
        
        // Store the adaptation
        val currentAdaptations = _activeAdaptations.value.toMutableList()
        currentAdaptations.add(adaptation)
        _activeAdaptations.value = currentAdaptations
        
        // Generate consequences
        generateQuestConsequences(adaptation)
        
        return adaptation
    }
    
    private fun extractEmotionalHistory(npcMemory: NPCMemory): List<EmotionalState> {
        return npcMemory.significantMemories
            .sortedBy { it.timestamp }
            .mapNotNull { memory ->
                // Extract emotional state from memory context
                when {
                    memory.description.contains("betrayed", ignoreCase = true) -> EmotionalState.BETRAYED
                    memory.description.contains("angry", ignoreCase = true) -> EmotionalState.WRATHFUL
                    memory.description.contains("happy", ignoreCase = true) -> EmotionalState.JOYFUL
                    memory.description.contains("hopeful", ignoreCase = true) -> EmotionalState.HOPEFUL
                    memory.description.contains("bitter", ignoreCase = true) -> EmotionalState.BITTER
                    memory.description.contains("loyal", ignoreCase = true) -> EmotionalState.LOYAL
                    else -> null
                }
            }
            .takeLast(5) // Keep recent history
    }
    
    private fun calculateRelationshipTrend(npcMemory: NPCMemory): RelationshipTrend {
        val recentMemories = npcMemory.significantMemories.takeLast(3)
        if (recentMemories.size < 2) return RelationshipTrend.STABLE
        
        val trustChanges = recentMemories.zipWithNext { first, second ->
            // Simulate trust changes based on memory significance and emotional content
            when {
                second.description.contains("betrayed") -> -0.3
                second.description.contains("helped") -> 0.2
                second.description.contains("loyal") -> 0.25
                second.description.contains("disappointed") -> -0.15
                else -> 0.0
            }
        }
        
        val totalChange = trustChanges.sum()
        val volatility = trustChanges.map { kotlin.math.abs(it) }.average()
        
        return when {
            volatility > 0.2 -> RelationshipTrend.VOLATILE
            totalChange > 0.1 -> RelationshipTrend.IMPROVING
            totalChange < -0.1 -> RelationshipTrend.DECLINING
            else -> RelationshipTrend.STABLE
        }
    }
    
    private fun determineAdaptationType(
        emotionalTransition: EmotionalTransition,
        context: EmotionalContext
    ): AdaptationType {
        return when (emotionalTransition.toState) {
            EmotionalState.BETRAYED -> when (context.relationshipTrend) {
                RelationshipTrend.DECLINING -> AdaptationType.RELATIONSHIP_CONSEQUENCE
                else -> AdaptationType.NARRATIVE_BRANCH
            }
            EmotionalState.LOYAL -> AdaptationType.REWARD_ADJUSTMENT
            EmotionalState.WRATHFUL -> AdaptationType.DIFFICULTY_SCALING
            EmotionalState.JOYFUL -> AdaptationType.OBJECTIVE_MODIFICATION
            EmotionalState.FEARFUL -> AdaptationType.ENVIRONMENTAL_CHANGE
            EmotionalState.BITTER -> AdaptationType.NPC_BEHAVIOR_CHANGE
            EmotionalState.HOPEFUL -> AdaptationType.OBJECTIVE_MODIFICATION
            EmotionalState.RESIGNED -> AdaptationType.NARRATIVE_BRANCH
            EmotionalState.NEUTRAL -> AdaptationType.NPC_BEHAVIOR_CHANGE
        }
    }
    
    private fun generateQuestModifications(
        quest: Quest,
        adaptationType: AdaptationType,
        trigger: AdaptationTrigger,
        context: EmotionalContext
    ): List<QuestModification> {
        return when (adaptationType) {
            AdaptationType.NARRATIVE_BRANCH -> generateNarrativeBranches(quest, trigger, context)
            AdaptationType.OBJECTIVE_MODIFICATION -> generateObjectiveModifications(quest, trigger, context)
            AdaptationType.NPC_BEHAVIOR_CHANGE -> generateBehaviorChanges(quest, trigger, context)
            AdaptationType.REWARD_ADJUSTMENT -> generateRewardAdjustments(quest, trigger, context)
            AdaptationType.DIFFICULTY_SCALING -> generateDifficultyScaling(quest, trigger, context)
            AdaptationType.ENVIRONMENTAL_CHANGE -> generateEnvironmentalChanges(quest, trigger, context)
            AdaptationType.RELATIONSHIP_CONSEQUENCE -> generateRelationshipConsequences(quest, trigger, context)
        }
    }
    
    private fun generateNarrativeBranches(
        quest: Quest,
        trigger: AdaptationTrigger,
        context: EmotionalContext
    ): List<QuestModification> {
        val modifications = mutableListOf<QuestModification>()
        
        when (context.dominantEmotion) {
            EmotionalState.BETRAYED -> {
                modifications.add(QuestModification(
                    type = ModificationType.ADD_OBJECTIVE,
                    target = "narrative",
                    change = "NPC refuses further cooperation, alternate path required",
                    impact = ImpactLevel.MAJOR
                ))
                
                modifications.add(QuestModification(
                    type = ModificationType.CHANGE_DIALOGUE,
                    target = trigger.npcId,
                    change = "Hostile dialogue options, mentions of past betrayal",
                    impact = ImpactLevel.MODERATE
                ))
            }
            
            EmotionalState.LOYAL -> {
                modifications.add(QuestModification(
                    type = ModificationType.CREATE_OPPORTUNITY,
                    target = "narrative",
                    change = "NPC offers additional assistance or secret information",
                    impact = ImpactLevel.MODERATE
                ))
            }
            
            EmotionalState.RESIGNED -> {
                modifications.add(QuestModification(
                    type = ModificationType.MODIFY_OBJECTIVE,
                    target = "main_objective",
                    change = "NPC becomes indifferent, player must find new motivation",
                    impact = ImpactLevel.MAJOR
                ))
            }
            
            else -> {}
        }
        
        return modifications
    }
    
    private fun generateObjectiveModifications(
        quest: Quest,
        trigger: AdaptationTrigger,
        context: EmotionalContext
    ): List<QuestModification> {
        val modifications = mutableListOf<QuestModification>()
        
        when (context.dominantEmotion) {
            EmotionalState.JOYFUL -> {
                modifications.add(QuestModification(
                    type = ModificationType.ADD_OBJECTIVE,
                    target = "bonus_objective",
                    change = "Celebration quest: organize festival or gathering",
                    impact = ImpactLevel.MINOR
                ))
            }
            
            EmotionalState.HOPEFUL -> {
                modifications.add(QuestModification(
                    type = ModificationType.MODIFY_OBJECTIVE,
                    target = "main_objective",
                    change = "Enhanced objective with greater potential impact",
                    impact = ImpactLevel.MODERATE
                ))
            }
            
            EmotionalState.WRATHFUL -> {
                modifications.add(QuestModification(
                    type = ModificationType.ADD_OBJECTIVE,
                    target = "vengeance_objective",
                    change = "Seek revenge against those who wronged the NPC",
                    impact = ImpactLevel.MAJOR
                ))
            }
            
            else -> {}
        }
        
        return modifications
    }
    
    private fun generateBehaviorChanges(
        quest: Quest,
        trigger: AdaptationTrigger,
        context: EmotionalContext
    ): List<QuestModification> {
        return listOf(
            QuestModification(
                type = ModificationType.CHANGE_DIALOGUE,
                target = trigger.npcId,
                change = "NPC behavior reflects ${context.dominantEmotion.name.lowercase()} state",
                impact = ImpactLevel.MODERATE
            )
        )
    }
    
    private fun generateRewardAdjustments(
        quest: Quest,
        trigger: AdaptationTrigger,
        context: EmotionalContext
    ): List<QuestModification> {
        val modifications = mutableListOf<QuestModification>()
        
        when (context.relationshipTrend) {
            RelationshipTrend.IMPROVING -> {
                modifications.add(QuestModification(
                    type = ModificationType.ALTER_REWARD,
                    target = "quest_rewards",
                    change = "Increased rewards due to improved relationship",
                    impact = ImpactLevel.MODERATE
                ))
            }
            
            RelationshipTrend.DECLINING -> {
                modifications.add(QuestModification(
                    type = ModificationType.ALTER_REWARD,
                    target = "quest_rewards",
                    change = "Reduced rewards due to damaged relationship",
                    impact = ImpactLevel.MODERATE
                ))
            }
            
            else -> {}
        }
        
        return modifications
    }
    
    private fun generateDifficultyScaling(
        quest: Quest,
        trigger: AdaptationTrigger,
        context: EmotionalContext
    ): List<QuestModification> {
        return when (context.dominantEmotion) {
            EmotionalState.WRATHFUL -> listOf(
                QuestModification(
                    type = ModificationType.ADD_COMPLICATION,
                    target = "quest_difficulty",
                    change = "NPC's anger creates additional challenges or enemies",
                    impact = ImpactLevel.MAJOR
                )
            )
            
            EmotionalState.FEARFUL -> listOf(
                QuestModification(
                    type = ModificationType.MODIFY_DIFFICULTY,
                    target = "quest_approach",
                    change = "Stealth and caution required due to NPC's fear",
                    impact = ImpactLevel.MODERATE
                )
            )
            
            else -> emptyList()
        }
    }
    
    private fun generateEnvironmentalChanges(
        quest: Quest,
        trigger: AdaptationTrigger,
        context: EmotionalContext
    ): List<QuestModification> {
        return listOf(
            QuestModification(
                type = ModificationType.MODIFY_OBJECTIVE,
                target = "quest_environment",
                change = "Environment reflects NPC's emotional state",
                impact = ImpactLevel.MINOR
            )
        )
    }
    
    private fun generateRelationshipConsequences(
        quest: Quest,
        trigger: AdaptationTrigger,
        context: EmotionalContext
    ): List<QuestModification> {
        return listOf(
            QuestModification(
                type = ModificationType.ADD_COMPLICATION,
                target = "relationship_network",
                change = "Other NPCs react to changed relationship status",
                impact = ImpactLevel.MAJOR
            )
        )
    }
    
    private fun calculateAdaptationPriority(
        emotionalTransition: EmotionalTransition,
        context: EmotionalContext
    ): Int {
        var priority = 50 // Base priority
        
        // High intensity emotions get higher priority
        priority += (emotionalTransition.intensity * 30).toInt()
        
        // Significant emotional changes get higher priority
        if (emotionalTransition.fromState != emotionalTransition.toState) {
            priority += 20
        }
        
        // Volatile relationships get higher priority
        if (context.relationshipTrend == RelationshipTrend.VOLATILE) {
            priority += 15
        }
        
        // High memory significance gets higher priority
        priority += (context.memorySignificance * 20).toInt()
        
        return priority.coerceIn(1, 100)
    }
    
    private fun generateQuestConsequences(adaptation: QuestAdaptation) {
        val consequences = mutableListOf<Consequence>()
        
        adaptation.modifications.forEach { modification ->
            when (modification.impact) {
                ImpactLevel.CRITICAL -> {
                    consequences.add(
                        Consequence(
                            type = ConsequenceType.STORY_BRANCH,
                            description = "Major story branch created due to ${adaptation.trigger.npcId}'s emotional state",
                            impact = ImpactLevel.CRITICAL,
                            affectedNPCs = listOf(adaptation.trigger.npcId),
                            questChainEffects = listOf("future_quest_availability", "ending_variation")
                        )
                    )
                }
                
                ImpactLevel.MAJOR -> {
                    consequences.add(
                        Consequence(
                            type = ConsequenceType.RELATIONSHIP_CHANGE,
                            description = "Significant relationship change affects quest outcomes",
                            impact = ImpactLevel.MAJOR,
                            affectedNPCs = listOf(adaptation.trigger.npcId),
                            questChainEffects = listOf("companion_availability", "dialogue_options")
                        )
                    )
                }
                
                else -> {}
            }
        }
        
        if (consequences.isNotEmpty()) {
            val currentConsequences = _questConsequences.value.toMutableMap()
            currentConsequences[adaptation.questId] = consequences
            _questConsequences.value = currentConsequences
        }
    }
    
    /**
     * Get available quest branches based on current emotional context
     */
    fun getAvailableQuestBranches(
        questId: String,
        npcEmotionalStates: Map<String, EmotionalState>
    ): List<QuestBranch> {
        val branches = mutableListOf<QuestBranch>()
        
        npcEmotionalStates.forEach { (npcId, emotion) ->
            val availableBranches = generateEmotionalQuestBranches(npcId, emotion)
            branches.addAll(availableBranches)
        }
        
        return branches.distinctBy { it.branchId }
    }
    
    private fun generateEmotionalQuestBranches(
        npcId: String,
        emotion: EmotionalState
    ): List<QuestBranch> {
        return when (emotion) {
            EmotionalState.LOYAL -> listOf(
                QuestBranch(
                    branchId = "${npcId}_loyalty_branch",
                    name = "Path of Loyalty",
                    description = "$npcId offers unwavering support",
                    requirements = listOf("High trust with $npcId"),
                    rewards = listOf("Loyal companion", "Enhanced abilities"),
                    consequences = listOf("Other factions may view you as allied")
                )
            )
            
            EmotionalState.BETRAYED -> listOf(
                QuestBranch(
                    branchId = "${npcId}_betrayal_branch",
                    name = "Broken Trust",
                    description = "$npcId becomes an obstacle or enemy",
                    requirements = listOf("Previously betrayed $npcId"),
                    rewards = listOf("Independent path", "Self-reliance bonus"),
                    consequences = listOf("Loss of NPC assistance", "Potential enemy")
                )
            )
            
            EmotionalState.WRATHFUL -> listOf(
                QuestBranch(
                    branchId = "${npcId}_wrath_branch",
                    name = "Vengeful Alliance",
                    description = "$npcId seeks revenge, you can help or hinder",
                    requirements = listOf("$npcId in wrathful state"),
                    rewards = listOf("Powerful ally in conflict", "Combat bonuses"),
                    consequences = listOf("Escalated conflicts", "Collateral damage")
                )
            )
            
            else -> emptyList()
        }
    }
    
    data class QuestBranch(
        val branchId: String,
        val name: String,
        val description: String,
        val requirements: List<String>,
        val rewards: List<String>,
        val consequences: List<String>
    )
    
    /**
     * Predict future quest adaptations based on current trajectory
     */
    fun predictFutureAdaptations(
        questId: String,
        npcId: String,
        currentContext: EmotionalContext
    ): List<PredictedAdaptation> {
        val predictions = mutableListOf<PredictedAdaptation>()
        
        // Predict based on relationship trend
        when (currentContext.relationshipTrend) {
            RelationshipTrend.IMPROVING -> {
                predictions.add(
                    PredictedAdaptation(
                        adaptationType = AdaptationType.REWARD_ADJUSTMENT,
                        probability = 0.8,
                        timeframe = "Within 2-3 interactions",
                        description = "Likely reward increase due to improving relationship"
                    )
                )
            }
            
            RelationshipTrend.DECLINING -> {
                predictions.add(
                    PredictedAdaptation(
                        adaptationType = AdaptationType.RELATIONSHIP_CONSEQUENCE,
                        probability = 0.7,
                        timeframe = "Within 1-2 interactions", 
                        description = "Potential quest complications due to relationship decline"
                    )
                )
            }
            
            RelationshipTrend.VOLATILE -> {
                predictions.add(
                    PredictedAdaptation(
                        adaptationType = AdaptationType.NARRATIVE_BRANCH,
                        probability = 0.6,
                        timeframe = "Immediate",
                        description = "Unpredictable quest developments due to volatile relationship"
                    )
                )
            }
            
            else -> {}
        }
        
        return predictions
    }
    
    data class PredictedAdaptation(
        val adaptationType: AdaptationType,
        val probability: Double,
        val timeframe: String,
        val description: String
    )
}