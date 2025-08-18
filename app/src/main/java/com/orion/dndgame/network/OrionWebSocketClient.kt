package com.orion.dndgame.network

import android.util.Log
import com.google.gson.Gson
import com.orion.dndgame.BuildConfig
import com.orion.dndgame.eds.dialogue.ChimeraDialogueRequest
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.json.JSONObject
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton

/**
 * WebSocket client for real-time communication with Orion orchestrator
 * Handles ProjectChimera consciousness integration and multiplayer functionality
 */
@Singleton
class OrionWebSocketClient @Inject constructor(
    private val gson: Gson
) {
    private var socket: Socket? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    
    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()
    
    private val _agentUpdates = MutableSharedFlow<AgentUpdate>()
    val agentUpdates: SharedFlow<AgentUpdate> = _agentUpdates.asSharedFlow()
    
    private val _systemMetrics = MutableSharedFlow<SystemMetrics>()
    val systemMetrics: SharedFlow<SystemMetrics> = _systemMetrics.asSharedFlow()
    
    private val _multiplayerEvents = MutableSharedFlow<MultiplayerEvent>()
    val multiplayerEvents: SharedFlow<MultiplayerEvent> = _multiplayerEvents.asSharedFlow()
    
    private val pendingRequests = mutableMapOf<String, CompletableDeferred<JSONObject>>()
    
    private var connectionAttempts = 0
    private val maxConnectionAttempts = 5
    private val connectionTimeout = 30000L
    
    /**
     * Connect to Orion orchestrator
     */
    suspend fun connect(): Boolean = withContext(Dispatchers.IO) {
        try {
            _connectionStatus.value = ConnectionStatus.CONNECTING
            
            val options = IO.Options().apply {
                timeout = connectionTimeout
                forceNew = true
                reconnection = true
                reconnectionAttempts = maxConnectionAttempts
                reconnectionDelay = 1000
            }
            
            socket = IO.socket(URI.create(BuildConfig.ORION_BASE_URL), options)
            
            setupSocketListeners()
            
            val connectResult = CompletableDeferred<Boolean>()
            
            socket?.on(Socket.EVENT_CONNECT) {
                Log.i(TAG, "Connected to Orion orchestrator")
                _isConnected.value = true
                _connectionStatus.value = ConnectionStatus.CONNECTED
                connectionAttempts = 0
                
                // Register as D&D game client
                registerClient()
                
                connectResult.complete(true)
            }
            
            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                Log.e(TAG, "Connection error: ${args.joinToString()}")
                _connectionStatus.value = ConnectionStatus.ERROR
                connectResult.complete(false)
            }
            
            socket?.on(Socket.EVENT_DISCONNECT) {
                Log.w(TAG, "Disconnected from Orion")
                _isConnected.value = false
                _connectionStatus.value = ConnectionStatus.DISCONNECTED
            }
            
            socket?.connect()
            
            withTimeout(connectionTimeout) {
                connectResult.await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect to Orion", e)
            _connectionStatus.value = ConnectionStatus.ERROR
            false
        }
    }
    
    /**
     * Disconnect from Orion orchestrator
     */
    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
        _isConnected.value = false
        _connectionStatus.value = ConnectionStatus.DISCONNECTED
        scope.coroutineContext.cancelChildren()
    }
    
    /**
     * Send a dialogue request to ProjectChimera through Orion
     */
    suspend fun sendChimeraDialogueRequest(
        request: ChimeraDialogueRequest
    ): ChimeraDialogueResponse = withContext(Dispatchers.IO) {
        
        val requestId = generateRequestId()
        val requestData = JSONObject().apply {
            put("id", requestId)
            put("type", "chimera_dialogue")
            put("data", JSONObject(gson.toJson(request)))
            put("timestamp", System.currentTimeMillis())
        }
        
        val responseDeferred = CompletableDeferred<JSONObject>()
        pendingRequests[requestId] = responseDeferred
        
        try {
            socket?.emit("agent_request", requestData)
            
            val response = withTimeout(10000) {
                responseDeferred.await()
            }
            
            val responseData = response.getJSONObject("data")
            gson.fromJson(responseData.toString(), ChimeraDialogueResponse::class.java)
            
        } catch (e: TimeoutCancellationException) {
            pendingRequests.remove(requestId)
            throw Exception("Chimera dialogue request timed out")
        } catch (e: Exception) {
            pendingRequests.remove(requestId)
            throw Exception("Failed to process Chimera dialogue request: ${e.message}")
        }
    }
    
    /**
     * Send a task to Orion for processing
     */
    suspend fun sendOrionTask(
        taskType: String,
        capability: String,
        data: Map<String, Any>
    ): OrionTaskResponse = withContext(Dispatchers.IO) {
        
        val requestId = generateRequestId()
        val taskData = JSONObject().apply {
            put("id", requestId)
            put("type", taskType)
            put("capability", capability)
            put("data", JSONObject(gson.toJson(data)))
            put("priority", "medium")
            put("timestamp", System.currentTimeMillis())
        }
        
        val responseDeferred = CompletableDeferred<JSONObject>()
        pendingRequests[requestId] = responseDeferred
        
        try {
            socket?.emit("task_request", taskData)
            
            val response = withTimeout(15000) {
                responseDeferred.await()
            }
            
            OrionTaskResponse(
                taskId = response.getString("taskId"),
                status = response.getString("status"),
                result = response.optJSONObject("result"),
                error = response.optString("error")
            )
            
        } catch (e: TimeoutCancellationException) {
            pendingRequests.remove(requestId)
            throw Exception("Orion task request timed out")
        } catch (e: Exception) {
            pendingRequests.remove(requestId)
            throw Exception("Failed to process Orion task: ${e.message}")
        }
    }
    
    /**
     * Join a multiplayer game session
     */
    suspend fun joinMultiplayerSession(sessionId: String, playerData: PlayerData): Boolean {
        return try {
            val joinData = JSONObject().apply {
                put("sessionId", sessionId)
                put("playerData", JSONObject(gson.toJson(playerData)))
                put("timestamp", System.currentTimeMillis())
            }
            
            socket?.emit("join_session", joinData)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to join multiplayer session", e)
            false
        }
    }
    
    /**
     * Send multiplayer game event
     */
    fun sendMultiplayerEvent(event: MultiplayerEvent) {
        try {
            val eventData = JSONObject().apply {
                put("type", event.type)
                put("data", JSONObject(gson.toJson(event)))
                put("timestamp", System.currentTimeMillis())
            }
            
            socket?.emit("multiplayer_event", eventData)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send multiplayer event", e)
        }
    }
    
    /**
     * Request real-time metrics from Orion
     */
    fun requestSystemMetrics() {
        socket?.emit("request_metrics", JSONObject().apply {
            put("timestamp", System.currentTimeMillis())
        })
    }
    
    private fun setupSocketListeners() {
        socket?.apply {
            // Handle agent responses
            on("agent_response") { args ->
                try {
                    val response = args[0] as JSONObject
                    val requestId = response.getString("id")
                    
                    pendingRequests[requestId]?.complete(response)
                    pendingRequests.remove(requestId)
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing agent response", e)
                }
            }
            
            // Handle task responses
            on("task_response") { args ->
                try {
                    val response = args[0] as JSONObject
                    val requestId = response.optString("requestId")
                    
                    if (requestId.isNotEmpty()) {
                        pendingRequests[requestId]?.complete(response)
                        pendingRequests.remove(requestId)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing task response", e)
                }
            }
            
            // Handle agent updates
            on("agent_update") { args ->
                scope.launch {
                    try {
                        val update = args[0] as JSONObject
                        val agentUpdate = gson.fromJson(update.toString(), AgentUpdate::class.java)
                        _agentUpdates.emit(agentUpdate)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing agent update", e)
                    }
                }
            }
            
            // Handle system metrics
            on("system_metrics") { args ->
                scope.launch {
                    try {
                        val metrics = args[0] as JSONObject
                        val systemMetrics = gson.fromJson(metrics.toString(), SystemMetrics::class.java)
                        _systemMetrics.emit(systemMetrics)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing system metrics", e)
                    }
                }
            }
            
            // Handle multiplayer events
            on("multiplayer_event") { args ->
                scope.launch {
                    try {
                        val event = args[0] as JSONObject
                        val multiplayerEvent = gson.fromJson(event.toString(), MultiplayerEvent::class.java)
                        _multiplayerEvents.emit(multiplayerEvent)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing multiplayer event", e)
                    }
                }
            }
            
            // Handle connection errors
            on("error") { args ->
                Log.e(TAG, "Socket error: ${args.joinToString()}")
                _connectionStatus.value = ConnectionStatus.ERROR
            }
        }
    }
    
    private fun registerClient() {
        val clientData = JSONObject().apply {
            put("clientType", "dnd_android_game")
            put("version", BuildConfig.VERSION_NAME)
            put("capabilities", listOf("dialogue", "multiplayer", "consciousness"))
            put("timestamp", System.currentTimeMillis())
        }
        
        socket?.emit("register_client", clientData)
    }
    
    private fun generateRequestId(): String {
        return "dnd_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
    
    companion object {
        private const val TAG = "OrionWebSocketClient"
    }
}

/**
 * Data classes for network communication
 */

enum class ConnectionStatus {
    DISCONNECTED, CONNECTING, CONNECTED, ERROR, RECONNECTING
}

data class ChimeraDialogueResponse(
    val npcId: String,
    val consciousnessUpdate: ConsciousnessUpdate,
    val emotionalResponse: EmotionalResponseData,
    val emergentBehavior: EmergentBehaviorData,
    val synthesizedResponse: SynthesizedResponseData,
    val consciousnessCommentary: String,
    val responseQuality: Float,
    val timestamp: Long
)

data class ConsciousnessUpdate(
    val awarenessLevel: Float,
    val cognitiveLoad: Float,
    val metacognitionLevel: Float,
    val stateCoherence: Float,
    val timestamp: Long
)

data class EmotionalResponseData(
    val triggeredEmotions: List<String>,
    val intensity: Float,
    val stability: Float,
    val empathicResonance: Float,
    val moodShift: Float
)

data class EmergentBehaviorData(
    val traits: List<EmergentTrait>,
    val adaptations: List<String>,
    val novelty: Float,
    val coherence: Float
)

data class EmergentTrait(
    val name: String,
    val strength: Float
)

data class SynthesizedResponseData(
    val content: String,
    val emotionalTone: String,
    val consciousnessLevel: Float,
    val authenticity: Float,
    val depth: Float
)

data class OrionTaskResponse(
    val taskId: String,
    val status: String,
    val result: org.json.JSONObject?,
    val error: String?
)

data class AgentUpdate(
    val agentId: String,
    val status: String,
    val metrics: Map<String, Any>,
    val timestamp: Long
)

data class SystemMetrics(
    val totalAgents: Int,
    val activeAgents: Int,
    val completedTasks: Int,
    val averageResponseTime: Float,
    val systemLoad: Float,
    val memoryUsage: Float,
    val timestamp: Long
)

data class MultiplayerEvent(
    val type: String,
    val sessionId: String,
    val playerId: String,
    val data: Map<String, Any>,
    val timestamp: Long
)

data class PlayerData(
    val playerId: String,
    val playerName: String,
    val characterId: String,
    val characterName: String,
    val level: Int,
    val location: String
)