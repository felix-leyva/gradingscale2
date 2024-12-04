package de.felixlf.gradingscale2.features.gradescalecalculator

import de.felixlf.gradingscale2.entities.models.GradeScale
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

data class GradeScaleCalculatorUIState(
    val selectedGradeScale: GradeScale?,
    val gradeScalesNamesWithId: ImmutableList<GradeScaleNameWithId>,
) {
    val gradeScalesNames: ImmutableList<String> =
        gradeScalesNamesWithId.map { it.gradeScaleName }.toImmutableList()

    companion object {
        val Initial = GradeScaleCalculatorUIState(
            gradeScalesNamesWithId = persistentListOf(),
            selectedGradeScale = null,
        )
    }

    data class GradeScaleNameWithId(
        val gradeScaleName: String,
        val gradeScaleId: String,
    )
}
