package com.orion.dndgame.cache

import androidx.room.Room
import com.orion.dndgame.data.models.*
import com.orion.dndgame.eds.core.EmotionalState
import com.orion.dndgame.eds.enhanced.EnhancedEmotionalDialogueSystem
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Performance Optimization System with intelligent caching and background processing
 */
@Singleton
class PerformanceOptimizationSystem @Inject constructor() {
    
    private val memoryCache = ConcurrentHashMap<String, CachedItem<*>>()
    private val emotionalStateCache = ConcurrentHashMap<String, CachedEmotionalState>()
    private val dialogueCache = ConcurrentHashMap<String, CachedDialogue>()
    private val consciousnessProcessingQueue = Channel<ConsciousnessTask>(capacity = 100)
    
    private val cacheScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // Performance metrics
    private val _performanceMetrics = MutableStateFlow(PerformanceMetrics())
    val performanceMetrics: StateFlow<PerformanceMetrics> = _performanceMetrics.asStateFlow()
    
    // Cache statistics
    private val _cacheStatistics = MutableStateFlow(CacheStatistics())
    val cacheStatistics: StateFlow<CacheStatistics> = _cacheStatistics.asStateFlow()
    
    data class CachedItem<T>(
        val data: T,
        val timestamp: Long,
        val expirationTime: Long,
        val accessCount: Int = 0,
        val lastAccessed: Long = System.currentTimeMillis()
    ) {
        fun isExpired(): Boolean = System.currentTimeMillis() > expirationTime
        fun isStale(): Boolean = System.currentTimeMillis() - timestamp > TimeUnit.MINUTES.toMillis(5)
    }
    
    data class CachedEmotionalState(
        val npcId: String,
        val emotionalState: EmotionalState,
        val intensity: Double,
        val transitionProbabilities: Map<EmotionalState, Double>,
        val cacheTime: Long,
        val validityDuration: Long = TimeUnit.MINUTES.toMillis(10)
    ) {
        fun isValid(): Boolean = System.currentTimeMillis() - cacheTime < validityDuration
    }
    
    data class CachedDialogue(
        val dialogueKey: String,
        val dialogue: String,
        val emotionalContext: String,
        val playerChoiceContext: String,
        val cacheTime: Long,
        val hitCount: Int = 0
    )
    
    data class ConsciousnessTask(
        val npcId: String,
        val taskType: TaskType,
        val priority: TaskPriority,
        val data: Any,
        val callback: (Any) -> Unit
    )
    
    enum class TaskType {
        EMOTIONAL_PROCESSING,
        CONSCIOUSNESS_UPDATE,
        MEMORY_CONSOLIDATION,
        BEHAVIOR_EVOLUTION,
        DIALOGUE_PREPROCESSING
    }
    
    enum class TaskPriority {
        LOW, NORMAL, HIGH, CRITICAL
    }
    
    data class PerformanceMetrics(
        val averageDialogueGenerationTime: Long = 0,
        val averageConsciousnessProcessingTime: Long = 0,
        val cacheHitRate: Double = 0.0,
        val memoryUsageKB: Long = 0,
        val activeCoroutines: Int = 0,
        val queuedTasks: Int = 0,
        val processedTasksPerSecond: Double = 0.0
    )
    
    data class CacheStatistics(
        val totalCacheSize: Int = 0,
        val emotionalStateCacheSize: Int = 0,
        val dialogueCacheSize: Int = 0,
        val cacheHits: Long = 0,
        val cacheMisses: Long = 0,
        val cacheEvictions: Long = 0,
        val averageAccessTime: Long = 0
    )
    
    init {
        startBackgroundProcessing()
        startPerformanceMonitoring()
        startCacheManagement()
    }
    
