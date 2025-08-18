package com.orion.dndgame.combat

import com.orion.dndgame.data.models.*
import com.orion.dndgame.dice.DiceRoller
import com.orion.dndgame.eds.core.EmotionalEvent
import com.orion.dndgame.eds.core.EmotionalState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * D&D 5e Combat Engine with emotional integration
 * Handles turn-based combat, initiative, actions, and reactions
 */
@Singleton
class CombatEngine @Inject constructor(
    private val diceRoller: DiceRoller
) {
    
    private val _combatState = MutableStateFlow<CombatState?>(null)
    val combatState: StateFlow<CombatState?> = _combatState.asStateFlow()
    
    private val _combatLog = MutableStateFlow<List<CombatLogEntry>>(emptyList())
    val combatLog: StateFlow<List<CombatLogEntry>> = _combatLog.asStateFlow()
    
    /**
     * Start a new combat encounter
     */
    fun startCombat(
        playerCharacters: List<Character>,
        npcs: List<NPC>,
        environment: CombatEnvironment = CombatEnvironment()
    ): CombatState {
        val combatants = createCombatants(playerCharacters, npcs)
        val initiativeOrder = rollInitiative(combatants)
        
        val newCombatState = CombatState(
            id = generateCombatId(),
            combatants = combatants,
            initiativeOrder = initiativeOrder,
            currentTurnIndex = 0,
            round = 1,
            phase = CombatPhase.COMBAT,
            environment = environment,
            activeEffects = emptyList(),
            startTime = System.currentTimeMillis()
        )
        
        _combatState.value = newCombatState
        addLogEntry("Combat begins!", CombatLogType.SYSTEM)
        
        return newCombatState
    }
    
    /**
     * Process a combat action
     */
    suspend fun processAction(action: CombatAction): CombatActionResult {
        val currentState = _combatState.value ?: return CombatActionResult.InvalidAction("No active combat")
        val actor = currentState.getCurrentCombatant() ?: return CombatActionResult.InvalidAction("No current combatant")
        
        // Validate action
        val validation = validateAction(action, actor, currentState)
        if (!validation.isValid) {
            return CombatActionResult.InvalidAction(validation.reason)
        }
        
        // Execute action based on type
        val result = when (action.type) {
            CombatActionType.ATTACK -> processAttack(action, actor, currentState)
            CombatActionType.CAST_SPELL -> processCastSpell(action, actor, currentState)
            CombatActionType.USE_ABILITY -> processUseAbility(action, actor, currentState)
            CombatActionType.MOVE -> processMove(action, actor, currentState)
            CombatActionType.DODGE -> processDodge(action, actor, currentState)
            CombatActionType.HELP -> processHelp(action, actor, currentState)
            CombatActionType.HIDE -> processHide(action, actor, currentState)
            CombatActionType.READY -> processReady(action, actor, currentState)
            CombatActionType.END_TURN -> processEndTurn(action, actor, currentState)
        }
        
        // Update combat state
        updateCombatState(result)
        
        // Check for combat end conditions
        checkCombatEnd()
        
        return result
    }
    
    /**
     * Process an attack action
     */
    private suspend fun processAttack(
        action: CombatAction,
        attacker: Combatant,
        combatState: CombatState
    ): CombatActionResult {
        val target = combatState.combatants.find { it.id == action.targetId }
            ?: return CombatActionResult.InvalidAction("Invalid target")
        
        val weapon = action.weaponId?.let { weaponId ->
            attacker.character?.equipment?.find { it.id == weaponId }
        }
        
        // Calculate attack roll
        val attackBonus = calculateAttackBonus(attacker, weapon)
        val attackRoll = diceRoller.rollD20() + attackBonus
        val targetAC = target.character?.armorClass ?: target.npc?.armorClass ?: 10
        
        val isHit = attackRoll >= targetAC
        val isCritical = diceRoller.lastD20Roll == 20
        val isCriticalMiss = diceRoller.lastD20Roll == 1
        
        if (isHit && !isCriticalMiss) {
            // Calculate damage
            val baseDamage = weapon?.damage ?: "1d4" // Unarmed strike
            val damageBonus = calculateDamageBonus(attacker, weapon)
            val damage = if (isCritical) {
                diceRoller.rollDamage(baseDamage, doubled = true) + damageBonus
            } else {
                diceRoller.rollDamage(baseDamage) + damageBonus
            }
            
            // Apply damage
            val newHitPoints = max(0, target.currentHitPoints - damage)
            val updatedTarget = target.copy(currentHitPoints = newHitPoints)
            
            // Create emotional impact for NPCs
            val emotionalEvents = createCombatEmotionalEvents(
                action = action,
                attacker = attacker,
                target = updatedTarget,
                damage = damage,
                isCritical = isCritical
            )
            
            addLogEntry(
                "${attacker.name} attacks ${target.name} for $damage damage${if (isCritical) " (Critical Hit!)" else ""}",
                CombatLogType.ATTACK
            )
            
            if (newHitPoints <= 0) {
                addLogEntry("${target.name} is defeated!", CombatLogType.DEFEAT)
            }
            
            return CombatActionResult.AttackSuccess(
                attacker = attacker,
                target = updatedTarget,
                damage = damage,
                isCritical = isCritical,
                emotionalEvents = emotionalEvents
            )
        } else {
            addLogEntry("${attacker.name} misses ${target.name}", CombatLogType.ATTACK)
            
            return CombatActionResult.AttackMiss(
                attacker = attacker,
                target = target,
                attackRoll = attackRoll,
                targetAC = targetAC
            )
        }
    }
    
    /**
     * Process spell casting
     */
    private suspend fun processCastSpell(
        action: CombatAction,
        caster: Combatant,
        combatState: CombatState
    ): CombatActionResult {
        val spell = action.spellId?.let { spellId ->
            caster.character?.knownSpells?.find { it.id == spellId }
        } ?: return CombatActionResult.InvalidAction("Spell not found")
        
        // Check spell slot availability
        val spellLevel = action.spellLevel ?: spell.level
        if (!hasSpellSlot(caster, spellLevel)) {
            return CombatActionResult.InvalidAction("No spell slots available")
        }
        
        // Consume spell slot
        consumeSpellSlot(caster, spellLevel)
        
        // Process spell effects
        val result = when (spell.school) {
            SpellSchool.EVOCATION -> processEvocationSpell(spell, caster, action, combatState)
            SpellSchool.ENCHANTMENT -> processEnchantmentSpell(spell, caster, action, combatState)
            SpellSchool.ABJURATION -> processAbjurationSpell(spell, caster, action, combatState)
            SpellSchool.CONJURATION -> processConjurationSpell(spell, caster, action, combatState)
            SpellSchool.DIVINATION -> processDivinationSpell(spell, caster, action, combatState)
            SpellSchool.ILLUSION -> processIllusionSpell(spell, caster, action, combatState)
            SpellSchool.NECROMANCY -> processNecromancySpell(spell, caster, action, combatState)
            SpellSchool.TRANSMUTATION -> processTransmutationSpell(spell, caster, action, combatState)
        }
        
        addLogEntry("${caster.name} casts ${spell.name}", CombatLogType.SPELL)
        
        return result
    }
    
    /**
     * Create emotional events from combat actions
     */
    private fun createCombatEmotionalEvents(
        action: CombatAction,
        attacker: Combatant,
        target: Combatant,
        damage: Int,
        isCritical: Boolean
    ): List<EmotionalEvent> {
        val events = mutableListOf<EmotionalEvent>()
        
        // Target emotional response to being attacked
        if (target.npc != null) {
            val attackImpact = when {
                damage >= target.maxHitPoints * 0.5f -> 0.8f // Massive damage
                damage >= target.maxHitPoints * 0.25f -> 0.5f // Significant damage
                else -> 0.3f // Minor damage
            }
            
            events.add(
                EmotionalEvent(
                    id = "combat_${System.currentTimeMillis()}_${target.id}",
                    type = "combat_damage_received",
                    description = "Received $damage damage from ${attacker.name}",
                    timestamp = System.currentTimeMillis(),
                    emotionalImpact = mapOf(
                        EmotionalState.FEARFUL.name to attackImpact,
                        EmotionalState.WRATHFUL.name to attackImpact * 0.7f,
                        EmotionalState.HOPEFUL.name to -attackImpact * 0.5f
                    ),
                    persistenceLevel = if (isCritical) 0.9f else 0.6f,
                    source = "combat_system"
                )
            )
        }
        
        // Attacker emotional response to dealing damage
        if (attacker.npc != null) {
            val confidence = if (isCritical) 0.7f else 0.4f
            
            events.add(
                EmotionalEvent(
                    id = "combat_${System.currentTimeMillis()}_${attacker.id}",
                    type = "combat_damage_dealt",
                    description = "Successfully attacked ${target.name} for $damage damage",
                    timestamp = System.currentTimeMillis(),
                    emotionalImpact = mapOf(
                        EmotionalState.HOPEFUL.name to confidence,
                        EmotionalState.JOYFUL.name to confidence * 0.5f,
                        EmotionalState.FEARFUL.name to -confidence * 0.3f
                    ),
                    persistenceLevel = 0.5f,
                    source = "combat_system"
                )
            )
        }
        
        return events
    }
    
    /**
     * Calculate attack bonus for a combatant
     */
    private fun calculateAttackBonus(combatant: Combatant, weapon: Equipment?): Int {
        val character = combatant.character
        val npc = combatant.npc
        
        return when {
            character != null -> {
                val abilityModifier = if (weapon?.properties?.contains("finesse") == true) {
                    max(getAbilityModifier(character.strength), getAbilityModifier(character.dexterity))
                } else {
                    getAbilityModifier(character.strength)
                }
                abilityModifier + character.proficiencyBonus
            }
            npc != null -> {
                // Simplified NPC attack bonus based on challenge rating
                (npc.challengeRating * 2).toInt() + 2
            }
            else -> 0
        }
    }
    
    /**
     * Calculate damage bonus for a combatant
     */
    private fun calculateDamageBonus(combatant: Combatant, weapon: Equipment?): Int {
        val character = combatant.character
        
        return when {
            character != null -> {
                if (weapon?.properties?.contains("finesse") == true) {
                    max(getAbilityModifier(character.strength), getAbilityModifier(character.dexterity))
                } else {
                    getAbilityModifier(character.strength)
                }
            }
            else -> 0
        }
    }
    
    /**
     * Get ability modifier from ability score
     */
    private fun getAbilityModifier(abilityScore: Int): Int {
        return (abilityScore - 10) / 2
    }
    
    /**
     * Roll initiative for all combatants
     */
    private fun rollInitiative(combatants: List<Combatant>): List<String> {
        return combatants.map { combatant ->
            val dexModifier = when {
                combatant.character != null -> getAbilityModifier(combatant.character.dexterity)
                combatant.npc != null -> 0 // Simplified for NPCs
                else -> 0
            }
            val initiative = diceRoller.rollD20() + dexModifier
            
            combatant.id to initiative
        }.sortedByDescending { it.second }
            .map { it.first }
    }
    
    /**
     * Create combatants from characters and NPCs
     */
    private fun createCombatants(characters: List<Character>, npcs: List<NPC>): List<Combatant> {
        val characterCombatants = characters.map { character ->
            Combatant(
                id = character.id,
                name = character.name,
                character = character,
                npc = null,
                currentHitPoints = character.hitPoints,
                maxHitPoints = character.maxHitPoints,
                temporaryHitPoints = 0,
                isPlayerControlled = true,
                position = CombatPosition(0, 0), // Default position
                conditions = emptyList(),
                concentrationSpell = null
            )
        }
        
        val npcCombatants = npcs.map { npc ->
            Combatant(
                id = npc.id,
                name = npc.name,
                character = null,
                npc = npc,
                currentHitPoints = npc.hitPoints,
                maxHitPoints = npc.maxHitPoints,
                temporaryHitPoints = 0,
                isPlayerControlled = false,
                position = CombatPosition(0, 0), // Default position
                conditions = emptyList(),
                concentrationSpell = null
            )
        }
        
        return characterCombatants + npcCombatants
    }
    
    private fun validateAction(action: CombatAction, actor: Combatant, state: CombatState): ActionValidation {
        // Basic validation logic
        if (actor.currentHitPoints <= 0) {
            return ActionValidation(false, "Actor is unconscious")
        }
        
        if (action.type == CombatActionType.ATTACK && action.targetId == null) {
            return ActionValidation(false, "Attack requires a target")
        }
        
        return ActionValidation(true, "")
    }
    
    private fun hasSpellSlot(caster: Combatant, level: Int): Boolean {
        return caster.character?.spellSlots?.get(level)?.let { it > 0 } ?: false
    }
    
    private fun consumeSpellSlot(caster: Combatant, level: Int) {
        // Implementation would update the character's spell slots
        // This would typically be handled by the character service
    }
    
    // Placeholder implementations for spell processing
    private fun processEvocationSpell(spell: Spell, caster: Combatant, action: CombatAction, state: CombatState): CombatActionResult {
        return CombatActionResult.SpellSuccess(caster, spell, emptyList())
    }
    
    private fun processEnchantmentSpell(spell: Spell, caster: Combatant, action: CombatAction, state: CombatState): CombatActionResult {
        return CombatActionResult.SpellSuccess(caster, spell, emptyList())
    }
    
    private fun processAbjurationSpell(spell: Spell, caster: Combatant, action: CombatAction, state: CombatState): CombatActionResult {
        return CombatActionResult.SpellSuccess(caster, spell, emptyList())
    }
    
    private fun processConjurationSpell(spell: Spell, caster: Combatant, action: CombatAction, state: CombatState): CombatActionResult {
        return CombatActionResult.SpellSuccess(caster, spell, emptyList())
    }
    
    private fun processDivinationSpell(spell: Spell, caster: Combatant, action: CombatAction, state: CombatState): CombatActionResult {
        return CombatActionResult.SpellSuccess(caster, spell, emptyList())
    }
    
    private fun processIllusionSpell(spell: Spell, caster: Combatant, action: CombatAction, state: CombatState): CombatActionResult {
        return CombatActionResult.SpellSuccess(caster, spell, emptyList())
    }
    
    private fun processNecromancySpell(spell: Spell, caster: Combatant, action: CombatAction, state: CombatState): CombatActionResult {
        return CombatActionResult.SpellSuccess(caster, spell, emptyList())
    }
    
    private fun processTransmutationSpell(spell: Spell, caster: Combatant, action: CombatAction, state: CombatState): CombatActionResult {
        return CombatActionResult.SpellSuccess(caster, spell, emptyList())
    }
    
    private fun processMove(action: CombatAction, actor: Combatant, state: CombatState): CombatActionResult {
        return CombatActionResult.MoveSuccess(actor, action.position ?: CombatPosition(0, 0))
    }
    
    private fun processDodge(action: CombatAction, actor: Combatant, state: CombatState): CombatActionResult {
        return CombatActionResult.DodgeSuccess(actor)
    }
    
    private fun processHelp(action: CombatAction, actor: Combatant, state: CombatState): CombatActionResult {
        return CombatActionResult.HelpSuccess(actor, action.targetId ?: "")
    }
    
    private fun processHide(action: CombatAction, actor: Combatant, state: CombatState): CombatActionResult {
        return CombatActionResult.HideSuccess(actor)
    }
    
    private fun processReady(action: CombatAction, actor: Combatant, state: CombatState): CombatActionResult {
        return CombatActionResult.ReadySuccess(actor, action.trigger ?: "")
    }
    
    private fun processEndTurn(action: CombatAction, actor: Combatant, state: CombatState): CombatActionResult {
        return CombatActionResult.EndTurnSuccess(actor)
    }
    
    private fun updateCombatState(result: CombatActionResult) {
        // Update the combat state based on the action result
        // This would modify _combatState.value appropriately
    }
    
    private fun checkCombatEnd() {
        // Check if combat should end (all enemies defeated, etc.)
    }
    
    private fun addLogEntry(message: String, type: CombatLogType) {
        val entry = CombatLogEntry(
            message = message,
            type = type,
            timestamp = System.currentTimeMillis()
        )
        _combatLog.value = _combatLog.value + entry
    }
    
    private fun generateCombatId(): String {
        return "combat_${System.currentTimeMillis()}_${Random.nextInt(1000, 9999)}"
    }
}

