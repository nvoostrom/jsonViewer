package com.example.jsonviewer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.jsonviewer.data.JsonNavigationItem

/**
 * Main screen component for the JSON viewer
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JsonViewerScreen(
    items: List<JsonNavigationItem>,
    path: List<String>,
    onNavigateBack: () -> Boolean,
    onItemClick: (JsonNavigationItem) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (path.isEmpty()) "JSON Viewer" else path.last(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    if (path.isNotEmpty()) {
                        IconButton(onClick = { onNavigateBack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Navigate Back"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No items to display",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn {
                    items(items) { item ->
                        JsonItemCard(item, onItemClick)
                    }
                }
            }
        }
    }
}

/**
 * Card component for a JSON item
 */
@Composable
fun JsonItemCard(
    item: JsonNavigationItem,
    onItemClick: (JsonNavigationItem) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable {
                if (item.isObject || item.isArray) {
                    onItemClick(item)
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.key,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                if (item.isObject || item.isArray) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Navigate"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            // Show content preview based on the type
            when {
                item.isObject -> {
                    // For objects, show each property on its own line with its value
                    if (item.objectKeys.isNotEmpty()) {
                        @Suppress("UNCHECKED_CAST")
                        val objectMap = item.node as? Map<String, Any?>

                        if (objectMap != null) {
                            // Create a column of key-value pairs
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                objectMap.entries.take(5).forEach { (key, value) ->
                                    val valueStr = when (value) {
                                        is Map<*, *> -> "..."
                                        is List<*> -> "..."
                                        null -> "Empty"
                                        else -> value.toString()
                                    }

                                    Text(
                                        text = "$key: $valueStr",
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                if (objectMap.size > 5) {
                                    Text(
                                        text = "...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        } else {
                            // Fallback if for some reason we can't get the values
                            val keyPreview = item.objectKeys.take(5).joinToString(", ")
                            Text(
                                text = "Contains: $keyPreview${if (item.objectKeys.size > 5) "..." else ""}",
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else {
                        Text(
                            text = "Empty",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                item.isArray -> {
                    // For arrays, show a user-friendly description of contained items
                    val contentPreview = if (item.arraySize > 0) {
                        val firstItem = (item.node as? List<*>)?.firstOrNull()

                        if (firstItem is Map<*, *>) {
                            val propertyPreview = (firstItem as Map<String, Any?>).keys.take(5).joinToString(", ")
                            "Contains ${item.arraySize} items with: $propertyPreview${if ((firstItem as Map<String, Any?>).size > 5) "..." else ""}"
                        } else {
                            "Contains ${item.arraySize} items"
                        }
                    } else {
                        "Empty list"
                    }

                    Text(
                        text = contentPreview,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                item.node == null -> {
                    Text(
                        text = "null",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                else -> {
                    // For primitive values
                    Text(
                        text = item.node.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Show type indicator with more user-friendly terms
            Text(
                text = when {
                    item.isObject -> "Group"
                    item.isArray -> "List"
                    item.node == null -> "Empty"
                    item.node is String -> "Text"
                    item.node is Number -> "Number"
                    item.node is Boolean -> "Yes/No"
                    else -> item.node::class.java.simpleName
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

/**
 * Navigation breadcrumb component
 */
@Composable
fun NavigationBreadcrumb(
    path: List<String>,
    onNavigateTo: (Int) -> Unit
) {
    if (path.isEmpty()) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Root",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { onNavigateTo(-1) }
        )

        path.forEachIndexed { index, segment ->
            Text(
                text = " > ",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = segment,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable { onNavigateTo(index) }
            )
        }
    }
}