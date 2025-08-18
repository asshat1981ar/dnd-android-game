package com.orion.dndgame.ui.events

import com.orion.dndgame.data.models.Character
import com.orion.dndgame.data.models.PlayerChoice

/**
 * Sealed class representing all possible game events
 */
sealed class GameEvent {
    data class CharacterCreated(val character: Character) : GameEvent()
    data class CharacterUpdated(val character: Character) : GameEvent()
    data class CharacterSelected(val characterId: String) : GameEvent()
    
    data class DialogueChoiceMade(val choice: PlayerChoice) : GameEvent()
    data class StartDialogue(val npcId: String) : GameEvent()
    data object EndDialogue : GameEvent()
    
    data class StartCombat(val enemies: List<String>) : GameEvent()
    data object EndCombat : GameEvent()
    
    data class QuestStarted(val questId: String) : GameEvent()
    data class QuestCompleted(val questId: String) : GameEvent()
    
    data class LocationChanged(val newLocation: String) : GameEvent()
    
    data class JoinMultiplayerSession(val sessionId: String) : GameEvent()
    data object LeaveMultiplayerSession : GameEvent()
    
    data class ShowConsciousnessDetails(val npcId: String) : GameEvent()
    
    data object SaveGame : GameEvent()
    data object LoadGame : GameEvent()
}