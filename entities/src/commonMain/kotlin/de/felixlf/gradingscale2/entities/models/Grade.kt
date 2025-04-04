package de.felixlf.gradingscale2.entities.models

import kotlinx.serialization.Serializable

@Serializable
data class Grade(
    val namedGrade: String = "",
    val percentage: Double = 0.0,
    val idOfGradeScale: String = "",
    val nameOfScale: String,
    val uuid: String,
) {
    init {
        require(percentage in 0.0..1.0) { "Percentage must be between 0 and 1" }
    }
}
