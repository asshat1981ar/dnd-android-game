package com.orion.dndgame.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orion.dndgame.data.models.GameState
import com.orion.dndgame.network.ConnectionStatus
import com.orion.dndgame.network.OrionWebSocketClient
import com.orion.dndgame.orion.OrionGameAgentCoordinator
import com.orion.dndgame.ui.events.GameEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val orionClient: OrionWebSocketClient,
    private val agentCoordinator: OrionGameAgentCoordinator,
    private val gameStateManager: GameStateManager
) : ViewModel() {
    
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    val connectionStatus: StateFlow<ConnectionStatus> = orionClient.connectionStatus
    
    fun initializeGame() {
        viewModelScope.launch {
            _isLoading.value = true
            gameStateManager.initializeGameState()
            agentCoordinator.initializeGameAgents()
            _isLoading.value = false
        }
    }
    
    fun connectToOrion() {
        viewModelScope.launch {
            orionClient.connect()
        }
    }
    
    fun disconnectFromOrion() {
        orionClient.disconnect()
    }
    
    fun pauseGame() {
        // Implement game pause logic
    }
    
    fun handleGameEvent(event: GameEvent) {
        viewModelScope.launch {
            gameStateManager.processGameEvent(event)
        }
    }
}

// Simplified GameStateManager
class GameStateManager @Inject constructor(
    private val characterRepository: com.orion.dndgame.data.repository.CharacterRepository,
    private val npcRepository: com.orion.dndgame.data.repository.NPCRepository,
    private val questRepository: com.orion.dndgame.data.repository.QuestRepository,
    private val sessionRepository: com.orion.dndgame.data.repository.GameSessionRepository,
    private val orionClient: OrionWebSocketClient
) {
    suspend fun initializeGameState() {
        // Initialize game state from repositories
    }
    
    suspend fun processGameEvent(event: GameEvent) {
        // Process game events and update state
    }
}