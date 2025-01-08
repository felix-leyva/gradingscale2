package de.felixlf.gradingscale2.features.list.components

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GradeScaleDropboxSelector(
    gradeScalesNames: ImmutableList<String>,
    selectedGradeScaleName: String?,
    onSelectGradeScale: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedDropdown by remember { mutableStateOf(false) }
    val textFieldState = rememberTextFieldState()

    LaunchedEffect(gradeScalesNames) {
        selectedGradeScaleName?.let(textFieldState::setTextAndPlaceCursorAtEnd)
    }

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expandedDropdown,
        onExpandedChange = { expandedDropdown = !expandedDropdown },
    ) {
        BasicTextField(
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
            state = textFieldState,
            readOnly = true,
            lineLimits = TextFieldLineLimits.SingleLine,
            //label = { Text(stringResource(Res.string.gradescale_list_select_grade_scale)) },
            //trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
            //colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
        )
        ExposedDropdownMenu(
            expanded = expandedDropdown,
            onDismissRequest = { expandedDropdown = false },
        ) {
            gradeScalesNames.forEach { string ->
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
