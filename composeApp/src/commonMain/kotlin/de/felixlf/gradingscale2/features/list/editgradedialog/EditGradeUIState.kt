package de.felixlf.gradingscale2.features.list.editgradedialog

import de.felixlf.gradingscale2.entities.models.Grade
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf

data class EditGradeUIState(
    val name: String?,
    val percentage: String?,
    val grade: Grade?,
    val error: ImmutableSet<Error> = persistentSetOf(),
) {
    val isLoading: Boolean = grade == null
    val isSaveButtonEnabled: Boolean =
        error.isEmpty() && (grade?.percentage ?: -1.0) in 0.0..100.0 && grade?.namedGrade?.isNotBlank() ?: false

    enum class Error {
        INVALID_PERCENTAGE, INVALID_NAME,
    }
}
