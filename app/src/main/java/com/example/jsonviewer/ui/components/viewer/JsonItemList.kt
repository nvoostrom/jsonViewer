package com.example.jsonviewer.ui.components.viewer

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import com.example.jsonviewer.data.JsonNavigationItem

/**
 * Component that displays a list of JSON items
 */
@Composable
fun JsonItemsList(
    items: List<JsonNavigationItem>,
    onItemClick: (JsonNavigationItem) -> Unit
) {
    LazyColumn(
        state = rememberLazyListState()
    ) {
        items(items) { item ->
            JsonItemCard(item, onItemClick)
        }
    }
}