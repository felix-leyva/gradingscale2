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
    private var state by mutableStateOf<State?>(State.Loading)

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
            state = state,
        )
    }

    override fun sendEvent(event: CreateGradeScaleUIEvent) {
        when (event) {
            CreateGradeScaleUIEvent.Cancel -> state = State.Cancel
            is CreateGradeScaleUIEvent.SetNewName -> newGradeScaleName = event.name
            is CreateGradeScaleUIEvent.Save -> {
                state = State.Loading
                scope.launch {
                    upsertGradeScaleUseCase(
                        gradeScaleName = newGradeScaleName,
                        gradeScaleId = TODO(
                            "Check which kind of IDs we are using in the Db. Maybe best is that the use case generates it.",
                        ),
                        defaultGradeName = event.defaultGradeName,
                    )
                        .onSuccess { state = State.Success(it) }
                        .onFailure { state = State.Error }
                }
            }
        }
    }
}

sealed interface CreateGradeScaleUIEvent {
    data class SetNewName(val name: String) : CreateGradeScaleUIEvent
    data class Save(val defaultGradeName: String) : CreateGradeScaleUIEvent
    object Cancel : CreateGradeScaleUIEvent
}
