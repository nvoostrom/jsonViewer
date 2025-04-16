package com.example.jsonviewer.ui.components.editor

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.material3.MaterialTheme

/**
 * Generic field editor that adapts based on field type
 */
@Composable
fun EditorField(
    type: String,
    initialValue: Any?,
    onValueChange: (Any?) -> Unit,
    modifier: Modifier = Modifier,
    label: String,
    isError: Boolean = false
) {
    when (type) {
        "Boolean" -> {
            // Use the specialized boolean field dropdown
            val boolValue = when {
                initialValue is Boolean -> initialValue
                initialValue is String -> initialValue.equals("true", ignoreCase = true)
                else -> false
            }

            EditorBooleanField(
                value = boolValue,
                onValueChange = onValueChange,
                modifier = modifier,
                label = label
            )
        }

        "null" -> {
            // For null values, we don't need an input field
            // Just show a placeholder or message
            Text(
                text = "Value will be null",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        else -> {
            // For other types, use a standard text field
            val initialTextValue = when {
                initialValue == null -> ""
                initialValue is String -> initialValue
                else -> initialValue.toString()
            }

            var textValue by remember { mutableStateOf(initialTextValue) }

            OutlinedTextField(
                value = textValue,
                onValueChange = {
                    textValue = it

                    // Convert the value based on type
                    val typedValue: Any? = when (type) {
                        "String" -> it
                        "Number" -> it.toDoubleOrNull() ?: 0.0
                        "Object" -> it
                        "Array" -> it
                        else -> it
                    }

                    onValueChange(typedValue)
                },
                label = { Text(label) },
                isError = isError,
                modifier = modifier.fillMaxWidth(),
                textStyle = if (type == "Object" || type == "Array") {
                    MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace
                    )
                } else {
                    MaterialTheme.typography.bodyMedium
                }
            )
        }
    }
}