package de.felixlf.gradingscale2.entities.uimodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlin.coroutines.EmptyCoroutineContext

/**
 * A Factory that produces a UI State of type [UIState] using a [androidx.compose.runtime.Composable] function and reacting to the [UICommand]s.
 *
 * The dependencies required to produce the UI State should be provided in the constructor of the implementing class.
 */
interface UIModel<UIState, UICommand> {
    /**
     * The coroutine scope of the UI Model. This is used to launch coroutines and manage the lifecycle of the UI Model.
     * Use the function [de.felixlf.gradingscale2.entities.util.DispatcherProvider.newUIScope] to create the scope with the proper dispatcher and job.
     * When instantiating a ViewModel, insert the scope in the viewModel constructor, to link the lifecycle of the ViewModel with the lifecycle of the
     * UI Model.
     */
    val scope: UIModelScope

    /**
     * The UI [kotlinx.coroutines.flow.StateFlow] which represents the UI State and is consumed by the UI.
     * Use the [moleculeUIState] function to create the StateFlow.
     * ```
     * override val uiState by moleculeState()
     * ```
     */
    val uiState: StateFlow<UIState>

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
     * Launches a Molecule with the [RecompositionMode.ContextClock] mode. This is used to create the UI state and dispatch UI events.
     * It returns a [StateFlow] lazily, to avoid that null pointer exceptions occur, due other vals not being initialized before.
     */
    fun moleculeUIState() = lazy {
        scope.launchMolecule(getRecompositionMode()) {
            produceUI()
        }
    }
}

typealias UIModelScope = CoroutineScope

expect fun getRecompositionMode(): RecompositionMode

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
