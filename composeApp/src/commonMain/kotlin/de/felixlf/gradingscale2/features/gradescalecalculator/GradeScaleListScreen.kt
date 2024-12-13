package de.felixlf.gradingscale2.features.gradescalecalculator

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.felixlf.gradingscale2.entities.models.PointedGrade
import de.felixlf.gradingscale2.entities.util.MockGradeScalesGenerator
import de.felixlf.gradingscale2.uicomponents.VerticalDivider
import de.felixlf.gradingscale2.utils.stringWithDecimals
import de.felixlf.gradingscale2.utils.textFieldManager
import gradingscale2.composeapp.generated.resources.Res
import gradingscale2.composeapp.generated.resources.gradescale_list_select_grade_scale
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GradeScaleListScreen() {
    val viewModel: GradeScaleListViewModel = koinViewModel()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    GradeScaleListScreen(
        uiState = uiState.value,
        onSelectGradeScale = viewModel::selectGradeScale,
        onSetTotalPoints = viewModel::setTotalPoints,
    )
}

@Composable
private fun GradeScaleListScreen(
    uiState: GradeScaleListUIState,
    onSelectGradeScale: (String) -> Unit = {},
    onSetTotalPoints: (Double) -> Unit = {},
) {
    Column {
        Row {
            val textFieldValue = textFieldManager(uiState.selectedGradeScale?.totalPoints?.stringWithDecimals() ?: "") {
                onSetTotalPoints(it.toDoubleOrNull() ?: 1.0)
            }
            GradeScaleDropboxSelector(uiState = uiState, onSelectGradeScale = onSelectGradeScale)
            BasicTextField(
                modifier = Modifier.weight(1f).padding(16.dp),
                state = textFieldValue,
            )
        }
        Divider()
        LazyColumn {
            uiState.selectedGradeScale?.let { gradeScale ->
                items(
                    count = gradeScale.sortedPointedGrades.size,
                    key = { gradeScale.sortedPointedGrades[it].uuid },
                ) {
                    ListItem(grade = gradeScale.sortedPointedGrades[it])
                    Divider()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun RowScope.GradeScaleDropboxSelector(
    uiState: GradeScaleListUIState,
    onSelectGradeScale: (String) -> Unit,
) {
    var expandedDropdown by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        modifier = Modifier.weight(1f),
        expanded = expandedDropdown,
        onExpandedChange = { expandedDropdown = !expandedDropdown },
    ) {
        DropdownMenuItem(
            onClick = { },
            content = {
                Text(
                    text =
                        uiState.selectedGradeScale?.gradeScaleName
                            ?: stringResource(Res.string.gradescale_list_select_grade_scale),
                )
            },
        )
        ExposedDropdownMenu(
            expanded = expandedDropdown,
            onDismissRequest = { expandedDropdown = false },
        ) {
            uiState.gradeScalesNames.forEach { string ->
                DropdownMenuItem(
                    onClick = {
                        expandedDropdown = false
                        onSelectGradeScale(string)
                    },
                    content = {
                        Text(text = string)
                    },
                )
            }
        }
    }
}

@Composable
private fun ListItem(grade: PointedGrade) {
    Row(
        modifier = Modifier.height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1f).padding(8.dp),
            text = grade.namedGrade,
        )
        VerticalDivider()
        Text(
            modifier = Modifier.weight(1f).padding(8.dp),
            text = grade.percentage.stringWithDecimals(),
        )
        VerticalDivider()
        Text(
            modifier = Modifier.weight(1f).padding(8.dp),
            text = grade.points.stringWithDecimals(),
        )
    }
}

@Preview
@Composable
private fun CalculatorScreenPreview() {
    GradeScaleListScreen(
        GradeScaleListUIState(
            selectedGradeScale = MockGradeScalesGenerator().gradeScales.first(),
            gradeScalesNamesWithId =
            MockGradeScalesGenerator()
                .gradeScales
                .map {
                    GradeScaleListUIState.GradeScaleNameWithId(
                        gradeScaleName = it.gradeScaleName,
                        gradeScaleId = it.id,
                    )
                }.toImmutableList(),
        ),
    )
}
