package de.felixlf.gradingscale2.features.import

import androidx.lifecycle.ViewModel
import de.felixlf.gradingscale2.entities.features.import.ImportCommand
import de.felixlf.gradingscale2.entities.features.import.ImportUIEvent
import de.felixlf.gradingscale2.entities.features.import.ImportUIModelWithEvents
import de.felixlf.gradingscale2.entities.features.import.ImportUIState
import de.felixlf.gradingscale2.entities.uimodel.UIModelWithEvents

class ImportViewModelWithEvents(
    private val importUIModel: ImportUIModelWithEvents,
) : ViewModel(importUIModel.scope), UIModelWithEvents<ImportUIState, ImportCommand, ImportUIEvent> by importUIModel
