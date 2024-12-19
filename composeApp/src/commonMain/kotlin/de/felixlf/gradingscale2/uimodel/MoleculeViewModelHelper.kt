package de.felixlf.gradingscale2.uimodel

import androidx.lifecycle.viewModelScope
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface which provides helper functions for a ViewModel that uses the Molecule UI pattern.
 * @param UIState The type of the UI State. This is usually a data class that represents the UI State.
 * @param UIEvent The type of the UI Event. This is usually a sealed interface with data classes or data objects that represent the UI Events.
 */
internal interface MoleculeViewModelHelper<UIState, UIEvent> {

    /**
     * The UI State Factory.
     * Provide here the [MoleculePresenter] which manages the UI State and the UI Events.
     */
    val factory: MoleculePresenter<UIState, UIEvent>

    /**
     * The UI [StateFlow] which represents the UI State and is consumed by the UI.
     * Use the [moleculeState] function to create the StateFlow.
     * ```
     * override val uiState = moleculeState()
     * ```
     */
    val uiState: StateFlow<UIState>

    /**
     * Function to handle UI Events. This function should be called from the UI to send events to the UI State Factory.
     * @param event The UI Event.
     */
    fun onEvent(event: UIEvent) {
        factory.sendEvent(event)
    }

    /**
     * Helper function to create the UI State [StateFlow].
     */
    fun <ViewModel, UIState, UIEvent> ViewModel.moleculeState(): StateFlow<UIState>
            where ViewModel : androidx.lifecycle.ViewModel, ViewModel : MoleculeViewModelHelper<UIState, UIEvent> {
        return viewModelScope.launchMolecule(RecompositionMode.Immediate) {
            factory.produceUI()
        }
    }
}
