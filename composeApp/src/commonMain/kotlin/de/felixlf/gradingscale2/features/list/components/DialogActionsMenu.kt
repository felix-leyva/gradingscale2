package de.felixlf.gradingscale2.features.list.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.ui.text.style.TextAlign
import de.felixlf.gradingscale2.features.list.GradeScaleListDialogCommand
import de.felixlf.gradingscale2.theme.AppTheme
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun DialogActionsMenu(
    gradeScaleId: String,
    modifier: Modifier = Modifier,
    onAction: (GradeScaleListDialogCommand) -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.wrapContentSize(Alignment.TopStart)) {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = "Grade scale options")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            // Add Grade
            DropdownMenuItem(
                leadingIcon = {
                    Icon(Icons.Default.Add, contentDescription = null)
                },
                onClick = {
                    onAction(GradeScaleListDialogCommand.AddNewGradeInCurrentGradeScale(gradeScaleId))
                    expanded = false
                },
                text = {
                    GradeScaleListDialogCommand.AddNewGradeInCurrentGradeScale(gradeScaleId).menuText?.let {
                        Text(stringResource(it), textAlign = TextAlign.Start)
                    }
                },
            )

            // Edit Grade Scale
            DropdownMenuItem(
                leadingIcon = {
                    Icon(Icons.Default.Edit, contentDescription = null)
                },
                onClick = {
                    onAction(GradeScaleListDialogCommand.EditGradeScale(gradeScaleId))
                    expanded = false
                },
                text = {
                    GradeScaleListDialogCommand.EditGradeScale(gradeScaleId).menuText?.let {
                        Text(stringResource(it), textAlign = TextAlign.Start)
                    }
                },
            )

            // Add New Grade Scale
            DropdownMenuItem(
                leadingIcon = {
                    Icon(Icons.Default.CreateNewFolder, contentDescription = null)
                },
                onClick = {
                    onAction(GradeScaleListDialogCommand.AddNewGradeScale)
                    expanded = false
                },
                text = {
                    GradeScaleListDialogCommand.AddNewGradeScale.menuText?.let {
                        Text(stringResource(it), textAlign = TextAlign.Start)
                    }
                },
            )

            HorizontalDivider()

            // Delete Grade Scale
            DropdownMenuItem(
                leadingIcon = {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                },
                onClick = {
                    onAction(GradeScaleListDialogCommand.DeleteGradeScale(gradeScaleId))
                    expanded = false
                },
                text = {
                    GradeScaleListDialogCommand.DeleteGradeScale(gradeScaleId).menuText?.let {
                        Text(
                            text = stringResource(it),
                            textAlign = TextAlign.Start,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                },
            )
        }
    }
}

@Preview
@Composable
private fun DialogActionsMenuPreview() = AppTheme {
    DialogActionsMenu(gradeScaleId = "test-id")
}
