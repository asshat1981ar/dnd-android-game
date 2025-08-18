package com.orion.dndgame.eds.core

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Core emotional states for NPCs in the Emotional Dialogue System
 * Each state influences dialogue generation and NPC behavior
 */
@Parcelize
enum class EmotionalState(
    val displayName: String,
    val description: String,
    val baseIntensity: Float,
    val compatibleStates: List<String> = emptyList(),
    val transitionTriggers: List<String> = emptyList()
) : Parcelable {
    
    HOPEFUL(
        displayName = "Hopeful",
        description = "Optimistic and expecting positive outcomes",
        baseIntensity = 0.7f,
        compatibleStates = listOf("JOYFUL", "LOYAL"),
        transitionTriggers = listOf("quest_success", "friendly_interaction", "promise_kept")
    ),
    
    BITTER(
        displayName = "Bitter",
        description = "Resentful and cynical due to past disappointments",
        baseIntensity = 0.8f,
        compatibleStates = listOf("WRATHFUL", "BETRAYED"),
        transitionTriggers = listOf("betrayal", "loss", "repeated_failures")
    ),
    
    WRATHFUL(
        displayName = "Wrathful",
        description = "Filled with intense anger and desire for revenge",
        baseIntensity = 0.9f,
        compatibleStates = listOf("BITTER", "BETRAYED"),
        transitionTriggers = listOf("attack", "insult", "threat_to_loved_ones")
    ),
    
    FEARFUL(
        displayName = "Fearful",
        description = "Anxious and afraid, seeking safety and protection",
        baseIntensity = 0.6f,
        compatibleStates = listOf("RESIGNED"),
        transitionTriggers = listOf("danger", "intimidation", "unknown_threat")
    ),
    
    RESIGNED(
        displayName = "Resigned",
        description = "Accepting defeat or unfortunate circumstances",
        baseIntensity = 0.4f,
        compatibleStates = listOf("FEARFUL", "BITTER"),
        transitionTriggers = listOf("repeated_failure", "overwhelming_odds", "loss_of_hope")
    ),
    
    JOYFUL(
        displayName = "Joyful",
        description = "Happy and enthusiastic about life",
        baseIntensity = 0.8f,
        compatibleStates = listOf("HOPEFUL", "LOYAL"),
        transitionTriggers = listOf("celebration", "gift_received", "dream_fulfilled")
    ),
    
    BETRAYED(
        displayName = "Betrayed",
        description = "Hurt and distrustful due to broken trust",
        baseIntensity = 0.7f,
        compatibleStates = listOf("BITTER", "WRATHFUL"),
        transitionTriggers = listOf("trust_broken", "abandoned", "lied_to")
    ),
    
    LOYAL(
        displayName = "Loyal",
        description = "Devoted and faithful to someone or something",
        baseIntensity = 0.6f,
        compatibleStates = listOf("HOPEFUL", "JOYFUL"),
        transitionTriggers = listOf("trust_earned", "saved_by_player", "shared_hardship")
    );

    /**
     * Calculate emotional intensity based on recent events and personality
     */
    fun calculateIntensity(
        recentEvents: List<EmotionalEvent>,
        personalityModifier: Float = 1.0f,
        timeFactor: Float = 1.0f
    ): Float {
        val eventModifier = recentEvents
            .filter { it.emotionalImpact.containsKey(this.name) }
            .sumOf { it.emotionalImpact[this.name]?.toDouble() ?: 0.0 }
            .toFloat()
            
        return (baseIntensity + eventModifier) * personalityModifier * timeFactor
    }

    /**
     * Check if this emotional state can transition to another state
     */
    fun canTransitionTo(targetState: EmotionalState, trigger: String): Boolean {
        return targetState.transitionTriggers.contains(trigger) ||
               compatibleStates.contains(targetState.name)
    }

    /**
     * Get the emotional color theme for UI representation
     */
    fun getColorTheme(): EmotionalColorTheme {
        return when (this) {
            HOPEFUL -> EmotionalColorTheme.WARM_BLUE
            BITTER -> EmotionalColorTheme.DARK_PURPLE
            WRATHFUL -> EmotionalColorTheme.FIERCE_RED
            FEARFUL -> EmotionalColorTheme.PALE_YELLOW
            RESIGNED -> EmotionalColorTheme.MUTED_GRAY
            JOYFUL -> EmotionalColorTheme.BRIGHT_GOLD
            BETRAYED -> EmotionalColorTheme.DEEP_PURPLE
            LOYAL -> EmotionalColorTheme.ROYAL_BLUE
        }
    }
}

/**
 * Color themes for emotional states in the UI
 */
enum class EmotionalColorTheme(val primaryColor: Long, val secondaryColor: Long) {
    WARM_BLUE(0xFF4A90E2, 0xFF7DB3F0),
    DARK_PURPLE(0xFF6A4C93, 0xFF8B6BB1),
    FIERCE_RED(0xFFD63031, 0xFFE85A5A),
    PALE_YELLOW(0xFFFDCB6E, 0xFFFEDC83),
    MUTED_GRAY(0xFF636E72, 0xFF828A8F),
    BRIGHT_GOLD(0xFFF39C12, 0xFFF5B041),
    DEEP_PURPLE(0xFF8E44AD, 0xFFAA6CC2),
    ROYAL_BLUE(0xFF2980B9, 0xFF5DADE2)
}

