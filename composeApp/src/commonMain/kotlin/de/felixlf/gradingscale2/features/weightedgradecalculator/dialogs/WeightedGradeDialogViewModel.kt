package de.felixlf.gradingscale2.features.weightedgradecalculator.dialogs

import androidx.lifecycle.ViewModel
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.dialog.WeightedGradeDialogCommand
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.dialog.WeightedGradeDialogEvent
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.dialog.WeightedGradeDialogUIModel
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.dialog.WeightedGradeDialogUIState
import de.felixlf.gradingscale2.entities.uimodel.UIModel

class WeightedGradeDialogViewModel(
    val uiModel: WeightedGradeDialogUIModel,
) : ViewModel(uiModel.scope),
    UIModel<WeightedGradeDialogUIState, WeightedGradeDialogCommand, WeightedGradeDialogEvent> by uiModel
