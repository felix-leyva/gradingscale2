package de.felixlf.gradingscale2.entities.features.export

import de.felixlf.gradingscale2.entities.models.ExamTableRow
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.models.TableExportOptions

/**
 * Interface for building exam table row models from a grade scale.
 */
interface ExamTableDataBuilder {
    /**
     * Builds the exam table rows based on the grade scale and options.
     *
     * @param gradeScale The source grade scale.
     * @param options The export options controlling sorting and intervals.
     * @param totalPoints Total points for calculation.
     */
    fun buildRows(
        gradeScale: GradeScale,
        options: TableExportOptions,
        totalPoints: Double,
    ): List<ExamTableRow>
}
