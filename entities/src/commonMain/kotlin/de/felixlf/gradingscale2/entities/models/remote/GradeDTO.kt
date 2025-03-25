package de.felixlf.gradingscale2.entities.models.remote

import kotlinx.serialization.Serializable

/**
 * Data class representing a grade.
 * @property gradeName The name of the grade.
 * @property percentage The percentage of the grade.
 */
@Serializable
data class GradeDTO(
    val gradeName: String,
    val percentage: Double,
)
