package com.example.jsonviewer.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.jsonviewer.data.SampleData
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Component for inputting JSON either by typing or uploading a file
 */
@Composable
fun JsonInputScreen(
    onJsonLoaded: (String) -> Unit
) {
    var jsonText by remember { mutableStateOf("") }
    val context = LocalContext.current

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

                // Parse the JSON once loaded
                if (jsonText.isNotBlank()) {
                    onJsonLoaded(jsonText)
                }
            } catch (e: Exception) {
                jsonText = "Error loading file: ${e.message}"
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "JSON Viewer",
                style = MaterialTheme.typography.titleLarge
            )

            OutlinedTextField(
                value = jsonText,
                onValueChange = { jsonText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Enter JSON") },
                maxLines = 10,
                minLines = 5
            )

            Button(
                onClick = {
                    if (jsonText.isNotBlank()) {
                        onJsonLoaded(jsonText)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Parse JSON")
            }

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { filePickerLauncher.launch("application/json") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Upload JSON File")
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = {
                        jsonText = SampleData.sampleJson
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Load Sample")
                }
            }
        }
    }
}