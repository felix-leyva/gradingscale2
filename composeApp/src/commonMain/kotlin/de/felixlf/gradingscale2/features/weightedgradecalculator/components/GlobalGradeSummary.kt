package de.felixlf.gradingscale2.features.weightedgradecalculator.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.felixlf.gradingscale2.entities.models.weightedgrade.WeightedGradeSummary
import gradingscale2.entities.generated.resources.Res
import gradingscale2.entities.generated.resources.weighted_grade_summary_percentage
import gradingscale2.entities.generated.resources.weighted_grade_summary_points
import gradingscale2.entities.generated.resources.weighted_grade_summary_title
import gradingscale2.entities.generated.resources.weighted_grade_summary_total
import gradingscale2.entities.generated.resources.weighted_grade_summary_total_points
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun GlobalGradeSummary(
    weightedGradesSummary: WeightedGradeSummary?,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
        ),
    ) {
        Column(modifier = modifier) {
            Text(
                text = stringResource(Res.string.weighted_grade_summary_title),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )

            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                GlobalGradeItem(
                    label = stringResource(Res.string.weighted_grade_summary_total),
                    value = weightedGradesSummary?.totalGradeName,
                )
                Divider()
                GlobalGradeItem(
                    label = stringResource(Res.string.weighted_grade_summary_percentage),
                    value = weightedGradesSummary?.weightedPercentage,
                )
                Divider()
                GlobalGradeItem(
                    label = stringResource(Res.string.weighted_grade_summary_points),
                    value = weightedGradesSummary?.earnedPoints,
                )
                Divider()
                GlobalGradeItem(
                    label = stringResource(Res.string.weighted_grade_summary_total_points),
                    value = weightedGradesSummary?.totalPoints,
                )
            }
        }
    }
}

@Composable
private fun Divider() {
    VerticalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
}

@Composable
private fun RowScope.GlobalGradeItem(
    label: String,
    value: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            maxLines = 2,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value ?: "",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
