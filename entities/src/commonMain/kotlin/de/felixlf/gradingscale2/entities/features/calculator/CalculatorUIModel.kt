package de.felixlf.gradingscale2.entities.features.calculator

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.felixlf.gradingscale2.entities.uimodel.StateProducer
import de.felixlf.gradingscale2.entities.uimodel.UIModel
import de.felixlf.gradingscale2.entities.uimodel.asState
import de.felixlf.gradingscale2.entities.usecases.GetAllGradeScalesUseCase
import de.felixlf.gradingscale2.entities.usecases.GetGradeScaleByIdUseCase
import de.felixlf.gradingscale2.entities.usecases.GetLastSelectedGradeScaleIdUseCase
import de.felixlf.gradingscale2.entities.usecases.SetLastSelectedGradeScaleIdUseCase
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * This class is responsible for managing the UI state of the calculator screen.
 * @param allGradeScalesUseCase UseCase for getting all grade scales.
 * @param getGradeScaleByIdUseCase UseCase for getting a grade scale by its ID.
 */
class CalculatorUIModel(
    override val stateProducer: StateProducer,
    private val allGradeScalesUseCase: GetAllGradeScalesUseCase,
    private val getGradeScaleByIdUseCase: GetGradeScaleByIdUseCase,
    private val getLastSelectedGradeScaleIdUseCase: GetLastSelectedGradeScaleIdUseCase,
    private val setLastSelectedGradeScaleIdUseCase: SetLastSelectedGradeScaleIdUseCase,
) : UIModel<GradeScaleCalculatorUIState, CalculatorUIEvent> {

    private var gradeScaleId by mutableStateOf<String?>(null)
    private var totalPoints by mutableStateOf(10.0)
    private var percentage by mutableStateOf(1.0)

    override val uiState: StateFlow<GradeScaleCalculatorUIState> by stateProducer {
        LaunchedEffect(Unit) {
            val savedId = getLastSelectedGradeScaleIdUseCase()
            if (gradeScaleId == null && !savedId.isNullOrBlank()) {
                gradeScaleId = savedId
            }
        }

        val allGradeScalesFlow = remember { allGradeScalesUseCase() }
        val selectedGradeScaleFlow = remember(gradeScaleId) { gradeScaleId?.let { getGradeScaleByIdUseCase(it) } }
        val selectedGradeScale = selectedGradeScaleFlow?.asState(null)
        val gradeScalesNamesWithId = allGradeScalesFlow.asState(persistentListOf()).map {
            GradeScaleCalculatorUIState.GradeScaleNameWithId(
                gradeScaleName = it.gradeScaleName,
                gradeScaleId = it.id,
            )
        }.toImmutableList()

        GradeScaleCalculatorUIState(
            selectedGradeScale = selectedGradeScale,
            gradeScalesNamesWithId = gradeScalesNamesWithId,
            currentPercentage = percentage,
            totalPoints = totalPoints,
        )
    }

    override fun sendCommand(command: CalculatorUIEvent) {
        when (command) {
            is CalculatorUIEvent.SelectGradeScale -> {
                val matchedId = uiState.value.gradeScalesNamesWithId
                    .firstOrNull { it.gradeScaleName == command.gradeScaleName }
                    ?.gradeScaleId
                    ?: return
                gradeScaleId = matchedId
                scope.launch { setLastSelectedGradeScaleIdUseCase(matchedId) }
            }

            is CalculatorUIEvent.SetTotalPoints -> {
                if (!command.points.isFinite() || command.points <= 0.0) return
                totalPoints = command.points
            }

            is CalculatorUIEvent.SetPercentage -> if (command.percentage.isFinite() && command.percentage in 0.0..1.0) {
                percentage = command.percentage
            }

            is CalculatorUIEvent.SetPoints -> if (command.points.isFinite() && command.points in 0.0..totalPoints) {
                percentage = command.points / totalPoints
            }

            is CalculatorUIEvent.SetGradeName -> {
                val selectedGradePercentage = uiState.value.selectedGradeScale?.getPercentageOrNull(command.gradeName) ?: return
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
