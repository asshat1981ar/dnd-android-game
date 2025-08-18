package com.orion.dndgame.dice

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * D&D dice rolling service with support for all standard dice types
 * Includes advanced features like advantage/disadvantage and exploding dice
 */
@Singleton
class DiceRoller @Inject constructor() {
    
    var lastD20Roll: Int = 0
        private set
    
    private val rollHistory = mutableListOf<DiceRollResult>()
    
    /**
     * Roll a standard d20
     */
    fun rollD20(): Int {
        val result = Random.nextInt(1, 21)
        lastD20Roll = result
        addToHistory(DiceRollResult("1d20", listOf(result), result, System.currentTimeMillis()))
        return result
    }
    
    /**
     * Roll d20 with advantage (roll twice, take higher)
     */
    fun rollD20WithAdvantage(): Int {
        val roll1 = Random.nextInt(1, 21)
        val roll2 = Random.nextInt(1, 21)
        val result = maxOf(roll1, roll2)
        lastD20Roll = result
        
        addToHistory(DiceRollResult(
            "1d20 (Advantage)", 
            listOf(roll1, roll2), 
            result, 
            System.currentTimeMillis(),
            modifier = "advantage"
        ))
        
        return result
    }
    
    /**
     * Roll d20 with disadvantage (roll twice, take lower)
     */
    fun rollD20WithDisadvantage(): Int {
        val roll1 = Random.nextInt(1, 21)
        val roll2 = Random.nextInt(1, 21)
        val result = minOf(roll1, roll2)
        lastD20Roll = result
        
        addToHistory(DiceRollResult(
            "1d20 (Disadvantage)", 
            listOf(roll1, roll2), 
            result, 
            System.currentTimeMillis(),
            modifier = "disadvantage"
        ))
        
        return result
    }
    
    /**
     * Roll any sided die
     */
    fun rollDie(sides: Int): Int {
        val result = Random.nextInt(1, sides + 1)
        addToHistory(DiceRollResult("1d$sides", listOf(result), result, System.currentTimeMillis()))
        return result
    }
    
    /**
     * Roll multiple dice of the same type
     */
    fun rollDice(count: Int, sides: Int): List<Int> {
        val rolls = (1..count).map { Random.nextInt(1, sides + 1) }
        val total = rolls.sum()
        addToHistory(DiceRollResult("${count}d$sides", rolls, total, System.currentTimeMillis()))
        return rolls
    }
    
    /**
     * Roll damage dice from a damage string (e.g., "1d8+3", "2d6+1")
     */
    fun rollDamage(damageString: String, doubled: Boolean = false): Int {
        val parsed = parseDamageString(damageString)
        
        val baseRolls = rollDice(parsed.count * (if (doubled) 2 else 1), parsed.sides)
        val total = baseRolls.sum() + parsed.modifier
        
        val notation = if (doubled) "${damageString} (Critical)" else damageString
        addToHistory(DiceRollResult(notation, baseRolls, total, System.currentTimeMillis()))
        
        return maxOf(1, total) // Minimum 1 damage
    }
    
    /**
     * Roll for ability scores (4d6, drop lowest)
     */
    fun rollAbilityScore(): Int {
        val rolls = rollDice(4, 6)
        val result = rolls.sorted().drop(1).sum()
        
        addToHistory(DiceRollResult(
            "4d6 (drop lowest)", 
            rolls, 
            result, 
            System.currentTimeMillis(),
            modifier = "ability_score"
        ))
        
        return result
    }
    
    /**
     * Roll for hit points
     */
    fun rollHitPoints(hitDie: Int, constitution: Int): Int {
        val roll = rollDie(hitDie)
        val constitutionModifier = (constitution - 10) / 2
        val result = maxOf(1, roll + constitutionModifier) // Minimum 1 HP
        
        addToHistory(DiceRollResult(
            "1d$hitDie + $constitutionModifier (HP)", 
            listOf(roll), 
            result, 
            System.currentTimeMillis(),
            modifier = "hit_points"
        ))
        
        return result
    }
    
    /**
     * Roll initiative
     */
    fun rollInitiative(dexterityModifier: Int): Int {
        val roll = rollD20()
        val result = roll + dexterityModifier
        
        addToHistory(DiceRollResult(
            "1d20 + $dexterityModifier (Initiative)", 
            listOf(roll), 
            result, 
            System.currentTimeMillis(),
            modifier = "initiative"
        ))
        
        return result
    }
    
    /**
     * Roll a skill check
     */
    fun rollSkillCheck(
        skillModifier: Int, 
        advantage: Boolean = false, 
        disadvantage: Boolean = false
    ): SkillCheckResult {
        val baseRoll = when {
            advantage -> rollD20WithAdvantage()
            disadvantage -> rollD20WithDisadvantage()
            else -> rollD20()
        }
        
        val total = baseRoll + skillModifier
        
        val result = SkillCheckResult(
            baseRoll = baseRoll,
            modifier = skillModifier,
            total = total,
            hasAdvantage = advantage,
            hasDisadvantage = disadvantage,
            timestamp = System.currentTimeMillis()
        )
        
        return result
    }
    
    /**
     * Roll a saving throw
     */
    fun rollSavingThrow(
        saveModifier: Int,
        difficulty: Int,
        advantage: Boolean = false,
        disadvantage: Boolean = false
    ): SavingThrowResult {
        val baseRoll = when {
            advantage -> rollD20WithAdvantage()
            disadvantage -> rollD20WithDisadvantage()
            else -> rollD20()
        }
        
        val total = baseRoll + saveModifier
        val success = total >= difficulty
        
        return SavingThrowResult(
            baseRoll = baseRoll,
            modifier = saveModifier,
            total = total,
            difficulty = difficulty,
            success = success,
            hasAdvantage = advantage,
            hasDisadvantage = disadvantage,
            timestamp = System.currentTimeMillis()
        )
    }
    
