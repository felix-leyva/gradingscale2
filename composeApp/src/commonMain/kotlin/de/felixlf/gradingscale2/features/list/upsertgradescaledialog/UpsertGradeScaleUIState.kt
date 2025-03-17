package de.felixlf.gradingscale2.features.list.upsertgradescaledialog

import de.felixlf.gradingscale2.features.list.upsertgradescaledialog.UpsertGradeScaleUIState.State.Operation
import gradingscale2.composeapp.generated.resources.Res
import gradingscale2.composeapp.generated.resources.gradescale_list_dialog_edit_save_changes
import gradingscale2.composeapp.generated.resources.gradescale_list_dialog_edit_save_new
import gradingscale2.composeapp.generated.resources.gradescale_list_menu_edit_grade_scale
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.StringResource

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

        sealed class Operation(val button: StringResource) {
            data object Insert : Operation(Res.string.gradescale_list_dialog_edit_save_new)
            data class Update(val currentGradeScaleId: String) : Operation(Res.string.gradescale_list_dialog_edit_save_changes)
        }
    }
}
