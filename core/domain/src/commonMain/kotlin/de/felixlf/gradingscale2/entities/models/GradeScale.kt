package de.felixlf.gradingscale2.entities.models

import de.felixlf.gradingscale2.entities.serializers.PersistentListSerializer
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Represents a grade scale with a list of grades.
 * @property id The unique identifier of the grade scale.
 * @property gradeScaleName The name of the grade scale.
 * @property totalPoints The total points of the grade scale. Must be greater than 0.
 * @param grades The list of [Grade] in the grade scale.
 * @property sortedPointedGrades The list of [PointedGrade] sorted by percentage.
 */
@Serializable
data class GradeScale(
    val id: String,
    val gradeScaleName: String,
    val totalPoints: Double,
    @Serializable(with = PersistentListSerializer::class)
    val grades: PersistentList<Grade>,
) {
    init {
        require(totalPoints > 0) { "Total points must be greater than 0" }
    }

    @Transient
    val sortedGrades = grades.sortedByDescending { it.percentage }.toImmutableList()

    @Transient
    val sortedPointedGrades: ImmutableList<PointedGrade> =
        sortedGrades.map { it.toPointedGrade(totalPoints) }.toImmutableList()

    @Transient
    val gradesNamesList = sortedGrades.map { it.namedGrade }.toImmutableList()

    fun getPercentageOrNull(gradeName: String): Double? =
        sortedGrades.find { it.namedGrade == gradeName }?.percentage

    fun getPercentage(gradeName: String): Double = getPercentageOrNull(gradeName) ?: 0.0

    fun gradeByPercentage(percentage: Double) =
        sortedGrades
            .find { validPercentage(percentage) >= it.percentage }
            ?.toPointedGrade(totalPoints)
            ?: sortedPointedGrades.last()

    fun gradeByPoints(points: Double) =
        (points.coerceIn(0.0, totalPoints) / totalPoints)
            .let(::gradeByPercentage)

    private fun validPercentage(percentage: Double) = percentage.coerceIn(0.0, 1.0)

    fun nameByPercentage(percentage: Double): String =
        gradeByPercentage(percentage).namedGrade
}
