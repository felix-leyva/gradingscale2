package de.felixlf.gradingscale2.uimodel.experiment

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Enables the dispatching of UI events to the UI layer.
 */
internal interface UIEventDispatcher<UIEvent> {
    val events: Channel<UIEvent>
    fun eventFlow(): Flow<UIEvent> = events.receiveAsFlow()
}

/**
 * Allows the UI layer to observe UI events.
 * @param flow A flow based on the [Channel] of UI events.
 * @param onEvent A function that is called when a new event is emitted.
 */
@Composable
fun <UIEvent> ObserveEvents(
    flow: Flow<UIEvent>,
    onEvent: (UIEvent) -> Unit,
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(Unit) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect { event ->
                onEvent(event)
            }
        }
    }
}
