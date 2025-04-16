package com.example.jsonviewer.data.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Service for handling local storage of JSON data and app preferences
 */
class LocalStorageService(context: Context) {

    companion object {
        private const val PREFS_NAME = "json_viewer_prefs"
        private const val KEY_IS_DARK_THEME = "is_dark_theme"
        private const val KEY_RECENT_FILES = "recent_files"
        private const val KEY_SAVED_JSONS = "saved_jsons"
        private const val MAX_RECENT_FILES = 10
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Theme preference
    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: Flow<Boolean> = _isDarkTheme.asStateFlow()

    // Recent files list
    private val _recentFiles = MutableStateFlow<List<RecentFile>>(emptyList())
    val recentFiles: Flow<List<RecentFile>> = _recentFiles.asStateFlow()

    // Saved JSON documents
    private val _savedJsons = MutableStateFlow<List<SavedJson>>(emptyList())
    val savedJsons: Flow<List<SavedJson>> = _savedJsons.asStateFlow()

    init {
        // Load initial data
        loadThemePreference()
        loadRecentFiles()
        loadSavedJsons()
    }

    // THEME PREFERENCES

    fun loadThemePreference() {
        _isDarkTheme.value = sharedPreferences.getBoolean(KEY_IS_DARK_THEME, false)
    }

    fun saveThemePreference(isDark: Boolean) {
        sharedPreferences.edit {
            putBoolean(KEY_IS_DARK_THEME, isDark)
        }
        _isDarkTheme.value = isDark
    }

    // RECENT FILES

    fun loadRecentFiles() {
        val recentFilesJson = sharedPreferences.getString(KEY_RECENT_FILES, "[]") ?: "[]"
        _recentFiles.value = JsonStorageUtils.fromRecentFileJsonList(recentFilesJson)
    }

    fun addRecentFile(name: String, content: String) {
        val currentFiles = _recentFiles.value.toMutableList()

        // Remove existing file with same name if present
        currentFiles.removeAll { it.name == name }

        // Add the new file at the beginning
        currentFiles.add(0, RecentFile(name, content, System.currentTimeMillis()))

        // Limit the list size
        if (currentFiles.size > MAX_RECENT_FILES) {
            currentFiles.removeAt(currentFiles.lastIndex)
        }

        _recentFiles.value = currentFiles

        // Save to SharedPreferences
        sharedPreferences.edit {
            putString(KEY_RECENT_FILES, JsonStorageUtils.toJson(currentFiles))
        }
    }

    fun clearRecentFiles() {
        _recentFiles.value = emptyList()
        sharedPreferences.edit {
            putString(KEY_RECENT_FILES, "[]")
        }
    }

    // SAVED JSON DOCUMENTS

    fun loadSavedJsons() {
        val savedJsonsJson = sharedPreferences.getString(KEY_SAVED_JSONS, "[]") ?: "[]"
        _savedJsons.value = JsonStorageUtils.fromSavedJsonList(savedJsonsJson)
    }

    fun saveJson(name: String, content: String, id: String = UUID.randomUUID().toString()): String {
        val currentSavedJsons = _savedJsons.value.toMutableList()

        // Check if we're updating an existing document
        val existingIndex = currentSavedJsons.indexOfFirst { it.id == id }

        if (existingIndex >= 0) {
            // Update existing
            currentSavedJsons[existingIndex] = SavedJson(
                id = id,
                name = name,
                content = content,
                lastModified = System.currentTimeMillis()
            )
        } else {
            // Add new
            currentSavedJsons.add(
                SavedJson(
                    id = id,
                    name = name,
                    content = content,
                    lastModified = System.currentTimeMillis()
                )
            )
        }

        _savedJsons.value = currentSavedJsons

        // Save to SharedPreferences
        sharedPreferences.edit {
            putString(KEY_SAVED_JSONS, JsonStorageUtils.toJson(currentSavedJsons))
        }

        // Add to recent files as well
        addRecentFile(name, content)

        return id
    }

    fun deleteJson(id: String) {
        val currentSavedJsons = _savedJsons.value.toMutableList()
        currentSavedJsons.removeAll { it.id == id }

        _savedJsons.value = currentSavedJsons

        // Save to SharedPreferences
        sharedPreferences.edit {
            putString(KEY_SAVED_JSONS, JsonStorageUtils.toJson(currentSavedJsons))
        }
    }

    fun getSavedJson(id: String): SavedJson? {
        return _savedJsons.value.find { it.id == id }
    }
}

/**
 * Data model for a recently opened file
 */
data class RecentFile(
    val name: String,
    val content: String,
    val timestamp: Long
)

/**
 * Data model for a saved JSON document
 */
data class SavedJson(
    val id: String,
    val name: String,
    val content: String,
    val lastModified: Long
)