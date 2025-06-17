package de.felixlf.gradingscale2.features.list.upsertgradescaledialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.felixlf.gradingscale2.entities.features.list.upsertgradescaledialog.UpserGradeScaleUIEvent
import de.felixlf.gradingscale2.entities.features.list.upsertgradescaledialog.UpsertGradeScaleUIState
import de.felixlf.gradingscale2.entities.features.list.upsertgradescaledialog.UpsertGradeScaleUIStateFactory
import de.felixlf.gradingscale2.entities.uimodel.MoleculePresenter
import de.felixlf.gradingscale2.entities.usecases.GetAllGradeScalesUseCase
import de.felixlf.gradingscale2.entities.usecases.InsertGradeScaleUseCase
import de.felixlf.gradingscale2.entities.usecases.ShowSnackbarUseCase
import de.felixlf.gradingscale2.entities.usecases.UpdateGradeScaleUseCase
import de.felixlf.gradingscale2.uimodel.MoleculeViewModelHelper
import kotlinx.coroutines.flow.StateFlow

internal class UpsertGradeScaleViewModel(
    private val getAllGradeScalesUseCase: GetAllGradeScalesUseCase,
    private val insertGradeScaleUseCase: InsertGradeScaleUseCase,
    private val updateGradeScaleUseCase: UpdateGradeScaleUseCase,
    private val showSnackbarUseCase: ShowSnackbarUseCase,
) : ViewModel(), MoleculeViewModelHelper<UpsertGradeScaleUIState, UpserGradeScaleUIEvent> {
    override val factory: MoleculePresenter<UpsertGradeScaleUIState, UpserGradeScaleUIEvent> = UpsertGradeScaleUIStateFactory(
        getAllGradeScalesUseCase = getAllGradeScalesUseCase,
        insertGradeScaleUseCase = insertGradeScaleUseCase,
        updateGradeScaleUseCase = updateGradeScaleUseCase,
        showSnackbarUseCase = showSnackbarUseCase,
        scope = viewModelScope,
    )
    override val uiState: StateFlow<UpsertGradeScaleUIState> = moleculeState()
}
