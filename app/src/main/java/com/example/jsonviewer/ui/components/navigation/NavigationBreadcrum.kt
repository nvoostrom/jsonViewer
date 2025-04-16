package com.example.jsonviewer.ui.components.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Navigation breadcrumb component with improved design
 */
@Composable
fun NavigationBreadcrumb(
    path: List<String>,
    onNavigateTo: (Int) -> Unit
) {
    if (path.isEmpty()) return

    Surface(
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
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

                // Current level is emphasized
                val isCurrentLevel = index == path.lastIndex
                Text(
                    text = segment,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isCurrentLevel) FontWeight.Bold else FontWeight.Normal,
                    color = if (isCurrentLevel)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.clickable { onNavigateTo(index) }
                )
            }
        }
    }
}