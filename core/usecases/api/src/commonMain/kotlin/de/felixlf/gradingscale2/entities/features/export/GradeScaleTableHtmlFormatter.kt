package de.felixlf.gradingscale2.entities.features.export

import de.felixlf.gradingscale2.entities.models.ExamTableRow
import de.felixlf.gradingscale2.entities.models.TableExportOptions
import de.felixlf.gradingscale2.entities.models.TableHeaders

/**
 * Formats exam table rows into an HTML table string.
 */
interface GradeScaleTableHtmlFormatter {
    /**
     * Formats exam table rows into an HTML table string compatible with
     * Microsoft Word, Google Docs, Apple Pages, and LibreOffice.
     */
    fun format(
        rows: List<ExamTableRow>,
        options: TableExportOptions,
        headers: TableHeaders = TableHeaders(),
    ): String
}
