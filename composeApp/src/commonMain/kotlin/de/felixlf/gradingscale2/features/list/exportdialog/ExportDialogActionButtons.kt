package de.felixlf.gradingscale2.features.list.exportdialog

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.felixlf.gradingscale2.theme.AppTheme
import gradingscale2.entities.generated.resources.Res
import gradingscale2.entities.generated.resources.gradescale_export_btn_close
import gradingscale2.entities.generated.resources.gradescale_export_btn_copy_tsv
import gradingscale2.entities.generated.resources.gradescale_export_btn_copy_word
import org.jetbrains.compose.resources.stringResource

/**
 * Confirm button for copying formatted HTML table (compatible with Word/Docs).
 */
@Composable
internal fun ExportConfirmButton(
    enabled: Boolean,
    onCopyWord: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onCopyWord,
        enabled = enabled,
        modifier = modifier,
    ) {
        Icon(
            imageVector = Icons.Default.TableChart,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(stringResource(Res.string.gradescale_export_btn_copy_word))
    }
}

/**
 * Dismiss and alternative copy (TSV) buttons.
 */
@Composable
internal fun ExportDismissButtons(
    enabled: Boolean,
    onCopyTsv: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        OutlinedButton(
            onClick = onCopyTsv,
            enabled = enabled,
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(stringResource(Res.string.gradescale_export_btn_copy_tsv))
        }

        Spacer(modifier = Modifier.width(6.dp))

        TextButton(onClick = onDismiss) {
            Text(stringResource(Res.string.gradescale_export_btn_close))
        }
    }
}

@Preview
@Composable
private fun ExportActionButtonsPreview() = AppTheme {
    Row {
        ExportDismissButtons(enabled = true, onCopyTsv = {}, onDismiss = {})
        Spacer(modifier = Modifier.width(8.dp))
        ExportConfirmButton(enabled = true, onCopyWord = {})
    }
}
