package de.felixlf.gradingscale2.entities.features.export

import de.felixlf.gradingscale2.entities.models.ExamTableRow
import de.felixlf.gradingscale2.entities.models.TableExportOptions
import de.felixlf.gradingscale2.entities.models.TableHeaders
import de.felixlf.gradingscale2.entities.util.stringWithDecimals

/**
 * Default implementation of [GradeScaleTableMarkdownFormatter].
 */
class GradeScaleTableMarkdownFormatterImpl : GradeScaleTableMarkdownFormatter {

    override fun format(
        rows: List<ExamTableRow>,
        options: TableExportOptions,
        headers: TableHeaders,
    ): String {
        if (rows.isEmpty()) return ""

        val sb = StringBuilder()

        // Header
        sb.append("| ").append(headers.gradeTitle.escapeMarkdown()).append(" | ").append(headers.pointsTitle.escapeMarkdown())
        if (options.includePercentage) {
            sb.append(" | ").append(headers.percentageTitle.escapeMarkdown())
        }
        sb.append(" |\n")

        // Separator
        sb.append("| :---: | :---: |")
        if (options.includePercentage) {
            sb.append(" :---: |")
        }
        sb.append("\n")

        // Rows
        rows.forEach { row ->
            val pointsText = if (options.showAsRange) {
                "${row.minPoints.stringWithDecimals()} – ${row.maxPoints.stringWithDecimals()}"
            } else {
                "≥ ${row.minPoints.stringWithDecimals()}"
            }

            sb.append("| ").append(row.gradeName.escapeMarkdown()).append(" | ").append(pointsText)
            if (options.includePercentage) {
                val percentageText = if (options.showAsRange) {
                    "${(row.minPercentage * 100).stringWithDecimals()}% – ${(row.maxPercentage * 100).stringWithDecimals()}%"
                } else {
                    "≥ ${(row.minPercentage * 100).stringWithDecimals()}%"
                }
                sb.append(" | ").append(percentageText)
            }
            sb.append(" |\n")
        }

        return sb.toString().trimEnd('\n')
    }

    private fun String.escapeMarkdown(): String =
        replace("\\", "\\\\")
            .replace("|", "\\|")
            .replace("\r", " ")
            .replace("\n", " ")
}
