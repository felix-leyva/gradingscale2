package de.felixlf.gradingscale2.features.list.editgradedialog

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.felixlf.gradingscale2.utils.dialogScopedViewModel
import de.felixlf.gradingscale2.utils.textFieldManager
import gradingscale2.composeapp.generated.resources.Res
import gradingscale2.composeapp.generated.resources.edit_grade_name
import gradingscale2.composeapp.generated.resources.edit_grade_percentage
import gradingscale2.composeapp.generated.resources.edit_grade_save_button
import kotlinx.collections.immutable.persistentSetOf
import org.jetbrains.compose.resources.stringResource

/**
 * The Edit Grade Dialog is a dialog that allows the user to edit a grade.
 */
@Composable
fun EditGradeDialog(uuid: String, onDismiss: () -> Unit) {
    val viewModel = dialogScopedViewModel<EditGradeViewModel>()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(uuid) { viewModel.setGradeUUID(uuid) }

    Dialog(onDismissRequest = onDismiss) {
        EditGradeDialog(
            uiState = uiState.value,
            onSetPercentage = viewModel::setPercentage,
            onSetName = viewModel::setGradeName,
            onSave = {
                viewModel.updateGrade()
                onDismiss()
            },
        )
    }
}

@Composable
private fun EditGradeDialog(
    uiState: EditGradeUIState,
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
                error = uiState.error.contains(EditGradeUIState.Error.INVALID_NAME),
            )
            EditGradeTextField(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                value = uiState.percentage ?: "",
                onValueChange = onSetPercentage,
                label = stringResource(Res.string.edit_grade_percentage),
                error = uiState.error.contains(EditGradeUIState.Error.INVALID_PERCENTAGE),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
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
    EditGradeDialog(
        uiState = EditGradeUIState(
            name = "Test",
            percentage = "50",
            error = persistentSetOf(),
            grade = null,
        ),
    )
}