/**
 * Represents an emotional event that can trigger state changes
 */
@Parcelize
data class EmotionalEvent(
    val id: String,
    val type: String,
    val description: String,
    val timestamp: Long,
    val emotionalImpact: Map<String, Float>, // State name to impact value
    val persistenceLevel: Float = 1.0f, // How long this event affects the NPC
    val source: String = "player_action"
) : Parcelable

/**
 * Complex emotional state that can contain multiple emotions
 */
@Parcelize
data class EmotionalProfile(
    val primaryState: EmotionalState,
    val secondaryStates: List<Pair<EmotionalState, Float>> = emptyList(),
    val stability: Float = 0.5f, // How quickly emotions change
    val intensity: Float = 0.7f, // Overall emotional intensity
    val memory: EmotionalMemory = EmotionalMemory(),
    val lastUpdated: Long = System.currentTimeMillis()
) : Parcelable {

    /**
     * Update the emotional profile based on a new event
     */
    fun processEvent(event: EmotionalEvent, timeElapsed: Long = 0): EmotionalProfile {
        val newMemory = memory.addEvent(event)
        val emotionalDecay = calculateEmotionalDecay(timeElapsed)
        
        // Calculate new primary state
        val newPrimaryState = calculateNewPrimaryState(event, emotionalDecay)
        val newSecondaryStates = calculateNewSecondaryStates(event, emotionalDecay)
        
        return copy(
            primaryState = newPrimaryState,
            secondaryStates = newSecondaryStates,
            memory = newMemory,
            lastUpdated = System.currentTimeMillis()
        )
    }

    private fun calculateEmotionalDecay(timeElapsed: Long): Float {
        // Emotions decay over time based on stability
        val hoursElapsed = timeElapsed / (1000 * 60 * 60)
        return kotlin.math.exp(-hoursElapsed * (1 - stability)).toFloat()
    }

    private fun calculateNewPrimaryState(event: EmotionalEvent, decay: Float): EmotionalState {
        // Find the emotion most strongly triggered by this event
        val mostImpactedEmotion = event.emotionalImpact.maxByOrNull { it.value }
        
        return if (mostImpactedEmotion != null && mostImpactedEmotion.value > 0.3f) {
            try {
                EmotionalState.valueOf(mostImpactedEmotion.key)
            } catch (e: IllegalArgumentException) {
                primaryState // Keep current state if invalid
            }
        } else {
            primaryState
        }
    }

    private fun calculateNewSecondaryStates(event: EmotionalEvent, decay: Float): List<Pair<EmotionalState, Float>> {
        val newSecondaryStates = mutableListOf<Pair<EmotionalState, Float>>()
        
        // Add states from the event
        event.emotionalImpact.forEach { (stateName, impact) ->
            try {
                val state = EmotionalState.valueOf(stateName)
                if (state != primaryState && impact > 0.1f) {
                    newSecondaryStates.add(state to impact)
                }
            } catch (e: IllegalArgumentException) {
                // Skip invalid state names
            }
        }
        
        // Apply decay to existing secondary states
        secondaryStates.forEach { (state, intensity) ->
            val newIntensity = intensity * decay
            if (newIntensity > 0.05f) {
                newSecondaryStates.add(state to newIntensity)
            }
        }
        
        // Keep only top 3 secondary states
        return newSecondaryStates
            .sortedByDescending { it.second }
            .take(3)
    }
}

/**
 * Memory system for emotional events
 */
@Parcelize
data class EmotionalMemory(
    val recentEvents: List<EmotionalEvent> = emptyList(),
    val significantEvents: List<EmotionalEvent> = emptyList(),
    val relationshipMemories: Map<String, Float> = emptyMap(), // Character ID to relationship value
    val maxRecentEvents: Int = 10,
    val maxSignificantEvents: Int = 50
) : Parcelable {

    fun addEvent(event: EmotionalEvent): EmotionalMemory {
        val updatedRecent = (recentEvents + event).takeLast(maxRecentEvents)
        val updatedSignificant = if (isSignificantEvent(event)) {
            (significantEvents + event).takeLast(maxSignificantEvents)
        } else {
            significantEvents
        }
        
        return copy(
            recentEvents = updatedRecent,
            significantEvents = updatedSignificant
        )
    }

    private fun isSignificantEvent(event: EmotionalEvent): Boolean {
        return event.emotionalImpact.values.any { it > 0.5f } || 
               event.persistenceLevel > 0.8f
    }

    fun getRelationshipWith(characterId: String): Float {
        return relationshipMemories[characterId] ?: 0.0f
    }

    fun updateRelationship(characterId: String, change: Float): EmotionalMemory {
        val currentValue = getRelationshipWith(characterId)
        val newValue = (currentValue + change).coerceIn(-1.0f, 1.0f)
        
        return copy(
            relationshipMemories = relationshipMemories + (characterId to newValue)
        )
    }
}