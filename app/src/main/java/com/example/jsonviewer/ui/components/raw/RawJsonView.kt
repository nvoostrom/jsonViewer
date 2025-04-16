package com.example.jsonviewer.ui.components.raw

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

/**
 * Raw JSON view component with syntax highlighting and formatting controls
 */
@Composable
fun RawJsonView(
    json: String,
    isPrettified: Boolean = true,
    onToggleFormat: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Format controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isPrettified) "Prettified" else "Minified",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            ElevatedButton(
                onClick = onToggleFormat,
                modifier = Modifier.padding(4.dp)
            ) {
                Text(
                    text = if (isPrettified) "Minify" else "Prettify",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        // JSON content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            val scrollState = rememberScrollState()

            // Syntax highlighting
            val formattedJson = buildAnnotatedString {
                val state = SyntaxHighlightState()
                var insideString = false

                json.forEachIndexed { index, char ->
                    when {
                        char == '"' && (index == 0 || json[index - 1] != '\\') -> {
                            insideString = !insideString
                            if (insideString) {
                                state.setStringStart()
                            } else {
                                state.setStringEnd()
                            }
                        }
                        char == ':' && !insideString -> {
                            state.setPropEnd()
                        }
                        char.isDigit() && !insideString && !state.isInValue -> {
                            state.setNumberStart()
                        }
                        !char.isDigit() && !insideString && state.isInNumber -> {
                            state.setNumberEnd()
                        }
                        (char == 't' || char == 'f' || char == 'n') && !insideString && !state.isInValue -> {
                            state.setSpecialValueStart()
                        }
                        !char.isLetter() && !insideString && state.isInSpecialValue -> {
                            state.setSpecialValueEnd()
                        }
                    }

                    // Apply the appropriate style
                    val style = when {
                        insideString && state.isInProp -> SpanStyle(color = MaterialTheme.colorScheme.primary)
                        insideString -> SpanStyle(color = MaterialTheme.colorScheme.secondary)
                        state.isInNumber -> SpanStyle(color = MaterialTheme.colorScheme.error)
                        state.isInSpecialValue -> SpanStyle(color = MaterialTheme.colorScheme.tertiary)
                        char == '{' || char == '}' || char == '[' || char == ']' || char == ',' || char == ':' ->
                            SpanStyle(color = MaterialTheme.colorScheme.outline)
                        else -> SpanStyle(color = MaterialTheme.colorScheme.onSurface)
                    }

                    withStyle(style) {
                        append(char)
                    }
                }
            }

            Text(
                text = formattedJson,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState)
            )
        }
    }
}

/**
 * State class for JSON syntax highlighting
 */
class SyntaxHighlightState {
    var isInString = false
    var isInProp = false
    var isInValue = false
    var isInNumber = false
    var isInSpecialValue = false

    fun setStringStart() {
        isInString = true
        isInProp = !isInValue
    }

    fun setStringEnd() {
        isInString = false
        if (isInProp) {
            isInProp = false
            isInValue = true
        } else {
            isInValue = false
        }
    }

    fun setPropEnd() {
        isInProp = false
        isInValue = true
    }

    fun setNumberStart() {
        isInNumber = true
        isInValue = true
    }

    fun setNumberEnd() {
        isInNumber = false
        isInValue = false
    }

    fun setSpecialValueStart() {
        isInSpecialValue = true
        isInValue = true
    }

    fun setSpecialValueEnd() {
        isInSpecialValue = false
        isInValue = false
    }
}