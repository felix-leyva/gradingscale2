package de.felixlf.gradingscale2.utils

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.selectAll
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import de.felixlf.gradingscale2.entities.util.toLocaleDoubleOrNull
import kotlinx.coroutines.delay

/**
 * Manages the text field state and updates the external field value after a debounce [delay].
 * @param externalFieldValue The external field value synced into this text field when modified externally.
 * @param delay The delay in milliseconds before invoking [onChangeExternalFieldValue]. Zero skips delaying.
 * @param isNumeric When true, formatting echoes that represent the same numeric value (such as `"85."` or `"85.0"` vs `"85"`) do not overwrite user input.
 * @param selectAllOnSettle When true, settled non-empty text without a trailing decimal separator is selected so the next keystroke replaces it.
 * @param isFocused Whether this field currently has focus. [selectAllOnSettle] only applies while focused.
 * @param onChangeExternalFieldValue The callback invoked when the text field value differs from [externalFieldValue].
 * @return The text field state.
 */
@Composable
fun textFieldManager(
    externalFieldValue: String,
    delay: Long = 400,
    isNumeric: Boolean = false,
    selectAllOnSettle: Boolean = false,
    isFocused: Boolean = false,
    onChangeExternalFieldValue: TextFieldState.(String) -> Unit,
): TextFieldState {
    val textFieldState = rememberTextFieldState(externalFieldValue)
    val latestOnChange by rememberUpdatedState(onChangeExternalFieldValue)
    val latestExternalValue by rememberUpdatedState(externalFieldValue)
    val latestIsFocused by rememberUpdatedState(isFocused)

    LaunchedEffect(textFieldState.text, delay, selectAllOnSettle) {
        if (delay > 0L) {
            delay(delay)
        }
        val fieldText = textFieldState.text.toString()
        if (fieldText != latestExternalValue) {
            textFieldState.latestOnChange(fieldText)
        } else if (
            selectAllOnSettle &&
            latestIsFocused &&
            fieldText.isNotEmpty() &&
            !hasTrailingDecimalSeparator(fieldText)
        ) {
            textFieldState.edit { selectAll() }
        }
    }

    LaunchedEffect(externalFieldValue, isNumeric) {
        val currentText = textFieldState.text.toString()
        if (shouldSyncExternalText(currentText = currentText, externalFieldValue = externalFieldValue, isNumeric = isNumeric)) {
            textFieldState.setTextAndPlaceCursorAtEnd(externalFieldValue)
        }
    }

    return textFieldState
}

internal fun shouldSyncExternalText(
    currentText: String,
    externalFieldValue: String,
    isNumeric: Boolean,
): Boolean {
    if (externalFieldValue == currentText) return false
    if (isNumeric) {
        val currentNumber = currentText.toLocaleDoubleOrNull()
        val externalNumber = externalFieldValue.toLocaleDoubleOrNull()
        if (currentNumber != null && externalNumber != null && currentNumber == externalNumber) {
            return false
        }
    }
    return true
}

internal fun hasTrailingDecimalSeparator(text: String): Boolean {
    val trimmed = text.trim()
    return trimmed.endsWith('.') || trimmed.endsWith(',')
}

internal fun isPartialDecimalToken(text: String): Boolean {
    val trimmed = text.trim()
    return trimmed == "." || trimmed == "," || trimmed == "-"
}
