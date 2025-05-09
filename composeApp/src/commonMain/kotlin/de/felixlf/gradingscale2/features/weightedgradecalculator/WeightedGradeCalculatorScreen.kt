package de.felixlf.gradingscale2.features.weightedgradecalculator

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.WeightCalculatorUIState
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.WeightedCalculatorCommand
import de.felixlf.gradingscale2.entities.models.weightedgrade.WeightedGrade
import de.felixlf.gradingscale2.features.weightedgradecalculator.components.GlobalGradeSummary
import de.felixlf.gradingscale2.features.weightedgradecalculator.components.PartialGradesList
import de.felixlf.gradingscale2.features.weightedgradecalculator.dialogs.GradeEditDialog
import de.felixlf.gradingscale2.uicomponents.DropboxSelector
import de.felixlf.gradingscale2.uicomponents.LoadingContent
import gradingscale2.entities.generated.resources.Res
import gradingscale2.entities.generated.resources.add_grade
import gradingscale2.entities.generated.resources.weighted_calculator_selected_grade_scale
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import kotlin.uuid.Uuid

@Composable
internal fun WeightedGradeCalculatorScreen() {
    val viewModel: WeightedCalculatorViewModel = koinViewModel<WeightedCalculatorViewModel>()
    val uiState by viewModel.uiState.collectAsState()

    WeightedGradeCalculatorScreen(
        uiState = uiState,
        onSendCommand = viewModel::sendCommand,
    )
}

@Composable
fun WeightedGradeCalculatorScreen(
    uiState: WeightCalculatorUIState,
    onSendCommand: (WeightedCalculatorCommand) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> LoadingContent()
            else -> MainContent(
                uiState = uiState,
                onSendCommand = onSendCommand,
            )
        }
        if (uiState.selectedGradeScale != null) {
            FloatingActionButton(
                onClick = {
                    val newGrade = WeightedGrade(
                        percentage = 1.0,
                        weight = 1.0,
                        uuid = Uuid.random().toString(),
                    )
                    onSendCommand(WeightedCalculatorCommand.AddGradeAtPos(uiState.grades.size, newGrade))
                    onSendCommand(WeightedCalculatorCommand.SelectGrade(newGrade.uuid))
                },
                modifier = Modifier.align(Alignment.BottomEnd).padding(32.dp),
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(Res.string.add_grade),
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }

    // Show dialog for editing/adding grades
    uiState.selectedGrade?.let {
        if (uiState.selectedGradeScale == null) return@let
        GradeEditDialog(
            grade = it,
            gradeScale = uiState.selectedGradeScale!!,
            onSave = { updatedGrade ->
                val position = uiState.grades.indexOf(uiState.selectedGrade)
                if (position != -1) {
                    onSendCommand(WeightedCalculatorCommand.UpdateGrade(updatedGrade))
                } else {
                    onSendCommand(WeightedCalculatorCommand.AddGradeAtPos(0, updatedGrade))
                }
            },
            onDelete = {
                val position = uiState.grades.indexOf(uiState.selectedGrade)
                if (position != -1) {
                    onSendCommand(WeightedCalculatorCommand.RemoveGrade(it.uuid))
                }
            },
            onDismiss = {
                onSendCommand(WeightedCalculatorCommand.SelectGrade(null))
            },
        )
    }
}

@Composable
private fun MainContent(
    uiState: WeightCalculatorUIState,
    onSendCommand: (WeightedCalculatorCommand) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
    ) {
        // Grade scale selector dropdown
        DropboxSelector(
            elements = uiState.gradeScaleNameAndIds.map { it.name }.toImmutableList(),
            selectedElement = uiState.selectedGradeScale?.gradeScaleName,
            onSelectElement = { selectedName ->
                val selectedId = uiState.gradeScaleNameAndIds.find { it.name == selectedName }?.id
                onSendCommand(WeightedCalculatorCommand.SelectGradeScale(selectedId))
            },
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(Res.string.weighted_calculator_selected_grade_scale),
        )

        GlobalGradeSummary(
            weightedGradesSummary = uiState.weightedGradeSummary,
            modifier = Modifier.fillMaxWidth(),
        )

        // List of partial grades
        PartialGradesList(
            weightedGrades = uiState.weightedGrades,
            onGradeClick = { id ->
                onSendCommand(WeightedCalculatorCommand.SelectGrade(id))
            },
            modifier = Modifier.fillMaxWidth().weight(1f),
        )
    }
}

@Preview
@Composable
fun WeightedGradeCalculatorScreenPreview() {
    MaterialTheme {
        Surface {
            // Preview implementation with mock data would go here
        }
    }
}
