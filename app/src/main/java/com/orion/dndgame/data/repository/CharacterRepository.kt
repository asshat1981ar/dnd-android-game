package com.orion.dndgame.data.repository

import com.orion.dndgame.data.database.dao.CharacterDao
import com.orion.dndgame.data.models.Character
import com.orion.dndgame.network.ApiService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface CharacterRepository {
    fun getAllCharacters(): Flow<List<Character>>
    suspend fun getCharacterById(id: String): Character?
    suspend fun insertCharacter(character: Character)
    suspend fun updateCharacter(character: Character)
    suspend fun deleteCharacter(character: Character)
}

@Singleton
class CharacterRepositoryImpl @Inject constructor(
    private val characterDao: CharacterDao,
    private val apiService: ApiService
) : CharacterRepository {
    
    override fun getAllCharacters(): Flow<List<Character>> {
        return characterDao.getAllCharacters()
    }
    
    override suspend fun getCharacterById(id: String): Character? {
        return characterDao.getCharacterById(id)
    }
    
    override suspend fun insertCharacter(character: Character) {
        characterDao.insertCharacter(character)
    }
    
    override suspend fun updateCharacter(character: Character) {
        characterDao.updateCharacter(character)
    }
    
    override suspend fun deleteCharacter(character: Character) {
        characterDao.deleteCharacter(character)
    }
}

// Similar implementations for other repositories
interface NPCRepository {
    fun getAllNPCs(): Flow<List<com.orion.dndgame.data.models.NPC>>
    suspend fun getNPCById(id: String): com.orion.dndgame.data.models.NPC?
}

@Singleton
class NPCRepositoryImpl @Inject constructor(
    private val npcDao: com.orion.dndgame.data.database.dao.NPCDao,
    private val apiService: ApiService
) : NPCRepository {
    override fun getAllNPCs() = npcDao.getAllNPCs()
    override suspend fun getNPCById(id: String) = npcDao.getNPCById(id)
}

interface QuestRepository {
    fun getAllQuests(): Flow<List<com.orion.dndgame.data.models.Quest>>
}

@Singleton
class QuestRepositoryImpl @Inject constructor(
    private val questDao: com.orion.dndgame.data.database.dao.QuestDao,
    private val apiService: ApiService
) : QuestRepository {
    override fun getAllQuests() = questDao.getAllQuests()
}

interface GameSessionRepository {
    fun getAllSessions(): Flow<List<com.orion.dndgame.data.models.GameSession>>
}

@Singleton
class GameSessionRepositoryImpl @Inject constructor(
    private val sessionDao: com.orion.dndgame.data.database.dao.GameSessionDao,
    private val apiService: ApiService
) : GameSessionRepository {
    override fun getAllSessions() = sessionDao.getAllSessions()
}