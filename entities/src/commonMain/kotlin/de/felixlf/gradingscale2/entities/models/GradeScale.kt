package de.felixlf.gradingscale2.entities.models

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * Represents a grade scale with a list of grades.
 * @property id The unique identifier of the grade scale.
 * @property gradeScaleName The name of the grade scale.
 * @property totalPoints The total points of the grade scale. Must be greater than 0.
 * @param grades The list of [Grade] in the grade scale.
 * @property sortedPointedGrades The list of [PointedGrade] sorted by percentage.
 */
data class GradeScale(
    val id: String,
    val gradeScaleName: String,
    val totalPoints: Double,
    val grades: ImmutableList<Grade>,
) {
    init {
        require(totalPoints > 0) { "Total points must be greater than 0" }
    }

    val sortedGrades = grades.sortedByDescending { it.percentage }.toImmutableList()
    val sortedPointedGrades: ImmutableList<PointedGrade> =
        sortedGrades.map { it.toPointedGrade(totalPoints) }.toImmutableList()

    val gradesNamesList = sortedGrades.map { it.namedGrade }.toImmutableList()

    fun getPercentage(gradeName: String): Double = sortedGrades.find {
        it.namedGrade == gradeName
    }?.percentage ?: 0.0

    fun gradeByPercentage(percentage: Double) =
        sortedGrades
            .find { validPercentage(percentage) >= it.percentage }
            ?.toPointedGrade(totalPoints)
            ?: sortedPointedGrades.last()

    fun gradeByPoints(points: Double) =
        (points.coerceIn(0.0, totalPoints) / totalPoints)
            .let(::gradeByPercentage)

    private fun validPercentage(percentage: Double) = percentage.coerceIn(0.0, 1.0)
}
