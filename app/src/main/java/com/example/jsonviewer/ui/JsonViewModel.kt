package com.example.jsonviewer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jsonviewer.data.JsonNavigationItem
import com.example.jsonviewer.data.JsonParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

/**
 * Data state for JSON parsing and navigation
 */
sealed class JsonViewState {
    object Initial : JsonViewState()
    object Loading : JsonViewState()
    data class Error(val message: String) : JsonViewState()
    object Success : JsonViewState()
}

class JsonViewModel : ViewModel() {
    private val jsonParser = JsonParser()

    private val _jsonData = MutableStateFlow<Map<String, Any?>>(emptyMap())
    val jsonData: StateFlow<Map<String, Any?>> = _jsonData.asStateFlow()

    private val _currentItems = MutableStateFlow<List<JsonNavigationItem>>(emptyList())
    val currentItems: StateFlow<List<JsonNavigationItem>> = _currentItems.asStateFlow()

    private val _navigationPath = MutableStateFlow<List<String>>(emptyList())
    val navigationPath: StateFlow<List<String>> = _navigationPath.asStateFlow()

    // Use a public getter but keep setter private
    private val _viewState = MutableStateFlow<JsonViewState>(JsonViewState.Initial)
    val viewState: StateFlow<JsonViewState> = _viewState.asStateFlow()

    /**
     * Reset to the initial state
     */
    fun resetToInitial() {
        _viewState.value = JsonViewState.Initial
    }

    /**
     * Parse JSON string and initialize the viewer
     */
    fun parseJsonString(jsonString: String) {
        viewModelScope.launch {
            _viewState.value = JsonViewState.Loading

            try {
                // Add a small delay to show loading state (optional)
                delay(500)

                val parsedJson = jsonParser.parseJson(jsonString)
                _jsonData.value = parsedJson
                _navigationPath.value = emptyList()
                _currentItems.value = jsonParser.getObjectEntries(parsedJson)
                _viewState.value = JsonViewState.Success
            } catch (e: Exception) {
                // Handle parsing errors
                _viewState.value = JsonViewState.Error("Failed to parse JSON: ${e.message}")
                _jsonData.value = emptyMap()
                _navigationPath.value = emptyList()
                _currentItems.value = emptyList()
            }
        }
    }

    /**
     * Navigate to a specific node in the JSON structure
     */
    fun navigateTo(key: String, node: Any?) {
        viewModelScope.launch {
            val newPath = _navigationPath.value.toMutableList().apply { add(key) }
            _navigationPath.value = newPath

            when (node) {
                is Map<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    _currentItems.value = jsonParser.getObjectEntries(node as Map<String, Any?>)
                }
                is List<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    _currentItems.value = jsonParser.getArrayEntries(node as List<Any?>)
                }
                else -> {
                    // Primitive value reached, nothing to navigate further
                    _currentItems.value = emptyList()
                }
            }
        }
    }

    /**
     * Navigate back to the previous level
     */
    fun navigateBack(): Boolean {
        if (_navigationPath.value.isEmpty()) {
            return false
        }

        val newPath = _navigationPath.value.toMutableList().apply { removeAt(lastIndex) }
        _navigationPath.value = newPath

        // Traverse the JSON tree to get to the right place
        var current: Any? = _jsonData.value
        newPath.forEach { pathSegment ->
            current = when (current) {
                is Map<*, *> -> (current as Map<String, Any?>)[pathSegment]
                is List<*> -> (current as List<Any?>)[pathSegment.toInt()]
                else -> null
            }
        }

        // Update current items based on where we are
        when (current) {
            is Map<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                _currentItems.value = jsonParser.getObjectEntries(current as Map<String, Any?>)
            }
            is List<*> -> {
                @Suppress("UNCHECKED_CAST")
                _currentItems.value = jsonParser.getArrayEntries(current as List<Any?>)
            }
            else -> {
                // We should never reach here as we're going back to a container
                _currentItems.value = emptyList()
            }
        }

        return true
    }

    /**
     * Reset navigation to root
     */
    fun resetToRoot() {
        _navigationPath.value = emptyList()
        _currentItems.value = jsonParser.getObjectEntries(_jsonData.value)
    }
}