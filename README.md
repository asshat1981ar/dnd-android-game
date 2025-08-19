# 🎮 D&D Android Game - Emotional Dialogue System

[![CI/CD Pipeline](https://github.com/asshat1981ar/dnd-android-game/workflows/🎮%20D&D%20Android%20Game%20-%20CI/CD%20Pipeline/badge.svg)](https://github.com/asshat1981ar/dnd-android-game/actions)
[![EDS Testing](https://github.com/asshat1981ar/dnd-android-game/workflows/🎭%20EDS%20(Emotional%20Dialogue%20System)%20Testing/badge.svg)](https://github.com/asshat1981ar/dnd-android-game/actions)
[![Code Quality](https://github.com/asshat1981ar/dnd-android-game/workflows/🔍%20Kotlin%20Code%20Quality%20&%20Formatting/badge.svg)](https://github.com/asshat1981ar/dnd-android-game/actions)

An immersive D&D mobile RPG featuring an advanced **Emotional Dialogue System (EDS)** that creates dynamic, emotionally responsive NPC interactions. Built with Kotlin and Jetpack Compose for Android.

## ✨ Key Features

### 🎭 Enhanced Emotional Dialogue System (EDS)
- **Dynamic Emotional States**: 8 distinct emotional states (Confident, Anxious, Angry, Afraid, Excited, Sad, Surprised, Neutral)
- **Seamless Transitions**: Smooth emotional state changes with visual and audio feedback
- **Context-Aware Responses**: NPCs react intelligently based on conversation history and current emotional context
- **Real-Time Feedback**: Visual indicators and animations that respond to emotional changes
- **ProjectChimera Integration**: Advanced dialogue adapter for complex narrative scenarios

### 🎲 D&D Game Mechanics
- **Dynamic Quest Adaptation**: Quests that adapt based on player choices and emotional states
- **Combat Integration**: Emotional states affect combat dialogue and NPC reactions
- **Character Progression**: Complete character management with stats, inventory, and abilities
- **Dice Rolling System**: Authentic D&D dice mechanics with emotional modifiers

### 🌐 Real-Time Connectivity
- **WebSocket Integration**: Real-time connection to Orion backend orchestrator
- **Agent Coordination**: Seamless integration with AI game master agents
- **Offline Mode**: Local database support for offline gameplay
- **Auto-Reconnection**: Intelligent reconnection with exponential backoff

### 📱 Modern Android Architecture
- **Jetpack Compose UI**: Modern, declarative UI framework
- **Room Database**: Efficient local data storage
- **Coroutines**: Asynchronous processing for smooth performance
- **MVVM Pattern**: Clean architecture with proper separation of concerns
- **Dependency Injection**: Hilt for efficient dependency management

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    🎮 D&D Android Game                      │
├─────────────────────────────────────────────────────────────┤
│  📱 UI Layer (Jetpack Compose)                             │
│  ├── DialogueScreen      ├── HomeScreen                    │
│  ├── EmotionalFeedback   ├── LoadingScreen                 │
│  └── ConnectionStatus    └── Navigation                    │
├─────────────────────────────────────────────────────────────┤
│  🧠 Business Logic Layer                                   │
│  ├── 🎭 EDS (Emotional Dialogue System)                   │
│  │   ├── DialogueEngine     ├── EmotionalStates           │
│  │   ├── ContextAnalyzer    ├── ProjectChimeraAdapter     │
│  │   └── EnhancedEDS       └── TransitionManager          │
│  ├── 🎲 Game Systems                                      │
│  │   ├── QuestAdaptation    ├── CombatEngine              │
│  │   ├── CharacterManager   ├── DiceRoller                │
│  │   └── GameState         └── NPCManager                 │
│  └── 🌐 Network Layer                                     │
│      ├── WebSocketClient    ├── AgentCoordinator          │
│      ├── ApiService        ├── ConnectionManager          │
│      └── RetryMechanism    └── MessageQueue               │
├─────────────────────────────────────────────────────────────┤
│  💾 Data Layer                                             │
│  ├── Room Database        ├── SharedPreferences           │
│  ├── Repository Pattern   ├── Local Caching              │
│  └── Data Models         └── Performance Optimization     │
├─────────────────────────────────────────────────────────────┤
│  🌐 Backend Integration (Orion Platform)                   │
│  ├── AI Game Master      ├── Quest Generator              │
│  ├── Character Manager   ├── World Builder                │
│  └── Rules Lawyer        └── NPC Consciousness            │
└─────────────────────────────────────────────────────────────┘
```

## 🚀 Getting Started

### Prerequisites
- **Android Studio** Arctic Fox or newer
- **JDK 17** or higher
- **Android SDK** API level 34
- **Gradle 8.2.1** or higher

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/asshat1981ar/dnd-android-game.git
   cd dnd-android-game
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an existing Android Studio project"
   - Navigate to the cloned directory

3. **Build the project**
   ```bash
   ./gradlew build
   ```

4. **Run on device/emulator**
   ```bash
   ./gradlew installDebug
   ```

## 🎭 Emotional Dialogue System (EDS) Usage

### Basic Implementation
```kotlin
// Initialize EDS
val eds = EnhancedEmotionalDialogueSystem()

// Set initial emotional state
eds.setEmotionalState(EmotionalState.CONFIDENT)

// Generate contextual response
val response = eds.generateResponse(
    context = "player_intimidation_attempt",
    npcPersonality = NPCPersonality.NERVOUS_GUARD,
    previousInteractions = conversationHistory
)

// Transition emotions smoothly
eds.transitionEmotion(
    from = EmotionalState.CONFIDENT,
    to = EmotionalState.AFRAID,
    duration = 2000L,
    trigger = "intimidation_success"
)
```

### Advanced Features
```kotlin
// Context-aware dialogue
val contextAnalyzer = DialogueContextAnalyzer()
val context = contextAnalyzer.analyzeCurrentSituation(
    playerActions = recentActions,
    environmentalFactors = currentScene,
    npcMemory = previousEncounters
)

// ProjectChimera integration for complex narratives
val adapter = ProjectChimeraDialogueAdapter()
val enrichedDialogue = adapter.enhanceWithNarrative(
    basicResponse = response,
    narrativeContext = currentQuestLine,
    emotionalUndertones = eds.getCurrentEmotionalState()
)
```

## 🎲 Quest System Integration

```kotlin
// Dynamic quest adaptation based on emotional interactions
val questSystem = DynamicQuestAdaptationSystem()
questSystem.adaptQuest(
    currentQuest = activeQuest,
    playerEmotionalHistory = eds.getEmotionalHistory(),
    npcRelationships = relationshipManager.getCurrentStanding(),
    worldState = gameWorldState
)
```

## 🌐 Backend Integration

### WebSocket Connection
```kotlin
// Initialize WebSocket connection to Orion backend
val webSocketClient = OrionWebSocketClient(
    serverUrl = "ws://orion-backend:3000/game",
    authToken = userAuthToken
)

// Handle real-time game events
webSocketClient.onMessage { message ->
    when (message.type) {
        "emotional_cue" -> eds.processEmotionalCue(message.data)
        "quest_update" -> questSystem.handleUpdate(message.data)
        "npc_behavior" -> npcManager.updateBehavior(message.data)
    }
}
```

### Agent Coordination
```kotlin
// Coordinate with Orion AI agents
val agentCoordinator = OrionGameAgentCoordinator()
agentCoordinator.requestGameMasterDecision(
    scenario = currentGameScenario,
    playerActions = recentPlayerActions,
    emotionalContext = eds.getCurrentContext()
)
```

## 🧪 Testing

### Run Unit Tests
```bash
./gradlew testDebugUnitTest
```

### Run EDS-Specific Tests
```bash
./gradlew testDebugUnitTest --tests="*EmotionalDialogue*"
```

### Run Integration Tests
```bash
./gradlew connectedDebugAndroidTest
```

### Performance Testing
```bash
./gradlew testDebugUnitTest --tests="*Performance*"
```

## 📊 Performance Optimization

### EDS Performance
- **Response Time**: < 200ms for dialogue generation
- **Emotional Transitions**: < 50ms for state changes
- **Memory Usage**: < 50MB for EDS components
- **Battery Optimization**: Efficient background processing

### App Performance
- **Cold Start**: < 2 seconds
- **APK Size**: Optimized with R8/ProGuard
- **Memory**: < 256MB heap usage
- **Network**: Intelligent batching and caching

## 🔧 Configuration

### EDS Configuration
```kotlin
// Configure emotional sensitivity
val edsConfig = EDSConfiguration(
    emotionalSensitivity = 0.7f,
    transitionDuration = 1500L,
    contextWindowSize = 10,
    memoryRetentionPeriod = Duration.ofHours(24)
)
```

### WebSocket Configuration
```kotlin
// Configure connection parameters
val wsConfig = WebSocketConfig(
    reconnectionDelay = 5.seconds,
    maxReconnectionAttempts = 5,
    pingInterval = 30.seconds,
    messageQueueSize = 100
)
```

## 📚 API Documentation

### Core EDS Classes
- **`EnhancedEmotionalDialogueSystem`**: Main EDS coordinator
- **`DialogueEngine`**: Core dialogue generation
- **`EmotionalState`**: Emotional state definitions
- **`DialogueContextAnalyzer`**: Context analysis and understanding
- **`ProjectChimeraDialogueAdapter`**: Advanced narrative integration

### Game Systems
- **`DynamicQuestAdaptationSystem`**: Quest adaptation engine
- **`OrionGameAgentCoordinator`**: Backend agent integration
- **`CombatEngine`**: Combat system with EDS integration
- **`CharacterRepository`**: Character data management

### Network Layer
- **`OrionWebSocketClient`**: Real-time communication
- **`ApiService`**: REST API integration
- **`ConnectionManager`**: Network state management

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines
- Follow Kotlin coding conventions
- Write unit tests for new features
- Update documentation for API changes
- Ensure CI/CD pipeline passes
- Test EDS integration thoroughly

## 📋 Project Roadmap

### 🎯 Current Sprint
- [ ] Enhanced NPC memory system
- [ ] Multi-character conversations
- [ ] Advanced emotional prediction
- [ ] Performance optimizations

### 🔮 Future Features
- [ ] Voice dialogue integration
- [ ] Multiplayer party system
- [ ] Advanced AI dungeon master
- [ ] Cross-platform synchronization
- [ ] VR/AR integration

## 🐛 Known Issues

- WebSocket reconnection may take up to 30 seconds in poor network conditions
- Some emotional transitions may not render smoothly on older devices (< Android 7.0)
- Large conversation histories may cause memory pressure

## 🔒 Security

- All network communications are encrypted
- User data is stored securely with Room database encryption
- Authentication tokens are managed through secure storage
- No sensitive data is logged in production builds

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **Orion Platform**: Backend orchestration and AI agents
- **Jetpack Compose Team**: Modern Android UI framework
- **Kotlin Team**: Excellent programming language
- **D&D Community**: Inspiration and game mechanics
- **Open Source Contributors**: Various libraries and tools

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/asshat1981ar/dnd-android-game/issues)
- **Discussions**: [GitHub Discussions](https://github.com/asshat1981ar/dnd-android-game/discussions)
- **Email**: support@orion-dnd-game.com

---

**🎮 Built with ❤️ for D&D enthusiasts and mobile RPG lovers**