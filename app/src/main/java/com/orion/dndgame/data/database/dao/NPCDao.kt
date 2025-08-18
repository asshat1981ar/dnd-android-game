package com.orion.dndgame.data.database.dao

import androidx.room.*
import com.orion.dndgame.data.models.NPC
import com.orion.dndgame.eds.core.EmotionalState
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for NPC operations with emotional state support
 */
@Dao
interface NPCDao {

    @Query("SELECT * FROM npcs ORDER BY lastUpdated DESC")
    fun getAllNPCs(): Flow<List<NPC>>

    @Query("SELECT * FROM npcs WHERE id = :npcId")
    suspend fun getNPCById(npcId: String): NPC?

    @Query("SELECT * FROM npcs WHERE id = :npcId")
    fun getNPCByIdFlow(npcId: String): Flow<NPC?>

    @Query("SELECT * FROM npcs WHERE currentLocation = :location")
    suspend fun getNPCsByLocation(location: String): List<NPC>

    @Query("SELECT * FROM npcs WHERE currentLocation = :location")
    fun getNPCsByLocationFlow(location: String): Flow<List<NPC>>

    @Query("SELECT * FROM npcs WHERE faction = :faction")
    suspend fun getNPCsByFaction(faction: String): List<NPC>

    @Query("SELECT * FROM npcs WHERE canTrade = 1")
    suspend fun getTradingNPCs(): List<NPC>

    @Query("SELECT * FROM npcs WHERE canTeach = 1")
    suspend fun getTeachingNPCs(): List<NPC>

    @Query("SELECT * FROM npcs WHERE isEssential = 1")
    suspend fun getEssentialNPCs(): List<NPC>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNPC(npc: NPC)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNPCs(npcs: List<NPC>)

    @Update
    suspend fun updateNPC(npc: NPC)

    @Delete
    suspend fun deleteNPC(npc: NPC)

    @Query("DELETE FROM npcs WHERE id = :npcId")
    suspend fun deleteNPCById(npcId: String)

    @Query("UPDATE npcs SET hitPoints = :hitPoints WHERE id = :npcId")
    suspend fun updateNPCHitPoints(npcId: String, hitPoints: Int)

    @Query("UPDATE npcs SET currentLocation = :location WHERE id = :npcId")
    suspend fun updateNPCLocation(npcId: String, location: String)

    @Query("SELECT COUNT(*) FROM npcs")
    suspend fun getNPCCount(): Int

    @Query("SELECT COUNT(*) FROM npcs WHERE currentLocation = :location")
    suspend fun getNPCCountByLocation(location: String): Int
}