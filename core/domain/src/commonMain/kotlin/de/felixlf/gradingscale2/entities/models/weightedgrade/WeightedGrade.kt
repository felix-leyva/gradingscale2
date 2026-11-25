package de.felixlf.gradingscale2.entities.models.weightedgrade

import kotlinx.serialization.Serializable

@Serializable
data class WeightedGrade(
    val percentage: Double,
    val weight: Double,
    val uuid: String,
) {
    init {
        require(percentage.isFinite() && percentage in 0.0..1.0) { "Percentage must be between 0 and 1" }
        require(weight.isFinite() && weight >= 0.0) { "Weight must be finite and non-negative" }
    }
}
