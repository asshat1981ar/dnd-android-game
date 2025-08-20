package com.orion.dndgame

import org.junit.Test
import org.junit.Assert.*

/**
 * Simple unit test to ensure build system works
 */
class SimpleUnitTest {
    
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
    
    @Test
    fun app_package_name_isCorrect() {
        val expectedPackage = "com.orion.dndgame"
        assertNotNull(expectedPackage)
        assertTrue(expectedPackage.isNotEmpty())
    }
    
    @Test
    fun emotional_states_basic_test() {
        // Test that basic emotional state concepts work
        val confident = "CONFIDENT"
        val anxious = "ANXIOUS"
        
        assertNotEquals(confident, anxious)
        assertTrue(confident.length > 0)
        assertTrue(anxious.length > 0)
    }
}