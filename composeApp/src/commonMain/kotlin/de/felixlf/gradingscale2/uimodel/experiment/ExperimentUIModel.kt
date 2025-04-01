package de.felixlf.gradingscale2.uimodel.experiment

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.felixlf.gradingscale2.entities.uimodel.UIModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel

/**
 * A UI model for the experiment screen.
 *
 */
class ExperimentUIModel(
    override val scope: CoroutineScope,
) : UIModel<ExperimentUIState, ExperimentUICommand, ExperimentUIEvent> {
    override val events = Channel<ExperimentUIEvent>()
    override val uiState by moleculeUIState()

    var text: String by mutableStateOf("Hello World")
    var count by mutableStateOf(0)

    @Composable
    override fun produceUI(): ExperimentUIState {
        LaunchedEffect(Unit) {
            println("produceUI called")
        }
        return ExperimentUIState(
            text = "$text $count",
        )
    }

    override fun sendCommand(command: ExperimentUICommand) {
        when (command) {
            is ExperimentUICommand.ShowDialog -> {
                events.trySend(ExperimentUIEvent.ShowDialogEvent)
                count++
            }
        }
    }
}

sealed interface ExperimentUICommand {
    data object ShowDialog : ExperimentUICommand
}

class ExperimentUIState(
    val text: String,
)

sealed interface ExperimentUIEvent {
    data object ShowDialogEvent : ExperimentUIEvent
}
