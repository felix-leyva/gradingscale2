package de.felixlf.gradingscale2.entities.models.remote

import kotlinx.serialization.Serializable

/**
 * Data class representing a grade scale.
 * @property gradeScaleName The name of the grade scale.
 * @property country The country of the grade scale.
 * @property grades The list of grades in the grade scale.
 */
@Serializable
data class GradeScaleDTO(
    val gradeScaleName: String,
    val country: Country,
    val grades: List<GradeDTO>,
)
