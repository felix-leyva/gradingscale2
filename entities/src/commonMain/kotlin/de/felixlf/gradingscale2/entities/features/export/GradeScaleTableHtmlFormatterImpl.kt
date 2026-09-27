package de.felixlf.gradingscale2.entities.features.export

import de.felixlf.gradingscale2.entities.models.ExamTableRow
import de.felixlf.gradingscale2.entities.models.TableExportOptions
import de.felixlf.gradingscale2.entities.models.TableHeaders
import de.felixlf.gradingscale2.entities.util.stringWithDecimals

/**
 * Default implementation of [GradeScaleTableHtmlFormatter].
 */
class GradeScaleTableHtmlFormatterImpl : GradeScaleTableHtmlFormatter {

    override fun format(
        rows: List<ExamTableRow>,
        options: TableExportOptions,
        headers: TableHeaders,
    ): String {
        if (rows.isEmpty()) return ""

        val sb = StringBuilder()
        sb.append("<table border=\"1\" cellpadding=\"6\" cellspacing=\"0\" style=\"border-collapse: collapse; font-family: Calibri, Arial, sans-serif; font-size: 11pt; text-align: center; border: 1px solid #999999;\">\n")

        // Header
        sb.append("  <thead>\n")
        sb.append("    <tr style=\"background-color: #f2f2f2;\">\n")
        sb.append("      <th style=\"border: 1px solid #999999; padding: 6px 14px; font-weight: bold; text-align: center;\">${headers.gradeTitle.escapeHtml()}</th>\n")
        sb.append("      <th style=\"border: 1px solid #999999; padding: 6px 14px; font-weight: bold; text-align: center;\">${headers.pointsTitle.escapeHtml()}</th>\n")
        if (options.includePercentage) {
            sb.append("      <th style=\"border: 1px solid #999999; padding: 6px 14px; font-weight: bold; text-align: center;\">${headers.percentageTitle.escapeHtml()}</th>\n")
        }
        sb.append("    </tr>\n")
        sb.append("  </thead>\n")

        // Body
        sb.append("  <tbody>\n")
        rows.forEach { row ->
            val pointsText = if (options.showAsRange) {
                "${row.minPoints.stringWithDecimals()} – ${row.maxPoints.stringWithDecimals()}"
            } else {
                "≥ ${row.minPoints.stringWithDecimals()}"
            }

            sb.append("    <tr>\n")
            sb.append("      <td style=\"border: 1px solid #999999; padding: 6px 14px; font-weight: bold; text-align: center;\">${row.gradeName.escapeHtml()}</td>\n")
            sb.append("      <td style=\"border: 1px solid #999999; padding: 6px 14px; text-align: center;\">$pointsText</td>\n")
            if (options.includePercentage) {
                val percentageText = if (options.showAsRange) {
                    "${(row.minPercentage * 100).stringWithDecimals()}% – ${(row.maxPercentage * 100).stringWithDecimals()}%"
                } else {
                    "≥ ${(row.minPercentage * 100).stringWithDecimals()}%"
                }
                sb.append("      <td style=\"border: 1px solid #999999; padding: 6px 14px; text-align: center;\">$percentageText</td>\n")
            }
            sb.append("    </tr>\n")
        }
        sb.append("  </tbody>\n")
        sb.append("</table>")

        return sb.toString()
    }

    private fun String.escapeHtml(): String =
        replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
}
