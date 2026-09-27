package de.felixlf.gradingscale2.entities.features.export

import androidx.compose.runtime.Stable
import de.felixlf.gradingscale2.entities.models.ExamTableRow
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.models.TableExportOptions
import de.felixlf.gradingscale2.entities.models.TableHeaders
import de.felixlf.gradingscale2.entities.util.stringWithDecimals
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/**
 * UI State for the Export Exam Table dialog.
 * Computes derived presentation properties from the UI model inputs.
 */
@Stable
data class ExportExamTableUIState(
    val gradeScale: GradeScale? = null,
    val totalPoints: Double = 10.0,
    val options: TableExportOptions = TableExportOptions(),
    val headers: TableHeaders = TableHeaders(),
    val rows: ImmutableList<ExamTableRow> = persistentListOf(),
    val isCopiedDismiss: Boolean = false,
) {
    val hasScale: Boolean = gradeScale != null
    val scaleName: String = gradeScale?.gradeScaleName ?: ""

    val includePercentage: Boolean = options.includePercentage
    val showAsRange: Boolean = options.showAsRange
    val sortDescending: Boolean = options.sortDescending

    val subtitleText: String = if (gradeScale != null) {
        "$scaleName • ${totalPoints.stringWithDecimals()} pts"
    } else {
        ""
    }

    val isEmpty: Boolean = rows.isEmpty()
    val canExport: Boolean = !isEmpty && hasScale
}
