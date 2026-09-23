package de.felixlf.gradingscale2.features.list.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.felixlf.gradingscale2.entities.models.GradeScaleNameAndId
import de.felixlf.gradingscale2.features.calculator.CalculatorTextField
import de.felixlf.gradingscale2.features.list.GradeScaleListDialogCommand
import de.felixlf.gradingscale2.theme.AppTheme
import de.felixlf.gradingscale2.uicomponents.GradeScaleSelectorDropdown
import gradingscale2.entities.generated.resources.Res
import gradingscale2.entities.generated.resources.gradescale_list_total_points
import gradingscale2.entities.generated.resources.gradescale_list_view_mode_grid
import gradingscale2.entities.generated.resources.gradescale_list_view_mode_list
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

/**
 * Header component for GradeScaleListScreen.
 *
 * Adaptive layout:
 * - Wide/Landscape/Desktop (isListPaneVisible == true):
 *   Single compact horizontal row combining scale title, Total Points input, view mode toggle, and actions menu.
 * - Compact/Portrait (isListPaneVisible == false):
 *   Two tightly packed rows: Row 1 has the dropdown selector + toggle + actions menu; Row 2 has the Total Points input.
 */
@Composable
fun GradeScaleListHeader(
    isListPaneVisible: Boolean,
    gradeScaleItems: PersistentList<GradeScaleNameAndId>,
    selectedGradeScaleId: String?,
    selectedGradeScaleName: String,
    totalPointsState: TextFieldState,
    isGridView: Boolean,
    onToggleViewMode: () -> Unit,
    onSelectGradeScaleId: (String?) -> Unit,
    onOpenDialog: (GradeScaleListDialogCommand) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        if (isListPaneVisible) {
            // Single-row horizontal layout for landscape / wide screen
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = selectedGradeScaleName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )

                Spacer(modifier = Modifier.width(10.dp))

                CalculatorTextField(
                    modifier = Modifier.width(115.dp),
                    state = totalPointsState,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                    label = stringResource(Res.string.gradescale_list_total_points),
                )

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(onClick = onToggleViewMode) {
                    Icon(
                        imageVector = if (isGridView) Icons.AutoMirrored.Filled.ViewList else Icons.Default.GridView,
                        contentDescription = stringResource(
                            if (isGridView) Res.string.gradescale_list_view_mode_list else Res.string.gradescale_list_view_mode_grid,
                        ),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }

                selectedGradeScaleId?.let { id ->
                    DialogActionsMenu(
                        gradeScaleId = id,
                        onAction = onOpenDialog,
                    )
                }
            }
        } else {
            // Tightly packed 2-row layout for portrait / phone mode
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    GradeScaleSelectorDropdown(
                        items = gradeScaleItems,
                        selectedItemId = selectedGradeScaleId,
                        onSelectionChange = onSelectGradeScaleId,
                        modifier = Modifier.weight(1f),
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(onClick = onToggleViewMode) {
                        Icon(
                            imageVector = if (isGridView) Icons.AutoMirrored.Filled.ViewList else Icons.Default.GridView,
                            contentDescription = stringResource(
                                if (isGridView) Res.string.gradescale_list_view_mode_list else Res.string.gradescale_list_view_mode_grid,
                            ),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }

                    selectedGradeScaleId?.let { id ->
                        DialogActionsMenu(
                            gradeScaleId = id,
                            onAction = onOpenDialog,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                CalculatorTextField(
                    modifier = Modifier.fillMaxWidth(),
                    state = totalPointsState,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                    label = stringResource(Res.string.gradescale_list_total_points),
                )
            }
        }
    }
}

@Preview
@Composable
private fun GradeScaleListHeaderPortraitPreview() = AppTheme {
    Column(modifier = Modifier.padding(16.dp)) {
        GradeScaleListHeader(
            isListPaneVisible = false,
            gradeScaleItems = persistentListOf(
                GradeScaleNameAndId(name = "Standard (A-F)", id = "1"),
                GradeScaleNameAndId(name = "Hamburg STS", id = "2"),
            ),
            selectedGradeScaleId = "1",
            selectedGradeScaleName = "Standard (A-F)",
            totalPointsState = TextFieldState("100"),
            isGridView = false,
            onToggleViewMode = {},
            onSelectGradeScaleId = {},
            onOpenDialog = {},
        )
    }
}

@Preview
@Composable
private fun GradeScaleListHeaderLandscapePreview() = AppTheme {
    Column(modifier = Modifier.padding(16.dp)) {
        GradeScaleListHeader(
            isListPaneVisible = true,
            gradeScaleItems = persistentListOf(
                GradeScaleNameAndId(name = "Standard (A-F)", id = "1"),
                GradeScaleNameAndId(name = "Hamburg STS", id = "2"),
            ),
            selectedGradeScaleId = "1",
            selectedGradeScaleName = "Standard (A-F)",
            totalPointsState = TextFieldState("100"),
            isGridView = false,
            onToggleViewMode = {},
            onSelectGradeScaleId = {},
            onOpenDialog = {},
        )
    }
}
