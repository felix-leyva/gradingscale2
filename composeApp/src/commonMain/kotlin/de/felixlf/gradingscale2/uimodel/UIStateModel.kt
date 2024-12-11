package de.felixlf.gradingscale2.uimodel

import androidx.lifecycle.viewModelScope
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import kotlinx.coroutines.flow.StateFlow

/**
 * A UI State Model that produces a UI State of type [T].
 * The UI State is produced by a [UIStateFactory] that should provided by an implementing class and is exposed as a [StateFlow].
 * In case this is a ViewModel, the [moleculeUIState] extension function can be used to automatically produce the UI State.
 */
internal interface UIStateModel<T> {

    /**
     * The factory that produces the UI State.
     */
    val uiStateFactory: UIStateFactory<T>

    /**
     * The UI [StateFlow] of type [T] that is produced by the [uiStateFactory].
     *
     * In case this is a ViewModel, the [moleculeUIState] extension function can be used to automatically produce the UI State. Example:
     * ```
     * override val uiState = moleculeUIState()
     * ```
     */
    val uiState: StateFlow<T>

    /**
     * Produces the UI State using the [uiStateFactory] and returns it as a [StateFlow].
     */
    fun <ViewModel, T> ViewModel.moleculeUIState(): StateFlow<T> where ViewModel : androidx.lifecycle.ViewModel, ViewModel : UIStateModel<T> {
        return viewModelScope.launchMolecule(mode = RecompositionMode.Immediate) { uiStateFactory.produceUI() }
    }
}
