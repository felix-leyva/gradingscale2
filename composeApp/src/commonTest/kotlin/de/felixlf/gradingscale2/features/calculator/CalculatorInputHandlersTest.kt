package de.felixlf.gradingscale2.features.calculator

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.text.TextRange
import de.felixlf.gradingscale2.features.calculator.components.applyEarnedPointsInput
import de.felixlf.gradingscale2.features.calculator.components.applyPercentageInput
import de.felixlf.gradingscale2.features.calculator.components.applyTotalPointsInput
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CalculatorInputHandlersTest {

    @Test
    fun totalPointsHandlerAcceptsLocaleCommaSelectsAllHandlesBlankAndRevertsNonPositive() {
        val committed = mutableListOf<Double>()
        val state = TextFieldState("12,5")
        state.edit { selection = TextRange(2) }

        applyTotalPointsInput(state, "12,5", "10", committed::add)
        assertEquals(listOf(12.5), committed)
        assertEquals(TextRange(0, 4), state.selection)

        val trailingDotState = TextFieldState("12.")
        trailingDotState.edit { selection = TextRange(3) }
        applyTotalPointsInput(trailingDotState, "12.", "10", committed::add)
        assertEquals(listOf(12.5, 12.0), committed)
        assertEquals(TextRange(3), trailingDotState.selection)

        val zeroDotState = TextFieldState("0.")
        applyTotalPointsInput(zeroDotState, "0.", "10", committed::add)
        assertEquals(listOf(12.5, 12.0), committed)
        assertEquals("0.", zeroDotState.text.toString())

        val emptyState = TextFieldState("")
        applyTotalPointsInput(emptyState, "", "10", committed::add)
        assertEquals(listOf(12.5, 12.0, 1.0), committed)
        assertEquals("1", emptyState.text.toString())
        assertEquals(TextRange(0, 1), emptyState.selection)

        val zeroState = TextFieldState("0")
        applyTotalPointsInput(zeroState, "0", "10", committed::add)
        assertEquals(listOf(12.5, 12.0, 1.0), committed)
        assertEquals("10", zeroState.text.toString())
        assertEquals(TextRange(0, 2), zeroState.selection)
    }

    @Test
    fun earnedPointsHandlerAcceptsValidValuesSelectsAllHandlesBlankAndRevertsOutOfRange() {
        val committed = mutableListOf<Double>()
        val validState = TextFieldState("85,5")
        validState.edit { selection = TextRange(2) }

        applyEarnedPointsInput(validState, "85,5", maxPoints = 100.0, fallbackText = "85", committed::add)
        assertEquals(listOf(85.5), committed)
        assertEquals(TextRange(0, 4), validState.selection)

        val trailingDotState = TextFieldState("85.")
        trailingDotState.edit { selection = TextRange(3) }
        applyEarnedPointsInput(trailingDotState, "85.", maxPoints = 100.0, fallbackText = "85", committed::add)
        assertEquals(listOf(85.5, 85.0), committed)
        assertEquals(TextRange(3), trailingDotState.selection)

        val overMaxState = TextFieldState("150")
        applyEarnedPointsInput(overMaxState, "150", maxPoints = 100.0, fallbackText = "85", committed::add)
        assertEquals(listOf(85.5, 85.0), committed)
        assertEquals("85", overMaxState.text.toString())
        assertEquals(TextRange(0, 2), overMaxState.selection)

        val blankState = TextFieldState("")
        applyEarnedPointsInput(blankState, "", maxPoints = 100.0, fallbackText = "85", committed::add)
        assertEquals(listOf(85.5, 85.0, 0.0), committed)
        assertEquals("0", blankState.text.toString())
        assertEquals(TextRange(0, 1), blankState.selection)
    }

    @Test
    fun percentageHandlerConvertsToUnitFractionSelectsAllHandlesBlankAndRevertsOutOfRange() {
        val committed = mutableListOf<Double>()
        val state = TextFieldState("85,5")
        state.edit { selection = TextRange(2) }

        applyPercentageInput(state, "85,5", fallbackText = "85", committed::add)
        assertEquals(1, committed.size)
        assertEquals(0.855, committed.single(), 1e-9)
        assertEquals(TextRange(0, 4), state.selection)

        val trailingDotState = TextFieldState("85.")
        trailingDotState.edit { selection = TextRange(3) }
        applyPercentageInput(trailingDotState, "85.", fallbackText = "85", committed::add)
        assertEquals(2, committed.size)
        assertEquals(0.85, committed.last(), 1e-9)
        assertEquals(TextRange(3), trailingDotState.selection)

        val overHundredState = TextFieldState("120")
        applyPercentageInput(overHundredState, "120", fallbackText = "85", committed::add)
        assertEquals(2, committed.size)
        assertEquals("85", overHundredState.text.toString())
        assertEquals(TextRange(0, 2), overHundredState.selection)

        val blankState = TextFieldState("")
        applyPercentageInput(blankState, "", fallbackText = "85", committed::add)
        assertEquals(3, committed.size)
        assertEquals(0.0, committed.last(), 1e-9)
        assertEquals("0", blankState.text.toString())
        assertEquals(TextRange(0, 1), blankState.selection)

        val dotOnlyState = TextFieldState(".")
        applyPercentageInput(dotOnlyState, ".", fallbackText = "85", committed::add)
        assertTrue(committed.size == 3)
        assertEquals(".", dotOnlyState.text.toString())
    }

    @Test
    fun handlersDoNotSelectAllWhenUnfocused() {
        val totalCommitted = mutableListOf<Double>()
        val validTotal = TextFieldState("20")
        applyTotalPointsInput(validTotal, "20", "10", totalCommitted::add, selectAllOnCommit = false)
        assertEquals(listOf(20.0), totalCommitted)
        assertTrue(validTotal.selection.collapsed)

        val blankTotal = TextFieldState("")
        applyTotalPointsInput(blankTotal, "", "10", totalCommitted::add, selectAllOnCommit = false)
        assertEquals(listOf(20.0, 1.0), totalCommitted)
        assertEquals("1", blankTotal.text.toString())
        assertEquals(TextRange(1), blankTotal.selection)

        val zeroTotal = TextFieldState("0")
        applyTotalPointsInput(zeroTotal, "0", "10", totalCommitted::add, selectAllOnCommit = false)
        assertEquals("10", zeroTotal.text.toString())
        assertEquals(TextRange(2), zeroTotal.selection)

        val pointsCommitted = mutableListOf<Double>()
        val validPoints = TextFieldState("40")
        applyEarnedPointsInput(validPoints, "40", maxPoints = 100.0, fallbackText = "25", pointsCommitted::add, selectAllOnCommit = false)
        assertEquals(listOf(40.0), pointsCommitted)
        assertTrue(validPoints.selection.collapsed)

        val blankPoints = TextFieldState("")
        applyEarnedPointsInput(blankPoints, "", maxPoints = 100.0, fallbackText = "25", pointsCommitted::add, selectAllOnCommit = false)
        assertEquals(listOf(40.0, 0.0), pointsCommitted)
        assertEquals("0", blankPoints.text.toString())
        assertEquals(TextRange(1), blankPoints.selection)

        val overPoints = TextFieldState("150")
        applyEarnedPointsInput(overPoints, "150", maxPoints = 100.0, fallbackText = "25", pointsCommitted::add, selectAllOnCommit = false)
        assertEquals("25", overPoints.text.toString())
        assertEquals(TextRange(2), overPoints.selection)

        val pctCommitted = mutableListOf<Double>()
        val validPct = TextFieldState("75")
        applyPercentageInput(validPct, "75", fallbackText = "50", pctCommitted::add, selectAllOnCommit = false)
        assertEquals(0.75, pctCommitted.single(), 1e-9)
        assertTrue(validPct.selection.collapsed)

        val blankPct = TextFieldState("")
        applyPercentageInput(blankPct, "", fallbackText = "50", pctCommitted::add, selectAllOnCommit = false)
        assertEquals(0.0, pctCommitted.last(), 1e-9)
        assertEquals("0", blankPct.text.toString())
        assertEquals(TextRange(1), blankPct.selection)

        val overPct = TextFieldState("120")
        applyPercentageInput(overPct, "120", fallbackText = "50", pctCommitted::add, selectAllOnCommit = false)
        assertEquals("50", overPct.text.toString())
        assertEquals(TextRange(2), overPct.selection)
    }

    @Test
    fun syncSelectionWithFocusSelectsOnFocusAndCollapsesOnBlur() {
        val state = TextFieldState("85")
        assertEquals(TextRange(2), state.selection)

        syncSelectionWithFocus(state, isFocused = true, selectAllOnFocus = true)
        assertEquals(TextRange(0, 2), state.selection)

        syncSelectionWithFocus(state, isFocused = false, selectAllOnFocus = true)
        assertEquals(TextRange(2), state.selection)

        syncSelectionWithFocus(state, isFocused = true, selectAllOnFocus = false)
        assertEquals(TextRange(2), state.selection)
    }
}
