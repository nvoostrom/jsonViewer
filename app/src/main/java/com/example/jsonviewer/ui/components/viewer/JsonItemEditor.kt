package com.example.jsonviewer.ui.components.viewer

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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.jsonviewer.R
import com.example.jsonviewer.data.JsonNavigationItem
import com.example.jsonviewer.utils.JsonUtils
import org.json.JSONArray
import org.json.JSONObject

/**
 * Dialog for editing, adding, or deleting JSON items
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JsonItemEditor(
    item: JsonNavigationItem? = null,
    isAdding: Boolean = false,
    parentIsArray: Boolean = false,
    currentKey: String = "",
    onDismiss: () -> Unit,
    onSave: (key: String, value: Any?) -> Unit,
    onDelete: () -> Unit = {}
) {
    // State for the key and value
    var key by remember { mutableStateOf(if (!parentIsArray) item?.key ?: "" else currentKey) }
    var valueText by remember {
        mutableStateOf(
            when {
                item == null -> ""
                item.node == null -> "null"
                item.node is String -> item.node
                else -> item.node.toString()
            }
        )
    }

    // Type dropdown state
    val types = listOf("String", "Number", "Boolean", "Object", "Array", "null")
    var expanded by remember { mutableStateOf(false) }
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

    // Validate function
    fun validate(): Boolean {
        // Key validation (not needed for array items)
        if (!parentIsArray && key.isBlank()) {
            isKeyValid = false
            validationMessage = "Key cannot be empty"
            return false
        }

        // Value validation based on type
        when (selectedType) {
            "Number" -> {
                try {
                    valueText.toDouble()
                    isValueValid = true
                } catch (e: NumberFormatException) {
                    isValueValid = false
                    validationMessage = "Invalid number format"
                    return false
                }
            }
            "Boolean" -> {
                if (valueText != "true" && valueText != "false") {
                    isValueValid = false
                    validationMessage = "Boolean must be 'true' or 'false'"
                    return false
                }
                isValueValid = true
            }
            "Object" -> {
                if (valueText.isNotBlank()) {
                    isValueValid = JsonUtils.isValidJson(valueText) && valueText.trim().startsWith("{")
                    if (!isValueValid) {
                        validationMessage = "Invalid JSON object"
                        return false
                    }
                }
                isValueValid = true
            }
            "Array" -> {
                if (valueText.isNotBlank()) {
                    isValueValid = JsonUtils.isValidJson(valueText) && valueText.trim().startsWith("[")
                    if (!isValueValid) {
                        validationMessage = "Invalid JSON array"
                        return false
                    }
                }
                isValueValid = true
            }
        }

        return isKeyValid && isValueValid
    }

    // Parse the value based on type
    fun parseValue(): Any? {
        return when (selectedType) {
            "String" -> valueText
            "Number" -> valueText.toDoubleOrNull() ?: 0.0
            "Boolean" -> valueText.toBoolean()
            "Object" -> {
                if (valueText.isBlank()) {
                    JsonUtils.jsonObjectToMap(JSONObject())
                } else {
                    try {
                        JsonUtils.parseJsonObject(valueText)
                    } catch (e: Exception) {
                        JsonUtils.jsonObjectToMap(JSONObject())
                    }
                }
            }
            "Array" -> {
                if (valueText.isBlank()) {
                    JsonUtils.jsonArrayToList(JSONArray())
                } else {
                    try {
                        JsonUtils.parseJsonArray(valueText)
                    } catch (e: Exception) {
                        JsonUtils.jsonArrayToList(JSONArray())
                    }
                }
            }
            "null" -> null
            else -> null
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        ElevatedCard(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
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

                // Type selection dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.value_type)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        types.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    selectedType = type
                                    expanded = false

                                    // Reset value for certain types
                                    valueText = when (type) {
                                        "Boolean" -> "false"
                                        "Object" -> "{}"
                                        "Array" -> "[]"
                                        "null" -> ""
                                        else -> valueText
                                    }
                                }
                            )
                        }
                    }
                }

                // Value field (not shown for null type)
                if (selectedType != "null") {
                    OutlinedTextField(
                        value = valueText,
                        onValueChange = {
                            valueText = it
                            isValueValid = true  // Will be validated on save
                        },
                        label = { Text(stringResource(R.string.value)) },
                        isError = !isValueValid,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace
                        )
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

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Delete button (only shown when editing)
                    if (!isAdding) {
                        OutlinedButton(
                            onClick = onDelete,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.delete))
                        }

                        Spacer(modifier = Modifier.width(8.dp))
                    }

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

                    Spacer(modifier = Modifier.width(8.dp))

                    // Save button
                    Button(
                        onClick = {
                            if (validate()) {
                                val finalKey = if (parentIsArray && !isAdding) currentKey else key
                                onSave(finalKey, parseValue())
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