package de.felixlf.gradingscale2.utils

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.selectAll
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import de.felixlf.gradingscale2.entities.util.toLocaleDoubleOrNull
import kotlinx.coroutines.delay
import kotlin.math.abs

/** Half of one step of the app's two-decimal formatter: the furthest a rendering can sit from its value. */
private const val TWO_DECIMAL_ECHO_TOLERANCE = 0.005

/** Absorbs last-bit binary floating-point noise exactly on the half-step boundary (0.115 - 0.11). */
private const val ECHO_FLOAT_EPSILON = 1e-9

/**
 * Manages the text field state and updates the external field value after a debounce [delay].
 * @param externalFieldValue The external field value synced into this text field when modified externally.
 * @param delay The delay in milliseconds before invoking [onChangeExternalFieldValue]. Zero skips delaying.
 * @param isNumeric When true, formatting echoes that represent the same numeric value (such as `"85."` or `"85.0"` vs `"85"`) do not overwrite user input.
 * @param selectAllOnSettle When true, a genuine external value change selects the whole field while [isFocused] so the next keystroke replaces it.
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
    val lastCommit = remember { LastCommitTracker() }

    // Debounced commit of the user's own edits. It must never move the selection or replace
    // text: the user may still be typing, and selecting all here would fight their editing.
    LaunchedEffect(textFieldState.text, delay) {
        if (delay > 0L) {
            delay(delay)
        }
        val fieldText = textFieldState.text.toString()
        // A blur may have committed the same text already while the timer was still pending.
        val alreadyCommittedAfterBlur = lastCommit.matches(fieldText) && !latestIsFocused
        if (fieldText != latestExternalValue && !alreadyCommittedAfterBlur) {
            lastCommit.record(fieldText, latestExternalValue)
            textFieldState.latestOnChange(fieldText)
        }
    }

    // Only a genuine external value change may rewrite the field or select all text. While this
    // field is focused, values that merely format/round the value this field itself committed are
    // echoes, not external changes. Once blurred, the external value is always authoritative.
    LaunchedEffect(externalFieldValue, isNumeric) {
        val currentText = textFieldState.text.toString()
        if (
            shouldSyncExternalText(
                currentText = currentText,
                externalFieldValue = externalFieldValue,
                isNumeric = isNumeric,
                ownCommittedText = lastCommit.text,
                isFocused = latestIsFocused,
            )
        ) {
            // The external value has genuinely moved on: any own-commit marker is stale now and
            // must not classify a later external change as an echo of an older commit.
            lastCommit.clear()
            if (latestIsFocused && selectAllOnSettle && externalFieldValue.isNotEmpty()) {
                textFieldState.edit {
                    replace(0, length, externalFieldValue)
                    selectAll()
                }
            } else {
                textFieldState.setTextAndPlaceCursorAtEnd(externalFieldValue)
            }
        }
    }

    // Finalize the value on blur so a field left blank (or partially typed) is validated
    // once the user is done instead of being rewritten mid-edit.
    LaunchedEffect(isFocused) {
        if (!isFocused) {
            val fieldText = textFieldState.text.toString()
            // Skip when this exact text was already committed and the domain has since responded.
            val alreadySettled = lastCommit.matches(fieldText) && latestExternalValue != lastCommit.externalValue
            if (fieldText != latestExternalValue && !alreadySettled) {
                lastCommit.record(fieldText, latestExternalValue)
                textFieldState.latestOnChange(fieldText)
            }
        }
    }

    return textFieldState
}

/**
 * Remembers the last text this field committed so its own formatting/rounding echo, or a
 * duplicate debounce/blur commit, is not mistaken for a genuine external change.
 */
private class LastCommitTracker {
    var text: String? = null
        private set
    var externalValue: String? = null
        private set

    fun record(text: String, externalValue: String) {
        this.text = text
        this.externalValue = externalValue
    }

    fun clear() {
        text = null
        externalValue = null
    }

    fun matches(fieldText: String): Boolean = fieldText == text
}

internal fun shouldSyncExternalText(
    currentText: String,
    externalFieldValue: String,
    isNumeric: Boolean,
    ownCommittedText: String? = null,
    isFocused: Boolean = true,
): Boolean {
    if (externalFieldValue == currentText) return false
    if (isNumeric) {
        val currentNumber = currentText.toLocaleDoubleOrNull()
        val externalNumber = externalFieldValue.toLocaleDoubleOrNull()
        if (currentNumber != null && externalNumber != null && currentNumber == externalNumber) {
            return false
        }
        // Own-edit echoes only exist while this field is focused and the user's text is still in
        // place. After blur, any external change is authoritative, even a numerically close one.
        if (isFocused &&
            ownCommittedText != null &&
            isEchoOfOwnCommit(
                currentText = currentText,
                committedText = ownCommittedText,
                externalFieldValue = externalFieldValue,
            )
        ) {
            return false
        }
    }
    return true
}

/**
 * True when [externalFieldValue] is the two-decimal rendering of the value this field committed.
 * The app formats every external numeric string with a fixed two-decimal formatter, so a rendered
 * value can never sit further than half of one step (0.005) from the value it renders. That fixed
 * half-step bound is the echo tolerance; deriving it from the digit count of the external string
 * would make a genuine external `86` look like an echo of an own commit of `85.5`. The bound is
 * symmetric because derived doubles can lose their last bit: `0.115` commits as `0.00115`, which
 * re-renders as `0.11499999999999999` and formats to `0.11`, while `0.115` itself canonicalizes to
 * `0.12`. Both lie within half a step of the own commit, so both are echoes.
 */
internal fun isEchoOfOwnCommit(
    currentText: String,
    committedText: String,
    externalFieldValue: String,
): Boolean {
    val currentNumber = currentText.toLocaleDoubleOrNull() ?: return false
    val committedNumber = committedText.toLocaleDoubleOrNull() ?: return false
    // The user's own text must still be in place; only then can the external value be its echo.
    if (currentNumber != committedNumber) return false
    val externalNumber = externalFieldValue.toLocaleDoubleOrNull() ?: return false
    return abs(externalNumber - committedNumber) <= TWO_DECIMAL_ECHO_TOLERANCE + ECHO_FLOAT_EPSILON
}

internal fun isPartialDecimalToken(text: String): Boolean {
    val trimmed = text.trim()
    return trimmed == "." || trimmed == "," || trimmed == "-" || trimmed == "0." || trimmed == "0,"
}
