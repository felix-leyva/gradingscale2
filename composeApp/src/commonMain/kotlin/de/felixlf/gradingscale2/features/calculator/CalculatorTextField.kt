package de.felixlf.gradingscale2.features.calculator

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CalculatorTextField(
    modifier: Modifier = Modifier,
    state: TextFieldState,
    label: String? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val isFocused by interactionSource.collectIsFocusedAsState()
    LaunchedEffect(isFocused) {
        if (isFocused) state.edit { selectAll() }
    }

    BasicTextField(
        modifier = modifier,
        state = state,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Decimal,
        ),
        textStyle = MaterialTheme.typography.titleLarge.copy(
            color = MaterialTheme.colorScheme.onSurface,
        ),
        interactionSource = interactionSource,
        decorator = { innerTextField ->
            OutlinedTextFieldDefaults.DecorationBox(
                value = state.text.toString(),
                innerTextField = innerTextField,
                enabled = true,
                singleLine = true,
                label = { label?.let { Text(it) } },
                interactionSource = interactionSource,
                visualTransformation = VisualTransformation.None,
            )
        },
    )
}
