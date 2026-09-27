package de.felixlf.gradingscale2.entities.models

/**
 * Calculated row data for the exam table.
 */
data class ExamTableRow(
    val gradeName: String,
    val minPoints: Double,
    val maxPoints: Double,
    val minPercentage: Double,
    val maxPercentage: Double,
)
