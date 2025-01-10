package de.felixlf.gradingscale2.features.calculator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.felixlf.gradingscale2.entities.usecases.GetAllGradeScalesUseCase
import de.felixlf.gradingscale2.entities.usecases.GetGradeScaleByIdUseCase
import de.felixlf.gradingscale2.uimodel.MoleculePresenter
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

internal class CalculatorUIStateFactory(
    private val allGradeScalesUseCase: GetAllGradeScalesUseCase,
    private val getGradeScaleByIdUseCase: GetGradeScaleByIdUseCase,
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

    override fun sendEvent(event: CalculatorUIEvent) {
        when (event) {
            is CalculatorUIEvent.SelectGradeScale ->
                gradeScaleId =
                    state?.gradeScalesNamesWithId?.firstOrNull { it.gradeScaleName == event.gradeScaleName }?.gradeScaleId

            is CalculatorUIEvent.SetTotalPoints -> {
                if (event.points <= 0) return
                totalPoints = event.points
            }

            is CalculatorUIEvent.SetPercentage -> if (event.percentage in 0.0..1.0) {
                percentage = event.percentage
            }

            is CalculatorUIEvent.SetPoints -> if (event.points in 0.0..totalPoints) {
                percentage = event.points / totalPoints
            }

            is CalculatorUIEvent.SetGradeName -> {
                val selectedGradePercentage = state?.selectedGradeScale?.getPercentage(event.gradeName) ?: return
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
