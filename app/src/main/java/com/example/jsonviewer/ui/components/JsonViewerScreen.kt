package com.example.jsonviewer.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.example.jsonviewer.data.JsonNavigationItem
import com.example.jsonviewer.ui.components.navigation.NavigationBreadcrumb
import com.example.jsonviewer.ui.components.raw.RawJsonView
import com.example.jsonviewer.ui.components.state.EmptyStateView
import com.example.jsonviewer.ui.components.viewer.JsonItemsList

/**
 * Main screen component for the JSON viewer
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JsonViewerScreen(
    items: List<JsonNavigationItem>,
    path: List<String>,
    rawJson: String,
    isViewingRawJson: Boolean,
    isPrettified: Boolean = true,
    onNavigateBack: () -> Boolean,
    onItemClick: (JsonNavigationItem) -> Unit,
    onToggleRawView: () -> Unit,
    onToggleJsonFormat: () -> Unit = {},
    onLoadNewJson: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isViewingRawJson) "Raw JSON"
                        else if (path.isEmpty()) "JSON Viewer"
                        else path.last(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Navigate Back"
                        )
                    }
                },
                actions = {
                    // Action to toggle between raw view and structured view
                    IconButton(onClick = { onToggleRawView() }) {
                        Icon(
                            imageVector = if (isViewingRawJson) Icons.Default.ViewList else Icons.Default.Code,
                            contentDescription = if (isViewingRawJson) "Structured View" else "Raw JSON View"
                        )
                    }

                    // Format toggle button (only show in raw view)
                    if (isViewingRawJson) {
                        IconButton(onClick = { onToggleJsonFormat() }) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = if (isPrettified) "Minify JSON" else "Prettify JSON"
                            )
                        }
                    }

                    // Home button to return to JSON input
                    IconButton(onClick = { onLoadNewJson() }) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Load New JSON"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            if (!isViewingRawJson && items.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { onToggleRawView() },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = "View Raw JSON",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isViewingRawJson) {
                // Raw JSON view
                RawJsonView(
                    json = rawJson,
                    isPrettified = isPrettified,
                    onToggleFormat = onToggleJsonFormat
                )
            } else {
                // Structured JSON view
                if (items.isEmpty()) {
                    EmptyStateView(
                        title = "No Items to Display",
                        message = "This JSON element contains no items to display or represents a primitive value.",
                        actionText = "View Raw JSON",
                        onAction = { onToggleRawView() }
                    )
                } else {
                    Column {
                        if (path.isNotEmpty()) {
                            NavigationBreadcrumb(
                                path = path,
                                onNavigateTo = { index ->
                                    if (index == -1) {
                                        // Navigate to root
                                        while (onNavigateBack()) {
                                            // Keep going back until we reach the root
                                        }
                                    }
                                }
                            )
                        }

                        JsonItemsList(
                            items = items,
                            onItemClick = onItemClick
                        )
                    }
                }
            }
        }
    }
}