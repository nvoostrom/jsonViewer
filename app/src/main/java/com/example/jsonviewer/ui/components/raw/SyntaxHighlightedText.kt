package com.example.jsonviewer.ui.components.raw

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle

/**
 * Component for displaying syntax-highlighted JSON text
 */
@Composable
fun SyntaxHighlightedText(
    json: String,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Apply syntax highlighting
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
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    )
}

/**
 * State class for JSON syntax highlighting
 */
class SyntaxHighlightState {
    private var isInString = false
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