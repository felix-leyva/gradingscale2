package de.felixlf.gradingscale2.features.list.creategradescaledialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.felixlf.gradingscale2.entities.usecases.GetAllGradeScalesUseCase
import de.felixlf.gradingscale2.entities.usecases.UpsertGradeScaleUseCase
import de.felixlf.gradingscale2.features.list.creategradescaledialog.CreateGradeScaleUIState.State
import de.felixlf.gradingscale2.uimodel.MoleculePresenter
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class CreateGradeScaleUIStateFactory(
    private val getAllGradeScalesUseCase: GetAllGradeScalesUseCase,
    private val upsertGradeScaleUseCase: UpsertGradeScaleUseCase,
    private val scope: CoroutineScope,
) : MoleculePresenter<CreateGradeScaleUIState, CreateGradeScaleUIEvent> {
    private var newGradeScaleName by mutableStateOf("")
    private var uiSaveState by mutableStateOf<State?>(null)

    @Composable
    override fun produceUI(): CreateGradeScaleUIState {
        val existingGradeScaleNames = getAllGradeScalesUseCase().asState(persistentListOf()).map { gradeScale ->
            CreateGradeScaleUIState.GradeScaleNameAndId(
                name = gradeScale.gradeScaleName,
                id = gradeScale.id,
            )
        }.toImmutableList()

        return CreateGradeScaleUIState(
            existingGradeScaleNames = existingGradeScaleNames,
            newName = newGradeScaleName,
            saveState = uiSaveState,
        )
    }

    override fun sendEvent(event: CreateGradeScaleUIEvent) {
        when (event) {
            is CreateGradeScaleUIEvent.SetNewName -> newGradeScaleName = event.name
            is CreateGradeScaleUIEvent.Save -> {
                uiSaveState = State.Loading
                scope.launch {
                    upsertGradeScaleUseCase(
                        gradeScaleName = newGradeScaleName,
                        defaultGradeName = event.defaultGradeName,
                    )
                        .onSuccess { uiSaveState = State.Success(it) }
                        .onFailure { uiSaveState = State.Error }
                }
            }
        }
    }
}

sealed interface CreateGradeScaleUIEvent {
    data class SetNewName(val name: String) : CreateGradeScaleUIEvent
    data class Save(val defaultGradeName: String) : CreateGradeScaleUIEvent
}
