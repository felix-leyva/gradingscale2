package de.felixlf.gradingscale2.features.list.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import de.felixlf.gradingscale2.theme.AppTheme

/**
 * Reusable icon button wrapped with a Material 3 [TooltipBox].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ActionIconButton(
    icon: ImageVector,
    tooltipText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = {
            PlainTooltip {
                Text(tooltipText)
            }
        },
        state = rememberTooltipState(),
        modifier = modifier,
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = tooltipText,
                tint = tint,
            )
        }
    }
}

@Preview
@Composable
private fun ActionIconButtonPreview() = AppTheme {
    ActionIconButton(
        icon = Icons.Default.TableChart,
        tooltipText = "Export Table",
        onClick = {},
    )
}
