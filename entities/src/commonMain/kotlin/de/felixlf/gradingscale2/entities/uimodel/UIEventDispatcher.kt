package de.felixlf.gradingscale2.entities.uimodel

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Enables the dispatching of UI events to the UI layer.
 */
interface UIEventDispatcher<UIEvent> {
    val events: Channel<UIEvent>
    fun eventFlow(): Flow<UIEvent> = events.receiveAsFlow()
}

