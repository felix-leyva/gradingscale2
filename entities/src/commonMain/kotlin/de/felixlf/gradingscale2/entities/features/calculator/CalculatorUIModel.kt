package de.felixlf.gradingscale2.entities.features.calculator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.felixlf.gradingscale2.entities.uimodel.MoleculePresenter
import de.felixlf.gradingscale2.entities.uimodel.UIModelScope
import de.felixlf.gradingscale2.entities.usecases.GetAllGradeScalesUseCase
import de.felixlf.gradingscale2.entities.usecases.GetGradeScaleByIdUseCase
import de.felixlf.gradingscale2.entities.usecases.GetLastSelectedGradeScaleIdUseCase
import de.felixlf.gradingscale2.entities.usecases.SetLastSelectedGradeScaleIdUseCase
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

/**
 * This class is responsible for managing the UI state of the calculator screen.
 * @param allGradeScalesUseCase UseCase for getting all grade scales.
 * @param getGradeScaleByIdUseCase UseCase for getting a grade scale by its ID.
 */
class CalculatorUIModel(
    private val scope: UIModelScope,
    private val allGradeScalesUseCase: GetAllGradeScalesUseCase,
    private val getGradeScaleByIdUseCase: GetGradeScaleByIdUseCase,
    private val getLastSelectedGradeScaleIdUseCase: GetLastSelectedGradeScaleIdUseCase,
    private val setLastSelectedGradeScaleIdUseCase: SetLastSelectedGradeScaleIdUseCase,
) : MoleculePresenter<GradeScaleCalculatorUIState, CalculatorUIEvent> {

    // MutableStateOf causes inside the produceUI function recomposition which is helpful to update the State. If we wish to "observe" this
    // value in other places, we need to do this inside @Composable functions.
    private var gradeScaleId by mutableStateOf<String?>(null)
    private var totalPoints by mutableStateOf(10.0)
    private var percentage by mutableStateOf(1.0)

    // Important exception: do not use this state value in the produceUI function, to avoid recomposition loops.
    private var state by mutableStateOf<GradeScaleCalculatorUIState?>(null)

    @Composable
    override fun produceUI(): GradeScaleCalculatorUIState {
        LaunchedEffect(Unit) {
            gradeScaleId = getLastSelectedGradeScaleIdUseCase()
        }

        val selectedGradeScale = gradeScaleId?.let { getGradeScaleByIdUseCase(it).asState(null) }
        val gradeScalesNamesWithId = allGradeScalesUseCase().asState(persistentListOf()).map {
            GradeScaleCalculatorUIState.GradeScaleNameWithId(
                gradeScaleName = it.gradeScaleName,
                gradeScaleId = it.id,
            )
        }.toImmutableList()

        return GradeScaleCalculatorUIState(
            selectedGradeScale = selectedGradeScale,
            gradeScalesNamesWithId = gradeScalesNamesWithId,
            currentPercentage = percentage,
            totalPoints = totalPoints,
        ).also { state = it }
    }

    override fun sendCommand(command: CalculatorUIEvent) {
        when (command) {
            is CalculatorUIEvent.SelectGradeScale -> {
                gradeScaleId =
                    state?.gradeScalesNamesWithId?.firstOrNull { it.gradeScaleName == command.gradeScaleName }?.gradeScaleId
                scope.launch { gradeScaleId?.let { setLastSelectedGradeScaleIdUseCase(it) } }
            }

            is CalculatorUIEvent.SetTotalPoints -> {
                if (command.points <= 0) return
                totalPoints = command.points
            }

            is CalculatorUIEvent.SetPercentage -> if (command.percentage in 0.0..1.0) {
                percentage = command.percentage
            }

            is CalculatorUIEvent.SetPoints -> if (command.points in 0.0..totalPoints) {
                percentage = command.points / totalPoints
            }

            is CalculatorUIEvent.SetGradeName -> {
                val selectedGradePercentage = state?.selectedGradeScale?.getPercentage(command.gradeName) ?: return
                percentage = selectedGradePercentage
            }
        }
    }
}

sealed interface CalculatorUIEvent {
    data class SelectGradeScale(val gradeScaleName: String) : CalculatorUIEvent
    data class SetTotalPoints(val points: Double) : CalculatorUIEvent
    data class SetPercentage(val percentage: Double) : CalculatorUIEvent
    data class SetPoints(val points: Double) : CalculatorUIEvent
    data class SetGradeName(val gradeName: String) : CalculatorUIEvent
}
