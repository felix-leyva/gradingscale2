package de.felixlf.gradingscale2.uicomponents

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A vertical divider that can be used in a [Row] to separate elements.
 * To make sure the divider is always the full height of the parent, give to the row the modifier Modifier.height(IntrinsicSize.Min)
 * @param modifier The modifier for the divider.
 * @param width The width of the divider.
 * @param color The color of the divider.
 */
@Composable
fun VerticalDivider(
    modifier: Modifier = Modifier.fillMaxHeight(),
    width: Dp = 1.dp,
    color: Color = MaterialTheme.colors.onSurface.copy(alpha = DividerAlpha)
) {
    Canvas(modifier = modifier) {
        val canvasHeight = size.height
        drawRect(
            color = color,
            size = Size(width.toPx(), canvasHeight),
        )
    }
}

private const val DividerAlpha = 0.12f
