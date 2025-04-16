package com.example.jsonviewer.ui.components.viewer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    onEditItem: (JsonNavigationItem, key: String, newValue: Any?) -> Unit,
    onAddItem: (key: String, value: Any?) -> Unit,
    onDeleteItem: (JsonNavigationItem) -> Unit
) {
    val listState = rememberLazyListState()

    // Dialog states
    var showAddDialog by remember { mutableStateOf(false) }
    var currentEditingItem by remember { mutableStateOf<JsonNavigationItem?>(null) }

    // Show editor dialog when needed
    if (showAddDialog) {
        JsonItemEditor(
            isAdding = true,
            parentIsArray = isArrayParent,
            onDismiss = { showAddDialog = false },
            onSave = { key, value ->
                onAddItem(key, value)
                showAddDialog = false
            }
        )
    }

    if (currentEditingItem != null) {
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

    Box(modifier = Modifier.fillMaxWidth()) {
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
                    }
                )
            }
        }

        // Add button at the bottom
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.add_item),
                tint = MaterialTheme.colorScheme.onSecondary
            )
        }
    }
}