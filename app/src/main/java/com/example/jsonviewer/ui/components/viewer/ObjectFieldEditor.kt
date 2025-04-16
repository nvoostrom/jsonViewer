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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.jsonviewer.R
import com.example.jsonviewer.data.JsonNavigationItem
import com.example.jsonviewer.utils.JsonUtils
import org.json.JSONArray
import org.json.JSONObject

/**
 * Dialog for editing object fields with individual field inputs
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObjectFieldEditor(
    item: JsonNavigationItem,
    onDismiss: () -> Unit,
    onSave: (Map<String, Any?>) -> Unit,
    onDelete: () -> Unit = {}
) {
    // Get the fields from the object
    @Suppress("UNCHECKED_CAST")
    val objectMap = (item.node as? Map<String, Any?>) ?: emptyMap()

    // State for tracking fields
    val fields = remember { mutableStateListOf<ObjectField>() }
    var validationError by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    // Initialize fields
    if (fields.isEmpty()) {
        objectMap.forEach { (key, value) ->
            fields.add(
                ObjectField(
                    key = key,
                    value = value,
                    type = when(value) {
                        is Map<*, *> -> "Object"
                        is List<*> -> "Array"
                        is String -> "String"
                        is Number -> "Number"
                        is Boolean -> "Boolean"
                        null -> "null"
                        else -> "String"
                    }
                )
            )
        }
    }

    // Function to add a new field
    fun addField() {
        fields.add(ObjectField(key = "", value = "", type = "String"))
    }

    // Function to remove a field
    fun removeField(index: Int) {
        if (index >= 0 && index < fields.size) {
            fields.removeAt(index)
        }
    }

    // Function to validate fields
    fun validate(): Boolean {
        // Check for empty keys
        fields.forEachIndexed { index, field ->
            if (field.key.isBlank()) {
                validationError = "Field #${index + 1} has an empty key"
                return false
            }
        }

        // Check for duplicate keys
        val keys = fields.map { it.key }
        if (keys.distinct().size != keys.size) {
            validationError = "Duplicate keys are not allowed"
            return false
        }

        // Validate values based on type
        fields.forEachIndexed { index, field ->
            when (field.type) {
                "Number" -> {
                    try {
                        (field.value as? String)?.toDouble()
                    } catch (e: NumberFormatException) {
                        validationError = "Field '${field.key}' has an invalid number format"
                        return false
                    }
                }
                "Boolean" -> {
                    val strValue = field.value as? String ?: ""
                    if (strValue != "true" && strValue != "false") {
                        validationError = "Field '${field.key}' must be 'true' or 'false'"
                        return false
                    }
                }
                "Object" -> {
                    val strValue = field.value as? String ?: ""
                    if (strValue.isNotBlank() && (!JsonUtils.isValidJson(strValue) || !strValue.trim().startsWith("{"))) {
                        validationError = "Field '${field.key}' has an invalid JSON object"
                        return false
                    }
                }
                "Array" -> {
                    val strValue = field.value as? String ?: ""
                    if (strValue.isNotBlank() && (!JsonUtils.isValidJson(strValue) || !strValue.trim().startsWith("["))) {
                        validationError = "Field '${field.key}' has an invalid JSON array"
                        return false
                    }
                }
            }
        }

        validationError = null
        return true
    }

    // Function to build result object
    fun buildResult(): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        fields.forEach { field ->
            val parsedValue = when (field.type) {
                "String" -> field.value as? String ?: ""
                "Number" -> (field.value as? String)?.toDoubleOrNull() ?: 0.0
                "Boolean" -> (field.value as? String)?.toBoolean() ?: false
                "Object" -> {
                    val strValue = field.value as? String ?: ""
                    if (strValue.isBlank()) {
                        JsonUtils.jsonObjectToMap(JSONObject())
                    } else {
                        try {
                            JsonUtils.parseJsonObject(strValue)
                        } catch (e: Exception) {
                            JsonUtils.jsonObjectToMap(JSONObject())
                        }
                    }
                }
                "Array" -> {
                    val strValue = field.value as? String ?: ""
                    if (strValue.isBlank()) {
                        JsonUtils.jsonArrayToList(JSONArray())
                    } else {
                        try {
                            JsonUtils.parseJsonArray(strValue)
                        } catch (e: Exception) {
                            JsonUtils.jsonArrayToList(JSONArray())
                        }
                    }
                }
                "null" -> null
                else -> field.value
            }

            result[field.key] = parsedValue
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
                // Title with field count
                Text(
                    text = "Edit Object: ${item.key}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "${fields.size} fields",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Fields
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

                // Validation error
                validationError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
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
                                onSave(buildResult())
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

                // Delete button
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

/**
 * Data class representing an object field
 */
data class ObjectField(
    val key: String,
    val value: Any?,
    val type: String
)

/**
 * UI component for a single object field input
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObjectFieldInput(
    field: ObjectField,
    onFieldChange: (ObjectField) -> Unit,
    onRemove: () -> Unit
) {
    val types = listOf("String", "Number", "Boolean", "Object", "Array", "null")
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Key name field
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Field",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove field",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            OutlinedTextField(
                value = field.key,
                onValueChange = {
                    onFieldChange(field.copy(key = it))
                },
                label = { Text("Key") },
                modifier = Modifier.fillMaxWidth()
            )

            // Type dropdown
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = field.type,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Type") },
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
                                val newValue = when (type) {
                                    "String" -> (field.value as? String) ?: ""
                                    "Number" -> (field.value as? Number)?.toString() ?: "0"
                                    "Boolean" -> (field.value as? Boolean)?.toString() ?: "false"
                                    "Object" -> "{}"
                                    "Array" -> "[]"
                                    "null" -> ""
                                    else -> field.value.toString()
                                }
                                onFieldChange(field.copy(type = type, value = newValue))
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Value field (not shown for null type)
            if (field.type != "null") {
                val valueStr = when {
                    field.value == null -> ""
                    field.value is String -> field.value
                    else -> field.value.toString()
                }

                OutlinedTextField(
                    value = valueStr,
                    onValueChange = {
                        onFieldChange(field.copy(value = it))
                    },
                    label = { Text("Value") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace
                    )
                )
            }
        }
    }
}