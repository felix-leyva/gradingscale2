package de.felixlf.gradingscale2.features.calculator.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.selectAll
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import de.felixlf.gradingscale2.entities.features.calculator.GradeScaleCalculatorUIState
import de.felixlf.gradingscale2.entities.util.MockGradeScalesGenerator
import de.felixlf.gradingscale2.theme.AppTheme
import de.felixlf.gradingscale2.utils.textFieldManager
import kotlinx.collections.immutable.toImmutableList

/**
 * Adaptive content layout for the calculator:
 * Displays side-by-side hero and parameters cards on wide displays (>= 720dp),
 * and vertical stacked cards on compact mobile screens.
 */
@Composable
fun CalculatorContent(
    uiState: GradeScaleCalculatorUIState,
    onSetTotalPoints: (Double) -> Unit,
    onSetPoints: (Double) -> Unit,
    onSetPercentage: (Double) -> Unit,
    onSelectGradeName: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val totalPointsFocusRequester = remember { FocusRequester() }
    val pointsFocusRequester = remember { FocusRequester() }
    val percentageFocusRequester = remember { FocusRequester() }

    val totalPointsState = textFieldManager(uiState.totalPointsString) {
        onSetTotalPoints(it.toDoubleOrNull() ?: 1.0)
        edit { selectAll() }
    }

    val pointState = textFieldManager(uiState.earnedPointsString) {
        onSetPoints(it.toDoubleOrNull() ?: 0.0)
        edit { selectAll() }
    }

    val percentageState = textFieldManager(uiState.percentageString) {
        onSetPercentage((it.toDoubleOrNull()?.div(100)) ?: 0.0)
        edit { selectAll() }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isExpandedLayout = maxWidth >= 720.dp

        if (isExpandedLayout) {
            // Adaptive wide layout: Hero card on the left, Parameters card on the right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    HeroResultCard(
                        uiState = uiState,
                        onSetPoints = onSetPoints,
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    CalculatorParametersCard(
                        totalPointsState = totalPointsState,
                        pointState = pointState,
                        percentageState = percentageState,
                        availableGradeNames = uiState.availableGradeNames,
                        selectedGradeName = uiState.gradeName,
                        onSelectGradeName = onSelectGradeName,
                        totalPointsFocusRequester = totalPointsFocusRequester,
                        pointsFocusRequester = pointsFocusRequester,
                        percentageFocusRequester = percentageFocusRequester,
                    )
                }
            }
        } else {
            // Compact mobile layout: Stacked vertically in scrollable container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                HeroResultCard(
                    uiState = uiState,
                    onSetPoints = onSetPoints,
                )

                CalculatorParametersCard(
                    totalPointsState = totalPointsState,
                    pointState = pointState,
                    percentageState = percentageState,
                    availableGradeNames = uiState.availableGradeNames,
                    selectedGradeName = uiState.gradeName,
                    onSelectGradeName = onSelectGradeName,
                    totalPointsFocusRequester = totalPointsFocusRequester,
                    pointsFocusRequester = pointsFocusRequester,
                    percentageFocusRequester = percentageFocusRequester,
                )
            }
        }
    }
}

@Preview
@Composable
private fun CalculatorContentPreview() = AppTheme {
    CalculatorContent(
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
        onSetTotalPoints = {},
        onSetPoints = {},
        onSetPercentage = {},
        onSelectGradeName = {},
    )
}
