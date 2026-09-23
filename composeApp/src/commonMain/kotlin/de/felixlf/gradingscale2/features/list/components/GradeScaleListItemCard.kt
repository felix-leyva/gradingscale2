package de.felixlf.gradingscale2.features.list.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.felixlf.gradingscale2.entities.features.list.GradeScaleListUIState.GradeListItemUIState
import de.felixlf.gradingscale2.theme.AppTheme
import de.felixlf.gradingscale2.theme.gradeColors
import androidx.compose.ui.tooling.preview.Preview

/**
 * Compact list item card for grade scale list view.
 * Features:
 * - Wider and flexible color badge directly on the grade name/letter to support longer names.
 * - Shorter, more compact progress bar line in mobile mode.
 * - Points needed and quick edit pencil button.
 */
@Composable
fun GradeScaleListItemCard(
    item: GradeListItemUIState,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit = {},
) {
    val gradeColor = MaterialTheme.gradeColors.forPercentage(item.percentage)

    Card(
        onClick = onEdit,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Wider and flexible grade badge for accommodating longer names (e.g. "Sehr gut", "15 Pkt", "1+")
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = gradeColor.badgeColor,
                modifier = Modifier
                    .height(36.dp)
                    .widthIn(min = 60.dp, max = 96.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 6.dp),
                ) {
                    Text(
                        text = item.namedGrade,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = gradeColor.onBadgeColor,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // Compact percentage column with shorter, refined progress bar
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .widthIn(min = 72.dp, max = 104.dp),
            ) {
                Text(
                    text = item.percentageFormatted,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(3.dp))
                LinearProgressIndicator(
                    progress = { item.percentageProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp),
                    color = gradeColor.badgeColor,
                    trackColor = gradeColor.containerColor,
                    strokeCap = StrokeCap.Round,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Points needed
            Text(
                text = "${item.pointsFormatted} pts",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.width(2.dp))

            // Edit button
            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(34.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit ${item.namedGrade}",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview
@Composable
private fun GradeScaleListItemCardPreview() = AppTheme {
    Column(modifier = Modifier.padding(16.dp)) {
        GradeScaleListItemCard(
            item = GradeListItemUIState(
                uuid = "1",
                namedGrade = "Sehr gut",
                percentage = 0.95,
                percentageFormatted = "95 %",
                points = 95.0,
                pointsFormatted = "95",
            ),
        )
        Spacer(modifier = Modifier.height(8.dp))
        GradeScaleListItemCard(
            item = GradeListItemUIState(
                uuid = "2",
                namedGrade = "15 Pkt",
                percentage = 0.85,
                percentageFormatted = "85 %",
                points = 85.0,
                pointsFormatted = "85",
            ),
        )
        Spacer(modifier = Modifier.height(8.dp))
        GradeScaleListItemCard(
            item = GradeListItemUIState(
                uuid = "3",
                namedGrade = "5.8",
                percentage = 0.10,
                percentageFormatted = "10.1 %",
                points = 1.01,
                pointsFormatted = "1.01",
            ),
        )
    }
}
