package de.felixlf.gradingscale2.entities.uimodel

import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/**
 * An [UIModel] manages the UI state and UI events of a screen. It exposes a uiState [StateFlow<UIState>] which is consumed by the UI.
 * It also exposes an eventFlow [Flow<UIEvent>] which is used to dispatch UI events to the UI layer.
 */
interface UIModel<UIState, UICommand, UIEvent> :
    MoleculePresenter<UIState, UICommand>,
    UIEventDispatcher<UIEvent>,
    UIStateProvider<UIState> {
    /**
     * The coroutine scope of the UI Model. This is used to launch coroutines and manage the lifecycle of the UI Model.
     * Use the function [de.felixlf.gradingscale2.entities.util.DispatcherProvider.newUIScope] to create the scope with the proper dispatcher and job.
     * When instantiating a ViewModel, insert the scope in the viewModel constructor, to link the lifecycle of the ViewModel with the lifecycle of the
     * UI Model.
     */
    val scope: UIModelScope

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
