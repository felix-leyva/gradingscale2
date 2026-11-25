package de.felixlf.gradingscale2.features.calculator

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.text.TextRange
import de.felixlf.gradingscale2.uicomponents.syncSelectionWithFocus
import de.felixlf.gradingscale2.utils.applyEarnedPointsInput
import de.felixlf.gradingscale2.utils.applyPercentageInput
import de.felixlf.gradingscale2.utils.applyTotalPointsInput
import kotlin.test.Test
import kotlin.test.assertEquals

class CalculatorInputHandlersTest {

    @Test
    fun totalPointsHandlerAcceptsLocaleCommaKeepsSelectionHandlesBlankAndRevertsNonPositive() {
        val committed = mutableListOf<Double>()
        val state = TextFieldState("12,5")
        state.edit { selection = TextRange(2) }

        applyTotalPointsInput(state, "12,5", "10", committed::add)
        assertEquals(listOf(12.5), committed)
        assertEquals(TextRange(2), state.selection)

        val trailingDotState = TextFieldState("12.")
        trailingDotState.edit { selection = TextRange(3) }
        applyTotalPointsInput(trailingDotState, "12.", "10", committed::add)
        assertEquals(listOf(12.5, 12.0), committed)
        assertEquals(TextRange(3), trailingDotState.selection)

        val zeroDotState = TextFieldState("0.")
        applyTotalPointsInput(zeroDotState, "0.", "10", committed::add)
        assertEquals(listOf(12.5, 12.0), committed)
        assertEquals("10", zeroDotState.text.toString())

        val editingZeroDotState = TextFieldState("0.")
        applyTotalPointsInput(editingZeroDotState, "0.", "10", committed::add, isEditing = true)
        assertEquals(listOf(12.5, 12.0), committed)
        assertEquals("0.", editingZeroDotState.text.toString())

        // Blank while the user is editing must stay blank and must not commit a default.
        val editingBlankState = TextFieldState("")
        applyTotalPointsInput(editingBlankState, "", "10", committed::add, isEditing = true)
        assertEquals(listOf(12.5, 12.0), committed)
        assertEquals("", editingBlankState.text.toString())

        // Blank outside editing (focus loss) restores the last committed text.
        val emptyState = TextFieldState("")
        applyTotalPointsInput(emptyState, "", "10", committed::add)
        assertEquals(listOf(12.5, 12.0), committed)
        assertEquals("10", emptyState.text.toString())
        assertEquals(TextRange(2), emptyState.selection)

        val zeroState = TextFieldState("0")
        applyTotalPointsInput(zeroState, "0", "10", committed::add)
        assertEquals(listOf(12.5, 12.0), committed)
        assertEquals("10", zeroState.text.toString())
        assertEquals(TextRange(2), zeroState.selection)
    }

    @Test
    fun earnedPointsHandlerAcceptsValidValuesKeepsSelectionHandlesBlankAndRevertsOutOfRange() {
        val committed = mutableListOf<Double>()
        val validState = TextFieldState("85,5")
        validState.edit { selection = TextRange(2) }

        applyEarnedPointsInput(validState, "85,5", maxPoints = 100.0, fallbackText = "85", onSetPoints = committed::add)
        assertEquals(listOf(85.5), committed)
        assertEquals(TextRange(2), validState.selection)

        val trailingDotState = TextFieldState("85.")
        trailingDotState.edit { selection = TextRange(3) }
        applyEarnedPointsInput(trailingDotState, "85.", maxPoints = 100.0, fallbackText = "85", onSetPoints = committed::add)
        assertEquals(listOf(85.5, 85.0), committed)
        assertEquals(TextRange(3), trailingDotState.selection)

        val overMaxState = TextFieldState("150")
        applyEarnedPointsInput(overMaxState, "150", maxPoints = 100.0, fallbackText = "85", onSetPoints = committed::add)
        assertEquals(listOf(85.5, 85.0), committed)
        assertEquals("85", overMaxState.text.toString())
        assertEquals(TextRange(2), overMaxState.selection)

        val editingBlankState = TextFieldState("")
        applyEarnedPointsInput(
            editingBlankState,
            "",
            maxPoints = 100.0,
            fallbackText = "85",
            onSetPoints = committed::add,
            isEditing = true,
        )
        assertEquals(listOf(85.5, 85.0), committed)
        assertEquals("", editingBlankState.text.toString())

        val blankState = TextFieldState("")
        applyEarnedPointsInput(blankState, "", maxPoints = 100.0, fallbackText = "85", onSetPoints = committed::add)
        assertEquals(listOf(85.5, 85.0), committed)
        assertEquals("85", blankState.text.toString())
        assertEquals(TextRange(2), blankState.selection)
    }

