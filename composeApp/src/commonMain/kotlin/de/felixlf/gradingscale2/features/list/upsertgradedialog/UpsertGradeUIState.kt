package de.felixlf.gradingscale2.features.list.upsertgradedialog

import de.felixlf.gradingscale2.entities.models.Grade
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf

data class UpsertGradeUIState(
    val name: String?,
    val percentage: String?,
    val grade: Grade?,
    val error: PersistentSet<Error> = persistentSetOf(),
) {
    val isLoading: Boolean = grade == null
    val isSaveButtonEnabled: Boolean =
        error.isEmpty() && (grade?.percentage ?: -1.0) in 0.0..100.0 && grade?.namedGrade?.isNotBlank() ?: false

    enum class Error {
        INVALID_PERCENTAGE, INVALID_NAME, DUPLICATE_NAME, DUPLICATE_PERCENTAGE
    }
}
