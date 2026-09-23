package de.felixlf.gradingscale2.features.list.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.felixlf.gradingscale2.entities.features.list.GradeScaleListUIState.GradeListItemUIState
import de.felixlf.gradingscale2.theme.AppTheme
import gradingscale2.entities.generated.resources.Res
import gradingscale2.entities.generated.resources.gradescale_list_column_grade
import gradingscale2.entities.generated.resources.gradescale_list_column_min_percentage
import gradingscale2.entities.generated.resources.gradescale_list_column_points_needed
import gradingscale2.entities.generated.resources.gradescale_list_no_grade_scale_selected
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

/**
 * Responsive content container for grade scale grades.
 *
 * Automatically renders:
 * - List mode: Column headers ("Grade", "Min. %", "Points Needed") + LazyColumn of [GradeScaleListItemCard]
 * - Grid mode: LazyVerticalGrid of [GradeScaleGridItemCard] (2 columns on mobile, adaptive multi-column on desktop/tablet)
 */
@Composable
fun GradeScaleListContent(
    gradeItems: ImmutableList<GradeListItemUIState>,
    isGridView: Boolean,
    isListPaneVisible: Boolean,
    onEditGrade: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (gradeItems.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(Res.string.gradescale_list_no_grade_scale_selected),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        return
    }

    if (isGridView) {
        val gridCells = if (isListPaneVisible) {
            GridCells.Adaptive(minSize = 220.dp)
        } else {
            GridCells.Fixed(2)
        }

        LazyVerticalGrid(
            columns = gridCells,
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 12.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(gradeItems, key = { it.uuid }) { grade ->
                GradeScaleGridItemCard(
                    item = grade,
                    onEdit = { onEditGrade(grade.uuid) },
                )
            }
            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    } else {
        Column(modifier = modifier.fillMaxSize()) {
            // Column Headers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.gradescale_list_column_grade),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(68.dp),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(Res.string.gradescale_list_column_min_percentage),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .width(88.dp),
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = stringResource(Res.string.gradescale_list_column_points_needed),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 36.dp),
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(gradeItems, key = { it.uuid }) { grade ->
                    GradeScaleListItemCard(
                        item = grade,
                        onEdit = { onEditGrade(grade.uuid) },
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun GradeScaleListContentListPreview() = AppTheme {
    val sampleGrades = persistentListOf(
        GradeListItemUIState("1", "A", 0.90, "90 %", 90.0, "90"),
        GradeListItemUIState("2", "B", 0.80, "80 %", 80.0, "80"),
        GradeListItemUIState("3", "C", 0.70, "70 %", 70.0, "70"),
        GradeListItemUIState("4", "D", 0.60, "60 %", 60.0, "60"),
        GradeListItemUIState("5", "F", 0.40, "40 %", 40.0, "40"),
    )
    GradeScaleListContent(
        gradeItems = sampleGrades,
        isGridView = false,
        isListPaneVisible = false,
        onEditGrade = {},
    )
}

@Preview
@Composable
private fun GradeScaleListContentGridPreview() = AppTheme {
    val sampleGrades = persistentListOf(
        GradeListItemUIState("1", "A", 0.90, "90 %", 90.0, "90"),
        GradeListItemUIState("2", "B", 0.80, "80 %", 80.0, "80"),
        GradeListItemUIState("3", "C", 0.70, "70 %", 70.0, "70"),
        GradeListItemUIState("4", "D", 0.60, "60 %", 60.0, "60"),
        GradeListItemUIState("5", "F", 0.40, "40 %", 40.0, "40"),
    )
    GradeScaleListContent(
        gradeItems = sampleGrades,
        isGridView = true,
        isListPaneVisible = false,
        onEditGrade = {},
    )
}
