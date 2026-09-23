package de.felixlf.gradingscale2.features.calculator.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import de.felixlf.gradingscale2.features.calculator.CalculatorTextField
import de.felixlf.gradingscale2.theme.AppTheme
import de.felixlf.gradingscale2.uicomponents.DropboxSelector
import gradingscale2.entities.generated.resources.Res
import gradingscale2.entities.generated.resources.calculator_calculation_parameters
import gradingscale2.entities.generated.resources.calculator_screen_grade_name_dropbox_default
import gradingscale2.entities.generated.resources.calculator_screen_grade_name_dropbox_label
import gradingscale2.entities.generated.resources.calculator_screen_percentage_input
import gradingscale2.entities.generated.resources.calculator_screen_points_input
import gradingscale2.entities.generated.resources.calculator_screen_total_points_input
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource

/**
 * Card containing the interactive input fields for the calculator:
 * Total Points, Earned Points, Percentage, and Grade Name Selector.
 */
@Composable
fun CalculatorParametersCard(
    totalPointsState: TextFieldState,
    pointState: TextFieldState,
    percentageState: TextFieldState,
    availableGradeNames: ImmutableList<String>,
    selectedGradeName: String,
    onSelectGradeName: (String) -> Unit,
    totalPointsFocusRequester: FocusRequester,
    pointsFocusRequester: FocusRequester,
    percentageFocusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(Res.string.calculator_calculation_parameters),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary,
            )

            // Row 1: Total Points (always editable text input) & Earned Points
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CalculatorTextField(
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(totalPointsFocusRequester)
                        .focusProperties { next = pointsFocusRequester },
                    state = totalPointsState,
                    label = stringResource(Res.string.calculator_screen_total_points_input),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next,
                    ),
                )

                CalculatorTextField(
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(pointsFocusRequester)
                        .focusProperties {
                            next = percentageFocusRequester
                            previous = totalPointsFocusRequester
                        },
                    state = pointState,
                    label = stringResource(Res.string.calculator_screen_points_input),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next,
                    ),
                )
            }

            // Row 2: Percentage & Grade Name Dropdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CalculatorTextField(
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(percentageFocusRequester)
                        .focusProperties { previous = pointsFocusRequester },
                    state = percentageState,
                    label = stringResource(Res.string.calculator_screen_percentage_input),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Number,
                    ),
                )

                DropboxSelector(
                    elements = availableGradeNames,
                    selectedElement = selectedGradeName.ifEmpty { null },
                    onSelectElement = onSelectGradeName,
                    defaultText = stringResource(Res.string.calculator_screen_grade_name_dropbox_default),
                    label = stringResource(Res.string.calculator_screen_grade_name_dropbox_label),
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                )
            }
        }
    }
}

@Preview
@Composable
private fun CalculatorParametersCardPreview() = AppTheme {
    val totalPointsState = rememberTextFieldState("100")
    val pointState = rememberTextFieldState("85")
    val percentageState = rememberTextFieldState("85")
    val totalFocus = remember { FocusRequester() }
    val pointFocus = remember { FocusRequester() }
    val percentFocus = remember { FocusRequester() }

    CalculatorParametersCard(
        totalPointsState = totalPointsState,
        pointState = pointState,
        percentageState = percentageState,
        availableGradeNames = persistentListOf("A", "B", "C", "D", "F"),
        selectedGradeName = "B",
        onSelectGradeName = {},
        totalPointsFocusRequester = totalFocus,
        pointsFocusRequester = pointFocus,
        percentageFocusRequester = percentFocus,
    )
}
