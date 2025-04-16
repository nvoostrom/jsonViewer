package com.example.jsonviewer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jsonviewer.data.JsonNavigationItem
import com.example.jsonviewer.data.JsonParser
import com.example.jsonviewer.utils.JsonUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import org.json.JSONException

/**
 * Data state for JSON parsing and navigation
 */
sealed class JsonViewState {
    data object Initial : JsonViewState()
    data object Loading : JsonViewState()
    data class Error(val message: String) : JsonViewState()
    data object Success : JsonViewState()
}

/**
 * Enhanced ViewModel for JSON editing, navigation, and viewing
 */
class JsonViewModel : ViewModel() {
    private val jsonParser = JsonParser()

    // Raw JSON string
    private val _rawJsonString = MutableStateFlow("")
//    val rawJsonString: StateFlow<String> = _rawJsonString.asStateFlow()

    // Current JSON text being edited
    private val _currentJsonText = MutableStateFlow("")
    val currentJsonText: StateFlow<String> = _currentJsonText.asStateFlow()

    // JSON validity state
    private val _isJsonValid = MutableStateFlow(true)
    val isJsonValid: StateFlow<Boolean> = _isJsonValid.asStateFlow()

    // Raw JSON edit mode
    private val _isEditingRawJson = MutableStateFlow(false)
    val isEditingRawJson: StateFlow<Boolean> = _isEditingRawJson.asStateFlow()

    // Formatted JSON for display
    private val _formattedJsonString = MutableStateFlow("")
    val formattedJsonString: StateFlow<String> = _formattedJsonString.asStateFlow()

    // Is raw JSON prettified
    private val _isPrettified = MutableStateFlow(true)
    val isPrettified: StateFlow<Boolean> = _isPrettified.asStateFlow()

    private val _jsonData = MutableStateFlow<Map<String, Any?>>(emptyMap())
//    val jsonData: StateFlow<Map<String, Any?>> = _jsonData.asStateFlow()

    private val _currentItems = MutableStateFlow<List<JsonNavigationItem>>(emptyList())
    val currentItems: StateFlow<List<JsonNavigationItem>> = _currentItems.asStateFlow()

    private val _navigationPath = MutableStateFlow<List<String>>(emptyList())
    val navigationPath: StateFlow<List<String>> = _navigationPath.asStateFlow()

    // Use a public getter but keep setter private
    private val _viewState = MutableStateFlow<JsonViewState>(JsonViewState.Initial)
    val viewState: StateFlow<JsonViewState> = _viewState.asStateFlow()

    // Is viewing raw JSON
    private val _isViewingRawJson = MutableStateFlow(false)
    val isViewingRawJson: StateFlow<Boolean> = _isViewingRawJson.asStateFlow()

    // Is current parent an array
    private val _isCurrentArrayParent = MutableStateFlow(false)
    val isCurrentArrayParent: StateFlow<Boolean> = _isCurrentArrayParent.asStateFlow()

    /**
     * Update the current JSON text and validate it
     */
    fun updateJsonText(text: String) {
        _currentJsonText.value = text
        validateJson(text)
    }

    /**
     * Validate if the current JSON text is valid
     */
    private fun validateJson(jsonText: String) {
        _isJsonValid.value = if (jsonText.trim().isEmpty()) {
            true // Empty is considered valid for UX purposes
        } else {
            JsonUtils.isValidJson(jsonText)
        }
    }

    /**
     * Format the current JSON text
     */
    fun formatCurrentJson() {
        if (_isJsonValid.value && _currentJsonText.value.isNotEmpty()) {
            _currentJsonText.value = JsonUtils.prettifyJson(_currentJsonText.value)
        }
    }

    /**
     * Toggle raw JSON view
     */
    fun toggleRawJsonView() {
        _isViewingRawJson.value = !_isViewingRawJson.value
        _isEditingRawJson.value = false // Reset edit mode when toggling view
    }

