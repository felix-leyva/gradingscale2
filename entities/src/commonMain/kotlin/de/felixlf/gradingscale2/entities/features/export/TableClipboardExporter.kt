package de.felixlf.gradingscale2.entities.features.export

/**
 * Platform abstraction to copy a formatted table to the system clipboard.
 */
interface TableClipboardExporter {

    /**
     * Copies both rich HTML and plain text (TSV) to the clipboard simultaneously.
     * When pasted into Word, Google Docs, or Pages, it appears as an editable table.
     * When pasted into Excel or plain text editors, it falls back to TSV / plain text.
     *
     * @param html The HTML table markup.
     * @param plainText The plain text / TSV fallback.
     * @return True if successful, false otherwise.
     */
    suspend fun copyTableToClipboard(html: String, plainText: String): Boolean
}
