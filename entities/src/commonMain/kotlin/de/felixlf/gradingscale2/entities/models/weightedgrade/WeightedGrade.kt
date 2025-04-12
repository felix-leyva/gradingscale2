package de.felixlf.gradingscale2.entities.models.weightedgrade

data class WeightedGrade(
    val percentage: Double,
    val weight: Double,
    val uuid: String,
) {
    init {
        require(percentage in 0.0..1.0) { "Percentage must be between 0 and 1" }
    }
}
