package de.felixlf.gradingscale2.features.list.creategradescaledialog

import kotlinx.collections.immutable.ImmutableList

data class CreateGradeScaleUIState(
    val existingGradeScaleNames: ImmutableList<GradeScaleNameAndId>,
    val newName: String?,
    val saveState: State?,
) {
    val nameIsValid = newName?.isNotBlank() == true
    val nameAlreadyExists = existingGradeScaleNames.map { it.name }.contains(newName)
    val isSaveEnabled = nameIsValid && !nameAlreadyExists

    val state = when {
        saveState != null -> saveState
        existingGradeScaleNames.isEmpty() -> State.Loading
        else -> State.Loaded
    }

    data class GradeScaleNameAndId(
        val name: String,
        val id: String,
    )

    sealed interface State {
        data object Loading : State
        data object Loaded : State
        data object Error : State
        data class Success(val gradeScaleId: String) : State
    }
}
