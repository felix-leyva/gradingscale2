package de.felixlf.gradingscale2.features.list.upsertgradedialog

import androidx.lifecycle.ViewModel
import de.felixlf.gradingscale2.entities.features.list.upsertgradedialog.UpsertGradeUIEvent
import de.felixlf.gradingscale2.entities.features.list.upsertgradedialog.UpsertGradeUIFactory
import de.felixlf.gradingscale2.entities.features.list.upsertgradedialog.UpsertGradeUIState
import de.felixlf.gradingscale2.entities.uimodel.UIModel

/**
 * ViewModel for editing a grade.
 * This ViewModel uses a molecule UI pattern to manage the state of the UI. In this case the Molecule is used as a Flow.combine operator
 * to ease the uiState creation.
 */
class UpsertGradeViewModel(
    uiModel: UpsertGradeUIFactory,
) : ViewModel(uiModel.scope), UIModel<UpsertGradeUIState, UpsertGradeUIEvent> by uiModel
