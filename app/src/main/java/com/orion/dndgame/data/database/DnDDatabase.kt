package com.orion.dndgame.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.orion.dndgame.data.database.dao.*
import com.orion.dndgame.data.models.*

/**
 * Main Room database for the D&D game application
 * Stores all game data including characters, NPCs, quests, and game sessions
 */
@Database(
    entities = [
        Character::class,
        NPC::class,
        Quest::class,
        GameSession::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    CharacterConverters::class,
    NPCConverters::class,
    QuestConverters::class,
    GameSessionConverters::class
)
abstract class DnDDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao
    abstract fun npcDao(): NPCDao
    abstract fun questDao(): QuestDao
    abstract fun gameSessionDao(): GameSessionDao
}