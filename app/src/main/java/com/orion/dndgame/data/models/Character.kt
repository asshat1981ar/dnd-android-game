package com.orion.dndgame.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.orion.dndgame.eds.core.EmotionalProfile
import com.orion.dndgame.eds.core.EmotionalState

/**
 * Player Character model for D&D game
 */
@Entity(tableName = "characters")
@TypeConverters(CharacterConverters::class)
data class Character(
    @PrimaryKey
    val id: String,
    val name: String,
    val race: CharacterRace,
    val characterClass: CharacterClass,
    val level: Int,
    val experience: Int,
    val hitPoints: Int,
    val maxHitPoints: Int,
    val armorClass: Int,
    val speed: Int,
    val proficiencyBonus: Int,
    
    // Ability Scores
    val strength: Int,
    val dexterity: Int,
    val constitution: Int,
    val intelligence: Int,
    val wisdom: Int,
    val charisma: Int,
    
    // Skills and Proficiencies
    val skills: Map<String, Int>,
    val proficientSkills: List<String>,
    val languages: List<String>,
    
    // Equipment and Inventory
    val equipment: List<Equipment>,
    val inventory: List<InventoryItem>,
    val gold: Int,
    
    // Spells (for spellcasters)
    val spellSlots: Map<Int, Int>, // Level to available slots
    val knownSpells: List<Spell>,
    val preparedSpells: List<String>, // Spell IDs
    
    // Character Background
    val background: String,
    val personalityTraits: List<String>,
    val ideals: List<String>,
    val bonds: List<String>,
    val flaws: List<String>,
    
    // Game State
    val currentLocation: String,
    val questIds: List<String>,
    val companionIds: List<String>,
    
    // Timestamps
    val createdAt: Long,
    val lastUpdated: Long
)

/**
 * NPC model with emotional intelligence and consciousness integration
 */
@Entity(tableName = "npcs")
@TypeConverters(NPCConverters::class)
data class NPC(
    @PrimaryKey
    val id: String,
    val name: String,
    val title: String? = null,
    val description: String,
    val race: CharacterRace,
    val characterClass: CharacterClass?,
    val level: Int,
    
    // Basic Stats (simplified for NPCs)
    val hitPoints: Int,
    val maxHitPoints: Int,
    val armorClass: Int,
    val challengeRating: Float,
    
    // Personality and Consciousness
    val personality: Map<String, Float>, // Trait to strength mapping
    val emotionalProfile: EmotionalProfile,
    val consciousnessLevel: Float = 0.5f,
    
    // Dialogue and Interaction
    val defaultDialogue: String,
    val questIds: List<String> = emptyList(),
    val canTrade: Boolean = false,
    val canTeach: Boolean = false,
    val canHire: Boolean = false,
    
    // Location and Behavior
    val currentLocation: String,
    val roamingArea: String? = null,
    val schedulePattern: String? = null, // JSON describing daily routine
    
    // Relationships
    val faction: String? = null,
    val allies: List<String> = emptyList(),
    val enemies: List<String> = emptyList(),
    
    // Game Mechanics
    val isEssential: Boolean = false, // Cannot be killed
    val respawnTime: Long? = null, // If killable, when they respawn
    val lootTable: List<LootEntry> = emptyList(),
    
    val createdAt: Long,
    val lastUpdated: Long
)

/**
 * Equipment item
 */
data class Equipment(
    val id: String,
    val name: String,
    val type: EquipmentType,
    val slot: EquipmentSlot,
    val armorClass: Int? = null,
    val damage: String? = null, // e.g., "1d8"
    val damageType: DamageType? = null,
    val properties: List<String> = emptyList(),
    val weight: Float,
    val value: Int, // in gold pieces
    val rarity: ItemRarity = ItemRarity.COMMON,
    val description: String,
    val requiresAttunement: Boolean = false,
    val magicalProperties: List<MagicalProperty> = emptyList()
)

/**
 * Inventory item with quantity
 */
data class InventoryItem(
    val itemId: String,
    val name: String,
    val description: String,
    val type: ItemType,
    val quantity: Int,
    val weight: Float,
    val value: Int,
    val rarity: ItemRarity = ItemRarity.COMMON,
    val consumable: Boolean = false,
    val stackable: Boolean = true
)

/**
 * Spell definition
 */
data class Spell(
    val id: String,
    val name: String,
    val level: Int,
    val school: SpellSchool,
    val castingTime: String,
    val range: String,
    val components: List<String>, // V, S, M
    val duration: String,
    val description: String,
    val higherLevelEffect: String? = null,
    val damage: String? = null,
    val damageType: DamageType? = null,
    val savingThrow: AbilityScore? = null,
    val spellAttack: Boolean = false,
    val concentration: Boolean = false,
    val ritual: Boolean = false
)

/**
 * Loot entry for NPCs
 */
data class LootEntry(
    val itemId: String,
    val dropChance: Float, // 0.0 to 1.0
    val quantity: IntRange = 1..1,
    val conditions: List<String> = emptyList() // Conditions for dropping
)

/**
 * Magical property for equipment
 */
data class MagicalProperty(
    val name: String,
    val description: String,
    val effect: String, // Mechanical effect
    val charges: Int? = null,
    val rechargeCondition: String? = null
)

/**
 * Enums for character system
 */

