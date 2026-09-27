package de.felixlf.gradingscale2.features.list.exportdialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import de.felixlf.gradingscale2.entities.features.export.ExportExamTableUIState
import de.felixlf.gradingscale2.theme.AppTheme
import gradingscale2.entities.generated.resources.Res
import gradingscale2.entities.generated.resources.gradescale_export_dialog_title
import gradingscale2.entities.generated.resources.gradescale_export_preview_title
import org.jetbrains.compose.resources.stringResource

/**
 * Stateless dialog UI layout for exam table export.
 */
@Composable
internal fun ExportExamTableDialogContent(
    uiState: ExportExamTableUIState,
    onTogglePercentage: () -> Unit,
    onToggleRange: () -> Unit,
    onToggleSortDescending: () -> Unit,
    onCopyWord: () -> Unit,
    onCopyTsv: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        modifier = modifier.fillMaxWidth(),
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.TableChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        title = {
            Text(stringResource(Res.string.gradescale_export_dialog_title), style = MaterialTheme.typography.headlineSmall)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                if (uiState.subtitleText.isNotEmpty()) {
                    Text(
                        text = uiState.subtitleText,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                ExportOptionsChips(
                    includePercentage = uiState.includePercentage,
                    showAsRange = uiState.showAsRange,
                    sortDescending = uiState.sortDescending,
                    onTogglePercentage = onTogglePercentage,
                    onToggleRange = onToggleRange,
                    onToggleSortDescending = onToggleSortDescending,
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = stringResource(Res.string.gradescale_export_preview_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(6.dp))

                ExportPreviewTable(
                    rows = uiState.rows,
                    headers = uiState.headers,
                    includePercentage = uiState.includePercentage,
                    showAsRange = uiState.showAsRange,
                )
            }
        },
        confirmButton = {
            ExportConfirmButton(enabled = uiState.canExport, onCopyWord = onCopyWord)
        },
        dismissButton = {
            ExportDismissButtons(enabled = uiState.canExport, onCopyTsv = onCopyTsv, onDismiss = onDismiss)
        },
    )
}

@Preview
@Composable
private fun ExportExamTableDialogContentPreview(
    @PreviewParameter(ExportExamTablePreviewParameterProvider::class) state: ExportExamTableUIState,
) = AppTheme {
    ExportExamTableDialogContent(
        uiState = state,
        onTogglePercentage = {},
        onToggleRange = {},
        onToggleSortDescending = {},
        onCopyWord = {},
        onCopyTsv = {},
        onDismiss = {},
    )
}