    /**
     * Cached dialogue generation with intelligent preprocessing
     */
    suspend fun getCachedDialogue(
        npcId: String,
        emotionalState: EmotionalState,
        playerChoice: String,
        questContext: String,
        dialogueSystem: EnhancedEmotionalDialogueSystem
    ): String = withContext(Dispatchers.IO) {
        
        val startTime = System.currentTimeMillis()
        
        val dialogueKey = generateDialogueKey(npcId, emotionalState, playerChoice, questContext)
        
        // Check cache first
        val cachedDialogue = dialogueCache[dialogueKey]
        if (cachedDialogue != null) {
            updateCacheStatistics(hit = true)
            _performanceMetrics.value = _performanceMetrics.value.copy(
                averageDialogueGenerationTime = calculateAverageTime(
                    _performanceMetrics.value.averageDialogueGenerationTime,
                    System.currentTimeMillis() - startTime
                )
            )
            return@withContext cachedDialogue.dialogue
        }
        
        updateCacheStatistics(hit = false)
        
        // Generate dialogue if not cached
        val dialogue = generateDialogueWithFallback(
            npcId, emotionalState, playerChoice, questContext, dialogueSystem
        )
        
        // Cache the result
        cacheDialogue(dialogueKey, dialogue, emotionalState.name, playerChoice)
        
        val processingTime = System.currentTimeMillis() - startTime
        _performanceMetrics.value = _performanceMetrics.value.copy(
            averageDialogueGenerationTime = calculateAverageTime(
                _performanceMetrics.value.averageDialogueGenerationTime,
                processingTime
            )
        )
        
        dialogue
    }
    
    /**
     * Cached emotional state processing with predictive caching
     */
    suspend fun getCachedEmotionalState(
        npcId: String,
        currentEmotion: EmotionalState,
        processor: suspend () -> EmotionalState
    ): EmotionalState = withContext(Dispatchers.IO) {
        
        val cached = emotionalStateCache[npcId]
        if (cached != null && cached.isValid()) {
            return@withContext cached.emotionalState
        }
        
        // Process new emotional state
        val newState = processor()
        
        // Cache with transition probabilities for predictive caching
        val transitionProbabilities = calculateTransitionProbabilities(currentEmotion)
        cacheEmotionalState(npcId, newState, 1.0, transitionProbabilities)
        
        // Predictively cache likely next states
        predictivelyCache(npcId, newState, transitionProbabilities)
        
        newState
    }
    
    /**
     * Background consciousness processing to reduce main thread load
     */
    fun queueConsciousnessTask(
        npcId: String,
        taskType: TaskType,
        priority: TaskPriority = TaskPriority.NORMAL,
        data: Any,
        callback: (Any) -> Unit
    ) {
        val task = ConsciousnessTask(npcId, taskType, priority, data, callback)
        
        cacheScope.launch {
            try {
                consciousnessProcessingQueue.send(task)
                _performanceMetrics.value = _performanceMetrics.value.copy(
                    queuedTasks = consciousnessProcessingQueue.isEmpty.let { if (it) 0 else _performanceMetrics.value.queuedTasks + 1 }
                )
            } catch (e: Exception) {
                // Handle queue full - process immediately for critical tasks
                if (priority == TaskPriority.CRITICAL) {
                    processConsciousnessTask(task)
                }
            }
        }
    }
    
    /**
     * Intelligent memory management with LRU eviction
     */
    fun <T> cacheWithIntelligentEviction(
        key: String,
        data: T,
        expirationMinutes: Long = 30,
        priority: CachePriority = CachePriority.NORMAL
    ) {
        val expirationTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(expirationMinutes)
        val cachedItem = CachedItem(data, System.currentTimeMillis(), expirationTime)
        
        // Check if cache is full and needs cleanup
        if (memoryCache.size > MAX_CACHE_SIZE) {
            performIntelligentCacheEviction(priority)
        }
        
        memoryCache[key] = cachedItem
        updateCacheStatistics()
    }
    
    @Suppress("UNCHECKED_CAST")
    fun <T> getCachedItem(key: String): T? {
        val item = memoryCache[key] as? CachedItem<T>
        return if (item != null && !item.isExpired()) {
            // Update access statistics
            memoryCache[key] = item.copy(
                accessCount = item.accessCount + 1,
                lastAccessed = System.currentTimeMillis()
            )
            item.data
        } else {
            // Remove expired item
            memoryCache.remove(key)
            null
        }
    }
    
    private fun startBackgroundProcessing() {
        cacheScope.launch {
            while (isActive) {
                try {
                    val task = consciousnessProcessingQueue.receive()
                    processConsciousnessTask(task)
                } catch (e: Exception) {
                    // Log error but continue processing
                }
            }
        }
    }
    
