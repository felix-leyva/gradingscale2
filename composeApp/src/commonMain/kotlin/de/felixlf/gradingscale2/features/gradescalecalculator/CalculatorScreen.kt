package de.felixlf.gradingscale2.features.gradescalecalculator

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndSelectAll
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CalculatorScreen() {
    val viewModel: GradeScaleCalculatorViewModel = koinViewModel()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    CalculatorScreen(
        uiState = uiState.value,
        onSelectGradeScale = viewModel::selectGradeScale,
        onSetTotalPoints = viewModel::setTotalPoints
    )
}


@Composable
private fun CalculatorScreen(
    uiState: GradeScaleCalculatorUIState,
    onSelectGradeScale: (String) -> Unit = {},
    onSetTotalPoints: (Double) -> Unit = {}
) {
    val textFieldValue = textFieldManager(
        uiState.selectedGradeScale?.totalPoints.toString()
    ) { onSetTotalPoints(it.toDoubleOrNull() ?: 0.0) }

    Column {
        BasicTextField(
            state = textFieldValue,
        )
        uiState.gradeScalesNames.forEach { string ->
            DropdownMenuItem(onClick = { onSelectGradeScale(string) }) {
                Text(text = string)
            }
        }
    }

    Divider(color = Color.Black)
    uiState.selectedGradeScale?.let {
        Text(text = it.toString())
    }
}

@Composable
fun textFieldManager(
    externalFieldValue: String,
    delay: Long = 500,
    onChangeExternalFieldValue: (String) -> Unit,
): TextFieldState {
    val textFieldState = rememberTextFieldState(externalFieldValue)

    LaunchedEffect(textFieldState.text) {
        delay(delay)
        val fieldText = textFieldState.text.toString()
        if (fieldText != externalFieldValue) {
            onChangeExternalFieldValue(fieldText)
        }
    }

    LaunchedEffect(externalFieldValue) {
        if (externalFieldValue != textFieldState.text) {
            textFieldState.setTextAndSelectAll(externalFieldValue)
        }
    }

    return textFieldState
}

@Preview
@Composable
private fun CalculatorScreenPreview() {
    CalculatorScreen(GradeScaleCalculatorUIState.Initial)
}