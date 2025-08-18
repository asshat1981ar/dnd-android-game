package com.orion.dndgame.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Central game state that holds all current game information
 */
@Parcelize
data class GameState(
    val currentCharacter: Character? = null,
    val currentLocation: String = "tavern",
    val activeQuests: List<Quest> = emptyList(),
    val completedQuests: List<Quest> = emptyList(),
    val nearbyNPCs: List<NPC> = emptyList(),
    val currentDialogueNPC: NPC? = null,
    val isInCombat: Boolean = false,
    val combatState: String? = null,
    val gameMode: GameMode = GameMode.EXPLORATION,
    val multiplayerSession: MultiplayerSession? = null,
    val lastSaveTime: Long = System.currentTimeMillis(),
    val playTime: Long = 0L,
    val achievements: List<String> = emptyList()
) : Parcelable {
    
    val hasActiveCharacter: Boolean
        get() = currentCharacter != null
    
    val isInMultiplayer: Boolean
        get() = multiplayerSession != null
}

@Parcelize
data class MultiplayerSession(
    val sessionId: String,
    val hostPlayerId: String,
    val players: List<PlayerData>,
    val isHost: Boolean,
    val maxPlayers: Int = 4
) : Parcelable

enum class GameMode {
    EXPLORATION, DIALOGUE, COMBAT, CHARACTER_CREATION, QUEST_MANAGEMENT
}