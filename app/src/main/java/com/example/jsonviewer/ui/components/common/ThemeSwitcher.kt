package com.example.jsonviewer.ui.components.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.jsonviewer.R

/**
 * Theme toggle button component
 */
/**
 * Theme toggle button component with improved styling
 */
@Composable
fun ThemeSwitcher(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onToggleTheme,
        modifier = modifier
    ) {
        // Animation for smooth transition between icons
        val transition = updateTransition(
            targetState = isDarkTheme,
            label = "ThemeIconTransition"
        )

        val animatedTint by transition.animateColor(
            label = "ThemeIconColor",
            transitionSpec = { tween(durationMillis = 300) }
        ) { dark ->
            if (dark) MaterialTheme.colorScheme.inverseOnSurface
            else MaterialTheme.colorScheme.onPrimaryContainer
        }

        Icon(
            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
            contentDescription = stringResource(if (isDarkTheme) R.string.light_mode else R.string.dark_mode),
            tint = animatedTint
        )
    }
}