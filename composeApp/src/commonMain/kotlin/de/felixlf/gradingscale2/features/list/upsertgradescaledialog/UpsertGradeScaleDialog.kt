package de.felixlf.gradingscale2.features.list.upsertgradescaledialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.felixlf.gradingscale2.entities.features.list.upsertgradescaledialog.UpserGradeScaleUIEvent
import de.felixlf.gradingscale2.entities.features.list.upsertgradescaledialog.UpsertGradeScaleUIState
import de.felixlf.gradingscale2.entities.features.list.upsertgradescaledialog.UpsertGradeScaleUIState.State.Operation
import de.felixlf.gradingscale2.entities.models.GradeScaleNameAndId
import de.felixlf.gradingscale2.entities.util.MockGradeScalesGenerator
import de.felixlf.gradingscale2.features.calculator.CalculatorTextField
import de.felixlf.gradingscale2.theme.AppTheme
import de.felixlf.gradingscale2.utils.dialogScopedViewModel
import de.felixlf.gradingscale2.utils.textFieldManager
import gradingscale2.entities.generated.resources.Res
import gradingscale2.entities.generated.resources.gradescale_list_dialog_edit_cancel
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider

@Composable
internal fun UpsertGradeScaleDialog(
    viewModel: UpsertGradeScaleViewModel = dialogScopedViewModel<UpsertGradeScaleViewModel>(),
    onDismiss: (gradeScaleId: String?) -> Unit = {},
    operation: Operation,
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(operation) {
        viewModel.onEvent(UpserGradeScaleUIEvent.SetOperation(operation))
    }

    LaunchedEffect(uiState.value.state) {
        (uiState.value.state as? UpsertGradeScaleUIState.State.Success)?.gradeScaleId?.let { onDismiss(it) }
    }

    // TODO: add into resources
    val defaultGradeName = "new grade"
    Dialog(onDismissRequest = { onDismiss(null) }) {
        UpsertGradeScaleDialog(
            uiState = uiState.value,
            onSetNewName = { viewModel.onEvent(UpserGradeScaleUIEvent.SetNewName(it)) },
            onSave = { viewModel.onEvent(UpserGradeScaleUIEvent.Save(defaultGradeName)) },
            onDismiss = { onDismiss(null) },
        )
    }
}

@Composable
private fun UpsertGradeScaleDialog(
    uiState: UpsertGradeScaleUIState,
    onSetNewName: (String) -> Unit = {},
    onSave: () -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    Card {
        Column(
            modifier = Modifier.padding(24.dp).width(IntrinsicSize.Max),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            when (uiState.state) {
                UpsertGradeScaleUIState.State.Loading -> CircularProgressIndicator()
                is UpsertGradeScaleUIState.State.Loaded -> {
                    val textFieldState = textFieldManager(uiState.newName ?: "") {
                        onSetNewName(it)
                    }
                    CalculatorTextField(
                        state = textFieldState,
                    )
                    uiState.error?.errorDescriptions?.let {
                        Text(stringResource(it))
                    }
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        Button(
                            onClick = onSave,
                            enabled = uiState.isSaveEnabled,
                        ) {
                            Text(stringResource((uiState.state as UpsertGradeScaleUIState.State.Loaded).operation.button))
                        }
                        Button(onClick = onDismiss) {
                            Text(stringResource(Res.string.gradescale_list_dialog_edit_cancel))
                        }
                    }
                }

                UpsertGradeScaleUIState.State.SaveError -> Unit // TODO: Implement error message
                is UpsertGradeScaleUIState.State.Success -> Unit // TODO: define if implement success message
            }
        }
    }
}

@Preview
@Composable
fun CreateNewGradeScaleDialogPreview() = AppTheme {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Workaround for https://youtrack.jetbrains.com/issue/KTIJ-33358/K2-PreviewParameters-leads-to-render-issues-for-Preview-in-commonMain-source-set
        CreateNewGradeScaleDialogPreviewParameter().values.forEach { state ->
            UpsertGradeScaleDialog(
                uiState = UpsertGradeScaleUIState(
                    existingGradeScaleNames = MockGradeScalesGenerator().gradeScales.map { gradeScale ->
                        GradeScaleNameAndId(
                            name = gradeScale.gradeScaleName,
                            id = gradeScale.id,
                        )
                    }.toImmutableList(),
                    newName = "new grade scale",
                    state = state,
                ),
            )
        }
    }
}

class CreateNewGradeScaleDialogPreviewParameter(
    override val values: Sequence<UpsertGradeScaleUIState.State> = sequenceOf(
        UpsertGradeScaleUIState.State.Loading,
        UpsertGradeScaleUIState.State.Loaded(Operation.Insert),
        UpsertGradeScaleUIState.State.Loaded(Operation.Update("1")),
        UpsertGradeScaleUIState.State.Success("1"),
        UpsertGradeScaleUIState.State.SaveError,
    ),
) : PreviewParameterProvider<UpsertGradeScaleUIState.State>
