package com.example.jsonviewer.utils

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Enhanced utility class for JSON operations including editing
 */
object JsonUtils {

    /**
     * Prettify a JSON string with proper indentation
     *
     * @param jsonString The raw JSON string to format
     * @param indentSpaces Number of spaces to use for indentation (default 2)
     * @return Formatted JSON string or original string if it's not valid JSON
     */
    fun prettifyJson(jsonString: String, indentSpaces: Int = 2): String {
        return try {
            if (jsonString.trim().startsWith("{")) {
                // It's a JSON object
                val jsonObject = JSONObject(jsonString)
                jsonObject.toString(indentSpaces)
            } else if (jsonString.trim().startsWith("[")) {
                // It's a JSON array
                val jsonArray = JSONArray(jsonString)
                jsonArray.toString(indentSpaces)
            } else {
                // Not valid JSON
                jsonString
            }
        } catch (e: JSONException) {
            // Return original if it's not valid JSON
            jsonString
        }
    }

    /**
     * Minify a JSON string by removing formatting whitespace
     *
     * @param jsonString The formatted JSON string to minify
     * @return Minified JSON string or original string if it's not valid JSON
     */
    fun minifyJson(jsonString: String): String {
        return try {
            if (jsonString.trim().startsWith("{")) {
                // It's a JSON object
                val jsonObject = JSONObject(jsonString)
                jsonObject.toString()
            } else if (jsonString.trim().startsWith("[")) {
                // It's a JSON array
                val jsonArray = JSONArray(jsonString)
                jsonArray.toString()
            } else {
                // Not valid JSON
                jsonString
            }
        } catch (e: JSONException) {
            // Return original if it's not valid JSON
            jsonString
        }
    }

    /**
     * Check if a string is valid JSON
     *
     * @param jsonString The string to check
     * @return True if valid JSON object or array, false otherwise
     */
    fun isValidJson(jsonString: String): Boolean {
        if (jsonString.trim().isEmpty()) {
            return false
        }

        return try {
            if (jsonString.trim().startsWith("{")) {
                JSONObject(jsonString)
                true
            } else if (jsonString.trim().startsWith("[")) {
                JSONArray(jsonString)
                true
            } else {
                false
            }
        } catch (e: JSONException) {
            false
        }
    }

    /**
     * Parse a JSON object string to a Map
     */
    fun parseJsonObject(jsonString: String): Map<String, Any?> {
        return try {
            val jsonObject = JSONObject(jsonString)
            jsonObjectToMap(jsonObject)
        } catch (e: JSONException) {
            emptyMap()
        }
    }

    /**
     * Parse a JSON array string to a List
     */
    fun parseJsonArray(jsonString: String): List<Any?> {
        return try {
            val jsonArray = JSONArray(jsonString)
            jsonArrayToList(jsonArray)
        } catch (e: JSONException) {
            emptyList()
        }
    }

    /**
     * Convert a JSONObject to a Map
     */
    fun jsonObjectToMap(jsonObject: JSONObject): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        val keys = jsonObject.keys()

        while (keys.hasNext()) {
            val key = keys.next()
            val value = jsonObject.get(key)

            map[key] = when (value) {
                is JSONObject -> jsonObjectToMap(value)
                is JSONArray -> jsonArrayToList(value)
                JSONObject.NULL -> null
                else -> value
            }
        }

