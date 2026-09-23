package de.felixlf.gradingscale2.features.weightedgradecalculator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.WeightCalculatorUIState
import de.felixlf.gradingscale2.entities.features.weightedgradecalculator.WeightedCalculatorCommand
import de.felixlf.gradingscale2.entities.models.weightedgrade.WeightedGrade
import de.felixlf.gradingscale2.features.weightedgradecalculator.components.EmptyWeightedGradesPlaceholder
import de.felixlf.gradingscale2.features.weightedgradecalculator.components.WeightedGradeItemCard
import de.felixlf.gradingscale2.features.weightedgradecalculator.components.WeightedHeroSummaryCard
import de.felixlf.gradingscale2.features.weightedgradecalculator.dialogs.GradeEditDialog
import de.felixlf.gradingscale2.theme.LocalHazeState
import de.felixlf.gradingscale2.uicomponents.AdaptiveGradeScaleSelector
import de.felixlf.gradingscale2.uicomponents.GradeScaleSelectorDropdown
import de.felixlf.gradingscale2.uicomponents.LoadingContent
import dev.chrisbanes.haze.hazeSource
import gradingscale2.entities.generated.resources.Res
import gradingscale2.entities.generated.resources.add_grade
import gradingscale2.entities.generated.resources.destinations_menu_weighted_grade_calculator
import gradingscale2.entities.generated.resources.weighted_calculator_add_grade
import gradingscale2.entities.generated.resources.weighted_grade_list_label_title
import kotlinx.collections.immutable.toPersistentList
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.uuid.Uuid

@Composable
internal fun WeightedGradeCalculatorScreen(
    modifier: Modifier = Modifier,
    viewModel: WeightedCalculatorViewModelWithEvents = koinViewModel<WeightedCalculatorViewModelWithEvents>(),
) {
    val uiState by viewModel.uiState.collectAsState()

    WeightedGradeCalculatorScreen(
        modifier = modifier,
        uiState = uiState,
        onSendCommand = viewModel::sendCommand,
    )
}

