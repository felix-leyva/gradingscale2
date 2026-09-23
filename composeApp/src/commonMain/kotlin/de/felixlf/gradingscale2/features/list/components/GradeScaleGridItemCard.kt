package de.felixlf.gradingscale2.features.list.components

import androidx.compose.foundation.layout.Arrangement
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
 * Compact grid item card for grade scale grid view (2 columns on mobile, 3-4 columns on desktop).
 *
 * Layout (compact 2-tier design):
 * - Top row: Wider, flexible grade badge on the left, Points needed in the middle/right, and quick edit pencil button on the right.
 * - Bottom row: Minimum percentage text and a coordinated horizontal progress bar directly underneath.
 */
@Composable
fun GradeScaleGridItemCard(
    item: GradeListItemUIState,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit = {},
) {
    val gradeColor = MaterialTheme.gradeColors.forPercentage(item.percentage)

    Card(
        onClick = onEdit,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
        ) {
            // Tier 1: Grade Badge (wider, supporting longer names) + Points Needed + Edit Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Grade badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = gradeColor.badgeColor,
                    modifier = Modifier
                        .height(34.dp)
                        .widthIn(min = 64.dp, max = 92.dp),
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

                Spacer(modifier = Modifier.weight(1f))

                // Points needed
                Text(
                    text = "${item.pointsFormatted} pts",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.width(2.dp))

                // Edit pencil button
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(30.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit ${item.namedGrade}",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Tier 2: Percentage label + Coordinated progress bar
            Text(
                text = item.percentageFormatted,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    }
}

@Preview
@Composable
private fun GradeScaleGridItemCardPreview() = AppTheme {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            GradeScaleGridItemCard(
                item = GradeListItemUIState(
                    uuid = "1",
                    namedGrade = "Sehr gut",
                    percentage = 0.90,
                    percentageFormatted = "90 %",
                    points = 90.0,
                    pointsFormatted = "90",
                ),
                modifier = Modifier.weight(1f),
            )
            GradeScaleGridItemCard(
                item = GradeListItemUIState(
                    uuid = "2",
                    namedGrade = "15 Pkt",
                    percentage = 0.80,
                    percentageFormatted = "80 %",
                    points = 80.0,
                    pointsFormatted = "80",
                ),
                modifier = Modifier.weight(1f),
            )
        }
    }
}