    @Test
    fun percentageHandlerConvertsToUnitFractionKeepsSelectionHandlesBlankAndRevertsOutOfRange() {
        val committed = mutableListOf<Double>()
        val state = TextFieldState("85,5")
        state.edit { selection = TextRange(2) }

        applyPercentageInput(state, "85,5", fallbackText = "85", onSetPercentage = committed::add)
        assertEquals(1, committed.size)
        assertEquals(0.855, committed.single(), 1e-9)
        assertEquals(TextRange(2), state.selection)

        val trailingDotState = TextFieldState("85.")
        trailingDotState.edit { selection = TextRange(3) }
        applyPercentageInput(trailingDotState, "85.", fallbackText = "85", onSetPercentage = committed::add)
        assertEquals(2, committed.size)
        assertEquals(0.85, committed.last(), 1e-9)
        assertEquals(TextRange(3), trailingDotState.selection)

        val overHundredState = TextFieldState("120")
        applyPercentageInput(overHundredState, "120", fallbackText = "85", onSetPercentage = committed::add)
        assertEquals(2, committed.size)
        assertEquals("85", overHundredState.text.toString())
        assertEquals(TextRange(2), overHundredState.selection)

        val editingBlankState = TextFieldState("")
        applyPercentageInput(editingBlankState, "", fallbackText = "85", onSetPercentage = committed::add, isEditing = true)
        assertEquals(2, committed.size)
        assertEquals("", editingBlankState.text.toString())

        val blankState = TextFieldState("")
        applyPercentageInput(blankState, "", fallbackText = "85", onSetPercentage = committed::add)
        assertEquals(2, committed.size)
        assertEquals("85", blankState.text.toString())
        assertEquals(TextRange(2), blankState.selection)

        val dotOnlyState = TextFieldState(".")
        applyPercentageInput(dotOnlyState, ".", fallbackText = "85", onSetPercentage = committed::add)
        assertEquals(2, committed.size)
        assertEquals("85", dotOnlyState.text.toString())

        val editingDotOnlyState = TextFieldState(".")
        applyPercentageInput(
            editingDotOnlyState,
            ".",
            fallbackText = "85",
            onSetPercentage = committed::add,
            isEditing = true,
        )
        assertEquals(2, committed.size)
        assertEquals(".", editingDotOnlyState.text.toString())
    }

    @Test
    fun partialDecimalTokensRestorePreviousValueOnlyOnBlur() {
        val tokens = listOf(".", ",", "0.", "0,")
        for (token in tokens) {
            val totalCommitted = mutableListOf<Double>()
            val totalState = TextFieldState(token)
            applyTotalPointsInput(totalState, token, "10", totalCommitted::add)
            assertEquals("10", totalState.text.toString(), "total points '$token' must restore on blur")
            assertEquals(emptyList(), totalCommitted, "total points '$token' must not commit")

            val pointsCommitted = mutableListOf<Double>()
            val pointsState = TextFieldState(token)
            applyEarnedPointsInput(pointsState, token, maxPoints = 100.0, fallbackText = "85", onSetPoints = pointsCommitted::add)
            assertEquals("85", pointsState.text.toString(), "earned points '$token' must restore on blur")
            assertEquals(emptyList(), pointsCommitted, "earned points '$token' must not commit")

            val percentageCommitted = mutableListOf<Double>()
            val percentageState = TextFieldState(token)
            applyPercentageInput(percentageState, token, fallbackText = "85", onSetPercentage = percentageCommitted::add)
            assertEquals("85", percentageState.text.toString(), "percentage '$token' must restore on blur")
            assertEquals(emptyList(), percentageCommitted, "percentage '$token' must not commit")

            val editingState = TextFieldState(token)
            applyPercentageInput(editingState, token, fallbackText = "85", onSetPercentage = {}, isEditing = true)
            assertEquals(token, editingState.text.toString(), "'$token' must stay while editing")
        }
    }

    @Test
    fun handlersNeverSelectAllOnTheirOwnCommit() {
        val totalCommitted = mutableListOf<Double>()
        val validTotal = TextFieldState("20")
        validTotal.edit { selection = TextRange(1) }
        applyTotalPointsInput(validTotal, "20", "10", totalCommitted::add)
        assertEquals(listOf(20.0), totalCommitted)
        assertEquals(TextRange(1), validTotal.selection)

        val pointsCommitted = mutableListOf<Double>()
        val validPoints = TextFieldState("40")
        validPoints.edit { selection = TextRange(1) }
        applyEarnedPointsInput(validPoints, "40", maxPoints = 100.0, fallbackText = "25", onSetPoints = pointsCommitted::add)
        assertEquals(listOf(40.0), pointsCommitted)
        assertEquals(TextRange(1), validPoints.selection)

        val pctCommitted = mutableListOf<Double>()
        val validPct = TextFieldState("75")
        validPct.edit { selection = TextRange(1) }
        applyPercentageInput(validPct, "75", fallbackText = "50", onSetPercentage = pctCommitted::add)
        assertEquals(0.75, pctCommitted.single(), 1e-9)
        assertEquals(TextRange(1), validPct.selection)
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
