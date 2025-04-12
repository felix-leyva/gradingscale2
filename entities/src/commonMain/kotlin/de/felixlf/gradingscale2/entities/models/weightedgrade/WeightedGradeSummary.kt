package de.felixlf.gradingscale2.entities.models.weightedgrade

data class WeightedGradeSummary(
    val totalGradeName: String,
    val weightedPercentage: String,
    val earnedPoints: String,
    val totalPoints: String,
)
