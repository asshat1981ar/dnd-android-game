package com.orion.dndgame.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.orion.dndgame.network.ConnectionStatus

@Composable
fun ConnectionStatusBar(
    connectionStatus: ConnectionStatus,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = connectionStatus != ConnectionStatus.CONNECTED,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
    ) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = when (connectionStatus) {
                ConnectionStatus.CONNECTING -> MaterialTheme.colorScheme.tertiary
                ConnectionStatus.ERROR -> MaterialTheme.colorScheme.error
                ConnectionStatus.DISCONNECTED -> MaterialTheme.colorScheme.surfaceVariant
                ConnectionStatus.RECONNECTING -> MaterialTheme.colorScheme.warning
                else -> Color.Transparent
            }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = when (connectionStatus) {
                        ConnectionStatus.CONNECTING, ConnectionStatus.RECONNECTING -> Icons.Default.Wifi
                        ConnectionStatus.ERROR, ConnectionStatus.DISCONNECTED -> Icons.Default.CloudOff
                        else -> Icons.Default.WifiOff
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = when (connectionStatus) {
                        ConnectionStatus.CONNECTING -> "Connecting to Orion..."
                        ConnectionStatus.RECONNECTING -> "Reconnecting..."
                        ConnectionStatus.ERROR -> "Connection Error"
                        ConnectionStatus.DISCONNECTED -> "Offline Mode"
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (connectionStatus == ConnectionStatus.CONNECTING || connectionStatus == ConnectionStatus.RECONNECTING) {
                    LoadingPulse(
                        size = 16.dp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

val MaterialTheme.colorScheme.warning: Color
    get() = Color(0xFFFF9800)