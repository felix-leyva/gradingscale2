package de.felixlf.gradingscale2.features.weightedgradecalculator.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.felixlf.gradingscale2.entities.models.weightedgrade.WeightedGradeSummary
import de.felixlf.gradingscale2.entities.models.weightedgrade.WeightedGradeWithName
import de.felixlf.gradingscale2.theme.gradeColors
import gradingscale2.entities.generated.resources.Res
import gradingscale2.entities.generated.resources.weighted_calculator_total_weight_label
import gradingscale2.entities.generated.resources.weighted_grade_summary_title
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource

@Composable
fun WeightedHeroSummaryCard(
    weightedGradeSummary: WeightedGradeSummary?,
    weightedGrades: ImmutableList<WeightedGradeWithName>,
    totalWeight: Double,
    scaleName: String?,
    modifier: Modifier = Modifier,
) {
    val gradeColors = MaterialTheme.gradeColors
    val currentPercentage = weightedGradeSummary?.weightedPercentageDouble ?: 0.0
    val resultingGradeColor = gradeColors.forPercentage(currentPercentage)

    val animatedProgress by animateFloatAsState(
        targetValue = currentPercentage.toFloat().coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600),
    )

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            // Header with scale name or screen title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = scaleName ?: stringResource(Res.string.weighted_grade_summary_title),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (weightedGrades.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Text(
                            text = "${stringResource(Res.string.weighted_calculator_total_weight_label)}: ${weightedGradeSummary?.totalPoints ?: totalWeight}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Content: Donut Chart on the left, Detailed Metrics on the right
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // The Hero Donut Chart
                WeightedDonutChart(
                    weightedGrades = weightedGrades,
                    totalWeight = totalWeight,
                    resultingGradeColor = resultingGradeColor,
                    resultingGradeName = weightedGradeSummary?.totalGradeName ?: "-",
                )

                Spacer(modifier = Modifier.width(20.dp))

                // Stats and Percentage
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center,
                ) {
                    if (weightedGradeSummary != null && weightedGrades.isNotEmpty()) {
                        Text(
                            text = weightedGradeSummary.weightedPercentage,
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${weightedGradeSummary.earnedPoints} / ${weightedGradeSummary.totalPoints} pts",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Smooth progress bar
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = resultingGradeColor.badgeColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap = StrokeCap.Round,
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "${weightedGrades.size} ${if (weightedGrades.size == 1) "grade" else "grades"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    } else {
                        Text(
                            text = "-",
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Add partial grades below",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                }
            }
        }
    }
}
