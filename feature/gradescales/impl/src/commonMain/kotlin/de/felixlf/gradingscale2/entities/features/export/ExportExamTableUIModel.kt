package de.felixlf.gradingscale2.entities.features.export

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.felixlf.gradingscale2.entities.models.ExamTableRow
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.models.TableExportOptions
import de.felixlf.gradingscale2.entities.models.TableHeaders
import de.felixlf.gradingscale2.entities.uimodel.StateProducer
import de.felixlf.gradingscale2.entities.uimodel.UIModel
import de.felixlf.gradingscale2.entities.usecases.ShowSnackbarUseCase
import de.felixlf.gradingscale2.core.ui.resources.Res
import de.felixlf.gradingscale2.core.ui.resources.gradescale_export_error_copying
import de.felixlf.gradingscale2.core.ui.resources.gradescale_export_success_table_copied
import de.felixlf.gradingscale2.core.ui.resources.gradescale_export_success_text_copied
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource

/**
 * UI State Factory for the Export Exam Table dialog.
 */
internal class ExportExamTableUIModel(
    override val stateProducer: StateProducer,
    private val examTableDataBuilder: ExamTableDataBuilder,
    private val htmlFormatter: GradeScaleTableHtmlFormatter,
    private val tsvFormatter: GradeScaleTableTsvFormatter,
    private val tableClipboardExporter: TableClipboardExporter,
    private val showSnackbarUseCase: ShowSnackbarUseCase,
) : UIModel<ExportExamTableUIState, ExportExamTableUIEvent> {

    private var gradeScale by mutableStateOf<GradeScale?>(null)
    private var totalPoints by mutableStateOf(10.0)
    private var options by mutableStateOf(TableExportOptions())
    private var headers by mutableStateOf(TableHeaders())
    private var isCopiedDismiss by mutableStateOf(false)

    override val uiState: StateFlow<ExportExamTableUIState> by stateProducer {
        val rows = gradeScale?.let { scale ->
            examTableDataBuilder.buildRows(scale, options, totalPoints).toImmutableList()
        } ?: persistentListOf()

        ExportExamTableUIState(
            gradeScale = gradeScale,
            totalPoints = totalPoints,
            options = options,
            headers = headers,
            rows = rows,
            isCopiedDismiss = isCopiedDismiss,
        )
    }

    override fun sendCommand(command: ExportExamTableUIEvent) {
        when (command) {
            is ExportExamTableUIEvent.SetGradeScale -> {
                gradeScale = command.gradeScale
                if (command.gradeScale.totalPoints.isFinite() && command.gradeScale.totalPoints > 0.0 && totalPoints == 10.0) {
                    totalPoints = command.gradeScale.totalPoints
                }
            }
            is ExportExamTableUIEvent.SetTotalPoints -> if (command.points.isFinite() && command.points > 0.0) totalPoints = command.points
            is ExportExamTableUIEvent.SetHeaders -> headers = command.headers
            ExportExamTableUIEvent.ToggleIncludePercentage -> {
                options = options.copy(includePercentage = !options.includePercentage)
            }
            ExportExamTableUIEvent.ToggleShowAsRange -> {
                options = options.copy(showAsRange = !options.showAsRange)
            }
            ExportExamTableUIEvent.ToggleSortDescending -> {
                options = options.copy(sortDescending = !options.sortDescending)
            }
            ExportExamTableUIEvent.CopyWordTable -> copyTable(
                successMessage = Res.string.gradescale_export_success_table_copied,
                buildHtml = htmlFormatter::format,
            )
            ExportExamTableUIEvent.CopyTsvTable -> copyTable(
                successMessage = Res.string.gradescale_export_success_text_copied,
                buildHtml = { _, _, _ -> "" },
            )
            ExportExamTableUIEvent.DismissHandled -> isCopiedDismiss = false
        }
    }

    private fun copyTable(
        successMessage: StringResource,
        buildHtml: (List<ExamTableRow>, TableExportOptions, TableHeaders) -> String,
    ) {
        val currentScale = gradeScale ?: return
        val snapshotTotalPoints = totalPoints
        val snapshotOptions = options
        val snapshotHeaders = headers
        val currentRows = examTableDataBuilder.buildRows(currentScale, snapshotOptions, snapshotTotalPoints)
        if (currentRows.isEmpty()) return
        scope.launch {
            val html = buildHtml(currentRows, snapshotOptions, snapshotHeaders)
            val tsv = tsvFormatter.format(currentRows, snapshotOptions, snapshotHeaders)
            val success = tableClipboardExporter.copyTableToClipboard(html, tsv)
            if (success) {
                isCopiedDismiss = true
                showSnackbarUseCase(
                    message = successMessage,
                    actionLabel = null,
                    duration = null,
                )
            } else {
                showSnackbarUseCase(
                    message = Res.string.gradescale_export_error_copying,
                    actionLabel = null,
                    duration = null,
                )
            }
        }
    }
}
