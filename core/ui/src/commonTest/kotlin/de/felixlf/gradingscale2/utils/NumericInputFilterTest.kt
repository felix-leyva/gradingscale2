package de.felixlf.gradingscale2.utils

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.text.input.KeyboardType
import de.felixlf.gradingscale2.uicomponents.shouldApplyNumericFilter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NumericInputFilterTest {

    @Test
    fun keyboardOptionsUsesKeyboardTypeDecimal() {
        assertEquals(KeyboardType.Decimal, NumericInputFilter().keyboardOptions.keyboardType)
    }

    @Test
    fun normalizesDecimalCommaToDot() {
        val state = TextFieldState("85")
        state.edit {
            append(",5")
            with(NumericInputFilter()) { transformInput() }
        }
        assertEquals("85.5", state.text.toString())
    }

    @Test
    fun rejectsSecondDecimalSeparator() {
        val filter = NumericInputFilter()
        val state = TextFieldState("85.5")

        state.edit {
            append(".")
            with(filter) { transformInput() }
        }
        assertEquals("85.5", state.text.toString())

        state.edit {
            append(",")
            with(filter) { transformInput() }
        }
        assertEquals("85.5", state.text.toString())
    }

    @Test
    fun allowsMinusOnlyAtIndexZeroWhenAllowNegativeIsTrue() {
        val allowNegativeFilter = NumericInputFilter(allowNegative = true)
        val negativeState = TextFieldState("")
        negativeState.edit {
            append("-85.5")
            with(allowNegativeFilter) { transformInput() }
        }
        assertEquals("-85.5", negativeState.text.toString())

        val middleMinusState = TextFieldState("85")
        middleMinusState.edit {
            append("-5")
            with(allowNegativeFilter) { transformInput() }
        }
        assertEquals("85", middleMinusState.text.toString())

        val doubleMinusState = TextFieldState("-")
        doubleMinusState.edit {
            append("-5")
            with(allowNegativeFilter) { transformInput() }
        }
        assertEquals("-", doubleMinusState.text.toString())

        val disallowNegativeFilter = NumericInputFilter(allowNegative = false)
        val blockedMinusState = TextFieldState("")
        blockedMinusState.edit {
            append("-5")
            with(disallowNegativeFilter) { transformInput() }
        }
        assertEquals("", blockedMinusState.text.toString())
    }

    @Test
    fun rejectsAlphabeticCharacters() {
        val state = TextFieldState("85")
        state.edit {
            append("a")
            with(NumericInputFilter()) { transformInput() }
        }
        assertEquals("85", state.text.toString())
    }

    @Test
    fun shouldApplyNumericFilterGatesOnEditableDecimalOrNumberKeyboards() {
        assertTrue(shouldApplyNumericFilter(readOnly = false, keyboardType = KeyboardType.Decimal))
        assertTrue(shouldApplyNumericFilter(readOnly = false, keyboardType = KeyboardType.Number))
        assertFalse(shouldApplyNumericFilter(readOnly = false, keyboardType = KeyboardType.Text))
        assertFalse(shouldApplyNumericFilter(readOnly = true, keyboardType = KeyboardType.Decimal))
        assertFalse(shouldApplyNumericFilter(readOnly = true, keyboardType = KeyboardType.Number))
    }
}
