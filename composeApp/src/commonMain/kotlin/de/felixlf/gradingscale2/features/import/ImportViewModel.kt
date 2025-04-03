package de.felixlf.gradingscale2.features.import

import androidx.lifecycle.ViewModel
import de.felixlf.gradingscale2.entities.features.import.ImportCommand
import de.felixlf.gradingscale2.entities.features.import.ImportUIEvent
import de.felixlf.gradingscale2.entities.features.import.ImportUIModel
import de.felixlf.gradingscale2.entities.features.import.ImportUIState
import de.felixlf.gradingscale2.entities.uimodel.UIModel

class ImportViewModel(
    private val importUIModel: ImportUIModel,
) : ViewModel(importUIModel.scope), UIModel<ImportUIState, ImportCommand, ImportUIEvent> by importUIModel
