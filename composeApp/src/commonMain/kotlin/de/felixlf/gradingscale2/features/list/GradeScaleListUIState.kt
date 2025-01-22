package de.felixlf.gradingscale2.features.list

import de.felixlf.gradingscale2.entities.models.GradeScale
import kotlinx.collections.immutable.ImmutableList

data class GradeScaleListUIState(
    val selectedGradeScale: GradeScale?,
    val gradeScalesNamesWithId: ImmutableList<GradeScaleNameWithId>,
) {

    data class GradeScaleNameWithId(
        val gradeScaleName: String,
        val gradeScaleId: String,
    )
}
