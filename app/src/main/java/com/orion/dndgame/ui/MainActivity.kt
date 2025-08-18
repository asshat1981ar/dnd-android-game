package com.orion.dndgame.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.orion.dndgame.ui.navigation.DnDNavigation
import com.orion.dndgame.ui.theme.DnDGameTheme
import com.orion.dndgame.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity for the D&D game with Orion integration
 * Handles the main navigation and global app state
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val mainViewModel: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            DnDGameTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DnDGameApp(mainViewModel)
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        mainViewModel.connectToOrion()
    }
    
    override fun onPause() {
        super.onPause()
        mainViewModel.pauseGame()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        mainViewModel.disconnectFromOrion()
    }
}

@Composable
fun DnDGameApp(
    viewModel: MainViewModel
) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val connectionStatus by viewModel.connectionStatus.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        viewModel.initializeGame()
    }
    
    DnDNavigation(
        gameState = gameState,
        connectionStatus = connectionStatus,
        isLoading = isLoading,
        onGameEvent = viewModel::handleGameEvent
    )
}