    private fun startPerformanceMonitoring() {
        cacheScope.launch {
            while (isActive) {
                delay(TimeUnit.SECONDS.toMillis(5)) // Update every 5 seconds
                updatePerformanceMetrics()
            }
        }
    }
    
    private fun startCacheManagement() {
        cacheScope.launch {
            while (isActive) {
                delay(TimeUnit.MINUTES.toMillis(5)) // Clean every 5 minutes
                performCacheCleanup()
            }
        }
    }
    
    private suspend fun processConsciousnessTask(task: ConsciousnessTask) {
        val startTime = System.currentTimeMillis()
        
        try {
            val result = when (task.taskType) {
                TaskType.EMOTIONAL_PROCESSING -> processEmotionalState(task.data)
                TaskType.CONSCIOUSNESS_UPDATE -> updateConsciousness(task.data)
                TaskType.MEMORY_CONSOLIDATION -> consolidateMemory(task.data)
                TaskType.BEHAVIOR_EVOLUTION -> evolveBehavior(task.data)
                TaskType.DIALOGUE_PREPROCESSING -> preprocessDialogue(task.data)
            }
            
            task.callback(result)
            
        } catch (e: Exception) {
            // Handle processing error
            task.callback("Error: ${e.message}")
        }
        
        val processingTime = System.currentTimeMillis() - startTime
        _performanceMetrics.value = _performanceMetrics.value.copy(
            averageConsciousnessProcessingTime = calculateAverageTime(
                _performanceMetrics.value.averageConsciousnessProcessingTime,
                processingTime
            )
        )
    }
    
    private suspend fun generateDialogueWithFallback(
        npcId: String,
        emotionalState: EmotionalState,
        playerChoice: String,
        questContext: String,
        dialogueSystem: EnhancedEmotionalDialogueSystem
    ): String {
        return try {
            // Try primary dialogue generation
            generatePrimaryDialogue(npcId, emotionalState, playerChoice, questContext, dialogueSystem)
        } catch (e: Exception) {
            // Fallback to cached similar dialogue
            findSimilarCachedDialogue(npcId, emotionalState, playerChoice) 
                ?: generateFallbackDialogue(emotionalState, playerChoice)
        }
    }
    
    private suspend fun generatePrimaryDialogue(
        npcId: String,
        emotionalState: EmotionalState,
        playerChoice: String,
        questContext: String,
        dialogueSystem: EnhancedEmotionalDialogueSystem
    ): String {
        // Simulate primary dialogue generation
        delay(Random.nextLong(50, 200)) // Realistic processing time
        
        return when (emotionalState) {
            EmotionalState.HOPEFUL -> "I sense great potential in this endeavor, ${getPlayerAddress(playerChoice)}."
            EmotionalState.BITTER -> "Another promise, another disappointment waiting to happen."
            EmotionalState.WRATHFUL -> "My anger burns bright - let us channel it toward justice!"
            EmotionalState.LOYAL -> "Together we shall overcome any challenge that awaits."
            EmotionalState.FEARFUL -> "I... I'm not sure we should proceed. The dangers seem too great."
            EmotionalState.JOYFUL -> "What wonderful news! This brings light to my heart!"
            EmotionalState.BETRAYED -> "How can I trust you after what has transpired?"
            EmotionalState.RESIGNED -> "Very well. I suppose there's little choice in the matter."
            EmotionalState.NEUTRAL -> "I understand. Let us proceed as planned."
        }
    }
    
    private fun getPlayerAddress(playerChoice: String): String {
        return when (playerChoice.uppercase()) {
            "HONORABLE" -> "noble friend"
            "COMPASSIONATE" -> "kind soul"
            "RUTHLESS" -> "determined one"
            "GREEDY" -> "ambitious partner"
            else -> "traveler"
        }
    }
    
    private fun findSimilarCachedDialogue(
        npcId: String,
        emotionalState: EmotionalState,
        playerChoice: String
    ): String? {
        return dialogueCache.values.firstOrNull { cached ->
            cached.emotionalContext.contains(emotionalState.name, ignoreCase = true) &&
            cached.playerChoiceContext.contains(playerChoice, ignoreCase = true)
        }?.dialogue
    }
    