/**
 * Data classes for combat system
 */

data class CombatState(
    val id: String,
    val combatants: List<Combatant>,
    val initiativeOrder: List<String>,
    val currentTurnIndex: Int,
    val round: Int,
    val phase: CombatPhase,
    val environment: CombatEnvironment,
    val activeEffects: List<CombatEffect>,
    val startTime: Long
) {
    fun getCurrentCombatant(): Combatant? {
        return if (currentTurnIndex < initiativeOrder.size) {
            val currentId = initiativeOrder[currentTurnIndex]
            combatants.find { it.id == currentId }
        } else null
    }
}

data class Combatant(
    val id: String,
    val name: String,
    val character: Character?,
    val npc: NPC?,
    val currentHitPoints: Int,
    val maxHitPoints: Int,
    val temporaryHitPoints: Int,
    val isPlayerControlled: Boolean,
    val position: CombatPosition,
    val conditions: List<CombatCondition>,
    val concentrationSpell: String?
)

data class CombatPosition(
    val x: Int,
    val y: Int
)

data class CombatEnvironment(
    val terrain: String = "normal",
    val lighting: String = "bright",
    val weather: String = "clear",
    val hazards: List<String> = emptyList(),
    val cover: Map<String, String> = emptyMap() // Position to cover type
)

