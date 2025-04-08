package de.felixlf.gradingscale2.entities.features.list.upsertgradescaledialog

import de.felixlf.gradingscale2.entities.models.GradeScaleNameAndId
import gradingscale2.entities.generated.resources.Res
import gradingscale2.entities.generated.resources.gradescale_list_dialog_edit_error_duplicated_name
import gradingscale2.entities.generated.resources.gradescale_list_dialog_edit_error_invalid_name
import gradingscale2.entities.generated.resources.gradescale_list_dialog_edit_save_changes
import gradingscale2.entities.generated.resources.gradescale_list_dialog_edit_save_new
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.StringResource

data class UpsertGradeScaleUIState(
    val existingGradeScaleNames: ImmutableList<GradeScaleNameAndId>,
    val newName: String?,
    val state: State,
) {
    private val currentGradeScaleNameAndId =
        ((state as? State.Loaded)?.operation as? State.Operation.Update)?.currentGradeScaleId?.let { selectedId ->
            existingGradeScaleNames.firstOrNull { it.id == selectedId }
        }

    val error = when {
        newName?.isBlank() == true -> Errors.InvalidName

        currentGradeScaleNameAndId?.name != newName && existingGradeScaleNames.map<GradeScaleNameAndId, String> { it.name }
            .contains<String?>(newName) -> Errors.DuplicatedName

        else -> null
    }

    val isSaveEnabled = error == null

    sealed class Errors(val errorDescriptions: StringResource) {
        data object DuplicatedName : Errors(Res.string.gradescale_list_dialog_edit_error_duplicated_name)
        data object InvalidName : Errors(Res.string.gradescale_list_dialog_edit_error_invalid_name)
    }

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
