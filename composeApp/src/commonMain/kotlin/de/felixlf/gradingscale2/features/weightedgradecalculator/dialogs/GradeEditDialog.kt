package de.felixlf.gradingscale2.features.weightedgradecalculator.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.dialog.WeightedGradeDialogCommand
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.dialog.WeightedGradeDialogUIState
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.models.weightedgrade.WeightedGrade
import de.felixlf.gradingscale2.features.calculator.CalculatorTextField
import de.felixlf.gradingscale2.uicomponents.DropboxSelector
import de.felixlf.gradingscale2.utils.dialogScopedViewModel
import de.felixlf.gradingscale2.utils.textFieldManager
import gradingscale2.entities.generated.resources.Res
import gradingscale2.entities.generated.resources.weighted_grade_dialog_cancel
import gradingscale2.entities.generated.resources.weighted_grade_dialog_delete
import gradingscale2.entities.generated.resources.weighted_grade_dialog_grade_name
import gradingscale2.entities.generated.resources.weighted_grade_dialog_percentage
import gradingscale2.entities.generated.resources.weighted_grade_dialog_points
import gradingscale2.entities.generated.resources.weighted_grade_dialog_save
import gradingscale2.entities.generated.resources.weighted_grade_dialog_title
import gradingscale2.entities.generated.resources.weighted_grade_dialog_total_points
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeEditDialog(
    grade: WeightedGrade,
    gradeScale: GradeScale,
    onSave: (WeightedGrade) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    isNewGrade: Boolean = false,
) {
    val viewModel = dialogScopedViewModel<WeightedGradeDialogViewModelWithEvents>()
    val uiState by viewModel.uiState.collectAsState()
    var hasSubmitted by remember(grade.uuid) { mutableStateOf(false) }

    LaunchedEffect(grade, gradeScale) {
        viewModel.sendCommand(
            WeightedGradeDialogCommand.Init(
                gradeScale = gradeScale,
                weightedGrade = grade,
            ),
        )
    }

    val percentageTextField = textFieldManager(
        externalFieldValue = uiState.percentageString,
        delay = 0L,
        isNumeric = true,
    ) {
        viewModel.sendCommand(WeightedGradeDialogCommand.SetPercentage(it))
    }
    val weightTextField = textFieldManager(
        externalFieldValue = uiState.weightString,
        delay = 0L,
        isNumeric = true,
    ) {
        viewModel.sendCommand(WeightedGradeDialogCommand.SetWeight(it))
    }
    val relativeWeightTextField = textFieldManager(
        externalFieldValue = uiState.relativeWeightString,
        delay = 0L,
        isNumeric = true,
    ) {
        viewModel.sendCommand(WeightedGradeDialogCommand.SetRelativeWeight(it))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.weighted_grade_dialog_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
            ) {
                DropboxSelector(
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(Res.string.weighted_grade_dialog_grade_name),
                    elements = gradeScale.gradesNamesList,
                    selectedElement = uiState.gradeNameString,
                    onSelectElement = { selectedGrade ->
                        viewModel.sendCommand(WeightedGradeDialogCommand.SelectGradeName(selectedGrade))
                    },
                )

                // Percentage field
                CalculatorTextField(
                    state = percentageTextField,
                    label = stringResource(Res.string.weighted_grade_dialog_percentage),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                )

                // Points field
                CalculatorTextField(
                    state = relativeWeightTextField,
                    label = stringResource(Res.string.weighted_grade_dialog_points),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                )

                // Total points field
                CalculatorTextField(
                    state = weightTextField,
                    label = stringResource(Res.string.weighted_grade_dialog_total_points),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (!hasSubmitted) {
                        hasSubmitted = true
                        onSave(resolveWeightedGradeForSave(uiState = uiState, fallbackGrade = grade))
                    }
                },
            ) {
                Text(
                    text = stringResource(Res.string.weighted_grade_dialog_save),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.weighted_grade_dialog_cancel))
            }

            if (!isNewGrade) {
                Column {
                    TextButton(
                        onClick = {
                            onDelete()
                            onDismiss()
                        },
                        content = {
                            Text(
                                text = stringResource(Res.string.weighted_grade_dialog_delete),
                                color = MaterialTheme.colorScheme.error,
                            )
                        },
                    )
                }
            }
        },
    )
}

internal fun resolveWeightedGradeForSave(
    uiState: WeightedGradeDialogUIState,
    fallbackGrade: WeightedGrade,
): WeightedGrade {
    val safePercentage = (uiState.percentage ?: fallbackGrade.percentage)
        .takeIf { it.isFinite() }
        ?.coerceIn(0.0, 1.0)
        ?: fallbackGrade.percentage.coerceIn(0.0, 1.0)
    val safeWeight = (uiState.weight ?: fallbackGrade.weight)
        .takeIf { it.isFinite() && it >= 0.01 }
        ?: fallbackGrade.weight.takeIf { it.isFinite() && it >= 0.01 }
        ?: 1.0
    return WeightedGrade(
        percentage = safePercentage,
        weight = safeWeight,
        uuid = fallbackGrade.uuid,
    )
}
