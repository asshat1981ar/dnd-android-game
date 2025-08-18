package com.orion.dndgame.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.orion.dndgame.data.models.GameState
import com.orion.dndgame.ui.events.GameEvent
import com.orion.dndgame.ui.navigation.NavigationRoute

@Composable
fun HomeScreen(
    gameState: GameState,
    onNavigate: (String) -> Unit,
    onGameEvent: (GameEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "D&D Game with Orion",
            style = MaterialTheme.typography.headlineMedium
        )
        
        if (gameState.hasActiveCharacter) {
            Button(
                onClick = { onNavigate(NavigationRoute.GAME) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue Game")
            }
        } else {
            Button(
                onClick = { onNavigate(NavigationRoute.CHARACTER_CREATION) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Character")
            }
        }
        
        OutlinedButton(
            onClick = { onNavigate(NavigationRoute.MULTIPLAYER) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Multiplayer")
        }
        
        OutlinedButton(
            onClick = { onNavigate(NavigationRoute.SETTINGS) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Settings")
        }
    }
}