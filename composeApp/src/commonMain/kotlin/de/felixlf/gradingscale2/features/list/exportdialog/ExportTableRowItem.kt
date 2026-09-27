package de.felixlf.gradingscale2.features.list.exportdialog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.felixlf.gradingscale2.entities.models.ExamTableRow
import de.felixlf.gradingscale2.entities.util.stringWithDecimals
import de.felixlf.gradingscale2.theme.AppTheme
import de.felixlf.gradingscale2.theme.gradeColors

/**
 * Single data row inside the exam table preview.
 */
@Composable
internal fun ExportTableRowItem(
    row: ExamTableRow,
    showAsRange: Boolean,
    includePercentage: Boolean,
    modifier: Modifier = Modifier,
) {
    val gradeColor = MaterialTheme.gradeColors.forPercentage(row.minPercentage)
    val pointsText = if (showAsRange) {
        "${row.minPoints.stringWithDecimals()} – ${row.maxPoints.stringWithDecimals()}"
    } else {
        "≥ ${row.minPoints.stringWithDecimals()}"
    }
    val percentageText = if (showAsRange) {
        "${(row.minPercentage * 100).stringWithDecimals()}% – ${(row.maxPercentage * 100).stringWithDecimals()}%"
    } else {
        "≥ ${(row.minPercentage * 100).stringWithDecimals()}%"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.weight(1.2f),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = gradeColor.badgeColor,
                modifier = Modifier.padding(2.dp),
            ) {
                Text(
                    text = row.gradeName,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = gradeColor.onBadgeColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Text(
            text = pointsText,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1.5f),
            textAlign = TextAlign.Center,
        )

        if (includePercentage) {
            Text(
                text = percentageText,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1.2f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview
@Composable
private fun ExportTableRowItemPreview() = AppTheme {
    ExportTableRowItem(
        row = ExamTableRow("1", 22.5, 25.0, 0.9, 1.0),
        showAsRange = true,
        includePercentage = true,
    )
}
