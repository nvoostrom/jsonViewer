package com.example.jsonviewer.ui.components.raw

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.jsonviewer.R
import com.example.jsonviewer.utils.JsonUtils

/**
 * Editable raw JSON view component with editing capabilities
 */
@Composable
fun EditableRawJsonView(
    json: String,
    isPrettified: Boolean = true,
    isEditing: Boolean = false,
    onToggleFormat: () -> Unit = {},
    onToggleEdit: () -> Unit = {},
    onSaveChanges: (String) -> Unit = {}
) {
    var editableText by remember(json, isEditing) { mutableStateOf(json) }
    var isJsonValid by remember(editableText) {
        mutableStateOf(JsonUtils.isValidJson(editableText))
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Action controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isEditing) {
                // Format control (only visible when not editing)
                Text(
                    text = if (isPrettified) stringResource(R.string.prettified) else stringResource(R.string.minified),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(8.dp))

                ElevatedButton(
                    onClick = onToggleFormat,
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(
                        text = if (isPrettified) stringResource(R.string.minify) else stringResource(R.string.prettify),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Edit/Save controls
            if (isEditing) {
                Button(
                    onClick = {
                        if (isJsonValid) {
                            // Format the JSON before saving for consistency
                            val formattedJson = if (isPrettified) {
                                JsonUtils.prettifyJson(editableText)
                            } else {
                                JsonUtils.minifyJson(editableText)
                            }
                            onSaveChanges(formattedJson)
                        }
                    },
                    enabled = isJsonValid
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(stringResource(R.string.save_changes))
                }
            } else {
                ElevatedButton(
                    onClick = onToggleEdit
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(stringResource(R.string.edit_json))
                }
            }
        }

        // Error message when JSON is invalid during editing
        if (isEditing && !isJsonValid) {
            Text(
                text = stringResource(R.string.invalid_json),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        // JSON content
        if (isEditing) {
            // Editable text field
            OutlinedTextField(
                value = editableText,
                onValueChange = {
                    editableText = it
                    isJsonValid = JsonUtils.isValidJson(it)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace
                ),
                isError = !isJsonValid,
                label = { Text(stringResource(R.string.json_content)) }
            )
        } else {
            // Read-only syntax-highlighted view
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(16.dp)
            ) {
                SyntaxHighlightedText(
                    json = json
                )
            }
        }
    }
}