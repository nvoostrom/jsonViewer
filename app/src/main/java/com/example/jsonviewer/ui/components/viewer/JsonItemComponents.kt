package com.example.jsonviewer.ui.components.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.jsonviewer.data.JsonNavigationItem

/**
 * Visual indicator for JSON value type
 */
@Composable
fun KeyTypeIndicator(item: JsonNavigationItem) {
    val (backgroundColor, icon) = when {
        item.isObject -> MaterialTheme.colorScheme.primary to Icons.Outlined.Info
        item.isArray -> MaterialTheme.colorScheme.tertiary to Icons.AutoMirrored.Filled.ViewList
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

/**
 * Type chip component for displaying value type
 */
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