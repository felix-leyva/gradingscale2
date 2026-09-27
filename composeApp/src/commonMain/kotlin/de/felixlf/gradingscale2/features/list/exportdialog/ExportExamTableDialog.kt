package de.felixlf.gradingscale2.features.list.exportdialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.felixlf.gradingscale2.entities.features.export.ExportExamTableUIEvent
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.models.TableHeaders
import de.felixlf.gradingscale2.utils.dialogScopedViewModel
import gradingscale2.entities.generated.resources.Res
import gradingscale2.entities.generated.resources.gradescale_list_column_grade
import gradingscale2.entities.generated.resources.gradescale_list_column_min_percentage
import gradingscale2.entities.generated.resources.gradescale_list_column_points_needed
import org.jetbrains.compose.resources.stringResource

/**
 * Stateful dialog allowing teachers to configure and export exam grade scale tables.
 */
@Composable
fun ExportExamTableDialog(
    gradeScale: GradeScale,
    totalPoints: Double,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExportExamTableViewModel = dialogScopedViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val gradeHeader = stringResource(Res.string.gradescale_list_column_grade)
    val pointsHeader = stringResource(Res.string.gradescale_list_column_points_needed)
    val percentageHeader = stringResource(Res.string.gradescale_list_column_min_percentage)

    LaunchedEffect(gradeScale, totalPoints, gradeHeader, pointsHeader, percentageHeader) {
        viewModel.sendCommand(ExportExamTableUIEvent.SetGradeScale(gradeScale))
        viewModel.sendCommand(ExportExamTableUIEvent.SetTotalPoints(totalPoints))
        viewModel.sendCommand(
            ExportExamTableUIEvent.SetHeaders(TableHeaders(gradeHeader, pointsHeader, percentageHeader)),
        )
    }

    LaunchedEffect(uiState.isCopiedDismiss) {
        if (uiState.isCopiedDismiss) {
            viewModel.sendCommand(ExportExamTableUIEvent.DismissHandled)
            onDismiss()
        }
    }

    ExportExamTableDialogContent(
        uiState = uiState,
        onTogglePercentage = { viewModel.sendCommand(ExportExamTableUIEvent.ToggleIncludePercentage) },
        onToggleRange = { viewModel.sendCommand(ExportExamTableUIEvent.ToggleShowAsRange) },
        onToggleSortDescending = { viewModel.sendCommand(ExportExamTableUIEvent.ToggleSortDescending) },
        onCopyWord = { viewModel.sendCommand(ExportExamTableUIEvent.CopyWordTable) },
        onCopyTsv = { viewModel.sendCommand(ExportExamTableUIEvent.CopyTsvTable) },
        onDismiss = onDismiss,
        modifier = modifier,
    )
}
