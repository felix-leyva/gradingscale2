package de.felixlf.gradingscale2.entities.features.export

import de.felixlf.gradingscale2.entities.models.ExamTableRow
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.models.TableExportOptions

/**
 * Default implementation of [ExamTableDataBuilder].
 */
class ExamTableDataBuilderImpl : ExamTableDataBuilder {

    override fun buildRows(
        gradeScale: GradeScale,
        options: TableExportOptions,
        totalPoints: Double,
    ): List<ExamTableRow> {
        val sortedGrades = gradeScale.sortedGrades
        if (sortedGrades.isEmpty()) return emptyList()

        val effectiveTotal = if (totalPoints > 0) totalPoints else gradeScale.totalPoints

        val rows = sortedGrades.mapIndexed { index, grade ->
            val minPercentage = grade.percentage
            val maxPercentage = if (index == 0) maxOf(1.0, grade.percentage) else sortedGrades[index - 1].percentage
            val minPoints = minPercentage * effectiveTotal
            val maxPoints = maxPercentage * effectiveTotal

            ExamTableRow(
                gradeName = grade.namedGrade,
                minPoints = minPoints,
                maxPoints = maxPoints,
                minPercentage = minPercentage,
                maxPercentage = maxPercentage,
            )
        }

        return if (options.sortDescending) {
            rows
        } else {
            rows.reversed()
        }
    }
}
