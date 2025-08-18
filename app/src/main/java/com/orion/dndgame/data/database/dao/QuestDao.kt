package com.orion.dndgame.data.database.dao

import androidx.room.*
import com.orion.dndgame.data.models.Quest
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestDao {
    @Query("SELECT * FROM quests ORDER BY priority DESC")
    fun getAllQuests(): Flow<List<Quest>>
    
    @Query("SELECT * FROM quests WHERE id = :questId")
    suspend fun getQuestById(questId: String): Quest?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuest(quest: Quest)
    
    @Update
    suspend fun updateQuest(quest: Quest)
}

@Dao
interface GameSessionDao {
    @Query("SELECT * FROM game_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<com.orion.dndgame.data.models.GameSession>>
    
    @Insert
    suspend fun insertSession(session: com.orion.dndgame.data.models.GameSession)
}