package de.felixlf.gradingscale2.entities.features.weightedgradecalculator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.WeightedCalculatorCommand.AddGradeAtPos
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.WeightedCalculatorCommand.RemoveGrade
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.WeightedCalculatorCommand.SelectGradeScale
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.WeightedCalculatorCommand.UpdateGrade
import de.felixlf.gradingscale2.entities.models.weightedgrade.WeightedGrade
import de.felixlf.gradingscale2.entities.models.GradeScaleNameAndId
import de.felixlf.gradingscale2.entities.uimodel.UIModel
import de.felixlf.gradingscale2.entities.uimodel.UIModelScope
import de.felixlf.gradingscale2.entities.usecases.GetAllGradeScalesUseCase
import de.felixlf.gradingscale2.entities.usecases.GetGradeScaleByIdUseCase
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow

class WeightCalculatorUIModel(
    override val scope: UIModelScope,
    private val getAllGradeScales: GetAllGradeScalesUseCase,
    private val getGradeScaleByIdUseCase: GetGradeScaleByIdUseCase,
) : UIModel<WeightCalculatorUIState, WeightedCalculatorCommand, WeightedCalculatorEvent> {
    override val events: Channel<WeightedCalculatorEvent> = Channel()
    override val uiState: StateFlow<WeightCalculatorUIState> by moleculeUIState()

    private var selectedGradeScaleId by mutableStateOf<String?>(null)
    private var openedGradePos by mutableStateOf<Int?>(null)

    // TODO: temporary solution, later this will be saved in the database
    private val grades = mutableStateListOf<WeightedGrade>()

    @Composable
    override fun produceUI(): WeightCalculatorUIState {
        val gradeScalesNamesAndIds = getAllGradeScales().asState(persistentListOf()).map {
            GradeScaleNameAndId(name = it.gradeScaleName, id = it.id)
        }

        val selectedGradeScale = selectedGradeScaleId?.let {
            getGradeScaleByIdUseCase(it).asState(null)
        }

        return WeightCalculatorUIState(
            gradeScaleNameAndIds = gradeScalesNamesAndIds.toImmutableList(),
            selectedGradeScale = selectedGradeScale,
            grades = grades.toImmutableList(),
            selectedGrade = openedGradePos?.let { grades[it] },
        )
    }

    override fun sendCommand(command: WeightedCalculatorCommand) {
        when (command) {
            is AddGradeAtPos -> grades.add(command.position, command.grade)

            is RemoveGrade -> {
                openedGradePos = null
                grades.removeAt(command.position)
            }

            is UpdateGrade -> {
                openedGradePos = null
                grades[command.position] = command.grade
            }

            is SelectGradeScale -> selectedGradeScaleId = command.gradeScaleId
            is WeightedCalculatorCommand.SelectGrade -> openedGradePos = command.position
        }
    }
}