data class CombatAction(
    val type: CombatActionType,
    val actorId: String,
    val targetId: String? = null,
    val weaponId: String? = null,
    val spellId: String? = null,
    val spellLevel: Int? = null,
    val position: CombatPosition? = null,
    val trigger: String? = null,
    val metadata: Map<String, Any> = emptyMap()
)

data class CombatEffect(
    val id: String,
    val name: String,
    val duration: Int, // Rounds
    val effect: String,
    val targetId: String
)

data class CombatCondition(
    val type: String,
    val duration: Int,
    val source: String
)

data class CombatLogEntry(
    val message: String,
    val type: CombatLogType,
    val timestamp: Long
)

data class ActionValidation(
    val isValid: Boolean,
    val reason: String
)

enum class CombatPhase {
    INITIATIVE, COMBAT, END
}

enum class CombatActionType {
    ATTACK, CAST_SPELL, USE_ABILITY, MOVE, DODGE, HELP, HIDE, READY, END_TURN
}

enum class CombatLogType {
    SYSTEM, ATTACK, SPELL, MOVEMENT, CONDITION, DEFEAT
}

/**
 * Sealed class for combat action results
 */
sealed class CombatActionResult {
    data class AttackSuccess(
        val attacker: Combatant,
        val target: Combatant,
        val damage: Int,
        val isCritical: Boolean,
        val emotionalEvents: List<EmotionalEvent>
    ) : CombatActionResult()
    
    data class AttackMiss(
        val attacker: Combatant,
        val target: Combatant,
        val attackRoll: Int,
        val targetAC: Int
    ) : CombatActionResult()
    
    data class SpellSuccess(
        val caster: Combatant,
        val spell: Spell,
        val targets: List<Combatant>
    ) : CombatActionResult()
    
    data class MoveSuccess(
        val actor: Combatant,
        val newPosition: CombatPosition
    ) : CombatActionResult()
    
    data class DodgeSuccess(
        val actor: Combatant
    ) : CombatActionResult()
    
    data class HelpSuccess(
        val actor: Combatant,
        val targetId: String
    ) : CombatActionResult()
    
    data class HideSuccess(
        val actor: Combatant
    ) : CombatActionResult()
    
    data class ReadySuccess(
        val actor: Combatant,
        val trigger: String
    ) : CombatActionResult()
    
    data class EndTurnSuccess(
        val actor: Combatant
    ) : CombatActionResult()
    
    data class InvalidAction(
        val reason: String
    ) : CombatActionResult()
}