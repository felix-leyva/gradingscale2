package de.felixlf.gradingscale2.entities.features.export

import de.felixlf.gradingscale2.entities.models.ExamTableRow
import de.felixlf.gradingscale2.entities.models.TableExportOptions
import de.felixlf.gradingscale2.entities.models.TableHeaders

/**
 * Formats exam table rows into a Markdown table string.
 */
interface GradeScaleTableMarkdownFormatter {
    /**
     * Formats exam table rows into a GitHub Flavored Markdown table.
     * Ideal for notes in Obsidian, Notion, GitHub, and markdown documents.
     */
    fun format(
        rows: List<ExamTableRow>,
        options: TableExportOptions,
        headers: TableHeaders = TableHeaders(),
    ): String
}
