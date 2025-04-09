package de.felixlf.gradingscale2.entities.features.weightedgradecalculator

import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.models.GradeScaleNameAndId
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

class WeightCalculatorUIState(
    val gradeScaleNameAndIds: ImmutableList<GradeScaleNameAndId>,
    val selectedGradeScale: GradeScale?,
    val grades: ImmutableList<WeightedGrade>,
    val selectedGrade: WeightedGrade?,
) {
    val isLoading: Boolean = gradeScaleNameAndIds.isEmpty()

    val totalWeight: Double = grades.sumOf { it.weight }

    val weightedGrades = selectedGradeScale?.let { gradeScale ->
        grades.map { grade ->
            val name = gradeScale.nameByPercentage(grade.percentage)
            WeightedGradeWithName(grade = grade, name = name, weightFromTotal = grade.weight / totalWeight)
        }.toImmutableList()
    } ?: persistentListOf()

    data class WeightedGradeWithName(
        val grade: WeightedGrade,
        val name: String,
        val weightFromTotal: Double,
    ) {
        val percentage: Double = grade.percentage
        val weight: Double = grade.weight
    }

    data class WeightedGrade(
        val percentage: Double,
        val weight: Double,
    )
}
