package de.felixlf.gradingscale2.features.weightedgradecalculator.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.felixlf.gradingscale2.entities.models.weightedgrade.WeightedGradeWithName
import de.felixlf.gradingscale2.theme.SemanticGradeColor
import de.felixlf.gradingscale2.theme.gradeColors
import kotlinx.collections.immutable.ImmutableList

/**
 * A donut chart representing weighted grade contribution:
 * - Outer ring: Segments colored with each partial grade's color and arc sweep proportional to its weight.
 * - Center core: Circle colored with the overall resulting grade color, displaying the final grade name.
 */
@Composable
fun WeightedDonutChart(
    weightedGrades: ImmutableList<WeightedGradeWithName>,
    totalWeight: Double,
    resultingGradeColor: SemanticGradeColor,
    resultingGradeName: String,
    modifier: Modifier = Modifier,
    chartSize: Dp = 112.dp,
    strokeWidth: Dp = 12.dp,
    innerBadgeSize: Dp = 72.dp,
) {
    val gradeColors = MaterialTheme.gradeColors
    val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

    // Smooth animation on appear / update
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(weightedGrades, totalWeight) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        )
    }

    Box(
        modifier = modifier.size(chartSize),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(strokeWidth / 2f)) {
            val strokePx = strokeWidth.toPx()
            val hasGrades = weightedGrades.isNotEmpty() && totalWeight > 0.0

            // Always draw background track ring
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )

            if (hasGrades) {
                val gapAngle = if (weightedGrades.size > 1) 4f else 0f
                var currentStartAngle = -90f
                val progress = animationProgress.value

                weightedGrades.forEach { item ->
                    val fullSweep = ((item.grade.weight / totalWeight) * 360.0).toFloat() * progress
                    val effectiveSweep = (fullSweep - gapAngle).coerceAtLeast(0f)
                    val segmentColor = gradeColors.forPercentage(item.percentage).badgeColor

                    if (effectiveSweep > 0f) {
                        drawArc(
                            color = segmentColor,
                            startAngle = currentStartAngle + gapAngle / 2f,
                            sweepAngle = effectiveSweep,
                            useCenter = false,
                            style = Stroke(width = strokePx, cap = StrokeCap.Round),
                        )
                    }
                    currentStartAngle += fullSweep
                }
            }
        }

        // Inner circular badge with the resulting grade
        Surface(
            shape = CircleShape,
            color = if (weightedGrades.isEmpty()) MaterialTheme.colorScheme.surfaceVariant else resultingGradeColor.badgeColor,
            modifier = Modifier.size(innerBadgeSize),
            shadowElevation = 2.dp,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp),
            ) {
                Text(
                    text = resultingGradeName.ifEmpty { "-" },
                    style = when {
                        resultingGradeName.length <= 2 -> MaterialTheme.typography.headlineSmall
                        resultingGradeName.length <= 5 -> MaterialTheme.typography.titleMedium
                        else -> MaterialTheme.typography.labelLarge
                    }.copy(fontWeight = FontWeight.Bold),
                    color = if (weightedGrades.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else resultingGradeColor.onBadgeColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
