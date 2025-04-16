package com.example.jsonviewer.ui.components.viewer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.jsonviewer.R
import com.example.jsonviewer.data.JsonNavigationItem
import kotlinx.coroutines.launch

/**
 * Enhanced JSON item card with editing capabilities
 */
@Composable
fun EditableJsonItemCard(
    item: JsonNavigationItem,
    parentIsArray: Boolean = false,
    onItemClick: (JsonNavigationItem) -> Unit,
    onEditItem: (JsonNavigationItem) -> Unit,
    onDeleteItem: (JsonNavigationItem) -> Unit,
    snackbarHostState: SnackbarHostState? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val isClickable = item.isObject || item.isArray
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    // Dialog states
    var showEditDialog by remember { mutableStateOf(false) }
    var showObjectEditor by remember { mutableStateOf(false) }

    // Show standard editor dialog when needed
    if (showEditDialog && !item.isObject) {
        JsonItemEditor(
            item = item,
            parentIsArray = parentIsArray,
            currentKey = item.key,
            onDismiss = { showEditDialog = false },
            onSave = { _, _ ->
                // Just close the dialog here - actual saving happens in the ViewModel
                showEditDialog = false
                onEditItem(item)
            },
            onDelete = {
                showEditDialog = false
                // Call delete on the parent component
                onDeleteItem(item)
            }
        )
    }

    // Show object field editor for objects
    if (showObjectEditor && item.isObject) {
        ObjectFieldEditor(
            item = item,
            onDismiss = { showObjectEditor = false },
            onSave = { updatedObject ->
                // Create a new item with the updated object
                val updatedItem = item.copy(node = updatedObject)
                onEditItem(updatedItem)
                showObjectEditor = false
            },
            onDelete = {
                showObjectEditor = false
                onDeleteItem(item)
            }
        )
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable {
                if (isClickable) {
                    onItemClick(item)
                } else {
                    // Toggle expansion for primitive values
                    expanded = !expanded
                }
            },
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Key icon indicator based on type
                KeyTypeIndicator(item)

                Spacer(modifier = Modifier.width(12.dp))

                // Key name
                Text(
                    text = item.key,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                // Copy button for primitive values
                if (!item.isObject && !item.isArray) {
                    IconButton(
                        onClick = {
                            val textToCopy = when (val value = item.node) {
                                null -> "null"
                                is String -> value
                                else -> value.toString()
                            }
                            clipboardManager.setText(AnnotatedString(textToCopy))

                            // Show snackbar if provided
                            snackbarHostState?.let {
                                scope.launch {
                                    it.showSnackbar("Value copied to clipboard")
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = stringResource(R.string.copy_to_clipboard),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Delete button
                IconButton(
                    onClick = { onDeleteItem(item) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }

                // Edit button
                IconButton(
                    onClick = {
                        if (item.isObject) {
                            showObjectEditor = true
                        } else {
                            showEditDialog = true
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.edit),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }

                if (isClickable) {
                    // Navigation arrow for objects and arrays
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = stringResource(R.string.navigate),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Type indicator chip
            TypeChip(
                type = when {
                    item.isObject -> "Object"
                    item.isArray -> "Array"
                    item.node == null -> "null"
                    item.node is String -> "String"
                    item.node is Number -> "Number"
                    item.node is Boolean -> "Boolean"
                    else -> "Unknown"
                }
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))

            // Content preview
            when {
                item.isObject -> {
                    ObjectPreview(item, clipboardManager, snackbarHostState)
                }
                item.isArray -> {
                    ArrayPreview(item)
                }
                else -> {
                    // For primitive values
                    PrimitiveValuePreview(item, expanded)
                }
            }
        }
    }
}