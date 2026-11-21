package de.felixlf.gradingscale2.features.list.exportdialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.felixlf.gradingscale2.entities.models.ExamTableRow
import de.felixlf.gradingscale2.entities.models.TableHeaders
import de.felixlf.gradingscale2.theme.AppTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Visual preview table of the exam grading scale with headers and rows.
 */
@Composable
internal fun ExportPreviewTable(
    rows: ImmutableList<ExamTableRow>,
    headers: TableHeaders,
    includePercentage: Boolean,
    showAsRange: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column {
            // Header row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = headers.gradeTitle,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1.2f),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = headers.pointsTitle,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1.5f),
                    textAlign = TextAlign.Center,
                )
                if (includePercentage) {
                    Text(
                        text = headers.percentageTitle,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1.2f),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Data rows
            rows.forEach { row ->
                ExportTableRowItem(
                    row = row,
                    showAsRange = showAsRange,
                    includePercentage = includePercentage,
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                )
            }
        }
    }
}

@Preview
@Composable
private fun ExportPreviewTablePreview() = AppTheme {
    ExportPreviewTable(
        rows = persistentListOf(
            ExamTableRow("1", 22.5, 25.0, 0.9, 1.0),
            ExamTableRow("2", 20.0, 22.5, 0.8, 0.9),
            ExamTableRow("3", 17.5, 20.0, 0.7, 0.8),
        ),
        headers = TableHeaders("Grade", "Points", "Min %"),
        includePercentage = true,
        showAsRange = false,
    )
}
