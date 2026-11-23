package de.felixlf.gradingscale2.utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TextFieldManagerTest {

    @Test
    fun sameExternalValueDoesNotResetCursorSelectionToEnd() = runTest {
        var external by mutableStateOf("100")

        val states = backgroundScope.launchMolecule(RecompositionMode.Immediate) {
            val currentExternal = external
            textFieldManager(
                externalFieldValue = currentExternal,
                delay = 400L,
                onChangeExternalFieldValue = {},
            )
        }

        states.test {
            val state = awaitItem()
            runCurrent()
            state.edit {
                replace(0, length, "1200")
                selection = TextRange(2)
            }
            assertEquals(TextRange(2), state.selection)

            external = "1200"
            runCurrent()

            assertEquals("1200", state.text.toString())
            assertEquals(TextRange(2), state.selection)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun changingCallbackLambdaOnRecompositionDoesNotResetDebounceTimer() = runTest {
        var external by mutableStateOf("100")
        var version by mutableStateOf(1)
        val recorded = mutableListOf<Pair<String, Int>>()

        val states = backgroundScope.launchMolecule(RecompositionMode.Immediate) {
            val currentExternal = external
            val currentVersion = version
            textFieldManager(
                externalFieldValue = currentExternal,
                delay = 400L,
                onChangeExternalFieldValue = { text ->
                    recorded.add(text to currentVersion)
                },
            )
        }

        states.test {
            val state = awaitItem()
            runCurrent()
            state.edit { replace(0, length, "50") }
            runCurrent()

            advanceTimeBy(250L)
            runCurrent()
            assertEquals(emptyList(), recorded)

            // Recompose with a new callback instance capturing version = 2.
            version = 2
            runCurrent()

            // Only 200ms after recomposition (450ms total since typing) -> timer must have fired once with version = 2.
            advanceTimeBy(200L)
            runCurrent()
            assertEquals(listOf("50" to 2), recorded)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun zeroDelayInvokesCallbackImmediatelyWithoutWaiting400ms() = runTest {
        var external by mutableStateOf("90")
        val committed = mutableListOf<String>()

        val states = backgroundScope.launchMolecule(RecompositionMode.Immediate) {
            val currentExternal = external
            textFieldManager(
                externalFieldValue = currentExternal,
                delay = 0L,
                onChangeExternalFieldValue = { committed.add(it) },
            )
        }

        states.test {
            val state = awaitItem()
            state.edit { replace(0, length, "95") }
            runCurrent()

            assertEquals(0L, currentTime)
            assertEquals(listOf("95"), committed)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun numericModePreservesTrailingDecimalSeparatorWhenExternalEchoesSameNumber() = runTest {
        var external by mutableStateOf("80")

        val states = backgroundScope.launchMolecule(RecompositionMode.Immediate) {
            val currentExternal = external
            textFieldManager(
                externalFieldValue = currentExternal,
                delay = 400L,
                isNumeric = true,
                onChangeExternalFieldValue = { committed ->
                    if (committed == "85.") {
                        external = "85"
                    }
                },
            )
        }

        states.test {
            val state = awaitItem()
            runCurrent()
            state.edit { replace(0, length, "85.") }
            runCurrent()

            // Trigger debounce -> external updates to formatted "85"
            advanceTimeBy(400L)
            runCurrent()
            assertEquals("85", external)
            assertEquals("85.", state.text.toString())

            // Simulate user continuing to "85.0" -> external still "85", text must remain "85.0"
            state.edit { append("0") }
            runCurrent()
            advanceTimeBy(400L)
            runCurrent()
            assertEquals("85.0", state.text.toString())

            // Genuine external change (e.g. stepper / grade dropdown) syncs normally
            external = "70"
            runCurrent()
            assertEquals("70", state.text.toString())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun ownEditsNeverSelectAllWhileGenuineExternalChangesSelectAllWhenFocused() = runTest {
        var external by mutableStateOf("100")
        var focused by mutableStateOf(false)

        val states = backgroundScope.launchMolecule(RecompositionMode.Immediate) {
            val currentExternal = external
            val currentFocused = focused
            textFieldManager(
                externalFieldValue = currentExternal,
                delay = 400L,
                isNumeric = true,
                selectAllOnSettle = true,
                isFocused = currentFocused,
                onChangeExternalFieldValue = {},
            )
        }

        states.test {
            val state = awaitItem()
            runCurrent()

            // Unfocused initial settle must not touch the selection.
            advanceTimeBy(400L)
            runCurrent()
            assertEquals(TextRange(3), state.selection)

            // Focused user retyping a value must not be selected after the debounce settles.
            focused = true
            runCurrent()
            state.edit { replace(0, length, "42") }
            runCurrent()
            advanceTimeBy(400L)
            runCurrent()
            assertEquals("42", state.text.toString())
            assertEquals(TextRange(2), state.selection)

            // Retyping exactly the external value is still the user's own edit: no select-all.
            state.edit { replace(0, length, "100") }
            runCurrent()
            advanceTimeBy(400L)
            runCurrent()
            assertEquals(TextRange(3), state.selection)

            // A genuine external change (stepper / grade change) syncs and selects all.
            external = "85"
            runCurrent()
            assertEquals("85", state.text.toString())
            assertEquals(TextRange(0, 2), state.selection)

            // The settle debounce must not re-select or otherwise move the selection.
            state.edit { selection = TextRange(2) }
            runCurrent()
            advanceTimeBy(400L)
            runCurrent()
            assertEquals(TextRange(2), state.selection)

            // Unfocused external change syncs without selecting all.
            focused = false
            external = "70"
            runCurrent()
            assertEquals("70", state.text.toString())
            assertEquals(TextRange(2), state.selection)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun twoDecimalFormatterEchoClassificationBoundaries() {
        // Own commit 33.333 is rendered by the app's 2-dp formatter as 33.33: that is an echo.
        assertTrue(isEchoOfOwnCommit("33.333", "33.333", "33.33"))
        // Trailing-zero variants of the committed value are still the same echo.
        assertTrue(isEchoOfOwnCommit("85.5", "85.5", "85.50"))
        // Half a unit away is not a rendering of 85.5 and must not be suppressed.
        assertFalse(isEchoOfOwnCommit("85.5", "85.5", "86"))
        // A different numeric text than the committed one is never an echo.
        assertFalse(isEchoOfOwnCommit("85.6", "85.5", "85.5"))
        // Echo suppression is limited to focused own edits; after blur external wins.
        assertTrue(shouldSyncExternalText("85.5", "86", isNumeric = true, ownCommittedText = "85.5", isFocused = false))
        assertFalse(
            shouldSyncExternalText("33.333", "33.33", isNumeric = true, ownCommittedText = "33.333", isFocused = true),
        )
        assertTrue(
            shouldSyncExternalText("33.333", "33.33", isNumeric = true, ownCommittedText = "33.333", isFocused = false),
        )
    }

    @Test
    fun echoToleranceIsTheHalfUnitOfTheTwoDecimalFormatter() {
        // (typed, external echo, expected echo)
        val cases = listOf(
            // Derived doubles lose their last bit: 0.115 -> 0.00115 -> *100 -> 0.11499999999999999,
            // which the 2-dp formatter renders as "0.11", while "0.115" itself canonicalizes to
            // "0.12". Both are echoes of the same own edit.
            Triple("0.115", "0.11", true),
            Triple("0.115", "0.12", true),
            // Same last-bit loss at the other half-unit boundary.
            Triple("7.555", "7.55", true),
            Triple("7.555", "7.56", true),
            // Nearest genuine 2-dp external increments are one whole step away: never echoes.
            Triple("85.50", "85.51", false),
            Triple("85.50", "85.49", false),
            Triple("0.115", "0.13", false),
            Triple("7.555", "7.57", false),
        )
        for ((typed, external, expectedEcho) in cases) {
            assertEquals(
                expectedEcho,
                isEchoOfOwnCommit(typed, typed, external),
                "isEchoOfOwnCommit(typed=$typed, external=$external)",
            )
            assertEquals(
                !expectedEcho,
                shouldSyncExternalText(typed, external, isNumeric = true, ownCommittedText = typed, isFocused = true),
                "shouldSyncExternalText(typed=$typed, external=$external)",
            )
        }
    }

    @Test
    fun ownCommitOf85Point5DoesNotSwallowGenuineExternal86() = runTest {
        var external by mutableStateOf("85")
        var focused by mutableStateOf(true)

        val states = backgroundScope.launchMolecule(RecompositionMode.Immediate) {
            val currentExternal = external
            val currentFocused = focused
            textFieldManager(
                externalFieldValue = currentExternal,
                delay = 400L,
                isNumeric = true,
                selectAllOnSettle = true,
                isFocused = currentFocused,
                onChangeExternalFieldValue = {},
            )
        }

        states.test {
            val state = awaitItem()
            runCurrent()
            state.edit { replace(0, length, "85.5") }
            runCurrent()
            advanceTimeBy(400L)
            runCurrent()

            // Own commit is echoed back by the domain using the canonical 2-dp formatter.
            external = "85.5"
            runCurrent()
            assertEquals("85.5", state.text.toString())

            // A genuine external change to the adjacent integer must win even though the
            // numeric distance equals the half-unit of the old digit-derived tolerance.
            external = "86"
            runCurrent()
            assertEquals("86", state.text.toString(), "external 86 must not be treated as an echo of own 85.5")
            assertEquals(TextRange(0, 2), state.selection, "genuine external sync must select all while focused")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun ownCommitAtTwoDecimalBoundaryDoesNotSwallowNearestGenuineIncrement() = runTest {
        var external by mutableStateOf("85")
        var focused by mutableStateOf(true)

        val states = backgroundScope.launchMolecule(RecompositionMode.Immediate) {
            val currentExternal = external
            val currentFocused = focused
            textFieldManager(
                externalFieldValue = currentExternal,
                delay = 400L,
                isNumeric = true,
                selectAllOnSettle = true,
                isFocused = currentFocused,
                onChangeExternalFieldValue = {},
            )
        }

        states.test {
            val state = awaitItem()
            runCurrent()
            state.edit { replace(0, length, "85.50") }
            runCurrent()
            state.edit { selection = TextRange(2) }
            runCurrent()
            advanceTimeBy(400L)
            runCurrent()

            // The domain echoes the own commit unchanged: no rewrite, no re-selection.
            external = "85.50"
            runCurrent()
            assertEquals("85.50", state.text.toString())
            assertEquals(TextRange(2), state.selection)

            // The nearest genuine 2-dp external increment is exactly one step away and must win.
            external = "85.51"
            runCurrent()
            assertEquals("85.51", state.text.toString(), "external 85.51 must not be treated as an echo of own 85.50")
            assertEquals(TextRange(0, 5), state.selection, "genuine external sync must select all while focused")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun externalChangeAfterBlurIsAuthoritativeEvenWhenNumericallyClose() = runTest {
        var external by mutableStateOf("85")
        var focused by mutableStateOf(true)

        val states = backgroundScope.launchMolecule(RecompositionMode.Immediate) {
            val currentExternal = external
            val currentFocused = focused
            textFieldManager(
                externalFieldValue = currentExternal,
                delay = 400L,
                isNumeric = true,
                selectAllOnSettle = true,
                isFocused = currentFocused,
                onChangeExternalFieldValue = {},
            )
        }

        states.test {
            val state = awaitItem()
            runCurrent()
            state.edit { replace(0, length, "85.5") }
            runCurrent()
            advanceTimeBy(400L)
            runCurrent()

            focused = false
            runCurrent()

            // After blur no own-edit echo may be suppressed: the external value is authoritative.
            external = "86"
            runCurrent()
            assertEquals("86", state.text.toString(), "external change after blur must rewrite the field")
            assertEquals(TextRange(2), state.selection)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun localeFormattedExternalChangeCloseToOwnCommitStillSyncs() = runTest {
        var external by mutableStateOf("85")
        var focused by mutableStateOf(true)

        val states = backgroundScope.launchMolecule(RecompositionMode.Immediate) {
            val currentExternal = external
            val currentFocused = focused
            textFieldManager(
                externalFieldValue = currentExternal,
                delay = 400L,
                isNumeric = true,
                selectAllOnSettle = true,
                isFocused = currentFocused,
                onChangeExternalFieldValue = {},
            )
        }

        states.test {
            val state = awaitItem()
            runCurrent()
            state.edit { replace(0, length, "85.5") }
            runCurrent()
            advanceTimeBy(400L)
            runCurrent()

            external = "86,0"
            runCurrent()
            assertEquals("86,0", state.text.toString(), "a locale-formatted genuine external value must sync")
            assertEquals(TextRange(0, 4), state.selection)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
