package de.felixlf.gradingscale2.features.calculator.components

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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import de.felixlf.gradingscale2.entities.features.calculator.GradeScaleCalculatorUIState
import de.felixlf.gradingscale2.theme.AppTheme
import de.felixlf.gradingscale2.theme.gradeColors
import gradingscale2.entities.generated.resources.Res
import gradingscale2.entities.generated.resources.calculator_screen_points_input
import org.jetbrains.compose.resources.stringResource

/**
 * Prominent card showcasing the calculated grade result:
 * Grade badge, percentage, earned / total points, and quick adjustment steppers.
 */
@Composable
fun HeroResultCard(
    uiState: GradeScaleCalculatorUIState,
    onSetPoints: (Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    HeroResultCard(
        selectedGradeScaleName = uiState.selectedGradeScaleName,
        gradeName = uiState.gradeName,
        percentageFormattedWithSymbol = uiState.percentageFormattedWithSymbol,
        earnedPointsString = uiState.earnedPointsString,
        totalPointsString = uiState.totalPointsString,
        hasGradeResult = uiState.hasGradeResult,
        currentPercentageVal = uiState.currentPercentageVal,
        canDecrementPoints = uiState.canDecrementPoints,
        canIncrementPoints = uiState.canIncrementPoints,
        onDecrementOne = { onSetPoints(uiState.nextLowerPoints) },
        onDecrementHalf = { onSetPoints(uiState.nextHalfLowerPoints) },
        onIncrementHalf = { onSetPoints(uiState.nextHalfHigherPoints) },
        onIncrementOne = { onSetPoints(uiState.nextHigherPoints) },
        modifier = modifier,
    )
}

@Composable
fun HeroResultCard(
    selectedGradeScaleName: String,
    gradeName: String,
    percentageFormattedWithSymbol: String,
    earnedPointsString: String,
    totalPointsString: String,
    hasGradeResult: Boolean,
    currentPercentageVal: Double,
    canDecrementPoints: Boolean,
    canIncrementPoints: Boolean,
    onDecrementOne: () -> Unit,
    onDecrementHalf: () -> Unit,
    onIncrementHalf: () -> Unit,
    onIncrementOne: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gradeColor = MaterialTheme.gradeColors.forPercentage(currentPercentageVal)

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header row with scale name
            Text(
                text = selectedGradeScaleName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Prominent grade badge and score metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = gradeColor.badgeColor,
                    modifier = Modifier
                        .height(76.dp)
                        .widthIn(min = 88.dp, max = 136.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .height(76.dp)
                            .widthIn(min = 88.dp, max = 136.dp)
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = gradeName.ifEmpty { "-" },
                            style = when {
                                gradeName.length <= 2 -> MaterialTheme.typography.displaySmall
                                gradeName.length <= 5 -> MaterialTheme.typography.headlineMedium
                                else -> MaterialTheme.typography.titleLarge
                            }.copy(fontWeight = FontWeight.Bold),
                            color = gradeColor.onBadgeColor,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(20.dp))

                Column(
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = percentageFormattedWithSymbol,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val pointsLabel = stringResource(Res.string.calculator_screen_points_input)
                    Text(
                        text = if (hasGradeResult) {
                            "$earnedPointsString / $totalPointsString $pointsLabel"
                        } else {
                            "- / $totalPointsString $pointsLabel"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Quick Adjust Steppers for rapid grading
            QuickAdjustSteppers(
                canDecrement = canDecrementPoints,
                canIncrement = canIncrementPoints,
                onDecrementOne = onDecrementOne,
                onDecrementHalf = onDecrementHalf,
                onIncrementHalf = onIncrementHalf,
                onIncrementOne = onIncrementOne,
            )
        }
    }
}

@Preview
@Composable
private fun HeroResultCardPreview() = AppTheme {
    HeroResultCard(
        selectedGradeScaleName = "Standard Scale (A-F)",
        gradeName = "B",
        percentageFormattedWithSymbol = "85 %",
        earnedPointsString = "85",
        totalPointsString = "100",
        hasGradeResult = true,
        currentPercentageVal = 0.85,
        canDecrementPoints = true,
        canIncrementPoints = true,
        onDecrementOne = {},
        onDecrementHalf = {},
        onIncrementHalf = {},
        onIncrementOne = {},
    )
}
