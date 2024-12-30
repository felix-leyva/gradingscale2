package de.felixlf.gradingscale2.features.list.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import de.felixlf.gradingscale2.features.list.GradeScaleListUIState
import gradingscale2.composeapp.generated.resources.Res
import gradingscale2.composeapp.generated.resources.gradescale_list_select_grade_scale
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RowScope.GradeScaleDropboxSelector(
    uiState: GradeScaleListUIState,
    onSelectGradeScale: (String) -> Unit,
) {
    var expandedDropdown by remember { mutableStateOf(false) }
    val textFieldState = rememberTextFieldState()

    LaunchedEffect(uiState.selectedGradeScale) {
        uiState.selectedGradeScale?.gradeScaleName?.let(textFieldState::setTextAndPlaceCursorAtEnd)
    }

    ExposedDropdownMenuBox(
        modifier = Modifier.Companion.weight(1f),
        expanded = expandedDropdown,
        onExpandedChange = { expandedDropdown = !expandedDropdown },
    ) {
        TextField(
            modifier = Modifier.Companion.menuAnchor(MenuAnchorType.PrimaryNotEditable),
            state = textFieldState,
            readOnly = true,
            lineLimits = TextFieldLineLimits.SingleLine,
            label = { Text(stringResource(Res.string.gradescale_list_select_grade_scale)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
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
                    text = {
                        Text(text = string)
                    },
                    colors = MenuDefaults.itemColors(),
                )
            }
        }
    }
}
