package de.felixlf.gradingscale2.features.gradescalecalculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.felixlf.gradingscale2.entities.usecases.GetAllGradeScalesUseCase
import de.felixlf.gradingscale2.entities.usecases.GetGradeScaleByIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

internal class GradeScaleListViewModel(
    allGradeScalesUseCase: GetAllGradeScalesUseCase,
    getGradeScaleByIdUseCase: GetGradeScaleByIdUseCase,
) : ViewModel() {

    private val selectedGradeScaleId = MutableStateFlow<String?>(null)
    private val totalPoints = MutableStateFlow(10.0)

    val uiState: StateFlow<GradeScaleListUIState> =  GradeListUIStateFactory(
        allGradeScalesUseCase = allGradeScalesUseCase,
        getGradeScaleByIdUseCase = getGradeScaleByIdUseCase,
        selectedGradeScaleId = selectedGradeScaleId,
        totalPoints = totalPoints,
    ).moleculeUIState(viewModelScope)

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
