package de.felixlf.gradingscale2.features.list.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import org.jetbrains.compose.resources.stringResource

@Composable
fun DialogActionsMenu(
    gradeScaleId: String,
    onAction: (GradeScaleListDialogCommand) -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }
    val menuItems = remember(gradeScaleId) {
        listOf(
            GradeScaleListDialogCommand.AddNewGradeInCurrentGradeScale(gradeScaleId),
            GradeScaleListDialogCommand.EditGradeScale(gradeScaleId),
            GradeScaleListDialogCommand.AddNewGradeScale,
        )
    }

    Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = "Localized description")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            menuItems.forEach { menuItem ->
                menuItem.menuText?.let { text ->
                    DropdownMenuItem(
                        onClick = {
                            onAction(menuItem)
                            expanded = false
                        },
                        text = { Text(stringResource(text), textAlign = TextAlign.Start) },
                    )
                }
            }
        }
    }
}
