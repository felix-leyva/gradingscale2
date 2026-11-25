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
        require(totalPoints.isFinite() && totalPoints > 0.0) { "Total points must be finite and greater than 0" }
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

    fun gradeByPercentageOrNull(percentage: Double): PointedGrade? {
        if (sortedGrades.isEmpty()) return null
        return sortedGrades
            .find { validPercentage(percentage) >= it.percentage }
            ?.toPointedGrade(totalPoints)
            ?: sortedPointedGrades.lastOrNull()
    }

    fun gradeByPointsOrNull(points: Double): PointedGrade? {
        if (sortedGrades.isEmpty()) return null
        val safePoints = if (points.isFinite()) points.coerceIn(0.0, totalPoints) else 0.0
        return gradeByPercentageOrNull(safePoints / totalPoints)
    }

    fun nameByPercentageOrNull(percentage: Double): String? =
        gradeByPercentageOrNull(percentage)?.namedGrade

    fun gradeByPercentage(percentage: Double): PointedGrade =
        gradeByPercentageOrNull(percentage) ?: throw NoSuchElementException("GradeScale has no grades")

    fun gradeByPoints(points: Double): PointedGrade =
        gradeByPointsOrNull(points) ?: throw NoSuchElementException("GradeScale has no grades")

    private fun validPercentage(percentage: Double): Double =
        if (percentage.isFinite()) percentage.coerceIn(0.0, 1.0) else 0.0

    fun nameByPercentage(percentage: Double): String =
        nameByPercentageOrNull(percentage) ?: ""
}