enum class CharacterRace(val displayName: String, val abilityBonuses: Map<String, Int>) {
    HUMAN("Human", mapOf("all" to 1)),
    ELF("Elf", mapOf("dexterity" to 2)),
    DWARF("Dwarf", mapOf("constitution" to 2)),
    HALFLING("Halfling", mapOf("dexterity" to 2)),
    DRAGONBORN("Dragonborn", mapOf("strength" to 2, "charisma" to 1)),
    GNOME("Gnome", mapOf("intelligence" to 2)),
    HALF_ELF("Half-Elf", mapOf("charisma" to 2)),
    HALF_ORC("Half-Orc", mapOf("strength" to 2, "constitution" to 1)),
    TIEFLING("Tiefling", mapOf("charisma" to 2, "intelligence" to 1))
}

enum class CharacterClass(
    val displayName: String,
    val hitDie: Int,
    val primaryAbility: String,
    val savingThrowProficiencies: List<String>,
    val isSpellcaster: Boolean = false
) {
    FIGHTER("Fighter", 10, "strength", listOf("strength", "constitution")),
    WIZARD("Wizard", 6, "intelligence", listOf("intelligence", "wisdom"), true),
    ROGUE("Rogue", 8, "dexterity", listOf("dexterity", "intelligence")),
    CLERIC("Cleric", 8, "wisdom", listOf("wisdom", "charisma"), true),
    RANGER("Ranger", 10, "dexterity", listOf("strength", "dexterity"), true),
    PALADIN("Paladin", 10, "strength", listOf("wisdom", "charisma"), true),
    BARBARIAN("Barbarian", 12, "strength", listOf("strength", "constitution")),
    BARD("Bard", 8, "charisma", listOf("dexterity", "charisma"), true),
    SORCERER("Sorcerer", 6, "charisma", listOf("constitution", "charisma"), true),
    WARLOCK("Warlock", 8, "charisma", listOf("wisdom", "charisma"), true),
    DRUID("Druid", 8, "wisdom", listOf("intelligence", "wisdom"), true),
    MONK("Monk", 8, "dexterity", listOf("strength", "dexterity"))
}

enum class EquipmentType {
    WEAPON, ARMOR, SHIELD, TOOL, INSTRUMENT, GEAR, WONDROUS_ITEM
}

enum class EquipmentSlot {
    MAIN_HAND, OFF_HAND, ARMOR, HELMET, GLOVES, BOOTS, CLOAK, RING, AMULET, BELT
}

enum class DamageType {
    SLASHING, PIERCING, BLUDGEONING, FIRE, COLD, LIGHTNING, THUNDER, 
    ACID, POISON, PSYCHIC, RADIANT, NECROTIC, FORCE
}

enum class ItemType {
    WEAPON, ARMOR, CONSUMABLE, TOOL, TREASURE, QUEST_ITEM, MATERIAL, BOOK, KEY
}

enum class ItemRarity {
    COMMON, UNCOMMON, RARE, VERY_RARE, LEGENDARY, ARTIFACT
}

enum class SpellSchool {
    ABJURATION, CONJURATION, DIVINATION, ENCHANTMENT, 
    EVOCATION, ILLUSION, NECROMANCY, TRANSMUTATION
}

enum class AbilityScore {
    STRENGTH, DEXTERITY, CONSTITUTION, INTELLIGENCE, WISDOM, CHARISMA
}

/**
 * Type converters for Room database
 */
class CharacterConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringIntMap(value: Map<String, Int>): String = gson.toJson(value)

    @TypeConverter
    fun toStringIntMap(value: String): Map<String, Int> {
        val type = object : TypeToken<Map<String, Int>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromStringList(value: List<String>): String = gson.toJson(value)

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromEquipmentList(value: List<Equipment>): String = gson.toJson(value)

    @TypeConverter
    fun toEquipmentList(value: String): List<Equipment> {
        val type = object : TypeToken<List<Equipment>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromInventoryItemList(value: List<InventoryItem>): String = gson.toJson(value)

    @TypeConverter
    fun toInventoryItemList(value: String): List<InventoryItem> {
        val type = object : TypeToken<List<InventoryItem>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromSpellList(value: List<Spell>): String = gson.toJson(value)

    @TypeConverter
    fun toSpellList(value: String): List<Spell> {
        val type = object : TypeToken<List<Spell>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromIntIntMap(value: Map<Int, Int>): String = gson.toJson(value)

    @TypeConverter
    fun toIntIntMap(value: String): Map<Int, Int> {
        val type = object : TypeToken<Map<Int, Int>>() {}.type
        return gson.fromJson(value, type)
    }
}

class NPCConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringFloatMap(value: Map<String, Float>): String = gson.toJson(value)

    @TypeConverter
    fun toStringFloatMap(value: String): Map<String, Float> {
        val type = object : TypeToken<Map<String, Float>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromEmotionalProfile(value: EmotionalProfile): String = gson.toJson(value)

    @TypeConverter
    fun toEmotionalProfile(value: String): EmotionalProfile = gson.fromJson(value, EmotionalProfile::class.java)

    @TypeConverter
    fun fromStringList(value: List<String>): String = gson.toJson(value)

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromLootEntryList(value: List<LootEntry>): String = gson.toJson(value)

    @TypeConverter
    fun toLootEntryList(value: String): List<LootEntry> {
        val type = object : TypeToken<List<LootEntry>>() {}.type
        return gson.fromJson(value, type)
    }
}