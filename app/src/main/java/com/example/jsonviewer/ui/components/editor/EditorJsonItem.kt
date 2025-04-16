package com.example.jsonviewer.ui.components.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.jsonviewer.R
import com.example.jsonviewer.data.JsonNavigationItem
import com.example.jsonviewer.utils.JsonUtils
import org.json.JSONArray
import org.json.JSONObject

/**
 * Refactored dialog for editing JSON items with separate concerns
 */
@Composable
fun EditorJsonItem(
    item: JsonNavigationItem? = null,
    isAdding: Boolean = false,
    parentIsArray: Boolean = false,
    currentKey: String = "",
    onDismiss: () -> Unit,
    onSave: (key: String, value: Any?) -> Unit,
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // State for the key and value
    var key by remember { mutableStateOf(if (!parentIsArray) item?.key ?: "" else currentKey) }
    var currentValue by remember {
        mutableStateOf(
            when {
                item == null -> ""
                item.node == null -> null
                else -> item.node
            }
        )
    }

    // Type state
    var selectedType by remember {
        mutableStateOf(
            when {
                item == null -> "String"
                item.isObject -> "Object"
                item.isArray -> "Array"
                item.node == null -> "null"
                item.node is String -> "String"
                item.node is Number -> "Number"
                item.node is Boolean -> "Boolean"
                else -> "String"
            }
        )
    }

    // Validation state
    var isKeyValid by remember { mutableStateOf(true) }
    var isValueValid by remember { mutableStateOf(true) }
    var validationMessage by remember { mutableStateOf("") }

    // Validate key and value
    fun validate(): Boolean {
        // Key validation
        if (!parentIsArray && key.isBlank()) {
            isKeyValid = false
            validationMessage = "Key cannot be empty"
            return false
        }

        // Value validation
        when (selectedType) {
            "Number" -> {
                if (currentValue is String) {
                    try {
                        (currentValue as String).toDouble()
                        isValueValid = true
                    } catch (e: NumberFormatException) {
                        isValueValid = false
                        validationMessage = "Invalid number format"
                        return false
                    }
                }
            }
            "Object" -> {
                if (currentValue is String && (currentValue as String).isNotBlank()) {
                    isValueValid = JsonUtils.isValidJson(currentValue as String) &&
                            (currentValue as String).trim().startsWith("{")
                    if (!isValueValid) {
                        validationMessage = "Invalid JSON object"
                        return false
                    }
                }
            }
            "Array" -> {
                if (currentValue is String && (currentValue as String).isNotBlank()) {
                    isValueValid = JsonUtils.isValidJson(currentValue as String) &&
                            (currentValue as String).trim().startsWith("[")
                    if (!isValueValid) {
                        validationMessage = "Invalid JSON array"
                        return false
                    }
                }
            }
        }

        return isKeyValid && isValueValid
    }

    // Parse value for saving
    fun parseValueForSave(): Any? {
        return when (selectedType) {
            "String" -> currentValue
            "Number" -> if (currentValue is String) currentValue.toString().toDoubleOrNull() ?: 0.0 else currentValue
            "Boolean" -> if (currentValue is Boolean) currentValue else false
            "Object" -> {
                if (currentValue is String && (currentValue as String).isNotBlank()) {
                    try {
                        JsonUtils.parseJsonObject(currentValue as String)
                    } catch (e: Exception) {
                        JsonUtils.jsonObjectToMap(JSONObject())
                    }
                } else {
                    JsonUtils.jsonObjectToMap(JSONObject())
                }
            }
            "Array" -> {
                if (currentValue is String && (currentValue as String).isNotBlank()) {
                    try {
                        JsonUtils.parseJsonArray(currentValue as String)
                    } catch (e: Exception) {
                        JsonUtils.jsonArrayToList(JSONArray())
                    }
                } else {
                    JsonUtils.jsonArrayToList(JSONArray())
                }
            }
            "null" -> null
            else -> currentValue
        }
    }

    // Update value when type changes
    fun onTypeChanged(newType: String) {
        val convertedValue = when (newType) {
            "String" -> when (currentValue) {
                is Boolean, is Number -> currentValue.toString()
                else -> ""
            }
            "Number" -> when (currentValue) {
                is String -> currentValue.toDoubleOrNull()?.toString() ?: "0"
                is Number -> currentValue.toString()
                else -> "0"
            }
            "Boolean" -> when (currentValue) {
                is Boolean -> currentValue
                is String -> currentValue.equals("true", ignoreCase = true)
                else -> false
            }
            "Object" -> "{}"
            "Array" -> "[]"
            "null" -> null
            else -> ""
        }

        selectedType = newType
        currentValue = convertedValue
    }

    Dialog(onDismissRequest = onDismiss) {
        ElevatedCard(
            shape = RoundedCornerShape(16.dp),
            modifier = modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                Text(
                    text = when {
                        isAdding -> stringResource(R.string.add_json_item)
                        else -> stringResource(R.string.edit_json_item)
                    },
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                // Key field (not shown for array items when editing)
                if (!parentIsArray || isAdding) {
                    OutlinedTextField(
                        value = key,
                        onValueChange = {
                            key = it
                            isKeyValid = it.isNotBlank()
                        },
                        label = { Text(stringResource(R.string.key)) },
                        isError = !isKeyValid,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !parentIsArray || isAdding
                    )
                }

                // Type selector component
                EditorTypeSelector(
                    selectedType = selectedType,
                    onTypeSelected = { onTypeChanged(it) },
                    modifier = Modifier.fillMaxWidth()
                )

                // Value field component (not shown for null type)
                if (selectedType != "null") {
                    EditorField(
                        type = selectedType,
                        initialValue = currentValue,
                        onValueChange = { currentValue = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = stringResource(R.string.value),
                        isError = !isValueValid
                    )
                }

                // Error message
                if (!isKeyValid || !isValueValid) {
                    Text(
                        text = validationMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action buttons row
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
                                val finalKey = if (parentIsArray && !isAdding) currentKey else key
                                onSave(finalKey, parseValueForSave())
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

                // Delete button in its own row if not adding
                if (!isAdding) {
                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.delete))
                    }
                }
            }
        }
    }
}