package de.felixlf.gradingscale2.entities.features.calculator

import de.felixlf.gradingscale2.entities.models.GradeScale
import de.felixlf.gradingscale2.entities.models.toPointedGrade
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

data class GradeScaleCalculatorUIState(
    val selectedGradeScale: GradeScale?,
    val currentPercentage: Double?,
    val totalPoints: Double?,
    val gradeScalesNamesWithId: ImmutableList<GradeScaleNameWithId>,
) {
    val currentGrade = when {
        selectedGradeScale != null && currentPercentage != null && totalPoints != null -> selectedGradeScale.gradeByPercentage(
            percentage = currentPercentage,
        ).grade.copy(percentage = currentPercentage).toPointedGrade(totalPoints)

        else -> null
    }

    val gradeScalesNames: ImmutableList<String> = gradeScalesNamesWithId.map { it.gradeScaleName }.toImmutableList()

    data class GradeScaleNameWithId(
        val gradeScaleName: String,
        val gradeScaleId: String,
    )
}
