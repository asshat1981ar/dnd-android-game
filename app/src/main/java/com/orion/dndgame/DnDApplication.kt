package com.orion.dndgame

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Main Application class for the D&D Game with Orion integration
 * Handles global application state and dependency injection setup
 */
@HiltAndroidApp
class DnDApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Initialize any global application state here
        initializeLogging()
        initializeMetrics()
    }

    private fun initializeLogging() {
        // Setup logging framework
        // In production, you might want to use a more sophisticated logging solution
    }

    private fun initializeMetrics() {
        // Initialize performance and usage metrics
        // Could integrate with analytics services
    }
}