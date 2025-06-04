package de.felixlf.gradingscale2.uimodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import de.felixlf.gradingscale2.entities.uimodel.MoleculePresenter
import de.felixlf.gradingscale2.entities.uimodel.UIStateProvider
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface which provides helper functions for a ViewModel that uses the Molecule UI pattern.
 * @param UIState The type of the UI State. This is usually a data class that represents the UI State.
 * @param UIEvent The type of the UI Event. This is usually a sealed interface with data classes or data objects that represent the UI Events.
 */
internal interface MoleculeViewModelHelper<UIState, UIEvent> : UIStateProvider<UIState> {

    /**
     * The UI State Factory.
     * Provide here the [de.felixlf.gradingscale2.entities.uimodel.MoleculePresenter] which manages the UI State and the UI Events.
     */
    val factory: MoleculePresenter<UIState, UIEvent>

    /**
     * Function to handle UI Events. This function should be called from the UI to send events to the UI State Factory.
     * @param event The UI Event.
     */
    fun onEvent(event: UIEvent) {
        factory.sendCommand(event)
    }

    /**
     * Helper function to create the UI State [kotlinx.coroutines.flow.StateFlow].
     */
    fun <ViewModel, UIState, UIEvent> ViewModel.moleculeState(): StateFlow<UIState>
        where ViewModel : androidx.lifecycle.ViewModel,
              ViewModel : MoleculeViewModelHelper<UIState, UIEvent> {
        return viewModelScope.launchMolecule(RecompositionMode.Immediate) {
            factory.produceUI()
        }
    }
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
    val lifecycle = androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(Unit) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect { event ->
                onEvent(event)
            }
        }
    }
}
