package de.felixlf.gradingscale2.features.list.upsertgradedialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.felixlf.gradingscale2.entities.usecases.GetGradeByUUIDUseCase
import de.felixlf.gradingscale2.entities.usecases.GetGradeScaleByIdUseCase
import de.felixlf.gradingscale2.entities.usecases.InsertGradeUseCase
import de.felixlf.gradingscale2.entities.usecases.UpsertGradeUseCase
import de.felixlf.gradingscale2.uimodel.MoleculeViewModelHelper

/**
 * ViewModel for editing a grade.
 * @param getGradeByUUIDUseCase UseCase for getting a grade by its UUID.
 * @param upsertGradeUseCase UseCase for updating a grade.
 * This ViewModel uses a molecule UI pattern to manage the state of the UI. In this case the Molecule is used as a Flow.combine operator
 * to ease the uiState creation.
 */
class UpsertGradeViewModel(
    getGradeByUUIDUseCase: GetGradeByUUIDUseCase,
    upsertGradeUseCase: UpsertGradeUseCase,
    insertGradeUseCase: InsertGradeUseCase,
    getGradeScaleByIdUseCase: GetGradeScaleByIdUseCase,
) : ViewModel(), MoleculeViewModelHelper<UpsertGradeUIState, UpsertGradeUIEvent> {
    override val factory = UpsertGradeUIFactory(
        getGradeByUUIDUseCase = getGradeByUUIDUseCase,
        insertGradeUseCase = insertGradeUseCase,
        upsertGradeUseCase = upsertGradeUseCase,
        getGradeScaleByIdUseCase = getGradeScaleByIdUseCase,
        viewModelScope = viewModelScope,
    )

    override val uiState = moleculeState()
}