    /**
     * Roll percentile dice (d100)
     */
    fun rollPercentile(): Int {
        val tens = Random.nextInt(0, 10) * 10
        val ones = Random.nextInt(1, 11)
        val result = if (tens == 0 && ones == 10) 100 else tens + ones
        
        addToHistory(DiceRollResult("1d100", listOf(tens, ones), result, System.currentTimeMillis()))
        
        return result
    }
    
    /**
     * Custom dice expression parser and roller
     */
    fun rollExpression(expression: String): DiceExpressionResult {
        try {
            val terms = parseExpression(expression)
            var total = 0
            val allRolls = mutableListOf<DiceRollResult>()
            
            for (term in terms) {
                when (term) {
                    is DiceTerm -> {
                        val rolls = rollDice(term.count, term.sides)
                        val termTotal = rolls.sum()
                        total += if (term.operator == "+") termTotal else -termTotal
                        
                        allRolls.add(DiceRollResult(
                            "${term.count}d${term.sides}", 
                            rolls, 
                            termTotal, 
                            System.currentTimeMillis()
                        ))
                    }
                    is ConstantTerm -> {
                        total += if (term.operator == "+") term.value else -term.value
                    }
                }
            }
            
            return DiceExpressionResult(
                expression = expression,
                terms = terms,
                rolls = allRolls,
                total = total,
                timestamp = System.currentTimeMillis()
            )
            
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid dice expression: $expression")
        }
    }
    
    /**
     * Get roll history
     */
    fun getRollHistory(limit: Int = 50): List<DiceRollResult> {
        return rollHistory.takeLast(limit).reversed()
    }
    
    /**
     * Clear roll history
     */
    fun clearHistory() {
        rollHistory.clear()
    }
    
    private fun addToHistory(result: DiceRollResult) {
        rollHistory.add(result)
        if (rollHistory.size > 100) {
            rollHistory.removeFirst()
        }
    }
    
    private fun parseDamageString(damageString: String): DamageComponents {
        val cleanString = damageString.trim().lowercase()
        
        // Pattern: XdY+Z or XdY-Z or XdY
        val regex = """(\d+)d(\d+)([+-]\d+)?""".toRegex()
        val match = regex.find(cleanString) 
            ?: throw IllegalArgumentException("Invalid damage string: $damageString")
        
        val count = match.groupValues[1].toInt()
        val sides = match.groupValues[2].toInt()
        val modifierString = match.groupValues[3]
        val modifier = if (modifierString.isNotEmpty()) {
            modifierString.toInt()
        } else {
            0
        }
        
        return DamageComponents(count, sides, modifier)
    }
    
    private fun parseExpression(expression: String): List<ExpressionTerm> {
        val terms = mutableListOf<ExpressionTerm>()
        val cleanExpression = expression.replace(" ", "")
        
        // Simple parser for basic dice expressions
        // This is a simplified implementation
        val regex = """([+-])?(\d+d\d+|\d+)""".toRegex()
        val matches = regex.findAll(cleanExpression)
        
        for ((index, match) in matches.withIndex()) {
            val operator = if (index == 0 && match.groupValues[1].isEmpty()) "+" else match.groupValues[1].ifEmpty { "+" }
            val termString = match.groupValues[2]
            
            if (termString.contains("d")) {
                val parts = termString.split("d")
                terms.add(DiceTerm(
                    operator = operator,
                    count = parts[0].toInt(),
                    sides = parts[1].toInt()
                ))
            } else {
                terms.add(ConstantTerm(
                    operator = operator,
                    value = termString.toInt()
                ))
            }
        }
        
        return terms
    }
}

/**
 * Data classes for dice rolling
 */

data class DiceRollResult(
    val notation: String,
    val individualRolls: List<Int>,
    val total: Int,
    val timestamp: Long,
    val modifier: String? = null
)

data class SkillCheckResult(
    val baseRoll: Int,
    val modifier: Int,
    val total: Int,
    val hasAdvantage: Boolean,
    val hasDisadvantage: Boolean,
    val timestamp: Long
) {
    fun beats(difficulty: Int): Boolean = total >= difficulty
    fun isCriticalSuccess(): Boolean = baseRoll == 20
    fun isCriticalFailure(): Boolean = baseRoll == 1
}

data class SavingThrowResult(
    val baseRoll: Int,
    val modifier: Int,
    val total: Int,
    val difficulty: Int,
    val success: Boolean,
    val hasAdvantage: Boolean,
    val hasDisadvantage: Boolean,
    val timestamp: Long
) {
    fun isCriticalSuccess(): Boolean = baseRoll == 20
    fun isCriticalFailure(): Boolean = baseRoll == 1
}

data class DiceExpressionResult(
    val expression: String,
    val terms: List<ExpressionTerm>,
    val rolls: List<DiceRollResult>,
    val total: Int,
    val timestamp: Long
)

private data class DamageComponents(
    val count: Int,
    val sides: Int,
    val modifier: Int
)

sealed class ExpressionTerm {
    abstract val operator: String
}

data class DiceTerm(
    override val operator: String,
    val count: Int,
    val sides: Int
) : ExpressionTerm()

data class ConstantTerm(
    override val operator: String,
    val value: Int
) : ExpressionTerm()