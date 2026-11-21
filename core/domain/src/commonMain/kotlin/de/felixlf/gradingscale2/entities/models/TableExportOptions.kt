package de.felixlf.gradingscale2.entities.models

/**
 * Configuration options for exporting a grade scale table for exams.
 *
 * @property includePercentage Whether to include a percentage column.
 * @property showAsRange If true, displays point and percentage intervals (e.g., "22.5 – 25").
 *                       If false, displays minimum threshold (e.g., "≥ 22.5").
 * @property sortDescending If true, highest grades appear first (e.g., A -> F or 10 -> 0).
 *                          If false, lowest grades appear first.
 */
data class TableExportOptions(
    val includePercentage: Boolean = true,
    val showAsRange: Boolean = false,
    val sortDescending: Boolean = true,
)
