package de.felixlf.gradingscale2.features.calculator.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import de.felixlf.gradingscale2.theme.AppTheme
import gradingscale2.entities.generated.resources.Res
import gradingscale2.entities.generated.resources.calculator_quick_adjust
import org.jetbrains.compose.resources.stringResource

/**
 * Stepper buttons (-1.0, -0.5, +0.5, +1.0) allowing rapid score adjustments during grading.
 */
@Composable
fun QuickAdjustSteppers(
    canDecrement: Boolean,
    canIncrement: Boolean,
    onDecrementOne: () -> Unit,
    onDecrementHalf: () -> Unit,
    onIncrementHalf: () -> Unit,
    onIncrementOne: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.calculator_quick_adjust),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            AssistChip(
                onClick = onDecrementOne,
                label = { Text("-1.0") },
                enabled = canDecrement,
            )
            AssistChip(
                onClick = onDecrementHalf,
                label = { Text("-0.5") },
                enabled = canDecrement,
            )
            AssistChip(
                onClick = onIncrementHalf,
                label = { Text("+0.5") },
                enabled = canIncrement,
            )
            AssistChip(
                onClick = onIncrementOne,
                label = { Text("+1.0") },
                enabled = canIncrement,
            )
        }
    }
}

@Preview
@Composable
private fun QuickAdjustSteppersPreview() = AppTheme {
    QuickAdjustSteppers(
        canDecrement = true,
        canIncrement = true,
        onDecrementOne = {},
        onDecrementHalf = {},
        onIncrementHalf = {},
        onIncrementOne = {},
    )
}
