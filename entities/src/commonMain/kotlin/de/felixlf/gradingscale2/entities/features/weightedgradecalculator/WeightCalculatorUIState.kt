package de.felixlf.gradingscale2.entities.features.weightedgradecalculator

import de.felixlf.gradingscale2.entities.models.weightedgrade.WeightedGrade
import de.felixlf.gradingscale2.entities.models.weightedgrade.WeightedGradeSummary
import de.felixlf.gradingscale2.entities.models.weightedgrade.WeightedGradeWithName
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.models.GradeScaleNameAndId
import de.felixlf.gradingscale2.entities.util.stringWithDecimals
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

    private val totalWeight: Double = grades.sumOf { it.weight }

    val weightedGradeSummary = selectedGradeScale?.let { gradeScale ->
        if (grades.isEmpty()) return@let null
        val totalPoints = grades.sumOf { it.percentage * it.weight }
        val weightedPercentage = totalPoints / totalWeight
        val totalGradeName = gradeScale.nameByPercentage(weightedPercentage)
        WeightedGradeSummary(
            totalGradeName = totalGradeName,
            weightedPercentage = "${(weightedPercentage * 100).stringWithDecimals()} %",
            earnedPoints = totalPoints.stringWithDecimals(),
            totalPoints = totalWeight.stringWithDecimals(),
        )
    }

    val weightedGrades = selectedGradeScale?.let { gradeScale ->
        grades.map { grade ->
            val name = gradeScale.nameByPercentage(grade.percentage)
            WeightedGradeWithName(grade = grade, name = name)
        }.toImmutableList()
    } ?: persistentListOf()
}
