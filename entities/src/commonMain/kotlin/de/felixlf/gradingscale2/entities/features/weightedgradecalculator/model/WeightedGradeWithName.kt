package de.felixlf.gradingscale2.entities.features.weightedgradecalculator.model

data class WeightedGradeWithName(
    val grade: WeightedGrade,
    val name: String,
) {
    val percentage: Double = grade.percentage
    val relativeWeight: Double = grade.weight * percentage
}
