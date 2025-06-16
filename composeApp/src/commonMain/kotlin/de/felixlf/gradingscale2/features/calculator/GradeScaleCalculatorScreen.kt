package de.felixlf.gradingscale2.features.calculator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.felixlf.gradingscale2.entities.features.calculator.CalculatorUIEvent
import de.felixlf.gradingscale2.entities.features.calculator.GradeScaleCalculatorUIState
import de.felixlf.gradingscale2.entities.models.GradeScaleNameAndId
import de.felixlf.gradingscale2.entities.util.MockGradeScalesGenerator
import de.felixlf.gradingscale2.entities.util.stringWithDecimals
import de.felixlf.gradingscale2.theme.LocalHazeState
import de.felixlf.gradingscale2.uicomponents.AdaptiveGradeScaleSelector
import de.felixlf.gradingscale2.uicomponents.DropboxSelector
import de.felixlf.gradingscale2.uicomponents.GradeScaleSelectorDropdown
import de.felixlf.gradingscale2.utils.isLargeScreenWidthLocal
import de.felixlf.gradingscale2.utils.textFieldManager
import dev.chrisbanes.haze.hazeSource
import gradingscale2.entities.generated.resources.Res
import gradingscale2.entities.generated.resources.calculator_screen_grade_name_dropbox_default
import gradingscale2.entities.generated.resources.calculator_screen_grade_name_dropbox_label
import gradingscale2.entities.generated.resources.calculator_screen_percentage_input
import gradingscale2.entities.generated.resources.calculator_screen_points_input
import gradingscale2.entities.generated.resources.calculator_screen_total_points_input
import gradingscale2.entities.generated.resources.gradescale_list_select_grade_scale
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

/**
 * The Calculator Screen is the main screen of the calculator feature. It allows the user to calculate the grade of a student based on the
 * selected grade scale.
 */
@Composable
internal fun GradeScaleCalculatorScreen(
    modifier: Modifier = Modifier,
    viewModel: CalculatorViewModel = koinViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    GradeScaleCalculatorScreen(
        modifier = modifier,
        uiState = uiState.value,
        onSelectGradeScale = { viewModel.onEvent(CalculatorUIEvent.SelectGradeScale(it)) },
        onSetTotalPoints = { viewModel.onEvent(CalculatorUIEvent.SetTotalPoints(it)) },
        onSetPoints = { viewModel.onEvent(CalculatorUIEvent.SetPoints(it)) },
        onSetPercentage = { viewModel.onEvent(CalculatorUIEvent.SetPercentage(it)) },
        onSelectGradeName = { viewModel.onEvent(CalculatorUIEvent.SetGradeName(it)) },
    )
}

@Composable
private fun GradeScaleCalculatorScreen(
    modifier: Modifier = Modifier,
    uiState: GradeScaleCalculatorUIState,
    onSelectGradeScale: (String) -> Unit = {},
    onSetTotalPoints: (Double) -> Unit = {},
    onSetPoints: (Double) -> Unit = {},
    onSetPercentage: (Double) -> Unit = {},
    onSelectGradeName: (String) -> Unit = {},
) {
    val gradeScale = remember(uiState.selectedGradeScale) { uiState.selectedGradeScale }
    val isLargeScreen by isLargeScreenWidthLocal()

    // Convert UI state to GradeScaleNameAndId list for the adaptive selector
    val gradeScaleItems = remember(uiState.gradeScalesNamesWithId) {
        uiState.gradeScalesNamesWithId.map {
            GradeScaleNameAndId(id = it.gradeScaleId, name = it.gradeScaleName)
        }.toPersistentList()
    }
    AdaptiveGradeScaleSelector(
        items = gradeScaleItems,
        selectedItemId = uiState.selectedGradeScale?.id,
        onSelectionChange = { id ->
            id?.let {
                val selectedName = uiState.gradeScalesNamesWithId.find { it.gradeScaleId == id }?.gradeScaleName
                selectedName?.let { onSelectGradeScale(it) }
            }
        },
    ) {
        Column(
            modifier = modifier.hazeSource(LocalHazeState.current).fillMaxSize().padding(16.dp),
        ) {
            // Only show dropdown on non-large screens
            GradeScaleSelectorDropdown(
                items = gradeScaleItems,
                selectedItemId = uiState.selectedGradeScale?.id,
                onSelectionChange = { id ->
                    id?.let {
                        val selectedName = uiState.gradeScalesNamesWithId.find { it.gradeScaleId == id }?.gradeScaleName
                        selectedName?.let { onSelectGradeScale(it) }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            if (!isLargeScreen) {
                HorizontalDivider()
            }

            if (gradeScale == null) {
                Text(text = stringResource(Res.string.gradescale_list_select_grade_scale))
            } else {
                Column(
                    modifier = Modifier.wrapContentWidth(),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        val totalPointsState = textFieldManager(uiState.totalPoints?.stringWithDecimals() ?: "") {
                            onSetTotalPoints(it.toDoubleOrNull() ?: 1.0)
                        }

                        CalculatorTextField(
                            modifier = Modifier.padding(vertical = 16.dp).weight(1f),
                            state = totalPointsState,
                            label = stringResource(Res.string.calculator_screen_total_points_input),
                        )

                        val pointState = textFieldManager(uiState.currentGrade?.points?.stringWithDecimals() ?: "") {
                            onSetPoints(it.toDoubleOrNull() ?: 0.0)
                        }

                        CalculatorTextField(
                            modifier = Modifier.padding(vertical = 16.dp).weight(1f),
                            state = pointState,
                            label = stringResource(Res.string.calculator_screen_points_input),
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        val percentageState = textFieldManager((uiState.currentPercentage)?.times(100)?.stringWithDecimals() ?: "") {
                            onSetPercentage((it.toDoubleOrNull()?.div(100)) ?: 0.0)
                        }

                        CalculatorTextField(
                            modifier = Modifier.padding(vertical = 16.dp).weight(1f),
                            state = percentageState,
                            label = stringResource(Res.string.calculator_screen_percentage_input),
                        )

                        DropboxSelector(
                            elements = uiState.selectedGradeScale?.sortedGrades?.map { it.namedGrade }?.toImmutableList()
                                ?: persistentListOf(),
                            selectedElement = uiState.currentGrade?.namedGrade,
                            onSelectElement = onSelectGradeName,
                            defaultText = stringResource(Res.string.calculator_screen_grade_name_dropbox_default),
                            label = stringResource(Res.string.calculator_screen_grade_name_dropbox_label),
                            modifier = Modifier.padding(vertical = 16.dp).weight(1f),
                            textStyle = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun CalculatorScreenPreview() {
    GradeScaleCalculatorScreen(
        uiState = GradeScaleCalculatorUIState(
            selectedGradeScale = MockGradeScalesGenerator().gradeScales.first(),
            gradeScalesNamesWithId = MockGradeScalesGenerator()
                .gradeScales
                .map {
                    GradeScaleCalculatorUIState.GradeScaleNameWithId(
                        gradeScaleName = it.gradeScaleName,
                        gradeScaleId = it.id,
                    )
                }.toImmutableList(),
            currentPercentage = 0.8,
            totalPoints = 10.0,
        ),
    )
}
