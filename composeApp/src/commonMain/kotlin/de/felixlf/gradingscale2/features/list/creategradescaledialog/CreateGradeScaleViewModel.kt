package de.felixlf.gradingscale2.features.list.creategradescaledialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.felixlf.gradingscale2.entities.usecases.GetAllGradeScalesUseCase
import de.felixlf.gradingscale2.entities.usecases.UpsertGradeScaleUseCase
import de.felixlf.gradingscale2.uimodel.MoleculePresenter
import de.felixlf.gradingscale2.uimodel.MoleculeViewModelHelper
import kotlinx.coroutines.flow.StateFlow

internal class CreateGradeScaleViewModel(
    private val getAllGradeScalesUseCase: GetAllGradeScalesUseCase,
    private val upsertGradeScaleUseCase: UpsertGradeScaleUseCase,
) : ViewModel(), MoleculeViewModelHelper<CreateGradeScaleUIState, CreateGradeScaleUIEvent> {
    override val factory: MoleculePresenter<CreateGradeScaleUIState, CreateGradeScaleUIEvent> = CreateGradeScaleUIStateFactory(
        getAllGradeScalesUseCase = getAllGradeScalesUseCase,
        upsertGradeScaleUseCase = upsertGradeScaleUseCase,
        scope = viewModelScope,
    )
    override val uiState: StateFlow<CreateGradeScaleUIState> = moleculeState()
}