    /**
     * Toggle raw JSON editing mode
     */
    fun toggleRawJsonEditing() {
        _isEditingRawJson.value = !_isEditingRawJson.value
    }

    /**
     * Save edited raw JSON
     */
    fun saveRawJsonChanges(editedJson: String) {
        if (JsonUtils.isValidJson(editedJson)) {
            parseJsonString(editedJson)
            _isEditingRawJson.value = false
        }
    }

    /**
     * Toggle between prettified and minified JSON
     */
    fun toggleJsonFormat() {
        _isPrettified.value = !_isPrettified.value
        updateFormattedJson()
    }

    /**
     * Update the formatted JSON based on current preferences
     */
    private fun updateFormattedJson() {
        if (_isPrettified.value) {
            _formattedJsonString.value = JsonUtils.prettifyJson(_rawJsonString.value)
        } else {
            _formattedJsonString.value = JsonUtils.minifyJson(_rawJsonString.value)
        }
    }

    /**
     * Reset to the initial state
     */
    fun resetToInitial() {
        _viewState.value = JsonViewState.Initial
        _isViewingRawJson.value = false
        _isEditingRawJson.value = false
        _currentJsonText.value = _rawJsonString.value // Preserve last JSON for editing
        _isJsonValid.value = true
    }

    /**
     * Parse JSON string and initialize the viewer
     */
    fun parseJsonString(jsonString: String) {
        viewModelScope.launch {
            _viewState.value = JsonViewState.Loading

            try {
                // Add a small delay to show loading state (optional)
                delay(300)

                if (!JsonUtils.isValidJson(jsonString)) {
                    throw JSONException("Invalid JSON format")
                }

                val parsedJson = jsonParser.parseJson(jsonString)
                _jsonData.value = parsedJson
                _navigationPath.value = emptyList()
                _currentItems.value = jsonParser.getObjectEntries(parsedJson)
                _rawJsonString.value = jsonString
                updateFormattedJson()

                // Determine if root is an array
                _isCurrentArrayParent.value = jsonString.trim().startsWith("[")

                _viewState.value = JsonViewState.Success
            } catch (e: Exception) {
                // Handle parsing errors
                _viewState.value = JsonViewState.Error("Failed to parse JSON: ${e.message}")
                _jsonData.value = emptyMap()
                _navigationPath.value = emptyList()
                _currentItems.value = emptyList()
                _rawJsonString.value = jsonString // Keep original input for reference
                _currentJsonText.value = jsonString // Keep the text for editing
                _isJsonValid.value = false
                _isCurrentArrayParent.value = false
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
                    _isCurrentArrayParent.value = false
                }
                is List<*> -> {
                    _currentItems.value = jsonParser.getArrayEntries(node as List<Any?>)
                    _isCurrentArrayParent.value = true
                }
                else -> {
                    // Primitive value reached, nothing to navigate further
                    _currentItems.value = emptyList()
                    _isCurrentArrayParent.value = false
                }
            }
        }
    }

    /**
     * Navigate back to the previous level
     */
    fun navigateBack(): Boolean {
        // If viewing raw JSON, go back to normal view
        if (_isViewingRawJson.value) {
            _isViewingRawJson.value = false
            _isEditingRawJson.value = false
            return true
        }

        if (_navigationPath.value.isEmpty()) {
            return false
        }

        val newPath = _navigationPath.value.toMutableList().apply { removeAt(lastIndex) }
        _navigationPath.value = newPath

        // Traverse the JSON tree to get to the right place
        var current: Any? = _jsonData.value
        newPath.forEach { pathSegment ->
            current = when (current) {
                is Map<*, *> -> (current as Map<*, *>)[pathSegment]
                is List<*> -> (current as List<Any?>)[pathSegment.toInt()]
                else -> null
            }
        }

        // Update current items based on where we are
        when (current) {
            is Map<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                _currentItems.value = jsonParser.getObjectEntries(current as Map<String, Any?>)
                _isCurrentArrayParent.value = false
            }
            is List<*> -> {
                _currentItems.value = jsonParser.getArrayEntries(current as List<Any?>)
                _isCurrentArrayParent.value = true
            }
            else -> {
                // This is the root level, which could be an object or array
                if (_rawJsonString.value.trim().startsWith("[")) {
                    _isCurrentArrayParent.value = true
                } else {
                    _isCurrentArrayParent.value = false
                }
                _currentItems.value = emptyList()
            }
        }

        return true
    }

    /**
     * Reset navigation to root
     */
