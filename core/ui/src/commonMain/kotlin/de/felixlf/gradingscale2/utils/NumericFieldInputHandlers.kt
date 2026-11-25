package de.felixlf.gradingscale2.utils

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.text.TextRange
import de.felixlf.gradingscale2.entities.util.toLocaleDoubleOrNull

/**
 * Input handlers shared by every numeric field driven by [textFieldManager].
 *
 * They live in :core:ui because more than one feature composes them (the calculator and the
 * grade scale list), and keeping them in a single place is what stops the comma/dot handling
 * from drifting between screens.
 */

private fun TextFieldState.replaceText(text: String) {
    edit {
        replace(0, length, text)
        selection = TextRange(length)
    }
}

fun applyTotalPointsInput(
    textFieldState: TextFieldState,
    rawText: String,
    fallbackText: String,
    onSetTotalPoints: (Double) -> Unit,
    isEditing: Boolean = false,
) {
    if (isPartialDecimalToken(rawText)) {
        // Keep partial input while editing; once the user is done restore the last valid value.
        if (!isEditing) textFieldState.replaceText(fallbackText)
        return
    }
    val trimmed = rawText.trim()
    if (trimmed.isEmpty()) {
        // While the user is editing, keep the field blank instead of writing a default
        // number back into it; the previous value is restored once editing ends.
        if (isEditing) return
        textFieldState.replaceText(fallbackText)
        return
    }
    val parsed = trimmed.toLocaleDoubleOrNull()
    if (parsed != null && parsed > 0.0) {
        // Domain validation still applies; never rewrite or re-select the user's own input.
        onSetTotalPoints(parsed)
    } else {
        // Out-of-range text is the user's own input: leave it until they finish editing.
        if (isEditing) return
        textFieldState.replaceText(fallbackText)
    }
}

fun applyEarnedPointsInput(
    textFieldState: TextFieldState,
    rawText: String,
    maxPoints: Double,
    fallbackText: String,
    onSetPoints: (Double) -> Unit,
    isEditing: Boolean = false,
) {
    if (isPartialDecimalToken(rawText)) {
        if (!isEditing) textFieldState.replaceText(fallbackText)
        return
    }
    val trimmed = rawText.trim()
    if (trimmed.isEmpty()) {
        if (isEditing) return
        textFieldState.replaceText(fallbackText)
        return
    }
    val parsed = trimmed.toLocaleDoubleOrNull()
    if (parsed != null && parsed in 0.0..maxPoints) {
        onSetPoints(parsed)
    } else {
        if (isEditing) return
        textFieldState.replaceText(fallbackText)
    }
}

fun applyPercentageInput(
    textFieldState: TextFieldState,
    rawText: String,
    fallbackText: String,
    onSetPercentage: (Double) -> Unit,
    isEditing: Boolean = false,
) {
    if (isPartialDecimalToken(rawText)) {
        if (!isEditing) textFieldState.replaceText(fallbackText)
        return
    }
    val trimmed = rawText.trim()
    if (trimmed.isEmpty()) {
        if (isEditing) return
        textFieldState.replaceText(fallbackText)
        return
    }
    val parsed = trimmed.toLocaleDoubleOrNull()
    if (parsed != null && parsed in 0.0..100.0) {
        onSetPercentage(parsed / 100.0)
    } else {
        if (isEditing) return
        textFieldState.replaceText(fallbackText)
    }
}
