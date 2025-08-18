package com.orion.dndgame.di

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.orion.dndgame.BuildConfig
import com.orion.dndgame.combat.CombatEngine
import com.orion.dndgame.data.database.DnDDatabase
import com.orion.dndgame.data.database.dao.*
import com.orion.dndgame.data.repository.*
import com.orion.dndgame.dice.DiceRoller
import com.orion.dndgame.eds.dialogue.*
import com.orion.dndgame.network.ApiService
import com.orion.dndgame.network.OrionWebSocketClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Dependency injection module for the D&D game application
 * Provides all major dependencies including network, database, and game systems
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(loggingInterceptor)
        }

        return builder.build()
    }

    @Provides
    @Singleton
    fun provideApiService(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): ApiService {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideDnDDatabase(
        @ApplicationContext context: Context
    ): DnDDatabase {
        return Room.databaseBuilder(
            context,
            DnDDatabase::class.java,
            "dnd_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideCharacterDao(database: DnDDatabase): CharacterDao {
        return database.characterDao()
    }

    @Provides
    @Singleton
    fun provideNPCDao(database: DnDDatabase): NPCDao {
        return database.npcDao()
    }

    @Provides
    @Singleton
    fun provideQuestDao(database: DnDDatabase): QuestDao {
        return database.questDao()
    }

    @Provides
    @Singleton
    fun provideGameSessionDao(database: DnDDatabase): GameSessionDao {
        return database.gameSessionDao()
    }

    @Provides
    @Singleton
    fun provideCharacterRepository(
        characterDao: CharacterDao,
        apiService: ApiService
    ): CharacterRepository {
        return CharacterRepositoryImpl(characterDao, apiService)
    }

    @Provides
    @Singleton
    fun provideNPCRepository(
        npcDao: NPCDao,
        apiService: ApiService
    ): NPCRepository {
        return NPCRepositoryImpl(npcDao, apiService)
    }

    @Provides
    @Singleton
    fun provideQuestRepository(
        questDao: QuestDao,
        apiService: ApiService
    ): QuestRepository {
        return QuestRepositoryImpl(questDao, apiService)
    }

    @Provides
    @Singleton
    fun provideGameSessionRepository(
        gameSessionDao: GameSessionDao,
        apiService: ApiService
    ): GameSessionRepository {
        return GameSessionRepositoryImpl(gameSessionDao, apiService)
    }

    @Provides
    @Singleton
    fun provideDiceRoller(): DiceRoller {
        return DiceRoller()
    }

    @Provides
    @Singleton
    fun provideOrionWebSocketClient(
        gson: Gson
    ): OrionWebSocketClient {
        return OrionWebSocketClient(gson)
    }

    @Provides
    @Singleton
    fun provideProjectChimeraDialogueAdapter(
        orionClient: OrionWebSocketClient
    ): ProjectChimeraDialogueAdapter {
        return ProjectChimeraDialogueAdapter(orionClient)
    }

    @Provides
    @Singleton
    fun provideDialogueContextAnalyzer(): DialogueContextAnalyzer {
        return DialogueContextAnalyzer()
    }

    @Provides
    @Singleton
    fun provideEmotionalResponseGenerator(): EmotionalResponseGenerator {
        return EmotionalResponseGenerator()
    }

    @Provides
    @Singleton
    fun provideDialogueMemoryManager(): DialogueMemoryManager {
        return DialogueMemoryManager()
    }

    @Provides
    @Singleton
    fun provideDialogueEngine(
        contextAnalyzer: DialogueContextAnalyzer,
        responseGenerator: EmotionalResponseGenerator,
        memoryManager: DialogueMemoryManager,
        chimeraIntegration: ProjectChimeraDialogueAdapter
    ): DialogueEngine {
        return DialogueEngine(
            contextAnalyzer,
            responseGenerator,
            memoryManager,
            chimeraIntegration
        )
    }

    @Provides
    @Singleton
    fun provideCombatEngine(
        diceRoller: DiceRoller
    ): CombatEngine {
        return CombatEngine(diceRoller)
    }

    @Provides
    @Singleton
    fun provideGameStateManager(
        characterRepository: CharacterRepository,
        npcRepository: NPCRepository,
        questRepository: QuestRepository,
        gameSessionRepository: GameSessionRepository,
        orionClient: OrionWebSocketClient
    ): GameStateManager {
        return GameStateManager(
            characterRepository,
            npcRepository,
            questRepository,
            gameSessionRepository,
            orionClient
        )
    }
}