package de.felixlf.gradingscale2.utils

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay

/**
 * Manages the text field state and updates the external field value after a delay.
 * @param externalFieldValue The external field value that should be updated. Whenever the external field value changes, the text field
 * will be updated after a delay.
 * @param delay The delay in milliseconds after which the external field value should be updated.
 * @param onChangeExternalFieldValue The callback that should be called when the external field value should be updated. This will be called
 * immediately when the text field value changes in case the external field value is different.
 * @return The text field state.
 */
@Composable
fun textFieldManager(
    externalFieldValue: String,
    delay: Long = 200,
    onChangeExternalFieldValue: (String) -> Unit,
): TextFieldState {
    val textFieldState = rememberTextFieldState(externalFieldValue)

    LaunchedEffect(textFieldState.text, onChangeExternalFieldValue) {
        delay(delay)
        val fieldText = textFieldState.text.toString()
        if (fieldText != externalFieldValue) {
            onChangeExternalFieldValue(fieldText)
        }
    }

    LaunchedEffect(externalFieldValue) {
        if (externalFieldValue != textFieldState.text) {
            textFieldState.setTextAndPlaceCursorAtEnd(externalFieldValue)
        }
    }

    return textFieldState
}