//    fun resetToRoot() {
//        _navigationPath.value = emptyList()
//        _currentItems.value = jsonParser.getObjectEntries(_jsonData.value)
//        _isViewingRawJson.value = false
//        _isEditingRawJson.value = false
//
//        // Determine if root is an array
//        _isCurrentArrayParent.value = _rawJsonString.value.trim().startsWith("[")
//    }

    /**
     * Edit a JSON item
     */
    fun editJsonItem(item: JsonNavigationItem, newKey: String, newValue: Any?) {
        viewModelScope.launch {
            try {
                val path = _navigationPath.value
                val oldKey = item.key
                val isRename = oldKey != newKey

                // Get the updated JSON string
                var updatedJson = _rawJsonString.value

                if (isRename && !_isCurrentArrayParent.value) {
                    // Remove the old key first if renaming (only for objects, not arrays)
                    updatedJson = JsonUtils.modifyJsonObject(updatedJson, path, oldKey, null, true)
                    // Then add with new key
                    updatedJson = JsonUtils.modifyJsonObject(updatedJson, path, newKey, newValue, false)
                } else {
                    // Just update the value
                    updatedJson = JsonUtils.modifyJsonObject(updatedJson, path, oldKey, newValue, false)
                }

                // Update the JSON and refresh all views
                parseJsonString(updatedJson)

                // Navigate back to the same path
                var current: Any? = _jsonData.value
                for (segment in path) {
                    current = when (current) {
                        is Map<*, *> -> (current)[segment]
                        is List<*> -> (current)[segment.toInt()]
                        else -> null
                    }
                    if (current != null) {
                        navigateTo(segment, current)
                    }
                }
            } catch (e: Exception) {
                // Handle errors
            }
        }
    }

    /**
     * Add a new JSON item
     */
    fun addJsonItem(key: String, value: Any?) {
        viewModelScope.launch {
            try {
                val path = _navigationPath.value

                // Get the updated JSON string
                val updatedJson = JsonUtils.modifyJsonObject(_rawJsonString.value, path, key, value, false)

                // Update the JSON and refresh all views
                parseJsonString(updatedJson)

                // Navigate back to the same path
                var current: Any? = _jsonData.value
                for (segment in path) {
                    current = when (current) {
                        is Map<*, *> -> (current)[segment]
                        is List<*> -> (current)[segment.toInt()]
                        else -> null
                    }
                    if (current != null) {
                        navigateTo(segment, current)
                    }
                }
            } catch (e: Exception) {
                // Handle errors
            }
        }
    }

    /**
     * Delete a JSON item
     */
    fun deleteJsonItem(item: JsonNavigationItem) {
        viewModelScope.launch {
            try {
                val path = _navigationPath.value
                val key = item.key

                // Get the updated JSON string
                val updatedJson = JsonUtils.modifyJsonObject(_rawJsonString.value, path, key, null, true)

                // Update the JSON and refresh all views
                parseJsonString(updatedJson)

                // Navigate back to the same path
                var current: Any? = _jsonData.value
                for (segment in path) {
                    current = when (current) {
                        is Map<*, *> -> (current )[segment]
                        is List<*> -> (current)[segment.toInt()]
                        else -> null
                    }
                    if (current != null) {
                        navigateTo(segment, current)
                    }
                }
            } catch (e: Exception) {
                // Handle errors
            }
        }
    }
}