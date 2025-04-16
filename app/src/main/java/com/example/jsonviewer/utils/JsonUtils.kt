package com.example.jsonviewer.utils

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Utility class for common JSON operations
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
}