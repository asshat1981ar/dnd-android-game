package com.orion.dndgame.data.database.dao

import androidx.room.*
import com.orion.dndgame.data.models.Character
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Character operations
 */
@Dao
interface CharacterDao {

    @Query("SELECT * FROM characters ORDER BY lastUpdated DESC")
    fun getAllCharacters(): Flow<List<Character>>

    @Query("SELECT * FROM characters WHERE id = :characterId")
    suspend fun getCharacterById(characterId: String): Character?

    @Query("SELECT * FROM characters WHERE id = :characterId")
    fun getCharacterByIdFlow(characterId: String): Flow<Character?>

    @Query("SELECT * FROM characters WHERE name LIKE :name")
    suspend fun getCharactersByName(name: String): List<Character>

    @Query("SELECT * FROM characters WHERE level >= :minLevel AND level <= :maxLevel")
    suspend fun getCharactersByLevel(minLevel: Int, maxLevel: Int): List<Character>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: Character)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacters(characters: List<Character>)

    @Update
    suspend fun updateCharacter(character: Character)

    @Delete
    suspend fun deleteCharacter(character: Character)

    @Query("DELETE FROM characters WHERE id = :characterId")
    suspend fun deleteCharacterById(characterId: String)

    @Query("DELETE FROM characters")
    suspend fun deleteAllCharacters()

    @Query("SELECT COUNT(*) FROM characters")
    suspend fun getCharacterCount(): Int

    @Query("SELECT * FROM characters WHERE experience >= :minExperience ORDER BY experience DESC")
    suspend fun getCharactersByExperience(minExperience: Int): List<Character>

    @Query("UPDATE characters SET hitPoints = :hitPoints WHERE id = :characterId")
    suspend fun updateCharacterHitPoints(characterId: String, hitPoints: Int)

    @Query("UPDATE characters SET experience = :experience, level = :level WHERE id = :characterId")
    suspend fun updateCharacterExperience(characterId: String, experience: Int, level: Int)

    @Query("UPDATE characters SET gold = :gold WHERE id = :characterId")
    suspend fun updateCharacterGold(characterId: String, gold: Int)

    @Query("UPDATE characters SET currentLocation = :location WHERE id = :characterId")
    suspend fun updateCharacterLocation(characterId: String, location: String)
}