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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.jsonviewer.R
import com.example.jsonviewer.data.JsonNavigationItem

/**
 * Enhanced JSON item card with editing capabilities
 */
@Composable
fun EditableJsonItemCard(
    item: JsonNavigationItem,
    parentIsArray: Boolean = false,
    onItemClick: (JsonNavigationItem) -> Unit,
    onEditItem: (JsonNavigationItem) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val isClickable = item.isObject || item.isArray

    // Dialog states
    var showEditDialog by remember { mutableStateOf(false) }

    // Show editor dialog when needed
    if (showEditDialog) {
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
                // Deletion is handled in the parent component
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

                // Edit button
                IconButton(
                    onClick = { showEditDialog = true }
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
                    ObjectPreview(item)
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