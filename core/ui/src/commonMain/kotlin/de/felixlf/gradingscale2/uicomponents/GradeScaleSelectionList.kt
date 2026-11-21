package de.felixlf.gradingscale2.uicomponents

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.felixlf.gradingscale2.entities.models.GradeScaleNameAndId
import dev.chrisbanes.haze.HazeDefaults.style
import kotlinx.collections.immutable.PersistentList

@Composable
fun GradeScaleSelectionList(
    items: PersistentList<GradeScaleNameAndId>,
    selectedItemId: String?,
    onSelectionChange: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.width(280.dp).fillMaxHeight(),
    ) {
        Surface(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
            ) {
                stickyHeader {
                        Text(
                            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp))
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            text = "Grade Scales",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                }
                items(items) { gradeScale ->
                    GradeScaleListItem(
                        gradeScale = gradeScale,
                        isSelected = gradeScale.id == selectedItemId,
                        onClick = { onSelectionChange(gradeScale.id) },
                    )
                }
            }
        }
        VerticalDivider(
            modifier = Modifier.fillMaxHeight(),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}

@Composable
private fun GradeScaleListItem(
    gradeScale: GradeScaleNameAndId,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(300),
        label = "background_color",
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(300),
        label = "text_color",
    )

    val horizontalPadding by animateDpAsState(
        targetValue = if (isSelected) 20.dp else 16.dp,
        animationSpec = tween(300),
        label = "horizontal_padding",
    )

    SideListItem(
        onClick = onClick,
        backgroundColor = backgroundColor,
        horizontalPadding = horizontalPadding,
    ) {
        Text(
            text = gradeScale.name,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor,
        )
    }
}

@Composable
private fun SideListItem(
    onClick: () -> Unit = {}, backgroundColor: Color, horizontalPadding: Dp, content: @Composable (BoxScope.() -> Unit)
) {
    Box(
        modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.small).clickable { onClick() }.background(backgroundColor)
            .padding(horizontal = horizontalPadding, vertical = 12.dp),
        contentAlignment = Alignment.CenterStart,
        content = content,
    )
}
