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
    fun selectAllOnSettleSelectsWholeFieldWhenRetypingSameValueAndSkipsTrailingDecimal() = runTest {
        var external by mutableStateOf("85")
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

            // Unfocused initial settle and external sync must NOT auto-select the field.
            advanceTimeBy(400L)
            runCurrent()
            assertEquals(TextRange(2), state.selection)

            external = "42"
            runCurrent()
            advanceTimeBy(400L)
            runCurrent()
            assertEquals("42", state.text.toString())
            assertEquals(TextRange(2), state.selection)

            // Focused field re-typing the same value selects all after debounce settles.
            focused = true
            external = "85"
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
            assertEquals(TextRange(0, 2), state.selection)

            state.edit {
                replace(0, length, "85.")
                selection = TextRange(3)
            }
            external = "85."
            runCurrent()

            advanceTimeBy(400L)
            runCurrent()
            assertEquals("85.", state.text.toString())
            assertEquals(TextRange(3), state.selection)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
