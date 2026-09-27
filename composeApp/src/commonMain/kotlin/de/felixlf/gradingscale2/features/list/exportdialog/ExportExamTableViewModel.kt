package de.felixlf.gradingscale2.features.list.exportdialog

import androidx.lifecycle.ViewModel
import de.felixlf.gradingscale2.entities.features.export.ExportExamTableUIEvent
import de.felixlf.gradingscale2.entities.features.export.ExportExamTableUIModel
import de.felixlf.gradingscale2.entities.features.export.ExportExamTableUIState
import de.felixlf.gradingscale2.entities.uimodel.UIModel

/**
 * ViewModel for the ExportExamTableDialog.
 * Bridges the Compose UI lifecycle to the multiplatform [ExportExamTableUIModel].
 */
class ExportExamTableViewModel(
    uiModel: ExportExamTableUIModel,
) : ViewModel(uiModel.scope), UIModel<ExportExamTableUIState, ExportExamTableUIEvent> by uiModel
