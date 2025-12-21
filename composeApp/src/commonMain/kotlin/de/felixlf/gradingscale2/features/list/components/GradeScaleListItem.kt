package de.felixlf.gradingscale2.features.list.components

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.felixlf.gradingscale2.entities.models.PointedGrade
import de.felixlf.gradingscale2.entities.util.stringWithDecimals
import de.felixlf.gradingscale2.uicomponents.VerticalDivider

/**
 * A list item that displays a grade with its name, percentage and points.
 */
@Composable
internal fun GradeScaleListItem(
    grade: PointedGrade,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Max),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1f).padding(8.dp),
            text = grade.namedGrade,
            textAlign = TextAlign.Center,
        )
        VerticalDivider()
        Text(
            modifier = Modifier.weight(1f).padding(8.dp),
            text = "${(grade.percentage * 100).stringWithDecimals()} %",
            textAlign = TextAlign.Center,
        )
        VerticalDivider()
        Text(
            modifier = Modifier.weight(1f).padding(8.dp),
            text = grade.points.stringWithDecimals(),
            textAlign = TextAlign.Center,
        )
    }
}
