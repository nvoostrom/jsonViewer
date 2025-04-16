package com.example.jsonviewer.data.storage

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/**
 * Utility class for JSON serialization/deserialization of storage objects
 */
object JsonStorageUtils {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    // Type adapters
    private val recentFileListType = Types.newParameterizedType(
        List::class.java,
        RecentFile::class.java
    )

    private val savedJsonListType = Types.newParameterizedType(
        List::class.java,
        SavedJson::class.java
    )

    private val recentFileAdapter: JsonAdapter<List<RecentFile>> = moshi.adapter(recentFileListType)
    private val savedJsonAdapter: JsonAdapter<List<SavedJson>> = moshi.adapter(savedJsonListType)

    // Convert RecentFile list to JSON string
    fun toJson(recentFiles: List<RecentFile>): String {
        return recentFileAdapter.toJson(recentFiles) ?: "[]"
    }

    // Parse JSON string to RecentFile list
    fun fromRecentFileJsonList(json: String): List<RecentFile> {
        return try {
            recentFileAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Convert SavedJson list to JSON string
    fun toJson(savedJsons: List<SavedJson>): String {
        return savedJsonAdapter.toJson(savedJsons) ?: "[]"
    }

    // Parse JSON string to SavedJson list
    fun fromSavedJsonList(json: String): List<SavedJson> {
        return try {
            savedJsonAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}