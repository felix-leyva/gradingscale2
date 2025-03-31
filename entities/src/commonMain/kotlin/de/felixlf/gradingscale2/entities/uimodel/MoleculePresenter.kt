package de.felixlf.gradingscale2.entities.uimodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlin.coroutines.EmptyCoroutineContext

/**
 * A Factory that produces a UI State of type [UIState] using a [androidx.compose.runtime.Composable] function and reacting to the [UICommand]s.
 *
 * The dependencies required to produce the UI State should be provided in the constructor of the implementing class.
 */
interface MoleculePresenter<UIState, UICommand> {
    /**
     * Produces the UI State of type [UIState] using a [androidx.compose.runtime.Composable] function.
     *
     * The UI State should be produced using the provided dependencies in the constructor of the implementing class.
     */
    @Composable
    fun produceUI(): UIState

    /**
     * Sends an command of type [UICommand] to the Factory.
     */
    fun sendCommand(command: UICommand)

    /**
     * Converts a [kotlinx.coroutines.flow.StateFlow] of type [T] to a state exposing the value of type [T] in a composable function and represents its latest value.
     * Every time there would be new value posted into the Flow the returned State will be updated causing recomposition of every value usage.
     */
    @Composable
    fun <T> StateFlow<T>.asState(): T = asState(value)

    /**
     * Converts a [kotlinx.coroutines.flow.Flow] of type [T] to a state exposing the value of type [T] in a composable function and represents its latest value.
     * Every time there would be new value posted into the Flow the returned State will be updated causing recomposition of every value usage.
     * @param initial the value of the state will have until the first flow value is emitted.
     */
    @Composable
    fun <T : R, R> Flow<T>.asState(initial: R): R = collectAsState(initial, EmptyCoroutineContext).value
}
