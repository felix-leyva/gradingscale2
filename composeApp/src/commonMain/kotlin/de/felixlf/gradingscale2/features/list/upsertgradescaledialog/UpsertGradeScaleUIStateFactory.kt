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
import de.felixlf.gradingscale2.features.list.upsertgradescaledialog.UpsertGradeScaleUIState.State.Error
import de.felixlf.gradingscale2.features.list.upsertgradescaledialog.UpsertGradeScaleUIState.State.Loading
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
    private var uiSaveState by mutableStateOf<State?>(null)
    private var currentGradeScaleId by mutableStateOf<String?>(null)

    @Composable
    override fun produceUI(): UpsertGradeScaleUIState {
        val existingGradeScaleNames = getAllGradeScalesUseCase().asState(persistentListOf()).map { gradeScale ->
            UpsertGradeScaleUIState.GradeScaleNameAndId(
                name = gradeScale.gradeScaleName,
                id = gradeScale.id,
            )
        }.toImmutableList()

        LaunchedEffect(existingGradeScaleNames, currentGradeScaleId) {
            val gradeScaleId = currentGradeScaleId
            if (gradeScaleId != null && existingGradeScaleNames.isNotEmpty()) {
                existingGradeScaleNames.firstOrNull { it.id == gradeScaleId }?.name?.let {
                    newGradeScaleName = it
                }
            }
        }

        return UpsertGradeScaleUIState(
            existingGradeScaleNames = existingGradeScaleNames,
            newName = newGradeScaleName,
            saveState = uiSaveState,
        )
    }

    override fun sendEvent(event: UpserGradeScaleUIEvent) {
        when (event) {
            is UpserGradeScaleUIEvent.SetNewName -> newGradeScaleName = event.name
            is UpserGradeScaleUIEvent.Save -> save(event)
            is UpserGradeScaleUIEvent.SetCurrentGradeScaleId -> currentGradeScaleId = event.gradeScaleId
        }
    }

    private fun save(event: UpserGradeScaleUIEvent.Save) {
        uiSaveState = Loading
        scope.launch {
            val id = currentGradeScaleId
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
            }.onSuccess { uiSaveState = Success(it) }.onFailure { uiSaveState = Error }
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
    data class SetCurrentGradeScaleId(val gradeScaleId: String) : UpserGradeScaleUIEvent

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
