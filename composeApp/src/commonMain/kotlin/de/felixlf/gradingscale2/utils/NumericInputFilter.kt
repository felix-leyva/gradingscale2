package de.felixlf.gradingscale2.utils

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.ui.text.input.KeyboardType

/**
 * InputTransformation that filters numeric input to allow only digits, decimal separators (. and ,), and minus sign
 * Based on the pattern from Compose Foundation's AllCapsTransformation
 */
data class NumericInputFilter(private val allowNegative: Boolean = false) : InputTransformation {
    override val keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)

    override fun TextFieldBuffer.transformInput() {
        val hasInvalidChars = asCharSequence().any { char ->
            !isValidNumericChar(char)
        }

        if (hasInvalidChars) revertAllChanges()
    }

    private fun isValidNumericChar(char: Char): Boolean {
        return char.isDigit() ||
            char == '.' ||
            char == ',' ||
            (allowNegative && char == '-')
    }

    override fun toString(): String = "InputTransformation.numericOnly(allowNegative=$allowNegative)"
}
