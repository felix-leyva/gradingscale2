package de.felixlf.gradingscale2.features.list.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import de.felixlf.gradingscale2.features.list.GradeScaleListDialogCommand
import de.felixlf.gradingscale2.theme.AppTheme
import gradingscale2.entities.generated.resources.Res
import gradingscale2.entities.generated.resources.gradescale_list_menu_add_new_grade
import gradingscale2.entities.generated.resources.gradescale_list_menu_add_new_grade_scale
import gradingscale2.entities.generated.resources.gradescale_list_menu_delete_grade_scale
import gradingscale2.entities.generated.resources.gradescale_list_menu_edit_grade_scale
import gradingscale2.entities.generated.resources.gradescale_list_menu_export_exam_table
import org.jetbrains.compose.resources.stringResource

/**
 * Row layout displaying grade scale action buttons directly for wide / extended screens.
 */
@Composable
internal fun DialogActionsRow(
    gradeScaleId: String,
    onAction: (GradeScaleListDialogCommand) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ActionIconButton(
            icon = Icons.Default.TableChart,
            tooltipText = stringResource(Res.string.gradescale_list_menu_export_exam_table),
            onClick = { onAction(GradeScaleListDialogCommand.ExportExamTable(gradeScaleId)) },
            tint = MaterialTheme.colorScheme.primary,
        )

        ActionIconButton(
            icon = Icons.Default.Add,
            tooltipText = stringResource(Res.string.gradescale_list_menu_add_new_grade),
            onClick = { onAction(GradeScaleListDialogCommand.AddNewGradeInCurrentGradeScale(gradeScaleId)) },
        )

        ActionIconButton(
            icon = Icons.Default.Edit,
            tooltipText = stringResource(Res.string.gradescale_list_menu_edit_grade_scale),
            onClick = { onAction(GradeScaleListDialogCommand.EditGradeScale(gradeScaleId)) },
        )

        ActionIconButton(
            icon = Icons.Default.CreateNewFolder,
            tooltipText = stringResource(Res.string.gradescale_list_menu_add_new_grade_scale),
            onClick = { onAction(GradeScaleListDialogCommand.AddNewGradeScale) },
        )

        ActionIconButton(
            icon = Icons.Default.Delete,
            tooltipText = stringResource(Res.string.gradescale_list_menu_delete_grade_scale),
            onClick = { onAction(GradeScaleListDialogCommand.DeleteGradeScale(gradeScaleId)) },
            tint = MaterialTheme.colorScheme.error,
        )
    }
}

@Preview
@Composable
private fun DialogActionsRowPreview() = AppTheme {
    DialogActionsRow(gradeScaleId = "test-id", onAction = {})
}
