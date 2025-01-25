package de.felixlf.gradingscale2.uicomponents

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.TextStyle
import de.felixlf.gradingscale2.features.calculator.CalculatorTextField
import de.felixlf.gradingscale2.theme.AppTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DropboxSelector(
    elements: ImmutableList<String>,
    selectedElement: String?,
    onSelectElement: (String) -> Unit,
    modifier: Modifier = Modifier,
    defaultText: String? = null,
    label: String? = null,
    textStyle: TextStyle = MaterialTheme.typography.titleSmall. copy(color = MaterialTheme. colorScheme. onSurface),
) {
    var expandedDropdown by remember { mutableStateOf(false) }
    val textFieldState = rememberTextFieldState(defaultText ?: selectedElement ?: "")

    LaunchedEffect(selectedElement) {
        selectedElement?.let(textFieldState::setTextAndPlaceCursorAtEnd)
    }

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expandedDropdown,
        onExpandedChange = { expandedDropdown = !expandedDropdown },
    ) {
        CalculatorTextField(
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true).exposedDropdownSize(),
            state = textFieldState,
            label = label,
            readOnly = true,
            selectAllOnFocus = false,
            textStyle = textStyle,
            interactionSource = remember { MutableInteractionSource() },
        )
        ExposedDropdownMenu(
            modifier = Modifier,
            expanded = expandedDropdown,
            onDismissRequest = { expandedDropdown = false },
        ) {
            elements.forEach { string ->
                DropdownMenuItem(
                    onClick = {
                        expandedDropdown = false
                        onSelectElement(string)
                    },
                    text = {
                        Text(text = string, style = textStyle)
                    },
                    colors = MenuDefaults.itemColors(),
                )
            }
        }
    }
}

@Preview
@Composable
private fun DropboxSelectorPreview() = AppTheme {
    DropboxSelector(
        elements = listOf("Element 1", "Element 2").toImmutableList(),
        selectedElement = "Element 1",
        onSelectElement = {},
        label = "Label",
        defaultText = "Select an element",
    )
}
