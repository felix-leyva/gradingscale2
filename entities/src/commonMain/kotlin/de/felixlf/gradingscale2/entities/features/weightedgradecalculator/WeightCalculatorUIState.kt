package de.felixlf.gradingscale2.entities.features.weightedgradecalculator

import androidx.compose.runtime.Stable
import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.models.GradeScaleNameAndId
import de.felixlf.gradingscale2.entities.models.weightedgrade.WeightedGrade
import de.felixlf.gradingscale2.entities.models.weightedgrade.WeightedGradeSummary
import de.felixlf.gradingscale2.entities.models.weightedgrade.WeightedGradeWithName
import de.felixlf.gradingscale2.entities.util.stringWithDecimals
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Stable
class WeightCalculatorUIState(
    val gradeScaleNameAndIds: ImmutableList<GradeScaleNameAndId>,
    val selectedGradeScale: GradeScale?,
    val grades: ImmutableList<WeightedGrade>,
    val selectedGrade: WeightedGrade?,
) {
    val isLoading: Boolean = gradeScaleNameAndIds.isEmpty()

    val totalWeight: Double = grades.sumOf { it.weight }

    val weightedGradeSummary = selectedGradeScale?.let { gradeScale ->
        if (grades.isEmpty()) return@let null
        val totalPoints = grades.sumOf { it.percentage * it.weight }
        val weightedPercentage = if (totalWeight > 0.0) totalPoints / totalWeight else 0.0
        val totalGradeName = gradeScale.nameByPercentage(weightedPercentage)
        WeightedGradeSummary(
            totalGradeName = totalGradeName,
            weightedPercentage = "${(weightedPercentage * 100).stringWithDecimals()} %",
            earnedPoints = totalPoints.stringWithDecimals(),
            totalPoints = totalWeight.stringWithDecimals(),
            weightedPercentageDouble = weightedPercentage,
        )
    }

    val weightedGrades = selectedGradeScale?.let { gradeScale ->
        grades.map { grade ->
            val name = gradeScale.nameByPercentage(grade.percentage)
            WeightedGradeWithName(grade = grade, name = name)
        }.toImmutableList()
    } ?: persistentListOf()
}
