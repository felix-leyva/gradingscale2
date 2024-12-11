package de.felixlf.gradingscale2.features.gradescalecalculator

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndSelectAll
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.felixlf.gradingscale2.entities.util.MockGradeScalesGenerator
import gradingscale2.composeapp.generated.resources.Res
import gradingscale2.composeapp.generated.resources.gradescale_list_select_grade_scale
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun GradeScaleListScreen(
    uiState: GradeScaleListUIState,
    onSelectGradeScale: (String) -> Unit = {},
    onSetTotalPoints: (Double) -> Unit = {},
) {
    Column {
        Row {
            val textFieldValue =
                textFieldManager(
                    uiState.selectedGradeScale?.totalPoints.toString(),
                ) { onSetTotalPoints(it.toDoubleOrNull() ?: 1.0) }
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
            BasicTextField(
                modifier = Modifier.weight(1f),
                state = textFieldValue,
            )
        }
        Divider(color = Color.Black)
        LazyColumn {
            uiState.selectedGradeScale?.let {
                item {
                    Text(text = it.toString())
                }
            }
        }
    }
}

@Composable
fun textFieldManager(
    externalFieldValue: String,
    delay: Long = 500,
    onChangeExternalFieldValue: (String) -> Unit,
): TextFieldState {
    val textFieldState = rememberTextFieldState(externalFieldValue)

    LaunchedEffect(textFieldState.text, onChangeExternalFieldValue) {
        delay(delay)
        val fieldText = textFieldState.text.toString()
        if (fieldText != externalFieldValue) {
            onChangeExternalFieldValue(fieldText)
        }
    }

    LaunchedEffect(externalFieldValue, onChangeExternalFieldValue) {
        if (externalFieldValue != textFieldState.text) {
            textFieldState.setTextAndSelectAll(externalFieldValue)
        }
    }

    return textFieldState
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
