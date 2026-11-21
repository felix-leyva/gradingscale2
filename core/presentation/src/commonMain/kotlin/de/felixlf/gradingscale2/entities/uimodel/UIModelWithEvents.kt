package de.felixlf.gradingscale2.entities.uimodel

/**
 * An [UIModelWithEvents] manages the UI state and UI events of a screen. It exposes a uiState [StateFlow<UIState>] which is consumed by the UI.
 * It also exposes an eventFlow [Flow<UIEvent>] which is used to dispatch UI events to the UI layer.
 */
interface UIModelWithEvents<UIState, UICommand, UIEvent> :
    UIModel<UIState, UICommand>,
    UIEventDispatcher<UIEvent>
