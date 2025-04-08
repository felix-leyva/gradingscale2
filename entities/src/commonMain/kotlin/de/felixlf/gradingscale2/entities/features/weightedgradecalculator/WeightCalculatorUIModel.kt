package de.felixlf.gradingscale2.entities.features.weightedgradecalculator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.WeightedCalculatorCommand.AddGradeAtEnd
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.WeightedCalculatorCommand.AddGradeAtPos
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.WeightedCalculatorCommand.GradeAtPos
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.WeightedCalculatorCommand.RemoveGrade
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.WeightedCalculatorCommand.SelectGradeScale
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.WeightedCalculatorCommand.UpdateGrade
import de.felixlf.gradingscale2.entities.models.GradeScaleNameAndId
import de.felixlf.gradingscale2.entities.uimodel.UIModel
import de.felixlf.gradingscale2.entities.usecases.GetAllGradeScalesUseCase
import de.felixlf.gradingscale2.entities.usecases.GetGradeScaleByIdUseCase
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow

class WeightCalculatorUIModel(
    override val scope: CoroutineScope,
    private val getAllGradeScales: GetAllGradeScalesUseCase,
    private val getGradeScaleByIdUseCase: GetGradeScaleByIdUseCase,
) : UIModel<WeightCalculatorUIState, WeightedCalculatorCommand, WeightedCalculatorEvent> {
    override val events: Channel<WeightedCalculatorEvent> = Channel()
    override val uiState: StateFlow<WeightCalculatorUIState> by moleculeUIState()

    // TODO: keep this because later weighted grades will be saved in the database
    var isLoading by mutableStateOf(true)
    var selectedGradeScaleId by mutableStateOf<String?>(null)
    var openedGradePos by mutableStateOf<Int?>(null)

    // TODO: temporary solution, later this will be saved in the database
    val grades = mutableStateListOf<WeightCalculatorUIState.WeightedGrade>()

    @Composable
    override fun produceUI(): WeightCalculatorUIState {
        val gradeScalesNamesAndIds = getAllGradeScales().asState(persistentListOf()).map {
            GradeScaleNameAndId(name = it.gradeScaleName, id = it.id)
        }

        LaunchedEffect(gradeScalesNamesAndIds) {
            isLoading = gradeScalesNamesAndIds.isEmpty()
        }

        val selectedGradeScale = selectedGradeScaleId?.let {
            getGradeScaleByIdUseCase(it).asState(null)
        }

        return WeightCalculatorUIState(
            isLoading = isLoading,
            gradeScaleNameAndIds = gradeScalesNamesAndIds.toImmutableList(),
            selectedGradeScale = selectedGradeScale,
            grades = grades.toImmutableList(),
        )
    }

    override fun sendCommand(command: WeightedCalculatorCommand) {
        when (command) {
            AddGradeAtEnd -> openedGradePos = grades.size
            is AddGradeAtPos -> openedGradePos = command.position
            is GradeAtPos -> TODO()
            is RemoveGrade -> grades.removeAt(command.position)
            is SelectGradeScale -> selectedGradeScaleId = command.gradeScaleId
            is UpdateGrade -> grades[command.position] = command.grade
        }
    }
}
