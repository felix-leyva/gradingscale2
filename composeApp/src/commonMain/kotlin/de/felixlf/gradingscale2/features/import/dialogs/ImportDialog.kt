package de.felixlf.gradingscale2.features.import.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.felixlf.gradingscale2.entities.models.remote.GradeScaleDTO
import gradingscale2.entities.generated.resources.Res
import gradingscale2.entities.generated.resources.import_dialog_no_button
import gradingscale2.entities.generated.resources.import_dialog_title
import gradingscale2.entities.generated.resources.import_dialog_yes_button
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ImportDialog(
    gradeScale: GradeScaleDTO,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.import_dialog_title)) },
        text = {
            Column {
                Text(
                    text = "${gradeScale.country}: ${gradeScale.gradeScaleName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    gradeScale.grades.forEach { grade ->
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                            ) {
                                Text(
                                    text = grade.gradeName,
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    text = "${(grade.percentage * 100).toInt()}%",
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(Res.string.import_dialog_yes_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.import_dialog_no_button))
            }
        },
    )
}