@Composable
fun WeightedGradeCalculatorScreen(
    modifier: Modifier = Modifier,
    uiState: WeightCalculatorUIState,
    onSendCommand: (WeightedCalculatorCommand) -> Unit,
) {
    val onAddGrade: () -> Unit = {
        val newGrade = WeightedGrade(
            percentage = 1.0,
            weight = 1.0,
            uuid = Uuid.random().toString(),
        )
        onSendCommand(WeightedCalculatorCommand.AddGradeAtPos(uiState.grades.size, newGrade))
        onSendCommand(WeightedCalculatorCommand.SelectGrade(newGrade.uuid))
    }

    Box(modifier = modifier.hazeSource(LocalHazeState.current).fillMaxSize()) {
        when {
            uiState.isLoading -> LoadingContent()
            else -> MainContent(
                uiState = uiState,
                onSendCommand = onSendCommand,
                onAddGrade = onAddGrade,
            )
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
    onAddGrade: () -> Unit,
) {
    val gradeScaleItems = uiState.gradeScaleNameAndIds.toPersistentList()

    AdaptiveGradeScaleSelector(
        items = gradeScaleItems,
        selectedItemId = uiState.selectedGradeScale?.id,
        onSelectionChange = { id ->
            onSendCommand(WeightedCalculatorCommand.SelectGradeScale(id))
        },
    ) { isListPaneVisible ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (!isListPaneVisible) {
                // Mobile compact portrait layout
                MobileWeightedCalculatorContent(
                    uiState = uiState,
                    onSendCommand = onSendCommand,
                    onAddGrade = onAddGrade,
                )

                if (uiState.selectedGradeScale != null) {
                    FloatingActionButton(
                        onClick = onAddGrade,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(24.dp),
                        containerColor = MaterialTheme.colorScheme.primary,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(Res.string.add_grade),
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
            } else {
                // Expanded tablet / desktop landscape layout
                ExpandedWeightedCalculatorContent(
                    uiState = uiState,
                    onSendCommand = onSendCommand,
                    onAddGrade = onAddGrade,
                )
            }
        }
    }
}

@Composable
private fun MobileWeightedCalculatorContent(
    uiState: WeightCalculatorUIState,
    onSendCommand: (WeightedCalculatorCommand) -> Unit,
    onAddGrade: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gradeScaleItems = uiState.gradeScaleNameAndIds.toPersistentList()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Dropdown scale selector
        item(key = "scale_selector") {
            GradeScaleSelectorDropdown(
                items = gradeScaleItems,
                selectedItemId = uiState.selectedGradeScale?.id,
                onSelectionChange = { id ->
                    onSendCommand(WeightedCalculatorCommand.SelectGradeScale(id))
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Hero Donut Summary Card
        item(key = "hero_summary") {
            WeightedHeroSummaryCard(
                weightedGradeSummary = uiState.weightedGradeSummary,
                weightedGrades = uiState.weightedGrades,
                totalWeight = uiState.totalWeight,
                scaleName = uiState.selectedGradeScale?.gradeScaleName,
            )
        }

        // Partial Grades Header
        item(key = "grades_header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.weighted_grade_list_label_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (uiState.weightedGrades.isNotEmpty()) {
                    Text(
                        text = "${uiState.weightedGrades.size} items",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }
        }

        // Empty placeholder or Partial Grades Cards
        if (uiState.weightedGrades.isEmpty()) {
            item(key = "empty_placeholder") {
                EmptyWeightedGradesPlaceholder(
                    onAddGrade = onAddGrade,
                )
            }
        } else {
            items(
                items = uiState.weightedGrades,
                key = { it.grade.uuid },
            ) { item ->
                WeightedGradeItemCard(
                    item = item,
                    totalWeight = uiState.totalWeight,
                    onEditClick = {
                        onSendCommand(WeightedCalculatorCommand.SelectGrade(item.grade.uuid))
                    },
                    onDeleteClick = {
                        onSendCommand(WeightedCalculatorCommand.RemoveGrade(item.grade.uuid))
                    },
                )
            }
        }
    }
}

@Composable
private fun ExpandedWeightedCalculatorContent(
    uiState: WeightCalculatorUIState,
    onSendCommand: (WeightedCalculatorCommand) -> Unit,
    onAddGrade: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 300.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 96.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Panoramic Header with Title + Add Grade button (spanning full width)
        item(span = { GridItemSpan(maxLineSpan) }, key = "expanded_header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = stringResource(Res.string.destinations_menu_weighted_grade_calculator),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    uiState.selectedGradeScale?.let {
                        Text(
                            text = it.gradeScaleName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                if (uiState.selectedGradeScale != null) {
                    FilledTonalButton(
                        onClick = onAddGrade,
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(Res.string.weighted_calculator_add_grade))
                    }
                }
            }
        }

        // Hero Donut Summary Card (spanning full width)
        item(span = { GridItemSpan(maxLineSpan) }, key = "expanded_hero") {
            WeightedHeroSummaryCard(
                weightedGradeSummary = uiState.weightedGradeSummary,
                weightedGrades = uiState.weightedGrades,
                totalWeight = uiState.totalWeight,
                scaleName = null,
            )
        }

        // Section Title (spanning full width)
        item(span = { GridItemSpan(maxLineSpan) }, key = "grades_header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.weighted_grade_list_label_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (uiState.weightedGrades.isNotEmpty()) {
                    Text(
                        text = "${uiState.weightedGrades.size} items • Total weight: ${uiState.totalWeight}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }
        }

        // Empty placeholder or Grid of Cards
        if (uiState.weightedGrades.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }, key = "empty_placeholder") {
                EmptyWeightedGradesPlaceholder(
                    onAddGrade = onAddGrade,
                )
            }
        } else {
            items(
                items = uiState.weightedGrades,
                key = { it.grade.uuid },
            ) { item ->
                WeightedGradeItemCard(
                    item = item,
                    totalWeight = uiState.totalWeight,
                    onEditClick = {
                        onSendCommand(WeightedCalculatorCommand.SelectGrade(item.grade.uuid))
                    },
                    onDeleteClick = {
                        onSendCommand(WeightedCalculatorCommand.RemoveGrade(item.grade.uuid))
                    },
                )
            }
        }
    }
}
