package de.felixlf.gradingscale2.entities.features.export

import de.felixlf.gradingscale2.entities.models.ExamTableRow
import de.felixlf.gradingscale2.entities.models.TableExportOptions
import de.felixlf.gradingscale2.entities.models.TableHeaders
import de.felixlf.gradingscale2.entities.util.stringWithDecimals

/**
 * Default implementation of [GradeScaleTableTsvFormatter].
 */
class GradeScaleTableTsvFormatterImpl : GradeScaleTableTsvFormatter {

    override fun format(
        rows: List<ExamTableRow>,
        options: TableExportOptions,
        headers: TableHeaders,
    ): String {
        if (rows.isEmpty()) return ""

        val sb = StringBuilder()

        // Header
        val headerColumns = mutableListOf(headers.gradeTitle.sanitizeTsv(), headers.pointsTitle.sanitizeTsv())
        if (options.includePercentage) {
            headerColumns.add(headers.percentageTitle.sanitizeTsv())
        }
        sb.append(headerColumns.joinToString("\t")).append("\n")

        // Rows
        rows.forEach { row ->
            val pointsText = if (options.showAsRange) {
                "${row.minPoints.stringWithDecimals()} – ${row.maxPoints.stringWithDecimals()}"
            } else {
                "≥ ${row.minPoints.stringWithDecimals()}"
            }

            val rowColumns = mutableListOf(row.gradeName.sanitizeTsv(), pointsText)
            if (options.includePercentage) {
                val percentageText = if (options.showAsRange) {
                    "${(row.minPercentage * 100).stringWithDecimals()}% – ${(row.maxPercentage * 100).stringWithDecimals()}%"
                } else {
                    "≥ ${(row.minPercentage * 100).stringWithDecimals()}%"
                }
                rowColumns.add(percentageText)
            }
            sb.append(rowColumns.joinToString("\t")).append("\n")
        }

        return sb.toString().trimEnd('\n')
    }

    private fun String.sanitizeTsv(): String =
        replace("\t", " ")
            .replace("\r", " ")
            .replace("\n", " ")
}
