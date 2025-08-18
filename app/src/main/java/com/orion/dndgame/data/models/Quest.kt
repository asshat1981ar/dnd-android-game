package com.orion.dndgame.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.orion.dndgame.eds.dialogue.QuestStatus
import com.orion.dndgame.eds.dialogue.QuestType

/**
 * Quest model with dynamic storytelling integration
 */
@Entity(tableName = "quests")
@TypeConverters(QuestConverters::class)
data class Quest(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val type: QuestType,
    val status: QuestStatus,
    val priority: QuestPriority,
    
    // Quest givers and related NPCs
    val questGiverId: String,
    val relatedNPCIds: List<String> = emptyList(),
    
    // Requirements and objectives
    val objectives: List<QuestObjective>,
    val requirements: QuestRequirements,
    val level: Int, // Recommended level
    
    // Rewards
    val experienceReward: Int,
    val goldReward: Int,
    val itemRewards: List<String> = emptyList(), // Item IDs
    val reputationRewards: Map<String, Int> = emptyMap(), // Faction to reputation change
    
    // Location and context
    val location: String,
    val region: String,
    val timeLimit: Long? = null, // Timestamp when quest expires
    
    // Story and emotional context
    val storyContext: StoryContext,
    val emotionalThemes: List<String> = emptyList(),
    val narrativeBranches: List<NarrativeBranch> = emptyList(),
    
    // Progress tracking
    val progress: Float = 0.0f, // 0.0 to 1.0
    val currentPhase: Int = 0,
    val completionHistory: List<CompletionStep> = emptyList(),
    
    // Dependencies
    val prerequisiteQuestIds: List<String> = emptyList(),
    val unlocksQuestIds: List<String> = emptyList(),
    val conflictingQuestIds: List<String> = emptyList(),
    
    // Metadata
    val createdAt: Long,
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val lastUpdated: Long
)

/**
 * Individual quest objective
 */
data class QuestObjective(
    val id: String,
    val description: String,
    val type: ObjectiveType,
    val isCompleted: Boolean = false,
    val isOptional: Boolean = false,
    val targetId: String? = null, // NPC, item, or location ID
    val targetQuantity: Int = 1,
    val currentQuantity: Int = 0,
    val conditions: List<String> = emptyList(),
    val hints: List<String> = emptyList(),
    val emotionalWeight: Float = 1.0f // How much this objective affects emotional outcomes
)

/**
 * Quest requirements and restrictions
 */
data class QuestRequirements(
    val minimumLevel: Int = 1,
    val maximumLevel: Int? = null,
    val requiredClasses: List<CharacterClass> = emptyList(),
    val requiredRaces: List<CharacterRace> = emptyList(),
    val requiredSkills: Map<String, Int> = emptyMap(), // Skill to minimum level
    val requiredItems: List<String> = emptyList(),
    val requiredReputation: Map<String, Int> = emptyMap(), // Faction to minimum reputation
    val forbiddenStatuses: List<String> = emptyList() // Character statuses that prevent quest
)

/**
 * Story context for dynamic narrative generation
 */
data class StoryContext(
    val theme: StoryTheme,
    val mood: StoryMood,
    val urgency: UrgencyLevel,
    val scope: StoryScope,
    val moralAlignment: MoralAlignment,
    val stakes: StoryStakes,
    val genre: StoryGenre = StoryGenre.FANTASY_ADVENTURE
)

/**
 * Narrative branch for dynamic storytelling
 */
data class NarrativeBranch(
    val id: String,
    val name: String,
    val trigger: BranchTrigger,
    val condition: String, // Logical condition for activation
    val consequences: List<QuestConsequence>,
    val alternativeObjectives: List<QuestObjective> = emptyList(),
    val emotionalImpact: Map<String, Float> = emptyMap(), // NPC ID to emotional change
    val storyDescription: String
)

/**
 * Quest completion step for tracking progress
 */
data class CompletionStep(
    val timestamp: Long,
    val description: String,
    val objectiveId: String? = null,
    val playerChoice: String? = null,
    val emotionalConsequences: Map<String, Float> = emptyMap(), // NPC ID to relationship change
    val storyImpact: String? = null
)

