package de.felixlf.gradingscale2.features.gradescalecalculator

import androidx.lifecycle.ViewModel
import de.felixlf.gradingscale2.entities.usecases.GetAllGradeScalesUseCase
import de.felixlf.gradingscale2.entities.usecases.GetGradeScaleByIdUseCase
import de.felixlf.gradingscale2.uimodel.UIEventPresenter
import de.felixlf.gradingscale2.uimodel.UIEventPresenterImpl
import kotlinx.coroutines.flow.StateFlow

internal class GradeScaleListViewModel(
    allGradeScalesUseCase: GetAllGradeScalesUseCase,
    getGradeScaleByIdUseCase: GetGradeScaleByIdUseCase,
) : ViewModel(), UIEventPresenter<GradeScaleListUIEvent> by UIEventPresenterImpl() {
    val uiState: StateFlow<GradeScaleListUIState> = GradeListUIStateFactory(
        allGradeScalesUseCase = allGradeScalesUseCase,
        getGradeScaleByIdUseCase = getGradeScaleByIdUseCase,
    ).moleculeUIState(this)
}
