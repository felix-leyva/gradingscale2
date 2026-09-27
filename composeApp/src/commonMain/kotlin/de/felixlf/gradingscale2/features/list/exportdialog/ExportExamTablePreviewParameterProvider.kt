package de.felixlf.gradingscale2.features.list.exportdialog

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.felixlf.gradingscale2.entities.features.export.ExamTableDataBuilderImpl
import de.felixlf.gradingscale2.entities.features.export.ExportExamTableUIState
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.models.TableExportOptions
import de.felixlf.gradingscale2.entities.models.TableHeaders
import de.felixlf.gradingscale2.entities.util.MockGradeScalesGenerator
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/**
 * PreviewParameterProvider generating diverse [ExportExamTableUIState] instances
 * for previewing the export dialog and its subcomponents.
 */
class ExportExamTablePreviewParameterProvider : PreviewParameterProvider<ExportExamTableUIState> {

    companion object {
        private val builder = ExamTableDataBuilderImpl()
        private val sampleScale: GradeScale by lazy { MockGradeScalesGenerator().gradeScales.first() }

        private fun createPreviewState(
            gradeScale: GradeScale?,
            totalPoints: Double,
            options: TableExportOptions,
            headers: TableHeaders,
        ): ExportExamTableUIState {
            val rows = gradeScale?.let {
                builder.buildRows(it, options, totalPoints).toImmutableList()
            } ?: persistentListOf()
            return ExportExamTableUIState(
                gradeScale = gradeScale,
                totalPoints = totalPoints,
                options = options,
                headers = headers,
                rows = rows,
            )
        }
    }

    override val values: Sequence<ExportExamTableUIState> = sequenceOf(
        createPreviewState(
            gradeScale = sampleScale,
            totalPoints = 25.0,
            options = TableExportOptions(includePercentage = true, showAsRange = false, sortDescending = true),
            headers = TableHeaders("Grade", "Points Needed", "Min %"),
        ),
        createPreviewState(
            gradeScale = sampleScale,
            totalPoints = 50.0,
            options = TableExportOptions(includePercentage = true, showAsRange = true, sortDescending = true),
            headers = TableHeaders("Grade", "Points", "% Range"),
        ),
        createPreviewState(
            gradeScale = sampleScale,
            totalPoints = 100.0,
            options = TableExportOptions(includePercentage = true, showAsRange = false, sortDescending = false),
            headers = TableHeaders("Grade", "Min Points", "Min %"),
        ),
        createPreviewState(
            gradeScale = sampleScale,
            totalPoints = 20.0,
            options = TableExportOptions(includePercentage = false, showAsRange = false, sortDescending = true),
            headers = TableHeaders("Grade", "Points", "Percentage"),
        ),
        createPreviewState(
            gradeScale = null,
            totalPoints = 10.0,
            options = TableExportOptions(),
            headers = TableHeaders(),
        ),
    )
}
