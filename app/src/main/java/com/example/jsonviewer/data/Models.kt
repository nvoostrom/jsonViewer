package com.example.jsonviewer.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Represents the root JSON structure
 */
@JsonClass(generateAdapter = true)
data class JsonRoot(
    @Json(name = "notFound") val notFound: List<NotFoundItem> = emptyList()
)

/**
 * Represents an item in the notFound list
 */
@JsonClass(generateAdapter = true)
data class NotFoundItem(
    @Json(name = "title") val title: String = "",
    @Json(name = "status") val status: String = "",
    @Json(name = "details") val details: String = ""
)

/**
 * Generic class to represent any JSON object for flexible parsing
 */
data class JsonNode(
    val value: Any? = null,
    val children: Map<String, JsonNode> = emptyMap(),
    val arrayItems: List<JsonNode> = emptyList()
) {
    fun isObject(): Boolean = children.isNotEmpty()
    fun isArray(): Boolean = arrayItems.isNotEmpty()
    fun isPrimitive(): Boolean = !isObject() && !isArray()
}

/**
 * Utility class for JSON navigation
 */
data class JsonNavigationItem(
    val key: String,
    val node: Any?,
    val isArray: Boolean = false,
    val isObject: Boolean = false,
    val objectKeys: List<String> = emptyList(),
    val arraySize: Int = 0,
    val arrayItemType: String = ""
)