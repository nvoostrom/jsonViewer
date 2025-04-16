package com.example.jsonviewer

import android.app.Application
import com.example.jsonviewer.data.storage.LocalStorageService

/**
 * Application class for JsonViewer to initialize app-wide components
 */
class JsonViewerApplication : Application() {

    // Single instance of LocalStorageService for the app
    lateinit var storageService: LocalStorageService
        private set

    override fun onCreate() {
        super.onCreate()

        // Initialize the storage service
        storageService = LocalStorageService(this)

        // Initialize other app-wide components here
    }

    companion object {
        // Helper to access the storage service from anywhere in the app
        @Volatile
        private var INSTANCE: JsonViewerApplication? = null

        fun getInstance(): JsonViewerApplication {
            return INSTANCE ?: synchronized(this) {
                throw IllegalStateException("JsonViewerApplication not initialized")
            }
        }
    }
}