package de.felixlf.gradingscale2.features.calculator

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.felixlf.gradingscale2.entities.features.calculator.CalculatorUIEvent
import de.felixlf.gradingscale2.entities.features.calculator.GradeScaleCalculatorUIState
import de.felixlf.gradingscale2.entities.util.MockGradeScalesGenerator
import de.felixlf.gradingscale2.features.calculator.components.CalculatorContent
import de.felixlf.gradingscale2.features.calculator.components.EmptyScalePlaceholder
import de.felixlf.gradingscale2.theme.AppTheme
import de.felixlf.gradingscale2.theme.LocalHazeState
import de.felixlf.gradingscale2.uicomponents.AdaptiveGradeScaleSelector
import de.felixlf.gradingscale2.uicomponents.GradeScaleSelectorDropdown
import dev.chrisbanes.haze.hazeSource
import kotlinx.collections.immutable.toImmutableList
import org.koin.compose.viewmodel.koinViewModel

/**
 * The Calculator Screen allows users to calculate grades bidirectionally
 * across total points, earned points, percentage, and grade letters.
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
        onSelectGradeScale = { viewModel.sendCommand(CalculatorUIEvent.SelectGradeScale(it)) },
        onSetTotalPoints = { viewModel.sendCommand(CalculatorUIEvent.SetTotalPoints(it)) },
        onSetPoints = { viewModel.sendCommand(CalculatorUIEvent.SetPoints(it)) },
        onSetPercentage = { viewModel.sendCommand(CalculatorUIEvent.SetPercentage(it)) },
        onSelectGradeName = { viewModel.sendCommand(CalculatorUIEvent.SetGradeName(it)) },
    )
}

@Composable
internal fun GradeScaleCalculatorScreen(
    modifier: Modifier = Modifier,
    uiState: GradeScaleCalculatorUIState,
    onSelectGradeScale: (String) -> Unit = {},
    onSetTotalPoints: (Double) -> Unit = {},
    onSetPoints: (Double) -> Unit = {},
    onSetPercentage: (Double) -> Unit = {},
    onSelectGradeName: (String) -> Unit = {},
) {
    AdaptiveGradeScaleSelector(
        items = uiState.gradeScaleItems,
        selectedItemId = uiState.selectedGradeScaleId,
        onSelectionChange = { id ->
            id?.let {
                val selectedName = uiState.gradeScalesNamesWithId.find { scale -> scale.gradeScaleId == id }?.gradeScaleName
                selectedName?.let { onSelectGradeScale(it) }
            }
        },
    ) { isListPaneVisible ->
        Column(
            modifier = modifier
                .hazeSource(LocalHazeState.current)
                .fillMaxSize()
                .padding(16.dp),
        ) {
            // Dropdown selector visible only on compact screens (where list pane is hidden)
            if (!isListPaneVisible) {
                GradeScaleSelectorDropdown(
                    items = uiState.gradeScaleItems,
                    selectedItemId = uiState.selectedGradeScaleId,
                    onSelectionChange = { id ->
                        id?.let {
                            val selectedName = uiState.gradeScalesNamesWithId.find { scale -> scale.gradeScaleId == id }?.gradeScaleName
                            selectedName?.let { onSelectGradeScale(it) }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (!uiState.hasSelectedGradeScale) {
                EmptyScalePlaceholder()
            } else {
                CalculatorContent(
                    uiState = uiState,
                    onSetTotalPoints = onSetTotalPoints,
                    onSetPoints = onSetPoints,
                    onSetPercentage = onSetPercentage,
                    onSelectGradeName = onSelectGradeName,
                )
            }
        }
    }
}

@Preview
@Composable
private fun CalculatorScreenPreview() = AppTheme {
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
            currentPercentage = 0.85,
            totalPoints = 100.0,
        ),
    )
}
