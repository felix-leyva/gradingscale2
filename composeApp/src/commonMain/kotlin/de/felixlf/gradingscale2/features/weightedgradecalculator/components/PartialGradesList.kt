package de.felixlf.gradingscale2.features.weightedgradecalculator.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.felixlf.gradingscale2.entities.models.weightedgrade.WeightedGradeWithName
import de.felixlf.gradingscale2.entities.util.stringWithDecimals
import gradingscale2.entities.generated.resources.Res
import gradingscale2.entities.generated.resources.weighted_grade_list_label_name
import gradingscale2.entities.generated.resources.weighted_grade_list_label_percentage
import gradingscale2.entities.generated.resources.weighted_grade_list_label_points
import gradingscale2.entities.generated.resources.weighted_grade_list_label_title
import gradingscale2.entities.generated.resources.weighted_grade_list_label_total_points
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun PartialGradesList(
    weightedGrades: ImmutableList<WeightedGradeWithName>,
    onGradeClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(Res.string.weighted_grade_list_label_title),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        LazyColumn {
            stickyHeader {
                GradeRowContent(
                    gradeName = stringResource(Res.string.weighted_grade_list_label_name),
                    percentage = stringResource(Res.string.weighted_grade_list_label_percentage),
                    points = stringResource(Res.string.weighted_grade_list_label_points),
                    totalPoints = stringResource(Res.string.weighted_grade_list_label_total_points),
                    isHeader = true,
                )
                HorizontalDivider()
            }
            itemsIndexed(weightedGrades) { index, grade ->
                GradeRow(
                    weightedGradeWithName = grade,
                    onClick = { onGradeClick(grade.grade.uuid) },
                )
                if (index < weightedGrades.lastIndex) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun GradeRow(
    weightedGradeWithName: WeightedGradeWithName,
    onClick: () -> Unit,
) {
    Surface(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        with(weightedGradeWithName) {
            GradeRowContent(
                gradeName = name,
                percentage = (percentage * 100).stringWithDecimals() + " %",
                points = relativeWeight.stringWithDecimals(),
                totalPoints = grade.weight.stringWithDecimals(),
                isHeader = false,
            )
        }
    }
}

@Composable
private fun GradeRowContent(
    gradeName: String,
    percentage: String,
    points: String,
    totalPoints: String,
    isHeader: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        TextRow(text = gradeName, isHeader = isHeader)
        VerticalDivider()
        TextRow(text = percentage, isHeader = isHeader)
        VerticalDivider()
        TextRow(text = points, isHeader = isHeader)
        VerticalDivider()
        TextRow(text = totalPoints, isHeader = isHeader)
    }
}

@Composable
private fun RowScope.TextRow(
    text: String,
    isHeader: Boolean,
) {
    Text(
        text = text,
        style = if (isHeader) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
        modifier = Modifier.weight(1f).padding(8.dp),
        textAlign = TextAlign.Center,
    )
}
