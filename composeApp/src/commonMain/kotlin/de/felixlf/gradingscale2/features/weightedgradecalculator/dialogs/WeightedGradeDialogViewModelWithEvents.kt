package de.felixlf.gradingscale2.features.weightedgradecalculator.dialogs

import androidx.lifecycle.ViewModel
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.dialog.WeightedGradeDialogCommand
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.dialog.WeightedGradeDialogEvent
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.dialog.WeightedGradeDialogUIModelWithEvents
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.dialog.WeightedGradeDialogUIState
import de.felixlf.gradingscale2.entities.uimodel.UIModelWithEvents

class WeightedGradeDialogViewModelWithEvents(
    val uiModel: WeightedGradeDialogUIModelWithEvents,
) : ViewModel(uiModel.scope),
    UIModelWithEvents<WeightedGradeDialogUIState, WeightedGradeDialogCommand, WeightedGradeDialogEvent> by uiModel
