package de.felixlf.gradingscale2.entities.features.weightedgradecalculator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.WeightedCalculatorCommand.AddGradeAtPos
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.WeightedCalculatorCommand.RemoveGrade
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.WeightedCalculatorCommand.SelectGradeScale
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.WeightedCalculatorCommand.UpdateGrade
import de.felixlf.gradingscale2.entities.models.GradeScaleNameAndId
import de.felixlf.gradingscale2.entities.uimodel.UIModel
import de.felixlf.gradingscale2.entities.uimodel.UIModelScope
import de.felixlf.gradingscale2.entities.usecases.DeleteWeightedGradeUseCase
import de.felixlf.gradingscale2.entities.usecases.GetAllGradeScalesUseCase
import de.felixlf.gradingscale2.entities.usecases.GetAllWeightedGradesUseCase
import de.felixlf.gradingscale2.entities.usecases.GetGradeScaleByIdUseCase
import de.felixlf.gradingscale2.entities.usecases.GetLastSelectedGradeScaleId
import de.felixlf.gradingscale2.entities.usecases.SetLastSelectedGradeScaleId
import de.felixlf.gradingscale2.entities.usecases.UpsertWeightedGradeUseCase
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WeightCalculatorUIModel(
    override val scope: UIModelScope,
    private val getAllGradeScales: GetAllGradeScalesUseCase,
    private val getGradeScaleByIdUseCase: GetGradeScaleByIdUseCase,
    private val getAllWeightedGradesUseCase: GetAllWeightedGradesUseCase,
    private val upsertWeightedGradeUseCase: UpsertWeightedGradeUseCase,
    private val deleteWeightedGradeUseCase: DeleteWeightedGradeUseCase,
    private val getLastSelectedGradeScaleIdUseCase: GetLastSelectedGradeScaleId,
    private val setLastSelectedGradeScaleIdUseCase: SetLastSelectedGradeScaleId,
) : UIModel<WeightCalculatorUIState, WeightedCalculatorCommand, WeightedCalculatorEvent> {
    override val events: Channel<WeightedCalculatorEvent> = Channel()
    override val uiState: StateFlow<WeightCalculatorUIState> by moleculeUIState()

    private var selectedGradeScaleId by mutableStateOf<String?>(null)
    private var openedGradeId by mutableStateOf<String?>(null)

    @Composable
    override fun produceUI(): WeightCalculatorUIState {
        LaunchedEffect(Unit) { getLastSelectedGradeScaleIdUseCase()?.let { selectedGradeScaleId = it } }
        val grades = getAllWeightedGradesUseCase().asState(persistentListOf())

        val gradeScalesNamesAndIds = getAllGradeScales().asState(persistentListOf()).map {
            GradeScaleNameAndId(name = it.gradeScaleName, id = it.id)
        }

        val selectedGradeScale = selectedGradeScaleId?.let {
            getGradeScaleByIdUseCase(it).asState(null)
        }

        return WeightCalculatorUIState(
            gradeScaleNameAndIds = gradeScalesNamesAndIds.toImmutableList(),
            selectedGradeScale = selectedGradeScale,
            grades = grades,
            selectedGrade = openedGradeId?.let { grades.find { it.uuid == openedGradeId } },
        )
    }

    override fun sendCommand(command: WeightedCalculatorCommand) {
        when (command) {
            is AddGradeAtPos -> scope.launch {
                upsertWeightedGradeUseCase(command.grade)
            }

            is RemoveGrade -> scope.launch {
                openedGradeId = null
                deleteWeightedGradeUseCase(command.id)
            }

            is UpdateGrade -> scope.launch {
                openedGradeId = null
                upsertWeightedGradeUseCase(command.grade)
            }

            is SelectGradeScale -> scope.launch {
                selectedGradeScaleId = command.gradeScaleId
                selectedGradeScaleId?.let { setLastSelectedGradeScaleIdUseCase(it) }
            }

            is WeightedCalculatorCommand.SelectGrade -> openedGradeId = command.id
        }
    }
}
