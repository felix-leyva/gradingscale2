package de.felixlf.gradingscale2.features.list.upsertgradescaledialog

import androidx.lifecycle.ViewModel
import de.felixlf.gradingscale2.entities.features.list.upsertgradescaledialog.UpserGradeScaleUIEvent
import de.felixlf.gradingscale2.entities.features.list.upsertgradescaledialog.UpsertGradeScaleUIState
import de.felixlf.gradingscale2.entities.features.list.upsertgradescaledialog.UpsertGradeScaleUIStateFactory
import de.felixlf.gradingscale2.entities.uimodel.UIModel

internal class UpsertGradeScaleViewModel(
    uiModel: UpsertGradeScaleUIStateFactory,
) : ViewModel(uiModel.scope), UIModel<UpsertGradeScaleUIState, UpserGradeScaleUIEvent> by uiModel
