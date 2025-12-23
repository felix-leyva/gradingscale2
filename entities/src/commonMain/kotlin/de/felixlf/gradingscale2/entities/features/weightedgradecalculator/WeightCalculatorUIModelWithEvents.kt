package de.felixlf.gradingscale2.entities.features.weightedgradecalculator

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.WeightedCalculatorCommand.AddGradeAtPos
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.WeightedCalculatorCommand.RemoveGrade
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.WeightedCalculatorCommand.SelectGradeScale
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.WeightedCalculatorCommand.UpdateGrade
import de.felixlf.gradingscale2.entities.models.GradeScaleNameAndId
import de.felixlf.gradingscale2.entities.uimodel.StateProducer
import de.felixlf.gradingscale2.entities.uimodel.UIModelWithEvents
import de.felixlf.gradingscale2.entities.uimodel.asState
import de.felixlf.gradingscale2.entities.usecases.DeleteWeightedGradeUseCase
import de.felixlf.gradingscale2.entities.usecases.GetAllGradeScalesUseCase
import de.felixlf.gradingscale2.entities.usecases.GetAllWeightedGradesUseCase
import de.felixlf.gradingscale2.entities.usecases.GetGradeScaleByIdUseCase
import de.felixlf.gradingscale2.entities.usecases.GetLastSelectedGradeScaleIdUseCase
import de.felixlf.gradingscale2.entities.usecases.SetLastSelectedGradeScaleIdUseCase
import de.felixlf.gradingscale2.entities.usecases.UpsertWeightedGradeUseCase
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WeightCalculatorUIModelWithEvents(
    override val stateProducer: StateProducer,
    private val getAllGradeScales: GetAllGradeScalesUseCase,
    private val getGradeScaleByIdUseCase: GetGradeScaleByIdUseCase,
    private val getAllWeightedGradesUseCase: GetAllWeightedGradesUseCase,
    private val upsertWeightedGradeUseCase: UpsertWeightedGradeUseCase,
    private val deleteWeightedGradeUseCase: DeleteWeightedGradeUseCase,
    private val getLastSelectedGradeScaleIdUseCase: GetLastSelectedGradeScaleIdUseCase,
    private val setLastSelectedGradeScaleIdUseCase: SetLastSelectedGradeScaleIdUseCase,
) : UIModelWithEvents<WeightCalculatorUIState, WeightedCalculatorCommand, WeightedCalculatorEvent> {
    override val events: Channel<WeightedCalculatorEvent> = Channel()

    private var selectedGradeScaleId by mutableStateOf<String?>(null)
    private var openedGradeId by mutableStateOf<String?>(null)

    override val uiState: StateFlow<WeightCalculatorUIState> by stateProducer {
        LaunchedEffect(Unit) { getLastSelectedGradeScaleIdUseCase()?.let { selectedGradeScaleId = it } }
        val grades = getAllWeightedGradesUseCase().asState(persistentListOf())

        val gradeScalesNamesAndIds = getAllGradeScales().asState(persistentListOf()).map {
            GradeScaleNameAndId(name = it.gradeScaleName, id = it.id)
        }

        val selectedGradeScale = selectedGradeScaleId?.let {
            getGradeScaleByIdUseCase(it).asState(null)
        }

        WeightCalculatorUIState(
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
