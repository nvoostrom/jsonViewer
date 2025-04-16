package com.example.jsonviewer.ui.components.viewer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.jsonviewer.R
import com.example.jsonviewer.data.JsonNavigationItem

/**
 * Enhanced list of JSON items with editing capabilities
 */
@Composable
fun EditableJsonItemsList(
    items: List<JsonNavigationItem>,
    isArrayParent: Boolean = false,
    onItemClick: (JsonNavigationItem) -> Unit,
    onEditItem: (JsonNavigationItem, String, Any?) -> Unit,
    onAddItem: (key: String, value: Any?) -> Unit,
    onDeleteItem: (JsonNavigationItem) -> Unit
) {
    val listState = rememberLazyListState()
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Dialog states
    var showAddDialog by remember { mutableStateOf(false) }
    var showAddObjectDialog by remember { mutableStateOf(false) }
    var showAddMenu by remember { mutableStateOf(false) }
    var currentEditingItem by remember { mutableStateOf<JsonNavigationItem?>(null) }

    // Get field templates from the first object in the list if it's an array of objects
    val fieldTemplates = remember(items) {
        if (isArrayParent && items.isNotEmpty() && items.firstOrNull()?.isObject == true) {
            @Suppress("UNCHECKED_CAST")
            (items.first().node as? Map<String, Any?>)?.keys?.toList() ?: emptyList()
        } else {
            emptyList()
        }
    }

    // Show standard add dialog
    if (showAddDialog) {
        JsonItemEditor(
            isAdding = true,
            parentIsArray = isArrayParent,
            currentKey = if (isArrayParent) items.size.toString() else "",
            onDismiss = { showAddDialog = false },
            onSave = { key, value ->
                onAddItem(key, value)
                showAddDialog = false
            }
        )
    }

    // Show structured object add dialog
    if (showAddObjectDialog) {
        NewObjectDialog(
            isInArray = isArrayParent,
            fieldTemplates = fieldTemplates,
            onDismiss = { showAddObjectDialog = false },
            onSave = { key, value ->
                val finalKey = if (isArrayParent) items.size.toString() else key
                onAddItem(finalKey, value)
            }
        )
    }

    // Show editor dialog
    if (currentEditingItem != null) {
        if (currentEditingItem?.isObject == true) {
            // Use object field editor for objects
            ObjectFieldEditor(
                item = currentEditingItem!!,
                onDismiss = { currentEditingItem = null },
                onSave = { updatedObject ->
                    currentEditingItem?.let { item ->
                        onEditItem(item, item.key, updatedObject)
                    }
                    currentEditingItem = null
                },
                onDelete = {
                    currentEditingItem?.let { item ->
                        onDeleteItem(item)
                    }
                    currentEditingItem = null
                }
            )
        } else {
            // Use standard editor for other types
            JsonItemEditor(
                item = currentEditingItem,
                parentIsArray = isArrayParent,
                currentKey = currentEditingItem?.key ?: "",
                onDismiss = { currentEditingItem = null },
                onSave = { key, value ->
                    currentEditingItem?.let { item ->
                        onEditItem(item, key, value)
                    }
                    currentEditingItem = null
                },
                onDelete = {
                    currentEditingItem?.let { item ->
                        onDeleteItem(item)
                    }
                    currentEditingItem = null
                }
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            // Add button with menu options
            Box {
                FloatingActionButton(
                    onClick = {
                        if (isArrayParent && fieldTemplates.isNotEmpty()) {
                            // Show menu for array items to choose between simple or object type
                            showAddMenu = true
                        } else {
                            showAddDialog = true
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_item),
                        tint = MaterialTheme.colorScheme.onSecondary
                    )
                }

                // Dropdown menu for add options
                DropdownMenu(
                    expanded = showAddMenu,
                    onDismissRequest = { showAddMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Add Simple Item") },
                        onClick = {
                            showAddMenu = false
                            showAddDialog = true
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Add Object Item")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "(${fieldTemplates.size} fields)",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        },
                        onClick = {
                            showAddMenu = false
                            showAddObjectDialog = true
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(paddingValues)) {
            LazyColumn(
                state = listState
            ) {
                items(items) { item ->
                    EditableJsonItemCard(
                        item = item,
                        parentIsArray = isArrayParent,
                        onItemClick = onItemClick,
                        onEditItem = {
                            currentEditingItem = item
                        },
                        onDeleteItem = {
                            onDeleteItem(item)
                        },
                        snackbarHostState = snackbarHostState
                    )
                }
            }
        }
    }
}