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

class MainActivity : ComponentActivity() {
    private val viewModel: JsonViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            JsonViewerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewState by viewModel.viewState.collectAsState()
                    val items by viewModel.currentItems.collectAsState()
                    val path by viewModel.navigationPath.collectAsState()
                    val rawJson by viewModel.formattedJsonString.collectAsState()
                    val isViewingRawJson by viewModel.isViewingRawJson.collectAsState()
                    val isPrettified by viewModel.isPrettified.collectAsState()

                    when (viewState) {
                        is JsonViewState.Initial -> {
                            // Show JSON input screen initially
                            JsonInputScreen(onJsonLoaded = { jsonString ->
                                viewModel.parseJsonString(jsonString)
                            })
                        }

                        is JsonViewState.Loading -> {
                            LoadingView(message = "Parsing JSON...")
                        }

                        is JsonViewState.Error -> {
                            val errorMessage = (viewState as JsonViewState.Error).message
                            ErrorView(
                                message = errorMessage,
                                onRetry = {
                                    // Reset to initial state to allow user to try again
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
                                onLoadNewJson = {
                                    viewModel.resetToInitial()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}