package de.felixlf.gradingscale2.features.weightedgradecalculator.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.dialog.WeightedGradeDialogCommand
import de.felixlf.gradingscale2.entities.models.weightedgrade.WeightedGrade
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.features.calculator.CalculatorTextField
import de.felixlf.gradingscale2.uicomponents.DropboxSelector
import de.felixlf.gradingscale2.utils.textFieldManager
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeEditDialog(
    grade: WeightedGrade,
    gradeScale: GradeScale,
    onSave: (WeightedGrade) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val viewModel = koinViewModel<WeightedGradeDialogViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(grade, gradeScale) {
        viewModel.sendCommand(
            WeightedGradeDialogCommand.Init(
                gradeScale = gradeScale,
                weightedGrade = grade,
            ),
        )
    }

    val percentageTextField = textFieldManager(uiState.percentageString) {
        viewModel.sendCommand(WeightedGradeDialogCommand.SetPercentage(it))
    }
    val weightTextField = textFieldManager(uiState.weightString) {
        viewModel.sendCommand(WeightedGradeDialogCommand.SetWeight(it))
    }
    val relativeWeightTextField = textFieldManager(uiState.relativeWeightString) {
        viewModel.sendCommand(WeightedGradeDialogCommand.SetRelativeWeight(it))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Grade") },
        text = {
            Column {
                DropboxSelector(
                    modifier = Modifier.fillMaxWidth(),
                    label = "Name of grade",
                    elements = gradeScale.gradesNamesList,
                    selectedElement = uiState.gradeNameString,
                    onSelectElement = { selectedGrade ->
                        viewModel.sendCommand(WeightedGradeDialogCommand.SelectGradeName(selectedGrade))
                    },
                )

                // Percentage field
                CalculatorTextField(
                    state = percentageTextField,
                    label = "Percentage",
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                )

                // Points field
                CalculatorTextField(
                    state = relativeWeightTextField,
                    label = "Points",
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                )

                // Total points field
                CalculatorTextField(
                    state = weightTextField,
                    label = "Total points",
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val percentage = (percentageTextField.text.toString().toDoubleOrNull() ?: 0.0) / 100
                    val weight = weightTextField.text.toString().toDoubleOrNull() ?: 0.0

                    onSave(
                        WeightedGrade(
                            percentage = percentage,
                            weight = weight,
                        ),
                    )
                },
            ) {
                Text(
                    text = "SAVE",
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }

            Column {
                TextButton(
                    onClick = {
                        onDelete()
                        onDismiss()
                    },
                    content = {
                        Text(
                            text = "DELETE",
                            color = MaterialTheme.colorScheme.error,
                        )
                    },
                )
            }
        },
    )
}
