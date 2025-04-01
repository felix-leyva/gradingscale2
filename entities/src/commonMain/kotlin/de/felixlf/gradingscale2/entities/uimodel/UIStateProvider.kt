package de.felixlf.gradingscale2.entities.uimodel

import kotlinx.coroutines.flow.StateFlow

interface UIStateProvider<UIState> {
    /**
     * The UI [kotlinx.coroutines.flow.StateFlow] which represents the UI State and is consumed by the UI.
     * Use the [moleculeState] function to create the StateFlow.
     * ```
     * override val uiState = moleculeState()
     * ```
     */
    val uiState: StateFlow<UIState>

}
