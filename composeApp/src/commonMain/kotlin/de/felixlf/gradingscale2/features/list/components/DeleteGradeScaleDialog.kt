package de.felixlf.gradingscale2.features.list.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.felixlf.gradingscale2.theme.AppTheme
import gradingscale2.entities.generated.resources.Res
import gradingscale2.entities.generated.resources.gradescale_list_dialog_delete_confirm
import gradingscale2.entities.generated.resources.gradescale_list_dialog_delete_message
import gradingscale2.entities.generated.resources.gradescale_list_dialog_delete_title
import gradingscale2.entities.generated.resources.gradescale_list_dialog_edit_cancel
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun DeleteGradeScaleDialogPreview() = AppTheme {
    DeleteGradeScaleDialog(
        gradeScaleName = "Standard (A-F)",
        onConfirm = {},
        onDismiss = {},
    )
}

/**
 * Confirmation dialog before permanently deleting a grade scale.
 */
@Composable
fun DeleteGradeScaleDialog(
    gradeScaleName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )
        },
        title = {
            Text(
                text = stringResource(Res.string.gradescale_list_dialog_delete_title),
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Text(
                text = stringResource(Res.string.gradescale_list_dialog_delete_message),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                ),
            ) {
                Text(stringResource(Res.string.gradescale_list_dialog_delete_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.gradescale_list_dialog_edit_cancel))
            }
        },
    )
}
