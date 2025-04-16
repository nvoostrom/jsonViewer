package com.example.jsonviewer.ui.components.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.jsonviewer.data.JsonNavigationItem

/**
 * Card component for a JSON item with improved visual design
 */
@Composable
fun JsonItemCard(
    item: JsonNavigationItem,
    onItemClick: (JsonNavigationItem) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val isClickable = item.isObject || item.isArray

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

                if (isClickable) {
                    // Navigation arrow for objects and arrays
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Navigate",
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
            Divider(color = MaterialTheme.colorScheme.surfaceVariant)
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

@Composable
fun KeyTypeIndicator(item: JsonNavigationItem) {
    val (backgroundColor, icon) = when {
        item.isObject -> MaterialTheme.colorScheme.primary to Icons.Outlined.Info
        item.isArray -> MaterialTheme.colorScheme.tertiary to Icons.Default.ViewList
        item.node is String -> MaterialTheme.colorScheme.secondary to Icons.Outlined.Info
        item.node is Number -> MaterialTheme.colorScheme.error to Icons.Outlined.Info
        item.node is Boolean -> MaterialTheme.colorScheme.surfaceVariant to Icons.Outlined.Info
        else -> MaterialTheme.colorScheme.outline to Icons.Outlined.Info
    }

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(backgroundColor.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = backgroundColor,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun TypeChip(type: String) {
    val chipColor = when (type) {
        "Object" -> MaterialTheme.colorScheme.primary
        "Array" -> MaterialTheme.colorScheme.tertiary
        "String" -> MaterialTheme.colorScheme.secondary
        "Number" -> MaterialTheme.colorScheme.error
        "Boolean" -> MaterialTheme.colorScheme.surfaceVariant
        "null" -> MaterialTheme.colorScheme.outline
        else -> MaterialTheme.colorScheme.outline
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(chipColor.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = type,
            style = MaterialTheme.typography.labelSmall,
            color = chipColor
        )
    }
}