package de.felixlf.gradingscale2.features.list.exportdialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.felixlf.gradingscale2.theme.AppTheme
import gradingscale2.entities.generated.resources.Res
import gradingscale2.entities.generated.resources.gradescale_export_option_descending
import gradingscale2.entities.generated.resources.gradescale_export_option_percentage
import gradingscale2.entities.generated.resources.gradescale_export_option_range
import org.jetbrains.compose.resources.stringResource

/**
 * Filter chips allowing users to toggle exam table formatting options:
 * percentage column, interval range display, and descending sort order.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ExportOptionsChips(
    includePercentage: Boolean,
    showAsRange: Boolean,
    sortDescending: Boolean,
    onTogglePercentage: () -> Unit,
    onToggleRange: () -> Unit,
    onToggleSortDescending: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        FilterChip(
            selected = includePercentage,
            onClick = onTogglePercentage,
            label = { Text(stringResource(Res.string.gradescale_export_option_percentage)) },
            leadingIcon = {
                if (includePercentage) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            },
        )

        FilterChip(
            selected = showAsRange,
            onClick = onToggleRange,
            label = { Text(stringResource(Res.string.gradescale_export_option_range)) },
            leadingIcon = {
                if (showAsRange) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            },
        )

        FilterChip(
            selected = sortDescending,
            onClick = onToggleSortDescending,
            label = { Text(stringResource(Res.string.gradescale_export_option_descending)) },
            leadingIcon = {
                if (sortDescending) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            },
        )
    }
}

@Preview
@Composable
private fun ExportOptionsChipsPreview() = AppTheme {
    ExportOptionsChips(
        includePercentage = true,
        showAsRange = false,
        sortDescending = true,
        onTogglePercentage = {},
        onToggleRange = {},
        onToggleSortDescending = {},
    )
}
