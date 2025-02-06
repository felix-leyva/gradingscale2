package de.felixlf.gradingscale2.features.list.upsertgradescaledialog

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
import androidx.compose.runtime.LaunchedEffect
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
internal fun UpsertGradeScaleDialog(
    viewModel: UpsertGradeScaleViewModel = dialogScopedViewModel<UpsertGradeScaleViewModel>(),
    onDismiss: () -> Unit = {},
    currentGradeScaleId: String? = null,
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(currentGradeScaleId) {
        if (currentGradeScaleId != null) {
            viewModel.onEvent(UpserGradeScaleUIEvent.SetCurrentGradeScaleId(currentGradeScaleId))
        }
    }

    // TODO: add into resources
    val defaultGradeName = "new grade"
    Dialog(onDismissRequest = onDismiss) {
        UpsertGradeScaleDialog(
            uiState = uiState.value,
            onSetNewName = { viewModel.onEvent(UpserGradeScaleUIEvent.SetNewName(it)) },
            onSave = {
                viewModel.onEvent(UpserGradeScaleUIEvent.Save(defaultGradeName))
                onDismiss()
            },
            onDismiss = onDismiss,
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
            if (uiState.saveState == UpsertGradeScaleUIState.State.Loading) {
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
    UpsertGradeScaleDialog(
        uiState = UpsertGradeScaleUIState(
            existingGradeScaleNames = MockGradeScalesGenerator().gradeScales.map { gradeScale ->
                UpsertGradeScaleUIState.GradeScaleNameAndId(
                    name = gradeScale.gradeScaleName,
                    id = gradeScale.id,
                )
            }.toImmutableList(),
            newName = "new grade scale",
            saveState = null,
        ),
    )
}
