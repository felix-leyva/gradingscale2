package de.felixlf.gradingscale2.features.gradescalecalculator

import androidx.lifecycle.ViewModel
import de.felixlf.gradingscale2.entities.usecases.GetAllGradeScalesUseCase
import de.felixlf.gradingscale2.entities.usecases.GetGradeScaleByIdUseCase
import de.felixlf.gradingscale2.uimodel.UIStateFactory
import de.felixlf.gradingscale2.uimodel.UIStateModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class GradeScaleListViewModel(
    allGradeScalesUseCase: GetAllGradeScalesUseCase,
    getGradeScaleByIdUseCase: GetGradeScaleByIdUseCase,
) : ViewModel(), UIStateModel<GradeScaleListUIState> {

    private val selectedGradeScaleId = MutableStateFlow<String?>(null)
    private val totalPoints = MutableStateFlow(10.0)
    override val uiStateFactory: UIStateFactory<GradeScaleListUIState> = GradeListUIStateFactory(
        allGradeScalesUseCase = allGradeScalesUseCase,
        getGradeScaleByIdUseCase = getGradeScaleByIdUseCase,
        selectedGradeScaleId = selectedGradeScaleId,
        totalPoints = totalPoints,
    )
    override val uiState = moleculeUIState()

    fun selectGradeScale(gradeScaleName: String) {
        val gradeScaleId =
            uiState.value.gradeScalesNamesWithId
                .firstOrNull { it.gradeScaleName == gradeScaleName }
                ?.gradeScaleId ?: return
        selectedGradeScaleId.update { gradeScaleId }
    }

    fun setTotalPoints(points: Double) {
        if (points <= 0) return
        totalPoints.update { points }
    }
}
