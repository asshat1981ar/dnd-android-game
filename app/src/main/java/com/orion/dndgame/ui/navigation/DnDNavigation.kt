package com.orion.dndgame.ui.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.orion.dndgame.data.models.GameState
import com.orion.dndgame.network.ConnectionStatus
import com.orion.dndgame.ui.screens.character.CharacterCreationScreen
import com.orion.dndgame.ui.screens.character.CharacterSheetScreen
import com.orion.dndgame.ui.screens.dialogue.DialogueScreen
import com.orion.dndgame.ui.screens.game.GameScreen
import com.orion.dndgame.ui.screens.home.HomeScreen
import com.orion.dndgame.ui.screens.loading.LoadingScreen
import com.orion.dndgame.ui.screens.multiplayer.MultiplayerLobbyScreen
import com.orion.dndgame.ui.screens.quest.QuestScreen
import com.orion.dndgame.ui.screens.settings.SettingsScreen
import com.orion.dndgame.ui.components.ConnectionStatusBar
import com.orion.dndgame.ui.events.GameEvent

/**
 * Main navigation component for the D&D game
 * Handles screen transitions and maintains navigation state
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DnDNavigation(
    gameState: GameState,
    connectionStatus: ConnectionStatus,
    isLoading: Boolean,
    onGameEvent: (GameEvent) -> Unit,
    navController: NavHostController = rememberNavController()
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Connection status bar
        ConnectionStatusBar(
            connectionStatus = connectionStatus,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Main navigation content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            if (isLoading) {
                LoadingScreen()
            } else {
                NavHost(
                    navController = navController,
                    startDestination = if (gameState.hasActiveCharacter) {
                        NavigationRoute.GAME
                    } else {
                        NavigationRoute.HOME
                    },
                    enterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { it }
                        ) + fadeIn()
                    },
                    exitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { -it }
                        ) + fadeOut()
                    }
                ) {
                    composable(NavigationRoute.HOME) {
                        HomeScreen(
                            gameState = gameState,
                            onNavigate = { route ->
                                navController.navigate(route)
                            },
                            onGameEvent = onGameEvent
                        )
                    }
                    
                    composable(NavigationRoute.CHARACTER_CREATION) {
                        CharacterCreationScreen(
                            onCharacterCreated = { character ->
                                onGameEvent(GameEvent.CharacterCreated(character))
                                navController.navigate(NavigationRoute.GAME) {
                                    popUpTo(NavigationRoute.HOME) { inclusive = true }
                                }
                            },
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                    
                    composable(NavigationRoute.GAME) {
                        GameScreen(
                            gameState = gameState,
                            onNavigate = { route ->
                                navController.navigate(route)
                            },
                            onGameEvent = onGameEvent
                        )
                    }
                    
                    composable(NavigationRoute.CHARACTER_SHEET) {
                        CharacterSheetScreen(
                            character = gameState.currentCharacter!!,
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onCharacterUpdated = { character ->
                                onGameEvent(GameEvent.CharacterUpdated(character))
                            }
                        )
                    }
                    
                    composable(NavigationRoute.DIALOGUE) {
                        DialogueScreen(
                            gameState = gameState,
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onGameEvent = onGameEvent
                        )
                    }
                    
                    composable(NavigationRoute.QUESTS) {
                        QuestScreen(
                            gameState = gameState,
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onGameEvent = onGameEvent
                        )
                    }
                    
                    composable(NavigationRoute.MULTIPLAYER) {
                        MultiplayerLobbyScreen(
                            gameState = gameState,
                            connectionStatus = connectionStatus,
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onGameEvent = onGameEvent
                        )
                    }
                    
                    composable(NavigationRoute.SETTINGS) {
                        SettingsScreen(
                            gameState = gameState,
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onGameEvent = onGameEvent
                        )
                    }
                }
            }
        }
    }
}

/**
 * Navigation routes for the application
 */
object NavigationRoute {
    const val HOME = "home"
    const val CHARACTER_CREATION = "character_creation"
    const val GAME = "game"
    const val CHARACTER_SHEET = "character_sheet"
    const val DIALOGUE = "dialogue"
    const val QUESTS = "quests"
    const val MULTIPLAYER = "multiplayer"
    const val SETTINGS = "settings"
}