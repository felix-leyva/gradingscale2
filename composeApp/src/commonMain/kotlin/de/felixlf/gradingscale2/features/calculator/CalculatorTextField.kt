package de.felixlf.gradingscale2.features.calculator

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.selectAll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import de.felixlf.gradingscale2.utils.NumericInputFilter
import de.felixlf.gradingscale2.utils.onPreviewTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CalculatorTextField(
    modifier: Modifier = Modifier,
    state: TextFieldState,
    label: String? = null,
    selectAllOnFocus: Boolean = true,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default.copy(
        keyboardType = KeyboardType.Number,
        imeAction = ImeAction.Done,
    ),
    textStyle: TextStyle = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.onSurface),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    inputTransformation: InputTransformation? = null,
) {
    val isFocused by interactionSource.collectIsFocusedAsState()
    LaunchedEffect(isFocused, selectAllOnFocus) {
        if (isFocused && selectAllOnFocus) state.edit { selectAll() }
    }

    // Apply numeric filter if keyboard type is Number
    val isNumberKeyboard = keyboardOptions.keyboardType == KeyboardType.Number
    val combinedTransformation = remember(inputTransformation, isNumberKeyboard) {
        if (isNumberKeyboard) {
            val numericFilter = NumericInputFilter()
            if (inputTransformation != null) {
                // Chain both transformations
                InputTransformation {
                    numericFilter.run { transformInput() }
                    inputTransformation.run { transformInput() }
                }
            } else {
                numericFilter
            }
        } else {
            inputTransformation
        }
    }

    val modifierWithTabHandler = modifier.onPreviewTab()

    BasicTextField(
        modifier = modifierWithTabHandler,
        state = state,
        enabled = enabled,
        readOnly = readOnly,
        keyboardOptions = keyboardOptions,
        textStyle = textStyle,
        interactionSource = interactionSource,
        inputTransformation = combinedTransformation,
        decorator = { innerTextField ->
            OutlinedTextFieldDefaults.DecorationBox(
                value = state.text.toString(),
                innerTextField = innerTextField,
                enabled = enabled,
                singleLine = singleLine,
                label = { label?.let { Text(it) } },
                interactionSource = interactionSource,
                visualTransformation = visualTransformation,
            )
        },
    )
}
