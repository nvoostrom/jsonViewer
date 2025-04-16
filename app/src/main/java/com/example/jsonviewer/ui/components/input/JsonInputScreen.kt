package com.example.jsonviewer.ui.components.input

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.luminance
import com.example.jsonviewer.R
import com.example.jsonviewer.ui.components.common.ThemeSwitcher

/**
 * Main screen for JSON input with enhanced UI and functionality
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JsonInputScreen(
    jsonText: String,
    isJsonValid: Boolean,
    onJsonTextChanged: (String) -> Unit,
    onFormatJson: () -> Unit,
    onJsonLoaded: (String) -> Unit,
    onToggleTheme: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.app_name))
                },
                actions = {
                    ThemeSwitcher(
                        isDarkTheme = MaterialTheme.colorScheme.surface.luminance() < 0.5f,
                        onToggleTheme = onToggleTheme
                    )
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
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Welcome header
                WelcomeHeader()

                Spacer(modifier = Modifier.height(16.dp))

                // JSON Editor
                JsonEditor(
                    jsonText = jsonText,
                    onJsonChanged = onJsonTextChanged,
                    isJsonValid = isJsonValid,
                    onFormatJson = onFormatJson
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action Area
                JsonInputActions(
                    jsonText = jsonText,
                    isJsonValid = isJsonValid,
                    onJsonLoaded = onJsonLoaded,
                    onError = { error ->
                        // Handle file loading errors by updating the JSON text
                        onJsonTextChanged(error)
                    }
                )
            }
        }
    }
}