    private fun generateFallbackDialogue(
        emotionalState: EmotionalState,
        playerChoice: String
    ): String {
        return "I ${getEmotionalResponse(emotionalState)} your ${getChoiceResponse(playerChoice)}."
    }
    
    private fun getEmotionalResponse(emotion: EmotionalState): String {
        return when (emotion) {
            EmotionalState.HOPEFUL -> "am encouraged by"
            EmotionalState.BITTER -> "have little faith in"
            EmotionalState.WRATHFUL -> "am inflamed by"
            EmotionalState.LOYAL -> "stand behind"
            EmotionalState.FEARFUL -> "am concerned about"
            EmotionalState.JOYFUL -> "celebrate"
            EmotionalState.BETRAYED -> "question"
            EmotionalState.RESIGNED -> "accept"
            EmotionalState.NEUTRAL -> "acknowledge"
        }
    }
    
    private fun getChoiceResponse(choice: String): String {
        return when (choice.uppercase()) {
            "HONORABLE" -> "noble intentions"
            "COMPASSIONATE" -> "kind gesture"
            "RUTHLESS" -> "decisive action"
            "GREEDY" -> "ambitious proposal"
            else -> "words"
        }
    }
    
    private fun generateDialogueKey(
        npcId: String,
        emotionalState: EmotionalState,
        playerChoice: String,
        questContext: String
    ): String {
        return "${npcId}_${emotionalState.name}_${playerChoice}_${questContext.hashCode()}"
    }
    
    private fun cacheDialogue(
        key: String,
        dialogue: String,
        emotionalContext: String,
        playerChoiceContext: String
    ) {
        dialogueCache[key] = CachedDialogue(
            dialogueKey = key,
            dialogue = dialogue,
            emotionalContext = emotionalContext,
            playerChoiceContext = playerChoiceContext,
            cacheTime = System.currentTimeMillis()
        )
        
        // Prevent cache overflow
        if (dialogueCache.size > MAX_DIALOGUE_CACHE_SIZE) {
            cleanupDialogueCache()
        }
    }
    
    private fun cacheEmotionalState(
        npcId: String,
        state: EmotionalState,
        intensity: Double,
        transitionProbabilities: Map<EmotionalState, Double>
    ) {
        emotionalStateCache[npcId] = CachedEmotionalState(
            npcId = npcId,
            emotionalState = state,
            intensity = intensity,
            transitionProbabilities = transitionProbabilities,
            cacheTime = System.currentTimeMillis()
        )
    }
    
    private fun calculateTransitionProbabilities(currentEmotion: EmotionalState): Map<EmotionalState, Double> {
        // Simplified transition probability calculation
        return mapOf(
            EmotionalState.NEUTRAL to 0.1,
            EmotionalState.HOPEFUL to when(currentEmotion) {
                EmotionalState.NEUTRAL -> 0.3
                EmotionalState.BITTER -> 0.1
                else -> 0.05
            },
            EmotionalState.BITTER to when(currentEmotion) {
                EmotionalState.BETRAYED -> 0.4
                EmotionalState.HOPEFUL -> 0.2
                else -> 0.1
            }
            // Add more transition probabilities as needed
        )
    }
    
    private suspend fun predictivelyCache(
        npcId: String,
        currentState: EmotionalState,
        transitionProbabilities: Map<EmotionalState, Double>
    ) {
        // Cache likely next emotional states
        transitionProbabilities.forEach { (state, probability) ->
            if (probability > 0.2) { // Only cache high probability transitions
                cacheScope.launch {
                    delay(100) // Small delay to not impact current processing
                    // Pre-generate dialogue for likely transitions
                    val preGeneratedDialogue = generateFallbackDialogue(state, "NEUTRAL")
                    val dialogueKey = generateDialogueKey(npcId, state, "NEUTRAL", "predictive")
                    cacheDialogue(dialogueKey, preGeneratedDialogue, state.name, "NEUTRAL")
                }
            }
        }
    }
    
    private suspend fun processEmotionalState(data: Any): Any {
        delay(50) // Simulate processing
        return "Emotional state processed"
    }
    
