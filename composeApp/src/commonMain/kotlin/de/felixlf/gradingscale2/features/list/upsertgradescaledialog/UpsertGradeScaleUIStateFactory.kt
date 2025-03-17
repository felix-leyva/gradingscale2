package de.felixlf.gradingscale2.features.list.upsertgradescaledialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.felixlf.gradingscale2.entities.usecases.GetAllGradeScalesUseCase
import de.felixlf.gradingscale2.entities.usecases.InsertGradeScaleUseCase
import de.felixlf.gradingscale2.entities.usecases.UpdateGradeScaleUseCase
import de.felixlf.gradingscale2.features.list.upsertgradescaledialog.UpsertGradeScaleUIState.State
import de.felixlf.gradingscale2.features.list.upsertgradescaledialog.UpsertGradeScaleUIState.State.Loaded
import de.felixlf.gradingscale2.features.list.upsertgradescaledialog.UpsertGradeScaleUIState.State.Loading
import de.felixlf.gradingscale2.features.list.upsertgradescaledialog.UpsertGradeScaleUIState.State.SaveError
import de.felixlf.gradingscale2.features.list.upsertgradescaledialog.UpsertGradeScaleUIState.State.Success
import de.felixlf.gradingscale2.uimodel.MoleculePresenter
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class UpsertGradeScaleUIStateFactory(
    private val getAllGradeScalesUseCase: GetAllGradeScalesUseCase,
    private val insertGradeScaleUseCase: InsertGradeScaleUseCase,
    private val updateGradeScaleUseCase: UpdateGradeScaleUseCase,
    private val scope: CoroutineScope,
) : MoleculePresenter<UpsertGradeScaleUIState, UpserGradeScaleUIEvent> {
    private var newGradeScaleName by mutableStateOf("")
    private var uiSaveState by mutableStateOf<State>(Loading)
    private var operation by mutableStateOf<State.Operation?>(null)

    @Composable
    override fun produceUI(): UpsertGradeScaleUIState {
        val existingGradeScaleNames = getAllGradeScalesUseCase().asState(persistentListOf()).map { gradeScale ->
            UpsertGradeScaleUIState.GradeScaleNameAndId(
                name = gradeScale.gradeScaleName,
                id = gradeScale.id,
            )
        }.toImmutableList()

        LaunchedEffect(existingGradeScaleNames, operation) {
            (operation as? State.Operation.Update)?.let { operation ->
                val gradeScale = existingGradeScaleNames.find { it.id == operation.currentGradeScaleId }
                newGradeScaleName = gradeScale?.name ?: ""
            }
            operation?.let { uiSaveState = Loaded(it) }
        }

        return UpsertGradeScaleUIState(
            existingGradeScaleNames = existingGradeScaleNames,
            newName = newGradeScaleName,
            state = uiSaveState,
        )
    }

    override fun sendEvent(event: UpserGradeScaleUIEvent) {
        when (event) {
            is UpserGradeScaleUIEvent.SetNewName -> newGradeScaleName = event.name
            is UpserGradeScaleUIEvent.Save -> save(event)
            is UpserGradeScaleUIEvent.SetOperation -> operation = event.operation
        }
    }

    private fun save(event: UpserGradeScaleUIEvent.Save) {
        uiSaveState = Loading
        scope.launch {
            val id = (operation as? State.Operation.Update)?.currentGradeScaleId
            when {
                id != null -> updateGradeScaleUseCase(
                    gradeScaleId = id,
                    gradeScaleName = newGradeScaleName,
                    defaultGradeName = event.defaultGradeName,
                )

                else -> insertGradeScaleUseCase(
                    gradeScaleName = newGradeScaleName,
                    defaultGradeName = event.defaultGradeName,
                )
            }.onSome { uiSaveState = Success(it) }.onNone { uiSaveState = SaveError }
        }
    }
}

/**
 * UI Event for the UpsertGradeScaleDialog
 */
sealed interface UpserGradeScaleUIEvent {

    /**
     * Set the current grade scale id, if the dialog is used to update a grade scale
     */
    data class SetOperation(val operation: State.Operation) : UpserGradeScaleUIEvent

    /**
     * Set the new name for the grade scale
     */
    data class SetNewName(val name: String) : UpserGradeScaleUIEvent

    /**
     * Save the grade scale
     * @param defaultGradeName the default grade name for the grade scale - which is provided by the localisation service
     */
    data class Save(val defaultGradeName: String) : UpserGradeScaleUIEvent
}
