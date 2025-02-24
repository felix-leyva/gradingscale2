package de.felixlf.gradingscale2.features.list.upsertgradedialog

import de.felixlf.gradingscale2.entities.models.Grade
import de.felixlf.gradingscale2.entities.models.GradeScale
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf

data class UpsertGradeUIState(
    val name: String?,
    val percentage: String?,
    val grade: Grade?,
    val gradeScale: GradeScale?,
    val error: PersistentSet<Error> = persistentSetOf(),
) {
    val isLoading: Boolean = grade == null && gradeScale == null
    val isSaveButtonEnabled: Boolean = !isLoading && error.isEmpty() && !name.isNullOrBlank() && !percentage.isNullOrBlank()

    enum class Error {
        INVALID_PERCENTAGE, INVALID_NAME, DUPLICATE_NAME, DUPLICATE_PERCENTAGE
    }
}
