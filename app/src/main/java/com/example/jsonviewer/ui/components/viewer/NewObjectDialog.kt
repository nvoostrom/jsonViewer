package com.example.jsonviewer.ui.components.viewer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.jsonviewer.R

/**
 * Dialog for adding a new object with predefined fields
 * Often used when in an array context to create a new object that follows the pattern of other objects
 */
@Composable
fun NewObjectDialog(
    isInArray: Boolean = false,
    fieldTemplates: List<String> = emptyList(), // Field names to pre-populate
    onDismiss: () -> Unit,
    onSave: (key: String, value: Map<String, Any?>) -> Unit
) {
    val scrollState = rememberScrollState()

    // State variables
    var objectKey by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }
    val fields = remember { mutableStateListOf<ObjectField>() }

    // Initialize fields based on templates
    if (fields.isEmpty()) {
        if (fieldTemplates.isNotEmpty()) {
            fieldTemplates.forEach { fieldName ->
                fields.add(ObjectField(key = fieldName, value = "", type = "String"))
            }
        } else {
            // Add a default empty field if no templates
            fields.add(ObjectField(key = "", value = "", type = "String"))
        }
    }

    // Add a new field
    fun addField() {
        fields.add(ObjectField(key = "", value = "", type = "String"))
    }

    // Remove a field
    fun removeField(index: Int) {
        if (index >= 0 && index < fields.size) {
            fields.removeAt(index)
        }
    }

    // Validate fields
    fun validate(): Boolean {
        // Validate object key if not in array
        if (!isInArray && objectKey.isBlank()) {
            validationError = "Object key cannot be empty"
            return false
        }

        // Check for empty field keys
        fields.forEachIndexed { index, field ->
            if (field.key.isBlank()) {
                validationError = "Field #${index + 1} has an empty key"
                return false
            }
        }

        // Check for duplicate field keys
        val keys = fields.map { it.key }
        if (keys.distinct().size != keys.size) {
            validationError = "Duplicate field keys are not allowed"
            return false
        }

        validationError = null
        return true
    }

    // Build the result object
    fun buildResult(): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        fields.forEach { field ->
            val value = when (field.type) {
                "String" -> field.value as? String ?: ""
                "Number" -> (field.value as? String)?.toDoubleOrNull() ?: 0.0
                "Boolean" -> (field.value as? String)?.toBoolean() ?: false
                "null" -> null
                else -> field.value
            }

            result[field.key] = value
        }

        return result
    }

    Dialog(onDismissRequest = onDismiss) {
        ElevatedCard(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Title
                Text(
                    text = "Add New Object",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                // Object key input (only if not in array)
                if (!isInArray) {
                    OutlinedTextField(
                        value = objectKey,
                        onValueChange = { objectKey = it },
                        label = { Text("Object Key") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = validationError?.contains("key") == true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Fields
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Object Fields",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    fields.forEachIndexed { index, field ->
                        ObjectFieldInput(
                            field = field,
                            onFieldChange = { fields[index] = it },
                            onRemove = { removeField(index) }
                        )
                    }

                    // Add field button
                    OutlinedButton(
                        onClick = { addField() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Add Field")
                    }
                }

                // Validation error
                validationError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Cancel button
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.cancel))
                    }

                    // Save button
                    Button(
                        onClick = {
                            if (validate()) {
                                onSave(objectKey, buildResult())
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }
    }
}