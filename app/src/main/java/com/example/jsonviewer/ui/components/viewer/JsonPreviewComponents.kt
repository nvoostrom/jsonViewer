package com.example.jsonviewer.ui.components.viewer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.jsonviewer.data.JsonNavigationItem
import kotlinx.coroutines.launch

/**
 * Preview component for Object type JSON values
 */
@Composable
fun ObjectPreview(
    item: JsonNavigationItem,
    clipboardManager: ClipboardManager? = null,
    snackbarHostState: SnackbarHostState? = null
) {
    val scope = rememberCoroutineScope()

    if (item.objectKeys.isNotEmpty()) {
        @Suppress("UNCHECKED_CAST")
        val objectMap = item.node as? Map<String, Any?>

        if (objectMap != null) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                objectMap.entries.take(3).forEach { (key, value) ->
                    val valueStr = when (value) {
                        is Map<*, *> -> "{...}"
                        is List<*> -> "[...]"
                        null -> "null"
                        else -> value.toString().take(50) + if (value.toString().length > 50) "..." else ""
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable(enabled = clipboardManager != null) {
                                clipboardManager?.let {
                                    val textToCopy = when(value) {
                                        is String -> "\"$value\""
                                        null -> "null"
                                        else -> value.toString()
                                    }
                                    it.setText(AnnotatedString(textToCopy))
                                    snackbarHostState?.let { host ->
                                        scope.launch {
                                            host.showSnackbar("Value for '$key' copied to clipboard")
                                        }
                                    }
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                                    append("\"$key\": ")
                                }

                                withStyle(SpanStyle(
                                    color = when(value) {
                                        is Map<*, *> -> MaterialTheme.colorScheme.primary
                                        is List<*> -> MaterialTheme.colorScheme.tertiary
                                        null -> MaterialTheme.colorScheme.outline
                                        is String -> MaterialTheme.colorScheme.secondary
                                        is Number -> MaterialTheme.colorScheme.error
                                        is Boolean -> MaterialTheme.colorScheme.onSurface
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )) {
                                    if (value is String) append("\"$valueStr\"") else append(valueStr)
                                }
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        if (clipboardManager != null && value !is Map<*, *> && value !is List<*>) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy value",
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                if (objectMap.size > 3) {
                    Text(
                        text = "... ${objectMap.size - 3} more field(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            // Fallback
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
            text = "{ }  Empty Object",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

/**
 * Preview component for Array type JSON values
 */
@Composable
fun ArrayPreview(item: JsonNavigationItem) {
    val contentPreview = if (item.arraySize > 0) {
        val firstItem = (item.node as? List<*>)?.firstOrNull()

        if (firstItem is Map<*, *>) {
            val propertyPreview = (firstItem).keys.take(3).joinToString(", ")
            "Contains ${item.arraySize} object(s) with fields: $propertyPreview${if ((firstItem).size > 3) "..." else ""}"
        } else {
            "Contains ${item.arraySize} item(s)"
        }
    } else {
        "[ ]  Empty Array"
    }

    Text(
        text = contentPreview,
        style = MaterialTheme.typography.bodyMedium,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
        color = if (item.arraySize == 0) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
    )
}

/**
 * Preview component for primitive JSON values (strings, numbers, booleans, null)
 */
@Composable
fun PrimitiveValuePreview(item: JsonNavigationItem, expanded: Boolean) {
    val maxLength = 100
    val text = when (item.node) {
        null -> "null"
        is String -> "\"${item.node}\""
        else -> item.node.toString()
    }

    val needsExpansion = text.length > maxLength

    Column {
        Text(
            text = if (!expanded && needsExpansion) "${text.take(maxLength)}..." else text,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace
            ),
            color = when (item.node) {
                null -> MaterialTheme.colorScheme.outline
                is String -> MaterialTheme.colorScheme.secondary
                is Number -> MaterialTheme.colorScheme.error
                is Boolean -> MaterialTheme.colorScheme.onSurface
                else -> MaterialTheme.colorScheme.onSurface
            },
            maxLines = if (expanded) Int.MAX_VALUE else 3,
            overflow = if (expanded) TextOverflow.Clip else TextOverflow.Ellipsis
        )

        if (needsExpansion) {
            Text(
                text = if (expanded) "Show less" else "Show more",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}