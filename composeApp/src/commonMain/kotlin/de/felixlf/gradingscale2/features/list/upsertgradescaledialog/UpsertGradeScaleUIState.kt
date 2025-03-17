package de.felixlf.gradingscale2.features.list.upsertgradescaledialog

import de.felixlf.gradingscale2.features.list.upsertgradescaledialog.UpsertGradeScaleUIState.State.Operation
import kotlinx.collections.immutable.ImmutableList

data class UpsertGradeScaleUIState(
    val existingGradeScaleNames: ImmutableList<GradeScaleNameAndId>,
    val newName: String?,
    val state: State,
) {

    val error = when {
        newName?.isBlank() == true -> Errors.INVALID_NAME
        existingGradeScaleNames.map<GradeScaleNameAndId, String> { it.name }.contains<String?>(newName) -> Errors.DUPLICATE_NAME
        else -> null
    }

    val isSaveEnabled = error == null && (state as? State.Loaded)?.operation is Operation.Insert
    val isInsertEnabled = error == null && (state as? State.Loaded)?.operation is Operation.Update

    data class GradeScaleNameAndId(val name: String, val id: String)

    enum class Errors { DUPLICATE_NAME, INVALID_NAME }

    sealed interface State {
        data object Loading : State
        data class Loaded(val operation: Operation) : State
        data class Success(val gradeScaleId: String) : State
        data object SaveError : State

        sealed interface Operation {
            data object Insert : Operation
            data class Update(val currentGradeScaleId: String) : Operation
        }
    }
}
