package de.felixlf.gradingscale2.features.list.upsertgradedialog

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.utils.dialogScopedViewModel
import de.felixlf.gradingscale2.utils.textFieldManager
import gradingscale2.composeapp.generated.resources.Res
import gradingscale2.composeapp.generated.resources.edit_grade_name
import gradingscale2.composeapp.generated.resources.edit_grade_percentage
import gradingscale2.composeapp.generated.resources.edit_grade_save_button
import kotlinx.collections.immutable.persistentSetOf
import org.jetbrains.compose.resources.stringResource

/**
 * The Insert Grade Dialog is a dialog that allows the user to insert a new grade.
 */
@Composable
fun InsertGradeDialog(gradeScaleId: String, onDismiss: () -> Unit) {
    val viewModel = dialogScopedViewModel<UpsertGradeViewModel>()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(gradeScaleId) { viewModel.onEvent(UpsertGradeUIEvent.SetGradeScaleId(gradeScaleId)) }
    UpsertGradeDialog(onDismiss = onDismiss, uiState = uiState, viewModel = viewModel)
}

/**
 * The Edit Grade Dialog is a dialog that allows the user to edit a grade.
 */
@Composable
fun EditGradeDialog(uuid: String, onDismiss: () -> Unit) {
    val viewModel = dialogScopedViewModel<UpsertGradeViewModel>()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(uuid) { viewModel.onEvent(UpsertGradeUIEvent.SetGradeUUID(uuid)) }
    UpsertGradeDialog(onDismiss = onDismiss, uiState = uiState, viewModel = viewModel)
}

@Composable
private fun UpsertGradeDialog(
    onDismiss: () -> Unit, uiState: State<UpsertGradeUIState>, viewModel: UpsertGradeViewModel
) {
    Dialog(onDismissRequest = onDismiss) {
        UpsertGradeDialog(
            uiState = uiState.value,
            onSetPercentage = { viewModel.onEvent(UpsertGradeUIEvent.SetPercentage(it)) },
            onSetName = { viewModel.onEvent(UpsertGradeUIEvent.SetGradeName(it)) },
            onSave = {
                viewModel.onEvent(UpsertGradeUIEvent.Save)
                onDismiss()
            },
        )
    }
}

@Composable
private fun UpsertGradeDialog(
    uiState: UpsertGradeUIState,
    onSetPercentage: (String) -> Unit = {},
    onSetName: (String) -> Unit = {},
    onSave: () -> Unit = {},
) {
    Card {
        Column(
            modifier = Modifier.padding(24.dp).width(IntrinsicSize.Max),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(32.dp))
                return@Card
            }

            EditGradeTextField(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                value = uiState.name ?: "",
                onValueChange = onSetName,
                label = stringResource(Res.string.edit_grade_name),
                error = uiState.error.any { it == UpsertGradeUIState.Error.INVALID_NAME || it == UpsertGradeUIState.Error.DUPLICATE_NAME },
            )
            EditGradeTextField(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                value = uiState.percentage ?: "",
                onValueChange = onSetPercentage,
                label = stringResource(Res.string.edit_grade_percentage),
                error = uiState.error.any { it == UpsertGradeUIState.Error.INVALID_PERCENTAGE || it == UpsertGradeUIState.Error.DUPLICATE_PERCENTAGE },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
            // TODO: update with a fixed space between the buttons and replace the text with a string resource
            uiState.error.joinToString { it.name }.ifBlank { null }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                )
            } ?: with(
                LocalDensity.current,
            ) {
                Spacer(modifier = Modifier.requiredHeight(MaterialTheme.typography.bodyMedium.lineHeight.toDp()).padding(bottom = 16.dp))
            }

            Button(
                onClick = onSave,
                enabled = uiState.isSaveButtonEnabled,
            ) {
                Text(stringResource(Res.string.edit_grade_save_button))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditGradeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: Boolean,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    val textFieldValue = textFieldManager(value) { onValueChange(it) }
    BasicTextField(
        modifier = modifier,
        state = textFieldValue,
        textStyle = TextStyle.Default.copy(color = MaterialTheme.colorScheme.primary),
        lineLimits = TextFieldLineLimits.SingleLine,
        keyboardOptions = keyboardOptions,
        decorator = @Composable { textField ->
            OutlinedTextFieldDefaults.DecorationBox(
                value = textFieldValue.text.toString(),
                label = { Text(label) },
                innerTextField = textField,
                enabled = true,
                singleLine = true,
                interactionSource = MutableInteractionSource(),
                visualTransformation = VisualTransformation.None,
                isError = error,
            )
        },
    )
}

@Preview
@Composable
private fun EditGradeDialogPreview() {
    UpsertGradeDialog(
        uiState = UpsertGradeUIState(
            name = "Test",
            percentage = "50",
            error = persistentSetOf(),
            grade = Grade(
                namedGrade = "A",
                percentage = 0.5,
                idOfGradeScale = "GradeScaleId",
                nameOfScale = "Test",
                uuid = "GradeUUID",
            ),
            gradeScale = null,
        ),
    )
}
