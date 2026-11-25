package de.felixlf.gradingscale2.utils

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.ui.text.input.KeyboardType

/**
 * InputTransformation that filters numeric input to allow only digits, a single decimal separator (. normalized from ,),
 * and an optional leading minus sign.
 */
data class NumericInputFilter(private val allowNegative: Boolean = false) : InputTransformation {
    override val keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)

    override fun TextFieldBuffer.transformInput() {
        val current = asCharSequence()
        var decimalSeparatorCount = 0
        for (i in 0 until current.length) {
            val char = current[i]
            val isValid = when {
                char.isDigit() -> true
                char == '.' || char == ',' -> {
                    decimalSeparatorCount++
                    decimalSeparatorCount <= 1
                }
                allowNegative && char == '-' -> i == 0
                else -> false
            }
            if (!isValid) {
                revertAllChanges()
                return
            }
        }
        for (i in 0 until length) {
            if (asCharSequence()[i] == ',') {
                replace(i, i + 1, ".")
            }
        }
    }

    override fun toString(): String = "InputTransformation.numericOnly(allowNegative=$allowNegative)"
}
