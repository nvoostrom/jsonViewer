package com.example.jsonviewer.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.jsonviewer.R
import com.example.jsonviewer.data.JsonNavigationItem
import com.example.jsonviewer.ui.components.common.ThemeSwitcher
import com.example.jsonviewer.ui.components.dialogs.SaveJsonDialog
import com.example.jsonviewer.ui.components.navigation.NavigationBreadcrumb
import com.example.jsonviewer.ui.components.raw.EditableRawJsonView
import com.example.jsonviewer.ui.components.state.EmptyStateView
import com.example.jsonviewer.ui.components.viewer.EditableJsonItemsList

/**
 * Enhanced main screen component for the JSON viewer with editing capabilities
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JsonViewerScreen(
    items: List<JsonNavigationItem>,
    path: List<String>,
    rawJson: String,
    isViewingRawJson: Boolean,
    isPrettified: Boolean = true,
    isEditingRawJson: Boolean = false,
    isCurrentArrayParent: Boolean = false,
    onNavigateBack: () -> Boolean,
    onItemClick: (JsonNavigationItem) -> Unit,
    onToggleRawView: () -> Unit,
    onToggleJsonFormat: () -> Unit = {},
    onToggleRawEditing: () -> Unit = {},
    onSaveRawJsonChanges: (String) -> Unit = {},
    onEditItem: (JsonNavigationItem, String, Any?) -> Unit = { _, _, _ -> },
    onAddItem: (String, Any?) -> Unit = { _, _ -> },
    onDeleteItem: (JsonNavigationItem) -> Unit = {},
    onLoadNewJson: () -> Unit,
    onToggleTheme: () -> Unit,
    onSaveJson: (String) -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Dialog state
    var showSaveDialog by remember { mutableStateOf(false) }

    // Show save dialog when needed
    if (showSaveDialog) {
        SaveJsonDialog(
            initialName = "",
            onDismiss = { showSaveDialog = false },
            onSave = { name ->
                onSaveJson(name)
                showSaveDialog = false
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isViewingRawJson) stringResource(R.string.raw_json)
                        else if (path.isEmpty()) stringResource(R.string.app_name)
                        else path.last(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    // Save button
                    IconButton(onClick = { showSaveDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = stringResource(R.string.save_json)
                        )
                    }

                    // Raw JSON actions
                    if (isViewingRawJson) {
                        // Edit/Save toggle for raw JSON
                        IconButton(onClick = {
                            if (isEditingRawJson) {
                                // Do nothing on click, save button is separate
                            } else {
                                onToggleRawEditing()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.edit_json),
                                tint = if (isEditingRawJson)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        // Format toggle button (only when not editing)
                        if (!isEditingRawJson) {
                            IconButton(onClick = { onToggleJsonFormat() }) {
                                Icon(
                                    imageVector = Icons.Outlined.Share,
                                    contentDescription = if (isPrettified)
                                        stringResource(R.string.minify)
                                    else
                                        stringResource(R.string.prettify)
                                )
                            }
                        }
                    } else {
                        // Toggle to raw view
                        IconButton(onClick = { onToggleRawView() }) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = stringResource(R.string.raw_json)
                            )
                        }
                    }

                    // Theme toggle button
                    ThemeSwitcher(
                        isDarkTheme = MaterialTheme.colorScheme.surface.luminance() < 0.5f,
                        onToggleTheme = onToggleTheme
                    )

                    // Home button to return to JSON input
                    IconButton(onClick = { onLoadNewJson() }) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = stringResource(R.string.load_new)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ){ paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isViewingRawJson) {
                // Editable raw JSON view
                EditableRawJsonView(
                    json = rawJson,
                    isPrettified = isPrettified,
                    isEditing = isEditingRawJson,
                    onToggleFormat = onToggleJsonFormat,
                    onToggleEdit = onToggleRawEditing,
                    onSaveChanges = onSaveRawJsonChanges
                )
            } else {
                // Structured JSON view with editing capabilities
                if (items.isEmpty()) {
                    EmptyStateView(
                        title = stringResource(R.string.no_items),
                        message = stringResource(R.string.no_items_message),
                        actionText = stringResource(R.string.view_raw_json),
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

                        // Editable JSON items list
                        EditableJsonItemsList(
                            items = items,
                            isArrayParent = isCurrentArrayParent,
                            onItemClick = onItemClick,
                            onEditItem = onEditItem,
                            onAddItem = onAddItem,
                            onDeleteItem = onDeleteItem
                        )
                    }
                }
            }
        }
    }
}