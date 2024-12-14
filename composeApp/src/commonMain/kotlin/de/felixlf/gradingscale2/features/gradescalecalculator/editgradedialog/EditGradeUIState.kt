package de.felixlf.gradingscale2.features.gradescalecalculator.editgradedialog

import de.felixlf.gradingscale2.entities.models.Grade

data class EditGradeUIState(
    val grade: Grade?
) {
    val isLoading: Boolean = grade == null
    val isSaveButtonEnabled: Boolean =
        (grade?.percentage ?: -1.0) in 0.0..100.0 && grade?.namedGrade?.isNotBlank() ?: false
}
