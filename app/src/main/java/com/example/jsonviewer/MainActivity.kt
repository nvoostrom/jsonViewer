package com.example.jsonviewer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.jsonviewer.ui.JsonViewModel
import com.example.jsonviewer.ui.JsonViewState
import com.example.jsonviewer.ui.components.state.ErrorView
import com.example.jsonviewer.ui.components.state.LoadingView
import com.example.jsonviewer.ui.components.input.JsonInputScreen
import com.example.jsonviewer.ui.components.JsonViewerScreen
import com.example.jsonviewer.ui.theme.JsonViewerTheme
import com.example.jsonviewer.ui.theme.ThemeState

class MainActivity : ComponentActivity() {
    private val viewModel: JsonViewModel by viewModels()
    private lateinit var storageService: LocalStorageService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize storage service
        storageService = (application as JsonViewerApplication).storageService

        // Initialize theme from saved preference
        storageService.loadThemePreference()
        lifecycleScope.launch {
            storageService.isDarkTheme.collect { isDark ->
                ThemeState.initialize(isDark)
            }
        }

        // Load recent and saved files
        storageService.loadRecentFiles()
        storageService.loadSavedJsons()

        setContent {
            val isDarkTheme = ThemeState.isDarkTheme

            // Collect storage data
            val recentFiles by storageService.recentFiles.collectAsState(initial = emptyList())
            val savedJsons by storageService.savedJsons.collectAsState(initial = emptyList())

            JsonViewerTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewState by viewModel.viewState.collectAsState()
                    val items by viewModel.currentItems.collectAsState()
                    val path by viewModel.navigationPath.collectAsState()
                    val rawJson by viewModel.formattedJsonString.collectAsState()
                    val isViewingRawJson by viewModel.isViewingRawJson.collectAsState()
                    val isEditingRawJson by viewModel.isEditingRawJson.collectAsState()
                    val isPrettified by viewModel.isPrettified.collectAsState()
                    val currentJsonText by viewModel.currentJsonText.collectAsState()
                    val isJsonValid by viewModel.isJsonValid.collectAsState()
                    val isCurrentArrayParent by viewModel.isCurrentArrayParent.collectAsState()

                    when (viewState) {
                        is JsonViewState.Initial -> {
                            // Show JSON input screen initially
                            JsonInputScreen(
                                jsonText = currentJsonText,
                                isJsonValid = isJsonValid,
                                onJsonTextChanged = { viewModel.updateJsonText(it) },
                                onFormatJson = { viewModel.formatCurrentJson() },
                                onJsonLoaded = { viewModel.parseJsonString(it) },
                                onToggleTheme = {
                                    ThemeState.setDarkTheme(!isDarkTheme, application)
                                },
                                // Add storage-related functionality
                                onSaveJson = { name, content ->
                                    storageService.saveJson(name, content)
                                },
                                recentFiles = recentFiles,
                                savedJsons = savedJsons,
                                onClearRecentFiles = {
                                    storageService.clearRecentFiles()
                                },
                                onRecentFileSelected = { recentFile ->
                                    viewModel.updateJsonText(recentFile.content)
                                    viewModel.parseJsonString(recentFile.content)
                                },
                                onSavedJsonSelected = { savedJson ->
                                    viewModel.updateJsonText(savedJson.content)
                                    viewModel.parseJsonString(savedJson.content)
                                },
                                onDeleteSavedJson = { id ->
                                    storageService.deleteJson(id)
                                }
                            )
                        }

                        is JsonViewState.Loading -> {
                            LoadingView(message = getString(R.string.parsing_json))
                        }

                        is JsonViewState.Error -> {
                            val errorMessage = (viewState as JsonViewState.Error).message
                            ErrorView(
                                message = errorMessage,
                                onRetry = {
                                    viewModel.resetToInitial()
                                }
                            )
                        }

                        is JsonViewState.Success -> {
                            // Show JSON viewer once JSON is loaded successfully
                            JsonViewerScreen(
                                items = items,
                                path = path,
                                rawJson = rawJson,
                                isViewingRawJson = isViewingRawJson,
                                isPrettified = isPrettified,
                                isEditingRawJson = isEditingRawJson,
                                isCurrentArrayParent = isCurrentArrayParent,
                                onNavigateBack = {
                                    val result = viewModel.navigateBack()
                                    // If we're at the root and trying to go back,
                                    // allow loading a new JSON
                                    if (!result && path.isEmpty() && !isViewingRawJson) {
                                        viewModel.resetToInitial()
                                    }
                                    result
                                },
                                onItemClick = { item ->
                                    viewModel.navigateTo(item.key, item.node)
                                },
                                onToggleRawView = {
                                    viewModel.toggleRawJsonView()
                                },
                                onToggleJsonFormat = {
                                    viewModel.toggleJsonFormat()
                                },
                                onToggleRawEditing = {
                                    viewModel.toggleRawJsonEditing()
                                },
                                onSaveRawJsonChanges = { editedJson ->
                                    viewModel.saveRawJsonChanges(editedJson)
                                },
                                onEditItem = { item, key, value ->
                                    viewModel.editJsonItem(item, key, value)
                                },
                                onAddItem = { key, value ->
                                    viewModel.addJsonItem(key, value)
                                },
                                onDeleteItem = { item ->
                                    viewModel.deleteJsonItem(item)
                                },
                                onLoadNewJson = {
                                    viewModel.resetToInitial()
                                },
                                onToggleTheme = {
                                    ThemeState.setDarkTheme(!isDarkTheme, application)
                                },
                                // Add save functionality for the JsonViewer screen
                                onSaveJson = { name ->
                                    storageService.saveJson(name, rawJson)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}