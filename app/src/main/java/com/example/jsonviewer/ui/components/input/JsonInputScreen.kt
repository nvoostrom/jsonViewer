package com.example.jsonviewer.ui.components.input

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.jsonviewer.data.SampleData
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Enhanced component for inputting JSON with modern UI
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JsonInputScreen(
    onJsonLoaded: (String) -> Unit
) {
    var jsonText by remember { mutableStateOf("") }
    var showSampleOptions by remember { mutableStateOf(false) }
    var isJsonValid by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val stringBuilder = StringBuilder()
                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    stringBuilder.append(line).append('\n')
                }

                jsonText = stringBuilder.toString()
                reader.close()

                // Don't automatically parse on load, let user review it first
                isJsonValid = true
            } catch (e: Exception) {
                jsonText = "Error loading file: ${e.message}"
                isJsonValid = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("JSON Viewer")
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
                // Header
                WelcomeHeader()

                Spacer(modifier = Modifier.height(24.dp))

                // JSON Input Card
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Title with JSON icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "Enter JSON Data",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            // Clear button if there's text
                            if (jsonText.isNotEmpty()) {
                                IconButton(
                                    onClick = { jsonText = "" }
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = "Clear",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }

                        // Text input area with monospaced font
                        OutlinedTextField(
                            value = jsonText,
                            onValueChange = {
                                jsonText = it
                                isJsonValid = true // Reset validation on change
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace
                            ),
                            isError = !isJsonValid,
                            label = { Text("Paste or type JSON here") },
                            placeholder = { Text("{ \"example\": \"value\" }") },
                            supportingText = {
                                if (!isJsonValid) {
                                    Text(
                                        text = "Invalid JSON format",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            trailingIcon = {
                                IconButton(onClick = {
                                    clipboardManager.getText()?.let {
                                        jsonText = it.text
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.ContentPaste,
                                        contentDescription = "Paste from clipboard"
                                    )
                                }
                            }
                        )

                        // Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Upload button
                            FilledTonalButton(
                                onClick = { filePickerLauncher.launch("application/json") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FileOpen,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Upload File")
                            }

                            // Sample data button
                            FilledTonalButton(
                                onClick = { showSampleOptions = !showSampleOptions },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Load Sample")
                            }
                        }

                        // Sample data options
                        AnimatedVisibility(
                            visible = showSampleOptions,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "Sample JSON Data",
                                        style = MaterialTheme.typography.titleSmall
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Sample options
                                    SampleOption("Conflicts Data") {
                                        jsonText = SampleData.sampleJson
                                        showSampleOptions = false
                                    }

                                    SampleOption("User Profile") {
                                        jsonText = SampleData.userProfileJson
                                        showSampleOptions = false
                                    }

                                    SampleOption("Weather Forecast") {
                                        jsonText = SampleData.weatherJson
                                        showSampleOptions = false
                                    }

                                    SampleOption("Product Catalog") {
                                        jsonText = SampleData.productJson
                                        showSampleOptions = false
                                    }

                                    // Close option
                                    TextButton(
                                        onClick = { showSampleOptions = false },
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Close"
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Close")
                                    }
                                }
                            }
                        }

                        // Parse button
                        Button(
                            onClick = {
                                try {
                                    // Simple validation attempt
                                    if (jsonText.trim().isEmpty()) {
                                        isJsonValid = false
                                    } else if (
                                        (jsonText.trim().startsWith("{") && jsonText.trim().endsWith("}")) ||
                                        (jsonText.trim().startsWith("[") && jsonText.trim().endsWith("]"))
                                    ) {
                                        onJsonLoaded(jsonText)
                                    } else {
                                        isJsonValid = false
                                    }
                                } catch (e: Exception) {
                                    isJsonValid = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = jsonText.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Parse JSON")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        Text(
            text = "JSON Viewer",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Visualize and navigate through JSON data with ease",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SampleOption(name: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.Code,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = name,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start
        )
    }
}