    private suspend fun updateConsciousness(data: Any): Any {
        delay(100) // Simulate consciousness update
        return "Consciousness updated"
    }
    
    private suspend fun consolidateMemory(data: Any): Any {
        delay(75) // Simulate memory consolidation
        return "Memory consolidated"
    }
    
    private suspend fun evolveBehavior(data: Any): Any {
        delay(150) // Simulate behavior evolution
        return "Behavior evolved"
    }
    
    private suspend fun preprocessDialogue(data: Any): Any {
        delay(25) // Simulate dialogue preprocessing
        return "Dialogue preprocessed"
    }
    
    private fun performIntelligentCacheEviction(priority: CachePriority) {
        val itemsToEvict = mutableListOf<String>()
        
        // First pass: remove expired items
        memoryCache.entries.forEach { (key, item) ->
            if (item.isExpired()) {
                itemsToEvict.add(key)
            }
        }
        
        // Second pass: remove least recently used items if still over limit
        if (memoryCache.size - itemsToEvict.size > MAX_CACHE_SIZE) {
            val sortedByAccess = memoryCache.entries
                .filter { !itemsToEvict.contains(it.key) }
                .sortedBy { it.value.lastAccessed }
                .take((memoryCache.size - itemsToEvict.size) - MAX_CACHE_SIZE)
            
            itemsToEvict.addAll(sortedByAccess.map { it.key })
        }
        
        itemsToEvict.forEach { memoryCache.remove(it) }
        
        _cacheStatistics.value = _cacheStatistics.value.copy(
            cacheEvictions = _cacheStatistics.value.cacheEvictions + itemsToEvict.size
        )
    }
    
    private fun cleanupDialogueCache() {
        val oldestEntries = dialogueCache.entries
            .sortedBy { it.value.cacheTime }
            .take(dialogueCache.size - MAX_DIALOGUE_CACHE_SIZE + CACHE_CLEANUP_BATCH_SIZE)
        
        oldestEntries.forEach { 
            dialogueCache.remove(it.key) 
        }
    }
    
    private fun performCacheCleanup() {
        // Clean expired emotional states
        val expiredEmotionalStates = emotionalStateCache.entries
            .filter { !it.value.isValid() }
            .map { it.key }
        
        expiredEmotionalStates.forEach { 
            emotionalStateCache.remove(it) 
        }
        
        // Clean old dialogue cache entries
        val oldDialogueEntries = dialogueCache.entries
            .filter { System.currentTimeMillis() - it.value.cacheTime > TimeUnit.HOURS.toMillis(1) }
            .map { it.key }
        
        oldDialogueEntries.forEach { 
            dialogueCache.remove(it) 
        }
        
        updateCacheStatistics()
    }
    
    private fun updatePerformanceMetrics() {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        
        _performanceMetrics.value = _performanceMetrics.value.copy(
            memoryUsageKB = usedMemory / 1024,
            activeCoroutines = Thread.activeCount(),
            cacheHitRate = calculateCacheHitRate()
        )
    }
    
    private fun updateCacheStatistics(hit: Boolean? = null) {
        val current = _cacheStatistics.value
        
        _cacheStatistics.value = current.copy(
            totalCacheSize = memoryCache.size,
            emotionalStateCacheSize = emotionalStateCache.size,
            dialogueCacheSize = dialogueCache.size,
            cacheHits = if (hit == true) current.cacheHits + 1 else current.cacheHits,
            cacheMisses = if (hit == false) current.cacheMisses + 1 else current.cacheMisses
        )
    }
    
    private fun calculateCacheHitRate(): Double {
        val stats = _cacheStatistics.value
        val totalRequests = stats.cacheHits + stats.cacheMisses
        return if (totalRequests > 0) stats.cacheHits.toDouble() / totalRequests else 0.0
    }
    
    private fun calculateAverageTime(currentAverage: Long, newTime: Long): Long {
        return (currentAverage + newTime) / 2
    }
    
    enum class CachePriority {
        LOW, NORMAL, HIGH, CRITICAL
    }
    
    companion object {
        private const val MAX_CACHE_SIZE = 1000
        private const val MAX_DIALOGUE_CACHE_SIZE = 500
        private const val CACHE_CLEANUP_BATCH_SIZE = 50
    }
}