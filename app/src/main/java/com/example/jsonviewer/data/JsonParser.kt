package com.example.jsonviewer.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.json.JSONArray
import org.json.JSONObject

/**
 * Utility class for parsing and navigating JSON data
 */
class JsonParser {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    /**
     * Parse a JSON string into a dynamic structure
     */
    fun parseJson(jsonString: String): Map<String, Any?> {
        val jsonObject = JSONObject(jsonString)
        return jsonObjectToMap(jsonObject)
    }

    /**
     * Convert a JSONObject to a Map for easier navigation
     */
    private fun jsonObjectToMap(jsonObject: JSONObject): Map<String, Any?> {
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
     * Convert a JSONArray to a List for easier navigation
     */
    private fun jsonArrayToList(jsonArray: JSONArray): List<Any?> {
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
     * Get navigation items for a JSON object
     */
    fun getObjectEntries(jsonMap: Map<String, Any?>): List<JsonNavigationItem> {
        return jsonMap.map { (key, value) ->
            JsonNavigationItem(
                key = key,
                node = value,
                isArray = value is List<*>,
                isObject = value is Map<*, *>
            )
        }
    }

    /**
     * Get navigation items for a JSON array
     */
    fun getArrayEntries(jsonArray: List<Any?>): List<JsonNavigationItem> {
        return jsonArray.mapIndexed { index, value ->
            JsonNavigationItem(
                key = index.toString(),
                node = value,
                isArray = value is List<*>,
                isObject = value is Map<*, *>
            )
        }
    }
}