/**
 * Consequence of quest actions
 */
data class QuestConsequence(
    val type: ConsequenceType,
    val targetId: String, // NPC, faction, or item ID
    val effect: String,
    val magnitude: Float,
    val description: String,
    val isPermanent: Boolean = true,
    val duration: Long? = null // For temporary effects
)

/**
 * Player choice in quest context
 */
data class PlayerChoice(
    val id: String,
    val text: String,
    val type: com.orion.dndgame.eds.dialogue.PlayerChoiceType,
    val emotionalImpact: Map<String, Float> = emptyMap(), // Emotional state to impact
    val questConsequences: List<QuestConsequence> = emptyList(),
    val requiresSkillCheck: SkillCheck? = null,
    val isAvailable: Boolean = true,
    val availabilityCondition: String? = null
)

/**
 * Skill check requirement
 */
data class SkillCheck(
    val skill: String,
    val difficulty: Int, // DC (Difficulty Class)
    val advantage: Boolean = false,
    val disadvantage: Boolean = false,
    val modifier: Int = 0
)

/**
 * Game session for tracking play time and events
 */
@Entity(tableName = "game_sessions")
@TypeConverters(GameSessionConverters::class)
data class GameSession(
    @PrimaryKey
    val id: String,
    val characterId: String,
    val startTime: Long,
    val endTime: Long? = null,
    val isActive: Boolean = true,
    
    // Session statistics
    val experienceGained: Int = 0,
    val goldGained: Int = 0,
    val questsCompleted: Int = 0,
    val npcsInteracted: List<String> = emptyList(),
    val locationsVisited: List<String> = emptyList(),
    
    // Story events
    val storyEvents: List<StoryEvent> = emptyList(),
    val emotionalHighlights: List<EmotionalHighlight> = emptyList(),
    
    // Performance metrics
    val decisionsCount: Int = 0,
    val averageDecisionTime: Float = 0.0f,
    val creativityScore: Float = 0.0f, // Based on unique choices and interactions
    val empathyScore: Float = 0.0f // Based on emotional awareness in choices
)

/**
 * Significant story event
 */
data class StoryEvent(
    val timestamp: Long,
    val type: EventType,
    val description: String,
    val involvedCharacters: List<String> = emptyList(),
    val questId: String? = null,
    val emotionalImpact: Float = 0.0f,
    val storySignificance: Float = 1.0f
)

/**
 * Emotional highlight from the session
 */
data class EmotionalHighlight(
    val timestamp: Long,
    val npcId: String,
    val emotionalState: com.orion.dndgame.eds.core.EmotionalState,
    val intensity: Float,
    val playerAction: String,
    val consequence: String,
    val significance: Float
)

/**
 * Enums for quest system
 */

enum class QuestPriority(val displayName: String, val sortOrder: Int) {
    CRITICAL("Critical", 0),
    HIGH("High", 1),
    MEDIUM("Medium", 2),
    LOW("Low", 3),
    OPTIONAL("Optional", 4)
}

enum class ObjectiveType {
    TALK_TO_NPC,
    KILL_ENEMY,
    COLLECT_ITEM,
    REACH_LOCATION,
    ESCORT_NPC,
    DEFEND_LOCATION,
    SOLVE_PUZZLE,
    CRAFT_ITEM,
    CAST_SPELL,
    USE_SKILL,
    WAIT_FOR_TIME,
    CUSTOM
}

enum class StoryTheme {
    HEROISM, REDEMPTION, REVENGE, DISCOVERY, SACRIFICE, 
    BETRAYAL, FRIENDSHIP, LOVE, LOSS, GROWTH, JUSTICE
}

enum class StoryMood {
    HOPEFUL, DARK, MYSTERIOUS, COMEDIC, TRAGIC, EPIC, 
    INTIMATE, SUSPENSEFUL, MELANCHOLIC, TRIUMPHANT
}

enum class UrgencyLevel {
    IMMEDIATE, URGENT, MODERATE, RELAXED, NO_PRESSURE
}

