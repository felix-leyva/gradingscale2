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

@Composable
internal fun ImportDialog(
    gradeScale: GradeScaleDTO,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import the selected grade scale?") },
        text = {
            Column {
                Text(
                    text = "${gradeScale.country}: ${gradeScale.gradeScaleName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Companion.Bold,
                )

                Spacer(modifier = Modifier.Companion.height(8.dp))
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    gradeScale.grades.forEach { grade ->
                        item {
                            Row(
                                modifier = Modifier.Companion
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                            ) {
                                Text(
                                    text = grade.gradeName,
                                    modifier = Modifier.Companion.weight(1f),
                                )
                                Text(
                                    text = "${(grade.percentage * 100).toInt()}%",
                                    modifier = Modifier.Companion.weight(1f),
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("YES")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("NO")
            }
        },
    )
}