        return map
    }

    /**
     * Convert a JSONArray to a List
     */
    fun jsonArrayToList(jsonArray: JSONArray): List<Any?> {
        val list = mutableListOf<Any?>()

        for (i in 0 until jsonArray.length()) {
            val value = jsonArray.get(i)

            list.add(when (value) {
                is JSONObject -> jsonObjectToMap(value)
                is JSONArray -> jsonArrayToList(value)
                JSONObject.NULL -> null
                else -> value
            })
        }

        return list
    }

    /**
     * Modify a JSON object by adding, updating, or removing a key-value pair
     *
     * @param jsonString The JSON string to modify
     * @param path The path to the parent object (dot-separated keys)
     * @param key The key to add, update, or remove
     * @param value The new value (null to remove)
     * @param isDeleting Whether the operation is a deletion
     * @return The modified JSON string
     */
    fun modifyJsonObject(
        jsonString: String,
        path: List<String>,
        key: String,
        value: Any?,
        isDeleting: Boolean = false
    ): String {
        try {
            // Parse the JSON string
            val jsonObject = if (jsonString.trim().startsWith("{")) {
                JSONObject(jsonString)
            } else if (jsonString.trim().startsWith("[")) {
                // Special case for root array
                val rootArray = JSONArray(jsonString)
                if (path.isEmpty()) {
                    // Direct modification of root array
                    if (isDeleting) {
                        // Remove item at index
                        val index = key.toInt()
                        val newArray = JSONArray()
                        for (i in 0 until rootArray.length()) {
                            if (i != index) {
                                newArray.put(rootArray.get(i))
                            }
                        }
                        return newArray.toString()
                    } else if (key.toIntOrNull() != null) {
                        // Update item at index
                        val index = key.toInt()
                        if (index >= 0 && index < rootArray.length()) {
                            rootArray.put(index, convertToJsonValue(value))
                        } else if (index == rootArray.length()) {
                            // Append to the end
                            rootArray.put(convertToJsonValue(value))
                        }
                        return rootArray.toString()
                    } else {
                        // Add new item at the end
                        rootArray.put(convertToJsonValue(value))
                        return rootArray.toString()
                    }
                } else {
                    // Wrap array in an object for path traversal
                    val wrapper = JSONObject()
                    wrapper.put("root", rootArray)
                    wrapper
                }
            } else {
                return jsonString
            }

            // Navigate to the parent object/array based on path
            var current: Any = jsonObject
            val isRootArray = jsonString.trim().startsWith("[") && path.isNotEmpty()

            for (i in path.indices) {
                val pathSegment = path[i]

                current = if (isRootArray && i == 0) {
                    // Special handling for root array wrapped in object
                    (current as JSONObject).getJSONArray("root")
                } else if (current is JSONObject) {
                    if (pathSegment.toIntOrNull() != null) {
                        throw JSONException("Cannot use numeric index with object")
                    }
                    (current).get(pathSegment)
                } else if (current is JSONArray) {
                    (current).get(pathSegment.toInt())
                } else {
                    throw JSONException("Invalid path segment: $pathSegment")
                }
            }

            // Modify the target object/array
            if (current is JSONObject) {
                val targetObject = current
                if (isDeleting) {
                    targetObject.remove(key)
                } else {
                    targetObject.put(key, convertToJsonValue(value))
                }
            } else if (current is JSONArray) {
                val targetArray = current
                if (isDeleting) {
                    // Remove item at index
                    val index = key.toInt()
                    val newArray = JSONArray()
                    for (i in 0 until targetArray.length()) {
                        if (i != index) {
                            newArray.put(targetArray.get(i))
                        }
                    }

                    // Replace the original array
                    var parentObject: Any = jsonObject
                    for (i in 0 until path.size - 1) {
                        val pathSegment = path[i]
                        parentObject = if (isRootArray && i == 0) {
                            (parentObject as JSONObject).getJSONObject("root")
                        } else if (parentObject is JSONObject) {
                            if (pathSegment.toIntOrNull() != null) {
                                throw JSONException("Cannot use numeric index with object")
                            }
                            (parentObject).get(pathSegment)
                        } else if (parentObject is JSONArray) {
                            (parentObject).get(pathSegment.toInt())
                        } else {
                            throw JSONException("Invalid path segment: $pathSegment")
                        }
                    }

                    val lastSegment = path.lastOrNull()
                    if (lastSegment != null) {
                        if (parentObject is JSONArray) {
                            // Parent is an array
                            (parentObject).put(lastSegment.toInt(), newArray)
                        } else if (parentObject is JSONObject) {
                            // Parent is an object
                            (parentObject).put(lastSegment, newArray)
                        }
                    }
                } else if (key.toIntOrNull() != null) {
                    // Update at specific index
                    val index = key.toInt()
                    if (index >= 0 && index < targetArray.length()) {
                        targetArray.put(index, convertToJsonValue(value))
                    } else if (index == targetArray.length()) {
                        // Append to the end
                        targetArray.put(convertToJsonValue(value))
                    }
                } else {
                    // Append to the end
                    targetArray.put(convertToJsonValue(value))
                }
            }

            // Return the modified JSON
            return if (isRootArray) {
                jsonObject.getJSONArray("root").toString()
            } else {
                jsonObject.toString()
            }
        } catch (e: Exception) {
            return jsonString
        }
    }

    /**
     * Convert a Kotlin value to a JSON-compatible value
     */
    private fun convertToJsonValue(value: Any?): Any? {
        return when (value) {
            null -> JSONObject.NULL
            is Map<*, *> -> {
                val jsonObject = JSONObject()
                @Suppress("UNCHECKED_CAST")
                (value as Map<String, Any?>).forEach { (k, v) ->
                    jsonObject.put(k, convertToJsonValue(v))
                }
                jsonObject
            }
            is List<*> -> {
                val jsonArray = JSONArray()
                value.forEach { item ->
                    jsonArray.put(convertToJsonValue(item))
                }
                jsonArray
            }
            else -> value
        }
    }

    /**
     * Extension function to convert JSONObject to Map for easier use
     */
//    fun JSONObject.toMap(): Map<String, Any?> {
//        return jsonObjectToMap(this)
//    }

    /**
     * Extension function to convert JSONArray to List for easier use
     */
//    fun JSONArray.toList(): List<Any?> {
//        return jsonArrayToList(this)
//    }
}