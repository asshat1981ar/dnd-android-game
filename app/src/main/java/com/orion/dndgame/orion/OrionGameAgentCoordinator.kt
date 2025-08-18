package com.orion.dndgame.orion

import com.google.gson.Gson
import com.orion.dndgame.data.models.*
import com.orion.dndgame.eds.core.EmotionalEvent
import com.orion.dndgame.eds.core.EmotionalState
import com.orion.dndgame.network.OrionWebSocketClient
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Specialized coordinator for integrating D&D game with Orion's agent ecosystem
 * Handles real-time agent communication, consciousness updates, and dynamic storytelling
 */
@Singleton
class OrionGameAgentCoordinator @Inject constructor(
    private val orionClient: OrionWebSocketClient,
    private val gson: Gson
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private val _activeAgents = MutableStateFlow<List<OrionGameAgent>>(emptyList())
    val activeAgents: StateFlow<List<OrionGameAgent>> = _activeAgents.asStateFlow()
    
    private val _storyEvents = MutableSharedFlow<DynamicStoryEvent>()
    val storyEvents: SharedFlow<DynamicStoryEvent> = _storyEvents.asSharedFlow()
    
    private val agentSpecializations = mapOf(
        "GameMasterAgent" to AgentCapability.NARRATIVE_GENERATION,
        "ConsciousNPCAgent" to AgentCapability.NPC_CONSCIOUSNESS,
        "WorldBuilderAgent" to AgentCapability.WORLD_DYNAMICS,
        "CombatCoordinatorAgent" to AgentCapability.COMBAT_AI,
        "CharacterManagerAgent" to AgentCapability.CHARACTER_DEVELOPMENT,
        "RulesLawyerAgent" to AgentCapability.RULES_ENFORCEMENT
    )
    
    /**
     * Initialize specialized agents for D&D game coordination
     */
    suspend fun initializeGameAgents(): Boolean {
        return try {
            // Request initialization of specialized D&D agents
            val initRequest = OrionAgentRequest(
                type = "initialize_dnd_agents",
                capability = "dnd_game_coordination",
                data = mapOf(
                    "game_type" to "dnd_5e",
                    "features" to listOf(
                        "emotional_dialogue_system",
                        "consciousness_integration",
                        "dynamic_storytelling",
                        "real_time_multiplayer"
                    ),
                    "agent_specializations" to agentSpecializations.keys.toList()
                )
            )
            
            val response = orionClient.sendOrionTask(
                initRequest.type,
                initRequest.capability,
                initRequest.data
            )
            
            if (response.status == "success") {
                startAgentMonitoring()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Request dynamic NPC consciousness updates from Orion agents
     */
    suspend fun requestNPCConsciousnessUpdate(
        npc: NPC,
        recentEvents: List<EmotionalEvent>,
        gameContext: GameContext
    ): NPCConsciousnessUpdate? {
        return try {
            val request = OrionAgentRequest(
                type = "npc_consciousness_update",
                capability = "consciousness_integration",
                data = mapOf(
                    "npc_id" to npc.id,
                    "npc_data" to mapOf(
                        "name" to npc.name,
                        "personality" to npc.personality,
                        "emotional_profile" to npc.emotionalProfile,
                        "consciousness_level" to npc.consciousnessLevel
                    ),
                    "recent_events" to recentEvents,
                    "game_context" to gameContext,
                    "projection_chimera_integration" to true
                )
            )
            
            val response = orionClient.sendOrionTask(
                request.type,
                request.capability,
                request.data
            )
            
            response.result?.let { result ->
                gson.fromJson(result.toString(), NPCConsciousnessUpdate::class.java)
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Request dynamic story generation from Orion's GameMaster agent
     */
    suspend fun requestDynamicStoryGeneration(
        currentState: GameState,
        playerActions: List<PlayerAction>,
        storyContext: StoryContext
    ): DynamicStoryResponse? {
        return try {
            val request = OrionAgentRequest(
                type = "dynamic_story_generation",
                capability = "narrative_generation",
                data = mapOf(
                    "game_state" to currentState,
                    "player_actions" to playerActions,
                    "story_context" to storyContext,
                    "emotional_context" to extractEmotionalContext(currentState),
                    "quest_dependencies" to analyzeQuestDependencies(currentState)
                )
            )
            
            val response = orionClient.sendOrionTask(
                request.type,
                request.capability,
                request.data
            )
            
            response.result?.let { result ->
                val storyResponse = gson.fromJson(result.toString(), DynamicStoryResponse::class.java)
                
                // Emit story event for other systems to react
                _storyEvents.emit(
                    DynamicStoryEvent(
                        type = "story_generation",
                        narrative = storyResponse.narrative,
                        consequences = storyResponse.consequences,
                        timestamp = System.currentTimeMillis()
                    )
                )
                
                storyResponse
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Request AI-driven combat assistance from Orion agents
     */
    suspend fun requestCombatAIAssistance(
        combatState: com.orion.dndgame.combat.CombatState,
        aiDifficulty: Float = 0.7f
    ): CombatAIResponse? {
        return try {
            val request = OrionAgentRequest(
                type = "combat_ai_assistance",
                capability = "combat_ai",
                data = mapOf(
                    "combat_state" to combatState,
                    "ai_difficulty" to aiDifficulty,
                    "tactical_analysis" to true,
                    "emotional_integration" to true
                )
            )
            
            val response = orionClient.sendOrionTask(
                request.type,
                request.capability,
                request.data
            )
            
            response.result?.let { result ->
                gson.fromJson(result.toString(), CombatAIResponse::class.java)
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Coordinate multi-agent quest progression
     */
    suspend fun coordinateQuestProgression(
        quest: Quest,
        playerChoices: List<PlayerChoice>,
        npcStates: Map<String, NPC>
    ): QuestProgressionResult? {
        return try {
            val request = OrionAgentRequest(
                type = "quest_progression_coordination",
                capability = "world_dynamics",
                data = mapOf(
                    "quest" to quest,
                    "player_choices" to playerChoices,
                    "npc_states" to npcStates,
                    "emotional_analysis" to true,
                    "consciousness_integration" to true,
                    "multi_agent_coordination" to true
                )
            )
            
            val response = orionClient.sendOrionTask(
                request.type,
                request.capability,
                request.data
            )
            
            response.result?.let { result ->
                gson.fromJson(result.toString(), QuestProgressionResult::class.java)
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Request character development guidance from Orion agents
     */
    suspend fun requestCharacterDevelopmentGuidance(
        character: Character,
        recentActions: List<CharacterAction>,
        storyMilestones: List<StoryMilestone>
    ): CharacterDevelopmentGuidance? {
        return try {
            val request = OrionAgentRequest(
                type = "character_development_guidance",
                capability = "character_development",
                data = mapOf(
                    "character" to character,
                    "recent_actions" to recentActions,
                    "story_milestones" to storyMilestones,
                    "emotional_growth" to true,
                    "skill_recommendations" to true
                )
            )
            
            val response = orionClient.sendOrionTask(
                request.type,
                request.capability,
                request.data
            )
            
            response.result?.let { result ->
                gson.fromJson(result.toString(), CharacterDevelopmentGuidance::class.java)
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Start monitoring Orion agents for real-time updates
     */
    private fun startAgentMonitoring() {
        scope.launch {
            orionClient.agentUpdates.collect { agentUpdate ->
                updateAgentStatus(agentUpdate)
            }
        }
        
        scope.launch {
            orionClient.systemMetrics.collect { metrics ->
                processSystemMetrics(metrics)
            }
        }
    }
    
    private fun updateAgentStatus(agentUpdate: com.orion.dndgame.network.AgentUpdate) {
        val currentAgents = _activeAgents.value.toMutableList()
        val agentIndex = currentAgents.indexOfFirst { it.id == agentUpdate.agentId }
        
        if (agentIndex >= 0) {
            currentAgents[agentIndex] = currentAgents[agentIndex].copy(
                status = agentUpdate.status,
                lastUpdate = agentUpdate.timestamp
            )
        } else {
            // New agent detected
            val specialization = agentSpecializations.entries
                .find { agentUpdate.agentId.contains(it.key) }?.value
                ?: AgentCapability.GENERAL
                
            currentAgents.add(
                OrionGameAgent(
                    id = agentUpdate.agentId,
                    name = agentUpdate.agentId,
                    capability = specialization,
                    status = agentUpdate.status,
                    lastUpdate = agentUpdate.timestamp
                )
            )
        }
        
        _activeAgents.value = currentAgents
    }
    
    private fun processSystemMetrics(metrics: com.orion.dndgame.network.SystemMetrics) {
        // Process system-wide metrics for performance optimization
        // Could be used to adjust AI difficulty, response times, etc.
    }
    
    private fun extractEmotionalContext(gameState: GameState): EmotionalContext {
        return EmotionalContext(
            dominantEmotions = gameState.nearbyNPCs.map { it.emotionalProfile.primaryState },
            relationshipTensions = gameState.nearbyNPCs.associate { 
                it.id to it.emotionalProfile.memory.getRelationshipWith("player")
            },
            averageIntensity = gameState.nearbyNPCs
                .map { it.emotionalProfile.intensity }
                .average()
                .toFloat()
        )
    }
    
    private fun analyzeQuestDependencies(gameState: GameState): QuestDependencies {
        return QuestDependencies(
            activeQuests = gameState.activeQuests.map { it.id },
            questConflicts = findQuestConflicts(gameState.activeQuests),
            npcInvolvement = mapNPCQuestInvolvement(gameState.activeQuests, gameState.nearbyNPCs)
        )
    }
    
    private fun findQuestConflicts(quests: List<Quest>): List<QuestConflict> {
        return quests.flatMap { quest1 ->
            quests.filter { quest2 -> 
                quest1.id != quest2.id && quest1.conflictingQuestIds.contains(quest2.id)
            }.map { quest2 ->
                QuestConflict(quest1.id, quest2.id, "direct_conflict")
            }
        }
    }
    
    private fun mapNPCQuestInvolvement(quests: List<Quest>, npcs: List<NPC>): Map<String, List<String>> {
        return npcs.associate { npc ->
            npc.id to quests.filter { quest ->
                quest.questGiverId == npc.id || quest.relatedNPCIds.contains(npc.id)
            }.map { it.id }
        }
    }
}

/**
 * Data classes for Orion integration
 */

data class OrionAgentRequest(
    val type: String,
    val capability: String,
    val data: Map<String, Any>
)

data class OrionGameAgent(
    val id: String,
    val name: String,
    val capability: AgentCapability,
    val status: String,
    val lastUpdate: Long
)

enum class AgentCapability {
    NARRATIVE_GENERATION,
    NPC_CONSCIOUSNESS,
    WORLD_DYNAMICS,
    COMBAT_AI,
    CHARACTER_DEVELOPMENT,
    RULES_ENFORCEMENT,
    GENERAL
}

data class NPCConsciousnessUpdate(
    val npcId: String,
    val newEmotionalState: EmotionalState,
    val consciousnessEvolution: Float,
    val behaviorChanges: List<String>,
    val memoryUpdates: List<String>,
    val narrativeInsights: String
)

data class DynamicStoryResponse(
    val narrative: String,
    val consequences: List<StoryConsequence>,
    val emergentElements: List<String>,
    val suggestedEvents: List<SuggestedEvent>
)

data class DynamicStoryEvent(
    val type: String,
    val narrative: String,
    val consequences: List<StoryConsequence>,
    val timestamp: Long
)

data class CombatAIResponse(
    val suggestedActions: List<SuggestedCombatAction>,
    val tacticalAnalysis: String,
    val difficultyAdjustments: Map<String, Float>,
    val emotionalImpacts: List<EmotionalCombatEvent>
)

data class QuestProgressionResult(
    val questUpdates: List<QuestUpdate>,
    val npcReactions: Map<String, NPCReaction>,
    val worldStateChanges: List<WorldStateChange>,
    val narrativeBranches: List<String>
)

data class CharacterDevelopmentGuidance(
    val skillRecommendations: List<SkillRecommendation>,
    val storyOpportunities: List<String>,
    val characterGrowthPaths: List<String>,
    val emotionalDevelopment: EmotionalGrowthSuggestion
)

data class GameContext(
    val location: String,
    val timeOfDay: String,
    val weather: String,
    val activeEvents: List<String>,
    val mood: String
)

data class EmotionalContext(
    val dominantEmotions: List<EmotionalState>,
    val relationshipTensions: Map<String, Float>,
    val averageIntensity: Float
)

data class QuestDependencies(
    val activeQuests: List<String>,
    val questConflicts: List<QuestConflict>,
    val npcInvolvement: Map<String, List<String>>
)

data class QuestConflict(
    val quest1Id: String,
    val quest2Id: String,
    val conflictType: String
)

data class PlayerAction(
    val type: String,
    val description: String,
    val timestamp: Long,
    val consequences: List<String>
)

data class StoryConsequence(
    val type: String,
    val description: String,
    val severity: Float,
    val affectedEntities: List<String>
)

data class SuggestedEvent(
    val type: String,
    val description: String,
    val probability: Float,
    val requirements: List<String>
)

data class SuggestedCombatAction(
    val actionType: String,
    val description: String,
    val tacticalValue: Float,
    val riskLevel: Float
)

data class EmotionalCombatEvent(
    val npcId: String,
    val emotionalChange: EmotionalState,
    val intensity: Float,
    val trigger: String
)

data class QuestUpdate(
    val questId: String,
    val progressChange: Float,
    val statusChange: String?,
    val objectiveUpdates: List<String>
)

data class NPCReaction(
    val npcId: String,
    val emotionalReaction: EmotionalState,
    val dialogueResponse: String,
    val relationshipChange: Float
)

data class WorldStateChange(
    val type: String,
    val description: String,
    val affectedAreas: List<String>,
    val permanence: Boolean
)

data class CharacterAction(
    val type: String,
    val description: String,
    val skillsUsed: List<String>,
    val outcome: String,
    val timestamp: Long
)

data class StoryMilestone(
    val name: String,
    val description: String,
    val significance: Float,
    val achievedAt: Long
)

data class SkillRecommendation(
    val skill: String,
    val reason: String,
    val priority: Float,
    val storyJustification: String
)

data class EmotionalGrowthSuggestion(
    val currentTraits: List<String>,
    val growthOpportunities: List<String>,
    val emotionalChallenges: List<String>
)