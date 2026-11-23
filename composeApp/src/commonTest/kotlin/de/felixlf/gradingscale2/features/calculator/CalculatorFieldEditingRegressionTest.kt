package de.felixlf.gradingscale2.features.calculator

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.TextRange
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import app.cash.turbine.test
import de.felixlf.gradingscale2.features.calculator.components.applyEarnedPointsInput
import de.felixlf.gradingscale2.features.calculator.components.applyPercentageInput
import de.felixlf.gradingscale2.features.calculator.components.applyTotalPointsInput
import de.felixlf.gradingscale2.utils.textFieldManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A5-03 / A6-09 regression coverage:
 * 1. Clearing a numeric field must never rewrite the field with a default value (1 / 0)
 *    while the user is editing it; the previously committed value is restored on blur.
 * 2. The debounced commit of the user's own edit must not select all text or otherwise
 *    touch the user's selection. Only a genuine external value change may do that.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CalculatorFieldEditingRegressionTest {

    private typealias Handler = (TextFieldState, String, String, Boolean, (Double) -> Unit) -> Unit

    @Test
    fun clearedTotalPointsStaysBlankWhileFocusedAndRestoresPreviousValueOnBlur() = runTest {
        blankFieldKeepsBlankWhileFocused(
            initialExternal = "10",
            handler = { state, raw, fallback, editing, commit ->
                applyTotalPointsInput(state, raw, fallback, commit, isEditing = editing)
            },
        )
    }

    @Test
    fun clearedEarnedPointsStaysBlankWhileFocusedAndRestoresPreviousValueOnBlur() = runTest {
        blankFieldKeepsBlankWhileFocused(
            initialExternal = "85",
            handler = { state, raw, fallback, editing, commit ->
                applyEarnedPointsInput(
                    state,
                    raw,
                    maxPoints = 100.0,
                    fallbackText = fallback,
                    onSetPoints = commit,
                    isEditing = editing,
                )
            },
        )
    }

    @Test
    fun clearedPercentageStaysBlankWhileFocusedAndRestoresPreviousValueOnBlur() = runTest {
        blankFieldKeepsBlankWhileFocused(
            initialExternal = "85",
            handler = { state, raw, fallback, editing, commit ->
                applyPercentageInput(state, raw, fallbackText = fallback, onSetPercentage = commit, isEditing = editing)
            },
        )
    }

    @Test
    fun committingOwnDebouncedEditDoesNotSelectAllOrMoveSelection() = runTest {
        val external = mutableStateOf("10")
        val focused = mutableStateOf(true)
        val committed = mutableListOf<Double>()

        val states = backgroundScope.launchMolecule(RecompositionMode.Immediate) {
            val currentExternal = external.value
            val currentFocused = focused.value
            textFieldManager(
                externalFieldValue = currentExternal,
                delay = 400L,
                isNumeric = true,
                selectAllOnSettle = true,
                isFocused = currentFocused,
            ) {
                applyTotalPointsInput(this, it, currentExternal, committed::add, isEditing = currentFocused)
            }
        }

        states.test {
            val state = awaitItem()
            runCurrent()
            state.edit {
                replace(0, length, "50")
                selection = TextRange(2)
            }
            runCurrent()

            advanceTimeBy(400L)
            runCurrent()

            assertEquals(listOf(50.0), committed)
            assertEquals("50", state.text.toString())
            // The user typed the value; the caret must stay where they left it.
            assertEquals(TextRange(2), state.selection)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun settlingOnSameTextAsExternalDoesNotSelectAllWhileFocused() = runTest {
        val external = mutableStateOf("85")
        val focused = mutableStateOf(true)

        val states = backgroundScope.launchMolecule(RecompositionMode.Immediate) {
            textFieldManager(
                externalFieldValue = external.value,
                delay = 400L,
                isNumeric = true,
                selectAllOnSettle = true,
                isFocused = focused.value,
                onChangeExternalFieldValue = {},
            )
        }

        states.test {
            val state = awaitItem()
            runCurrent()

            state.edit { replace(0, length, "8") }
            runCurrent()
            state.edit {
                append("5")
                selection = TextRange(2)
            }
            runCurrent()

            advanceTimeBy(400L)
            runCurrent()

            assertEquals("85", state.text.toString())
            assertEquals(TextRange(2), state.selection)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun genuineExternalValueChangeStillSyncsAndSelectsAllWhileFocused() = runTest {
        val external = mutableStateOf("85")
        val focused = mutableStateOf(true)

        val states = backgroundScope.launchMolecule(RecompositionMode.Immediate) {
            textFieldManager(
                externalFieldValue = external.value,
                delay = 400L,
                isNumeric = true,
                selectAllOnSettle = true,
                isFocused = focused.value,
                onChangeExternalFieldValue = {},
            )
        }

        states.test {
            val state = awaitItem()
            runCurrent()
            state.edit { selection = TextRange(1) }
            runCurrent()

            external.value = "70"
            runCurrent()

            assertEquals("70", state.text.toString())
            assertEquals(TextRange(0, 2), state.selection)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun focusedPercentageCommitEchoRoundedByDomainKeepsRawTextAndCaret() = runTest {
        val external = mutableStateOf("85")
        val focused = mutableStateOf(true)
        val committed = mutableListOf<Double>()

        val states = backgroundScope.launchMolecule(RecompositionMode.Immediate) {
            val currentExternal = external.value
            val currentFocused = focused.value
            textFieldManager(
                externalFieldValue = currentExternal,
                delay = 400L,
                isNumeric = true,
                selectAllOnSettle = true,
                isFocused = currentFocused,
            ) {
                applyPercentageInput(this, it, currentExternal, committed::add, isEditing = currentFocused)
            }
        }

        states.test {
            val state = awaitItem()
            runCurrent()

            state.edit {
                replace(0, length, "33.333")
                selection = TextRange(3)
            }
            runCurrent()

            advanceTimeBy(400L)
            runCurrent()
            assertEquals(1, committed.size)
            assertEquals(0.33333, committed.single(), 1e-9)

            // The domain keeps 0.33333 but its UI state formats the percentage to two decimals.
            external.value = "33.33"
            runCurrent()

            assertEquals("33.333", state.text.toString(), "an echo of our own commit must not rewrite the field")
            assertEquals(TextRange(3), state.selection, "an echo of our own commit must not move the caret")

            // The next keystroke must edit the existing text instead of starting from a wiped field.
            state.edit { append("5") }
            runCurrent()
            assertEquals("33.3335", state.text.toString())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun focusedOutOfRangeEarnedPointsKeepsRawTextUntilBlurThenReverts() = runTest {
        val external = mutableStateOf("85")
        val focused = mutableStateOf(true)
        val committed = mutableListOf<Double>()

        val states = backgroundScope.launchMolecule(RecompositionMode.Immediate) {
            val currentExternal = external.value
            val currentFocused = focused.value
            textFieldManager(
                externalFieldValue = currentExternal,
                delay = 400L,
                isNumeric = true,
                selectAllOnSettle = true,
                isFocused = currentFocused,
            ) {
                applyEarnedPointsInput(
                    textFieldState = this,
                    rawText = it,
                    maxPoints = 100.0,
                    fallbackText = currentExternal,
                    onSetPoints = committed::add,
                    isEditing = currentFocused,
                )
            }
        }

        states.test {
            val state = awaitItem()
            runCurrent()

            state.edit {
                replace(0, length, "150")
                selection = TextRange(3)
            }
            runCurrent()
            advanceTimeBy(400L)
            runCurrent()

            assertEquals("150", state.text.toString(), "out-of-range text must stay while focused")
            assertEquals(TextRange(3), state.selection)
            assertEquals(emptyList(), committed)

            focused.value = false
            runCurrent()

            assertEquals("85", state.text.toString(), "blur must restore the last valid value")
            assertEquals(emptyList(), committed)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun focusedOutOfRangePercentageKeepsRawTextUntilBlurThenReverts() = runTest {
        val external = mutableStateOf("85")
        val focused = mutableStateOf(true)
        val committed = mutableListOf<Double>()

        val states = backgroundScope.launchMolecule(RecompositionMode.Immediate) {
            val currentExternal = external.value
            val currentFocused = focused.value
            textFieldManager(
                externalFieldValue = currentExternal,
                delay = 400L,
                isNumeric = true,
                selectAllOnSettle = true,
                isFocused = currentFocused,
            ) {
                applyPercentageInput(this, it, currentExternal, committed::add, isEditing = currentFocused)
            }
        }

        states.test {
            val state = awaitItem()
            runCurrent()

            state.edit {
                replace(0, length, "120")
                selection = TextRange(3)
            }
            runCurrent()
            advanceTimeBy(400L)
            runCurrent()

            assertEquals("120", state.text.toString(), "out-of-range text must stay while focused")
            assertEquals(TextRange(3), state.selection)
            assertEquals(emptyList(), committed)

            focused.value = false
            runCurrent()

            assertEquals("85", state.text.toString(), "blur must restore the last valid value")
            assertEquals(emptyList(), committed)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun blurCommitIsNotRepeatedByPendingDebounce() = runTest {
        val external = mutableStateOf("10")
        val focused = mutableStateOf(true)
        val committed = mutableListOf<Double>()

        val states = backgroundScope.launchMolecule(RecompositionMode.Immediate) {
            textFieldManager(
                externalFieldValue = external.value,
                delay = 400L,
                isNumeric = true,
                selectAllOnSettle = true,
                isFocused = focused.value,
            ) {
                applyTotalPointsInput(this, it, external.value, committed::add, isEditing = focused.value)
            }
        }

        states.test {
            val state = awaitItem()
            runCurrent()
            state.edit {
                replace(0, length, "50")
                selection = TextRange(2)
            }
            runCurrent()

            focused.value = false
            runCurrent()
            assertEquals(listOf(50.0), committed)

            // The pending debounce for the same text must not commit the same value a second time.
            advanceTimeBy(400L)
            runCurrent()
            assertEquals(listOf(50.0), committed)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun blurCommitBeforeDomainEchoIsNotRepeatedByPendingDebounce() = runTest {
        val external = mutableStateOf("85")
        val focused = mutableStateOf(true)
        val committed = mutableListOf<Double>()

        val states = backgroundScope.launchMolecule(RecompositionMode.Immediate) {
            val currentFocused = focused.value
            textFieldManager(
                externalFieldValue = external.value,
                delay = 400L,
                isNumeric = true,
                selectAllOnSettle = true,
                isFocused = currentFocused,
            ) {
                applyPercentageInput(this, it, external.value, committed::add, isEditing = currentFocused)
            }
        }

        states.test {
            val state = awaitItem()
            runCurrent()
            state.edit {
                replace(0, length, "33.333")
                selection = TextRange(6)
            }
            runCurrent()

            focused.value = false
            runCurrent()
            assertEquals(1, committed.size)

            // Domain responds after the blur commit with its rounded representation.
            external.value = "33.33"
            runCurrent()
            advanceTimeBy(400L)
            runCurrent()

            assertEquals(1, committed.size, "the pending debounce must not commit the same edit again")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun focusedPercentageEchoThatLosesItsLastBitKeepsRawTextAndCaret() = runTest {
        // 0.115% -> fraction 0.00115 -> domain re-renders as *100 = 0.11499999999999999 -> "0.11"
        // while the typed text canonicalizes to "0.12": the mismatch must still be an echo.
        assertPercentageEchoKeepsRawText(typedText = "0.115", echoedExternal = "0.11")
    }

    @Test
    fun focusedPercentageEchoAtTheOtherHalfUnitBoundaryKeepsRawTextAndCaret() = runTest {
        // 7.555% -> 0.07555 -> *100 = 7.554999999999999 -> "7.55" (typed canonical is "7.56").
        assertPercentageEchoKeepsRawText(typedText = "7.555", echoedExternal = "7.55")
    }

    @Test
    fun focusedPercentageOwnCommitDoesNotSwallowNearestGenuineTwoDecimalIncrement() = runTest {
        val external = mutableStateOf("85")
        val focused = mutableStateOf(true)
        val committed = mutableListOf<Double>()

        val states = backgroundScope.launchMolecule(RecompositionMode.Immediate) {
            val currentExternal = external.value
            val currentFocused = focused.value
            textFieldManager(
                externalFieldValue = currentExternal,
                delay = 400L,
                isNumeric = true,
                selectAllOnSettle = true,
                isFocused = currentFocused,
            ) {
                applyPercentageInput(this, it, currentExternal, committed::add, isEditing = currentFocused)
            }
        }

        states.test {
            val state = awaitItem()
            runCurrent()

            state.edit {
                replace(0, length, "85.50")
                selection = TextRange(2)
            }
            runCurrent()
            advanceTimeBy(400L)
            runCurrent()
            assertEquals(listOf(0.855), committed, "the typed percentage must commit once as a fraction")

            // The domain echoes the own commit unchanged: still our own edit, no rewrite.
            external.value = "85.50"
            runCurrent()
            assertEquals("85.50", state.text.toString())
            assertEquals(TextRange(2), state.selection)

            // One whole 2-dp step away is a genuine external change: it must rewrite and select all.
            external.value = "85.51"
            runCurrent()
            assertEquals("85.51", state.text.toString(), "a genuine 2-dp external increment must not be swallowed")
            assertEquals(TextRange(0, 5), state.selection)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private suspend fun TestScope.assertPercentageEchoKeepsRawText(typedText: String, echoedExternal: String) {
        val external = mutableStateOf("85")
        val focused = mutableStateOf(true)
        val committed = mutableListOf<Double>()

        val states = backgroundScope.launchMolecule(RecompositionMode.Immediate) {
            val currentExternal = external.value
            val currentFocused = focused.value
            textFieldManager(
                externalFieldValue = currentExternal,
                delay = 400L,
                isNumeric = true,
                selectAllOnSettle = true,
                isFocused = currentFocused,
            ) {
                applyPercentageInput(this, it, currentExternal, committed::add, isEditing = currentFocused)
            }
        }

        states.test {
            val state = awaitItem()
            runCurrent()

            state.edit {
                replace(0, length, typedText)
                selection = TextRange(1)
            }
            runCurrent()
            advanceTimeBy(400L)
            runCurrent()
            assertEquals(listOf(typedText.toDouble() / 100.0), committed)

            external.value = echoedExternal
            runCurrent()

            assertEquals(typedText, state.text.toString(), "an echo of our own commit must not rewrite the field")
            assertEquals(TextRange(1), state.selection, "an echo of our own commit must not move the caret")

            // The field is still editable: append at the end instead of starting from a wiped value.
            state.edit { selection = TextRange(state.text.length) }
            state.edit { append("5") }
            runCurrent()
            assertEquals(typedText + "5", state.text.toString())
            cancelAndIgnoreRemainingEvents()
        }
    }

    private suspend fun TestScope.blankFieldKeepsBlankWhileFocused(
        initialExternal: String,
        handler: Handler,
    ) {
        val external = mutableStateOf(initialExternal)
        val focused = mutableStateOf(true)
        val committed = mutableListOf<Double>()

        val states = backgroundScope.launchMolecule(RecompositionMode.Immediate) {
            val currentExternal = external.value
            val currentFocused = focused.value
            textFieldManager(
                externalFieldValue = currentExternal,
                delay = 400L,
                isNumeric = true,
                selectAllOnSettle = true,
                isFocused = currentFocused,
            ) {
                handler(this, it, currentExternal, currentFocused, committed::add)
            }
        }

        states.test {
            val state = awaitItem()
            runCurrent()

            // User clears the field (select-all + delete / backspace).
            state.edit { replace(0, length, "") }
            runCurrent()

            advanceTimeBy(400L)
            runCurrent()

            assertEquals("", state.text.toString(), "blank field must stay blank after the 400ms debounce")
            assertEquals(emptyList(), committed, "clearing a field must not commit a default number")

            // Losing focus restores the last committed domain value; still no default commit.
            focused.value = false
            runCurrent()

            assertEquals(initialExternal, state.text.toString())
            assertEquals(emptyList(), committed)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
