package de.felixlf.gradingscale2.features.list.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import de.felixlf.gradingscale2.features.list.GradeScaleListDialogCommand
import de.felixlf.gradingscale2.theme.AppTheme

/**
 * Layout coordinator for grade scale dialog actions.
 * Automatically adapts between an extended row and a compact dropdown menu.
 */
@Composable
fun DialogActionsMenu(
    gradeScaleId: String,
    modifier: Modifier = Modifier,
    isExtended: Boolean = false,
    onAction: (GradeScaleListDialogCommand) -> Unit = {},
) {
    if (isExtended) {
        DialogActionsRow(
            gradeScaleId = gradeScaleId,
            onAction = onAction,
            modifier = modifier,
        )
    } else {
        DialogActionsDropdown(
            gradeScaleId = gradeScaleId,
            onAction = onAction,
            modifier = modifier,
        )
    }
}

@Preview
@Composable
private fun DialogActionsMenuExtendedPreview() = AppTheme {
    DialogActionsMenu(gradeScaleId = "test-id", isExtended = true)
}

@Preview
@Composable
private fun DialogActionsMenuCompactPreview() = AppTheme {
    DialogActionsMenu(gradeScaleId = "test-id", isExtended = false)
}
