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
            val isObject = value is Map<*, *>
            val isArray = value is List<*>

            // Enhanced metadata for display
            val objectKeys = if (isObject) {
                @Suppress("UNCHECKED_CAST")
                (value as Map<String, Any?>).keys.toList()
            } else {
                emptyList()
            }

            val arraySize = if (isArray) {
                @Suppress("UNCHECKED_CAST")
                (value as List<*>).size
            } else {
                0
            }

            val arrayItemType = if (isArray && (value as List<*>).isNotEmpty()) {
                val firstItem = (value as List<*>)[0]
                when {
                    firstItem is Map<*, *> -> "Object"
                    firstItem is List<*> -> "Array"
                    firstItem == null -> "null"
                    else -> firstItem::class.java.simpleName
                }
            } else {
                ""
            }

            JsonNavigationItem(
                key = key,
                node = value,
                isArray = isArray,
                isObject = isObject,
                objectKeys = objectKeys,
                arraySize = arraySize,
                arrayItemType = arrayItemType
            )
        }
    }

    /**
     * Get navigation items for a JSON array
     */
    fun getArrayEntries(jsonArray: List<Any?>): List<JsonNavigationItem> {
        return jsonArray.mapIndexed { index, value ->
            val isObject = value is Map<*, *>
            val isArray = value is List<*>

            // Enhanced metadata for display
            val objectKeys = if (isObject) {
                @Suppress("UNCHECKED_CAST")
                (value as Map<String, Any?>).keys.toList()
            } else {
                emptyList()
            }

            val arraySize = if (isArray) {
                @Suppress("UNCHECKED_CAST")
                (value as List<*>).size
            } else {
                0
            }

            val arrayItemType = if (isArray && (value as List<*>).isNotEmpty()) {
                val firstItem = (value as List<*>)[0]
                when {
                    firstItem is Map<*, *> -> "Object"
                    firstItem is List<*> -> "Array"
                    firstItem == null -> "null"
                    else -> firstItem::class.java.simpleName
                }
            } else {
                ""
            }

            JsonNavigationItem(
                key = index.toString(),
                node = value,
                isArray = isArray,
                isObject = isObject,
                objectKeys = objectKeys,
                arraySize = arraySize,
                arrayItemType = arrayItemType
            )
        }
    }
}