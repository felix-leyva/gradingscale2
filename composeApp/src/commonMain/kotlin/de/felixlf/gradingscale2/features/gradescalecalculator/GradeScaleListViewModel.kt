package de.felixlf.gradingscale2.features.gradescalecalculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.felixlf.gradingscale2.entities.usecases.GetAllGradeScalesUseCase
import de.felixlf.gradingscale2.entities.usecases.GetGradeScaleByIdUseCase
import de.felixlf.gradingscale2.features.gradescalecalculator.GradeScaleListUIState.GradeScaleNameWithId
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class GradeScaleListViewModel(
    getAllGradeScalesUseCase: GetAllGradeScalesUseCase,
    private val getGradeScaleByIdUseCase: GetGradeScaleByIdUseCase,
) : ViewModel() {
    private val selectedGradeScaleId = MutableStateFlow<String?>(null)
    private val totalPoints = MutableStateFlow(10.0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState =
        selectedGradeScaleId
            .flatMapLatest { id ->
                id?.let { getGradeScaleByIdUseCase(it) } ?: flowOf(null)
            }.combine(totalPoints) { gradeScale, totalPoints ->
                gradeScale?.copy(totalPoints = totalPoints)
            }.combine(getAllGradeScalesUseCase()) { selectedGradeScale, gradeScales ->
                GradeScaleListUIState(
                    selectedGradeScale = selectedGradeScale,
                    gradeScalesNamesWithId =
                        gradeScales
                            .map {
                                GradeScaleNameWithId(
                                    gradeScaleName = it.gradeScaleName,
                                    gradeScaleId = it.id,
                                )
                            }.toImmutableList(),
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = GradeScaleListUIState.Initial,
            )

    fun selectGradeScale(gradeScaleName: String) {
        val gradeScaleId =
            uiState.value.gradeScalesNamesWithId
                .firstOrNull { it.gradeScaleName == gradeScaleName }
                ?.gradeScaleId ?: return
        selectedGradeScaleId.update { gradeScaleId }
    }

    fun setTotalPoints(points: Double) {
        totalPoints.update { points }
    }
}
