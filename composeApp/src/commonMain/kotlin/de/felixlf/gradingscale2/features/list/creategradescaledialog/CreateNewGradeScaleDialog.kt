package de.felixlf.gradingscale2.features.list.creategradescaledialog

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.felixlf.gradingscale2.entities.util.MockGradeScalesGenerator
import de.felixlf.gradingscale2.features.calculator.CalculatorTextField
import de.felixlf.gradingscale2.theme.AppTheme
import de.felixlf.gradingscale2.utils.dialogScopedViewModel
import de.felixlf.gradingscale2.utils.textFieldManager
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun CreateNewGradeScaleDialog(
    viewModel: CreateGradeScaleViewModel = dialogScopedViewModel<CreateGradeScaleViewModel>(),
    onDismiss: () -> Unit = {},
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    // TODO: add into resources
    val defaultGradeName = "new grade"
    Dialog(onDismissRequest = onDismiss) {
        CreateNewGradeScaleDialog(
            uiState = uiState.value,
            onSetNewName = { viewModel.onEvent(CreateGradeScaleUIEvent.SetNewName(it)) },
            onSave = {
                viewModel.onEvent(CreateGradeScaleUIEvent.Save(defaultGradeName))
                onDismiss()
            },
            onDismiss = onDismiss,
        )
    }
}

@Composable
private fun CreateNewGradeScaleDialog(
    uiState: CreateGradeScaleUIState,
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
            if (uiState.saveState == CreateGradeScaleUIState.State.Loading) {
                CircularProgressIndicator()
            } else {
                val textFieldState = textFieldManager(uiState.newName ?: "") {
                    onSetNewName(it)
                }
                CalculatorTextField(
                    state = textFieldState,
                )
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    Button(onClick = onSave) {
                        Text("Save")
                    }
                    Button(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}


@Preview
@Composable
fun CreateNewGradeScaleDialogPreview() = AppTheme {
    CreateNewGradeScaleDialog(
        uiState = CreateGradeScaleUIState(
            existingGradeScaleNames = MockGradeScalesGenerator().gradeScales.map { gradeScale ->
                CreateGradeScaleUIState.GradeScaleNameAndId(
                    name = gradeScale.gradeScaleName,
                    id = gradeScale.id,
                )
            }.toImmutableList(),
            newName = "new grade scale",
            saveState = null,
        ),
    )
}
