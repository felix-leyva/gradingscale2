package de.felixlf.gradingscale2.features.calculator

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.felixlf.gradingscale2.entities.util.MockGradeScalesGenerator
import de.felixlf.gradingscale2.features.list.components.GradeScaleDropboxSelector
import de.felixlf.gradingscale2.utils.stringWithDecimals
import de.felixlf.gradingscale2.utils.textFieldManager
import gradingscale2.composeapp.generated.resources.Res
import gradingscale2.composeapp.generated.resources.gradescale_list_select_grade_scale
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GradeScaleCalculatorScreen() {
    val viewModel: CalculatorViewModel = koinViewModel()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    GradeScaleCalculatorScreen(
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
    uiState: GradeScaleCalculatorUIState,
    onSelectGradeScale: (String) -> Unit = {},
    onSetTotalPoints: (Double) -> Unit = {},
    onSetPoints: (Double) -> Unit = {},
    onSetPercentage: (Double) -> Unit = {},
    onSelectGradeName: (String) -> Unit = {},
) {
    val gradeScale = remember(uiState.selectedGradeScale) { uiState.selectedGradeScale }
    Column(
        modifier = Modifier.fillMaxSize().background(color = MaterialTheme.colorScheme.onPrimary),
        verticalArrangement = Arrangement.Center
    ) {
        GradeScaleDropboxSelector(
            gradeScalesNames = uiState.gradeScalesNamesWithId.map { it.gradeScaleName }.toImmutableList(),
            selectedGradeScaleName = uiState.selectedGradeScale?.gradeScaleName,
            onSelectGradeScale = onSelectGradeScale,
            modifier = Modifier.weight(1f),
            defaultText = stringResource(Res.string.gradescale_list_select_grade_scale)
        )        
      
            val totalPointsState = textFieldManager(uiState.totalPoints?.stringWithDecimals() ?: "") {
                onSetTotalPoints(it.toDoubleOrNull() ?: 1.0)
            }

            BasicTextField(
                modifier = Modifier.weight(1f).padding(16.dp),
                state = totalPointsState,
            )
            
            val pointState = textFieldManager(uiState.currentGrade?.points?.stringWithDecimals() ?: "") {
                onSetPoints(it.toDoubleOrNull() ?: 0.0)
            }
            
            BasicTextField(
                modifier = Modifier.weight(1f).padding(16.dp),
                state = pointState,
            )
            
            val percentageState = textFieldManager(uiState.currentPercentage?.stringWithDecimals() ?: "") {
                onSetPercentage(it.toDoubleOrNull() ?: 0.0)
            }
            
            BasicTextField(
                modifier = Modifier.weight(1f).padding(16.dp),
                state = percentageState,
            )
            
            val gradeNameState = textFieldManager(uiState.currentGrade?.namedGrade ?: "") {
                onSelectGradeName(it)
            }
            
            BasicTextField(
                modifier = Modifier.weight(1f).padding(16.dp),
                state = gradeNameState,
            )
            
        }
        HorizontalDivider()
        if (gradeScale == null) {
            Text(text = "No grade scale selected")
            return
        }
    
}

@Preview
@Composable
private fun CalculatorScreenPreview() {
    GradeScaleCalculatorScreen(
        GradeScaleCalculatorUIState(
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
