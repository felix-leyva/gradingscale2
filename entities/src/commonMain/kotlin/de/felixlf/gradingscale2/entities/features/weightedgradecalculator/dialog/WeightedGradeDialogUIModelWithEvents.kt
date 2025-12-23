package de.felixlf.gradingscale2.entities.features.weightedgradecalculator.dialog

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.uimodel.StateProducer
import de.felixlf.gradingscale2.entities.uimodel.UIModelWithEvents
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow

class WeightedGradeDialogUIModelWithEvents(
    override val stateProducer: StateProducer,
) : UIModelWithEvents<WeightedGradeDialogUIState, WeightedGradeDialogCommand, WeightedGradeDialogEvent> {
    override val events: Channel<WeightedGradeDialogEvent> = Channel()
    private var gradeScale: GradeScale? by mutableStateOf(null)
    private var percentage: Double? by mutableStateOf(null)
    private var weight: Double? by mutableStateOf(null)

    override val uiState: StateFlow<WeightedGradeDialogUIState> by stateProducer {
        WeightedGradeDialogUIState(
            gradeScale = gradeScale,
            percentage = percentage,
            weight = weight,
        )
    }

    override fun sendCommand(command: WeightedGradeDialogCommand) {
        when (command) {
            is WeightedGradeDialogCommand.Init -> {
                gradeScale = command.gradeScale
                percentage = command.weightedGrade.percentage
                weight = command.weightedGrade.weight
            }

            is WeightedGradeDialogCommand.SelectGradeName -> {
                val newPercentage = gradeScale?.getPercentageOrNull(command.name) ?: return
                percentage = newPercentage
            }

            is WeightedGradeDialogCommand.SetPercentage -> {
                val newPercentage = command.percentage.toDoubleOrNull()?.coerceIn(0.0, 100.0) ?: return
                percentage = newPercentage / 100
            }

            is WeightedGradeDialogCommand.SetRelativeWeight -> {
                val relativeWeight = command.relativeWeight.toDoubleOrNull()?.coerceAtLeast(0.01) ?: return
                val weight = weight ?: return

                when {
                    relativeWeight > weight -> {
                        this.weight = relativeWeight
                        percentage = 1.0
                    }

                    else -> percentage = relativeWeight / weight
                }
            }

            is WeightedGradeDialogCommand.SetWeight -> {
                val newWeight = command.weight.toDoubleOrNull()?.coerceAtLeast(0.01) ?: return
                weight = newWeight
            }
        }
    }
}
