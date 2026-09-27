package de.felixlf.gradingscale2.entities.features.export

import de.felixlf.gradingscale2.entities.models.ExamTableRow
import de.felixlf.gradingscale2.entities.models.TableExportOptions
import de.felixlf.gradingscale2.entities.models.TableHeaders

/**
 * Formats exam table rows into Tab-Separated Values (TSV).
 */
interface GradeScaleTableTsvFormatter {
    /**
     * Formats exam table rows into Tab-Separated Values (TSV).
     * Ideal for pasting directly into Excel, Google Sheets, or plain text fallbacks.
     */
    fun format(
        rows: List<ExamTableRow>,
        options: TableExportOptions,
        headers: TableHeaders = TableHeaders(),
    ): String
}
