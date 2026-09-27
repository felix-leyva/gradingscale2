package de.felixlf.gradingscale2.features.list.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import de.felixlf.gradingscale2.features.list.GradeScaleListDialogCommand
import de.felixlf.gradingscale2.theme.AppTheme
import gradingscale2.entities.generated.resources.Res
import gradingscale2.entities.generated.resources.gradescale_list_menu_add_new_grade
import gradingscale2.entities.generated.resources.gradescale_list_menu_add_new_grade_scale
import gradingscale2.entities.generated.resources.gradescale_list_menu_delete_grade_scale
import gradingscale2.entities.generated.resources.gradescale_list_menu_edit_grade_scale
import gradingscale2.entities.generated.resources.gradescale_list_menu_export_exam_table
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Compact overflow dropdown menu for grade scale actions (3-dots menu).
 */
@Composable
internal fun DialogActionsDropdown(
    gradeScaleId: String,
    onAction: (GradeScaleListDialogCommand) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.wrapContentSize(Alignment.TopStart)) {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = "Grade scale options")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownItem(
                icon = Icons.Default.TableChart,
                titleRes = Res.string.gradescale_list_menu_export_exam_table,
                onClick = {
                    onAction(GradeScaleListDialogCommand.ExportExamTable(gradeScaleId))
                    expanded = false
                },
                isPrimary = true,
            )
            HorizontalDivider()
            DropdownItem(
                icon = Icons.Default.Add,
                titleRes = Res.string.gradescale_list_menu_add_new_grade,
                onClick = {
                    onAction(GradeScaleListDialogCommand.AddNewGradeInCurrentGradeScale(gradeScaleId))
                    expanded = false
                },
            )
            DropdownItem(
                icon = Icons.Default.Edit,
                titleRes = Res.string.gradescale_list_menu_edit_grade_scale,
                onClick = {
                    onAction(GradeScaleListDialogCommand.EditGradeScale(gradeScaleId))
                    expanded = false
                },
            )
            DropdownItem(
                icon = Icons.Default.CreateNewFolder,
                titleRes = Res.string.gradescale_list_menu_add_new_grade_scale,
                onClick = {
                    onAction(GradeScaleListDialogCommand.AddNewGradeScale)
                    expanded = false
                },
            )
            HorizontalDivider()
            DropdownItem(
                icon = Icons.Default.Delete,
                titleRes = Res.string.gradescale_list_menu_delete_grade_scale,
                onClick = {
                    onAction(GradeScaleListDialogCommand.DeleteGradeScale(gradeScaleId))
                    expanded = false
                },
                isDestructive = true,
            )
        }
    }
}

@Composable
private fun DropdownItem(
    icon: ImageVector,
    titleRes: StringResource,
    onClick: () -> Unit,
    isPrimary: Boolean = false,
    isDestructive: Boolean = false,
) {
    val tint = when {
        isDestructive -> MaterialTheme.colorScheme.error
        isPrimary -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }
    DropdownMenuItem(
        leadingIcon = { Icon(icon, contentDescription = null, tint = tint) },
        onClick = onClick,
        text = {
            Text(
                text = stringResource(titleRes),
                textAlign = TextAlign.Start,
                color = tint,
                fontWeight = if (isPrimary) FontWeight.Medium else FontWeight.Normal,
            )
        },
    )
}

@Preview
@Composable
private fun DialogActionsDropdownPreview() = AppTheme {
    DialogActionsDropdown(gradeScaleId = "test-id", onAction = {})
}