enum class StoryScope {
    PERSONAL, LOCAL, REGIONAL, NATIONAL, WORLD_CHANGING, COSMIC
}

enum class MoralAlignment {
    GOOD, NEUTRAL, EVIL, COMPLEX, AMBIGUOUS
}

enum class StoryStakes {
    LIFE_OR_DEATH, REPUTATION, RELATIONSHIPS, WEALTH, 
    FREEDOM, KNOWLEDGE, POWER, PEACE, FUTURE
}

enum class StoryGenre {
    FANTASY_ADVENTURE, MYSTERY, HORROR, ROMANCE, 
    POLITICAL_INTRIGUE, WAR, EXPLORATION, COMEDY
}

enum class BranchTrigger {
    PLAYER_CHOICE, SKILL_CHECK_SUCCESS, SKILL_CHECK_FAILURE,
    TIME_ELAPSED, RELATIONSHIP_THRESHOLD, EMOTIONAL_STATE,
    ITEM_POSSESSED, QUEST_COMPLETED, NPC_DEATH, RANDOM_EVENT
}

enum class ConsequenceType {
    REPUTATION_CHANGE, RELATIONSHIP_CHANGE, ITEM_GAIN,
    ITEM_LOSS, ABILITY_GAIN, ABILITY_LOSS, STATUS_EFFECT,
    UNLOCK_LOCATION, LOCK_LOCATION, SPAWN_NPC, REMOVE_NPC,
    QUEST_UNLOCK, QUEST_FAIL, WORLD_STATE_CHANGE
}

enum class EventType {
    QUEST_START, QUEST_COMPLETE, QUEST_FAIL, NPC_MEETING,
    COMBAT_START, COMBAT_END, DIALOGUE_CHOICE, SKILL_CHECK,
    ITEM_FOUND, LOCATION_DISCOVERED, RELATIONSHIP_CHANGE,
    EMOTIONAL_BREAKTHROUGH, STORY_REVELATION
}

/**
 * Type converters for Room database
 */
class QuestConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromObjectiveList(value: List<QuestObjective>): String = gson.toJson(value)

    @TypeConverter
    fun toObjectiveList(value: String): List<QuestObjective> {
        val type = object : TypeToken<List<QuestObjective>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromRequirements(value: QuestRequirements): String = gson.toJson(value)

    @TypeConverter
    fun toRequirements(value: String): QuestRequirements = 
        gson.fromJson(value, QuestRequirements::class.java)

    @TypeConverter
    fun fromStoryContext(value: StoryContext): String = gson.toJson(value)

    @TypeConverter
    fun toStoryContext(value: String): StoryContext = 
        gson.fromJson(value, StoryContext::class.java)

    @TypeConverter
    fun fromStringList(value: List<String>): String = gson.toJson(value)

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromStringIntMap(value: Map<String, Int>): String = gson.toJson(value)

    @TypeConverter
    fun toStringIntMap(value: String): Map<String, Int> {
        val type = object : TypeToken<Map<String, Int>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromNarrativeBranchList(value: List<NarrativeBranch>): String = gson.toJson(value)

    @TypeConverter
    fun toNarrativeBranchList(value: String): List<NarrativeBranch> {
        val type = object : TypeToken<List<NarrativeBranch>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromCompletionStepList(value: List<CompletionStep>): String = gson.toJson(value)

    @TypeConverter
    fun toCompletionStepList(value: String): List<CompletionStep> {
        val type = object : TypeToken<List<CompletionStep>>() {}.type
        return gson.fromJson(value, type)
    }
}

class GameSessionConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromStoryEventList(value: List<StoryEvent>): String = gson.toJson(value)

    @TypeConverter
    fun toStoryEventList(value: String): List<StoryEvent> {
        val type = object : TypeToken<List<StoryEvent>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromEmotionalHighlightList(value: List<EmotionalHighlight>): String = gson.toJson(value)

    @TypeConverter
    fun toEmotionalHighlightList(value: String): List<EmotionalHighlight> {
        val type = object : TypeToken<List<EmotionalHighlight>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromStringList(value: List<String>): String = gson.toJson(value)

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type)
    }
}